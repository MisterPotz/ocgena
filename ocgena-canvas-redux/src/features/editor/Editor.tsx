import { KonvaEventObject, NodeConfig } from "konva/lib/Node"
import { useEffect, useRef, useState } from "react"
import { Circle, Layer, Rect, Stage } from "react-konva"
import { useAppDispatch, useAppSelector } from "../../app/hooks"
import {
  elementSelected,
  Element,
  elementSelector,
  elementDragEpicTrigger,
  PositionUpdatePayload,
  elementDragEndEpicTrigger,
  RectangleShape,
  CircleShape,
  elementSelectionCancelled,
} from "./editorSlice"
import { CircleConfig } from "konva/lib/shapes/Circle"
import { RectConfig } from "konva/lib/shapes/Rect"
import { TextShape } from "./TextShape"
import Konva from "konva"
import { click } from "@testing-library/user-event/dist/cjs/convenience/click.js"

function dragEventToPayload(
  e: KonvaEventObject<DragEvent>,
): PositionUpdatePayload {
  return {
    id: e.target.id(),
    x: e.target.x(),
    y: e.target.y(),
  }
}

interface SelectorData {
  x: number
  y: number
}

type SelectorOrNull = SelectorData | null

export function Editor() {
  const dispatch = useAppDispatch()
  //   const elementIds = useAppSelector(elementIdSelector)
  const elements = useAppSelector(elementSelector)
  //   const selectedIds = useAppSelector(selectedIdsSelector)
  const stageRef = useRef<Konva.Stage | null>(null)
  const [selector, setSelector] = useState<SelectorOrNull>(null)
  const selectorDataRef = useRef<SelectorOrNull>(null)
  const selectorShapeRef = useRef<Konva.Rect | null>(null)
  const selectionLayer = useRef<Konva.Layer | null>(null)
  // State for storing elements
  const [tool, setTool] = useState<Tool | null>(null) // Current selected tool

  useEffect(() => {
    const windowMouseMoveCallback = (ev: MouseEvent) => {
      const selectorShape = selectorShapeRef.current
      const stage = stageRef.current!
      const selector = selectorDataRef.current
      if (!selector || !selectorShape) return
      // console.log("mouse move event", ev, "pointer position", pointerPosition)
      // Translate window coordinates to stage coordinates
      const pointerPosition = stage.getRelativePointerPosition()
      if (!pointerPosition) return
      const mouseX = ev.x
      const mouseY = ev.y

      const stageBoundingClientRect = stage.container().getBoundingClientRect()
      const stageClientRectX = stageBoundingClientRect.x + window.scrollX
      const stageCientRectY = stageBoundingClientRect.y + window.scrollY
      // const relativeMouseX =

      const x = pointerPosition.x
      const y = pointerPosition.y

      const startX = selector.x
      const startY = selector.y
      console.log(
        "mouse move event mous",
        { mouseX, mouseY },
        "pointer",
        { x, y },
        "start",
        { startX, startY },
      )
      selectorShape.setAttrs({
        x: Math.min(x, startX),
        y: Math.min(y, startY),
        width: Math.abs(x - startX),
        height: Math.abs(y - startY),
      })

      selectionLayer.current!.batchDraw()
    }
    const windowMouseUpCallback = (ev: MouseEvent) => {
      const selectorShape = selectorShapeRef.current
      const selector = selectorDataRef.current
      if (!selector || !selectorShape) return
      console.log("mouse up event", ev)
      setSelector(null)
      const selectionBox = selectorShape.getClientRect()
      // const selectedNodes = layer.getChildren(node => {
      //   const nodeBox = node.getClientRect()
      //   return Konva.Util.haveIntersection(selectionBox, nodeBox)
      // })

      // console.log("Selected nodes:", selectedNodes)
      // selectionRectangle.curr.destroy()
      selectionLayer.current!.draw()
    }

    window.addEventListener("mousemove", windowMouseMoveCallback)
    window.addEventListener("mouseup", windowMouseUpCallback)
    return () => {
      window.removeEventListener("mousemove", windowMouseMoveCallback)
      window.removeEventListener("mouseup", windowMouseUpCallback)
    }
  }, [])

  const handleElementDragEnd = (e: KonvaEventObject<DragEvent>) => {
    dispatch(elementDragEndEpicTrigger(dragEventToPayload(e)))
  }

  return (
    <>
      <ToolPane onSelectTool={setTool} />
      <Stage
        ref={stageRef}
        width={window.innerWidth}
        height={window.innerHeight}
        onMouseDown={(e: Konva.KonvaEventObject<MouseEvent>) => {
          console.log("stage mouse down target", e.target)
          const stage = stageRef.current!
          // Ignore clicks on shapes
          if (e.target !== stageRef.current) {
            return
          }
          dispatch(elementSelectionCancelled())
          const startX = stage.getRelativePointerPosition()?.x
          const startY = stage.getRelativePointerPosition()?.y

          if (startX && startY) {
            console.log("setting selector at", startX, startY)
            const selectorData = { x: startX, y: startY }
            selectorDataRef.current = selectorData
            setSelector(selectorData)
          }
        }}
      >
        <Layer
          onDragEnd={e => {
            dispatch(elementDragEndEpicTrigger(dragEventToPayload(e)))
          }}
          onClick={(e: Konva.KonvaEventObject<MouseEvent>) => {
            const clickedId = e.target?.id()
            console.log("clicked ", clickedId)
            if (clickedId) {
              dispatch(elementSelected(clickedId))
            }
          }}
        >
          {elements.map((el, index) => {
            return <TextShape key={el.id} {...el} />
          })}
        </Layer>
        <Layer ref={selectionLayer}>
          {selector && (
            <Rect
              ref={selectorShapeRef}
              x={selector.x}
              y={selector.y}
              width={10}
              height={10}
              draggable={false}
              fill={"#22B8EB"}
              opacity={0.3}
              strokeWidth={1}
              stroke={"#239EF4"}
            />
          )}
        </Layer>
      </Stage>
    </>
  )
}

export type Tool = "transition" | "place" | "var arc" | "normal arc"

export function ToolPane(props: { onSelectTool: (param: Tool) => void }) {
  const { onSelectTool } = props

  return (
    <div style={{ position: "absolute", top: 0, left: 0, zIndex: 1 }}>
      <button onClick={() => onSelectTool("transition")}>Transition</button>
      <button onClick={() => onSelectTool("place")}>Place</button>
      <button onClick={() => onSelectTool("var arc")}>Var Arc</button>
      <button onClick={() => onSelectTool("normal arc")}>Normal Arc</button>
      {}
    </div>
  )
}
