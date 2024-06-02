import { current } from "@reduxjs/toolkit";
import { Sequelize, DataTypes, Model } from "sequelize";
import { SimulationRun, RunsToPaths, Project as IProject } from "../../shared/domain.ts";

class DBProject extends Model {
  declare id: string;
  declare userName: string | null;
  declare simulationRuns: SimulationRun[];
  declare simulationConfigPaths: string[];
  declare modelPaths: string[];
  declare runToSimulationDB: RunsToPaths;
  declare runToOcel: RunsToPaths;
  declare current: boolean;

  static initEntity(sequelize: Sequelize) {
    return DBProject.init(
      {
        id: {
          type: DataTypes.INTEGER,
          primaryKey: true,
          autoIncrement: true,
          allowNull: false,
        },
        userName: {
          type: DataTypes.STRING,
          allowNull: true,
        },
        simulationRuns: {
          type: DataTypes.TEXT,
          allowNull: false,
          get() {
            return JSON.parse(
              this.getDataValue("simulationRuns")
            ) as SimulationRun[];
          },
          set(val: SimulationRun[]) {
            this.setDataValue("simulationRuns", JSON.stringify(val));
          },
          defaultValue: () => {
            return [] as SimulationRun[];
          },
        },
        simulationConfigPaths: {
          type: DataTypes.TEXT,
          allowNull: false,
          get() {
            return JSON.parse(
              this.getDataValue("simulationConfigPaths")
            ) as string[];
          },
          set(val: string[]) {
            this.setDataValue("simulationConfigPaths", JSON.stringify(val));
          },
          defaultValue: () => {
            return [] as string[];
          },
        },
        modelPaths: {
          type: DataTypes.TEXT,
          allowNull: false,
          get() {
            return JSON.parse(this.getDataValue("modelPaths")) as string[];
          },
          set(val: string[]) {
            this.setDataValue("modelPaths", JSON.stringify(val));
          },
          defaultValue: () => {
            return [] as string[];
          },
        },
        runToSimulationDB: {
          type: DataTypes.TEXT,
          allowNull: false,
          get() {
            return JSON.parse(
              this.getDataValue("runToSimulationDB")
            ) as RunsToPaths;
          },
          set(val: RunsToPaths) {
            this.setDataValue("runToSimulationDB", JSON.stringify(val));
          },
          defaultValue: () => {
            return {} as RunsToPaths;
          },
        },
        runToOcel: {
          type: DataTypes.TEXT,
          allowNull: false,
          get() {
            return JSON.parse(this.getDataValue("runToOcel")) as RunsToPaths;
          },
          set(val: RunsToPaths) {
            this.setDataValue("runToOcel", JSON.stringify(val));
          },
          defaultValue: () => {
            return {} as RunsToPaths;
          },
        },
        current: {
          type: DataTypes.BOOLEAN,
          allowNull: false,
          defaultValue: false,
        },
      },
      {
        sequelize,
      }
    );
  }

  static makeNew(project: IProject): Promise<DBProject> {
    return DBProject.create({
      ...project,
    });
  }
}

class SequelizeHolder {
  private static instance: SequelizeHolder;

  public sequelize: Sequelize;
  public dbProject;

  // Private constructor to prevent direct instantiation
  private constructor(dbPath: string) {
    const sequelize = new Sequelize({
      dialect: "sqlite",
      storage: dbPath,
    });
    this.sequelize = sequelize;
    this.dbProject = DBProject.initEntity(sequelize);
  }

  async init() {
    this.sequelize.sync();
  }

  // Public method to get the single instance of the class
  public static async initInstance(dbPath: string) {
    if (!SequelizeHolder.instance) {
      SequelizeHolder.instance = new SequelizeHolder(dbPath);
      SequelizeHolder.instance.init();
    }
    return SequelizeHolder.instance;
  }

  public static requireInstance() {
    return SequelizeHolder.instance!;
  }

  // Example method
  public showMessage(): void {
    console.log("Singleton instance method called!");
  }
}

async function setupAppSqlite(dbPath: string) {
  return await SequelizeHolder.initInstance(dbPath);
}
