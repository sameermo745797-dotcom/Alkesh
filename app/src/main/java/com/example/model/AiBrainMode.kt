package com.example.model

enum class AiBrainMode(
    val title: String,
    val subtitle: String,
    val tag: String,
    val description: String
) {
    JARVIS("JARVIS ASSISTANT", "Tony Stark Tactical Persona", "JARVIS", "Courteous, authoritative British/Indian tactical copilot addressing you as Sir/Commander with supersonic efficiency."),
    CHATGPT_RESEARCH("CHATGPT DEEP RESEARCH", "General Intelligence & Q&A", "GPT-5", "Deep multi-domain knowledge, scientific breakdowns, creative ideation, and clear logical explanations."),
    CLAUDE_CODING("CLAUDE ARTIFACTS / CODER", "Programming & Architecture", "CLAUDE", "Code generation, bug debugging, software architecture, and algorithm analysis in Kotlin, Python, JS & more."),
    DEEPSEEK_REASONING("DEEPSEEK R1 COGNITIVE", "Math & Step-by-Step Logic", "R1-THINK", "Rigorous chains of thought, complex mathematical proofs, tactical puzzle solving, and step-by-step reasoning."),
    PERPLEXITY_SEARCH("PERPLEXITY LIVE SEARCH", "Real-Time Fact Synthesis", "PERPLEX", "Direct factual summaries, live knowledge compilation, web citations style, and rapid verification."),
    CREATIVE_PROMPTER("MIDJOURNEY / DALL-E ART", "Visual Prompt Architect", "IMAGINE", "Creates state-of-the-art cinematic hyperrealistic image generation prompts and artistic concepts.")
}

data class QuickToolAction(
    val id: String,
    val title: String,
    val category: String,
    val promptTemplate: String,
    val iconName: String
)
