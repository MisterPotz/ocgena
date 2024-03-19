import { useEffect, useRef } from "react";
import Konva from 'konva';

function setCanvasCorrect(
  width: number,
  height: number,
  canvas: HTMLCanvasElement
) {
  let ctx = canvas.getContext("2d");
  let density = window.devicePixelRatio;
  canvas.width = width * density;
  canvas.height = height * density;

  canvas.style.width = `${width}px`;
  canvas.style.height = `${height}px`;
  ctx?.scale(density, density);
}

declare global {
  interface CanvasRenderingContext2D {
    rotateWithOrigin(x: number, y: number, degree: number): any;
    transformWith(transformation: number[]): any;
  }
}

CanvasRenderingContext2D.prototype.rotateWithOrigin = function (
  x: number,
  y: number,
  degree: number
) {
  let ctx: CanvasRenderingContext2D = this;
  degree = (Math.PI / 180) * degree;
  ctx.transform(
    Math.cos(degree),
    Math.sin(degree),
    -Math.sin(degree),
    Math.cos(degree),
    x,
    y
  );
};

CanvasRenderingContext2D.prototype.transformWith = function (
  transformation: number[]
) {
  this.transform(
    transformation[A],
    transformation[B],
    transformation[C],
    transformation[D],
    transformation[E],
    transformation[F]
  );
};

function rotateWithOrigin(x: number, y: number, degree: number, out: number[]) {
  degree = (Math.PI / 180) * degree;
  out[A] = Math.cos(degree);
  out[B] = Math.sin(degree);
  out[C] = -Math.sin(degree);
  out[D] = Math.cos(degree);
  out[E] = x;
  out[F] = y;
}

let degree = 30;

function identityMatrix(): number[] {
  return [1, 0, 0, 0, 1, 0, 0, 0, 1];
}

let inverseTransformation = identityMatrix();
let directTransformation = identityMatrix();

const A = 0;
const B = 3;
const C = 1;
const D = 4;
const E = 2;
const F = 5;

let tempVector = [0, 0, 0];

let X = 0;
let Y = 1;

function multiply(matrix: number[], x: number, y: number, out: number[]) {
  out[X] = matrix[0] * x + matrix[1] * y + matrix[2] * 1;
  out[Y] = matrix[3] * x + matrix[4] * y + matrix[5] * 1;
}

function inverseMatrix(matrix: number[], out: number[]) {
  let denom = matrix[A] * matrix[D] - matrix[B] * matrix[C];
  out[A] = matrix[D] / denom;
  out[C] = -matrix[C] / denom;
  out[B] = -matrix[B] / denom;
  out[D] = matrix[A] / denom;
  out[E] = -(matrix[E] * matrix[D] - matrix[C] * matrix[F]) / denom;
  out[F] = -(matrix[F] * matrix[A] - matrix[B] * matrix[E]) / denom;
}

function render() {
  let ctx = (
    window.document.getElementById("canvas") as HTMLCanvasElement
  ).getContext("2d")!;
  ctx.clearRect(0, 0, 500, 500);
  ctx.save();

  //   ctx.translate(150, 150);
  //   ctx.rotate(-30)
  rotateWithOrigin(100, 100, degree, directTransformation);
  inverseMatrix(directTransformation, inverseTransformation);
  ctx.transformWith(directTransformation);

  // ctx.rotateWithOrigin(100, 100, degree);
  // ctx.transform(1, 0, 0, 1, 150, 150);
  ctx.fillStyle = "black"
  ctx.fillRect(-50, -50, 100, 100);
  ctx.fillStyle = "red";
  ctx.fillText("Прикольный текст", -40, 0);

  ctx.strokeStyle = "blue";
  ctx.strokeRect(-55, -55, 110, 110);

  ctx.restore();

  degree += 1;

  window.requestAnimationFrame(render);
}

function ExperimentalCanvas() {
  const canvas = useRef<HTMLCanvasElement>(null);
  const canvasSetup = useRef(false);

  useEffect(() => {
    if (canvas.current && !canvasSetup.current) {
      canvasSetup.current = true;
      setCanvasCorrect(500, 500, canvas.current);

      canvas.current.addEventListener(
        "click",
        (ev: MouseEvent) => {
          // let clickedChild =
          //   localArrangementStore.getClickedChild(localHtmlCoord);
          console.log("click coord", ev.clientX, ev.clientY);
          console.log("direct transf", directTransformation);
          multiply(inverseTransformation, ev.clientX, ev.clientY, tempVector);
          console.log("transformed vector", tempVector);
        },
        false
      );

      render();
    }
  }, []);

  return (
    <>
      <canvas id={"canvas"} ref={canvas}></canvas>
    </>
  );
}

export default ExperimentalCanvas;
