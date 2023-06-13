import styles from './actions-bar.module.css';
import '@vscode/codicons/dist/codicon.css';
import { FileType } from 'main/preload';

import * as React from 'react';

export interface ActionButtonProps {
  onClick: () => void;
  text: string;
  iconClass: string;
  buttonStyle?: string;
  disabled?: boolean;
}

export function ActionButton(props: ActionButtonProps) {
  return (
    <button
      disabled={props.disabled}
      onClick={props.onClick}
      className={`
            relative
            flex
            flex-row 
            rounded-none
            px-2
            shadow-none
            transition-colors
            duration-300
            ease-in-out
            ${props.buttonStyle ? props.buttonStyle : ''} `}
    >
      <div
        className={`codicon relative scale-90 text-xs ${props.iconClass} `}
      ></div>
      <div className={`relative pe-1 ps-1 text-xs text-black`}>
        {props.text}
      </div>
    </button>
  );
}

export type StartButtonMode = 'executing' | 'start' | 'disabled';

export type ActionBarProps = {
  startButtonMode: StartButtonMode;
  pauseButtonEnabled: boolean;
  onClickStop: () => void;
  onClickStart: () => void;
  onClickRefresh: () => void;
  onOpenNewFile: (fileType: FileType) => void;
};

export function ActionBar({
  startButtonMode,
  pauseButtonEnabled,
  onClickStart,
  onClickStop,
  onClickRefresh,
  onOpenNewFile,
}: ActionBarProps) {
  let showOutline = startButtonMode == 'executing';
  let disabled = startButtonMode == 'disabled';

  return (
    <div
      className={`${styles.actionBar} flex h-9 flex-row items-start justify-start bg-zinc-50`}
    >
      <div
        className="
            rounded-none
            border-0
            border-r-1 border-solid border-r-black border-opacity-10 bg-transparent px-2 text-sm text-black"
      >
        <b>OCGena</b>
      </div>
      <ActionButton
        onClick={() => {
          onOpenNewFile('ocdot');
        }}
        iconClass="codicon-symbol-file"
        text="Open model file"
        buttonStyle={`text-black bg-transparent text-opacity-75 border-solid border-0 border-r-black border-r-1 border-opacity-10  hover:bg-zinc-200`}
      />

      <ActionButton
        onClick={() => {
          onOpenNewFile('simconfig');
        }}
        iconClass="codicon-symbol-file"
        text="Open configuration file"
        buttonStyle={`text-black bg-transparent text-opacity-75 border-solid border-0 border-r-black border-r-1 border-opacity-10  hover:bg-zinc-200`}
      />

      {startButtonMode != 'executing' ? (
        <ActionButton
          onClick={onClickStart}
          iconClass="codicon-debug-start"
          text="Start"
          buttonStyle={`
          ml-2
          ${
            disabled
              ? 'text-opacity-50 bg-zinc-300'
              : 'text-opacity-75 hover:bg-green-300'
          }  bg-transparent text-black  border-solid border-0 border-r-black border-black border-l-1 border-r-1 border-opacity-10   ${
            showOutline ? 'outline-green-500 outline-2 outline' : ''
          }`}
          disabled={disabled}
        />
      ) : (
        <ActionButton
          onClick={onClickStop}
          iconClass="codicon-stop-circle"
          text="Stop"
          buttonStyle={`ml-2 text-opacity-75 bg-green-400 hover:bg-red-300  text-black  border-solid border-black border-l-1 border-r-1 border-opacity-10`}
        />
      )}

      {/* <ActionButton
        onClick={onClickRefresh}
        iconClass="codicon-debug-restart"
        text="Restart"
        buttonStyle={`text-black text-opacity-75 border-solid border-0 border-r-black border-r-1 border-opacity-10  hover:bg-yellow-200`}
      /> */}
    </div>
  );
}
