import {
  INPUT_PLACES,
  OUTPUT_PLACES,
  OC_NET_DEFINITION,
  PLACE_TYPING,
  LABEL_MAPPING,
  INITIAL_MARKING,
  TRANSITIONS_CONFIG,
  RANDOM,
} from './LABEL_MAPPING';

export {
  LABEL_MAPPING,
  INPUT_PLACES,
  OUTPUT_PLACES,
  INITIAL_MARKING,
  OC_NET_DEFINITION,
  PLACE_TYPING,
  TRANSITIONS_CONFIG,
};

export const simconfigSchemaId = 'http://myserver/sim-config.json';
export const timeIntervalSchemaId = 'http://myserver/transition-interval.json';

export const simconfigSchema = {
  // Id of the first schema
  uri: simconfigSchemaId,
  $id: simconfigSchemaId,
  // Associate with our model
  // fileMatch: [String(modelUri)],
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
      [RANDOM]: {
        type: 'object',
        properties: {
          turnOn: {
            type: 'boolean',
          },
          seed: {
            type: 'integer'
          }
        }
      }
    },
  },
};

export const integerRangeSchemaId = 'http://myserver/time-range.json';
export const integerRangeSchema = {
  // Id of the first schema
  uri: integerRangeSchemaId,
  $id: integerRangeSchemaId,
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
};

export const timeIntervalSchema = {
  // Id of the first schema
  uri: timeIntervalSchemaId,
  $id: timeIntervalSchemaId,
  fileMatch: [],
  schema: {
    type: 'object',
    properties: {
      duration: {
        $ref: integerRangeSchemaId,
      },
      minOccurrenceInterval: {
        $ref: integerRangeSchemaId,
      },
    },
    description:
      'Define possible ranges for duration and minOccurrenceInterval',
  },
};
