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
import org.jetbrains.ktor.netty.Netty
import org.jetbrains.ktor.routing.Routing
import org.jetbrains.ktor.websocket.*

val gson = Gson()
var state: Boolean = false
var trueTime: Long = 0
var falseTime: Long = 0
var lastUpdatedAt: Long = System.currentTimeMillis()
val clients: ArrayList<WebSocketSession> = ArrayList()

var timeString: String = ""
    get() = "time:$trueTime:$falseTime"

fun main(args: Array<String>) {
    embeddedServer(Netty, 8080) {
        install(DefaultHeaders)
        install(CallLogging)
        install(WebSockets)
        install(Routing) {
            static {
                files(".")
                default("index.html")
            }

//            post("/toggle") {
//                toggle()
//                call.respondText("Hello, world!", ContentType.Text.Html)
//            }

            webSocket("/ws") {
                clients.add(this)

                this.send(Frame.Text(gson.toJson(Update(state, trueTime, falseTime))))

                try {
                    incoming.consumeEach { frame ->
                        if (frame is Frame.Text) {
                            val text = frame.readText()
                            println("message from client: $text")
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

    if (previousState)
        trueTime += (System.currentTimeMillis() - lastUpdatedAt)
    else
        falseTime += (System.currentTimeMillis() - lastUpdatedAt)
    lastUpdatedAt = System.currentTimeMillis()
    if (state != previousState) {
        updateClients()
    }
}

fun updateClients() {
    println("updating ${clients.size} clients")
    for (client in clients) {
        async(CommonPool) { client.send(Frame.Text(gson.toJson(Update(state, trueTime, falseTime)))) }
    }
}