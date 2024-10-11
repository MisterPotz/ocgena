type EventType = "getStoreValue" | "getStoreAll" | "setStoreValue";

export function event(eventType: EventType) {
  return eventType as string;
}

type PreloadType = 'api' | "toMain" | "fromMain"

export function com(preloadType: PreloadType) : string {
  return preloadType as string
}
