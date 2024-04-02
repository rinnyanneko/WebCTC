import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import js.objects.jso
import org.webctc.common.types.kotlinxJson
import pages.*
import pages.tecon.TeConEdit
import pages.tecon.TeConList
import pages.tecon.TeConView
import react.create
import react.dom.client.createRoot
import react.router.RouterProvider
import react.router.dom.createBrowserRouter
import web.dom.document

val client = HttpClient(Js) {
    install(ContentNegotiation) {
        json(kotlinxJson)
    }
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(kotlinxJson)
    }
}

fun main() {
    val container = document.createElement("div").also {
        document.body.appendChild(it)
        document.body.style.apply { fontFamily = "'Noto Sans JP', sans-serif" }
    }

    val router = createBrowserRouter(arrayOf(
        jso {
            path = "/"
            children = arrayOf(
                jso {
                    index = true
                    element = MapView.create()
                }, jso {
                    path = "login"
                    element = Login.create()
                }, jso {
                    path = "p"
                    children = arrayOf(
                        jso {
                            path = "account"
                            element = Account.create()
                        }, jso {
                            path = "railgroup"
                            element = RailGroupManager.create()
                        }, jso {
                            path = "waypoint"
                            element = WayPointEditor.create()
                        }, jso {
                            path = "tecons"
                            children = arrayOf(
                                jso {
                                    index = true
                                    element = TeConList.create()
                                }, jso {
                                    path = "view/:uuid"
                                    element = TeConView.create()
                                }, jso {
                                    path = "edit/:uuid"
                                    element = TeConEdit.create()
                                })
                        })
                })
        }, jso {
            path = "*"
            element = FallBack.create()
        })
    )

    createRoot(container).render(RouterProvider.create { this.router = router })
}