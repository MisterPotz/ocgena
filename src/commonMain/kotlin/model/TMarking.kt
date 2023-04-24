package model

class TMarking {
    private val transitionsToTMarkingValue = mutableMapOf<Transition, ActiveFiringTransitions>()
    operator fun get(transition: model.Transition): ActiveFiringTransitions? {
        return transitionsToTMarkingValue[transition]
    }

    fun shiftByTime(time: Time) {
        transitionsToTMarkingValue.forEach { entry ->
            entry.value.shiftByTime(time)
        }
    }

    fun getActiveTransitionWithEarliestFinish(): ActiveFiringTransition? {
        val transitionEntry = transitionsToTMarkingValue.filter {
            it.value.hasTransitions()
        }.minByOrNull { entry ->
            val activeFiringTransitions = entry.value
            val timeLeftUntilFinish = activeFiringTransitions
                .getWithEarliestFinishTime()
                .timeLeftUntilFinish()

            return@minByOrNull timeLeftUntilFinish
        }

        return transitionEntry?.value?.getWithEarliestFinishTime()
    }

    fun getEndedTransitions(): Collection<ActiveFiringTransition> {
        return transitionsToTMarkingValue.values.fold(mutableListOf<ActiveFiringTransition>()) { accum, transitions ->
            val endedTransitions = transitions.getEndedTransitions()
            accum.addAll(endedTransitions)
            return@fold accum
        }
    }

    fun getAndPopEndedTransitions(): Collection<ActiveFiringTransition> {
        val mutableList = mutableListOf<ActiveFiringTransition>()
        for (key in transitionsToTMarkingValue.keys.toList()) {
            val value = transitionsToTMarkingValue[key]!!
            val endedTransitions = value.getAndPopEndedTransitions()
            mutableList.addAll(endedTransitions)
            if (value.hasTransitions().not()) {
                transitionsToTMarkingValue.remove(key)
            }
        }
        return mutableList
    }

    fun pushTMarking(tMarkingValue: ActiveFiringTransition) {
        val transition = tMarkingValue.transition
        val current = transitionsToTMarkingValue.getOrPut(transition) {
            ActiveFiringTransitions()
        }
        current.add(tMarkingValue)
    }

    override fun toString(): String {
        return transitionsToTMarkingValue.keys.joinToString("\n") {
            val tMarkings = transitionsToTMarkingValue[it]
            """${tMarkings?.prettyPrintState()/*?.prependIndent("\t")*/}""".trimMargin()
        }
    }
}

