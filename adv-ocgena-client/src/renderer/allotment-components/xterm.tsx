import React, { useEffect, useRef } from 'react';
import { Observable, Subscription } from 'rxjs';
import { container } from 'webpack';
import { Terminal } from 'xterm';
import { FitAddon } from 'xterm-addon-fit';

export type XTermProps = {
    outputLine$: Observable<string[] | undefined>;
    clean$: Observable<boolean>;
    visible: boolean,
    sizeChange$: Observable<number[]>;
    fitRequest$: Observable<boolean>;
};

const XtermComponent = ({
    outputLine$,
    sizeChange$,
    visible,
    fitRequest$,
    clean$,
  }: XTermProps) => {
  // Create a ref object to refer to the terminal's DOM element
  const containerRef = useRef<HTMLDivElement | null>(null);
  const terminalRef = useRef<Terminal | null>(null); // We keep terminal instance in a ref
  const fitAddonRef = useRef<FitAddon | null>(null); // Also keeping FitAddon instance in a ref

  useEffect(() => {
    let subscriptionWriteLine: Subscription | undefined;
    let subscriptionSizeChange: Subscription | undefined;
    let subscriptionClean: Subscription | undefined;

    if (terminalRef.current) {
    let term = terminalRef.current

      console.log('terminal subscribed to line output');
      // fitAddon?.fit();

      subscriptionWriteLine = outputLine$.subscribe((lines) => {
        let term = terminalRef.current
        if (lines) {
            if (term) { 
                for (let line of lines) { 
                  term.writeln(line);
                }
            }
        } else {
          term?.clear();
        }
      });
      subscriptionSizeChange = sizeChange$.subscribe((newsize) => {
        // fitAddon?.fit();
      });
      subscriptionClean = clean$.subscribe((clean) => {
        let term = terminalRef.current
        term?.clear();
      });
    }

    return () => {
      subscriptionWriteLine?.unsubscribe();
      subscriptionSizeChange?.unsubscribe();
      subscriptionClean?.unsubscribe();
    };
  }, [terminalRef.current]);

  useEffect(() => {
    if (visible) {
        terminalRef.current?.scrollToBottom()
    }
  }, [visible])
  
  useEffect(() => {
    if (containerRef.current) {
        // Create a new terminal and attach it to the div
        const terminal = new Terminal({
            disableStdin: true,
            tabStopWidth: 2,
            convertEol: true,
            // cols: 120,
            overviewRulerWidth: 10,
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
        terminal.open(containerRef.current);
        const fitAddon = new FitAddon();
        
        terminal.loadAddon(fitAddon); // We load the addon to the terminal
        fitAddon.fit(); // We use .fit() to make the terminal size fit its container
    
        terminalRef.current = terminal;
        fitAddonRef.current = fitAddon;

        let disp = terminalRef.current.onScroll((numb, s) => {
            // console.log("scrolling terminal to %s", numb)
        })
        // Write something to the new terminal
        terminal.writeln('Hello from xterm.js\r\n'.repeat(100));

        // Clean up the terminal when the component is unmounted
        return () => {
            console.log("disposing of terminal");
            disp.dispose();
            terminal.dispose();
        }
    }
  }, [containerRef.current]);  // The effect should run only once when the component is mounted

  useEffect(() => {
    // Handle window resize
    const handleResize = () => {
      // On window resize, refit the terminal
      if (fitAddonRef.current){ 
        fitAddonRef.current.fit();
      }
    };

    window.addEventListener('resize', handleResize);
    return () => {
      window.removeEventListener('resize', handleResize);
    };
  }, []);

  return (
    <div className='h-full w-full' ref={containerRef}></div>
  );
};

export default XtermComponent;