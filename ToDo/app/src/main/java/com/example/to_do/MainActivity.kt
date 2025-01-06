package com.example.to_do

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    private lateinit var todoViewModel: TodoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the Room database instance
        val db = TodoDatabase.getInstance(applicationContext)

        // Initialize the ViewModel
        todoViewModel = ViewModelProvider(this, TodoViewModelFactory(db)).get(TodoViewModel::class.java)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Pass the ViewModel to the TodoScreen
                    TodoScreen(viewModel = todoViewModel)
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(viewModel: TodoViewModel) {
    var taskInput by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = { paddingValues ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
    ) {
        TextField(
            value = taskInput,
            onValueChange = { taskInput = it },
            label = { Text("Enter a new task") },
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 8.dp, shape = CutCornerShape(4.dp), ambientColor = Color.Black)
                .background(Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                cursorColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Gray,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color.Blue,
                unfocusedBorderColor = Color.Gray

            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    if (taskInput.isNotBlank()) {
                        viewModel.addTodo(taskInput)
                        taskInput = ""
                    }
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .widthIn(min = 200.dp)
                    .height(56.dp)
                    .graphicsLayer {
                        shape = RoundedCornerShape(16.dp)
                        clip = true
                        shadowElevation = 100f
                        spotShadowColor = Color.Black.copy(alpha = 0.25f)
                    },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Task",
                    modifier = Modifier.size(32.dp),
                    tint = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        //Display the list of todos
        val todos by viewModel.todos.collectAsState()
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(todos) { todo ->
                TodoItem(
                    todo = todo,
                    onDelete = {
                        viewModel.removeTodo(todo)
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                            message = "Todo '${todo.task}' deleted",
                            actionLabel = "Undo",
                                duration = SnackbarDuration.Short
                        )
                            when (result) {
                                SnackbarResult.ActionPerformed -> {
                                    // Restore the deleted item
                                    viewModel.restoreTodo() // Implement this method in your ViewModel
                                }
                                SnackbarResult.Dismissed -> {
                                    // Handle the case where the Snackbar was dismissed
                                }
                            }

                        }
                               },
                    onToggleCompletion = { viewModel.toggleCompletion(todo) }
                )
            }
        }
    }
}
    )
    }

@Composable
fun TodoItem(todo: Todo, onDelete: () -> Unit, onToggleCompletion: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = todo.isCompleted,
            onCheckedChange = { onToggleCompletion() },
            colors = CheckboxDefaults.colors(
                checkedColor = Color.Black,
                uncheckedColor = Color.Black.copy(alpha = 0.6f)
            )
        )
        Text(
            text = todo.task,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        )
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete Task",
                tint = Color.Black
            )
        }
    }
}