package ru.misterpotz.ocgena.ocnet.utils

import ru.misterpotz.ocgena.ocnet.OCNet
import ru.misterpotz.ocgena.ocnet.OCNetImpl
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.PlaceType
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcIdTo
import ru.misterpotz.ocgena.simulation.ObjectType

const val defaultObjTypeId = "obj"

val defaultObjType = ObjectType(defaultObjTypeId, defaultObjTypeId)
typealias ArrowAtomId = PetriAtomId
class OCNetBuilder() {
    fun defineAtoms(atomDefinitionBlock: AtomDefinitionBlock.() -> Unit): OCNet {
        val atomBlock = AtomDefinitionBlockImpl()
        atomBlock.atomDefinitionBlock()
        val builderRegistry = atomBlock.builderRegistry

        return OCNetImpl(
            objectTypeRegistry = builderRegistry.objectTypeRegistry,
            placeTypeRegistry = builderRegistry.placeTypeRegistry,
            placeToObjectTypeRegistry = builderRegistry.placeObjectTypeRegistry,
            petriAtomRegistry = builderRegistry.petriAtomRegistry
        )
    }

    interface AtomDefinitionBlock {
        val String.t : String
        val String.p : String

        fun String.t(block: (TransitionBlock.() -> Unit)? = null): String
        fun String.arc(block: (ArcBlock.() -> Unit)? = null): String
        fun String.p(block: (PlaceBlock.() -> Unit)? = null): String
        fun PetriAtomId.arc(
            petriAtomId: PetriAtomId,
            block: (ArcBlock.() -> Unit)? = null
        ): ArrowAtomId
    }

    class AtomDefinitionBlockImpl() : AtomDefinitionBlock {
        internal val builderRegistry = BuilderRegistry()
        override val String.t: String
            get() {
                return this.t()
            }
        override val String.p: String
            get() {
                return this.p()
            }

        override fun String.t(block: (TransitionBlock.() -> Unit)?): String {
            val receiver = builderRegistry.getTransition(this)
            if (block != null) {
                receiver.block()
            }
            return this
        }

        override fun String.arc(block: (ArcBlock.() -> Unit)?): String {
            val receiver = builderRegistry.getArc(this)
            if (block != null) {
                receiver.block()
            }
            return this
        }

        override fun PetriAtomId.arc(petriAtomId: PetriAtomId, block: (ArcBlock.() -> Unit)?): String {
            return this.arcIdTo(petriAtomId).also {
                builderRegistry.getArc(petriAtomId)
            }
        }

        override fun String.p(block: (PlaceBlock.() -> Unit)?): String {
            val receiver = builderRegistry.getPlace(this)
            if (block != null) {
                receiver.block()
            }
            return this
        }
    }

    interface AtomBlock

    interface TransitionBlock : AtomBlock {
        val id: PetriAtomId
    }

    class TransitionBlockImpl(override val id: PetriAtomId) : TransitionBlock

    interface PlaceBlock : AtomBlock {
        val id: PetriAtomId
        var placeType: PlaceType
        var objectTypeId: ObjectTypeId

        val input: Unit
        val output: Unit
    }

    class PlaceBlockImpl(
        override val id: PetriAtomId,
        override var placeType: PlaceType = PlaceType.NORMAL,
        override var objectTypeId: ObjectTypeId = defaultObjTypeId
    ) : PlaceBlock {
        override val input: Unit
            get() {
                placeType = PlaceType.INPUT
            }
        override val output: Unit
            get() {
                placeType = PlaceType.OUTPUT
            }
    }

    interface ArcBlock : AtomBlock {
        val id: PetriAtomId
        var type: Type
        var multiplicity: Int

        val vari: Unit
        val norm: Unit

        fun m(multiplicity: Int)

        enum class Type {
            VAR,
            NORMAL
        }
    }

    class ArcBlockImpl(
        override val id: PetriAtomId,
        override var type: ArcBlock.Type = ArcBlock.Type.NORMAL,
        override var multiplicity: Int = 1
    ) : ArcBlock {
        override val vari: Unit
            get() {
                type = ArcBlock.Type.VAR
            }
        override val norm: Unit
            get() {
                type = ArcBlock.Type.NORMAL
            }

        override fun m(multiplicity: Int) {
            this.multiplicity = multiplicity
        }
    }
}
