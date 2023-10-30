package simulation.utils

import ru.misterpotz.model.marking.ObjectToken

class ObjectTokenStack(objectTokens : List<ObjectToken> ) {
    private val objectTokens : MutableList<ObjectToken> = objectTokens.toMutableList()

    fun tryConsume(amount: Int) : List<ObjectToken> {
        val amountToTake = amount.coerceAtMost(objectTokens.size)
        val toRet = objectTokens.takeLast(amountToTake)
        for (i in 0 until amountToTake) {
            objectTokens.removeLast()
        }
        return toRet
    }

    fun tryConsumeAll() : List<ObjectToken> {
        return tryConsume(objectTokens.size)
    }
}
