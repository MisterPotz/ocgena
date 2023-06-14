import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { useObservableState } from 'observable-hooks';
import Graph from 'renderer/components/Graph';
import { ProjectWindow } from '../../main/domain';
import { ProjectWindowManager } from '../StructureNode';
import { Console } from 'renderer/allotment-components/console';
import { SavedFile } from 'main/main';
import { useState } from 'react';
import { produce } from 'immer';
import { red, reset } from 'main/red';
import Anser from 'anser';

export type OcelConsoleState = {
  canExport: boolean;
};

export type OcelObj = any;

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
  onExport: (savedFile: OcelObj) => void;

  constructor(onExport: (savedFile: OcelObj) => void) {
    this.onExport = onExport;
  }

  set ocel(ocel: any) {
    this.currentOcel = ocel;
    if (Object.keys(this.currentOcel['ocel:events']).length > 300) {
      console.log('Log size is too big to be displayed.');
      this.outputLine$.next([
        Anser.ansiToHtml(
          `<div>${red}Log size is too big to be displayed.${reset}</div>`
        ),
      ]);
    } else {
      this.outputLine$.next([
        JSON.stringify(this.currentOcel, undefined, 4).substring(0, 1000),
        Anser.ansiToHtml(`<div>${red}...${reset}</div>`),
        Anser.ansiToHtml(`<div>${red}Export to see full JSON${reset}</div>`),
      ]);
    }

    let newState = produce(this.state$.getValue(), (draft) => {
      draft.canExport = this.currentOcel != null;
    });
    this.state$.next(newState);
  }

  readonly onExportClick = () => {
    // let contents = JSON.stringify(this.currentOcel)

    this.onExport(this.currentOcel);
  };

  clean() {
    this.outputLine$.next(undefined);
  }

  createReactComponent = (
    onSizeChangeObservable: Observable<number[]>,
    visible: boolean
  ) => {
    const state = useObservableState(this.state$);

    return (
      <div className="flex h-full w-full flex-col">
        <div
          className={`flex h-12 flex-grow-0 flex-row items-start justify-start bg-zinc-50 pb-3`}
        >
          <button
            disabled={!state.canExport}
            onClick={this.onExportClick}
            className={`
            relative
            flex
            flex-row 
            rounded-sm
            ml-4
            border-solid
            border-1
            bg-white
            border-black
            border-opacity-20
            hover:bg-zinc-200
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

        <div className="h-full w-full flex-grow">
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
