package com.rupamsaini.interviewprep.presentation.settings

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
        ) {
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

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
