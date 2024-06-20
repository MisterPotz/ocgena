import { dots } from "./DotField";

export { }
declare global {
    export interface Number {
        closestDotX(): number;
        closestDotY() : number
        closestSize() : number;
    }
}
Number.prototype.closestDotX = function (this: number)  {
    return dots.getClosestX(this);
};
Number.prototype.closestDotY = function (this: number)  {
    return dots.getClosestY(this);
};
Number.prototype.closestSize = function (this: number)  {
    return dots.getClosestSize(this);
};
