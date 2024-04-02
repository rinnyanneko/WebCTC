package components.tecon.editor.element

import components.railgroup.detail.TextFieldPosIntXYZ
import mui.material.*
import mui.material.Size
import mui.system.responsive
import mui.system.sx
import org.webctc.common.types.PosInt
import org.webctc.common.types.signal.SignalState
import org.webctc.common.types.tecon.shape.Signal
import pages.xs
import react.FC
import react.ReactNode
import react.create
import react.dom.svg.ReactSVG.circle
import react.dom.svg.ReactSVG.line
import web.cssom.*

external interface SignalElementProps : ITeConElementProps, IShapeElementProps<Signal> {
    var signalState: SignalState?
}

val SignalElement = FC<SignalElementProps> { props ->
    val signal = props.iShape
    val pos = signal.pos
    val rotation = signal.rotation
    val signalLevel = props.signalState?.level ?: 0

    ITeConElementBase {
        mode = props.mode
        onDelete = props.onDelete
        onSelect = props.onSelect
        fill = (if (signalLevel > 1) "LawnGreen" else "#202020")
        stroke = if (signal.signalPos == PosInt.ZERO) "gray" else "white"
        selected = props.selected
        transform = "translate(${pos.x} ${pos.y}) rotate(${rotation ?: 0})"
        rotation?.let {
            line {
                x1 = 0.0
                y1 = 12.0
                x2 = 0.0
                y2 = 32.0
                strokeWidth = 4.0
            }
            line {
                x1 = 12.0
                y1 = 32.0
                x2 = -12.0
                y2 = 32.0
                strokeWidth = 4.0
            }
        }
        circle {
            r = 12.0
            strokeWidth = 4.0
        }
    }
}
val SignalProperty = FC<IShapePropertyElementProps<Signal>> { props ->
    val signal = props.iShape
    val onChange = props.onChange
    val pos = signal.signalPos
    Box {
        h2 {
            +"Signal"
        }
        Box {
            sx {
                display = Display.flex
                padding = 6.px
                gap = 8.px
            }
            TextFieldPosIntXYZ {
                this.pos = pos
                this.onChange = { onChange(signal.copy(signalPos = it)) }
            }
        }
        Box {
            Box {
                FormControlLabel {
                    label = ReactNode("Show Baseline")
                    control = Checkbox.create {
                        checked = signal.rotation != null
                        this.onChange = { _, checked -> onChange(signal.copy(rotation = if (checked) 0 else null)) }
                    }
                }
            }
            Grid {
                container = true
                spacing = responsive(2)
                sx {
                    alignItems = AlignItems.center
                }

                Grid {
                    item = true
                    xs = true
                    Slider {
                        disabled = signal.rotation == null
                        value = signal.rotation ?: 0
                        min = 0.0
                        max = 360.0
                        step = 1.0
                        this.onChange = { _, value, _ ->
                            onChange(signal.copy(rotation = value.toString().toInt()))
                        }
                    }
                }
                Grid {
                    item = true
                    OutlinedInput {
                        disabled = signal.rotation == null
                        value = signal.rotation ?: 0
                        size = Size.small
                        type = "number"
                        this.onChange = { event ->
                            val value = event.target.asDynamic().value.toString().toIntOrNull() ?: 0
                            val clamp = value.coerceIn(0, 360)
                            onChange(signal.copy(rotation = clamp))
                        }
                    }
                }
            }
        }
    }
}