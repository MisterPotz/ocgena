export {}

declare global {
  interface Window {
    api: {
      request: (channel: string, ...data: any[]) => Promise<any>;
    };
  }
}
