package simulation.random

import model.ObjectToken
import simulation.binding.EnabledBinding
import kotlin.random.Random

interface RandomFactory {
    fun create(): Random
}

class RandomFactoryImpl(private val randomSeed: Int?) : RandomFactory {
    override fun create(): Random {
        return randomSeed?.let { Random(randomSeed) } ?: Random
    }
}

interface BindingSelector {
    fun selectBinding(enabledBindings: List<EnabledBinding>): EnabledBinding
}

class BindingSelectorImpl(
    private val random: Random?,
) : BindingSelector {
    override fun selectBinding(enabledBindings: List<EnabledBinding>): EnabledBinding {
        return random?.let { enabledBindings.random(random = it) } ?: enabledBindings.first()
    }
}

interface TokenSelector {
    fun getTokensFromSet(
        set: Set<ObjectToken>,
        amount: Int
    ): Set<ObjectToken>

    fun shuffleTokens(tokens: List<ObjectToken>): List<ObjectToken>
}

class TokenSelectorImpl(
    private val random: Random?
) : TokenSelector {
    override fun getTokensFromSet(
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

    override fun shuffleTokens(tokens: List<ObjectToken>): List<ObjectToken> {
        return random?.let { tokens.sortedBy { it.id }.shuffled(it) } ?: tokens.sortedBy { it.id }
    }
}
