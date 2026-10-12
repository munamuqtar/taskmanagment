package com.example.lifeflow.features.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lifeflow.core.ui.LifeFlowButton
import com.example.lifeflow.domain.model.Task

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(
    taskId: String,
    navController: NavController,
    viewModel: TaskViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val existingTask = remember(uiState.tasks, taskId) {
        uiState.tasks.find { it.id == taskId }
    }

    var title by remember(existingTask) { mutableStateOf(existingTask?.title ?: "") }
    var description by remember(existingTask) { mutableStateOf(existingTask?.description ?: "") }
    var taskDate by remember(existingTask) { mutableStateOf(existingTask?.taskDate ?: "") }
    var startTime by remember(existingTask) { mutableStateOf(existingTask?.startTime ?: "") }
    var endTime by remember(existingTask) { mutableStateOf(existingTask?.endTime ?: "") }
    var priority by remember(existingTask) { mutableStateOf(existingTask?.priority ?: "Medium") }
    var category by remember(existingTask) { mutableStateOf(existingTask?.category ?: "Personal") }

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Task", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (existingTask == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                Text("Task not found")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = taskDate,
                        onValueChange = { taskDate = it },
                        label = { Text("Date") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start Time") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    )
                }

                Text("Priority", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Low", "Medium", "High").forEach { item ->
                        FilterChip(
                            selected = priority == item,
                            onClick = { priority = item },
                            label = { Text(item) }
                        )
                    }
                }

                Text("Category", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Personal", "Work", "Health", "Learning").forEach { item ->
                        FilterChip(
                            selected = category == item,
                            onClick = { category = item },
                            label = { Text(item) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LifeFlowButton(
                    text = "Save Changes",
                    onClick = {
                        val updated = existingTask.copy(
                            title = title,
                            description = description,
                            taskDate = taskDate,
                            startTime = startTime,
                            endTime = endTime,
                            priority = priority,
                            category = category
                        )
                        viewModel.updateTask(updated, onSuccess = { navController.popBackStack() })
                    }
                )
            }
        }
    }
}
