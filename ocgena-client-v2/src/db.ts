import Store from "electron-store";

export class StoreHolder {
  private static instance: StoreHolder;

  public store;

  constructor() {
    this.store = new Store({
      name: "appconfig",
      defaults: {
        projects: [],
      },
    });
  }

  static getInstance() {
    if (StoreHolder.instance == null) {
      this.instance = new StoreHolder();
    }
    return this.instance;
  }
}
