/* eslint global-require: off, no-console: off, promise/always-return: off */

/**
 * This module executes inside of electron's main process. You can start
 * electron renderer process from here and communicate with the other processes
 * through IPC.
 *
 * When running `npm run build` or `npm run build:main`, this file is compiled to
 * `./src/main.js` using webpack. This gives us some performance wins.
 */
import { app, BrowserWindow, shell, ipcMain, dialog, screen, clipboard } from 'electron';
import { autoUpdater } from 'electron-updater';
import log from 'electron-log';
import MenuBuilder from './menu';
import { resolveHtmlPath } from './util';
import { Channels, FileType } from './preload';
import * as fs from 'fs';
import path from 'path';
import { JsonOcelExporter } from '../ocel/json_ocel_exporter';

class AppUpdater {
  constructor() {
    log.transports.file.level = 'info';
    autoUpdater.logger = log;
    autoUpdater.checkForUpdatesAndNotify();
  }
}

let mainWindow: BrowserWindow | null = null;

ipcMain.on('ipc-example', async (event, arg) => {
  const msgTemplate = (pingPong: string) => `IPC test: ${pingPong}`;
  console.log(msgTemplate(arg));
  event.reply('ipc-example', msgTemplate('pong'));
});

export type ModelFiles = {
  ocDot?: string;
  ocDotFilePath?: string;
  simConfig?: string;
  simConfigFilePath?: string;
};

export type SavedFile = {
  contents?: string;
  fileName?: string;
  extension: string;
  fileType: string;
  defaultDirPath?: string;
};

export const mainService = {
  handleOpenRequest: (args: unknown[]) => {
    if (!(args && args[0])) {
      return;
    }
    let fileType = args[0] as FileType;
    console.log('received request to open %s', fileType);

    switch (fileType) {
      case 'ocdot':
        getOcDotFileFromUser(mainWindow!);
        break;
      case 'simconfig':
        getSimConfigFileFromUser(mainWindow!);
        break;
    }
  },
  handleSaveAllShortcutRequest: (args: unknown[]) => {
    // request the front about their file
    let window = mainWindow;
    if (!window) {
      return;
    }
    window.webContents.send('save-all-shortcut' as Channels);
  },
  handleSaveCurrentFileRequest: (args: unknown[]) => {
    let window = mainWindow;
    if (!window) return;
    window.webContents.send('save-the-current-file' as Channels);
  },
  handleSaveCurrentFileResponse: (args: unknown[]) => {
    if (!(args && args[0])) {
      return;
    }
    let savedFile = args[0] as SavedFile;
    saveFileOrOpenSaveDialog(savedFile);
  },
  handleSaveResponse: (args: unknown[]) => {
    if (!(args && args[0])) {
      return;
    }
    let modelFiles = args[0] as ModelFiles;
    console.log(
      'saving model files: %s and %s',
      modelFiles.ocDotFilePath,
      modelFiles.simConfigFilePath
    );

    if (modelFiles.ocDotFilePath && modelFiles.ocDot) {
      saveFile(modelFiles.ocDotFilePath, modelFiles.ocDot);
    }
    if (modelFiles.simConfig && modelFiles.simConfigFilePath) {
      saveFile(modelFiles.simConfigFilePath, modelFiles.simConfig);
    }
  },
};

const saveFileOrOpenSaveDialog = async (savedFile: SavedFile) => {
  if (savedFile.fileName && fs.existsSync(savedFile.fileName)) {
    saveFile(savedFile.fileName, savedFile.contents ? savedFile.contents : '');
    return;
  }

  let defaultDesktopPath = savedFile.defaultDirPath
    ? savedFile.defaultDirPath
    : app.getPath('desktop');

  // Displays save dialog box
  let { filePath } = await dialog.showSaveDialog({
    title: 'Save file',
    defaultPath: defaultDesktopPath,
    // Filters are used to limit the filetypes that can be chosen
    filters: [{ name: savedFile.fileType, extensions: [savedFile.extension] }],
  });

  if (filePath) {
    saveFile(filePath, savedFile.contents ? savedFile.contents : '');
  } else {
    console.log("couldn't save file");
  }
};

export type SuccessfullySavedFile = {
  fileName: string;
  contents: string;
  extension: string,
};

const saveFile = (filePath: string, fileContents: string) => {
  console.log('saving for %s', filePath);
  let dirname = path.dirname(filePath);
  if (!fs.existsSync(dirname)) {
    return;
  }

  fs.writeFile(filePath, fileContents, (error) => {
    if (error) {
      console.log(`An error occurred: ${error.message}`);
    } else {

      let extension = path.extname(filePath)
      extension = extension.substring(1, extension.length)

      mainWindow!.webContents.send(
        'saved-current-file' as Channels,
        { fileName: filePath, contents: fileContents, extension } as SuccessfullySavedFile
      );

      console.log('File saved successfully!');
    }
  });
};

ipcMain.on('open', (event, args: unknown[]) => {
  mainService.handleOpenRequest(args);
});

ipcMain.on('save-all-shortcut' as Channels, (event, args: unknown[]) => {
  mainService.handleSaveResponse(args);
});

ipcMain.on('save-the-current-file' as Channels, (event, args: unknown[]) => {
  mainService.handleSaveCurrentFileResponse(args);
});

ipcMain.on('copy' as Channels, (event, args: unknown[]) => {
  if (!(args && args[0])) {
    return 
  }
  clipboard.writeText(args[0] as string);
})

ipcMain.on('transform-ocel' as Channels, (event, args: unknown[]) => {
  if (!(args && args[0])) {
    return 
  }
  
  let ocelObj = args[0]

  let resultString = JsonOcelExporter.apply(ocelObj)

  mainWindow?.webContents?.send('transform-ocel' as Channels, resultString)
})
const openFile = (window: BrowserWindow, file: string, fileType: FileType) => {
  const fileContents = fs.readFileSync(file).toString();
  console.log(fileContents);
  window.webContents.send('file-opened', file, fileContents, fileType);
};

const getSimConfigFileFromUser = async (targetWindow: BrowserWindow) => {
  const files = await dialog.showOpenDialog(targetWindow, {
    properties: ['openFile'],
    filters: [
      {
        name: 'Simulation Configuration',
        extensions: ['yaml'],
      },
    ],
  });

  if (files && files.filePaths && files.filePaths[0]) {
    openFile(targetWindow, files.filePaths[0], 'simconfig');
  }
};

const getOcDotFileFromUser = async (targetWindow: BrowserWindow) => {
  const files = dialog.showOpenDialog(targetWindow, {
    properties: ['openFile'],
    filters: [
      {
        name: 'OcDot files',
        extensions: ['ocdot'],
      },
    ],
  });
  const kek = await files;
  if (kek && kek.filePaths && kek.filePaths[0]) {
    openFile(targetWindow, kek.filePaths[0], 'ocdot');
  }
};

if (process.env.NODE_ENV === 'production') {
  const sourceMapSupport = require('source-map-support');
  sourceMapSupport.install();
}

const isDebug =
  process.env.NODE_ENV === 'development' || process.env.DEBUG_PROD === 'true';

if (isDebug) {
  require('electron-debug')();
}

const installExtensions = async () => {
  const installer = require('electron-devtools-installer');
  const forceDownload = !!process.env.UPGRADE_EXTENSIONS;
  const extensions = ['REACT_DEVELOPER_TOOLS'];

  return installer
    .default(
      extensions.map((name) => installer[name]),
      forceDownload
    )
    .catch(console.log);
};

export function createDevToolWindow(win: Electron.BrowserWindow) {
  // Create a new window for the DevTools
  
  const displays = screen.getAllDisplays();
  let externalDisplay = displays.find((display) => {
    return display.bounds.x !== 0 || display.bounds.y !== 0;
  });

  let devTools : BrowserWindow
  if (externalDisplay) {
    devTools = new BrowserWindow({
      x: externalDisplay.bounds.x + 50,
      y: externalDisplay.bounds.y + 700,
      width: 1800,
      height: 1000,
      title: 'OCGena - DevTools',
  
    });
  } else {
    devTools = new BrowserWindow({
      y: 1080,
      width: 1200,
      height: 1000,
      title: 'OCGena - DevTools',
    });
  }

  win.webContents.setDevToolsWebContents(devTools.webContents);
  win.webContents.openDevTools({ mode: 'detach' });

  // Resize the DevTools window

  devTools.on('ready-to-show',() => {
    devTools.webContents.closeDevTools();
  })
  devTools.webContents.closeDevTools();
}

const createWindow = async () => {
  // if (isDebug) {
  //   await installExtensions();
  // }

  const RESOURCES_PATH = app.isPackaged
    ? path.join(process.resourcesPath, 'assets')
    : path.join(__dirname, '../../assets');

  const getAssetPath = (...paths: string[]): string => {
    return path.join(RESOURCES_PATH, ...paths);
  };

  const displays = screen.getAllDisplays();
  let externalDisplay = displays.find((display) => {
    return display.bounds.x !== 0 || display.bounds.y !== 0;
  });

  if (externalDisplay) {
    mainWindow = new BrowserWindow({
      show: false,
      x: externalDisplay.bounds.x + 50,
      y: externalDisplay.bounds.y + 50,
      width: 1800,
      height: 1500,
      icon: getAssetPath('ocgena.png'),
      title: 'OCGena',
      webPreferences: {
        preload: app.isPackaged
          ? path.join(__dirname, 'preload.js')
          : path.join(__dirname, '../../.erb/dll/preload.js'),
      },
    });
  } else {
    mainWindow = new BrowserWindow({
      show: false,
      width: 1200,
      height: 1080,
      icon: getAssetPath('ocgena.png'),
      title: 'OCGena',
      webPreferences: {
        preload: app.isPackaged
          ? path.join(__dirname, 'preload.js')
          : path.join(__dirname, '../../.erb/dll/preload.js'),
      },
    });
  }

  mainWindow.webContents.closeDevTools();

  mainWindow.loadURL(resolveHtmlPath('index.html'));

  mainWindow.on('ready-to-show', () => {
    if (!mainWindow) {
      throw new Error('"mainWindow" is not defined');
    }
    if (process.env.START_MINIMIZED) {
      mainWindow.minimize();
      mainWindow.webContents.closeDevTools();
    } else {
      mainWindow.show();
      mainWindow.webContents.closeDevTools();
    }
  });

  mainWindow.on('closed', () => {
    mainWindow = null;
  });

  const menuBuilder = new MenuBuilder(mainWindow);
  menuBuilder.buildMenu();

  // Open urls in the user's browser
  mainWindow.webContents.setWindowOpenHandler((edata) => {
    shell.openExternal(edata.url);
    return { action: 'deny' };
  });

  // Remove this if your app does not use auto updates
  // eslint-disable-next-line
  new AppUpdater();
};

/**
 * Add event listeners...
 */

app.on('window-all-closed', () => {
  // Respect the OSX convention of having the application in memory even
  // after all windows have been closed
  if (process.platform !== 'darwin') {
    app.quit();
  }
});

app
  .whenReady()
  .then(() => {
    createWindow();
    app.on('activate', () => {
      // On macOS it's common to re-create a window in the app when the
      // dock icon is clicked and there are no other windows open.
      if (mainWindow === null) createWindow();
    });
  })
  .catch(console.log);
