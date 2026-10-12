package com.example.lifeflow.features.goals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lifeflow.core.ui.CategoryBadge
import com.example.lifeflow.core.theme.ErrorRed
import com.example.lifeflow.core.theme.PurplePrimary
import com.example.lifeflow.core.theme.SuccessGreen
import com.example.lifeflow.domain.model.Milestone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailsScreen(
    goalId: String,
    navController: NavController,
    viewModel: GoalViewModel = viewModel()
) {
    LaunchedEffect(goalId) {
        viewModel.getGoalDetails(goalId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val goal = uiState.selectedGoal

    var newMilestoneTitle by remember { mutableStateOf("") }
    var showAddMilestoneDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Goal Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.deleteGoal(goalId, onSuccess = { navController.popBackStack() })
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Goal", tint = ErrorRed)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddMilestoneDialog = true },
                containerColor = PurplePrimary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Milestone")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (goal == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CategoryBadge(category = goal.category)

                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (goal.description.isNotEmpty()) {
                            Text(
                                text = goal.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Progress bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Goal Progress", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${goal.progressPercentage}%", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = PurplePrimary)
                        }
                        LinearProgressIndicator(
                            progress = { goal.progressPercentage / 100f },
                            modifier = Modifier.fillMaxWidth().height(10.dp),
                            color = PurplePrimary
                        )
                    }
                }

                Text(
                    text = "Milestones Checklist",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (goal.milestones.isEmpty()) {
                    Text(
                        text = "No milestones added yet. Tap + to add steps towards achieving this goal.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(goal.milestones) { milestone ->
                            MilestoneItem(
                                milestone = milestone,
                                onToggle = { completed ->
                                    viewModel.toggleMilestone(goal.id, milestone.id, completed)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddMilestoneDialog) {
        AlertDialog(
            onDismissRequest = { showAddMilestoneDialog = false },
            title = { Text("Add Milestone Step") },
            text = {
                OutlinedTextField(
                    value = newMilestoneTitle,
                    onValueChange = { newMilestoneTitle = it },
                    label = { Text("Milestone Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newMilestoneTitle.isNotBlank()) {
                            viewModel.addMilestone(goalId, newMilestoneTitle)
                            newMilestoneTitle = ""
                            showAddMilestoneDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMilestoneDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MilestoneItem(milestone: Milestone, onToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onToggle(!milestone.completed) }) {
                Icon(
                    imageVector = if (milestone.completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (milestone.completed) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = milestone.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = if (milestone.completed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}
