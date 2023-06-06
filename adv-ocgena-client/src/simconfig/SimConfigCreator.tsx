import {
  INITIAL_MARKING,
  INPUT_PLACES,
  LABEL_MAPPING,
  OC_NET_DEFINITION,
  OUTPUT_PLACES,
  PLACE_TYPING,
  TRANSITIONS_CONFIG, TimeRangeType,
  TransitionIntervalsType,
  TransitionsConfig,
  TransitionsConfigType
} from 'simconfig/simconfig_yaml';
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
      this.createTransitionsConfig(obj)
    ].filter(this.notEmpty);

    let simConfig = new simulation.config.SimulationConfig(configs);
    return simConfig;
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
      value = model.OcNetType.valueOf(obj[OC_NET_DEFINITION]);
    } catch (e) {
      return null;
    }

    return new config.OCNetTypeConfig(value);
  }

  private createPlaceTyping(
    obj: SimConfigObj
  ): config.PlaceTypingConfig | null {
    if (!this.checkObjectIsPresent(PLACE_TYPING, obj)) {
      return null;
    }
    return new config.PlaceTypingConfig(obj[PLACE_TYPING]);
  }

  private createLabelMapping(
    obj: SimConfigObj
  ): config.LabelMappingConfig | null {
    if (!this.checkObjectIsPresent(LABEL_MAPPING, obj)) {
      return null;
    }

    return new config.LabelMappingConfig(obj[LABEL_MAPPING]);
  }

  private createInitialMarking(
    obj: SimConfigObj
  ): config.InitialMarkingConfig | null {
    if (!this.checkObjectIsPresent(INITIAL_MARKING, obj)) {
      return null;
    }

    return new config.InitialMarkingConfig(obj[INITIAL_MARKING]);
  }

  private checkObjectIsPresent(
    propertyName: string,
    obj: SimConfigObj
  ): boolean {
    return (
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
  ): config.TransitionIntervals {
    let item = {
      duration: this.convertTimeRange(transitionIntervals.duration),
      minOccurrenceInterval: this.convertTimeRange(
        transitionIntervals.minOccurrenceInterval
      ),
    } as config.JsTransitionIntervals;

    return config.toTransitionIntervals(item);
  }

  private transitionConfigChecker = (
    input: any
  ): input is TransitionsConfigType => TransitionsConfig.is(input);

  private createTransitionsConfig(
    obj: SimConfigObj
  ): config.TransitionsConfig | null {
    if (!this.checkObjectIsPresent(TRANSITIONS_CONFIG, obj)) {
      return null;
    }
    let transitionsConfig = obj[TRANSITIONS_CONFIG];

    if (this.transitionConfigChecker(transitionsConfig)) {
      let defaultTransition: config.TransitionIntervals | undefined = undefined;

      if (transitionsConfig.defaultTransitionInterval) {
        defaultTransition = this.convertTransitionInterval(
          transitionsConfig.defaultTransitionInterval
        );
      }

      let transitionsToIntervals: {
        [key: string]: config.TransitionIntervals;
      } = {};

      for (let key in transitionsConfig.transitionsToIntervals) {
        let value = this.convertTransitionInterval(
          transitionsConfig.transitionsToIntervals[key]
        );
        transitionsToIntervals[key] = value;
      }

      return new config.TransitionsConfig(
        defaultTransition,
        transitionsToIntervals
      );
    }

    return null;
  }
}
