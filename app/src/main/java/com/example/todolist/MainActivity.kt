package com.example.todolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.todolist.ui.theme.ToDoListTheme
import java.util.*

class MainActivity : ComponentActivity() {
    private lateinit var taskRepository: TaskRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        taskRepository = TaskRepository(this)

        enableEdgeToEdge()
        setContent {
            ToDoListTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TodoListApp(
                        modifier = Modifier.padding(innerPadding),
                        initialTasks = taskRepository.loadTasks(),
                        saveTasks = { tasks -> taskRepository.saveTasks(tasks) }
                    )
                }
            }
        }
    }
}

data class Task(
    val id: UUID = UUID.randomUUID(),
    val description: String,
    val isCompleted: Boolean = false
)

@Composable
fun TodoListApp(
    modifier: Modifier = Modifier,
    initialTasks: List<Task> = emptyList(),
    saveTasks: (List<Task>) -> Unit = {}
) {
    var newTaskText by remember { mutableStateOf("") }
    var tasks by remember { mutableStateOf(initialTasks) }

    // Save tasks whenever they change
    LaunchedEffect(tasks) {
        saveTasks(tasks)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Input area with TextField and Add button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newTaskText,
                onValueChange = { newTaskText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Enter a task") },
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (newTaskText.isNotBlank()) {
                        tasks = tasks + Task(description = newTaskText.trim())
                        newTaskText = ""
                    }
                }
            ) {
                Text("Add")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tasks list with sections
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Remaining tasks section
            item {
                Text(
                    text = "Remaining Tasks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            val remainingTasks = tasks.filter { !it.isCompleted }
            if (remainingTasks.isEmpty()) {
                item {
                    Text(
                        "No remaining tasks",
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                    )
                }
            } else {
                items(remainingTasks) { task ->
                    TaskItem(
                        task = task,
                        onCheckChange = { checked ->
                            tasks = tasks.map {
                                if (it.id == task.id) it.copy(isCompleted = checked) else it
                            }
                        }
                    )
                }
            }

            // Divider between sections
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            // Completed tasks section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Completed Tasks",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    val completedTasks = tasks.filter { it.isCompleted }
                    TextButton(
                        onClick = { tasks = tasks.filter { !it.isCompleted } },
                        enabled = completedTasks.isNotEmpty()
                    ) {
                        Text("Clear Completed")
                    }
                }
            }

            val completedTasks = tasks.filter { it.isCompleted }
            if (completedTasks.isEmpty()) {
                item {
                    Text(
                        "No completed tasks",
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            } else {
                items(completedTasks) { task ->
                    TaskItem(
                        task = task,
                        onCheckChange = { checked ->
                            if (!checked) {
                                tasks = tasks.map {
                                    if (it.id == task.id) it.copy(isCompleted = false) else it
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onCheckChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = { onCheckChange(it) }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = task.description,
            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
            color = if (task.isCompleted) Color.Gray else Color.Unspecified
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TodoListPreview() {
    ToDoListTheme {
        TodoListApp()
    }
}