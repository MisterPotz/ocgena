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
} from "./editorSlice"
import { CircleConfig } from "konva/lib/shapes/Circle"
import { RectConfig } from "konva/lib/shapes/Rect"
import { TextShape } from "./TextShape"
import Konva from "konva"

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
  const selectorRef = useRef<Konva.Rect | null>(null)
  const selectionLayer = useRef<Konva.Layer | null>(null)
  // State for storing elements
  const [tool, setTool] = useState<Tool | null>(null) // Current selected tool

  useEffect(() => {
    const windowMouseMoveCallback = (ev: MouseEvent) => {
      const selectorShape = selectorRef.current
      const stage = stageRef.current!
      if (!selector || !selectorShape) return
      console.log("mouse move event", ev)
      // Translate window coordinates to stage coordinates
      const pointerPosition = stage.getRelativePointerPosition()
      if (!pointerPosition) return
      const x = pointerPosition.x
      const y = pointerPosition.y
      const startX = selector.x
      const startY = selector.y

      selectorShape.setAttrs({
        x: Math.min(x, startX),
        y: Math.min(y, startY),
        width: Math.abs(x - startX),
        height: Math.abs(y - startY),
      })

      selectionLayer.current!.batchDraw()
    }
    const windowMouseUpCallback = (ev: MouseEvent) => {
      const selectorShape = selectorRef.current
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
  const handleElementDrag = (e: KonvaEventObject<DragEvent>) => {
    dispatch(elementDragEpicTrigger(dragEventToPayload(e)))
  }
  const handleElementDragEnd = (e: KonvaEventObject<DragEvent>) => {
    dispatch(elementDragEndEpicTrigger(dragEventToPayload(e)))
  }
  let testRectProps: Element<RectangleShape> = {
    id: "test_rect",
    shape: {
      height: 200,
      width: 400,
      type: "rect",
    },
    x: 300,
    y: 200,
    selected: false,
    fill: "orange",
  }
  return (
    <>
      <ToolPane onSelectTool={setTool} />
      <Stage
        ref={stageRef}
        width={window.innerWidth}
        height={window.innerHeight}
        // onMouseMove={(e: Konva.KonvaEventObject<MouseEvent>) => {
        //   const selectorShape = selectorRef.current
        //   const selectorData = selector

        //   const stage = stageRef.current!
        //   if (!selectorShape || !selectorData) return;

        //   const pointerPosition = stage.getPointerPosition();
        //   if (!pointerPosition) return;
        //   const x = pointerPosition.x;
        //   const y = pointerPosition.y;
        //   const startX = selectorData.x
        //   const startY = selectorData.y

        //   selectorShape.setAttrs({
        //     x: Math.min(x, startX),
        //     y: Math.min(y, startY),
        //     width: Math.abs(x - startX),
        //     height: Math.abs(y - startY),
        //   })
        //   selectionLayer.current!.batchDraw();
        // }}
        // onMouseUp={ () => setSelector(null)}
        onMouseDown={(e: Konva.KonvaEventObject<MouseEvent>) => {
          console.log("stage mouse down target", e.target)
          const stage = stageRef.current!
          // Ignore clicks on shapes
          if (e.target !== stageRef.current) {
            return
          }
          const startX = stage.getRelativePointerPosition()?.x
          const startY = stage.getPointerPosition()?.y

          if (startX && startY) { 
            console.log("setting selector")
            setSelector({ x: startX, y: startY })
          }
        }}
      >
        <Layer
          onDragEnd={handleElementDragEnd}
          onDragMove={handleElementDrag}
          // onClick={(ev: Konva.KonvaEventObject<MouseEvent>) => {
          //   console.log(ev)
          // }}
        >
          <TextShape {...testRectProps} />
          {elements.map((el, index) => {
            let render = renderFunction(el)
            console.log(render)
            return render
          })}
        </Layer>
        <Layer ref={selectionLayer}>
          {selector && (
            <Rect
              ref={selectorRef}
              x={selector.x}
              y={selector.y}
              draggable={false}
              fill={"#22B8EB"}
              opacity={0.83}
              strokeWidth={1}
              stroke={"#239EF4"}
            />
          )}
        </Layer>
      </Stage>
    </>
  )
}

function renderFunction(element: Element) {
  let nodeConfig: NodeConfig = {
    id: element.id,
    x: element.x,
    y: element.y,
    stroke: element.stroke,
    fill: element.fill,
    draggable: true,
    key: element.id,
    // onDragStart={} <--- want to create a common object to not define a callback to each rendered shape
  }
  switch (element.shape.type) {
    case "circle":
      let circleConfig: CircleConfig = {
        ...nodeConfig,
        radius: element.shape.radius,
      }
      return <Circle {...circleConfig} />
    case "rect":
      let rectConfig: RectConfig = {
        ...nodeConfig,
        width: element.shape.width,
        height: element.shape.height,
      }
      return <Rect {...rectConfig} />
      break
    default:
      break
  }
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
