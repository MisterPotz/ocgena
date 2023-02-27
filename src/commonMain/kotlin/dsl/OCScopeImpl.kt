package dsl

class OCScopeImpl(
    val rootScope: OCScopeImpl? = null,
    val defaultScopeType: ObjectTypeDSL? = null,
) : TypeScope, SubgraphDSL {
    private val groupIdIssuer: GroupsIdIssuer =
        rootScope?.groupIdIssuer ?: GroupsIdIssuer()
    private val defaultTransitionIdIssuer: NestedIdIssuer
        get() = groupIdIssuer.nestedIdIssuerFor("t")
    private val defaultPlaceIdIssuer: NestedIdIssuer
        get() = groupIdIssuer.nestedIdIssuerFor("p")

    private val defaultObjectIdIssuer: NestedIdIssuer
        get() = groupIdIssuer.nestedIdIssuerFor("ot")

    private val defaultObjectTypeDSL: ObjectTypeDSL =
        defaultScopeType
            ?: rootScope?.defaultObjectTypeDSL
            ?: ObjectTypeImpl(
                id = defaultObjectIdIssuer.getInlineBuilder().newIdString(),
                label = "object"
            )

    private var _subgraphInput : PlaceDSL? = null
    private var _subgraphOutput : PlaceDSL? = null

    override var input: PlaceDSL
        get() = checkNotNull(_subgraphInput)
        set(value) {
            _subgraphInput = value
        }

    override var output: PlaceDSL
        get() = checkNotNull(_subgraphOutput)
        set(value) {
            _subgraphOutput = value
        }

    val places: MutableMap<String, PlaceDSL> = rootScope?.places ?: mutableMapOf()
    private val transitions: MutableMap<String, TransitionDSL> = rootScope?.transitions ?: mutableMapOf()
    private val objectTypes: MutableMap<String, ObjectTypeDSL> = rootScope?.objectTypes ?: mutableMapOf()
    private val arcs: MutableList<ArcDSL> = rootScope?.arcs ?: mutableListOf()

    override val scopeType: ObjectTypeDSL
        get() = checkNotNull(defaultScopeType)

    override fun place(block: OCPlaceScope.() -> Unit): PlaceDSL {
        return internalPlace(null, block)
    }

    override fun setAsInputOutput(placeDSL: PlaceDSL) {
        input = placeDSL
        output = placeDSL
    }

    override fun place(label: String, block: OCPlaceScope.() -> Unit): PlaceDSL {
        return internalPlace(label, block)
    }

    override fun place(label: String): PlaceDSL {
        return internalPlace(label) { }
    }

    override fun transition(block: OCTransitionScope.() -> Unit): TransitionDSL {
        return internalTransition(null, block)
    }

    override fun transition(label: String, block: OCTransitionScope.() -> Unit): TransitionDSL {
        return internalTransition(label, block)
    }

    override fun transition(label: String): TransitionDSL {
        return internalTransition(label) { }
    }

    private fun internalTransition(label: String?, block: OCTransitionScope.() -> Unit): TransitionDSL {
        if (label != null) {
            val transition = transitions[label]
            if (transition != null) {
                return transition
            }
        }
        val defaultId = defaultTransitionIdIssuer.getInlineBuilder().newIdString()

        val transitionDSLImpl = TransitionDSLImpl(
            defaultLabel = label ?: defaultId,
        )
        transitionDSLImpl.block()

        transitions[label ?: defaultId] = transitionDSLImpl
        return transitionDSLImpl
    }



    override fun selectPlace(block: PlaceDSL.() -> Boolean): PlaceDSL {
        return places.values.first { atomDSL ->
            atomDSL.block()
        }
    }

    override fun LinkChainDSL.arcTo(multiplicity: Int, linkChainDSL: LinkChainDSL): LinkChainDSL {
        return internalArc(this, linkChainDSL, multiplicity, isVariable = false)
    }

    override fun LinkChainDSL.arcTo(linkChainDSL: LinkChainDSL): LinkChainDSL {
        return internalArc(this, linkChainDSL, multiplicity = 1, isVariable = false)
    }

    override fun List<HasLast>.arcTo(linkChainDSL: LinkChainDSL): HasLast {
        return internalArc(this, linkChainDSL, multiplicity = 1, isVariable = false)
    }

    override fun LinkChainDSL.arcTo(linkChainDSLList: List<HasFirst>): HasFirst {
        return internalArc(this, linkChainDSLList, multiplicity = 1, isVariable = false)
    }

    override fun List<HasLast>.arcTo(multiplicity: Int, linkChainDSL: LinkChainDSL): HasLast {
        return internalArc(this, linkChainDSL, multiplicity = multiplicity, isVariable = false)
    }

    override fun LinkChainDSL.arcTo(multiplicity: Int, linkChainDSLList: List<HasFirst>): HasFirst {
        return internalArc(this, linkChainDSLList, multiplicity = multiplicity, isVariable = false)
    }

    override fun subgraph(block: SubgraphDSL.() -> Unit): LinkChainDSL {
        val newScope = OCScopeImpl(
            rootScope = rootScope,
            defaultScopeType = defaultScopeType
        )
        newScope.block()
        return LinkChainDSLImpl(
            firstElement = newScope.input,
            lastElement = newScope.output
        )
    }

    override fun LinkChainDSL.variableArcTo(linkChainDSL: LinkChainDSL): LinkChainDSL {
        return internalArc(this, linkChainDSL, multiplicity = 1, isVariable = true)
    }

    override fun List<HasLast>.variableArcTo(linkChainDSL: LinkChainDSL): HasLast {
        return internalArc(this, linkChainDSL, multiplicity = 1, isVariable = true)
    }

    override fun LinkChainDSL.variableArcTo(linkChainDSLList: List<HasFirst>): HasFirst {
        return internalArc(this, linkChainDSLList, multiplicity = 1, isVariable = true)
    }

    private fun internalArc(
        from: LinkChainDSL,
        to: LinkChainDSL,
        multiplicity: Int,
        isVariable: Boolean,
    ): LinkChainDSL {
        val newLinkChainDSLImpl = LinkChainDSLImpl(firstElement = from.lastElement, lastElement = to.firstElement)

        newLinkChainDSLImpl.apply {
            val newArc = if (isVariable) {
                VariableArcDSLImpl(tailAtom = from.lastElement, arrowAtom = to.firstElement)
            } else {
                NormalArcDSLImpl(multiplicity = multiplicity, tailAtom = from.lastElement, arrowAtom = to.firstElement)
            }
            arcs.add(newArc)
        }
        return newLinkChainDSLImpl
    }

    private fun internalArc(
        from: List<HasLast>,
        to: LinkChainDSL,
        multiplicity: Int,
        isVariable: Boolean,
    ): HasLast {
        val newLinkChainDSLImpl = HasLastImpl(to.lastElement)
        val toElement = to.firstElement
        for (fromEntity in from) {
            val newArc = if (isVariable) {
                VariableArcDSLImpl(tailAtom = fromEntity.lastElement, arrowAtom = toElement)
            } else {
                NormalArcDSLImpl(multiplicity = multiplicity, tailAtom = fromEntity.lastElement, arrowAtom = toElement)
            }

            arcs.add(newArc)
        }
        return newLinkChainDSLImpl
    }

    private fun internalArc(
        from: LinkChainDSL,
        to: List<HasFirst>,
        multiplicity: Int,
        isVariable: Boolean,
    ): HasFirst {
        val newLinkChainDSLImpl = HasFirstImpl(from.firstElement)

        val fromElement = from.lastElement
        for (toEntity in to) {
            val newArc = if (isVariable) {
                VariableArcDSLImpl(tailAtom = fromElement, arrowAtom = toEntity.firstElement)
            } else {
                NormalArcDSLImpl(multiplicity = multiplicity, tailAtom = fromElement, arrowAtom = toEntity.firstElement)
            }

            arcs.add(newArc)
        }
        return newLinkChainDSLImpl
    }

    override fun selectTransition(block: TransitionDSL.() -> Boolean): TransitionDSL {
        return transitions.values.first { transitionDSL ->
            transitionDSL.block()
        }
    }

    override fun forType(objectTypeDSL: ObjectTypeDSL, block: TypeScope.() -> Unit) {
        val ocScopeImpl = OCScopeImpl(
            rootScope = rootScope ?: this,
            defaultScopeType = objectTypeDSL
        )
        ocScopeImpl.block()
    }

    override fun objectType(label: String): ObjectTypeDSL {
        return objectTypes.getOrPut(label) {
            val newId = defaultObjectIdIssuer.getInlineBuilder().newIdString()
            ObjectTypeImpl(newId, label)
        }
    }


    private fun internalPlace(label: String? = null, block: OCPlaceScope.() -> Unit): PlaceDSL {
        // TODO: simplify code for place dsl, as change of label inside the ocplacescope is not required
        if (label != null) {
            val place = places[label]
            if (place != null) {
                return place
            }
        }
        var defaultId = defaultPlaceIdIssuer.getInlineBuilder().newIdString()
        val objectTypesStack = mutableListOf<ObjectTypeDSL>(defaultObjectTypeDSL)

        val placeDSLImpl =
            PlaceDSLImpl(
                onAssignNewLabel = { },
                onAssignNewObjectType = {
                    for (i in objectTypesStack.size.downTo(1)) {
                        val type = objectTypesStack[i]

                        groupIdIssuer.nestedIdIssuerFor(type.label).getInlineBuilder().removeLast()
                        objectTypesStack.removeLast()
                    }
                    objectTypesStack.add(it)
                    defaultId = groupIdIssuer.nestedIdIssuerFor(it.label).getInlineBuilder().newIdString()
                },
                labelFactory = {
                    label ?: defaultId
                },
                objectType = defaultObjectTypeDSL
            )
        placeDSLImpl.block()
        places[label ?: defaultId] = (placeDSLImpl)
        return placeDSLImpl
    }

    companion object {
        const val DEFAULT_PLACE_PREFIX = "p"
        const val DEFAULT_TRANSITION_PREFIX = "t"
        const val OBJECT_TYPE_PREFIX = "ot"
        const val USER_SAFE_PREFIX = "u"
    }
}
