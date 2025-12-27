"""
API Layer for Video RAG System

Responsibilities:
- Expose ingestion and Q&A endpoints
- Orchestrate VideoProcessor, RAGPipeline, and LLMHandler
- No business logic lives here

Author: Championstein
Date: 2025
"""

import logging
from typing import List, Optional

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

from .config import config
from .video_processor import VideoProcessor
from .rag_pipeline import RAGPipeline
from .llm_handler import LLMHandler

# --------------------------------------------------
# Logging
# --------------------------------------------------
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# --------------------------------------------------
# App initialization
# --------------------------------------------------
app = FastAPI(
    title="Video RAG System",
    description="Ask questions from teaching videos using RAG",
    version="1.0.0",
)

# --------------------------------------------------
# Singletons (created once)
# --------------------------------------------------
video_processor = VideoProcessor()
rag_pipeline = RAGPipeline()
llm_handler = LLMHandler()

# --------------------------------------------------
# Request / Response Models
# --------------------------------------------------
class YouTubeIngestRequest(BaseModel):
    urls: List[str]


class AskRequest(BaseModel):
    question: str
    video_id: Optional[str] = None  # optional filtering


class AskResponse(BaseModel):
    question: str
    answer: str


# --------------------------------------------------
# Health Check
# --------------------------------------------------
@app.get("/health")
def health_check():
    return {
        "status": "ok",
        "environment": config.environment,
        "llm_provider": config.llm_provider,
        "llm_model": config.llm_model,
    }


# --------------------------------------------------
# Ingestion Endpoint
# --------------------------------------------------
@app.post("/ingest/youtube")
def ingest_youtube(req: YouTubeIngestRequest):
    if not req.urls:
        raise HTTPException(status_code=400, detail="No URLs provided")

    logger.info("Ingesting %d YouTube videos", len(req.urls))

    transcripts = video_processor.process_youtube_videos(req.urls)

    if not transcripts:
        raise HTTPException(
            status_code=500,
            detail="Failed to ingest any videos",
        )

    rag_pipeline.add_documents(transcripts)

    # Cleanup temp files
    for t in transcripts:
        video_id = t.get("video_id")
        if video_id:
            video_processor.cleanup_resources(video_id)

    return {
        "message": "Videos ingested successfully",
        "videos_ingested": len(transcripts),
        "video_ids": [t.get("video_id") for t in transcripts],
    }


# --------------------------------------------------
# Question Answering Endpoint
# --------------------------------------------------
@app.post("/ask", response_model=AskResponse)
def ask_question(req: AskRequest):
    if not req.question.strip():
        raise HTTPException(status_code=400, detail="Question cannot be empty")

    logger.info("Received question: %s", req.question)

    retrieved_docs = rag_pipeline.retrieve(
        query=req.question,
        filter_video_id=req.video_id,
    )

    context = rag_pipeline.generate_context(retrieved_docs)

    answer_payload = llm_handler.answer_question(
        question=req.question,
        context=context,
    )

    return {
        "question": req.question,
        "answer": answer_payload["answer"],
    }
