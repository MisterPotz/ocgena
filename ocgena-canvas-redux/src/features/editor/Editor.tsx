import { Tuple } from "@reduxjs/toolkit"
import { KonvaEventObject, NodeConfig } from "konva/lib/Node"
import { useEffect, useRef, useState } from "react"
import {
  Circle,
  Group,
  Layer,
  Rect,
  Stage,
  Text,
  Transformer,
} from "react-konva"
import { useAppDispatch, useAppSelector } from "../../app/hooks"
import {
  editorSlice,
  elementSelected,
  Element,
  elementPositionUpdate,
  elementSelector,
  elementDragEpicTrigger,
  PositionUpdatePayload,
  elementDragEndEpicTrigger,
  selectedIdsSelector,
  RectangleShape,
  SpecificShape,
  CircleShape,
} from "./editorSlice"
import { CircleConfig } from "konva/lib/shapes/Circle"
import { RectConfig } from "konva/lib/shapes/Rect"
import Konva from "konva"
import React from "react"
import { Shape } from "konva/lib/Shape"
import { max } from "fp-ts/lib/ReadonlyNonEmptyArray"

// { id: 1, type: 'transition', x: 50, y: 50, width: 100, height: 100, fill: 'green' },
// { id: 2, type: 'place', x: 200, y: 200, radius: 50, stroke: 'black' },

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
    // if (tool === "transition") {
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
  let testCircProps: Element<CircleShape> = {
    id: "test_circ",
    shape: {
      radius: 40,
      type: "circle",
    },
    x: 400,
    y: 100,
    selected: false,
    fill: "indigo",
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
          {/* <Rect width={50} height={50} fill="red" />
          <Circle x={200} y={200} stroke="black" radius={50} /> */}
          {/* <Rectangle {...testRectProps} />
          <Circ {...testCircProps} /> */}
          {<GroupWithText />}
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

function renderGroup() {
  return <Group></Group>
}

type AnyElement = Element<SpecificShape>

function elementToNodeConfig(
  element: Element<SpecificShape>,
): CircleConfig | RectConfig {
  let baseConfig = {
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
    case "rect":
      let rectConfig: RectConfig = {
        ...baseConfig,
        width: element.shape.width,
        height: element.shape.height,
      }
      return rectConfig
    case "circle":
      let circleConfig: CircleConfig = {
        ...baseConfig,
        radius: element.shape.radius,
      }
      return circleConfig
  }
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

const setMinWidthAndHeight = (
  node: Konva.Node,
  minWidth: number,
  minHeight: number,
) => {
  const width = node.width()
  const height = node.height()
  const scaleX = node.scaleX()
  const scaleY = node.scaleY()
  const newWidth = Math.max(scaleX * width, minWidth)
  const newHeight = Math.max(scaleY * height, minHeight)
  // we will reset it back
  node.scaleX(1)
  node.scaleY(1)
  node.width(newWidth)
  node.height(newHeight)
  node.width()
}

const synchronizeTwoNodes = (srcNode: Konva.Node, targetNode: Konva.Node) => {
  targetNode.setAttrs({
    width: srcNode.scaleX() * srcNode.width(),
    height: srcNode.scaleY() * srcNode.height(),
    x: srcNode.x(),
    y: srcNode.y(),
  })
}

function GroupWithText() {
  const [selected, setSelected] = useState(false)
  const trRef = useRef<Konva.Transformer | null>(null)
  const groupRef = useRef<Konva.Group | null>(null)
  const textRef = useRef<Konva.Text | null>(null)
  const rectRef = useRef<Konva.Rect | null>(null)

  let testRectProps: Element<RectangleShape> = {
    id: "test_rect",
    shape: {
      height: 200,
      width: 200,
      type: "rect",
    },
    x: 0,
    y: 0,
    selected: false,
    stroke: "black",
  }

  useEffect(() => {
    synchronizeTwoNodes(rectRef.current!, textRef.current!)
  }, [])

  React.useEffect(() => {
    if (selected) {
      if (trRef.current && groupRef.current) {
        let transformableNodes = [rectRef.current!]
        trRef.current.nodes(transformableNodes)
        trRef.current.getLayer()?.batchDraw()
      }
    }
  }, [selected])
  const MIN_WIDTH = 50
  const MIN_HEIGHT = 50

  let props = elementToNodeConfig(testRectProps)
  return (
    <React.Fragment>
      <Group onClick={() => setSelected(true)} ref={groupRef} draggable={true}>
        <Rect
          ref={rectRef}
          {...props}
          draggable={false}
          onTransform={() => {
            synchronizeTwoNodes(rectRef.current!, textRef.current!)
          }}
          onTransformEnd={() => {
            setMinWidthAndHeight(rectRef.current!, MIN_WIDTH, MIN_HEIGHT)
          }}
        />
        {/* задача - сделать текст редактируемым */}
        <Text
          ref={textRef}
          fontSize={24}
          ellipsis
          align="center"
          verticalAlign="middle"
          wrap="word"
          text="kek lol arbidol"
          draggable={false}
        />
      </Group>
      {selected && (
        <Transformer
          padding={5}
          ref={trRef}
          rotateEnabled={false}
          flipEnabled={false}
          boundBoxFunc={(oldBox, newBox) => {
            // limit resize
            if (
              Math.abs(newBox.width) < MIN_WIDTH ||
              Math.abs(newBox.height) < MIN_HEIGHT
            ) {
              return oldBox
            }
            return newBox
          }}
        />
      )}
    </React.Fragment>
  )
}

function Rectangle(element: Element<RectangleShape>) {
  const dispatch = useAppDispatch()
  const shapeRef = React.useRef<Konva.Rect | null>(null)
  const trRef = React.useRef<Konva.Transformer | null>(null)
  const [selected, setSelected] = useState(false)

  React.useEffect(() => {
    if (selected) {
      if (trRef.current && shapeRef.current) {
        trRef.current.nodes([shapeRef.current])
        trRef.current.getLayer()?.batchDraw()
      }
    }
  }, [selected])

  let rectConfig: RectConfig = {
    ...elementToNodeConfig(element),
    width: element.shape.width,
    height: element.shape.height,
  }

  const onSelect = () => {
    console.log("in listener of Rectangle")
    setSelected(true)
  }

  return (
    <React.Fragment>
      <Rect
        onClick={onSelect}
        // onTap={onSelect}
        ref={shapeRef}
        // {...shapeProps}
        {...rectConfig}
        onTransformEnd={e => {
          // transformer is changing scale of the node
          // and NOT its width or height
          // but in the store we have only width and height
          // to match the data better we will reset scale on transform end
          const node = shapeRef.current
          if (node == null) return
          const scaleX = node.scaleX()
          const scaleY = node.scaleY()
          const x = node.x()
          const y = node.y()
          // we will reset it back
          node.scaleX(1)
          node.scaleY(1)

          console.log(`scaleX`, scaleX, "scaleY", scaleY, "x", x, "y", y)
          // set minimal value
          // width: Math.max(5, node.width() * scaleX),
          // height: Math.max(5, node.height() * scaleY),
        }}
      />
      {selected && (
        <Transformer
          ref={trRef}
          flipEnabled={false}
          boundBoxFunc={(oldBox, newBox) => {
            // limit resize
            if (Math.abs(newBox.width) < 5 || Math.abs(newBox.height) < 5) {
              return oldBox
            }
            return newBox
          }}
        />
      )}
    </React.Fragment>
  )
}

function Circ(element: Element<CircleShape>) {
  const dispatch = useAppDispatch()
  const shapeRef = React.useRef<Konva.Circle | null>(null)
  const trRef = React.useRef<Konva.Transformer | null>(null)
  const [selected, setSelected] = useState(false)

  React.useEffect(() => {
    if (selected) {
      if (trRef.current && shapeRef.current) {
        trRef.current.nodes([shapeRef.current])
        trRef.current.getLayer()?.batchDraw()
      }
    }
  }, [selected])

  let circleConfig: CircleConfig = {
    ...elementToNodeConfig(element),
    // radius: element.shape.radius
    width: 400,
    height: 100,
  }

  const onSelect = () => {
    console.log("in listener of Rectangle")
    setSelected(true)
  }

  return (
    <React.Fragment>
      <Circle
        onClick={onSelect}
        // onTap={onSelect}
        ref={shapeRef}
        // {...shapeProps}
        {...circleConfig}
        onTransformEnd={e => {
          // transformer is changing scale of the node
          // and NOT its width or height
          // but in the store we have only width and height
          // to match the data better we will reset scale on transform end
          const node = shapeRef.current
          if (node == null) return
          const scaleX = node.scaleX()
          const scaleY = node.scaleY()
          const x = node.x()
          const y = node.y()
          // we will reset it back
          node.scaleX(1)
          node.scaleY(1)

          console.log(`scaleX`, scaleX, "scaleY", scaleY, "x", x, "y", y)
          // set minimal value
          // width: Math.max(5, node.width() * scaleX),
          // height: Math.max(5, node.height() * scaleY),
        }}
      />
      {selected && (
        <Transformer
          ref={trRef}
          flipEnabled={false}
          boundBoxFunc={(oldBox, newBox) => {
            // limit resize
            // if (Math.abs(newBox.width) < 5 || Math.abs(newBox.height) < 5) {
            //   return oldBox
            // }
            // return newBox
            console.log(oldBox, newBox)
            return { x: 100, y: 100, width: 500, height: 500, rotation: 0 }
          }}
        />
      )}
    </React.Fragment>
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
