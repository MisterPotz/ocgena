import Ajv from 'ajv';
import { simconfigSchema, integerRangeSchema, integerRangeSchemaId, timeIntervalSchema, simconfigSchemaId, timeIntervalSchemaId } from './simconfigSchema';

console.log("simconfigSchemaId: %s", simconfigSchemaId)

export const createAjv : () => Ajv = () => {
  let newAjv = new Ajv({ strict: false });
  newAjv.addSchema(simconfigSchema, simconfigSchemaId);
  newAjv.addSchema(integerRangeSchema, integerRangeSchemaId);
  newAjv.addSchema(timeIntervalSchema, timeIntervalSchemaId);
  return newAjv;
}
