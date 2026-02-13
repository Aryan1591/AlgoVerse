"""
Video RAG System - Source Package

A Retrieval-Augmented Generation system for teaching videos.
Allows users to ask questions and get answers from video transcripts.

Author: Championstein
Date: 2025
"""

__version__ = "1.0.0"
__author__ = "Championstein"

# This makes imports cleaner:
# Instead of: from src.config import config
# You can do: from src import config
from .config import config

__all__ = ["config"]