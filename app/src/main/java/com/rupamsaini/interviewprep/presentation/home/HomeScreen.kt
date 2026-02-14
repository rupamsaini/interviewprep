package com.rupamsaini.interviewprep.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rupamsaini.interviewprep.domain.model.Question

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onQuestionClick: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val questions by viewModel.questions.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val showSourcePicker by viewModel.showSourcePicker.collectAsState()
    val fetchedQuestionId by viewModel.fetchedQuestionId.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedDifficulty by viewModel.selectedDifficulty.collectAsState()
    val selectedSource by viewModel.selectedSource.collectAsState()
    var showImportDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // Navigate to question detail when a question is fetched
    LaunchedEffect(fetchedQuestionId) {
        fetchedQuestionId?.let { id ->
            onQuestionClick(id)
            viewModel.clearFetchedQuestion()
        }
    }

    if (showImportDialog) {
        ImportUrlDialog(
            onDismiss = { showImportDialog = false },
            onImport = { url ->
                viewModel.importQuestions(url)
                showImportDialog = false
            }
        )
    }

    // Source picker bottom sheet
    if (showSourcePicker) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissSourcePicker() },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Get a Question",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                ListItem(
                    modifier = Modifier.clickable { viewModel.fetchFromDatabase() },
                    headlineContent = {
                        Text("From Database", style = MaterialTheme.typography.titleMedium)
                    },
                    supportingContent = {
                        Text("Pick a random question from your local collection")
                    },
                    leadingContent = {
                        Text("📂", style = MaterialTheme.typography.headlineMedium)
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                ListItem(
                    modifier = Modifier.clickable { viewModel.fetchFromAi() },
                    headlineContent = {
                        Text("Generate with AI", style = MaterialTheme.typography.titleMedium)
                    },
                    supportingContent = {
                        Text("Create a fresh question using Gemini AI")
                    },
                    leadingContent = {
                        Text("🤖", style = MaterialTheme.typography.headlineMedium)
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Interview Questions") },
                actions = {
                    IconButton(onClick = { showImportDialog = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Import from Web")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.onFetchQuestionClick() }) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    Icon(Icons.Default.Add, contentDescription = "Get Question")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Collapsible filter section
            CollapsibleFilterBar(
                selectedCategory = selectedCategory,
                selectedDifficulty = selectedDifficulty,
                selectedSource = selectedSource,
                onCategorySelected = { viewModel.setSelectedCategory(it) },
                onDifficultySelected = { viewModel.setSelectedDifficulty(it) },
                onSourceSelected = { viewModel.setSelectedSource(it) }
            )

            HorizontalDivider()

            // Question count
            Text(
                text = "${questions.size} questions",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Questions list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                items(questions) { question ->
                    QuestionItem(question, onQuestionClick)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsibleFilterBar(
    selectedCategory: String,
    selectedDifficulty: String,
    selectedSource: String,
    onCategorySelected: (String) -> Unit,
    onDifficultySelected: (String) -> Unit,
    onSourceSelected: (String) -> Unit
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val activeFilterCount = listOf(selectedCategory, selectedDifficulty, selectedSource)
        .count { it != "All" }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Collapsible header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Filters",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                if (activeFilterCount > 0 && !isExpanded) {
                    Badge {
                        Text("$activeFilterCount")
                    }
                }
            }
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse filters" else "Expand filters",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Animated filter chips
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column {
                // Category chips
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HomeViewModel.CATEGORIES.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { onCategorySelected(category) },
                            label = { Text(category, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Difficulty chips
                Text(
                    text = "Difficulty",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HomeViewModel.DIFFICULTIES.forEach { difficulty ->
                        FilterChip(
                            selected = selectedDifficulty == difficulty,
                            onClick = { onDifficultySelected(difficulty) },
                            label = { Text(difficulty, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Source chips
                Text(
                    text = "Source",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HomeViewModel.SOURCES.forEach { source ->
                        FilterChip(
                            selected = selectedSource == source,
                            onClick = { onSourceSelected(source) },
                            label = { Text(source, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ImportUrlDialog(onDismiss: () -> Unit, onImport: (String) -> Unit) {
    var url by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import Questions") },
        text = {
            TextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("Enter URL") },
                placeholder = { Text("https://example.com/questions") }
            )
        },
        confirmButton = {
            Button(onClick = { onImport(url) }) {
                Text("Import")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun QuestionItem(
    question: Question,
    onClick: (Long) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick(question.id) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = question.question,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = question.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = question.difficulty.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = question.source.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
