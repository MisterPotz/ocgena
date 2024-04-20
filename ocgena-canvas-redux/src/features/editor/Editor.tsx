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
  mouseMoveEpicTrigger,
  elementPositionUpdate,
} from "./editorSlice"
import { TextShape } from "./TextShape"
import Konva from "konva"
import styles from "./Editor.module.css"
import { ELEMENT_CHILD_PREFIX, ELEMENT_PREFIX, TRANSFORMER_PREFIX } from "./Keywords"
import { tryGetElementId } from "./Utils"

function dragEventToPayload(
  e: KonvaEventObject<DragEvent>,
): PositionUpdatePayload {
  const absolutePosition = e.target.absolutePosition()
  console.log("drag event ", e.target.id(), absolutePosition)
  return {
    id: e.target.id(),
    x: absolutePosition.x,
    y: absolutePosition.y,
  }
}

interface SelectorData {
  x: number
  y: number
}

type SelectorOrNull = SelectorData | null

type DelegateType = "window" | "document" | "stage" | "layer"

type InclusiveEventMap = GlobalEventHandlersEventMap & {
  [index: string]: any
}

interface CallbackPack<
  Key extends keyof InclusiveEventMap = keyof InclusiveEventMap,
> {
  key?: Key
  window?: (this: Window, ev: InclusiveEventMap[Key]) => any
  document?: (this: Document, ev: InclusiveEventMap[Key]) => any
  contextMenu?: (this: HTMLDivElement, ev: InclusiveEventMap[Key]) => any
  stage?: (
    this: Konva.Stage,
    ev: Konva.KonvaEventObject<InclusiveEventMap[Key] | any>,
  ) => any
  elementsLayer?: (
    this: Konva.Layer,
    ev: Konva.KonvaEventObject<InclusiveEventMap[Key] | any>,
  ) => any
  selectionLayer?: (
    this: Konva.Layer,
    ev: Konva.KonvaEventObject<InclusiveEventMap[Key] | any>,
  ) => any
}

function wrapCallback(key: any, callback: (this: any, ev: any) => void) {
  return function wrappedCallback(this: any, event: any) {
    // Assuming you want to log or do something before and after the callback
    if (key != "mousemove") {
      console.log(`${key}`)
    }

    callback.call(this, event)
  }
}

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
  const [patternImage, setPatternImage] = useState(new window.Image())
  const [stagePosition, setStagePosition] = useState({ x: 0, y: 0 })
  const isDraggingRef = useRef(false)
  const [lastPosition, setLastPosition] = useState({ x: 0, y: 0 })
  const spacePressedRef = useRef(false)

  const [callbackRegistrar, _] = useState(() => {
    const callbackRegistrar = new CallbackRegistrar()
    callbackRegistrar
      .addCallbackPack("mousemove", {
        window: (ev: MouseEvent) => {
          const selectorShape = selectorShapeRef.current
          const stage = stageRef.current!
          const selector = selectorDataRef.current

          const stageBoundingClientRect = stage
            .container()
            .getBoundingClientRect()
          const stageClientRectX = stageBoundingClientRect.left //+ window.scrollX
          const stageClientRectY = stageBoundingClientRect.top //+ window.scrollY
          const stageClientRightX = stageBoundingClientRect.right
          const stageClientBottomY = stageBoundingClientRect.bottom

          const pointerPosition = stage.getPointerPosition()

          dispatch(
            mouseMoveEpicTrigger({
              clientX: ev.clientX,
              clientY: ev.clientY,
              stageX: pointerPosition?.x!, // ev.clientX - stageClientRectX,
              stageY: pointerPosition?.y!, // ev.clientY - stageClientRectY,
            }),
          )

          if (selector && selectorShape) {
            // Translate window coordinates to stage coordinates
            handleMouseKey(ev, {
              leftKey: () => {
                const mouseX = ev.clientX
                const mouseY = ev.clientY

                const clientStartX = selector.x + stageClientRectX
                const clientStartY = selector.y + stageClientRectY

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

                selectionLayer.current!.batchDraw()
              },
            })
          }
        },
        stage(ev: Konva.KonvaEventObject<MouseEvent>) {
          const stage = stageRef.current
          if (stage && isDraggingRef.current) {
            const newPosition = stage.getPointerPosition()!
            setStagePosition({
              x: stagePosition.x + (newPosition.x - lastPosition.x),
              y: stagePosition.y + (newPosition.y - lastPosition.y),
            })
            setLastPosition(newPosition)
          }
        },
      })
      .addCallbackPack("mouseup", {
        window: (ev: MouseEvent) => {
          const selectorShape = selectorShapeRef.current
          const selector = selectorDataRef.current
          if (!selector || !selectorShape) return
          handleMouseKey(ev, {
            leftKey: () => {
              setSelector(null)
              isDraggingRef.current = false
              selectionLayer.current!.draw()
            },
          })
        },
      })
      .addCallbackPack("mousedown", {
        stage(ev: Konva.KonvaEventObject<MouseEvent>) {
          const stage = stageRef.current!
          const downItem = ev.target
          const parent = downItem.getParent()

          console.log("down item", downItem)
          handleMouseKey(ev.evt, {
            leftKey: () => {
              if (
                downItem !== stage &&
                downItem.id().startsWith(ELEMENT_PREFIX)
              ) {
                let clickedCompound = downItem as Konva.Group
                dispatch(elementSelected(clickedCompound.id()))
              } else if (
                downItem !== stage &&
                downItem.id().startsWith(ELEMENT_CHILD_PREFIX)
              ) {
                let clickedCompound = parent as Konva.Group
                dispatch(elementSelected(clickedCompound.id()))
              } else if (
                downItem !== stage &&
                downItem.id().startsWith(TRANSFORMER_PREFIX)
              ) {
                console.log("is transformer")
                return
              } else if (
                ev.target === stage &&
                spacePressedRef.current
              ) {
                isDraggingRef.current = true
                setLastPosition(stage.getPointerPosition()!)
              } else if (ev.target === stage) {
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
        },
      })
      .addCallbackPack("keydown", {
        window: (ev: KeyboardEvent) => {
          if (ev.key === " ") {
            ev.preventDefault()
            spacePressedRef.current = true
          }
        },
      })
      .addCallbackPack("keyup", {
        window: (ev: KeyboardEvent) => {
          if (ev.key === " ") {
            ev.preventDefault()
            spacePressedRef.current = false
          }
        },
      })
      .addCallbackPack("click", {
        document: (event: MouseEvent) => {
          if (
            contextMenuRef.current &&
            !contextMenuRef.current.contains(event.target as Node)
          ) {
            // Clicked outside the context menu, close it
            dispatch(elementContextMenuClosed())
          }
        },
      })
      .addCallbackPack("contextmenu", {
        stage: (event: Konva.KonvaEventObject<PointerEvent>) => {
          handleMouseKey(event.evt, {
            rightKey: () => {
              console.log("onContextMenu")
              // event.target.preventDefault()
              event.evt.preventDefault()
              const target = event.target
              if (target && stageRef.current! !== target) {
                const targetShapeId = tryGetElementId(event.target!)
                if (targetShapeId) {
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
              }
            },
          })
        },
      })
      .addCallbackPack("dragend", {
        elementsLayer(ev: Konva.KonvaEventObject<DragEvent>) {
          handleMouseKey(ev.evt, {
            leftKey: () => {
              dispatch(elementDragEndEpicTrigger(dragEventToPayload(ev)))
            },
          })
        },
      })
      .addCallbackPack("blur", {
        contextMenu(ev: FocusEvent) {
          dispatch(elementContextMenuClosed())
        },
      })
    return callbackRegistrar
  })

  useEffect(() => {
    const contextMenu = contextMenuRef.current
    if (contextMenu) {
      callbackRegistrar.applyContextMenuCallbacks(contextMenu)
    }

    return () => {
      if (contextMenu) {
        callbackRegistrar.removeContextMenuCallbacks(contextMenu)
      }
    }
  }, [contextMenuRef.current])

  useEffect(() => {
    callbackRegistrar.applyCallbacks(
      document,
      window,
      stageRef.current!,
      selectionLayer.current!,
      elementsLayerRef.current!,
    )

    return () => {
      callbackRegistrar.removeCallbacks(
        document,
        window,
        stageRef.current,
        selectionLayer.current,
        elementsLayerRef.current,
      )
    }
  }, [])

  useEffect(() => {
    const dotPatternCanvas = document.createElement("canvas")
    const context = dotPatternCanvas.getContext("2d")!

    const spacing = 20 // Spacing between dots
    const dotRadius = 2 // Radius of the dots

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
      <ToolPane onSelectTool={setTool} />
      <Stage
        style={{
          border: "solid",
          borderWidth: "1px",
          // padding: "0px 12px"
        }}
        ref={stageRef}
        width={1200}
        height={800}
      >
        <Layer>
          <Rect
            draggable={false}
            listening={false}
            x={0}
            y={0}
            width={1200}
            height={800}
            fillPatternImage={patternImage}
            fillPatternOffset={{ x: 0, y: 0 }}
            fillPatternRepeat="repeat"
          />
        </Layer>
        <Layer ref={elementsLayerRef}>
          {elements.map((el, index) => {
            return (
              <TextShape
                key={el.id}
                element={el}
                updatePosition={payload => {
                  dispatch(elementPositionUpdate(payload))
                }}
              />
            )
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

class CallbackRegistrar {
  private callbackPacks: CallbackPack[] = []

  addCallbackPack<Key extends keyof InclusiveEventMap>(
    type: Key,
    callbackPack: CallbackPack<Key>,
  ): CallbackRegistrar {
    this.callbackPacks.push({
      window: callbackPack.window
        ? wrapCallback(type, callbackPack.window)
        : undefined,
      document: callbackPack.document
        ? wrapCallback(type, callbackPack.document)
        : undefined,
      stage: callbackPack.stage
        ? wrapCallback(type, callbackPack.stage)
        : undefined,
      selectionLayer: callbackPack.selectionLayer
        ? wrapCallback(type, callbackPack.selectionLayer)
        : undefined,
      elementsLayer: callbackPack.elementsLayer
        ? wrapCallback(type, callbackPack.elementsLayer)
        : undefined,
      contextMenu: callbackPack.contextMenu
        ? wrapCallback(type, callbackPack.contextMenu)
        : undefined,
      key: type,
    })
    return this
  }

  applyCallbacks(
    document: Document,
    window: Window,
    stage: Konva.Stage,
    selectionLayer: Konva.Layer,
    elementsLayer: Konva.Layer,
  ) {
    this.callbackPacks.forEach(pack => {
      const convertedKey = pack.key as keyof GlobalEventHandlersEventMap
      if (pack.window) {
        window.addEventListener(convertedKey, pack.window)
      }
      if (pack.document) {
        document.addEventListener(convertedKey, pack.document)
      }
      if (pack.stage) {
        stage.on(convertedKey, pack.stage)
      }
      if (pack.selectionLayer) {
        selectionLayer.on(convertedKey, pack.selectionLayer)
      }
      if (pack.elementsLayer) {
        elementsLayer.on(convertedKey, pack.elementsLayer)
      }
    })
  }

  private convertKey(
    key: keyof InclusiveEventMap,
  ): keyof GlobalEventHandlersEventMap {
    return key as keyof GlobalEventHandlersEventMap
  }

  applyContextMenuCallbacks(contextMenu: HTMLDivElement) {
    this.callbackPacks.forEach(pack => {
      if (pack.contextMenu) {
        contextMenu.addEventListener(
          this.convertKey(pack.key!),
          pack.contextMenu,
        )
      }
    })
  }

  removeContextMenuCallbacks(contextMenu: HTMLDivElement) {
    this.callbackPacks.forEach(pack => {
      if (pack.contextMenu) {
        contextMenu.removeEventListener(
          this.convertKey(pack.key!),
          pack.contextMenu,
        )
      }
    })
  }

  removeCallbacks(
    document: Document,
    window: Window,
    stage?: Konva.Stage | null,
    selectionLayer?: Konva.Layer | null,
    elementsLayer?: Konva.Layer | null,
  ) {
    this.callbackPacks.forEach(pack => {
      const convertedKey = pack.key as keyof GlobalEventHandlersEventMap
      if (pack.window) {
        window.removeEventListener(convertedKey, pack.window)
      }
      if (pack.document) {
        document.removeEventListener(convertedKey, pack.document)
      }
      if (pack.stage && stage) {
        stage.off(convertedKey, pack.stage)
      }
      if (pack.selectionLayer && selectionLayer) {
        selectionLayer.off(convertedKey, pack.selectionLayer)
      }
      if (pack.elementsLayer && elementsLayer) {
        elementsLayer.off(convertedKey, pack.elementsLayer)
      }
    })
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
        clicks.leftKey()
      }
      break
    case 1:
      break
    case 2:
      if (clicks.rightKey) {
        clicks.rightKey()
      }
      break
    default:
      break
  }
}
