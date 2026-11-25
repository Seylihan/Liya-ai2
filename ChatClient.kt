package com.liya.ai.ai

import com.liya.ai.data.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class ChatClient {
    private val client = OkHttpClient()
    suspend fun reply(url: String, key: String, model: String, history: List<ChatMessage>, memories: List<String>): String = withContext(Dispatchers.IO) {
        require(key.isNotBlank()) { "Ayarlar bölümüne API anahtarını eklemelisin." }
        val system = "Sen Liya adında, kadın karakterli, nazik ve doğal bir Türkçe kişisel asistansın. Bilmediğin şeyleri uydurma. Kullanıcının öğrettiği bilgilere önem ver. KAYITLI HAFIZA:\n" + memories.joinToString("\n") { "- $it" }
        val msgs = JSONArray().put(JSONObject().put("role", "system").put("content", system))
        history.takeLast(24).forEach { msgs.put(JSONObject().put("role", it.role).put("content", it.content)) }
        val body = JSONObject().put("model", model).put("messages", msgs).put("temperature", 0.7).toString().toRequestBody("application/json".toMediaType())
        val req = Request.Builder().url(url).addHeader("Authorization", "Bearer $key").post(body).build()
        client.newCall(req).execute().use { r ->
            val raw = r.body?.string().orEmpty()
            if (!r.isSuccessful) error("AI sunucusu ${r.code}: ${raw.take(250)}")
            JSONObject(raw).getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
        }
    }
}
