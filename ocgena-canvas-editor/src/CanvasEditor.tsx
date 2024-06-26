import { useEffect, useRef, useState } from "react";
import "./CanvasEditor.css";

interface ICoord {
  x: number;
  y: number;
}

interface IArea {
  topLeft: ICoord;
  bottomRight: ICoord;
}

class Coord implements ICoord {
  x: number = 0;
  y: number = 0;

  constructor(df?: { x?: number; y?: number }) {
    this.x = df?.x ?? 0;
    this.y = df?.y ?? 0;
  }

  set(offset: ICoord) {
    this.x = offset.x;
    this.y = offset.y;
  }

  plusCopy(offset: { x?: number; y?: number }) {
    return new Coord({
      x: this.x + (offset.x ?? 0),
      y: this.y + (offset.y ?? 0),
    });
  }

  minusCopy(coord: ICoord): Coord {
    return new Coord({ x: this.x - coord.x, y: this.y - coord.y });
  }
}

interface IRect {
  height: number;
  width: number;
}

interface ICircle {
  radius: number;
}

class Element {
  shape: Shape;
  localCoord: ICoord;
  zCoord: number;

  constructor(args: { shape: Shape; localCoord?: ICoord; zCoord?: number }) {
    this.shape = args.shape;
    this.localCoord = args.localCoord ?? new Coord();
    this.zCoord = args.zCoord ?? 0;
  }
}

abstract class Shape {
  abstract isMineCoord(coordRelativeTopLeft: ICoord): Boolean;
  abstract draw(canvas: CanvasProxy): any;
}

interface CanvasProxyDrawBlock {
  (canvas: CanvasRenderingContext2D): any;
}

class CanvasProxy {
  canvas: CanvasRenderingContext2D;
  // currentOffset: Coord = new Coord();

  constructor(canvas: CanvasRenderingContext2D) {
    this.canvas = canvas;
  }

  drawWithOffset(canvasProxyDrawBlock: CanvasProxyDrawBlock) {
    canvasProxyDrawBlock(this.canvas);
  }
}

class Rectangle extends Shape {
  rect: IRect;

  constructor(rect: IRect = { height: 100, width: 100 }) {
    super();
    this.rect = rect;
  }

  draw(canvas: CanvasProxy) {
    canvas.drawWithOffset((canvas) => {
      console.log("canvas fillRect", this.rect.width, this.rect.height);
      canvas.fillRect(0, 0, this.rect.width, this.rect.height);
    });
  }
  isMineCoord(localCoord: Coord): Boolean {
    return coordFallsIn(
      localCoord,
      { x: 0, y: 0 },
      { x: this.rect.width, y: this.rect.height }
    );
  }
}

class Circle extends Shape {
  circle: ICircle;

  constructor(circle: ICircle) {
    super();
    this.circle = circle;
  }

  draw(canvas: CanvasProxy) {
    canvas.drawWithOffset((canvas) => {
      canvas.beginPath();
      canvas.ellipse(
        this.circle.radius,
        this.circle.radius,
        this.circle.radius,
        this.circle.radius,
        0,
        0,
        Math.PI * 2
      );
      canvas.stroke();
      canvas.closePath();
    });
  }

  isMineCoord(coordRelativeTopLeft: ICoord): Boolean {
    let center = { x: this.circle.radius, y: this.circle.radius };
    return distance(center, coordRelativeTopLeft) <= this.circle.radius;
  }
}

function distance(coord2: ICoord, coord1: ICoord): number {
  return Math.sqrt((coord2.x - coord1.x) ** 2 + (coord2.y - coord1.y) ** 2);
}

function coordFallsIn(
  coord: ICoord,
  areaTopLeft: ICoord,
  areaBottomRight: ICoord
): Boolean {
  return (
    areaTopLeft.x <= coord.x &&
    areaTopLeft.y <= coord.y &&
    coord.x <= areaBottomRight.x &&
    coord.y <= areaBottomRight.y
  );
}

class ArrangementPainter {
  canvasProxy: CanvasProxy;

  constructor(canvasProxy: CanvasProxy) {
    this.canvasProxy = canvasProxy;
  }

  paint(localArrangementStore: Arrangement) {
    // this.canvasProxy.canvas.scale(1, 1);
    localArrangementStore.childsWithMetadata.forEach((element) => {
      // this.canvasProxy.currentOffset.set(element.localCoord);
      this.canvasProxy.canvas.save();
      console.log("element local coord", element.localCoord);
      this.canvasProxy.canvas.translate(
        element.localCoord.x,
        element.localCoord.y
      );
      element.shape.draw(this.canvasProxy);
      this.canvasProxy.canvas.restore();
    });
  }
}

class CanvasWrapper {
  canvas: HTMLCanvasElement;

  constructor(canvas: HTMLCanvasElement) {
    this.canvas = canvas;
  }

  resolveCanvasCoord(): Coord {
    return new Coord({
      x: this.canvas.offsetLeft + this.canvas.clientLeft,
      y: this.canvas.offsetTop + this.canvas.clientTop,
    });
  }
}

// implement zoom
// implement zoom with mouse tracking
// implement panning

class ViewOptions {
  panOffset: Coord;
  zoom: number;

  constructor() {
    this.panOffset = new Coord();
    this.zoom = 1;
  }
}

class Arrangement {
  childsWithMetadata: Element[] = [];

  getClickedChild(localCoord: Coord): Element | null {
    let foundChild = this.childsWithMetadata.reduceRight(
      (prevValue: Element | null, current: Element) => {
        if (prevValue) return prevValue;
        let offset = localCoord.minusCopy(current.localCoord);
        return current.shape.isMineCoord(offset) ? current : null;
      },
      null
    );
    return foundChild ?? null;
  }

  addChild(childWithMetadata: Element) {
    let insertionIndex = this.childsWithMetadata.findIndex((value) => {
      value.zCoord - childWithMetadata.zCoord;
    });
    this.childsWithMetadata.splice(insertionIndex ?? 0, 0, childWithMetadata);
  }

  sortChildrenByZ() {
    this.childsWithMetadata.sort((child1, child2) => {
      return child1.zCoord - child2.zCoord;
    });
  }
}

function CanvasEditor() {
  //   let myPoint = new Coord({x: 3});
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const eventListenerAdded = useRef(false);
  useEffect(() => {
    if (canvasRef.current) {
      console.log(`in current `, canvasRef.current);
      const canvasWrapper = new CanvasWrapper(canvasRef.current);
      const localArrangementStore = new Arrangement();
      var ctx = canvasRef.current.getContext("2d")!;
      let canvas = canvasRef.current;

      if (!eventListenerAdded.current) {
        var canvasWidth = 500;
        var canvasHeight = 500;

        // if (window.devicePixelRatio > 1) {
        //   canvas.width = canvasWidth * window.devicePixelRatio;
        //   canvas.height = canvasHeight * window.devicePixelRatio;
        //   console.log(
        //     "setting canvas width and height",
        //     "pixel ratio",
        //     window.devicePixelRatio,
        //     "resulting width and height",
        //     canvas.width,
        //     canvas.height
        //   );
        //   canvas.style.width = canvasWidth + "px";
        //   canvas.style.height = canvasHeight + "px";
        //   // ctx.scale(window.devicePixelRatio, window.devicePixelRatio);
        // } else {
        //   canvas.style.width = canvasWidth + "px";
        //   canvas.style.height = canvasHeight + "px";
        //   // ctx.scale(window.devicePixelRatio, window.devicePixelRatio);
        // }

        // Get the device pixel ratio, falling back to 1.
        var dpr = window.devicePixelRatio || 1;

        // Get the size of the canvas in CSS pixels.
        var rect = canvas.getBoundingClientRect();
        let rect2 = { width: 500, height: 500 }
        // Give the canvas pixel dimensions of their CSS
        // size * the device pixel ratio.
        canvas.width = rect2.width * dpr;
        canvas.height = rect2.height * dpr;

        canvas.style.width = rect2.width + "px";
        canvas.style.height = rect2.height + "px";

        // Scale all drawing operations by the dpr, so you
        // don't have to manually scale each draw operation.
        ctx.scale(dpr, dpr);
        console.log("device pixel ratio", dpr, window.devicePixelRatio, "bounding rect", rect)

        canvasRef.current.addEventListener("drag", (ev: DragEvent) => {
          console.log("drag event", ev);
        });

        canvasRef.current.addEventListener(
          "click",
          (ev: MouseEvent) => {
            let clientX = ev.clientX;
            let clientY = ev.clientY;
            let clientCoord = new Coord({ x: clientX, y: clientY });
            let htmlCoord = canvasWrapper.resolveCanvasCoord();

            console.log("html coord", htmlCoord);
            let localHtmlCoord = clientCoord.minusCopy(htmlCoord);
            console.log("local html coord", localHtmlCoord);
            let clickedChild =
              localArrangementStore.getClickedChild(localHtmlCoord);
            console.log("clicked child", clickedChild);
          },
          false
        );
        eventListenerAdded.current = true;
      }
      // localArrangementStore.addChild(
      //   new Element({
      //     shape: new Rectangle({ height: 50, width: 50 }),
      //     localCoord: { x: 100, y: 0 },
      //   })
      // );
      // localArrangementStore.addChild(
      //   new Element({
      //     shape: new Circle({ radius: 50 }),
      //     localCoord: { x: 0, y: 0 },
      //   })
      // );
      localArrangementStore.addChild(
        new Element({
          shape: new Rectangle({ width: 10, height: 10 }),
          localCoord: { x: 0, y: 0 },
        })
      );
      const arrangementPainter = new ArrangementPainter(new CanvasProxy(ctx));
      // arrangementPainter.paint(localArrangementStore);

      canvas.style.scale = "1 1";
      ctx.scale(1, 1);
      ctx.fillRect(10, 10, 10, 10);
    }
  }, []);
  return (
    <>
      {/* <div>
        <p>Hallo Guten Tag</p>
      </div> */}
      <canvas ref={canvasRef}></canvas>
    </>
  );
}

export default CanvasEditor;
