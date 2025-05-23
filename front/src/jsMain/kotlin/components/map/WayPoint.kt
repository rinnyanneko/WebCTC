package components.map

import emotion.react.css
import org.webctc.common.types.waypoint.WayPoint
import org.webctc.common.types.waypoint.range.CircleRange
import org.webctc.common.types.waypoint.range.RectangleRange
import react.FC
import react.Props
import react.dom.html.ReactHTML.title
import react.dom.svg.ReactSVG.circle
import react.dom.svg.ReactSVG.g
import react.dom.svg.ReactSVG.rect
import react.dom.svg.ReactSVG.text
import web.cssom.None

external interface WayPointProps : Props {
    var wayPoint: WayPoint
    var scale: Double?
}

val WWayPoint = FC<WayPointProps> {
    val wayPoint = it.wayPoint
    val pos = wayPoint.pos
    val range = wayPoint.range

    g {
        css {
            pointerEvents = None.none
        }
        when (range) {
            is CircleRange -> {
                circle {
                    cx = range.center.x
                    cy = range.center.z
                    r = range.radius
                    fill = "yellow"
                    fillOpacity = "0.05"
                }
            }

            is RectangleRange -> {
                rect {
                    x = range.minX
                    y = range.minZ
                    width = range.width
                    height = range.height
                    fill = "yellow"
                    fillOpacity = "0.05"
                }
            }
        }
        g {
            transform = "translate(${pos.x} ${pos.z}) scale(${2 / (it.scale ?: 1.0)})"
            rect {
                x = (-2 - 4 * wayPoint.displayName.length).toDouble()
                y = -8.0
                width = 8.0 * wayPoint.displayName.length + 4
                height = 10.0
                fill = "black"
                fillOpacity = "0.8"
                rx = 2.0
                ry = 2.0
            }
            text {
                +wayPoint.displayName
                fill = "white"
                fontSize = 8.0
                fontWeight = "bold"
                textAnchor = "middle"

            }
            title {
                +wayPoint.identifyName
            }
        }
    }
}