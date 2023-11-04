package ru.misterpotz.ocgena.ocnet.utils

import model.ArcsRegistry
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.arcs.NormalArc
import ru.misterpotz.ocgena.ocnet.primitives.arcs.VariableArc
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Place
import ru.misterpotz.ocgena.ocnet.primitives.atoms.Transition
import ru.misterpotz.ocgena.ocnet.utils.OCNetBuilder.ArcBlock.Type.*
import ru.misterpotz.ocgena.registries.*
import ru.misterpotz.ocgena.simulation.ObjectType
import java.lang.IllegalArgumentException

internal class BuilderRegistry() {
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
            .map { it.objectTypeId }
            .toSet()
            .map {
                ObjectType(it, id = it)
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
                    it.objectTypeId
                }
            ).let {
                PlaceToObjectTypeRegistry(defaultObjTypeId, it.toMutableMap())
            }
    }

    val petriAtomRegistry by lazy(LazyThreadSafetyMode.NONE) {
        atomBuilders.values.map {
            val atom = when (it) {
                is OCNetBuilder.PlaceBlock -> {
                    Place(it.id, it.id)
                }
                is OCNetBuilder.TransitionBlock -> {
                    Transition(it.id, it.id)
                }
                is OCNetBuilder.ArcBlock -> {
                    when (it.type) {
                        VAR -> VariableArc(it.id)
                        NORMAL -> NormalArc(it.id)
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
