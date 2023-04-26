"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.removeEmptyLineSpaces = exports.coerceAtMost = exports.coerceAtLeast = exports.isNotEmpty = exports.isEmpty = exports.isEmptyOrBlank = exports.trimIndent = exports.prependIndent = void 0;
function prependIndent(value, indent = "  ") {
    return value
        .split("\n")
        .map(line => indent + line)
        .join("\n");
}
exports.prependIndent = prependIndent;
function trimIndent(str) {
    const lines = str.split('\n');
    let minIndent = Infinity;
    var counter = 0;
    const firstlineBlank = isEmptyOrBlank(lines[0]);
    const lastLineBlank = isEmptyOrBlank(lines[lines.length - 1]);
    const length = str.length;
    for (const line of lines) {
        const indent = line.search(/\S|$/);
        if (firstlineBlank && counter === 0) {
        }
        else if (lastLineBlank && counter === length - 1) {
        }
        else if (indent !== -1
            && indent < minIndent
            && !isEmptyOrBlank(line)) {
            minIndent = indent;
        }
        counter++;
    }
    return lines.slice(firstlineBlank
        ? coerceAtMost(1, length - 1)
        : 0, lastLineBlank
        ? coerceAtLeast(length, 0)
        : length - 1)
        .map(line => line.slice(minIndent))
        .join('\n');
}
exports.trimIndent = trimIndent;
function isEmptyOrBlank(str) {
    if (isEmpty(str))
        return true;
    const character = /\S/;
    if (str.match(character)) {
        return false;
    }
    return true;
}
exports.isEmptyOrBlank = isEmptyOrBlank;
function isEmpty(str) {
    return str.length == 0;
}
exports.isEmpty = isEmpty;
function isNotEmpty(str) {
    return !isEmpty(str);
}
exports.isNotEmpty = isNotEmpty;
function coerceAtLeast(value, minimumValue) {
    return Math.max(value, minimumValue);
}
exports.coerceAtLeast = coerceAtLeast;
function coerceAtMost(value, maximumValue) {
    return Math.min(value, maximumValue);
}
exports.coerceAtMost = coerceAtMost;
function removeEmptyLineSpaces(input) {
    const lines = input.split('\n');
    for (let i = 0; i < lines.length; i++) {
        if (lines[i].trim() === '') {
            lines[i] = '';
        }
        else {
            lines[i] = lines[i].replace(/\t/g, '').replace(/\s/g, '');
        }
    }
    return lines.join('\n');
}
exports.removeEmptyLineSpaces = removeEmptyLineSpaces;
//# sourceMappingURL=exts.js.map