"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.prependIndent = prependIndent;
exports.trimIndent = trimIndent;
exports.isEmptyOrBlank = isEmptyOrBlank;
exports.isEmpty = isEmpty;
exports.isNotEmpty = isNotEmpty;
exports.coerceAtLeast = coerceAtLeast;
exports.coerceAtMost = coerceAtMost;
exports.removeEmptyLineSpaces = removeEmptyLineSpaces;
function prependIndent(value, indent = "  ") {
    return value
        .split("\n")
        .map(line => indent + line)
        .join("\n");
}
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
function isEmptyOrBlank(str) {
    if (isEmpty(str))
        return true;
    const character = /\S/;
    if (str.match(character)) {
        return false;
    }
    return true;
}
function isEmpty(str) {
    return str.length == 0;
}
function isNotEmpty(str) {
    return !isEmpty(str);
}
function coerceAtLeast(value, minimumValue) {
    return Math.max(value, minimumValue);
}
function coerceAtMost(value, maximumValue) {
    return Math.min(value, maximumValue);
}
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
//# sourceMappingURL=exts.js.map