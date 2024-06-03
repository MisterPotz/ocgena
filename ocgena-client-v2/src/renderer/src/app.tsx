import "allotment/dist/style.css";
import "./app.module.css";
import { useEffect } from "react";
import { useAppDispatch } from "./app/hooks.ts";
import { Provider } from "react-redux";
import { store } from "./app/store.ts";
import React from "react";

export function App() {
  const dispatch = useAppDispatch();

  useEffect(() => {
    // dispatch(load())
  }, []);

  return (
    <Provider store={store}>
      <div className={"container"}>
        <h2>Hello from React!</h2>
      </div>
    </Provider>
  );
}
