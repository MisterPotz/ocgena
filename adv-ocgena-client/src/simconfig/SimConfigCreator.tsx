import {
  GENERATION,
  INITIAL_MARKING,
  INPUT_PLACES,
  LABEL_MAPPING,
  OC_NET_DEFINITION,
  OUTPUT_PLACES,
  PLACE_TYPING,
  RANDOM,
  TRANSITIONS_CONFIG
} from './LABEL_MAPPING';
import {
  GenerationConfig,
  GenerationConfigType,
  TimeRangeType,
  TransitionIntervalsType,
  TransitionsConfig,
  TransitionsConfigType
} from './TimeRange';
import { simulation, config, model } from 'ocgena';

export type SimConfigObj = any;

export class SimConfigCreator {

  private notEmpty<TValue>(value: TValue | null | undefined): value is TValue {
    return value != null;
  }

  createFromObj(obj: SimConfigObj): simulation.config.SimulationConfig {
    let configs = [
      this.createInputPlacesConfig(obj),
      this.createOutputPlacesConfig(obj),
      this.createOcNetDefinitionConfig(obj),
      this.createPlaceTyping(obj),
      this.createLabelMapping(obj),
      this.createInitialMarking(obj),
      this.createTransitionsConfig(obj),
      this.createRandomConfig(obj),
      this.createGenerationConfig(obj)
    ].filter(this.notEmpty);

    let simConfig = new simulation.config.SimulationConfig(configs);
    return simConfig;
  }

  createGenerationConfig(obj: any) { 
    if (!this.checkObjectIsPresent(GENERATION, obj)) {
      return null 
    }

    try {
      let generationConfig = obj[GENERATION]
      if (this.generationConfigChecker(generationConfig)) {
        let defaultTimeRange;

        if (generationConfig.default) { 
          defaultTimeRange = this.convertTimeRange(generationConfig.default)
        }

        return config.toGenerationConfig(
          generationConfig.generationTargets || {},
          defaultTimeRange
        )
      }
    } catch(e) {
      return null
    }
  }
  
  createRandomConfig(obj: any) {
    if (!this.checkObjectIsPresent(RANDOM, obj)) {
        return null
    }

    try {
      let turnOn = obj[RANDOM]['turnOn']
      let seed = obj[RANDOM]['seed'] as number

      return new config.RandomConfig(turnOn, seed)
    } catch(e) {
      return new config.RandomConfig()
    }
  }

  private createInputPlacesConfig(
    obj: SimConfigObj
  ): config.InputPlacesConfig | null {
    if (!this.checkStringIsPresent(INPUT_PLACES, obj)) {
      return null;
    }

    return new config.InputPlacesConfig(obj[INPUT_PLACES] as string);
  }

  private createOutputPlacesConfig(
    obj: SimConfigObj
  ): config.OutputPlacesConfig | null {
    if (!this.checkStringIsPresent(OUTPUT_PLACES, obj)) {
      return null;
    }

    return new config.OutputPlacesConfig(obj[OUTPUT_PLACES] as string);
  }

  private createOcNetDefinitionConfig(
    obj: SimConfigObj
  ): config.OCNetTypeConfig | null {
    if (!this.checkStringIsPresent(OC_NET_DEFINITION, obj)) {
      return null;
    }
    let value: model.OcNetType;

    try {
      value = model.OcNetType.valueOf((obj[OC_NET_DEFINITION] as string).toUpperCase());
    } catch (e) {
      return null;
    }

    return new config.OCNetTypeConfig(value);
  }

  private createPlaceTyping(
    obj: SimConfigObj
  ): simulation.config.Config | null {
    if (!this.checkObjectIsPresent(PLACE_TYPING, obj)) {
      return null;
    }
    return config.toPlaceTypingConfig(obj[PLACE_TYPING]);
  }

  private createLabelMapping(
    obj: SimConfigObj
  ): simulation.config.Config | null {
    if (!this.checkObjectIsPresent(LABEL_MAPPING, obj)) {
      return null;
    }

    return config.toLabelMappingConfig(obj[LABEL_MAPPING]);
  }

  private createInitialMarking(
    obj: SimConfigObj
  ): simulation.config.Config | null {
    if (!this.checkObjectIsPresent(INITIAL_MARKING, obj)) {
      return null;
    }
    let initialMarking 
    try {
      initialMarking = config.toInitialMarkingConfig(obj[INITIAL_MARKING])
    } catch(e) { 
      return null
    }
    return initialMarking
  }

  private checkObjectIsPresent(
    propertyName: string,
    obj: SimConfigObj
  ): boolean {
    return (
      obj &&
      propertyName in obj &&
      typeof obj[propertyName] === 'object' &&
      obj[propertyName] != null
    );
  }

  private checkStringIsPresent(
    propertyName: string,
    obj: SimConfigObj
  ): boolean {
    return (
      obj &&
      propertyName in obj &&
      typeof obj[propertyName] === 'string' &&
      obj[propertyName] != null
    );
  }

  private convertTimeRange(timeRange: TimeRangeType): config.TimeRangeClass {
    return new config.TimeRangeClass(timeRange);
  }

  private convertTransitionInterval(
    transitionIntervals: TransitionIntervalsType
  ): config.TransitionIntervals | null {
    let item = {
      duration: this.convertTimeRange(transitionIntervals.duration),
      minOccurrenceInterval: this.convertTimeRange(
        transitionIntervals.minOccurrenceInterval
      ),
    } as config.JsTransitionIntervals;

    let otherc;

    try {
      otherc = config.toTransitionIntervals(item);
    } catch(e) { 
      return null
    }
    return otherc
  }

  private generationConfigChecker = (
    input: any
  ): input is GenerationConfigType => GenerationConfig.is(input);


  private transitionConfigChecker = (
    input: any
  ): input is TransitionsConfigType => TransitionsConfig.is(input);

  private createTransitionsConfig(
    obj: SimConfigObj
  ): simulation.config.Config | null {
    if (!this.checkObjectIsPresent(TRANSITIONS_CONFIG, obj)) {
      return null;
    }
    let transitionsConfig = obj[TRANSITIONS_CONFIG];

    if (this.transitionConfigChecker(transitionsConfig)) {
      let defaultTransition: config.TransitionIntervals | undefined = undefined;

      if (transitionsConfig.defaultTransitionInterval) {
        let ihatetypescript = this.convertTransitionInterval(
          transitionsConfig.defaultTransitionInterval
        );
        if (ihatetypescript) { 
          defaultTransition = ihatetypescript
        }
      }

      let transitionsToIntervals: {
        [key: string]: config.TransitionIntervals;
      } = {};

      if (transitionsConfig.transitionsToIntervals) {
        for (let key in transitionsConfig.transitionsToIntervals) {

          let value = this.convertTransitionInterval(
            transitionsConfig.transitionsToIntervals[key]
          );
          if (value) { 
            transitionsToIntervals[key] = value;
          }
        }  
      }
      
      return config.toTransitionsConfig(
        defaultTransition,
        transitionsToIntervals
      );
    }

    return null;
  }
}
