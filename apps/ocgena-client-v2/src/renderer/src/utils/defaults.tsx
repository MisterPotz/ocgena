export const DEFAULT_PROJECT_NAME = "New project"

export function makeDefaultName(count: number) : string {
    return `${DEFAULT_PROJECT_NAME} ${count}`
}