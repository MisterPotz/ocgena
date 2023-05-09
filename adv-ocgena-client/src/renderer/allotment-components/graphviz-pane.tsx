import { useEffect, useRef, useState } from "react"

import Graph from "renderer/components/Graph"
import { useObservableState } from 'observable-hooks';
import { appService } from "renderer/AppService";
export type GraphvizProps = {
    dotSrc: string,
    registerParentSizeUpdate: (onParentSizeUpdate: () => void) => void;
}

export const GraphvizPane = (
    {
        registerParentSizeUpdate,
        dotSrc
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
        <div className="w-full h-full bg-white">
            <Graph
                dotSrc={dot}
                fit
                transitionDuration={1}
                registerParentSizeUpdate={registerParentSizeUpdate}
            />
        </div>
    )
}
