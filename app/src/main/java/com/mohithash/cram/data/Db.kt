package com.mohithash.cram.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "sets")
data class StudySet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subject: String = "",
    /** The source text (or a note that it came from a photo). */
    val source: String = "",
    /** domain.StudyContent JSON */
    val json: String,
    val bestScore: Int = -1,
    /** Comma-separated indices of terms marked known. */
    val known: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastStudied: Long = 0,
)

@Dao
interface SetDao {
    @Query("SELECT * FROM sets ORDER BY createdAt DESC") fun all(): Flow<List<StudySet>>
    @Insert suspend fun insert(s: StudySet): Long
    @Update suspend fun update(s: StudySet)
    @Query("DELETE FROM sets WHERE id = :id") suspend fun delete(id: Long)
    @Query("SELECT * FROM sets WHERE id = :id") suspend fun get(id: Long): StudySet?
}

@Database(entities = [StudySet::class], version = 1, exportSchema = false)
abstract class AppDb : RoomDatabase() { abstract fun sets(): SetDao }
