package com.jem.shukishaplease.features.trivia

import com.jem.shukishaplease.core.model.TriviaQuestion
import java.util.UUID

class MockAiTriviaGenerator {

    fun generateQuestionsForCategory(category: String, difficulty: String): List<TriviaQuestion> {
        val questions = when (category) {
            "Towns and Places" -> listOf(
                TriviaQuestion(
                    id = UUID.randomUUID().toString(),
                    category = category,
                    questionText = "Which Rift Valley town in Kenya is famous for its pink flamingo-lined lake and national park?",
                    options = listOf("Nakuru", "Naivasha", "Eldoret", "Kericho"),
                    correctAnswerIndex = 0,
                    explanation = "Lake Nakuru National Park is world-famous for flamingos nesting along its shores.",
                    difficulty = difficulty,
                    reference = "Kenya Tourism Board",
                    isAiGenerated = true
                ),
                TriviaQuestion(
                    id = UUID.randomUUID().toString(),
                    category = category,
                    questionText = "Which coastal city in Kenya features the historic 16th-century Portuguese fortress Fort Jesus?",
                    options = listOf("Malindi", "Mombasa", "Lamu", "Diani"),
                    correctAnswerIndex = 1,
                    explanation = "Fort Jesus was built by the Portuguese in 1593–1596 to guard the Old Port of Mombasa.",
                    difficulty = difficulty,
                    reference = "UNESCO World Heritage",
                    isAiGenerated = true
                ),
                TriviaQuestion(
                    id = UUID.randomUUID().toString(),
                    category = category,
                    questionText = "Which high-altitude Kenyan town is internationally renowned as the 'Home of Champions' for long-distance runners?",
                    options = listOf("Iten", "Kapsabet", "Eldoret", "Nyeri"),
                    correctAnswerIndex = 0,
                    explanation = "Iten sits at 2,400 meters altitude and is the global training hub for world-champion marathoners.",
                    difficulty = difficulty,
                    reference = "Athletics Kenya",
                    isAiGenerated = true
                )
            )
            "Economics and Money" -> listOf(
                TriviaQuestion(
                    id = UUID.randomUUID().toString(),
                    category = category,
                    questionText = "Which regulator oversees the banking sector and issues banknotes in Kenya?",
                    options = listOf("Central Bank of Kenya", "Kenya Revenue Authority", "Nairobi Securities Exchange", "Treasury"),
                    correctAnswerIndex = 0,
                    explanation = "The Central Bank of Kenya (CBK) formulates monetary policy and regulates financial institutions.",
                    difficulty = difficulty,
                    reference = "Central Bank of Kenya",
                    isAiGenerated = true
                ),
                TriviaQuestion(
                    id = UUID.randomUUID().toString(),
                    category = category,
                    questionText = "What term describes the percentage rate charged by lenders to borrowers on loan facilities?",
                    options = listOf("Interest Rate", "Inflation Rate", "Exchange Rate", "Discount Rate"),
                    correctAnswerIndex = 0,
                    explanation = "Interest rate represents the cost of borrowing capital over time.",
                    difficulty = difficulty,
                    reference = "Financial Literacy Handbook",
                    isAiGenerated = true
                )
            )
            "Sports" -> listOf(
                TriviaQuestion(
                    id = UUID.randomUUID().toString(),
                    category = category,
                    questionText = "Which famous Kenyan middle-distance runner won back-to-back Olympic gold medals in the 800m in 2012 and 2016?",
                    options = listOf("David Rudisha", "Asbel Kiprop", "Ezekiel Kemboi", "Conseslus Kipruto"),
                    correctAnswerIndex = 0,
                    explanation = "David Rudisha set the world record of 1:40.91 at the 2012 London Olympics.",
                    difficulty = difficulty,
                    reference = "World Athletics",
                    isAiGenerated = true
                ),
                TriviaQuestion(
                    id = UUID.randomUUID().toString(),
                    category = category,
                    questionText = "What is the traditional premier football derby in Kenya played between Gor Mahia and AFC Leopards?",
                    options = listOf("Mashemeji Derby", "Mombasa Derby", "Rift Valley Classic", "Capital Clash"),
                    correctAnswerIndex = 0,
                    explanation = "The Mashemeji Derby is Kenya's oldest and most passionate club football rivalry.",
                    difficulty = difficulty,
                    reference = "Football Kenya Federation",
                    isAiGenerated = true
                )
            )
            "Science and Technology" -> listOf(
                TriviaQuestion(
                    id = UUID.randomUUID().toString(),
                    category = category,
                    questionText = "What green renewable energy source produces over 40% of Kenya's total electricity supply from the Great Rift Valley?",
                    options = listOf("Geothermal Power", "Nuclear Energy", "Coal Power", "Biomass"),
                    correctAnswerIndex = 0,
                    explanation = "Kenya is a global leader in geothermal energy generation centered around Olkaria near Naivasha.",
                    difficulty = difficulty,
                    reference = "KenGen Energy Report",
                    isAiGenerated = true
                )
            )
            else -> listOf(
                TriviaQuestion(
                    id = UUID.randomUUID().toString(),
                    category = category,
                    questionText = "What is the capital city and economic hub of Kenya?",
                    options = listOf("Mombasa", "Nairobi", "Kisumu", "Nakuru"),
                    correctAnswerIndex = 1,
                    explanation = "Nairobi is known as the 'Green City in the Sun' and is Kenya's commercial center.",
                    difficulty = difficulty,
                    reference = "General Knowledge Guide",
                    isAiGenerated = true
                )
            )
        }
        return questions.shuffled()
    }
}
