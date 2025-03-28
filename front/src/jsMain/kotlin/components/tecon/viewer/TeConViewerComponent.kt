package components.tecon.viewer

import components.tecon.editor.SvgWithDot
import components.tecon.editor.element.RailLineElement
import components.tecon.editor.element.RailPolyLineElement
import components.tecon.editor.element.RectBoxElement
import components.tecon.editor.element.SignalElement
import org.webctc.common.types.railgroup.RailGroupState
import org.webctc.common.types.signal.SignalState
import org.webctc.common.types.tecon.shape.*
import react.FC
import react.Props
import utils.useListDataWS

external interface TeConViewerProps : Props {
    var parts: List<IShape>
}

val TeConViewer = FC<TeConViewerProps> { props ->
    val parts = props.parts

    val rgStateList by useListDataWS<RailGroupState>(
        "/api/railgroups/state/ws",
        props.parts.filterIsInstance<RailShape>().map { it.railGroupList }.flatten().toSet()
    ) { a, b -> a.uuid == b.uuid }

    val signalStateList by useListDataWS<SignalState>(
        "/api/signals/state/ws",
        props.parts.filterIsInstance<Signal>().map { it.signalPos }.toSet()
    ) { a, b -> a.pos == b.pos }

    SvgWithDot {
        dotVisibility = false
        cursorVisibility = false

        parts.forEach {
            when (it) {
                is RailLine -> RailLineElement {
                    iShape = it
                    rgState = rgStateList.filter { rg -> rg.uuid in it.railGroupList }.toSet()
                }

                is RailPolyLine -> RailPolyLineElement {
                    iShape = it
                    rgState = rgStateList.filter { rg -> rg.uuid in it.railGroupList }.toSet()
                }

                is Signal -> SignalElement {
                    iShape = it
                    signalState = signalStateList.find { ss -> it.signalPos == ss.pos }
                }

                is RectBox -> RectBoxElement {
                    iShape = it
                }
            }
        }
    }
}