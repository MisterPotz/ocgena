import { app, BrowserWindow, ipcMain, session } from "electron";
import path from "path";
import { StoreHolder } from "./db.js";
import {
  installExtension,
  REACT_DEVELOPER_TOOLS,
  REDUX_DEVTOOLS,
} from "@tomjs/electron-devtools-installer";

async function checkStartup() {
  const electronSquirrelStartup = await import("electron-squirrel-startup");
  if (electronSquirrelStartup.default) {
    app.quit();
  }
}
checkStartup();

const isDev = process.env.NODE_ENV === "development";

const createWindow = () => {
  const preloadPath = path.resolve(app.getAppPath(), ".vite/build/preload.js");

  // Create the browser window.
  const mainWindow = new BrowserWindow({
    width: 1400,
    height: 800,
    webPreferences: {
      preload: preloadPath,
    },
    show: !isDev,
  });

  // and load the index.html of the app.
  if (MAIN_WINDOW_VITE_DEV_SERVER_URL) {
    mainWindow.loadURL(MAIN_WINDOW_VITE_DEV_SERVER_URL);
  } else {
    mainWindow.loadFile(
      path.join(`.vite/renderer/${MAIN_WINDOW_VITE_NAME}/index.html`)
    );
  }

  if (isDev) {
    console.log("show window inactive", isDev);
    mainWindow.showInactive();
    mainWindow.blur(); // Blur the window to ensure it does not grab focus
  }

  mainWindow.webContents.once("dom-ready", async () => {
    StoreHolder.getInstance().set("launched", true);
    console.log("setting store to true");

    // dbStore.clear()
    // dbStore.set("k", { kek: "lol arbidol" });
    if (isDev) {
      await installExtension([REACT_DEVELOPER_TOOLS, REDUX_DEVTOOLS])
        .then((name) => console.log(`Added Extension ${name}`))
        .catch((err) => console.log(`An error occurred `, err));

      isDev &&
        setTimeout(() => {
          mainWindow.reload();
        }, 500);
      // Open the DevTools.
      mainWindow.webContents.openDevTools();
    }
  });
};

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.on("ready", async () => {
  createWindow();
});

// Quit when all windows are closed, except on macOS. There, it's common
// for applications and their menu bar to stay active until the user quits
// explicitly with Cmd + Q.
app.on("window-all-closed", () => {
  if (process.platform !== "darwin") {
    app.quit();
  }
});

app.on("activate", () => {
  // On OS X it's common to re-create a window in the app when the
  // dock icon is clicked and there are no other windows open.
  if (BrowserWindow.getAllWindows().length === 0) {
    createWindow();
  }
});

// In this file you can include the rest of your app's specific main process
// code. You can also put them in separate files and import them here.

ipcMain.handle("getStoreValue", (event, key) => {
  return StoreHolder.getInstance().get(key);
});

ipcMain.handle("getStoreAll", (event, key) => {
  return StoreHolder.getInstance().store.projects
});

ipcMain.handle('setStoreValue', (event, key, value) => {
  StoreHolder.getInstance().set(key, value)
})
