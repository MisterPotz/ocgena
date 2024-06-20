import { TypedActionCreator } from "@reduxjs/toolkit/dist/mapBuilders";

export const loadType = "load";
export const loadTypeFinished = "loadFinished";

function createAction(type: string): TypedActionCreator<string> {
  const actionCreator = (...args: any[]) => {
    console.log(args)
    return ({
      type: type,
      payload: args.length === 1 ? args[0] : args,
    });
  };
  actionCreator.type = type;
  return actionCreator;
}

export const loadV2 = createAction(loadType);
export const loadFulfilled = createAction(loadTypeFinished);
