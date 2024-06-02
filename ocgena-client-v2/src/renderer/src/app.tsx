import { Allotment } from "allotment";
import { createRoot } from "react-dom/client";
import "allotment/dist/style.css";
import "./app.module.css";
import { useEffect } from "react";
import { useAppDispatch } from "./app/hooks.ts";
import { createNewProject, load } from "./app/redux.ts";
import { Provider } from "react-redux";
import { store } from "./app/store.ts";
import React from "react";
import { Editor } from "./features/editor/Editor.tsx";

const root = createRoot(document.getElementById("container"));

// const kek = dbStore

export function App() {
  const dispatch = useAppDispatch();

  useEffect(() => {
    // dispatch(load())
  }, []);

  return (
    <>
      <div className={".container"}>
        <Editor />
        {/* <h2>Hello from React!</h2> */}
        {/* <Allotment >
          <Allotment.Pane minSize={200}>
            <div>Pane 1</div>
          </Allotment.Pane>
          <Allotment.Pane snap>
            <div>Pane 1</div>
          </Allotment.Pane>
        </Allotment> */}
      </div>
    </>
  );
}

root.render(
  <React.StrictMode>
    <Provider store={store}>
      <App />
    </Provider>
  </React.StrictMode>
);
