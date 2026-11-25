package com.liya.ai

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.liya.ai.ai.ChatClient
import com.liya.ai.data.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

private val Bg = Color(0xFF100F18); private val Purple = Color(0xFF9B7BFF); private val Card = Color(0xFF211E2D)
class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private lateinit var dao: LiyaDao; private lateinit var settings: SecureSettings; private lateinit var tts: TextToSpeech
    private var recognizer: SpeechRecognizer? = null; private var speechResult: ((String) -> Unit)? = null
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); dao = LiyaDb.get(this).dao(); settings = SecureSettings(this); tts = TextToSpeech(this, this); setContent { Screen() } }
    override fun onInit(status: Int) { if (status == TextToSpeech.SUCCESS) tts.language = Locale("tr", "TR") }
    private fun speak(s: String) { tts.speak(s, TextToSpeech.QUEUE_FLUSH, null, "liya") }
    private fun listen(result: (String) -> Unit) {
        speechResult = result; if (!SpeechRecognizer.isRecognitionAvailable(this)) return
        recognizer?.destroy(); recognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onResults(b: Bundle?) { b?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let { speechResult?.invoke(it) } }
                override fun onError(e: Int) {}; override fun onReadyForSpeech(p: Bundle?) {}; override fun onBeginningOfSpeech() {}; override fun onRmsChanged(r: Float) {}; override fun onBufferReceived(b: ByteArray?) {}; override fun onEndOfSpeech() {}; override fun onPartialResults(b: Bundle?) {}; override fun onEvent(t: Int, b: Bundle?) {}
            })
            startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM).putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR"))
        }
    }
    @Composable private fun Screen() {
        var rows by remember { mutableStateOf(emptyList<ChatMessage>()) }; var memories by remember { mutableStateOf(emptyList<Memory>()) }
        var input by remember { mutableStateOf("") }; var busy by remember { mutableStateOf(false) }; var settingsPage by remember { mutableStateOf(false) }
        var key by remember { mutableStateOf(settings.apiKey) }; var url by remember { mutableStateOf(settings.endpoint) }; var model by remember { mutableStateOf(settings.model) }
        val scope = rememberCoroutineScope()
        val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok -> if (ok) listen { input = it } }
        LaunchedEffect(Unit) { launch { dao.messages().collectLatest { rows = it } }; launch { dao.memories().collectLatest { memories = it } } }
        fun send() {
            val text = input.trim(); if (text.isEmpty() || busy) return; input = ""
            scope.launch {
                dao.addMessage(ChatMessage(role = "user", content = text))
                val teach = listOf("Liya, öğren:", "Öğren:", "Bunu hatırla:", "Şunu hatırla:").firstOrNull { text.startsWith(it, true) }
                if (teach != null && text.substringAfter(":").isNotBlank()) {
                    dao.addMemory(Memory(content = text.substringAfter(":").trim()))
                    dao.addMessage(ChatMessage(role = "assistant", content = "Bunu hafızama kaydettim. ❤️"))
                } else {
                    busy = true
                    try { val answer = ChatClient().reply(settings.endpoint, settings.apiKey, settings.model, rows + ChatMessage(role = "user", content = text), dao.recentMemories().map { it.content }); dao.addMessage(ChatMessage(role = "assistant", content = answer)); speak(answer) }
                    catch (e: Exception) { dao.addMessage(ChatMessage(role = "assistant", content = "Şu anda cevap veremedim: ${e.message ?: "Bağlantı hatası"}")) }
                    finally { busy = false }
                }
            }
        }
        MaterialTheme(colorScheme = darkColorScheme(primary = Purple, background = Bg, surface = Card)) {
            Column(Modifier.fillMaxSize().background(Bg).padding(14.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { Text("LIYA", style = MaterialTheme.typography.headlineMedium, color = Color.White); Text("Kişisel yapay zekân", color = Purple) }
                    TextButton(onClick = { settingsPage = !settingsPage }) { Text(if (settingsPage) "Sohbet" else "Ayarlar") }
                }
                if (settingsPage) {
                    Text("AI bağlantısı", color = Color.White, style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(key, { key = it }, label = { Text("API anahtarı") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(url, { url = it }, label = { Text("Chat Completions URL") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(model, { model = it }, label = { Text("Model adı") }, modifier = Modifier.fillMaxWidth())
                    Button(onClick = { settings.apiKey = key.trim(); settings.endpoint = url.trim(); settings.model = model.trim(); settingsPage = false }) { Text("Kaydet") }
                    Text("API anahtarı cihazda şifreli ayarlarda saklanır. Mesajların AI sağlayıcısına gönderilir.", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                    Text("Öğretme komutu: Liya, öğren: ...", color = Purple)
                } else {
                    LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        if (rows.isEmpty()) item { Text("Merhaba! Ben Liya. ❤️\nBana sohbet ederek kendimi öğretebilirsin.", color = Color.White) }
                        items(rows, key = { it.id }) { m -> Row(Modifier.fillMaxWidth(), horizontalArrangement = if (m.role == "user") Arrangement.End else Arrangement.Start) { Surface(color = if (m.role == "user") Color(0xFF493A70) else Card, shape = RoundedCornerShape(16.dp)) { Text(m.content, Modifier.padding(12.dp), color = Color.White) } } }
                        if (busy) item { Text("Liya yazıyor…", color = Purple) }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(input, { input = it }, modifier = Modifier.weight(1f), placeholder = { Text("Liya'ya yaz...") }, maxLines = 4)
                        IconButton(onClick = { micPermission.launch(Manifest.permission.RECORD_AUDIO) }) { Icon(Icons.Default.Mic, "Sesle konuş", tint = Purple) }
                        IconButton(onClick = { send() }, enabled = !busy) { Icon(Icons.Default.Send, "Gönder", tint = Purple) }
                    }
                    Text("${memories.size} hafıza kaydı • Öğret: Liya, öğren: ...", color = Color.LightGray, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
    override fun onDestroy() { recognizer?.destroy(); tts.stop(); tts.shutdown(); super.onDestroy() }
}
