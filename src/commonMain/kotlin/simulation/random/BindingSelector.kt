package simulation.random

import model.ObjectToken
import simulation.binding.EnabledBinding
import kotlin.random.Random

class RandomFactory {
    fun create(randomSeed: Long?) : Random {
        return randomSeed?.let { Random(randomSeed) } ?: Random
    }
}

class BindingSelector(
    private val random: Random = Random,
) {
    fun selectBinding(enabledBindings: List<EnabledBinding>): EnabledBinding {
        return enabledBindings.random(random = random)
    }
}

class TokenSelector(
    private val random: Random
) {
    fun getTokensFromSet(
        set : Set<ObjectToken>,
        amount : Int
    ) : Set<ObjectToken> {
        return set.shuffled(random = random).take(amount).toSet()
    }
}
