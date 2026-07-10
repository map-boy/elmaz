package com.nyumbahub.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nyumbahub.core.ui.theme.NavyPrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class AiMessage(val role: String, val content: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WandaaAiChatScreen(onBack: () -> Unit) {
    val hfToken = BuildConfig.HF_TOKEN
    val hfModel = BuildConfig.HF_MODEL
    val groqKey = BuildConfig.GROQ_API_KEY
    var messages by remember { mutableStateOf(listOf<AiMessage>()) }
    var input by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("WANDAA AI", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Property assistant for Rwanda", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavyPrimary,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(8.dp)
                    .navigationBarsPadding()
                    .imePadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Ask about properties in Rwanda...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3,
                    enabled = !isLoading
                )
                IconButton(
                    onClick = {
                        val text = input.trim()
                        if (text.isEmpty() || isLoading) return@IconButton
                        input = ""
                        val newMessages = messages + AiMessage("user", text)
                        messages = newMessages
                        isLoading = true
                        scope.launch {
                            val reply = callHuggingFace(hfToken, hfModel, groqKey, newMessages)
                            messages = newMessages + AiMessage("assistant", reply)
                            isLoading = false
                        }
                    },
                    enabled = !isLoading && input.isNotBlank()
                ) {
                    Icon(
                        Icons.Default.Send, null,
                        tint = if (!isLoading && input.isNotBlank()) NavyPrimary else Color.Gray
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillParentMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("\uD83C\uDFE0", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("WANDAA AI", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = NavyPrimary)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Ask me anything about renting,\nbuying or selling property in Rwanda",
                            color = Color.Gray, fontSize = 14.sp, textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(24.dp))
                        listOf("Find rentals in Kigali", "How to list my house?", "What is average rent in Kimironko?").forEach { suggestion ->
                            SuggestionChip(
                                onClick = {
                                    input = suggestion
                                },
                                label = { Text(suggestion, fontSize = 12.sp) },
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            items(messages) { msg -> AiMessageBubble(msg) }
            if (isLoading) {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                        Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFFF0F0F0)) {
                            Text(
                                "WANDAA is thinking...",
                                modifier = Modifier.padding(12.dp),
                                color = Color.Gray, fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AiMessageBubble(msg: AiMessage) {
    val isUser = msg.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) NavyPrimary else Color(0xFFF0F0F0),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                msg.content,
                modifier = Modifier.padding(12.dp),
                color = if (isUser) Color.White else Color.Black,
                fontSize = 14.sp
            )
        }
    }
}

private val SYSTEM_PROMPT = "You are WANDAA AI, a helpful real estate assistant for Elmaz, a property platform in Rwanda. Help users with listings, renting, buying, selling, and real estate advice in Rwanda. Be concise and friendly."

private fun buildMessagesJson(messages: List<AiMessage>): JSONArray {
    val msgs = JSONArray()
    msgs.put(JSONObject().apply {
        put("role", "system")
        put("content", SYSTEM_PROMPT)
    })
    messages.forEach { msg ->
        msgs.put(JSONObject().apply {
            put("role", msg.role)
            put("content", msg.content)
        })
    }
    return msgs
}

private fun callChatApi(urlStr: String, token: String, model: String, messages: List<AiMessage>): Pair<Boolean, String> {
    return try {
        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer $token")
        conn.doOutput = true
        conn.connectTimeout = 20000
        conn.readTimeout = 45000

        val body = JSONObject().apply {
            put("model", model)
            put("messages", buildMessagesJson(messages))
            put("max_tokens", 512)
            put("stream", false)
        }
        conn.outputStream.use { it.write(body.toString().toByteArray()) }

        val status = conn.responseCode
        val stream = if (status in 200..299) conn.inputStream else conn.errorStream
        val response = BufferedReader(InputStreamReader(stream)).use { it.readText() }

        if (status !in 200..299) {
            return false to "[$model] HTTP $status: $response"
        }

        val text = JSONObject(response)
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
            .trim()
        true to text
    } catch (e: Exception) {
        false to "[$model] ${e.javaClass.simpleName}: ${e.message}"
    }
}

private suspend fun callHuggingFace(
    hfToken: String,
    hfModel: String,
    groqKey: String,
    messages: List<AiMessage>
): String {
    return withContext(Dispatchers.IO) {
        val errors = mutableListOf<String>()

        if (groqKey.isNotBlank()) {
            val (ok, result) = callChatApi(
                "https://api.groq.com/openai/v1/chat/completions",
                groqKey,
                "llama-3.3-70b-versatile",
                messages
            )
            if (ok) return@withContext result
            errors.add(result)
        }

        if (hfToken.isNotBlank()) {
            val model = hfModel.ifBlank { "openai/gpt-oss-120b:cerebras" }
            val (ok, result) = callChatApi(
                "https://router.huggingface.co/v1/chat/completions",
                hfToken,
                model,
                messages
            )
            if (ok) return@withContext result
            errors.add(result)

            val (ok2, result2) = callChatApi(
                "https://router.huggingface.co/v1/chat/completions",
                hfToken,
                "meta-llama/Llama-3.1-8B-Instruct",
                messages
            )
            if (ok2) return@withContext result2
            errors.add(result2)
        }

        "Sorry, I couldn't connect right now. Please try again.`n" + errors.joinToString("`n")
    }
}
