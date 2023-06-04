import { useEffect, useRef, useState } from "react"

import Graph from "renderer/components/Graph"
import { useObservableState } from 'observable-hooks';
import { appService } from "renderer/AppService";
import { loadavg } from "os";
export type GraphvizProps = {
    dotSrc: string,
    loading: boolean,
    registerParentSizeUpdate: (onParentSizeUpdate: () => void) => void;
}

export const GraphvizPane = (
    {
        registerParentSizeUpdate,
        dotSrc,
        loading = true
    }: GraphvizProps
) => {

    // const [graphvizs, setGraphviz] = useState<string>("digraph {a -> b}'")
    console.log("pane is invoked with callback " + registerParentSizeUpdate)

    // useEffect(() => {
    //     if (!graphvizEl.current) return;

    //     setGraphviz((graphvizs) => {
    //         if (graphvizs) { 
    //             graphvizs.fit(true)
    //             .renderDot("digraph {a -> b}'")
    //             return graphvizs
    //         }

    //         const graph = graphviz(graphvizEl.current)
    //         graph.fit(true)
    //         .renderDot("digraph {a -> b}'")
    //         return graph
    //     })

    // }, [dotString])

    const [dot, updateDot] = useObservableState(() => appService.getGraphvizObservable(), "");

    console.log("doing this dot: \n" + dot);

    return (
        <div className="w-full h-full bg-white relative">
            {/* <div className={`${loading ? "visible" : "invisible"} animate-spin ease-linear rounded-full border-4 border-t-4 border-blue-600 border-t-blue-200 h-8 w-8 absolute right-2 top-2`}></div> */}
            <svg className={`${loading ? "visible" : "invisible"} animate-spin ease-linear h-5 w-5 mr-3 text-blue-500 absolute right-2 top-2`} viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"></circle>
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.789 3 7.938l3-2.647z"></path>
            </svg>

            <Graph
                dotSrc={dot}
                fit
                transitionDuration={1}
                registerParentSizeUpdate={registerParentSizeUpdate}
            />
        </div>
    )
}
