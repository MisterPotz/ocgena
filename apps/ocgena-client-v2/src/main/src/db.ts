import Store from "electron-store";

export class StoreHolder {
  private static instance: Store<{ projects: undefined[]; }>;

  static getInstance() {
    if (StoreHolder.instance == null) {
      StoreHolder.instance = new Store({
        name: "appconfig",
        defaults: {
          projects: [],
        },
      });
    }
    return this.instance;
  }
}
