type EventType = "getStoreValue" | "getStoreAll" | "setStoreValue";

export function event(eventType: EventType) {
  return eventType as string;
}
