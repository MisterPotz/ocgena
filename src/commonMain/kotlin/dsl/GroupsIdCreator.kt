package dsl

class GroupsIdCreator {
    private val idCreators = mutableMapOf<String, PatternIdCreator>()

    fun patternIdCreatorFor(group: String) : PatternIdCreator {
        return idCreators.getOrPut(group) {
            PatternIdCreator(startIndex = 0) { "${group}_$it" }
        }
    }

    fun addPatternIdCreatorFor(
        group: String,
        startIndex : Int,
        formatter: (index: Int) -> String) : PatternIdCreator {
        if (group in idCreators) return idCreators[group]!!
        idCreators[group] = PatternIdCreator(startIndex, formatter)
        return idCreators[group]!!
    }
}
