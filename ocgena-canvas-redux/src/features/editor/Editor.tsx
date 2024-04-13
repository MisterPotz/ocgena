import { KonvaEventObject } from "konva/lib/Node"
import { useEffect, useRef, useState } from "react"
import { Layer, Rect, Stage } from "react-konva"
import { useAppDispatch, useAppSelector } from "../../app/hooks"
import {
  elementSelected,
  elementSelector,
  PositionUpdatePayload,
  elementDragEndEpicTrigger,
  elementSelectionCancelled,
} from "./editorSlice"
import { TextShape } from "./TextShape"
import Konva from "konva"
import styles from "./Editor.module.css"

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
  const elements = useAppSelector(elementSelector)
  const stageRef = useRef<Konva.Stage | null>(null)
  const [selector, setSelector] = useState<SelectorOrNull>(null)
  const elementsLayerRef = useRef<Konva.Layer | null>(null)
  const selectorDataRef = useRef<SelectorOrNull>(null)
  const selectorShapeRef = useRef<Konva.Rect | null>(null)
  const selectionLayer = useRef<Konva.Layer | null>(null)
  // State for storing elements
  const [tool, setTool] = useState<Tool | null>(null) // Current selected tool
  const [menuPosition, setMenuPosition] = useState({ x: 0, y: 0 })
  const [showMenu, setShowMenu] = useState(false)

  const handleRightClick = (event: Konva.KonvaEventObject<PointerEvent>) => {
    // event.preventDefault()
    event.target.preventDefault()
    event.evt.preventDefault()
    const targetShapeId = event.target.id()
    if (targetShapeId) {
      console.log("event target", event.target.id())
      setShowMenu(true)
      setMenuPosition({ x: event.evt.pageX, y: event.evt.pageY })
      dispatch(elementSelected(targetShapeId))
    }
  }

  useEffect(() => {
    const documentRightClickCallback = (ev: KeyboardEvent) => {
      console.log("key pressed", ev.key)
    }
    document.body.addEventListener("keypress", documentRightClickCallback)
    
    // document.addEventListener('click', function(event) {
    //   const contextMenu = document.querySelector('.custom-context-menu');
    //   contextMenu.style.display = 'none';
    // });

    const windowMouseMoveCallback = windowMouseMoveCallbackCreator(
      selectorShapeRef,
      stageRef,
      selectorDataRef,
      selectionLayer,
    )
    const windowMouseUpCallback = windowMouseUpCallbackCreator(
      selectorShapeRef,
      setSelector,
      stageRef,
      selectorDataRef,
      selectionLayer,
    )
    window.addEventListener("mousemove", windowMouseMoveCallback)
    window.addEventListener("mouseup", windowMouseUpCallback)
    return () => {
      window.removeEventListener("mousemove", windowMouseMoveCallback)
      window.removeEventListener("mouseup", windowMouseUpCallback)
      document.body.removeEventListener("keypress", documentRightClickCallback)
    }
  }, [])

  const handleElementDragEnd = (e: KonvaEventObject<DragEvent>) => {
    dispatch(elementDragEndEpicTrigger(dragEventToPayload(e)))
  }

  return (
    <>
      <ToolPane onSelectTool={setTool} />
      <Stage
        onContextMenu={handleRightClick}
        ref={stageRef}
        width={window.innerWidth}
        height={window.innerHeight}
        onMouseDown={(e: Konva.KonvaEventObject<MouseEvent>) => {
          console.log(
            "stage mouse down target",
            e.target,
            "left click",
            e.evt.button,
          )
          const stage = stageRef.current!
          switch (e.evt.button) {
            case 0:
              setShowMenu(false)

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
              break
            case 2: // right click
              break;
            default:
              break;
          }
        }}
      >
        <Layer
          ref={elementsLayerRef}
          onDragStart={e => {
            const targetShapeID = e.target.id()
            if (targetShapeID) {
              dispatch(elementSelected(targetShapeID))
            }
          }}
          onDragEnd={e => {
            dispatch(elementDragEndEpicTrigger(dragEventToPayload(e)))
          }}
          onClick={(e: Konva.KonvaEventObject<MouseEvent>) => {
            const clickedId = e.target?.id()
            console.log("clicked ", clickedId)
            setShowMenu(false)
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
      {showMenu && (
        <div className=".customContextMenu">
          Custom Context Menu
          <button
            onClick={() => {
              setShowMenu(false)
            }}
          >
            Close Menu
          </button>
        </div>
      )}
    </>
  )
}

const windowMouseMoveCallbackCreator =
  (
    selectorShapeRef: React.MutableRefObject<Konva.Node | null>,
    stageRef: React.MutableRefObject<Konva.Stage | null>,
    selectorDataRef: React.MutableRefObject<SelectorOrNull | null>,
    selectionLayerRef: React.MutableRefObject<Konva.Layer | null>,
  ) =>
  (ev: MouseEvent) => {
    const selectorShape = selectorShapeRef.current
    const stage = stageRef.current!
    const selector = selectorDataRef.current
    if (!selector || !selectorShape) return
    // Translate window coordinates to stage coordinates
    const pointerPosition = stage.getRelativePointerPosition()
    if (!pointerPosition) return
    const mouseX = ev.x
    const mouseY = ev.y

    const stageBoundingClientRect = stage.container().getBoundingClientRect()
    const stageClientRectX = stageBoundingClientRect.x + window.scrollX
    const stageCientRectY = stageBoundingClientRect.y + window.scrollY

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
    selectionLayerRef.current!.batchDraw()
  }
const windowMouseUpCallbackCreator =
  (
    selectorShapeRef: React.MutableRefObject<Konva.Node | null>,
    setSelector: (selectorData: SelectorOrNull) => void,
    stageRef: React.MutableRefObject<Konva.Stage | null>,
    selectorDataRef: React.MutableRefObject<SelectorOrNull | null>,
    selectionLayerRef: React.MutableRefObject<Konva.Layer | null>,
  ) =>
  (ev: MouseEvent) => {
    const selectorShape = selectorShapeRef.current
    const selector = selectorDataRef.current
    if (!selector || !selectorShape) return
    console.log("mouse up event", ev)
    setSelector(null)
    const selectionBox = selectorShape.getClientRect()
    selectionLayerRef.current!.draw()
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
