package converter

import ast.Edge

class EdgeToFromIterator(val edge: Edge) : Iterator<Connector.ToFrom> {
    var currentToIndex = 0
    override fun hasNext(): Boolean {
        return currentToIndex < edge.targets.size
    }

    override fun next(): Connector.ToFrom {
        val from = if (currentToIndex == 0) {
            edge.from
        } else {
            edge.targets[currentToIndex - 1]
        }

        val to = edge.targets[currentToIndex]
        currentToIndex++

        return Connector.ToFrom(from, to)
    }
}
