package com.example.to_do

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TodoDao {
    @Insert
    fun insert(todo: Todo)

    @Query("SELECT * FROM todos ")
    fun getAllTodos(): List<Todo>

    @Update
   fun update(todo: Todo)

    @Query("DELETE FROM todos WHERE id = :id")
    fun deleteById(id: Long)
}
