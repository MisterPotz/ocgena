package ru.misterpotz.ocgena.dsl

import ru.misterpotz.ocgena.dsl.GroupsIdCreator

class GroupIdCreatorSetupper() {
    fun setupGroupIdCreator(groupsIdCreator: GroupsIdCreator) {
        groupsIdCreator.addPatternIdCreatorFor("t", startIndex = 1) {
            "t$it"
        }
        groupsIdCreator.addPatternIdCreatorFor("subgraphs", startIndex = 1) {
            "subgr$it"
        }
    }
}
