package ru.misterpotz.ocgena.ocnet.utils

import ru.misterpotz.expression.node.MathNode
import ru.misterpotz.ocgena.ocnet.OCNetStruct
import ru.misterpotz.ocgena.ocnet.primitives.ObjectTypeId
import ru.misterpotz.ocgena.ocnet.primitives.OcNetType
import ru.misterpotz.ocgena.ocnet.primitives.PetriAtomId
import ru.misterpotz.ocgena.ocnet.primitives.PlaceType
import ru.misterpotz.ocgena.ocnet.primitives.ext.arcIdTo
import ru.misterpotz.ocgena.registries.ObjectTypeRegistryMap
import ru.misterpotz.ocgena.registries.PetriAtomRegistryStruct
import ru.misterpotz.ocgena.simulation.ObjectTokenId
import ru.misterpotz.ocgena.simulation.ObjectType
import java.lang.IllegalStateException

const val objTypePrefix = "△"
const val objPrefix = "●"
const val defaultObjTypeId = "${objTypePrefix}0"
fun ObjectTypeId.makeObjTypeId(): ObjectTypeId {
    return if (startsWith(objTypePrefix)) {
        this
    } else {
        "$objTypePrefix$this"
    }
}

fun ObjectTokenId.toObjTokenString(): String {
    return "$objPrefix$this"
}

fun ObjectTypeId.firstLetter(): String {
    return (if (!startsWith(objTypePrefix)) {
        first()
    } else {
        get(1)
    }).toString()
}

val defaultObjType = ObjectType(defaultObjTypeId, defaultObjTypeId)
typealias ArrowAtomId = PetriAtomId

class OCNetBuilder(
    private var useSpecialSymbolsInNaming: Boolean = true,
    val ocNetType: OcNetType = OcNetType.AALST
) {
    fun defineAtoms(atomDefinitionBlock: AtomDefinitionBlock.() -> Unit): OCNetStruct {
        val atomBlock = AtomDefinitionBlockImpl(useSpecialSymbolsInNaming, ocNetType = ocNetType)
        atomBlock.atomDefinitionBlock()
        val builderRegistry = atomBlock.builderRegistry

        return OCNetStruct(
            objectTypeRegistry = builderRegistry.objectTypeRegistry as ObjectTypeRegistryMap,
            placeTypeRegistry = builderRegistry.placeTypeRegistry,
            placeToObjectTypeRegistry = builderRegistry.placeObjectTypeRegistry,
            petriAtomRegistry = builderRegistry.petriAtomRegistry as PetriAtomRegistryStruct,
            ocNetType = ocNetType
        )
    }

    interface AtomDefinitionBlock {
        val String.t: String
        val String.p: String

        fun String.t(block: (TransitionBlock.() -> Unit)? = null): String
        fun String.arc(block: (ArcBlock.() -> Unit)? = null): String
        fun String.p(block: (PlaceBlock.() -> Unit)? = null): String
        fun PetriAtomId.arc(
            petriAtomId: PetriAtomId,
            block: (ArcBlock.() -> Unit)? = null
        ): ArrowAtomId
    }

    class AtomDefinitionBlockImpl(useSpecialSymbolsInNaming: Boolean, ocNetType: OcNetType) : AtomDefinitionBlock {
        internal val builderRegistry = BuilderRegistry(useSpecialSymbolsInNaming, ocNetType = ocNetType)
        override val String.t: String
            get() {
                return this.t()
            }
        override val String.p: String
            get() {
                return this.p()
            }

        override fun String.t(block: (TransitionBlock.() -> Unit)?): String {
            this.replace(" ", "_").apply {
                val receiver = builderRegistry.getTransition(this)
                if (block != null) {
                    receiver.block()
                }
                return this
            }

        }

        override fun String.arc(block: (ArcBlock.() -> Unit)?): String {
            this.replace(" ", "_").apply {
                val receiver = builderRegistry.getArc(this)
                if (block != null) {
                    receiver.block()
                }
                return this
            }
        }

        override fun PetriAtomId.arc(petriAtomId: PetriAtomId, block: (ArcBlock.() -> Unit)?): String {
            this.replace(" ", "_").apply {
                val arcId = arcIdTo(petriAtomId)
                val receiver = builderRegistry.getArc(arcId)
                if (block != null) {
                    receiver.block()
                }
                return petriAtomId.replace(" ", "_")
            }
        }

        override fun String.p(block: (PlaceBlock.() -> Unit)?): String {
            this.replace(" ", "_").apply {
                val receiver = builderRegistry.getPlace(this)
                if (block != null) {
                    receiver.block()
                }
                return this
            }
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

        var mathExpr: String

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

        var innerMathExpr: String? = null

        override var mathExpr: String
            get() = throw IllegalStateException()
            set(value) {
                innerMathExpr = value
            }
    }
}
