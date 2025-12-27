"""
RAG Pipeline for Video Knowledge System

Responsibilities:
1. Chunk transcripts (sentence-aware, overlap-based)
2. Generate embeddings (lazy-loaded)
3. Store & retrieve from ChromaDB (persistent)
4. Build clean context for LLMs

Author: Championstein
Date: 2025
"""

import uuid
import logging
import re
from typing import List, Dict, Optional
from pathlib import Path

import chromadb
from chromadb.config import Settings
from sentence_transformers import SentenceTransformer

from .config import config

# --------------------------------------------------
# Logging
# --------------------------------------------------
logger = logging.getLogger(__name__)


class RAGPipeline:
    """
    Core RAG system for video transcripts.
    """

    def __init__(
        self,
        collection_name: Optional[str] = None,
        embedding_model: Optional[str] = None,
        db_path: Optional[Path] = None,
    ):
        self.collection_name = collection_name or config.collection_name
        self.embedding_model_name = embedding_model or config.embedding_model
        self.db_path = db_path or config.vector_db_path

        # Lazy-loaded embedding model
        self._embedder = None

        # Initialize ChromaDB
        logger.info("Initializing ChromaDB at %s", self.db_path)
        self.client = chromadb.PersistentClient(
            path=str(self.db_path),
            settings=Settings(anonymized_telemetry=False),
        )

        self.collection = self.client.get_or_create_collection(
            name=self.collection_name,
            metadata={"hnsw:space": "cosine"},
        )

        logger.info(
            "Collection '%s' ready (%d chunks)",
            self.collection_name,
            self.collection.count(),
        )

    # --------------------------------------------------
    # Embedding (lazy-loaded)
    # --------------------------------------------------
    @property
    def embedder(self) -> SentenceTransformer:
        if self._embedder is None:
            logger.info(
                "Loading embedding model '%s' (one-time)...",
                self.embedding_model_name,
            )
            self._embedder = SentenceTransformer(self.embedding_model_name)
            logger.info(
                "Embedding model loaded (dim=%d)",
                self._embedder.get_sentence_embedding_dimension(),
            )
        return self._embedder

    # --------------------------------------------------
    # Chunking
    # --------------------------------------------------
    def chunk_transcript(
        self,
        transcript: Dict,
        chunk_size: Optional[int] = None,
        overlap: Optional[int] = None,
    ) -> List[Dict]:
        """
        Sentence-aware chunking with overlap.
        Chunk size & overlap are character-based (approx).
        """
        chunk_size = chunk_size or config.chunk_size
        overlap = overlap or config.chunk_overlap

        video_id = transcript.get("video_id", "unknown")
        segments = transcript.get("segments", [])
        text = transcript.get("text", "")

        # Split into sentences
        sentences = re.split(r"(?<=[.!?])\s+", text)
        sentences = [s.strip() for s in sentences if s.strip()]

        chunks = []
        current_sentences = []
        current_len = 0
        segment_idx = 0

        for sentence in sentences:
            sent_len = len(sentence)

            if current_len + sent_len > chunk_size and current_sentences:
                chunk_text = " ".join(current_sentences)

                # Timestamp = first segment in this chunk
                ts_start = (
                    segments[segment_idx]["start"]
                    if segment_idx < len(segments)
                    else None
                )

                chunks.append(
                    {
                        "text": chunk_text,
                        "video_id": video_id,
                        "timestamp_start": ts_start,
                        "char_count": len(chunk_text),
                    }
                )

                # Build overlap
                overlap_chars = 0
                overlap_sentences = []
                for s in reversed(current_sentences):
                    if overlap_chars + len(s) <= overlap:
                        overlap_sentences.insert(0, s)
                        overlap_chars += len(s)
                    else:
                        break

                current_sentences = overlap_sentences + [sentence]
                current_len = sum(len(s) for s in current_sentences)
            else:
                current_sentences.append(sentence)
                current_len += sent_len

            segment_idx = min(segment_idx + 1, len(segments) - 1)

        # Final chunk
        if current_sentences:
            chunk_text = " ".join(current_sentences)
            ts_start = (
                segments[segment_idx]["start"]
                if segment_idx < len(segments)
                else None
            )
            chunks.append(
                {
                    "text": chunk_text,
                    "video_id": video_id,
                    "timestamp_start": ts_start,
                    "char_count": len(chunk_text),
                }
            )

        logger.info(
            "Chunked video '%s' into %d chunks",
            video_id,
            len(chunks),
        )
        return chunks

    # --------------------------------------------------
    # Indexing
    # --------------------------------------------------
    def add_documents(self, transcripts: List[Dict], batch_size: int = 64) -> None:
        if not transcripts:
            logger.warning("No transcripts provided")
            return

        all_chunks = []
        for transcript in transcripts:
            all_chunks.extend(self.chunk_transcript(transcript))

        logger.info("Indexing %d chunks", len(all_chunks))

        for i in range(0, len(all_chunks), batch_size):
            batch = all_chunks[i : i + batch_size]

            texts = [c["text"] for c in batch]
            embeddings = self.embedder.encode(
                texts, convert_to_numpy=True, show_progress_bar=False
            )

            ids = [str(uuid.uuid4()) for _ in batch]

            metadatas = [
                {
                    "video_id": c["video_id"],
                    "timestamp_start": c["timestamp_start"],
                    "char_count": c["char_count"],
                }
                for c in batch
            ]

            self.collection.add(
                ids=ids,
                documents=texts,
                embeddings=embeddings.tolist(),
                metadatas=metadatas,
            )

        logger.info(
            "Indexing complete. Total chunks in DB: %d",
            self.collection.count(),
        )

    # --------------------------------------------------
    # Retrieval
    # --------------------------------------------------
    def retrieve(
        self,
        query: str,
        top_k: Optional[int] = None,
        filter_video_id: Optional[str] = None,
    ) -> List[Dict]:
        top_k = top_k or config.top_k

        query_embedding = self.embedder.encode([query])[0]

        where = {"video_id": filter_video_id} if filter_video_id else None

        results = self.collection.query(
            query_embeddings=[query_embedding.tolist()],
            n_results=top_k,
            where=where,
        )

        retrieved = []
        if results.get("documents"):
            for i in range(len(results["documents"][0])):
                retrieved.append(
                    {
                        "text": results["documents"][0][i],
                        "metadata": results["metadatas"][0][i],
                        "distance": results["distances"][0][i],
                    }
                )

        return retrieved

    # --------------------------------------------------
    # Context builder
    # --------------------------------------------------
    def generate_context(self, retrieved_docs: List[Dict]) -> str:
        if not retrieved_docs:
            return "No relevant information found in the video transcripts."

        parts = []
        for i, doc in enumerate(retrieved_docs, 1):
            ts = doc["metadata"].get("timestamp_start")
            if ts is not None:
                minutes = int(ts // 60)
                seconds = int(ts % 60)
                time_str = f"{minutes}:{seconds:02d}"
            else:
                time_str = "N/A"

            parts.append(
                f"[Source {i} | Video: {doc['metadata'].get('video_id')} | Time: {time_str}]\n"
                f"{doc['text']}"
            )

        return "\n\n".join(parts)

    # --------------------------------------------------
    # Utilities
    # --------------------------------------------------
    def clear_collection(self) -> None:
        logger.warning("Clearing collection '%s'", self.collection_name)
        self.client.delete_collection(self.collection_name)
        self.collection = self.client.get_or_create_collection(
            name=self.collection_name,
            metadata={"hnsw:space": "cosine"},
        )


# --------------------------------------------------
# Manual test
# --------------------------------------------------
if __name__ == "__main__":
    rag = RAGPipeline()
    logger.info("RAGPipeline ready")
