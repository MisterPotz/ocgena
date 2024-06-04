import "allotment/dist/style.css";
import "./app.module.css";
import { useEffect } from "react";
import { useAppDispatch } from "./app/hooks";
import { loadV2 } from "./features/epicsTypes";

export function App() {
  const dispatch = useAppDispatch();

  useEffect(() => {
    dispatch(loadV2());
  }, []);

  return (
    <div className="containerMy">
      <h2>You are a bold one, General Kenobi!</h2>
    </div>
  );
}
