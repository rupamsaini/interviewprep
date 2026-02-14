package com.rupamsaini.interviewprep.data.remote

import com.rupamsaini.interviewprep.domain.model.Question
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebScraperDataSource @Inject constructor() {

    suspend fun scrapeQuestions(url: String): List<Question> {
        return withContext(Dispatchers.IO) {
            try {
                val doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .get()

                val questions = mutableListOf<Question>()

                // Generic scraper logic:
                // Look for common patterns. E.g. h3 or h4 for questions, following sibling p/div for answers.
                // This is highly site-specific. For this "generic" scraper, we'll try to find headers that look like questions.
                
                // Strategy: Find elements containing "?" that are headings or strong tags.
                val elements = doc.select("h1, h2, h3, h4, h5, h6, strong, b")
                
                for (element in elements) {
                    val text = element.text()
                    if (text.contains("?") && text.length > 10) {
                        // Potential question.
                        // Try to get the next sibling element as the answer.
                        var next = element.nextElementSibling()
                        val answerBuilder = StringBuilder()
                        
                        // Collect siblings until next header
                        while (next != null) {
                            if (next.tagName().matches(Regex("h[1-6]"))) break
                            answerBuilder.append(next.text()).append("\n")
                            next = next.nextElementSibling()
                        }
                        
                        val answer = answerBuilder.toString().trim()
                        if (answer.isNotEmpty()) {
                            questions.add(
                                Question(
                                    question = text,
                                    answer = answer,
                                    category = "Scraped",
                                    difficulty = "Unknown",
                                    source = "web"
                                )
                            )
                        }
                    }
                }
                questions
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
}
