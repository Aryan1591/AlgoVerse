"""
Video Processor for Video RAG System

Responsibilities:
1. Fetch YouTube captions (FAST, FREE)
2. Fallback to audio download + Whisper transcription (ACCURATE)
3. Handle local video files
4. Persist transcripts with timestamps (RAG-ready)

Strategy:
- YouTube URL
    -> Try captions
    -> If unavailable, download audio + Whisper
- Local video
    -> Extract audio + Whisper

Author: Championstein
Date: 2025
"""

import os
import json
import logging
from pathlib import Path
from typing import Dict, List, Optional

import whisper
import yt_dlp
from yt_dlp.utils import DownloadError
from youtube_transcript_api import YouTubeTranscriptApi
from youtube_transcript_api._errors import (
    TranscriptsDisabled,
    NoTranscriptFound,
    VideoUnavailable,
)

from .config import config  # since file is under src/

# --------------------------------------------------
# Logging
# --------------------------------------------------
logger = logging.getLogger(__name__)


class VideoProcessor:
    """
    Handles YouTube & local video transcription.
    """

    def __init__(self, output_dir: Optional[Path] = None):
        """
        Args:
            output_dir: Directory to store audio & transcripts.
                        Defaults to config.data_dir
        """
        self.output_dir = output_dir or config.data_dir
        self.output_dir.mkdir(parents=True, exist_ok=True)

        self._whisper_model = None  # lazy load

        logger.info("VideoProcessor initialized (output_dir=%s)", self.output_dir)

    # --------------------------------------------------
    # Whisper (lazy-loaded)
    # --------------------------------------------------
    @property
    def whisper_model(self):
        if self._whisper_model is None:
            logger.info(
                "Loading Whisper model '%s' (one-time cost)...",
                config.whisper_model,
            )
            self._whisper_model = whisper.load_model(config.whisper_model)
            logger.info("Whisper model loaded")
        return self._whisper_model

    # --------------------------------------------------
    # YouTube helpers
    # --------------------------------------------------
    def extract_youtube_id(self, url: str) -> str:
        """
        Extract YouTube video ID from common URL formats.
        """
        if "youtu.be/" in url:
            return url.split("youtu.be/")[-1].split("?")[0]
        if "watch?v=" in url:
            return url.split("watch?v=")[-1].split("&")[0]
        if "shorts/" in url:
            return url.split("shorts/")[-1].split("?")[0]

        raise ValueError(f"Unsupported YouTube URL: {url}")

    def get_youtube_captions(self, video_id: str) -> Optional[Dict]:
        """
        Attempt to fetch YouTube captions.
        """
        if not config.use_youtube_captions:
            return None

        try:
            logger.info("Trying YouTube captions for %s", video_id)

            transcript_list = YouTubeTranscriptApi.list_transcripts(video_id)

            try:
                transcript = transcript_list.find_manually_created_transcript(["en"])
                logger.info("Using manual English captions")
            except NoTranscriptFound:
                transcript = transcript_list.find_generated_transcript(["en"])
                logger.info("Using auto-generated English captions")

            data = transcript.fetch()

            segments = [
                {
                    "start": item["start"],
                    "end": item["start"] + item["duration"],
                    "text": item["text"].strip(),
                }
                for item in data
            ]

            return {
                "video_id": video_id,
                "text": " ".join(s["text"] for s in segments),
                "segments": segments,
                "source": "youtube_captions",
            }

        except (TranscriptsDisabled, NoTranscriptFound, VideoUnavailable):
            logger.info("No captions available for %s", video_id)
            return None
        except Exception as e:
            logger.warning("Caption fetch failed: %s", e)
            return None

    # --------------------------------------------------
    # Audio download + Whisper
    # --------------------------------------------------
    def download_youtube_audio(self, url: str, video_id: str) -> Path:
        """
        Download audio using yt-dlp.
        """
        audio_path = self.output_dir / f"{video_id}.mp3"
        if audio_path.exists():
            return audio_path

        logger.info("Downloading audio for %s", video_id)

        ydl_opts = {
            "format": "bestaudio/best",
            "outtmpl": str(self.output_dir / f"{video_id}.%(ext)s"),
            "postprocessors": [
                {
                    "key": "FFmpegExtractAudio",
                    "preferredcodec": "mp3",
                    "preferredquality": "192",
                }
            ],
            "quiet": True,
        }

        try:
            with yt_dlp.YoutubeDL(ydl_opts) as ydl:
                ydl.download([url])
        except DownloadError as e:
            if "HTTP Error 416" in str(e) or "Requested range not satisfiable" in str(e):
                logger.warning("Encountered HTTP 416 error (resume failed). Cleaning up partial files and retrying...")
                
                # Cleanup partial files for this video
                # Pattern: {video_id}.* (including .part, .ytdl, etc.)
                for partial_file in self.output_dir.glob(f"{video_id}.*"):
                    try:
                        os.remove(partial_file)
                        logger.info("Deleted partial file: %s", partial_file.name)
                    except OSError as cleanup_error:
                        logger.warning("Failed to delete partial file %s: %s", partial_file.name, cleanup_error)
                
                # Retry download
                logger.info("Retrying download for %s", video_id)
                with yt_dlp.YoutubeDL(ydl_opts) as ydl:
                    ydl.download([url])
            else:
                raise e

        return audio_path

    def transcribe_audio(self, audio_path: Path, video_id: str) -> Dict:
        """
        Transcribe audio via Whisper.
        """
        transcript_path = self.output_dir / f"{video_id}_transcript.json"

        if transcript_path.exists():
            with open(transcript_path, "r", encoding="utf-8") as f:
                return json.load(f)

        logger.info("Transcribing audio with Whisper (%s)", audio_path)

        result = self.whisper_model.transcribe(
            str(audio_path),
            language=os.getenv("WHISPER_LANGUAGE", "en"),
            verbose=False,
        )

        transcript = {
            "video_id": video_id,
            "text": result["text"],
            "segments": [
                {
                    "start": seg["start"],
                    "end": seg["end"],
                    "text": seg["text"].strip(),
                }
                for seg in result["segments"]
            ],
            "source": "whisper",
        }

        with open(transcript_path, "w", encoding="utf-8") as f:
            json.dump(transcript, f, indent=2, ensure_ascii=False)

        return transcript

    # --------------------------------------------------
    # Public APIs
    # --------------------------------------------------
    def process_youtube_video(self, url: str) -> Dict:
        """
        Main YouTube ingestion method.
        """
        video_id = self.extract_youtube_id(url)
        transcript_path = self.output_dir / f"{video_id}_transcript.json"

        if transcript_path.exists():
            with open(transcript_path, "r", encoding="utf-8") as f:
                return json.load(f)

        logger.info("Processing YouTube video %s", video_id)

        transcript = self.get_youtube_captions(video_id)
        if transcript:
            with open(transcript_path, "w", encoding="utf-8") as f:
                json.dump(transcript, f, indent=2, ensure_ascii=False)
            return transcript

        audio_path = self.download_youtube_audio(url, video_id)
        return self.transcribe_audio(audio_path, video_id)

    def process_youtube_videos(self, urls: List[str]) -> List[Dict]:
        """
        Process multiple YouTube videos.
        """
        results = []
        for url in urls:
            try:
                results.append(self.process_youtube_video(url))
            except Exception as e:
                logger.error("Failed to process %s: %s", url, e)
        return results

    # --------------------------------------------------
    # Cleanup
    # --------------------------------------------------
    def cleanup_resources(self, video_id: str) -> None:
        """
        Delete .mp3 and .json files to save space.
        """
        files_to_delete = [
            self.output_dir / f"{video_id}.mp3",
            self.output_dir / f"{video_id}_transcript.json",
        ]

        for path in files_to_delete:
            try:
                if path.exists():
                    os.remove(path)
                    logger.info("Deleted cleanup file: %s", path.name)
            except OSError as e:
                logger.warning("Failed to delete %s: %s", path.name, e)


# --------------------------------------------------
# Manual test
# --------------------------------------------------
if __name__ == "__main__":
    vp = VideoProcessor()
    logger.info("VideoProcessor ready")
