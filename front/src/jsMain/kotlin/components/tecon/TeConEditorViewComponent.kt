package components.tecon

import client
import components.common.OutlinedInputWithLabel
import components.tecon.editor.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import mui.material.*
import mui.system.sx
import org.webctc.common.types.PosInt2D
import org.webctc.common.types.tecon.TeCon
import org.webctc.common.types.tecon.shape.IShape
import org.webctc.common.types.tecon.shape.RailLine
import org.webctc.common.types.tecon.shape.Signal
import react.FC
import react.Props
import react.dom.html.ReactHTML.h2
import react.dom.svg.ReactSVG.line
import react.router.useNavigate
import react.useRef
import react.useState
import utils.removeAtNew
import utils.setNew
import web.cssom.*

external interface TeConEditorViewComponentProps : Props {
    var tecon: TeCon
}

val TeConEditorViewComponent = FC<TeConEditorViewComponentProps> { props ->
    val tecon = props.tecon

    var name by useState(tecon.name)
    var parts by useState(tecon.parts)

    var dotVisible by useState(true)
    val panzoomRef = useRef<dynamic>()

    var mode by useState(EditMode.HAND)

    var nowMousePos by useState(PosInt2D(0, 0))
    var selectedPosList by useState<List<PosInt2D>>(listOf())
    var selectedPart by useState<IShape?>(null)

    var sending by useState(false)

    val navigate = useNavigate()

    Box {
        sx {
            height = 100.pct
            display = Display.flex
            flexDirection = FlexDirection.row
        }
        SvgWithDot {
            dotVisibility = dotVisible
            cursorVisibility = mode.posCount > 0
            onClick = { _ ->
                if (mode.posCount > 0) {
                    var newSelectedPosList =
                        if (nowMousePos in selectedPosList) selectedPosList - nowMousePos
                        else selectedPosList + nowMousePos.copy()

                    if (newSelectedPosList.size == mode.posCount) {
                        parts += mode.create(newSelectedPosList)!!
                        newSelectedPosList = listOf()
                    }
                    selectedPosList = newSelectedPosList
                }
            }
            onUpdateMousePos = { nowMousePos = it }
            onInitPanzoom = { panzoomRef.current = it }

            parts.forEachIndexed { index, iShape ->
                val onSelect = { if (selectedPart == iShape) selectedPart = null else selectedPart = iShape }
                val onDelete = { parts = parts.removeAtNew(index) }
                val selected = selectedPart == iShape
                when (iShape) {
                    is RailLine -> RailLineElement {
                        rail = iShape
                        this.mode = mode
                        this.onSelect = onSelect
                        this.onDelete = onDelete
                        this.selected = selected
                    }

                    is Signal -> SignalElement {
                        signal = iShape
                        this.mode = mode
                        this.onSelect = onSelect
                        this.onDelete = onDelete
                        this.selected = selected
                    }

                    else -> {}
                }
            }

            if (mode == EditMode.RAIL && selectedPosList.isNotEmpty()) {
                val selectedPos = selectedPosList.first()
                line {
                    x1 = selectedPos.x.toDouble()
                    y1 = selectedPos.y.toDouble()
                    x2 = nowMousePos.x.toDouble()
                    y2 = nowMousePos.y.toDouble()
                    stroke = "white"
                    strokeWidth = 8.0
                    opacity = 0.5
                }
            }
        }
        Box {
            sx {
                height = 100.pct
                position = Position.relative
                display = Display.flex
                flexDirection = FlexDirection.rowReverse
            }
            Box {
                sx {
                    position = Position.absolute
                    width = 30.vw
                    height = 100.pct
                    borderRadius = 16.px
                    padding = 16.px
                    display = Display.flex
                    flexDirection = FlexDirection.column
                    gap = 16.px
                }
                Card {
                    sx {
                        backgroundColor = Color("white")
                        height = 100.pct
                        overflow = Auto.auto
                    }
                    CardContent {
                        h2 { +"Editor" }
                        Box {
                            sx {
                                display = Display.flex
                                flexDirection = FlexDirection.column
                                gap = 16.px
                            }

                            OutlinedInputWithLabel {
                                this.name = name
                                this.onChange = { name = it }
                            }

                            Box {
                                sx {
                                    display = Display.flex
                                    gap = 8.px
                                }
                                ToggleButtonHome { panzoom = panzoomRef.current }

                                ToggleButtonGroupZoom { panzoom = panzoomRef.current }

                                ToggleButtonGroupVisibility { onChange = { dotVisible = it } }
                            }
                            ToggleButtonGroupEditMode {
                                onChange = {
                                    if (it == EditMode.HAND) {
                                        panzoomRef.current?.resume()
                                    } else {
                                        panzoomRef.current?.pause()
                                    }
                                    mode = it
                                    selectedPosList = listOf()
                                }
                            }
                            Box {
                                sx {
                                    display = Display.flex
                                    justifyContent = JustifyContent.spaceBetween
                                }
                                Button {
                                    +"Save"
                                    variant = ButtonVariant.contained
                                    disabled = name == tecon.name && parts == tecon.parts || sending
                                    onClick = {
                                        sending = true

                                        val newTeCon = tecon.copy(name = name, parts = parts)
                                        MainScope().launch {
                                            client.put("/api/tecons/${tecon.uuid}") {
                                                contentType(ContentType.Application.Json)
                                                setBody(newTeCon)
                                            }
                                            tecon.updateBy(newTeCon)
                                            sending = false
                                        }
                                    }
                                }
                                Button {
                                    +"Delete"
                                    variant = ButtonVariant.outlined
                                    color = ButtonColor.error
                                    onClick = {
                                        MainScope().launch {
                                            client.delete("/api/tecons/${tecon.uuid}")
                                            navigate("/p/tecons")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (selectedPart != null) {
                    Card {
                        sx {
                            backgroundColor = Color("white")
                            height = 100.pct
                            overflow = Auto.auto
                        }
                        CardContent {
                            val onChange = { it: IShape ->
                                val index = parts.indexOf(selectedPart)
                                selectedPart = it
                                parts = parts.setNew(index, it)
                            }
                            when (selectedPart) {
                                is RailLine -> RailLineProperty {
                                    railLine = selectedPart as RailLine
                                    this.onChange = onChange
                                }

                                is Signal -> SignalProperty {
                                    signal = selectedPart as Signal
                                    this.onChange = onChange
                                }

                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    }
}