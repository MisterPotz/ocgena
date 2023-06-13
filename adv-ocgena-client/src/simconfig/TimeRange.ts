import * as t from 'io-ts';

const TimeRange = t.tuple([t.number, t.number]);

export type TimeRangeType = t.TypeOf<typeof TimeRange>;

export class TimeRangeClass {
  constructor(private timeRange: [number, number]) { }

  get start(): number {
    return this.timeRange[0];
  }

  get end(): number {
    return this.timeRange[1];
  }
}

const TransitionIntervals = t.type({
  duration: TimeRange,
  minOccurrenceInterval: TimeRange,
});

export type TransitionIntervalsType = t.TypeOf<typeof TransitionIntervals>;

export const TransitionsConfig = t.partial({
  defaultTransitionInterval: t.union([TransitionIntervals, t.undefined]),
  transitionsToIntervals: t.union([t.record(t.string, TransitionIntervals), t.undefined]),
});

export type TransitionsConfigType = t.TypeOf<typeof TransitionsConfig>;

export const GenerationConfig = t.partial({
  default: t.union([TimeRange, t.undefined]),
  generationTargets: t.union([t.record(t.string, t.number), t.undefined])
})

export type GenerationConfigType = t.TypeOf<typeof GenerationConfig>;