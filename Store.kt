package com.liya.ai.data

import android.content.Context
import androidx.room.*
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "messages")
data class ChatMessage(@PrimaryKey(autoGenerate = true) val id: Long = 0, val role: String, val content: String, val createdAt: Long = System.currentTimeMillis())
@Entity(tableName = "memories")
data class Memory(@PrimaryKey(autoGenerate = true) val id: Long = 0, val content: String, val createdAt: Long = System.currentTimeMillis())

@Dao interface LiyaDao {
    @Query("SELECT * FROM messages ORDER BY createdAt ASC") fun messages(): Flow<List<ChatMessage>>
    @Insert suspend fun addMessage(m: ChatMessage)
    @Query("SELECT * FROM memories ORDER BY createdAt DESC") fun memories(): Flow<List<Memory>>
    @Query("SELECT * FROM memories ORDER BY createdAt DESC LIMIT 30") suspend fun recentMemories(): List<Memory>
    @Insert suspend fun addMemory(m: Memory)
}
@Database(entities = [ChatMessage::class, Memory::class], version = 1, exportSchema = false)
abstract class LiyaDb : RoomDatabase() {
    abstract fun dao(): LiyaDao
    companion object { @Volatile private var instance: LiyaDb? = null; fun get(c: Context) = instance ?: synchronized(this) { instance ?: Room.databaseBuilder(c.applicationContext, LiyaDb::class.java, "liya_memory.db").build().also { instance = it } } }
}
class SecureSettings(c: Context) {
    private val key = MasterKey.Builder(c).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
    private val p = EncryptedSharedPreferences.create(c, "liya_settings", key, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
    var apiKey: String get() = p.getString("key", "") ?: ""; set(v) { p.edit().putString("key", v).apply() }
    var endpoint: String get() = p.getString("url", "https://api.openai.com/v1/chat/completions") ?: ""; set(v) { p.edit().putString("url", v).apply() }
    var model: String get() = p.getString("model", "gpt-4o-mini") ?: ""; set(v) { p.edit().putString("model", v).apply() }
}
