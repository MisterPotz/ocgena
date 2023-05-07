package simulation.random

import model.ObjectToken
import simulation.binding.EnabledBinding
import kotlin.random.Random

class RandomFactory {
    fun create(randomSeed: Long?): Random {
        return randomSeed?.let { Random(randomSeed) } ?: Random
    }
}

class BindingSelector(
    private val random: Random?,
) {
    fun selectBinding(enabledBindings: List<EnabledBinding>): EnabledBinding {
        return random?.let { enabledBindings.random(random = it) } ?: enabledBindings.first()
    }
}

class TokenSelector(
    private val random: Random?
) {
    fun getTokensFromSet(
        set: Set<ObjectToken>,
        amount: Int
    ): Set<ObjectToken> {
        return random?.let {
            set.shuffled(random = it).take(amount).toSet()
        } ?: set
            .toList()
            .sortedBy { it.id }
            .take(amount)
            .toSet()
    }

    fun shuffleTokens(tokens: List<ObjectToken>): List<ObjectToken> {
        return random?.let { tokens.sortedBy { it.id }.shuffled(it) } ?: tokens.sortedBy { it.id }
    }
}
