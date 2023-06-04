import styles from './actions-bar.module.css'
import "@vscode/codicons/dist/codicon.css";

import * as React from 'react';

export interface ActionButtonProps {
    onClick: () => void;
    text: string;
    iconClass: string;
    buttonStyle?: string;
}

export function ActionButton(props: ActionButtonProps) {
    return (
        <button onClick={props.onClick} className={`
            bg-transparent
            relative
            flex-row 
            flex
            px-2
            py-2
            rounded-sm
            transition-colors
            duration-300
            ease-in-out
            ${props.buttonStyle ? props.buttonStyle : ""} `}>
            <div className={`relative text-sm codicon ${props.iconClass} !top-0.5`}></div>
            <div className={`ps-1 text-gray-200 relative text-sm pe-1`}>{props.text}</div>
        </button>

    );
}

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

            <ActionButton
                onClick={onClickStart}
                iconClass='codicon-debug-start'
                text='Start'
                buttonStyle={`text-green-500 hover:bg-green-800 ${showOutline ? "outline-green-500 outline-2 outline" : ""}`}
            />

            <ActionButton
                onClick={onClickRefresh}
                iconClass='codicon-debug-restart'
                text='Restart'
                buttonStyle={`text-yellow-400 hover:bg-yellow-700`}
            />

            <ActionButton
                onClick={onClickRefresh}
                iconClass='codicon-symbol-file'
                text='Open file'
                buttonStyle={`text-gray-200 hover:bg-zinc-400`}
            />
        </div>
    )
}