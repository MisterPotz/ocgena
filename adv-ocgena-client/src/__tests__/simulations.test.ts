import { exampleModel } from 'domain/views/ModelEditor';
import { exampleConfiguration as exampleConfiguration } from 'simconfig/simconfig_yaml';
import * as yaml from 'js-yaml';
import ocgena, { simulation } from 'ocgena';
import { ProjectSingleSimulationExecutor } from 'domain/ProjectSingleSimulationExecutor';
import { defer } from 'rxjs';
import { SimConfigMapper } from 'domain/Project';

describe('simulation tests', () => {
  test('simple simulation', () => {
    let model = exampleModel;
    let config = exampleConfiguration;

    let yamlObj = yaml.load(config);
    let simConfigMapper = new SimConfigMapper();

    let deferred = new Promise((resolve, reject) => {
        let simClient = new ProjectSingleSimulationExecutor(
            (line: string) => {
              console.log(line);
            },
            {
              onExecutionFinish: () => {
                console.log('execution finished');
              },
              onExecutionStart: () => {
                console.log('execution started');
              },
              onExecutionTimeout: () => {
                console.log('execution timeout');
              },
            } as simulation.client.JsSimTaskClientCallback,
            (any: any) => {
              console.log('received ocel from executor');
            }
          );

        simClient.updateModel(model)
        simClient.updateSimulationConfig(simConfigMapper.convertRawSimConfigToSimConfig(config))


    })
    

    
  });
});
