import { SchemasSettings, setDiagnosticsOptions } from 'monaco-yaml';
import { trimIndent } from 'ocdot-parser/lib/exts';
import {
  simconfigSchema,
  integerRangeSchema,
  timeIntervalSchema,
  INPUT_PLACES,
  OUTPUT_PLACES,
  OC_NET_DEFINITION,
  PLACE_TYPING,
  LABEL_MAPPING,
  INITIAL_MARKING,
  TRANSITIONS_CONFIG,
} from './simconfigSchema';

import { Uri } from 'monaco-editor';

export const modelUri = Uri.parse('a://sim-config.yaml');

export const exampleConfiguration = trimIndent(`
    ${LABEL_MAPPING}:
        p1: sd
    ${INPUT_PLACES}: p1 
    ${OUTPUT_PLACES}: p3
    ${INITIAL_MARKING}:
        p1: 3
    ${OC_NET_DEFINITION}: aalst
    ${PLACE_TYPING}: {}
    ${TRANSITIONS_CONFIG}:
        defaultTransitionInterval:
            duration: [1, 1]
            minOccurrenceInterval: [2,2]
        transitionsToIntervals:
            t1:
              duration: [1, 1]
              minOccurrenceInterval: [1,2]
`);

export function setupYamlLanguageServer() {
  let updatedScheme = {
    ...simconfigSchema,
    fileMatch: [String(modelUri)],
  } as SchemasSettings;

  setDiagnosticsOptions({
    enableSchemaRequest: true,
    hover: true,
    completion: true,
    validate: true,
    format: true,
    schemas: [
      updatedScheme,
      integerRangeSchema as SchemasSettings,
      timeIntervalSchema as SchemasSettings,
    ],
  });
}
