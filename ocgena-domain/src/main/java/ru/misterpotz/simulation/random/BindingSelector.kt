package simulation.random

import ru.misterpotz.marking.objects.ObjectToken
import ru.misterpotz.marking.objects.ObjectTokenId
import simulation.binding.EnabledBinding
import java.util.SortedSet
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
        set: SortedSet<ObjectTokenId>,
        amount: Int
    ): SortedSet<ObjectTokenId>

    fun shuffleTokens(tokens: List<ObjectToken>): List<ObjectToken>
}

class TokenSelectorImpl(
    private val random: Random?
) : TokenSelector {
    override fun getTokensFromSet(set: SortedSet<ObjectTokenId>, amount: Int): SortedSet<ObjectTokenId> {
        return random?.let {
            set.shuffled(random = it)
                .take(amount)
                .toSortedSet()
        } ?: set
            .take(amount)
            .toSortedSet()
    }

    override fun shuffleTokens(tokens: List<ObjectToken>): List<ObjectToken> {
        return random?.let { tokens.sortedBy { it.id }.shuffled(it) } ?: tokens.sortedBy { it.id }
    }
}
