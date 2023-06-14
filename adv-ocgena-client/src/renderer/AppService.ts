import { Project } from 'domain/Project';
import { Channels, FileType } from 'main/preload';
import { ModelFiles, SavedFile, SuccessfullySavedFile } from 'main/main';
import { SimulationClientStatus } from '../main/shared'
export class AppService {

  private openedProject = new Project(
    (ocdot : string | null) => {
      window.electron.ipcRenderer.sendMessage('update-ocdot' as Channels, [ocdot])
    },
    (simConfig : string | null) => {
      window.electron.ipcRenderer.sendMessage('update-simconfig' as Channels, [simConfig])
    },
    () => {
      window.electron.ipcRenderer.sendMessage('launch-sim', [])
    },
    () => {
      window.electron.ipcRenderer.sendMessage('stop-sim' as Channels, [])
    }
  );

  getActiveProject(): Project {
    return this.openedProject;
  }

  getProjectState$() {
    return this.openedProject.projectState$;
  }

  onClickStart() {
    this.openedProject.onClickStart();
  }

  onClickStop(): void {
    console.log("on click stop")
    this.openedProject.onClickStop();
  }

  openFile(fileType: FileType) {
    switch (fileType) {
      case 'ocdot':
        this.openModelFile();
        break;
      case 'simconfig':
        this.openConfigurationFile();
        break;
    }
  }

  openConfigurationFile() {
    let fileType: FileType = 'simconfig';
    window.electron.ipcRenderer.sendMessage('open', [fileType]);
  }

  openModelFile() {
    let fileType: FileType = 'ocdot';
    window.electron.ipcRenderer.sendMessage('open', [fileType]);
  }

  initialize() {
    window.electron.ipcRenderer.on('write-ocel-console', (ocel: any) => {
      this.openedProject.writeOcelConsole(ocel);
    })
    window.electron.ipcRenderer.on('write-html-console', (htmlLines: string[] | undefined | any) => {
      this.openedProject.writeHtmlConsole(htmlLines)
    })
    window.electron.ipcRenderer.on('write-error-console', (errors: string[] | undefined | any) => {
      this.openedProject.writeErrors(errors)
    })
    window.electron.ipcRenderer.on('write-simulation-status', (simulationStatus: SimulationClientStatus | any) => {
      this.openedProject.updateSimulationStatus(simulationStatus)
    })
    window.electron.ipcRenderer.on(
      'file-opened',
      (fileName, fileContents, fileType) => {
        switch (fileType as FileType) {
          case 'ocdot':
            this.openedProject.setModelOcDotContentsFromFile(
              fileContents as string,
              fileName as string
            );
            break;
          case 'simconfig':
            this.openedProject.setSimConfigContentsFromFile(
              fileContents as string,
              fileName as string
            );
            break;
        }
      }
    );
    window.electron.ipcRenderer.on(
      'saved-current-file',
      (...args: unknown[]) => {
        if (!(args && args[0])) {
          return;
        }
        let successfullySavedFile = args[0] as SuccessfullySavedFile;
        console.log('setting successfully saved file data %s', successfullySavedFile.fileName)
        switch (successfullySavedFile.extension) {
          case 'ocdot':
            this.openedProject.setModelOcDotContentsFromFile(
              successfullySavedFile.contents,
              successfullySavedFile.fileName
            );
            break;
          case 'yaml':
            this.openedProject.setSimConfigContentsFromFile(
              successfullySavedFile.contents,
              successfullySavedFile.fileName
            );
            break;
        }
        return;
      }
    );
    window.electron.ipcRenderer.on(
      'save-the-current-file',
      (...args: unknown[]) => {
        let lastFocusedEditor = this.openedProject.getLastFocusedEditor();

        if (lastFocusedEditor) {
          let delegate = lastFocusedEditor.editorDelegate;
          let content = delegate.currentContent;
          let fileName = delegate.openedFilePath;

          let savedFile: SavedFile = {
            extension: delegate.extension,
            fileType: delegate.fileType,
            contents: content,
            fileName: fileName,
            defaultDirPath: undefined,
          };

          console.log('sending file data to save: %s', savedFile.fileName);
          window.electron.ipcRenderer.sendMessage('save-the-current-file', [
            savedFile,
          ]);
        }
      }
    );
    window.electron.ipcRenderer.on('transform-ocel', (...args : unknown[]) => {
      if (!(args && args[0] && args[0] as string)) {
        return;
      }
      let savedFile : SavedFile = {
        extension : "jsonocel",
        fileType: "OCEL JSON",
        contents: args[0] as string,
      } 
      window.electron.ipcRenderer.sendMessage('save-the-current-file', [savedFile])
    });
    window.electron.ipcRenderer.on(
      'save-all-shortcut',
      (...args: unknown[]) => {
        let ocDotContent = this.openedProject.modelEditor.collectFile();
        let simConfigContent =
          this.openedProject.simulationConfigEditor.collectFile();

        let modelFiles = {
          ocDot: ocDotContent.contents,
          ocDotFilePath: ocDotContent.filePath,
          simConfig: simConfigContent.contents,
          simConfigFilePath: simConfigContent.filePath,
        } as ModelFiles;

        console.log('collected model files and sending');
        window.electron.ipcRenderer.sendMessage('save-all-shortcut', [
          modelFiles,
        ]);
      }
    );
  }
}

export const appService = new AppService();
