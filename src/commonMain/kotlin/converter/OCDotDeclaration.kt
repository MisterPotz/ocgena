package converter

interface OCDotDeclaration {
    // assumption: it is ordered list of statements
    // plain - doesn't contain any data that is relevant to simulation
    fun getNodeAttributeList(nodeLabel: String): Map<String, String>

    // plain - doesn't contain any data that is relevant to simulation
    fun getEdgeAttributeList(edgeLabel: String): Map<String, String>
}
