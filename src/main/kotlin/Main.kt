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

var checked: Boolean = false
var trueTime: Long = 0
var falseTime: Long = 0
val clients: ArrayList<WebSocketSession> = ArrayList()

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

                this.send(Frame.Text(checked.toString()))

                try {
                    incoming.consumeEach { frame ->
                        if (frame is Frame.Text) {
                            val text = frame.readText()
                            println("message from client: $text")
                            if (text == "true")
                                checked = true
                            else if (text == "false")
                                checked = false
                            updateClients()
                        }
                    }
                } finally {
                    clients.remove(this)
                }
            }
        }
    }.start(wait = true)
}

fun updateClients() {
    println("updating ${clients.size} clients")
    for (client in clients) {
        async(CommonPool) { client.send(Frame.Text(checked.toString())) }
    }
}