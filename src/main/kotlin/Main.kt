import org.jetbrains.ktor.routing.*
import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.content.default
import org.jetbrains.ktor.content.files
import org.jetbrains.ktor.content.static
import org.jetbrains.ktor.features.CallLogging
import org.jetbrains.ktor.features.DefaultHeaders
import org.jetbrains.ktor.host.*
import org.jetbrains.ktor.http.*
import org.jetbrains.ktor.netty.Netty
import org.jetbrains.ktor.response.*
import org.jetbrains.ktor.websocket.WebSockets
import org.jetbrains.ktor.websocket.webSocket

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

            post("/toggle") {
                toggle()
                call.respondText("Hello, world!", ContentType.Text.Html)
            }

            webSocket("/ws") {
//                val session = call.sessions.get()
            }
        }
    }.start(wait = true)
}

enum class COLOR {
    BLUE, RED
}

var currentColor: COLOR = COLOR.BLUE

fun toggle() {
    when (currentColor) {
        COLOR.BLUE -> currentColor = COLOR.RED
        COLOR.RED -> currentColor = COLOR.BLUE
    }
}