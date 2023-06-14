"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.isIpcMessage = void 0;
function isIpcMessage(message) {
    return 'type' in message;
}
exports.isIpcMessage = isIpcMessage;
//# sourceMappingURL=shared.js.map