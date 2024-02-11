import React from 'react'
import ReactDOM from 'react-dom/client'
// import App from './App.tsx'
import CanvasEditor from './CanvasEditor.tsx'
import './index.css'
import ExperimentalCanvas from './ExperimentalCanvas.tsx'
import ExperimentalCanvasKonva from './ExperimentalCanvasKonva.tsx'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    {/* <App /> */}
    <ExperimentalCanvasKonva />
  </React.StrictMode>,
)
