import { Allotment } from "allotment";
import { createRoot } from "react-dom/client";
import "allotment/dist/style.css";
import styles from "./app.module.css";
import { useEffect } from "react";
import { useAppDispatch } from "./app/hooks.ts";
import { createNewProject } from "./app/redux.ts";

const root = createRoot(document.body);

// const kek = dbStore

export const App = () => {
  const dispatch =  useAppDispatch()
  useEffect(() => {
    dispatch(createNewProject())
  }, []);

  return (
    <>
      <div className={styles.container}>
        <h2>Hello from React!</h2>
        <Allotment>
          <Allotment.Pane minSize={200}>
            <div>Pane 1</div>
          </Allotment.Pane>
          <Allotment.Pane snap>
            <div>Pane 1</div>
          </Allotment.Pane>
        </Allotment>
      </div>
    </>
  );
};

root.render(App());
