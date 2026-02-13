"""
Configuration loader for Video RAG System

This file loads all settings from .env and provides easy access
throughout the application. It also handles environment-specific
configuration (development vs production).

Author: Championstein
Date: 2025
"""

import os
import logging
from pathlib import Path
from dotenv import load_dotenv
from typing import Literal

# --------------------------------------------------
# Logging setup
# --------------------------------------------------
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Load environment variables from .env file
load_dotenv()


class Config:
    """
    Central configuration class.

    Usage:
        from config import config
        print(config.llm_provider)
    """

    def __init__(self):
        # ============================================
        # ENVIRONMENT
        # ============================================
        self.environment: Literal["development", "production"] = os.getenv(
            "ENV", "development"
        ).lower()

        if self.environment not in ("development", "production"):
            raise ValueError(
                f"Invalid ENV value: {self.environment}. "
                "Must be 'development' or 'production'."
            )

        # ============================================
        # LLM CONFIGURATION
        # ============================================
        if self.environment == "development":
            self.llm_provider = os.getenv("LOCAL_LLM_PROVIDER", "ollama")
            self.llm_model = os.getenv("LOCAL_LLM_MODEL", "llama3.2:latest")

            if self.llm_provider == "groq":
                self.groq_api_key = os.getenv("GROQ_API_KEY")
                if not self.groq_api_key:
                    logger.warning("Local provider is 'groq' but GROQ_API_KEY is missing!")
            else:
                self.groq_api_key = None

            logger.info(
                "[DEV MODE] LLM Provider=%s | Model=%s",
                self.llm_provider,
                self.llm_model,
            )
        else:
            self.llm_provider = os.getenv("PROD_LLM_PROVIDER", "groq")
            self.llm_model = os.getenv(
                "PROD_LLM_MODEL",
                "llama-3.3-70b-versatile",
            )
            self.groq_api_key = os.getenv("GROQ_API_KEY")

            if not self.groq_api_key:
                raise ValueError(
                    "GROQ_API_KEY is required in production environment."
                )

            logger.info(
                "[PROD MODE] LLM Provider=%s | Model=%s",
                self.llm_provider,
                self.llm_model,
            )

        # ============================================
        # WHISPER CONFIGURATION
        # ============================================
        self.whisper_model = os.getenv("WHISPER_MODEL", "base")

        valid_whisper_models = {"tiny", "base", "small", "medium", "large"}
        if self.whisper_model not in valid_whisper_models:
            raise ValueError(
                f"Invalid WHISPER_MODEL: {self.whisper_model}. "
                f"Must be one of {sorted(valid_whisper_models)}"
            )

        # ============================================
        # EMBEDDING CONFIGURATION
        # ============================================
        self.embedding_model = os.getenv(
            "EMBEDDING_MODEL",
            "all-MiniLM-L6-v2",
        )

        # ============================================
        # RAG CONFIGURATION (character-based)
        # ============================================
        self.chunk_size = int(os.getenv("CHUNK_SIZE", "500"))
        self.chunk_overlap = int(os.getenv("CHUNK_OVERLAP", "100"))
        self.top_k = int(os.getenv("TOP_K", "5"))

        if self.chunk_size <= 0:
            raise ValueError("CHUNK_SIZE must be positive")
        if self.chunk_overlap >= self.chunk_size:
            raise ValueError("CHUNK_OVERLAP must be less than CHUNK_SIZE")
        if self.top_k <= 0:
            raise ValueError("TOP_K must be positive")

        # ============================================
        # YOUTUBE INGESTION
        # ============================================
        self.use_youtube_captions = (
            os.getenv("USE_YOUTUBE_CAPTIONS", "true").lower() == "true"
        )

        # ============================================
        # STORAGE PATHS
        # ============================================
        self.data_dir = Path(os.getenv("DATA_DIR", "./data")).resolve()
        self.vector_db_path = Path(
            os.getenv("VECTOR_DB_PATH", "./chroma_db")
        ).resolve()
        self.collection_name = os.getenv(
            "COLLECTION_NAME",
            "video_knowledge",
        )

        self.data_dir.mkdir(parents=True, exist_ok=True)
        self.vector_db_path.mkdir(parents=True, exist_ok=True)

        # ============================================
        # API CONFIGURATION
        # ============================================
        self.api_host = os.getenv("API_HOST", "0.0.0.0")
        self.api_port = int(os.getenv("API_PORT", "8000"))

    # --------------------------------------------------
    # Helper methods
    # --------------------------------------------------
    def is_development(self) -> bool:
        return self.environment == "development"

    def is_production(self) -> bool:
        return self.environment == "production"

    def get_llm_config(self) -> dict:
        return {
            "provider": self.llm_provider,
            "model": self.llm_model,
            "api_key": self.groq_api_key,
        }

    def print_config(self) -> None:
        print("\n" + "=" * 60)
        print("📋 CONFIGURATION SUMMARY")
        print("=" * 60)
        print(f"Environment        : {self.environment}")
        print(f"LLM Provider       : {self.llm_provider}")
        print(f"LLM Model          : {self.llm_model}")
        print(f"Whisper Model      : {self.whisper_model}")
        print(f"Embedding Model    : {self.embedding_model}")
        print(f"Chunk Size (chars) : {self.chunk_size}")
        print(f"Chunk Overlap      : {self.chunk_overlap}")
        print(f"Top K              : {self.top_k}")
        print(f"Use YT Captions    : {self.use_youtube_captions}")
        print(f"Data Dir           : {self.data_dir}")
        print(f"Vector DB Path     : {self.vector_db_path}")
        print(f"Collection Name    : {self.collection_name}")
        print("=" * 60 + "\n")


# Singleton configuration instance
config = Config()


if __name__ == "__main__":
    logger.info("Testing configuration loader")
    config.print_config()
    logger.info("Is Development? %s", config.is_development())
    logger.info("Is Production? %s", config.is_production())
    logger.info("LLM Config: %s", config.get_llm_config())
    logger.info("Configuration loaded successfully ✅")
