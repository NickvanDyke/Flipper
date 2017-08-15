import com.google.gson.Gson
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.consumeEach
import org.jetbrains.ktor.application.install
import org.jetbrains.ktor.content.default
import org.jetbrains.ktor.content.files
import org.jetbrains.ktor.content.static
import org.jetbrains.ktor.features.CallLogging
import org.jetbrains.ktor.features.DefaultHeaders
import org.jetbrains.ktor.host.embeddedServer
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.netty.Netty
import org.jetbrains.ktor.response.respondText
import org.jetbrains.ktor.routing.Routing
import org.jetbrains.ktor.routing.get
import org.jetbrains.ktor.websocket.*
import java.util.*

val gson = Gson()
var state: Boolean = false
var trueMs: Long = 0
var falseMs: Long = 0
var lastUpdatedAt: Long = System.currentTimeMillis()
val clients: ArrayList<WebSocketSession> = ArrayList()

var updateJson: String = ""
    get() = gson.toJson(Update(state, trueMs, falseMs))

fun main(args: Array<String>) {
    Timer().scheduleAtFixedRate(object : TimerTask() {
        override fun run() {
            updateTime()
            updateClients()
        }
    }, 1000, 1000)
    embeddedServer(Netty, 8080) {
        install(DefaultHeaders)
        install(CallLogging)
        install(WebSockets)
        install(Routing) {
            static {
                files(".")
                default("index.html")
            }

            get("/status") {
                call.respondText(updateJson, ContentType.Application.Json)
            }

            webSocket("/ws") {
                clients.add(this)

                this.send(Frame.Text(updateJson))

                try {
                    incoming.consumeEach { frame ->
                        if (frame is Frame.Text) {
                            val text = frame.readText()
                            handleClientMsg(text)
                        }
                    }
                } finally {
                    clients.remove(this)
                }
            }
        }
    }.start(wait = true)
}

fun handleClientMsg(text: String) {
    val previousState = state
    if (text == "true")
        state = true
    else if (text == "false")
        state = false
    else
        return
    updateTime()
    if (state != previousState) {
        updateClients()
    }
}

fun updateTime() {
    if (state)
        trueMs += (System.currentTimeMillis() - lastUpdatedAt)
    else
        falseMs += (System.currentTimeMillis() - lastUpdatedAt)
    lastUpdatedAt = System.currentTimeMillis()
}

fun updateClients() {
    for (client in clients) {
        async(CommonPool) { client.send(Frame.Text(updateJson)) }
    }
}