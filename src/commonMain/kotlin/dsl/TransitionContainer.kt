package dsl

interface TransitionContainer {
    val transitions: MutableMap<String, TransitionDSL>
    val transitionPatternIdCreator: PatternIdCreator
}
