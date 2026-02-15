package com.rupamsaini.interviewprep.presentation.settings

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val deleteResult by viewModel.deleteResult.collectAsState()
    val context = LocalContext.current
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Show toast for delete result
    LaunchedEffect(deleteResult) {
        deleteResult?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearDeleteResult()
        }
    }

    // Confirmation dialog
    if (showDeleteConfirmation) {
        val scopeLabel = SettingsViewModel.DELETE_SCOPES
            .find { it.first == uiState.autoDeleteScope }?.second ?: uiState.autoDeleteScope

        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Confirm Deletion") },
            text = {
                Text("Are you sure you want to delete \"$scopeLabel\"? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteNow()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
                .verticalScroll(rememberScrollState())
        ) {
            // --- Question Preferences Section ---
            Text(
                text = "Question Preferences",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            SettingsDropdown(
                label = "Preferred Difficulty",
                selectedValue = uiState.preferredDifficulty,
                options = SettingsViewModel.DIFFICULTIES,
                onOptionSelected = { viewModel.setPreferredDifficulty(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsDropdown(
                label = "Preferred Category",
                selectedValue = uiState.preferredCategory,
                options = SettingsViewModel.CATEGORIES,
                onOptionSelected = { viewModel.setPreferredCategory(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // --- Data Management Section ---
            Text(
                text = "Data Management",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Delete scope dropdown
            SettingsDropdownPair(
                label = "Delete Scope",
                selectedKey = uiState.autoDeleteScope,
                options = SettingsViewModel.DELETE_SCOPES,
                onOptionSelected = { viewModel.setAutoDeleteScope(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Delete Now button
            Button(
                onClick = { showDeleteConfirmation = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete Now")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Schedule daily deletion toggle
            ListItem(
                headlineContent = { Text("Schedule Daily Deletion") },
                supportingContent = { Text("Automatically delete at a set time each day") },
                trailingContent = {
                    Switch(
                        checked = uiState.autoDeleteScheduled,
                        onCheckedChange = { viewModel.toggleScheduledDelete(it) }
                    )
                }
            )

            // Deletion time picker
            if (uiState.autoDeleteScheduled) {
                ListItem(
                    headlineContent = { Text("Deletion Time") },
                    supportingContent = {
                        Text(
                            String.format(
                                Locale.getDefault(),
                                "%02d:%02d",
                                uiState.autoDeleteHour,
                                uiState.autoDeleteMinute
                            )
                        )
                    },
                    modifier = Modifier.clickable {
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                viewModel.setAutoDeleteTime(hour, minute)
                            },
                            uiState.autoDeleteHour,
                            uiState.autoDeleteMinute,
                            true
                        ).show()
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // --- Notifications Section ---
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            ListItem(
                headlineContent = { Text("Daily Notification") },
                supportingContent = { Text("Get a new question every day") },
                trailingContent = {
                    Switch(
                        checked = uiState.dailyNotificationEnabled,
                        onCheckedChange = { viewModel.toggleDailyNotification(it) }
                    )
                }
            )

            if (uiState.dailyNotificationEnabled) {
                ListItem(
                    headlineContent = { Text("Notification Time") },
                    supportingContent = {
                        Text(
                            String.format(
                                Locale.getDefault(),
                                "%02d:%02d",
                                uiState.notificationHour,
                                uiState.notificationMinute
                            )
                        )
                    },
                    modifier = Modifier.clickable {
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                viewModel.setNotificationTime(hour, minute)
                            },
                            uiState.notificationHour,
                            uiState.notificationMinute,
                            true
                        ).show()
                    }
                )

                ListItem(
                    headlineContent = { Text("Weekend Mode") },
                    supportingContent = { Text("Pause notifications on Saturday and Sunday") },
                    trailingContent = {
                        Switch(
                            checked = uiState.weekendModeEnabled,
                            onCheckedChange = { viewModel.toggleWeekendMode(it) }
                        )
                    }
                )
            }

            // Bottom spacing
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDropdown(
    label: String,
    selectedValue: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDropdownPair(
    label: String,
    selectedKey: String,
    options: List<Pair<String, String>>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val displayValue = options.find { it.first == selectedKey }?.second ?: selectedKey

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = displayValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (key, display) ->
                DropdownMenuItem(
                    text = { Text(display) },
                    onClick = {
                        onOptionSelected(key)
                        expanded = false
                    }
                )
            }
        }
    }
}
