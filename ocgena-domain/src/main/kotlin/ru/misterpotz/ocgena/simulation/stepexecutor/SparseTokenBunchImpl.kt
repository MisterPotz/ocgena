package ru.misterpotz.ocgena.simulation.stepexecutor

import ru.misterpotz.ocgena.collections.PlaceToObjectMarking
import ru.misterpotz.ocgena.collections.PlaceToObjectMarkingMap
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.utils.defaultObjTypeId
import ru.misterpotz.ocgena.registries.PlaceToObjectTypeRegistry
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.simulation.interactors.SimpleTokenAmountStorage
import ru.misterpotz.ocgena.simulation.interactors.TokenAmountStorage

data class SparseTokenBunchImpl(
    val marking: PlaceToObjectMarking = PlaceToObjectMarkingMap(),
    val tokenAmountStorage: SimpleTokenAmountStorage = SimpleTokenAmountStorage(),
) : SparseTokenBunch {
    override fun objectMarking(): PlaceToObjectMarking {
        return marking
    }

    override fun tokenAmountStorage(): TokenAmountStorage {
        return tokenAmountStorage
    }

    override fun append(tokenBunch: SparseTokenBunch) {
        objectMarking().plus(tokenBunch.objectMarking())
        tokenAmountStorage().plus(tokenBunch.tokenAmountStorage())
    }


    override fun minus(tokenBunch: SparseTokenBunch) {
        tokenAmountStorage().minus(tokenBunch.tokenAmountStorage())
        objectMarking().minus(tokenBunch.objectMarking())
        validateState()
    }

    private fun validateState() {
        if (tokenAmountStorage.places.count() < objectMarking().places.count()) {
            throw IllegalStateException("cannot contain more places than token storage")
        }
        for (i in tokenAmountStorage.places) {
            if (tokenAmountStorage.getTokensAt(i) < objectMarking()[i].size) {
                throw IllegalStateException("cannot contain more than expected")
            }
        }
    }

    fun reindex() {
        tokenAmountStorage.reindexFrom(marking)
    }

    interface Builder {
        fun forPlace(petriAtomId: PetriAtomId, block: PlaceAccessa.() -> Unit): Builder
        fun setAtPlace(petriAtomId: PetriAtomId, realTokens : Int) : Builder
        fun buildTokenBunch(): SparseTokenBunchImpl
        fun buildWithTypeRegistry(): Pair<SparseTokenBunchImpl, PlaceToObjectTypeRegistry>

        infix fun String.to(tokenAmount : Int)
    }

    private class BuilderImpl : Builder {
        val forPlace = mutableMapOf<PetriAtomId, PlaceAccessaImpl>()

        class PlaceAccessaImpl : PlaceAccessa {
            override var realTokens: Int = 0

            override val initializedTokens: MutableSet<ObjectTokenId> = mutableSetOf()
            var realType: ObjectTypeId? = null
            override var type: ObjectTypeId
                get() = realType!!
                set(value) {
                    realType = value
                }

            override fun addAll(vararg tokens: Int) {
                initializedTokens.addAll(tokens.toList().map { it.toLong() })
            }
        }

        override fun forPlace(petriAtomId: PetriAtomId, block: PlaceAccessa.() -> Unit): Builder {
            forPlace.getOrPut(petriAtomId) {
                PlaceAccessaImpl()
            }.block()
            return this
        }

        override fun setAtPlace(petriAtomId: PetriAtomId, realTokens: Int): Builder {
            forPlace(petriAtomId) {
                this.realTokens = realTokens
            }
            return this
        }

        override fun buildTokenBunch(): SparseTokenBunchImpl {
            val marking = forPlace.mapValues { (id, block) ->
                block.initializedTokens.toSortedSet()
            }.let {
                PlaceToObjectMarkingMap(it.toMutableMap())
            }
            return SparseTokenBunchImpl(
                tokenAmountStorage = SimpleTokenAmountStorage(
                    placeToTokens = forPlace.mapValues { (id, block) ->
                        block.realTokens.coerceAtLeast(marking[id].size)
                    }.toMutableMap(),
                ),
                marking = marking
            )
        }

        override fun buildWithTypeRegistry(): Pair<SparseTokenBunchImpl, PlaceToObjectTypeRegistry> {
            val sparseTokenBunch = buildTokenBunch()
            val placeToObjectTypeRegistry = PlaceToObjectTypeRegistry(
                defaultObjTypeId,
                placeIdToObjectType = forPlace.mapValues { (_, block) ->
                    block.realType ?: defaultObjTypeId
                }.toMutableMap()
            )
            return Pair(sparseTokenBunch, placeToObjectTypeRegistry)
        }

        override fun String.to(tokenAmount: Int) {
            setAtPlace(this, tokenAmount)
        }
    }

    override fun toString(): String {
        return tokenAmountStorage.places.joinToString(separator = "  ##  ") {
            "${it} -- ${tokenAmountStorage.getTokensAt(it)}"
        }
    }

    interface PlaceAccessa {
        var realTokens: Int
        val initializedTokens: MutableSet<ObjectTokenId>
        var type: ObjectTypeId
        fun addAll(vararg tokens: Int)
    }

    companion object {
        fun makeBuilder(builda: Builder.() -> Unit): Builder {
            val builder = BuilderImpl()
            builder.builda()
            return builder
        }
    }
}