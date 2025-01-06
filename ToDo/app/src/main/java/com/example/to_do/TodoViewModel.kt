package com.example.to_do

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TodoViewModel(private val database: TodoDatabase) : ViewModel() {
    private val _todos = MutableStateFlow<List<Todo>>(emptyList())
    val todos: StateFlow<List<Todo>> = _todos

    private var recentlyDeletedTodo: Todo? = null

    init {
        getTodos()
    }

    private fun getTodos() {
        viewModelScope.launch {
            _todos.value = withContext(Dispatchers.IO) {
                database.todoDao().getAllTodos()
            }
        }
    }

    fun addTodo(description: String) {
        val newTodo = Todo(task = description)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.todoDao().insert(newTodo)
            }
            getTodos()
        }
    }

    fun removeTodo(todo: Todo) {
        recentlyDeletedTodo = todo // Store the deleted item
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.todoDao().deleteById(todo.id)
            }
            getTodos()
        }
    }

    fun restoreTodo() {
        recentlyDeletedTodo?.let { todo ->
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    database.todoDao().insert(todo) // Restore the deleted item
                }
                getTodos()
                recentlyDeletedTodo = null // Clear the reference
            }
        }
    }

    fun toggleCompletion(todo: Todo) {
        val updatedTodo = todo.copy(isCompleted = !todo.isCompleted)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.todoDao().update(updatedTodo)
            }
            getTodos()
        }
    }
}
