import { Observable } from "rxjs";
import { useObservableState } from "observable-hooks";
import Graph from "renderer/components/Graph";
import { ProjectWindow } from "./domain";
import { ProjectWindowManager } from "./StructureNode";


export class GraphvizView implements ProjectWindow {
    readonly title: string = "graphviz";
    isOpened: boolean = false;
    dotObservable: Observable<string>;
    loadingObservable: Observable<boolean>;
    static id: string = "grapvhiz"
    id = GraphvizView.id
    
    constructor(
        dotObservable: Observable<string>,
        loadingObservable: Observable<boolean>) {
        this.dotObservable = dotObservable;
        this.loadingObservable = loadingObservable;
    }

    createReactComponent = (onSizeChangeObservable: Observable<number[]>) => {
        let dot = useObservableState(this.dotObservable);
        let loading = useObservableState(this.loadingObservable, false);
        let dotString = dot ? dot : "";

        return <div className="w-full h-full bg-white relative">
            {/* <div className={`${loading ? "visible" : "invisible"} animate-spin ease-linear rounded-full border-4 border-t-4 border-blue-600 border-t-blue-200 h-8 w-8 absolute right-2 top-2`}></div> */}
            <svg className={`${loading ? "visible" : "invisible"} animate-spin ease-linear h-5 w-5 mr-3 text-blue-500 absolute right-2 top-2`} viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"></circle>
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.789 3 7.938l3-2.647z"></path>
            </svg>

            <Graph
                dotSrc={dotString}
                fit
                transitionDuration={1}
                sizeUpdateObservable={onSizeChangeObservable} />
        </div>;
    };
}
