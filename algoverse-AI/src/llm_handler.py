"""
LLM Handler for Video RAG System

Supports:
- Ollama (local development)
- Groq (production)

This layer ONLY handles LLM interaction.
No retrieval logic lives here.

Author: Championstein
Date: 2025
"""

import logging
import requests
from typing import Dict, List, Optional

from .config import config

logger = logging.getLogger(__name__)


class LLMHandler:
    """
    Unified interface for LLM generation.
    """

    def __init__(
        self,
        provider: Optional[str] = None,
        model: Optional[str] = None,
        api_key: Optional[str] = None,
    ):
        llm_cfg = config.get_llm_config()

        self.provider = provider or llm_cfg["provider"]
        self.model = model or llm_cfg["model"]
        self.api_key = api_key or llm_cfg["api_key"]

        if self.provider == "ollama":
            self.base_url = "http://localhost:11434"
            logger.info("LLM: Ollama (%s)", self.model)

        elif self.provider == "groq":
            self.base_url = "https://api.groq.com/openai/v1"
            if not self.api_key:
                raise ValueError("GROQ_API_KEY missing in production")
            logger.info("LLM: Groq (%s)", self.model)

        else:
            raise ValueError(f"Unsupported LLM provider: {self.provider}")

    # --------------------------------------------------
    # Prompting
    # --------------------------------------------------
    def _system_prompt(self) -> str:
        return (
            "You are an expert Teaching Assistant for a Computer Science course covering Data Structures, Algorithms, and System Design.\n\n"
            "CRITICAL RULES:\n"
            "1. **SCOPE RESTRICTION**: If the user asks about a topic completely unrelated to Computer Science (e.g., cooking, politics, movies), strictly refuse by saying:\n"
            "   \"I can only answer questions related to Data Structures, Algorithms, or System Design.\"\n\n"
            "2. **CONTEXT USAGE**:\n"
            "   - Use the provided CONTEXT to answer the question.\n"
            "   - If the answer is found in the CONTEXT, answer directly and cite timestamps if available.\n\n"
            "3. **FALLBACK BEHAVIOR**:\n"
            "   - If the question is relevant to CS but the answer is NOT in the CONTEXT, you MUST start your response with:\n"
            "     \"This specific topic was not covered in the uploaded videos, but generally speaking...\"\n"
            "     ...and then provide a concise, correct technical explanation based on your general knowledge.\n\n"
            "STYLE:\n"
            "- Be clear, concise, and educational.\n"
        )

    def _user_prompt(self, question: str, context: str) -> str:
        return (
            f"CONTEXT:\n{context}\n\n"
            f"QUESTION:\n{question}\n\n"
            "ANSWER:"
        )

    # --------------------------------------------------
    # Ollama
    # --------------------------------------------------
    def _generate_ollama(self, question: str, context: str) -> str:
        payload = {
            "model": self.model,
            "system": self._system_prompt(),
            "prompt": self._user_prompt(question, context),
            "stream": False,
            "options": {
                "temperature": 0.25,
                "top_p": 0.9,
                "num_predict": 512,
            },
        }

        try:
            resp = requests.post(
                f"{self.base_url}/api/generate",
                json=payload,
                timeout=120,
            )
            resp.raise_for_status()
            return resp.json().get("response", "").strip()

        except Exception as e:
            logger.error("Ollama error: %s", e)
            return "Error: Unable to generate response."

    # --------------------------------------------------
    # Groq
    # --------------------------------------------------
    def _generate_groq(self, question: str, context: str) -> str:
        payload = {
            "model": self.model,
            "messages": [
                {"role": "system", "content": self._system_prompt()},
                {"role": "user", "content": self._user_prompt(question, context)},
            ],
            "temperature": 0.25,
            "max_tokens": 512,
        }

        headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json",
        }

        try:
            resp = requests.post(
                f"{self.base_url}/chat/completions",
                headers=headers,
                json=payload,
                timeout=30,
            )
            resp.raise_for_status()
            return resp.json()["choices"][0]["message"]["content"].strip()

        except requests.exceptions.HTTPError as e:
            logger.error("Groq HTTP error: %s", e.response.text)
            return "Error: LLM request failed."

        except Exception as e:
            logger.error("Groq error: %s", e)
            return "Error: Unable to generate response."

    # --------------------------------------------------
    # Public API
    # --------------------------------------------------
    def generate(self, question: str, context: str) -> str:
        if not question.strip():
            return "Error: Empty question."

        # REMOVED: context check to allow general knowledge fallback
        # if not context.strip():
        #    return "This topic was not covered in the videos."

        if self.provider == "ollama":
            return self._generate_ollama(question, context)

        return self._generate_groq(question, context)

    def answer_question(self, question: str, context: str) -> Dict:
        """
        Final step of RAG: context already built upstream.
        """
        answer = self.generate(question, context)

        return {
            "question": question,
            "answer": answer,
        }

    # --------------------------------------------------
    # Analysis API
    # --------------------------------------------------
    def generate_performance_analysis(self, user_data: Dict) -> str:
        """
        Generates a comprehensive performance analysis report based on user data.
        """
        system_prompt = (
            "You are an expert Technical Interview Coach and Data Structures & Algorithms Mentor.\n"
            "Your task is to analyze the user's solved problem history and provide a detailed performance report.\n\n"
            "REPORT STRUCTURE:\n"
            "1. **Executive Summary**: Brief overview of their current standing (Beginner/Intermediate/Advanced).\n"
            "2. **Strengths**: Identify topics where they are consistent and strong.\n"
            "3. **Weaknesses**: Identify important topics they have neglected or struggled with.\n"
            "4. **Actionable Study Plan**: A 4-week plan to improve their weak areas.\n"
            "5. **Recommended Focus**: Suggest 3 specific types of problems they should solve next.\n\n"
            "TONE: Encouraging, professional, and data-driven.\n"
            "FORMAT: Markdown."
        )

        user_prompt = (
            f"Here is the user's performance data:\n\n"
            f"Total Solved: {user_data.get('total_solved', 0)}\n"
            f"Easy: {user_data.get('easy_solved', 0)}\n"
            f"Medium: {user_data.get('medium_solved', 0)}\n"
            f"Hard: {user_data.get('hard_solved', 0)}\n\n"
            f"Topic Breakdown:\n{user_data.get('topic_stats', {})}\n\n"
            f"Recent Activity:\n{user_data.get('recent_problems', [])}\n\n"
            "Please analyze this data and generate the report."
        )

        if self.provider == "ollama":
            # Reuse _generate_ollama but we need to bypass the hardcoded prompts in it.
            # Since _generate_ollama is hardcoded, we will implement a specific private method for this or duplicate logic slightly for safety.
            # Ideally refactor, but for now let's add a specific internal handler or refactor _generate_ollama to take prompts.
            return self._generate_ollama_custom(system_prompt, user_prompt)
        
        return self._generate_groq_custom(system_prompt, user_prompt)

    def _generate_ollama_custom(self, system_prompt: str, user_prompt: str) -> str:
        payload = {
            "model": self.model,
            "system": system_prompt,
            "prompt": user_prompt,
            "stream": False,
            "options": {
                "temperature": 0.7, # Higher temp for more creative advice
                "top_p": 0.9,
            },
        }
        try:
            resp = requests.post(
                f"{self.base_url}/api/generate",
                json=payload,
                timeout=120,
            )
            resp.raise_for_status()
            return resp.json().get("response", "").strip()
        except Exception as e:
            logger.error("Ollama Analysis error: %s", e)
            return "Error: Unable to generate analysis."

    def _generate_groq_custom(self, system_prompt: str, user_prompt: str) -> str:
        payload = {
            "model": self.model,
            "messages": [
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt},
            ],
            "temperature": 0.5,
            "max_tokens": 1500, # Allow longer response for analysis
        }
        headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json",
        }
        try:
            resp = requests.post(
                f"{self.base_url}/chat/completions",
                headers=headers,
                json=payload,
                timeout=60,
            )
            resp.raise_for_status()
            return resp.json()["choices"][0]["message"]["content"].strip()
        except Exception as e:
            logger.error("Groq Analysis error: %s", e)
            return "Error: Unable to generate analysis."
