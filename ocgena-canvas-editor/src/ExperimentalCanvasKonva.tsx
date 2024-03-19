import Konva from "konva";
import { KonvaEventObject } from "konva/lib/Node";
import { useEffect, useRef } from "react";
import { render } from "react-dom";
import {
  KonvaNodeComponent,
  Layer,
  Rect,
  Stage,
  StageProps,
} from "react-konva";

function createTextInputArea(text: Konva.Text, stage: Konva.Stage) {
  // create textarea over canvas with absolute position

  // first we need to find position for textarea
  // how to find it?

  // at first lets find position of text node relative to the stage:
  var textPosition = text.getAbsolutePosition();

  // then lets find position of stage container on the page:
  var stageBox = stage.container().getBoundingClientRect();

  // so position of textarea will be the sum of positions above:
  var areaPosition = {
    x: stageBox.left + textPosition.x,
    y: stageBox.top + textPosition.y,
  };

  // create textarea and style it
  var textarea = document.createElement("textarea");
  document.body.appendChild(textarea);

  textarea.value = text.text();
  textarea.style.position = "absolute";
  textarea.style.top = areaPosition.y + "px";
  textarea.style.left = areaPosition.x + "px";
  textarea.style.width = text.width() + "px";

  textarea.focus();

  textarea.addEventListener("keydown", function (e) {
    // hide on enter
    if (e.code === "Enter") {
      text.text(textarea.value);
      document.body.removeChild(textarea);
    }
  });
}

interface TransitionNode {
  rect: Konva.Rect;
  text: Konva.Text;
  group: Konva.Group;
}

function createTransitionNode(): TransitionNode {
  let rect = new Konva.Rect({
    x: 0,
    y: 0,
    width: 100,
    height: 100,
    fill: "black",
    stroke: "blue",
    strokeWidth: 4,
  });

  var text = new Konva.Text({
    x: 0,
    y: 0,
    fontFamily: "Calibri",
    fontSize: 24,
    text: "Hallo",
    fill: "white",
    width: 100,
  });
  var group = new Konva.Group();
  group.add(rect, text);
  group.setDraggable(true);
  return {
    rect,
    text,
    group,
  };
}

function arrow() {}

function ExperimentalCanvasKonva() {
  const stageRef = useRef<Konva.Stage>(null);
  const containerRef = useRef(null);
  const stageSetup = useRef(false);
  const clicked = useRef(0);

  useEffect(() => {
    // let stage = stageRef.current;
    if (containerRef.current && !stageSetup.current) {
      console.log("at stageRef update");
      stageSetup.current = true;

      let stage = new Konva.Stage({
        container: "container",
        width: 500,
        height: 500,
      });
      let rectLayer = new Konva.Layer();

      var group1 = createTransitionNode();
      var group2 = createTransitionNode();

      let arrow = new Konva.Arrow({
        x: stage.width() / 4,
        y: stage.height() / 4,
        points: [0, 0, 30, 50,  200, 200],
        bezier: true,
        tension: 0.5,
        pointerLength: 20,
        pointerWidth: 20,
        fill: "black",
        stroke: "black",
        strokeWidth: 4,
      });

      

      // rect.on("click", () => {`
      //   text.setText(`clicked ${clicked.current} times`);
      //   console.log("click");
      //   clicked.current = clicked.current + 1;
      // });
      // rect.setDraggable(true)

      // text.on("dblclick dbltap", () => {
      //   createTextInputArea(text, stage);
      // });

      stage.on("drag", (event: KonvaEventObject<DragEvent>) => {
        console.log("drag", event);
      });
      stage.on("dragstart", (event) => {
        console.log("dragstart", event);
      });

      // rectLayer.add(rect);
      // rectLayer.add(text);
      rectLayer.add(group1.group, group2.group, arrow);
      stage.add(rectLayer);
    }
  }, []);
  return (
    <>
      <div id={"container"} ref={containerRef}></div>
      {/* <Stage ref={stageRef} width={500} height={500}>
        <Layer>
          <Rect
            x={50}
            y={50}
            width={100}
            height={100}
            fill="black"
            stroke={"blue"}
            strokeWidth={4}
          ></Rect>
        </Layer>
      </Stage> */}
    </>
  );
}

export default ExperimentalCanvasKonva;
