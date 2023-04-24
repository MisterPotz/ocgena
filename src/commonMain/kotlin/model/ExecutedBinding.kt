package model

data class ExecutedBinding(
    val finishedTransition : ActiveFiringTransition,
    val consumedMap : ObjectMarking,
    val producedMap : ObjectMarking
) {
    fun prettyString() : String {
        return """transition ${finishedTransition.transition}
            |   consumed:
            |${consumedMap.prettyPrint().prependIndent("    ")}
            |${producedMap.prettyPrint().prependIndent("    ")}
        """.trimMargin()
    }

    override fun toString(): String {
        return """executed transition: ${finishedTransition.transition}
            |   consumed:
            |${consumedMap.toString().prependIndent("\t\t")}
            |   produced:
            |${producedMap.toString().prependIndent("\t\t")}
        """.trimMargin()
    }
}
