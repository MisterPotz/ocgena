export function trimIndent(str: string): string {
  const lines = str.split('\n');
  let minIndent = Infinity;
  var counter = 0;

  const firstlineBlank = isEmptyOrBlank(lines[0]);
  const lastLineBlank = isEmptyOrBlank(lines[lines.length - 1])

  const length = str.length;
  for (const line of lines) {

    const indent = line.search(/\S|$/);
    if (firstlineBlank && counter === 0) { 

    }
    else if (lastLineBlank && counter === length - 1) {

    } else if (
      indent !== -1
      && indent < minIndent
      && !isEmptyOrBlank(line)
    ) {
      minIndent = indent;
    }
    counter++;
  }

  return lines.slice(
    firstlineBlank
      ? coerceAtMost(1, length - 1)
      : 0,
    lastLineBlank
      ? coerceAtLeast(length, 0)
      : length - 1)
    .map(line => line.slice(minIndent))
    .join('\n');
}

export function isEmptyOrBlank(str: string): boolean {
  if (isEmpty(str)) return true;
  const character = /\S/

  if (str.match(character)) {
    return false
  }
  return true;
}

export function isEmpty(str: string): boolean {
  return str.length == 0;
}

export function isNotEmpty(str: string): boolean {
  return !isEmpty(str)
}

export function coerceAtLeast(value: number, minimumValue: number): number {
  return Math.max(value, minimumValue);
}

export function coerceAtMost(value: number, maximumValue: number): number {
  return Math.min(value, maximumValue);
}

export function removeEmptyLineSpaces(input: string): string {
  const lines = input.split('\n');
  for (let i = 0; i < lines.length; i++) {
    if (lines[i].trim() === '') {
      lines[i] = '';
    } else {
      lines[i] = lines[i].replace(/\t/g, '').replace(/\s/g, '');
    }
  }
  return lines.join('\n');
}
