package com.mohithash.cram.ai

import com.mohithash.cram.domain.Settings
import com.mohithash.cram.domain.StudyContent

class StudyAi(private val client: AiClient) {
    private val schema = Schema.obj(
        "title" to Schema.str, "summary" to Schema.arr(Schema.str), "key_points" to Schema.arr(Schema.str),
        "terms" to Schema.arr(Schema.obj("term" to Schema.str, "definition" to Schema.str)),
        "quiz" to Schema.arr(Schema.obj("question" to Schema.str, "options" to Schema.arr(Schema.str), "answer" to Schema.int, "explanation" to Schema.str)),
        "difficulty" to Schema.str,
    )

    suspend fun build(ai: AiSettings, s: Settings, text: String, image: String?, subjectHint: String): StudyContent {
        val system = """You turn study material into a compact study set for a ${s.level} student.
            |title: short. summary: 3-6 bullet sentences a student could recite. key_points: 4-8 facts/formulas/dates worth memorising verbatim.
            |terms: 8-16 flashcards (term → one-sentence definition). quiz: 8 multiple-choice questions with exactly 4 options each; answer is the 0-based index of the correct option; explanation one sentence.
            |Stay faithful to the source; do not invent facts not in it (you may add standard context a textbook would). difficulty: easy|medium|hard.""".trimMargin()
        val user = (if (subjectHint.isNotBlank()) "Subject: $subjectHint.\n" else "") + text.ifBlank { "Use the attached photo of notes/textbook as the source." }
        return client.ask(ai, system, user, schema, image, 8000)
    }

    suspend fun ask(ai: AiSettings, s: Settings, content: StudyContent, question: String): String {
        val ctx = "Study set: ${content.title}\nSummary: ${content.summary.joinToString(" ")}\nKey points: ${content.key_points.joinToString("; ")}\nTerms: ${content.terms.joinToString("; ") { "${it.term}: ${it.definition}" }}"
        return client.chat(ai, "You are a patient tutor for a ${s.level} student. Answer using the study set below first; keep answers under 120 words; use an analogy if it helps.\n\n$ctx",
            listOf(ChatMsg("user", question)), maxTokens = 700)
    }
}
