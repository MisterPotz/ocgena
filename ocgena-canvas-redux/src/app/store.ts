import type { Action, ThunkAction } from "@reduxjs/toolkit"
import { combineSlices, configureStore } from "@reduxjs/toolkit"
import { setupListeners } from "@reduxjs/toolkit/query"
import { counterSlice } from "../features/counter/counterSlice"
import { quotesApiSlice } from "../features/quotes/quotesApiSlice"
import {
    editorActionFilter,
    editorHandleDragEpic,
    editorSlice,
} from "../features/editor/editorSlice"
import { combineEpics, createEpicMiddleware } from "redux-observable"
import { createFilteringMiddleware } from "../utils/redux_utils"
import { editorV2Slice } from "../features/editorv2/editorv2Slice"

const rootEpic = combineEpics(editorHandleDragEpic)
const epicMiddleware = createEpicMiddleware<Action, Action, void, any>()
const filteringMiddleware = createFilteringMiddleware(editorActionFilter)

// `combineSlices` automatically combines the reducers using
// their `reducerPath`s, therefore we no longer need to call `combineReducers`.
const rootReducer = combineSlices(/* counterSlice, quotesApiSlice,  */ editorSlice, editorV2Slice)
// Infer the `RootState` type from the root reducer
export type RootState = ReturnType<typeof rootReducer>

// The store setup is wrapped in `makeStore` to allow reuse
// when setting up tests that need the same store config
export const makeStore = (preloadedState?: Partial<RootState>) => {
    const store = configureStore({
        reducer: rootReducer,
        // Adding the api middleware enables caching, invalidation, polling,
        // and other useful features of `rtk-query`.
        middleware: getDefaultMiddleware => {
            return getDefaultMiddleware().concat(epicMiddleware).concat(filteringMiddleware)
            // .concat(quotesApiSlice.middleware)
        },
        preloadedState,
    })
    // configure listeners using the provided defaults
    // optional, but required for `refetchOnFocus`/`refetchOnReconnect` behaviors
    setupListeners(store.dispatch)

    epicMiddleware.run(rootEpic)
    return store
}

export const store = makeStore()

store.subscribe(() => {
    console.log(`State after dispatch ${JSON.stringify(store.getState())}`)
})

// Infer the type of `store`
export type AppStore = typeof store
// Infer the `AppDispatch` type from the store itself
export type AppDispatch = AppStore["dispatch"]
export type AppThunk<ThunkReturnType = void> = ThunkAction<
    ThunkReturnType,
    RootState,
    unknown,
    Action
>
