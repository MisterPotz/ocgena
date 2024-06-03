import "allotment/dist/style.css";
import "./app.module.css";
import { useEffect } from "react";
import { useAppDispatch } from "./app/hooks";
import { Provider } from "react-redux";
import { store } from "./app/store";
import { load } from "./app/redux";
import {} from 'rxjs/operators'
import { loadV2 } from "./features/epicsTypes";

export function App() {
  const dispatch = useAppDispatch();

  useEffect(() => {
    dispatch(loadV2()) 
  }, []);

  return (
      <div className={"container"}>
        <h2>You are a bold one, General Kenobi!</h2>
      </div>
  );
}
