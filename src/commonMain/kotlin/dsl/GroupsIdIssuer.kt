package dsl

class GroupsIdIssuer {
    private val nestedIdIssuers = mutableMapOf<String, NestedIdIssuer>()

    fun nestedIdIssuerFor(group: String) : NestedIdIssuer {
        return nestedIdIssuers.getOrPut(group) {
            NestedIdIssuer()
        }
    }
}
