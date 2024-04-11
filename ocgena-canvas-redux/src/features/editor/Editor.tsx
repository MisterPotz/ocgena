import { KonvaEventObject, NodeConfig } from "konva/lib/Node"
import { useState } from "react"
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

function dragEventToPayload(
  e: KonvaEventObject<DragEvent>,
): PositionUpdatePayload {
  return {
    id: e.target.id(),
    x: e.target.x(),
    y: e.target.y(),
  }
}

export function Editor() {
  const dispatch = useAppDispatch()
  //   const elementIds = useAppSelector(elementIdSelector)
  const elements = useAppSelector(elementSelector)
  //   const selectedIds = useAppSelector(selectedIdsSelector)

  const handleSelectElement = (id: string) => {
    dispatch(elementSelected(id))
  }
  //   const onElementDragged = (id: string, x: number, y: number) => {
  //     dispatch(elementPositionUpdate({ id, x, y }))
  //   }
  //   const onElementMoved = (id: string, x: number, y: number) => {
  //     dispatch(onElementMoved({ id, x, y }))
  //   }

  // State for storing elements
  const [tool, setTool] = useState<Tool | null>(null) // Current selected tool

  const handleElementDrag = (e: KonvaEventObject<DragEvent>) => {
    dispatch(elementDragEpicTrigger(dragEventToPayload(e)))
  }
  const handleElementDragEnd = (e: KonvaEventObject<DragEvent>) => {
    dispatch(elementDragEndEpicTrigger(dragEventToPayload(e)))
  }
  const handleStageMouseDown = (e: KonvaEventObject<MouseEvent>) => {
    console.log(
      "target mouse down",
      e.target,
      /* e.target._id, */ e.target.id(),
    )
    // Example: Add a rectangle if the rectangle tool is selected
    // if (tool === "transition") {¡
    //   const newElement: Element = {
    //     type: "transition",
    //     x: 50,
    //     y: 50,
    //     width: 100,
    //     height: 100,
    //   }
    //   setElements([...elements, newElement])
    //   setTool(null) // Reset tool or keep it selected based on your UX choice
    // }
    // Handle other tools similarly
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
        width={window.innerWidth}
        height={window.innerHeight}
        onMouseDown={handleStageMouseDown}
      >
        <Layer onDragEnd={handleElementDragEnd} onDragMove={handleElementDrag}>
          <TextShape {...testRectProps} />
          {elements.map((el, index) => {
            let render = renderFunction(el)
            console.log(render)
            return render
          })}
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
