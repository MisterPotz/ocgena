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
  elementContextOpened,
  contextMenuAddTransition,
  contextMenuSelector,
  elementContextMenuClosed,
  contextMenuAddPlace,
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
  const contextMenu = useAppSelector(contextMenuSelector)
  const contextMenuRef = useRef<HTMLDivElement | null>(null) // Add this line to create a ref for the context menu
  const stageRef = useRef<Konva.Stage | null>(null)
  const [selector, setSelector] = useState<SelectorOrNull>(null)
  const elementsLayerRef = useRef<Konva.Layer | null>(null)
  const selectorDataRef = useRef<SelectorOrNull>(null)
  const selectorShapeRef = useRef<Konva.Rect | null>(null)
  const selectionLayer = useRef<Konva.Layer | null>(null)
  // State for storing elements
  const [tool, setTool] = useState<Tool | null>(null) // Current selected tool

  useEffect(() => {
    // Handler to call on document click
    const handleDocumentClick = (event: MouseEvent) => {
      if (
        contextMenuRef.current &&
        !contextMenuRef.current.contains(event.target as Node)
      ) {
        // Clicked outside the context menu, close it
        dispatch(elementContextMenuClosed())
      }
    }

    if (contextMenu) {
      document.addEventListener("click", handleDocumentClick)
      return () => document.removeEventListener("click", handleDocumentClick)
    }

    // Clean up the listener when the component unmounts or the menu closes
    return () => {}
  }, [contextMenu]) // Only re-run if contextMenu changes

  useEffect(() => {
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
    }
  }, [])

  const handleElementDragEnd = (e: KonvaEventObject<DragEvent>) => {
    dispatch(elementDragEndEpicTrigger(dragEventToPayload(e)))
  }

  return (
    <>
      <ToolPane onSelectTool={setTool} />
      <Stage
        onContextMenu={(event: Konva.KonvaEventObject<PointerEvent>) => {
          handleMouseKey(event.evt, {
            rightKey: () => {
              console.log("onContextMenu")
              // event.target.preventDefault()
              event.evt.preventDefault()
              const targetShapeId = event.target.id()
              if (targetShapeId) {
                console.log("event target", event.target.id())
                // setShowMenu(true)
                // setMenuPosition({ x: event.evt.pageX, y: event.evt.pageY })
                dispatch(elementSelected(targetShapeId))
                dispatch(
                  elementContextOpened({
                    targetElement: targetShapeId,
                    x: event.evt.pageX,
                    y: event.evt.pageY,
                  }),
                )
              }
            },
          })
        }}
        ref={stageRef}
        width={window.innerWidth}
        height={window.innerHeight}
        onMouseDown={(e: Konva.KonvaEventObject<MouseEvent>) => {
          // console.log(
          //   "stage mouse down target",
          //   e.target,
          //   "left click",
          //   e.evt.button,
          // )
          const stage = stageRef.current!
          handleMouseKey(e.evt, {
            leftKey: () => {
              console.log("onMouseDown")
              if (e.target !== stageRef.current) {
                const selectionId = e.target.id()
                if (selectionId) {
                  dispatch(elementSelected(e.target.id()))
                }
              } else {
                dispatch(elementSelectionCancelled())
                const startX = stage.getRelativePointerPosition()?.x
                const startY = stage.getRelativePointerPosition()?.y

                if (startX && startY) {
                  console.log("setting selector at", startX, startY)
                  const selectorData = { x: startX, y: startY }
                  selectorDataRef.current = selectorData
                  setSelector(selectorData)
                }
              }
            },
          })
        }}
        onMouseMove={(evt: KonvaEventObject<MouseEvent>) => {}}
      >
        <Layer>
          
        </Layer>
        <Layer
          ref={elementsLayerRef}
          onDragStart={e => {
            handleMouseKey(e.evt, {
              leftKey: () => {
                console.log("onDragStart")
                const targetShapeID = e.target.id()
                if (targetShapeID) {
                  dispatch(elementSelected(targetShapeID))
                }
              },
            })
          }}
          onDragEnd={e => {
            handleMouseKey(e.evt, {
              leftKey: () => {
                console.log("onDragEnd")
                dispatch(elementDragEndEpicTrigger(dragEventToPayload(e)))
              },
            })
          }}
          onClick={(e: Konva.KonvaEventObject<MouseEvent>) => {
            handleMouseKey(e.evt, {
              leftKey: () => {
                const clickedId = e.target?.id()
                console.log("onClick ", clickedId)
                if (clickedId) {
                  dispatch(elementSelected(clickedId))
                }
              },
            })
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
      {contextMenu && (
        <div
          ref={contextMenuRef}
          className={styles.customContextMenu} /* "customContextMenu" */
          style={{
            position: "absolute",
            top: `${contextMenu.y}px`,
            left: `${contextMenu.x}px`,
            backgroundColor: "white",
            // border: '1px solid black',
            // padding: '10px'
          }}
          onBlur={() => {
            console.log("blur event")
            dispatch(elementContextMenuClosed())
          }}
        >
          Custom Context Menu
          <ul>
            <li className={styles.sectionTitle}>Create new shape</li>
            <li
              onClick={() => {
                dispatch(contextMenuAddTransition())
              }}
              className={styles.menuItem}
            >
              <span className={styles.menuText}>Transition</span>
              <span className={styles.shortcut}>Cmd+V</span>
            </li>
            <li
              onClick={() => {
                dispatch(contextMenuAddPlace())
              }}
              className={styles.menuItem}
            >
              <span className={styles.menuText}>Place</span>
              <span className={styles.shortcut}>Cmd+V</span>
            </li>

            <li className={styles.menuItem}>
              <span className={styles.menuText}>Copy to clipboard as PNG</span>
              <span className={styles.shortcut}>Shift+Option+C</span>
              {/* <a href="#"></a> */}
            </li>
            {/* <li>
              <a href="#">
                <span className={styles.menuText}>
                  Copy to clipboard as SVG
                </span>
                <span className={styles.shortcut}>Cmd+C</span>
              </a>
            </li> */}
            <li className={styles.separator}></li>
            <li className={styles.menuItem}>
              <span className={styles.menuText}>Select all</span>
              <span className={styles.shortcut}>Cmd+A</span>
            </li>
          </ul>
          <button
            onClick={() => {
              dispatch(elementContextMenuClosed())
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
    handleMouseKey(ev, {
      leftKey: () => {
        console.log("onMouseMove")
        const mouseX = ev.clientX
        const mouseY = ev.clientY

        const stageBoundingClientRect = stage
          .container()
          .getBoundingClientRect()
        const stageClientRectX = stageBoundingClientRect.left //+ window.scrollX
        const stageClientRectY = stageBoundingClientRect.top //+ window.scrollY
        const stageClientRightX = stageBoundingClientRect.right
        const stageClientBottomY = stageBoundingClientRect.bottom

        const clientStartX = selector.x + stageClientRectX
        const clientStartY = selector.y + stageClientRectY
        // console.log("mouseX, mouseY", mouseX, mouseY)
        // console.log(
        //   "canvas left top right bottom",
        //   stageClientRectX,
        //   stageClientRectY,
        //   stageClientRightX,
        //   stageClientBottomY,
        // )
        const fixedMouseX = Math.min(
          Math.max(mouseX, stageClientRectX),
          stageClientRightX,
        )
        const fixedMouseY = Math.min(
          Math.max(mouseY, stageClientRectY),
          stageClientBottomY,
        )
        selectorShape.setAttrs({
          x: Math.min(selector.x, fixedMouseX - stageClientRectX),
          y: Math.min(selector.y, fixedMouseY - stageClientRectY),
          width: Math.abs(fixedMouseX - clientStartX),
          height: Math.abs(fixedMouseY - clientStartY),
        })

        selectionLayerRef.current!.batchDraw()
      },
    })
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
    handleMouseKey(ev, {
      leftKey: () => {
        console.log("onMouseUp")
        setSelector(null)
        selectionLayerRef.current!.draw()
      },
    })
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

function handleMouseKey(
  evt: MouseEvent,
  clicks: {
    leftKey?: () => void
    rightKey?: () => void
  },
) {
  switch (evt.button) {
    case 0:
      if (clicks.leftKey) {
        console.log("handling left key", evt)
        clicks.leftKey()
      }
      break
    case 1:
      break
    case 2:
      if (clicks.rightKey) {
        console.log("handling right key ", evt)
        clicks.rightKey()
      }
      break
    default:
      break
  }
}
