package components.tecon.editor.element

import components.railgroup.detail.BoxRailGroupList
import mui.material.Box
import org.webctc.common.types.tecon.shape.RailPolyLine
import react.FC
import react.dom.html.ReactHTML.h2
import react.dom.svg.ReactSVG.polyline

external interface RailPolyLineElementProps : RGStateElementProps, IShapeElementProps<RailPolyLine>

val RailPolyLineElement = FC<RailPolyLineElementProps> { props ->
    val railPolyLine = props.iShape
    val pos = railPolyLine.pos

    val rgStateList = props.rgState ?: emptySet()

    val color =
        if (rgStateList.any { it.trainOnRail == true }) "red"
        else if (rgStateList.any { it.reserved == true }) "yellow"
        else if (rgStateList.any { it.locked == true }) "orange"
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
        h2 { +"RailPolyLine" }
    }
    BoxRailGroupList {
        title = "Properties(RailPolyLine)"
        railGroupList = uuids
        updateRailGroupList = { railLine.copy(railGroupList = it).also(onChange) }
    }
}

