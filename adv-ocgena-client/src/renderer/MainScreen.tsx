import { Allotment } from "allotment";
import { EditorWrapper } from "./components/Editor";
import { Hello } from "./App";
import "allotment/dist/style.css"

export function MainScreen() {
    // return (
    //     <Allotment vertical={false}>
    //         <Allotment.Pane minSize={300} >
    //             <Hello />
    //         </Allotment.Pane>
    //         <Allotment.Pane minSize={300} >
    //             <Hello />
    //         </Allotment.Pane>
    //     </Allotment>
    // );
    return (
        <Allotment vertical={false} onDragEnd={
            () => {
                
            }
        }>
            <Allotment.Pane minSize={300} >
                <EditorWrapper />
            </Allotment.Pane>
            <Allotment.Pane minSize={300} snap={true} >
                <Hello />
            </Allotment.Pane>
        </Allotment>
    )
}