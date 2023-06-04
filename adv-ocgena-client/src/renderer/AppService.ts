import { AST } from 'ocdot-parser';
import { PeggySyntaxError } from 'ocdot-parser/lib/ocdot.peggy';
import { OCDotToDOTConverter } from 'ocdot/converter';
import { Observable, Subject } from 'rxjs';
import * as rxops from 'rxjs/operators'

export type OcDotContent = string

export class AppService {

    private ocDotContentSubject = new Subject<OcDotContent>();
    private graphvizDot = new Subject<string>();
    private internalEditorSubject = new Subject<string>();
    private graphvizLoading = new Subject<boolean>();

    private convertRawOcDotToDot(rawOcDot: string) : string | null {
        let result = null
        try {
            const ast = AST.parse(rawOcDot, { rule: AST.Types.OcDot })
            const converter = new OCDotToDOTConverter(ast);

            result = converter.compileDot();
        } catch (e: PeggySyntaxError | any) {
            console.log(e)
        }
        return result
    }

    showLoading() {
        this.graphvizLoading.next(true);
    }

    hideLoading() { 
        this.graphvizLoading.next(false);
    }

    getGraphvizObservable() : Observable<string> {
        return this.graphvizDot;
    }

    getGraphvizLoading() : Observable<boolean> {
        return this.graphvizLoading
    }

    getOcDotFileSourceObservable(): Observable<string> {
        return this.ocDotContentSubject;
    }

    initialize() {
        window.electron.ipcRenderer.on('file-opened', (fileName, fileContents) => {
            this.ocDotContentSubject.next(fileContents as OcDotContent)
        });


        this.internalEditorSubject.pipe(
            rxops.tap((value) => {
                this.showLoading();
            }),
            rxops.debounceTime(1000),
            rxops.map(((rawOcDot) => {
                console.log("accepting ocdot value " + rawOcDot);
                return this.convertRawOcDotToDot(rawOcDot);
            })))
            .subscribe((newDot) => {
                console.log("new dot: " + newDot)
                if (newDot) {
                    this.graphvizDot.next(newDot);
                }
                this.hideLoading();
            })
    
    }

    openNewFile() {
        window.electron.ipcRenderer.sendMessage('open', []);
    }

    onNewOcDotEditorValue(newValue: string) {
        this.internalEditorSubject.next(newValue);
    }
}

export const appService = new AppService();