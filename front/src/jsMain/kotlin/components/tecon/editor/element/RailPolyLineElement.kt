package components.tecon.editor.element

import components.railgroup.detail.BoxRailGroupList
import mui.material.Box
import mui.material.Typography
import mui.system.sx
import org.webctc.common.types.tecon.shape.RailPolyLine
import react.FC
import react.dom.svg.ReactSVG.polyline
import web.cssom.FontSize
import web.cssom.FontWeight

external interface RailPolyLineElementProps : RGStateElementProps, IShapeElementProps<RailPolyLine>

val RailPolyLineElement = FC<RailPolyLineElementProps> { props ->
    val railPolyLine = props.iShape
    val pos = railPolyLine.pos

    val rgStateList = props.rgState ?: emptySet()

    val color =
        if (rgStateList.any { it.trainOnRail }) "red"
        else if (rgStateList.any { it.reserved }) "yellow"
        else if (rgStateList.any { it.locked }) "orange"
        else if (railPolyLine.railGroupList.isEmpty()) "gray"
        else "white"

    ITeConElementBase {
        mode = props.mode
        onDelete = props.onDelete
        onSelect = props.onSelect
        stroke = color
        transform = "translate(${pos.x} ${pos.y})"

        polyline {
            strokeWidth = 8.0
            points = railPolyLine.points.joinToString(" ") { "${it.x},${it.y}" }
            if (props.preview == true) {
                opacity = 0.5
            }
        }
    }
}

val RailPolyLineProperty = FC<IShapePropertyElementProps<RailPolyLine>> { props ->
    val railLine = props.iShape
    val uuids = props.iShape.railGroupList
    val onChange = props.onChange

    Box {
        Typography {
            sx {
                fontSize = FontSize.larger
                fontWeight = FontWeight.bold
            }
            +"RailPolyLine"
        }
    }
    BoxRailGroupList {
        title = "Properties(RailPolyLine)"
        railGroupList = uuids
        updateRailGroupList = { railLine.copy(railGroupList = it).also(onChange) }
    }
}

