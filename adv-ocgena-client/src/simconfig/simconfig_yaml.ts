import Ajv from 'ajv';
import { Uri } from 'monaco-editor';
import { SchemasSettings, setDiagnosticsOptions } from 'monaco-yaml';
import { trimIndent } from 'ocdot-parser/lib/exts';
import * as t from 'io-ts';
import { config } from 'ocgena';

export const modelUri = Uri.parse('a://sim-config.yaml');
export const setupTemplate = trimIndent(`
    labelMapping:
        p1: sd
    inputPlaces: p1 
    outputPlaces: p3
    initialMarking:
        p1: 3
    ocNetDefinition: aalst
    placeTyping:
        ot1: p1
    transitionInterval:
        defaultTransitionInterval:
            duration: [1, 1]
            minOccurrenceInterval: [2,2]
        transitionsToIntervals:
          t1:
              duration: [1, 1]
              minOccurrenceInterval: [1,2]
`);

export const simconfigSchemaId = 'http://myserver/sim-config.json';
export const timeIntervalSchemaId = 'http://myserver/transition-interval.json';

export const INPUT_PLACES = 'inputPlaces';
export const OUTPUT_PLACES = 'outputPlaces';
export const OC_NET_DEFINITION = 'ocNetDefinition';
export const PLACE_TYPING = 'placeTyping';
export const LABEL_MAPPING = 'labelMapping';
export const INITIAL_MARKING = 'initialMarking';
export const TRANSITIONS_CONFIG = 'transitionsConfig';

// export type TimeRange = {
//   start : number,
//   end : number
// }

// export type TransitionInterval = {
//   duration: TimeRange,
//   minOccurrenceInterval : TimeRange
// }

// export type TransitionIntervals = {
//   defaultTransitionInterval?: TransitionInterval,
//   transitionsToIntervals: {
//     [transitionId : string] : TransitionInterval
//   }
// }

const TimeRange = t.tuple([t.number, t.number]);

export type TimeRangeType = t.TypeOf<typeof TimeRange>;

export class TimeRangeClass {
  constructor(private timeRange: [number, number]) {}

  get start(): number {
    return this.timeRange[0];
  }

  get end(): number {
    return this.timeRange[1];
  }
}

const TransitionIntervals = t.type({
  duration: TimeRange,
  minOccurrenceInterval: TimeRange,
});

export type TransitionIntervalsType = t.TypeOf<typeof TransitionIntervals>;

export const TransitionsConfig = t.type({
  defaultTransitionInterval: t.union([TransitionIntervals, t.undefined]),
  transitionsToIntervals: t.record(t.string, TransitionIntervals),
});
export type TransitionsConfigType = t.TypeOf<typeof TransitionsConfig>;

export const simconfigSchema = {
  // Id of the first schema
  uri: simconfigSchemaId,
  // Associate with our model
  fileMatch: [String(modelUri)],
  schema: {
    type: 'object',
    properties: {
      [INPUT_PLACES]: {
        type: 'string',
        title: 'input places',
        description: 'List all input places separated by space',
      },
      [OUTPUT_PLACES]: {
        type: 'string',
        title: 'output places',
        description: 'List all output places separated by space',
      },
      [OC_NET_DEFINITION]: {
        enum: ['aalst', 'lomazova'],
        title: 'Definition of OC-net',
        description:
          'Chosen definition of OC-net may affect calculations and consistency checks',
      },
      [PLACE_TYPING]: {
        type: 'object',
        description:
          'Mapping from object type to places, list all places with spaces',
        additionalProperties: {
          type: 'string',
        },
      },
      [LABEL_MAPPING]: {
        type: 'object',
        description: 'Mapping from place id to activity label',
        additionalProperties: {
          type: 'string',
        },
      },
      [INITIAL_MARKING]: {
        type: 'object',
        description: 'Places to their respective initial marking',
        additionalProperties: {
          type: 'integer',
        },
      },
      [TRANSITIONS_CONFIG]: {
        type: 'object',
        properties: {
          defaultTransitionInterval: {
            $ref: timeIntervalSchemaId,
          },
          transitionsToIntervals: {
            type: 'object',
            additionalProperties: {
              $ref: timeIntervalSchemaId,
            },
          },
        },
      },
      // p1: {
      //     enum: ['v1', 'v2']
      // },
      // p2: {
      //     // Reference the second schema
      //     $ref: 'http://myserver/bar-schema.json'
      // }
    },
  },
} as SchemasSettings;

export const timeRangeSchemaId = 'http://myserver/time-range.json';
export const timeRangeSchema = {
  // Id of the first schema
  uri: timeRangeSchemaId,
  fileMatch: [],
  schema: {
    type: 'array',
    items: {
      type: 'integer',
    },
    minItems: 2,
    maxItems: 2,
    description: 'Start and end value of the possible range',
  },
} as SchemasSettings;

export const timeIntervalSchema = {
  // Id of the first schema
  uri: timeIntervalSchemaId,
  fileMatch: [],
  schema: {
    type: 'object',
    properties: {
      duration: {
        $ref: timeRangeSchemaId,
      },
      minOccurrenceInterval: {
        $ref: timeRangeSchemaId,
      },
    },
    description:
      'Define possible ranges for duration and minOccurrenceInterval',
  },
} as SchemasSettings;

export function createAjv(): Ajv {
  let newAjv = new Ajv({ strict: false });
  newAjv.addSchema(simconfigSchema, simconfigSchemaId);
  newAjv.addSchema(timeRangeSchema, timeRangeSchemaId);
  newAjv.addSchema(timeIntervalSchema, timeIntervalSchemaId);
  return newAjv;
}

export function setupYamlLanguageServer() {
  setDiagnosticsOptions({
    enableSchemaRequest: true,
    hover: true,
    completion: true,
    validate: true,
    format: true,
    schemas: [
        simconfigSchema,
        timeRangeSchema,
        timeIntervalSchema,
    ],
  });
}
