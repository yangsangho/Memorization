package kr.yangbob.memorization.db

import androidx.room.*

@Dao
interface TestDao
{
    @Query("SELECT * FROM test")
    suspend fun getAll(): List<Test>

    @Query("SELECT " + "(SELECT title FROM Qst WHERE id = qst_id) AS title, caldata_id, isCorrect, stage From Test")
    suspend fun getAllIncTitle(): List<TestIncTitle>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(test: Test)

    @Delete
    fun delete(test: Test)
}