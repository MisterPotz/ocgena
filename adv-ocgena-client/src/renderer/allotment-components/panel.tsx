import 'xterm/css/xterm.css';

import { useEffect, useRef, useState } from 'react';
import { Terminal } from 'xterm';
import { FitAddon } from 'xterm-addon-fit';
import styles from './panel.module.css';
import { Observable, Subscription } from 'rxjs';
import { useSubscription } from 'observable-hooks';

export type PanelProps = {
  maximized: boolean;
  outputLine$: Observable<string[] | undefined>;
  clean$: Observable<boolean>;
  sizeChange$: Observable<number[]>;
  fitRequest$: Observable<boolean>;
  onClose: () => void;
  onMaximize: () => void;
  onMinimize: () => void;
};

export const Panel = ({
  maximized,
  outputLine$,
  sizeChange$,
  fitRequest$,
  clean$,
  onClose,
  onMaximize,
  onMinimize,
}: PanelProps) => {
  const ref = useRef(null!);
  const [term, setTerminal] = useState<Terminal | undefined>(undefined);
  const [fitAddon, setFitAddon] = useState<FitAddon | undefined>(undefined);

  useSubscription(fitRequest$, (request) => {
    console.log('fitting the addon');
    // fitAddon?.fit();
  });

  console.log('executing panel');
  useEffect(() => {
    let subscriptionWriteLine: Subscription | undefined;
    let subscriptionSizeChange: Subscription | undefined;
    let subscriptionClean: Subscription | undefined;

    if (term) {
      console.log('terminal subscribed to line output');
      // fitAddon?.fit();

      subscriptionWriteLine = outputLine$.subscribe((lines) => {
        if (lines) {
          for (let line of lines) {
            term.writeln(line);
          }
        } else {
          term.clear();
        }
      });
      subscriptionSizeChange = sizeChange$.subscribe((newsize) => {
        // fitAddon?.fit();
      });
      subscriptionClean = clean$.subscribe((clean) => {
        term.clear();
      });
    }

    return () => {
      subscriptionWriteLine?.unsubscribe();
      subscriptionSizeChange?.unsubscribe();
      subscriptionClean?.unsubscribe();
    };
  }, [outputLine$, term]);

  useEffect(() => {
    const term = new Terminal({
      disableStdin: true,
      fontSize: 13,
      cols: 120,
      overviewRulerWidth: 10,
      smoothScrollDuration: 125,
      theme: {
        background: '#FFFFFF',
        foreground: '#333333',
        cursor: '#333333',
        selectionBackground: '#ADD6FF',
        black: '#000000',
        red: '#CD3131',
        green: '#0DBC79',
        yellow: '#ffc107',
        blue: '#2472C8',
        magenta: '#BC3FBC',
        cyan: '#11A8CD',
        white: '#E5E5E5',
        brightBlack: '#666666',
        brightRed: '#F14C4C',
        brightGreen: '#23D18B',
        brightYellow: '#F5F543',
        brightBlue: '#3B8EEA',
        brightMagenta: '#D670D6',
        brightCyan: '#29B8DB',
        brightWhite: '#E5E5E5',
      },
      // theme: { background: 'rgb(30,30,30)' },
    });

    const fitAddon = new FitAddon();
    setFitAddon(fitAddon);

    setTerminal(term);
    term.loadAddon(fitAddon);
    term.open(ref.current);

    const xterm_resize_ob = new ResizeObserver(function (entries) {
      // since we are observing only a single element, so we access the first element in entries array
      try {
        console.log("fitting")
        fitAddon && fitAddon.fit();
      } catch (err) {
        console.log(err);
      }
    });
    
    // start observing for resize
    xterm_resize_ob.observe(ref.current);
    
    term.writeln('Welcome to allotment');
    term.writeln(
      'This is a local terminal emulation, without a real terminal in the back-end.'
    );
    term.writeln('Type some keys and commands to play around.');
    term.writeln('');
    // window.addEventListener('resize', () => {
    //   // fitAddon.fit()
    // });

    term.attachCustomKeyEventHandler((e) => {
      // Ctrl+C or Cmd+C pressed and text is selected
      if (
        (e.ctrlKey || e.metaKey) &&
        e.code === 'KeyC' &&
        term.hasSelection()
      ) {
        window.electron.ipcRenderer.sendMessage('copy', [term.getSelection()]);
        return false; // Prevent the event from being handled by the global listener
      }
      return true;
    });

    // fitAddon.fit();
    fitAddon.activate(term);

    // term.onKey((e: { key: string; domEvent: KeyboardEvent }) => {
    //   const ev = e.domEvent;
    //   const printable = !ev.altKey && !ev.ctrlKey && !ev.metaKey;

    //   if (ev.keyCode === 13) {
    //     prompt();
    //   } else if (ev.keyCode === 8) {
    //     // Do not delete the prompt
    //     /*         if (term._core.buffer.x > 2) {
    //       term.write("\b \b");
    //     } */
    //   } else if (printable) {
    //     term.write(e.key);
    //   }
    // });
  }, []);

  return (
    <div className={` flex h-full w-full flex-col border-white bg-white pr-0`}>
      {/* <div className={`${styles.title} flex h-9 overflow-hidden pl-2 pr-2 justify-between`}> */}
      {/* <div className={styles.actionBar}>
          <ul className={styles.actionsContainer}>
            <li className={classNames(styles.actionItem, 'checked', "text-opacity-75 text-black")}>
              <a className={classNames(styles.actionLabel, "text-black text-opacity-75")}>
                Terminal
              </a>
              <div className={classNames(styles.activeItemIndicator, "bg-white bg-opacity-75")} />
            </li>
          </ul>
        </div> */}
      {/* <div>
          <ul className={styles.actionsContainer}>
            <li>
              {maximized ? (
                <a
                  className={classNames(
                    'codicon codicon-chevron-down',
                    styles.actionLabel
                  )}
                  role="button"
                  title="Minimize Panel Size"
                  onClick={onMinimize}
                />
              ) : (
                <a
                  className={classNames(
                    'codicon codicon-chevron-up',
                    styles.actionLabel
                  )}
                  role="button"
                  title="Maximize Panel Size"
                  onClick={onMaximize}
                />
              )}
            </li>
            <li>
              <a
                className={classNames(
                  'codicon codicon-close',
                  styles.actionLabel
                )}
                role="button"
                title="Close Panel"
                onClick={onClose}
              ></a>
            </li>
          </ul>
        </div>
      </div> */}
      <div className={`${styles.content} w-full overflow-hidden pr-0`}>
        <div
          ref={ref}
          className={`${styles.terminalWrapper} overflow-hidden !pr-0`}
        ></div>
      </div>
    </div>
  );
};
