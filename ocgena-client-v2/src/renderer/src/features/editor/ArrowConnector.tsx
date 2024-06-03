import Konva from "konva"
import { useRef, useEffect, useState } from "react"
import { Stage, Layer, Rect, Arrow } from "react-konva"

// can help in building infinite canvas
export const PatternImage = () => {
  const [patternImage, setPatternImage] = useState(new window.Image())
  const stageRef = useRef<Konva.Stage | null>(null)
  const [stagePosition, setStagePosition] = useState({ x: 0, y: 0 })
  const [isDragging, setIsDragging] = useState(false)
  const [lastPosition, setLastPosition] = useState({ x: 0, y: 0 })

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

  const width = window.innerWidth
  const height = window.innerHeight

  const handleMouseDown = (event: Konva.KonvaEventObject<MouseEvent>) => {
    const stage = stageRef.current!
    if (stage) {
      setIsDragging(true)
      setLastPosition(stage.getPointerPosition()!)
    }
  }

  const handleMouseMove = (event: Konva.KonvaEventObject<MouseEvent>) => {
    const stage = stageRef.current
    if (stage && isDragging) {
      const newPosition = stage.getPointerPosition()!
      setStagePosition({
        x: stagePosition.x + (newPosition.x - lastPosition.x),
        y: stagePosition.y + (newPosition.y - lastPosition.y),
      })
      setLastPosition(newPosition)
    }
  }

  const handleMouseUp = () => {
    setIsDragging(false)
  }

  return (
    <Stage
      width={window.innerWidth}
      height={window.innerHeight}
      onMouseDown={handleMouseDown}
      onMouseMove={handleMouseMove}
      onMouseUp={handleMouseUp}
      onMouseOut={handleMouseUp}
      ref={stageRef}
      draggable
    >
      <Layer>
        <Rect
          x={-window.innerWidth / 2}
          y={-window.innerHeight / 2}
          width={window.innerWidth * 2}
          height={window.innerHeight * 2}
          fillPatternImage={patternImage}
          fillPatternOffset={stagePosition}
          fillPatternRepeat="repeat"
        />
        {/* Add other shapes or layers here */}
      </Layer>
    </Stage>
  )
}

function ArrowConnector() {
  const rect1Ref = useRef<Konva.Rect>(null)
  const rect2Ref = useRef<Konva.Rect>(null)
  const arrowRef = useRef<Konva.Arrow>(null)

  // Function to update the arrow points
  const updateArrow = () => {
    new Konva.Arrow()
    const rect1 = rect1Ref.current
    const rect2 = rect2Ref.current
    const arrow = arrowRef.current

    if (rect1 && rect2 && arrow) {
      const points = [
        rect1.x() + rect1.width() / 2,
        rect1.y() + rect1.height() / 2,
        rect2.x() + rect2.width() / 2,
        rect2.y() + rect2.height() / 2,
      ]
      arrow.points(points)
    }
  }

  //   useEffect(() => {
  //     //
  //     arrowRef
  //       .current!//
  //       .updateArrow()
  //   }, [])

  return (
    <Stage width={window.innerWidth} height={window.innerHeight}>
      <Layer>
        <Rect
          x={50}
          y={70}
          width={100}
          height={50}
          fill="red"
          draggable
          onDragMove={updateArrow}
          ref={rect1Ref}
        />
        <Rect
          x={200}
          y={150}
          width={100}
          height={50}
          fill="blue"
          draggable
          onDragMove={updateArrow}
          ref={rect2Ref}
        />
        <Arrow stroke="black" fill="black" ref={arrowRef} points={[]} />
      </Layer>
    </Stage>
  )
}

export function ArrowConnectorFollowMouse() {
  const rectRef = useRef<Konva.Rect>(null)
  const [arrowPoints, setArrowPoints] = useState([50, 70, 50, 70]) // Initial points, updated dynamically
  const [isDrawing, setIsDrawing] = useState(false) // State to track if we are in drawing mode

  // Function to start drawing
  const startDrawing = (e: Konva.KonvaEventObject<MouseEvent>) => {
    setIsDrawing(true)
    const stage = e.target.getStage()
    const mousePos = stage!.getPointerPosition()
    setArrowPoints([
      rectRef.current!.x() + rectRef.current!.width() / 2,
      rectRef.current!.y() + rectRef.current!.height() / 2,
      mousePos!.x,
      mousePos!.y,
    ])
  }

  // Function to update the arrow to follow the mouse
  const handleMouseMove = (e: Konva.KonvaEventObject<MouseEvent>) => {
    if (isDrawing) {
      const stage = e.target.getStage()
      const mousePos = stage!.getPointerPosition()
      setArrowPoints([arrowPoints[0], arrowPoints[1], mousePos!.x, mousePos!.y])
    }
  }

  // Function to stop drawing
  const stopDrawing = () => {
    setIsDrawing(false)
  }

  return (
    <Stage
      width={window.innerWidth}
      height={window.innerHeight}
      onMouseMove={handleMouseMove}
      onMouseUp={stopDrawing}
    >
      <Layer>
        <Rect
          x={50}
          y={70}
          width={100}
          height={50}
          fill="red"
          draggable
          ref={rectRef}
          onMouseDown={startDrawing}
        />
        <Arrow
          points={arrowPoints}
          stroke="black"
          fill="black"
          strokeWidth={4}
          pointerLength={10}
          pointerWidth={10}
          lineCap="round"
          lineJoin="round"
        />
      </Layer>
    </Stage>
  )
}

export default ArrowConnector
