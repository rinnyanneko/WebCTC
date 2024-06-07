package components.tecon.editor.element

import components.railgroup.detail.BoxRailGroupList
import mui.material.Box
import mui.material.Typography
import mui.system.sx
import org.webctc.common.types.railgroup.RailGroupState
import org.webctc.common.types.tecon.shape.RailLine
import react.FC
import react.dom.svg.ReactSVG.path
import web.cssom.FontSize
import web.cssom.FontWeight
import kotlin.math.abs

external interface RGStateElementProps : ITeConElementProps, PreviewElementProps {
    var rgState: Set<RailGroupState>?
}

external interface LineElementProps : RGStateElementProps, IShapeElementProps<RailLine>

val RailLineElement = FC<LineElementProps> { props ->
    val railLine = props.iShape
    val pos = railLine.pos
    val startPos = railLine.start
    val endPos = railLine.end

    val rgStateList = props.rgState ?: emptySet()

    val color =
        if (rgStateList.any { it.trainOnRail }) "red"
        else if (rgStateList.any { it.reserved }) "yellow"
        else if (rgStateList.any { it.locked }) "orange"
        else if (railLine.railGroupList.isEmpty()) "gray"
        else "white"

    ITeConElementBase {
        mode = props.mode
        onDelete = props.onDelete
        onSelect = props.onSelect
        fill = color
        selected = props.selected
        transform = "translate(${pos.x} ${pos.y})"
        path {
            val startX = startPos.x
            val startY = startPos.y
            val endX = endPos.x
            val endY = endPos.y
            val horizontal = abs((endY - startY).toDouble() / (endX - startX).toDouble()) > 1
            d = if (horizontal) {
                "M ${startX - 4} $startY L ${endX - 4} $endY L ${endX + 4} $endY L ${startX + 4} $startY Z"
            } else {
                "M $startX ${startY - 4} L $endX ${endY - 4} L $endX ${endY + 4} L $startX ${startY + 4} Z"
            }
            if (props.preview == true) {
                opacity = 0.5
            }
        }
    }
}

val RailLineProperty = FC<IShapePropertyElementProps<RailLine>> { props ->
    val railLine = props.iShape
    val uuids = props.iShape.railGroupList
    val onChange = props.onChange

    Box {
        Typography {
            sx {
                fontSize = FontSize.larger
                fontWeight = FontWeight.bold
            }
            +"RailLine"
        }
    }
    BoxRailGroupList {
        title = "Properties(RailLine)"
        railGroupList = uuids
        updateRailGroupList = { railLine.copy(railGroupList = it).also(onChange) }
    }
}
