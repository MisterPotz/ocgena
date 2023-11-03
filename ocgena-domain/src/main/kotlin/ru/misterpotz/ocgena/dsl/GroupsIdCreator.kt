package ru.misterpotz.ocgena.dsl

import dsl.PatternIdCreator

class GroupsIdCreator {
    private val idCreators = mutableMapOf<String, PatternIdCreator>()

    fun patternIdCreatorFor(group: String) : PatternIdCreator {
        return idCreators.getOrPut(group) {
            PatternIdCreator(startIndex = 0) { "${group}_$it" }
        }
    }

    fun addPatternIdCreatorFor(
        group: String,
        startIndex : Long,
        formatter: (index: Long) -> String) : PatternIdCreator {
        if (group in idCreators) return idCreators[group]!!
        idCreators[group] = PatternIdCreator(startIndex, formatter)
        return idCreators[group]!!
    }
}
