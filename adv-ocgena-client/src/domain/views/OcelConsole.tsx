import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { useObservableState } from 'observable-hooks';
import Graph from 'renderer/components/Graph';
import { ProjectWindow } from '../domain';
import { ProjectWindowManager } from '../StructureNode';
import { Console } from 'renderer/allotment-components/console';
import { SavedFile } from 'main/main';
import { useState } from 'react';
import { produce } from 'immer';

export type OcelConsoleState = {
  canExport: boolean;
};

export class OcelConsole implements ProjectWindow {
  readonly title: string = 'Generated OCEL';
  isOpened: boolean = false;
  static id: string = 'Generated OCEL';
  id = OcelConsole.id;

  private currentOcel: any;

  outputLine$ = new Subject<string[] | undefined>();
  clean$ = new Subject<boolean>();
  fitRequest$ = new Subject<boolean>();

  state$ = new BehaviorSubject<OcelConsoleState>({
    canExport: false,
  });
  onExport : (savedFile : SavedFile) => void;

  constructor(onExport: (savedFile: SavedFile) => void) {
    this.onExport = onExport
  }

  set ocel(ocel: any) {
    console.log("setting ocel " + ocel)
    this.currentOcel = ocel;
    console.log('current ocel ' + this.currentOcel)

    this.outputLine$.next([JSON.stringify(this.currentOcel)])
    let newState = produce(this.state$.getValue(), (draft) => {
      draft.canExport = this.currentOcel != null
    })
    this.state$.next(newState)
  }

  readonly onExportClick = () => {
    console.log('exporting ocel, current value ' + this.currentOcel)
    let contents = JSON.stringify(this.currentOcel)

    this.onExport({
      extension: "json",
      fileType: "OCEL JSON",
      contents: contents,
    })
  }

  clean() {
    this.outputLine$.next(undefined);
  }

  createReactComponent = (
    onSizeChangeObservable: Observable<number[]>,
    visible: boolean
  ) => {
    const state = useObservableState(this.state$)

    return (
      <div className="flex h-full w-full flex-col">
        <div
          className={`flex flex-grow-0 h-9 flex-row items-start justify-start bg-zinc-50`}
        >
            <button
              disabled={!state.canExport}
              onClick={this.onExportClick}
              className={`
            relative
            flex
            flex-row 
            rounded-none
            bg-transparent
            px-2
            shadow-none
            transition-colors
            duration-300
            ease-in-out
           `}
            >
              <div className={`relative pe-1 ps-1 text-xs text-black`}>
                Export OCEL
              </div>
            </button>
        </div>

        <div className="w-full flex-grow">
          <Console
            sizeChange$={onSizeChangeObservable}
            outputLine$={this.outputLine$}
            clean$={this.clean$}
            fitRequest$={this.fitRequest$}
            visible={visible}
          />
        </div>
      </div>
    );
  };
}
