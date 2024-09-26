import Konva from "konva"
import { useEffect, useRef, useState } from "react"
import { Layer, Rect, Stage } from "react-konva"
import { useAppDispatch, useAppSelector } from "../../app/hooks"
import {
    buttonDown,
    buttonUp,
    editorV2Slice,
    mouseDown,
    mouseMove,
    mouseRelease,
} from "./editorv2Slice"
import { ButtonKeys, MouseKeys } from "./SpaceModel"

function mouseBtnToKey(button: number): MouseKeys | null {
    switch (button) {
        case 0:
            return "left"
        case 2:
            return "right"
        default:
            return null
    }
}

function keyboardBtnToKey(button: string): ButtonKeys | null {
    switch (button) {
        case " ":
            return "space"
        default:
            return null
    }
}

export function EditorV2() {
    const stage = useRef<Konva.Stage | null>(null)
    const state = useAppSelector(state => state.editorv2)
    const dispatch = useAppDispatch()

    useEffect(() => {
        const handleKeyDown: (ev: WindowEventMap["keydown"]) => any = evt => {
            const key = keyboardBtnToKey(evt.key)
            if (key) {
                dispatch(buttonDown({ key }))
            }
        }

        const handleKeyUp: (ev: WindowEventMap["keyup"]) => any = evt => {
            const key = keyboardBtnToKey(evt.key)
            if (key) {
                dispatch(buttonUp({ key }))
            }
        }

        window.addEventListener("keydown", handleKeyDown)
        window.addEventListener("keyup", handleKeyUp)

        // Cleanup function to remove the event listeners
        return () => {
            window.removeEventListener("keydown", handleKeyDown)
            window.removeEventListener("keyup", handleKeyUp)
        }
    }, [dispatch])

    return (
        <>
            <Stage
                style={{
                    border: "solid",
                    borderWidth: "1px",
                }}
                ref={stage}
                width={1200}
                height={800}
                onMouseDown={evt => {
                    const mouseKey = mouseBtnToKey(evt.evt.button)
                    const stageloc = stage.current
                    const pointerPos = stageloc?.getPointerPosition()
                    if (mouseKey && stageloc && pointerPos) {
                        dispatch(
                            mouseDown({
                                key: mouseKey,
                                x: pointerPos.x,
                                y: pointerPos.y,
                                targetId: undefined,
                            }),
                        )
                    }
                }}
                onMouseUp={evt => {
                    const mouseKey = mouseBtnToKey(evt.evt.button)
                    const stageloc = stage.current
                    const pointerPos = stageloc?.getPointerPosition()
                    if (mouseKey && stageloc && pointerPos) {
                        const stageCoord = stageloc.container().getBoundingClientRect()

                        dispatch(
                            mouseRelease({
                                key: mouseKey,
                                releaseX: evt.evt.clientX - stageCoord.x,
                                releaseY: evt.evt.clientY - pointerPos.y,
                            }),
                        )
                    }
                }}
                onMouseMove={evt => {
                    const stageloc = stage.current
                    const pointerPos = stageloc?.getPointerPosition()
                    if (stageloc && pointerPos) {
                        const stageCoord = stageloc.container().getBoundingClientRect()
                        dispatch(
                            mouseMove({
                                newX: evt.evt.clientX - stageCoord.x,
                                newY: evt.evt.clientY - stageCoord.y,
                            }),
                        )
                    }
                }}
            >
                <PatternBackground />
                {/* <Layer ref={elementsLayerRef}>
              {elements.map((el, index) => {
                return (
                  <AutoSizeTextShape
                    key={el.id}
                    element={el}
                    updatePosition={payload => {
                      dispatch(elementPositionUpdate(payload))
                    }}
                  />
                )
              })}
            </Layer> */}
            </Stage>
        </>
    )
}

function PatternBackground() {
    const [patternImage, setPatternImage] = useState(new window.Image())

    const offsetX = useAppSelector(state => state.editorv2.spaceViewer.offsetX)
    const offsetY = useAppSelector(state => state.editorv2.spaceViewer.offsetY)

    useEffect(() => {
        const dotPatternCanvas = document.createElement("canvas")
        const context = dotPatternCanvas.getContext("2d")!

        const spacing = 20 // Spacing between dots
        const dotRadius = 1 // Radius of the dots

        // Set the canvas size to the size of the pattern
        dotPatternCanvas.width = spacing
        dotPatternCanvas.height = spacing

        // Draw the dot pattern once
        context.beginPath()
        context.arc(spacing / 2, spacing / 2, dotRadius, 0, 2 * Math.PI)
        context.fillStyle = "black"
        context.fill()
        // Convert canvas to an image
        const pattern = new Image()
        pattern.src = dotPatternCanvas.toDataURL()
        pattern.onload = () => {
            setPatternImage(pattern)
        }
    }, [])

    return (
        <>
            <Layer>
                <Rect
                    draggable={false}
                    listening={false}
                    x={0}
                    y={0}
                    width={1200}
                    height={800}
                    fillPatternImage={patternImage}
                    fillPatternOffset={{
                        x: offsetX,
                        y: offsetY,
                    }}
                    fillPatternRepeat="repeat"
                />
            </Layer>
        </>
    )
}
