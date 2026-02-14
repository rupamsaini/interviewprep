package com.rupamsaini.interviewprep.presentation.question

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionScreen(
    onBackClick: () -> Unit,
    viewModel: QuestionViewModel = hiltViewModel()
) {
    val question by viewModel.question.collectAsState()
    val isAnswerVisible by viewModel.isAnswerVisible.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Question Detail") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            question?.let { q ->
                // Question Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Question",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = q.question,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Category: ${q.category} | Difficulty: ${q.difficulty}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (isAnswerVisible) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Answer",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = q.answer,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            q.explanation?.let { explanation ->
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Explanation",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = explanation,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            q.codeExample?.let { code ->
                                Spacer(modifier = Modifier.height(16.dp))
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Text(
                                        text = code,
                                        modifier = Modifier.padding(8.dp),
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "How well did you know this?",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ReviewButton("Again", 0) { viewModel.processReview(0); onBackClick() }
                        ReviewButton("Hard", 3) { viewModel.processReview(3); onBackClick() }
                        ReviewButton("Good", 4) { viewModel.processReview(4); onBackClick() }
                        ReviewButton("Easy", 5) { viewModel.processReview(5); onBackClick() }
                    }
                } else {
                    Button(
                        onClick = { viewModel.toggleAnswerVisibility() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Show Answer")
                    }
                }
            } ?: run {
                Text("Loading...")
            }
        }
    }
}

@Composable
fun ReviewButton(text: String, quality: Int, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick) {
        Text(text)
    }
}
