import "allotment/dist/style.css";
import "./app.module.css";
import { useEffect } from "react";
import { useAppDispatch } from "./app/hooks";
import { loadV2 } from "./features/epicsTypes";
import "../index.css"
import { Layout } from "./features/layout/layout";

export function App() {
  const dispatch = useAppDispatch();

  useEffect(() => {
    dispatch(loadV2());
  }, []);

  return (
    <Layout></Layout>
    // <div className="h-3">
    //   <h2 className="text-3xl font-bold underline">You are a bold one, General Kenobi!</h2>
    // </div>
  );
}
