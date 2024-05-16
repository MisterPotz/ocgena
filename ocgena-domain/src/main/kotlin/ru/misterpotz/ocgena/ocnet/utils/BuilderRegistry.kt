package ru.misterpotz.ocgena.ocnet.utils

import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.registries.ArcsRegistry
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.arcs.AalstVariableArcMeta
import ru.misterpotz.ocgena.ocnet.primitives.arcs.LomazovaVariableArcMeta
import ru.misterpotz.ocgena.ocnet.primitives.arcs.NormalArc
import ru.misterpotz.ocgena.ocnet.primitives.arcs.VariableArc
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Place
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcArrowId
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcTailId
import ru.misterpotz.ocgena.ocnet.utils.OCNetBuilder.ArcBlock.Type.*
import ru.misterpotz.ocgena.registries.*
import ru.misterpotz.ocgena.simulation_old.ObjectType
import java.lang.IllegalArgumentException

internal class BuilderRegistry(useSpecialSymbolsInNaming: Boolean, ocNetType: OcNetType) {
    private val atomBuilders: MutableMap<PetriAtomId, OCNetBuilder.AtomBlock> = mutableMapOf()

    fun getPlace(petriAtomId: PetriAtomId): OCNetBuilder.PlaceBlock {
        return atomBuilders.getOrPut(petriAtomId) {
            OCNetBuilder.PlaceBlockImpl(petriAtomId)
        } as OCNetBuilder.PlaceBlock
    }

    fun getTransition(petriAtomId: PetriAtomId): OCNetBuilder.TransitionBlock {
        return atomBuilders.getOrPut(petriAtomId) {
            OCNetBuilder.TransitionBlockImpl(petriAtomId)
        } as OCNetBuilder.TransitionBlock
    }

    fun getArc(petriAtomId: PetriAtomId): OCNetBuilder.ArcBlock {
        return atomBuilders.getOrPut(petriAtomId) {
            OCNetBuilder.ArcBlockImpl(petriAtomId)
        } as OCNetBuilder.ArcBlock
    }

    val objectTypeRegistry by lazy(LazyThreadSafetyMode.NONE) {
        atomBuilders.values
            .asSequence()
            .filterIsInstance<OCNetBuilder.PlaceBlock>()
            .map { if (useSpecialSymbolsInNaming) it.objectTypeId.makeObjTypeId() else it.objectTypeId }
            .toSet()
            .map {
                ObjectType(id = it, label = it)
            }
            .associateBy {
                it.id
            }.let {
                ObjectTypeRegistry(it.toMutableMap())
            }
    }

    val placeTypeRegistry by lazy(LazyThreadSafetyMode.NONE) {
        atomBuilders.values
            .filterIsInstance<OCNetBuilder.PlaceBlock>()
            .associateBy(
                keySelector = {
                    it.id
                },
                valueTransform = {
                    it.placeType
                }
            ).let {
                PlaceTypeRegistry(it)
            }
    }

    val placeObjectTypeRegistry by lazy(LazyThreadSafetyMode.NONE) {
        atomBuilders.values
            .filterIsInstance<OCNetBuilder.PlaceBlock>()
            .associateBy(
                keySelector = {
                    it.id
                },
                valueTransform = {
                    if (useSpecialSymbolsInNaming) {
                        it.objectTypeId.makeObjTypeId()
                    } else {
                        it.objectTypeId
                    }
                }
            ).let {
                PlaceToObjectTypeRegistry(defaultObjTypeId, it.toMutableMap())
            }
    }

    private fun Collection<PetriAtomId>.filterArcsEndWith(petriAtomId: PetriAtomId): List<PetriAtomId> {
        return filter {
            it.endsWith(petriAtomId) && it.length != petriAtomId.length && it.contains(".") &&
                    it.substringAfter(".") == petriAtomId
        }
    }

    private fun Collection<PetriAtomId>.filterArcsStartWith(petriAtomId: PetriAtomId): List<PetriAtomId> {
        return filter {
            it.startsWith(petriAtomId) &&
                    it.contains(".") &&
                    it.length != petriAtomId.length &&
                    it.substringBefore(".") == petriAtomId
        }
    }

    private fun Collection<PetriAtomId>.mapArcsTail(): List<PetriAtomId> {
        return map { it.arcTailId() }
    }

    private fun Collection<PetriAtomId>.mapArcsHead(): List<PetriAtomId> {
        return map { it.arcArrowId() }
    }

    val petriAtomRegistry by lazy(LazyThreadSafetyMode.NONE) {
        atomBuilders.values.map {
            val atom = when (it) {
                is OCNetBuilder.PlaceBlock -> {
                    Place(
                        id = it.id,
                        label = it.id,
                        fromTransitions = atomBuilders
                            .keys
                            .filterArcsEndWith(it.id)
                            .mapArcsTail()
                            .toMutableList(),
                        toTransitions = atomBuilders
                            .keys
                            .filterArcsStartWith(it.id)
                            .mapArcsHead()
                            .toMutableList()
                    )
                }

                is OCNetBuilder.TransitionBlock -> {
                    Transition(
                        id = it.id,
                        label = it.id,
                        fromPlaces = atomBuilders
                            .keys
                            .filterArcsEndWith(it.id)
                            .mapArcsTail()
                            .toMutableList(),
                        toPlaces = atomBuilders
                            .keys
                            .filterArcsStartWith(it.id)
                            .mapArcsHead()
                            .toMutableList()
                    )
                }

                is OCNetBuilder.ArcBlockImpl -> {
                    when (it.type) {
                        VAR -> VariableArc(
                            id = it.id,
                            when (ocNetType) {
                                OcNetType.AALST -> {
                                    AalstVariableArcMeta
                                }

                                OcNetType.LOMAZOVA -> {
                                    require(it.innerMathExpr != null) {
                                        "Lomazova arcs must have a variable denoted"
                                    }
                                    LomazovaVariableArcMeta(it.innerMathExpr!!)
                                }
                            }
                        )

                        NORMAL -> NormalArc(id = it.id, multiplicity = it.multiplicity)
                    }
                }

                else -> throw IllegalArgumentException()
            }
            atom
        }.associateBy {
            it.id
        }.let {
            PetriAtomRegistry(it)
        }
    }

    val placeRegistry by lazy(LazyThreadSafetyMode.NONE) {
        PlaceRegistry(petriAtomRegistry)
    }
    val transitionRegistry by lazy(LazyThreadSafetyMode.NONE) {
        TransitionsRegistry(petriAtomRegistry)
    }
    val arcRegistry by lazy(LazyThreadSafetyMode.NONE) {
        ArcsRegistry(petriAtomRegistry)
    }
}
