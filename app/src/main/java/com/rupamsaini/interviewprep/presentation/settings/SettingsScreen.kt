package com.rupamsaini.interviewprep.presentation.settings

import android.app.TimePickerDialog
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
    val context = LocalContext.current

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

            // Difficulty dropdown
            SettingsDropdown(
                label = "Preferred Difficulty",
                selectedValue = uiState.preferredDifficulty,
                options = SettingsViewModel.DIFFICULTIES,
                onOptionSelected = { viewModel.setPreferredDifficulty(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category dropdown
            SettingsDropdown(
                label = "Preferred Category",
                selectedValue = uiState.preferredCategory,
                options = SettingsViewModel.CATEGORIES,
                onOptionSelected = { viewModel.setPreferredCategory(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Notifications Section ---
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Enable Notifications
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

            // Time Picker
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

                // Weekend Mode
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
