import styles from './actions-bar.module.css'
import "@vscode/codicons/dist/codicon.css";

export type ActionBarProps = {
    startButtonMode: "executing" | "start";
    pauseButtonEnabled: boolean,
    onClickStart: () => void,
    onClickRefresh: () => void,
    onOpenNewFile: () => void,
}

export function ActionBar(
    {
        startButtonMode,
        pauseButtonEnabled,
        onClickStart,
        onClickRefresh,
        onOpenNewFile,
    }: ActionBarProps
) {
    let showOutline = startButtonMode == "executing"

    return (
        <div className={`${styles.actionBar} h-full flex`} >

            <button onClick={onClickStart} className={`
                ${showOutline ? "outline-gree-500 outline-2 outline" : ""}
                bg-zinc-700
                relative
                flex-row
                flex    
                px-2 py-2
                text-green-500
                rounded-sm
                active:bg-green-800`}>
                <div className={`codicon codicon-debug-start relative text-sm ${styles.myCodeIcon}`}></div>
                <div className='ps-1 text-gray-200 relative text-sm pe-1'>Start</div>
            </button>

            <button onClick={onClickRefresh} className={`
                bg-zinc-700
                relative
                flex-row
                flex    
                px-2 py-2
                text-yellow-400
                ms-2
                rounded-sm
                active:bg-yellow-700`}>
                <div className={`codicon codicon-debug-restart relative text-sm ${styles.myCodeIcon}`}></div>
                <div className='ps-1 text-gray-200 relative text-sm pe-1'>Reload</div>
            </button>

            <button onClick={onOpenNewFile} className={`
                bg-zinc-700
                relative
                flex-row
                flex    
                px-2 py-2
                text-gray-200
                ms-2
                rounded-sm
                active:bg-zinc-400`}>
                <div className={`codicon codicon-symbol-file relative text-sm ${styles.myCodeIcon}`}></div>
                <div className='ps-1 text-gray-200 relative text-sm pe-1'>Open file</div>
            </button>
        </div>
    )
}