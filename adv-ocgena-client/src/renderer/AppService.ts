import { OcDotContent, ProjectWindowStructure } from 'domain/domain';
import { ProjectSingleSimulationExecutor } from "domain/ProjectSingleSimulationExecutor";
import { Project, ProjectState } from "domain/Project";
import { AST } from 'ocdot-parser';
import { PeggySyntaxError } from 'ocdot-parser/lib/ocdot.peggy';
import { OCDotToDOTConverter } from 'ocdot/converter';
import { Observable, Subject } from 'rxjs';
import * as rxops from 'rxjs/operators'
import { FileType } from 'main/preload';


export class AppService {
    private openedProject = new Project();


    getActiveProject(): Project {
        return this.openedProject;
    }

    getProjectState$() {
        return this.openedProject.projectState$
    }

    onClickStart() {
        this.openedProject.onClickStart();
    }

    openFile(fileType : FileType) {
        switch (fileType) {
            case 'ocdot':
                this.openModelFile()
                break;
            case 'simconfig':
                this.openConfigurationFile();
                break;
        }
    }

    openConfigurationFile() {
        let fileType : FileType = 'simconfig'
        window.electron.ipcRenderer.sendMessage('open', [fileType]);
    }

    openModelFile() {
        let fileType : FileType = 'ocdot'
        window.electron.ipcRenderer.sendMessage('open', [fileType])
    }

    initialize() {
        window.electron.ipcRenderer.on('file-opened', (fileName, fileContents, fileType) => {
            switch(fileType as FileType) {
                case 'ocdot':
                    this.openedProject.setModelOcDotContents(fileContents as string)
                    break;
                case 'simconfig':
                    this.openedProject.setSimConfigContents(fileContents as string)
                    break;
            }
        });
    }
}

export const appService = new AppService(); 