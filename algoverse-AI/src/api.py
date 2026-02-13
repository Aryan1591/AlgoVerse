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
from typing import List, Optional, Dict

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


class AnalysisRequest(BaseModel):
    user_id: str
    total_solved: int
    easy_solved: int
    medium_solved: int
    hard_solved: int
    topic_stats: Dict[str, float]  # e.g. {"Arrays": 0.8, "DP": 0.1}
    recent_problems: List[str]  # List of problem names


class AnalysisResponse(BaseModel):
    user_id: str
    analysis_report: str


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


# --------------------------------------------------
# Performance Analysis Endpoint
# --------------------------------------------------
@app.post("/analyze/performance", response_model=AnalysisResponse)
def analyze_performance(req: AnalysisRequest):
    logger.info("Analyzing performance for user: %s", req.user_id)

    user_data = {
        "total_solved": req.total_solved,
        "easy_solved": req.easy_solved,
        "medium_solved": req.medium_solved,
        "hard_solved": req.hard_solved,
        "topic_stats": req.topic_stats,
        "recent_problems": req.recent_problems,
    }

    report = llm_handler.generate_performance_analysis(user_data)

    return AnalysisResponse(
        user_id=req.user_id,
        analysis_report=report,
    )
