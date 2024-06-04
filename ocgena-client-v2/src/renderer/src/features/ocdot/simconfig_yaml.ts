import { SchemasSettings, configureMonacoYaml } from "monaco-yaml";
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
} from "./simconfigSchema";
import * as monaco from "monaco-editor/esm/vs/editor/editor.api";
export * from "monaco-editor";
import { Uri } from "monaco-editor";
import { trimIndent } from 'ocdot-parser/lib/exts';

export const modelUri = Uri.parse("a://sim-config.yaml");

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

  configureMonacoYaml(monaco, {
    schemas: [
      updatedScheme,
      integerRangeSchema as SchemasSettings,
      timeIntervalSchema as SchemasSettings,
    ],
    completion: true,
    format: true,
    validate: true,
    hover: true,
    enableSchemaRequest: true,
  });
}
