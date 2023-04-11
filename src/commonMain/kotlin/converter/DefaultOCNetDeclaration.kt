@file:OptIn(ExperimentalJsExport::class)
@file:JsExport

package converter

import kotlin.js.ExperimentalJsExport


import kotlin.js.JsExport


class DefaultOCNetDeclaration(
    // in form key:"t1" value:"color=\"green\" ... other attributes"

    val nodeAttributes: Map<String /* id */, String> = mapOf(),
    val edgeAttributes: Map<String /* id */, String> = mapOf(),
) : OCDotDeclaration {
    override fun getNodeAttributeList(nodeLabel: String): Map<String, String> {
        return nodeAttributes[nodeLabel]?.let {
            getAttributeListFromString(it)
        } ?: mapOf()
    }

    private fun getAttributeListFromString(string: String): Map<String, String> {
        return string.split(" ")?.map {
            val keyAndValue = it.split("=")
            keyAndValue[0] to keyAndValue[1]
        }?.let {
            buildMap {
                for (i in it) {
                    put(i.first, i.second)
                }
            }
        } ?: mapOf()
    }

    override fun getEdgeAttributeList(edgeLabel: String): Map<String, String> {
        return edgeAttributes[edgeLabel]?.let {
            getAttributeListFromString(it)
        } ?: mapOf()
    }
}
