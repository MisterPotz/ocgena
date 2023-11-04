package simulation.utils

import ru.misterpotz.ocgena.simulation.ObjectTokenId

class ObjectTokenStack(objectTokens : List<ObjectTokenId> ) {
    private val objectTokens : MutableList<ObjectTokenId> = objectTokens.toMutableList()

    fun tryConsume(amount: Int) : List<ObjectTokenId> {
        val amountToTake = amount.coerceAtMost(objectTokens.size)
        val toRet = objectTokens.takeLast(amountToTake)
        for (i in 0 until amountToTake) {
            objectTokens.removeLast()
        }
        return toRet
    }

    fun tryConsumeAll() : List<ObjectTokenId> {
        return tryConsume(objectTokens.size)
    }
}
