package model

import dsl.OCNetFacadeBuilder
import model.utils.NodesCacherVisitorDFS
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class NodeCacherVisitorDFSTest {
    private val defaultPlaceTyping = PlaceTyping.build()
    private val defaultInputOutputPlaces = InputOutputPlaces.build {
        inputPlaces("p1")
        outputPlaces("p2")
    }

    @Test
    fun checkSimpleNetIsCached() {
        val ocNetFacadeBuilder = OCNetFacadeBuilder()
        val placeTyping = PlaceTyping.build()
        val ocNet = ocNetFacadeBuilder.tryBuildModelFromDSl(
            placeTyping,
            inputOutputPlaces = defaultInputOutputPlaces
        ) {
            place { }
                .arcTo(transition { })
                .arcTo(place { })
        }.requireConsistentOCNet()

        val nodeCacher = NodesCacherVisitorDFS()

        nodeCacher.collectAllNodes(ocNet)
        val collectedNodes = nodeCacher.getCachedNodes()
        val allNodeLabels = collectedNodes.allNodeLabels()
        assertEquals(3, allNodeLabels.size, message = "expected 3 collected nodes")
        assertContains(allNodeLabels, "p1")
        assertContains(allNodeLabels, "p2")
        assertContains(allNodeLabels, "t1")
    }

    @Test
    fun checkComplexOcNetCacher() {
        val ocNetFacadeBuilder = OCNetFacadeBuilder()
        val ocNet = ocNetFacadeBuilder.tryBuildModelFromDSl(
            defaultPlaceTyping,
            InputOutputPlaces.build {
                outputPlaces("p3")
                inputPlaces("p1")
            }) {
            place { }
                .arcTo(transition { })
                .connectTo(subgraph {
                    this.inNode
                        .arcTo(place { })
                        .variableArcTo(transition { })
                        .variableArcTo(outNode)
                })
                .connectToLeftOf(place { })
        }.requireConsistentOCNet()

        val nodeCacher = NodesCacherVisitorDFS()

        nodeCacher.collectAllNodes(ocNet)
        val collectedNodes = nodeCacher.getCachedNodes()
        val allNodeLabels = collectedNodes.allNodeLabels()
        assertEquals(5, allNodeLabels.size, message = "expected 3 collected nodes")
        assertContains(allNodeLabels, "p1")
        assertContains(allNodeLabels, "p2")
        assertContains(allNodeLabels, "p3")
        assertContains(allNodeLabels, "t1")
        assertContains(allNodeLabels, "t2")
    }

    @Test
    fun checkComplexOcNetCacherObjectTypes() {
        val ocNetFacadeBuilder = OCNetFacadeBuilder()
        val ocNet = ocNetFacadeBuilder.tryBuildModelFromDSl(
            placeTyping = PlaceTyping.build {
                objectType("ob1", "ob11")
                objectType("ob2", "ob21 ob22")
            },
            inputOutputPlaces = InputOutputPlaces.build {
                inputPlaces("ob11")
                outputPlaces("ob22")
            }
        ) {
            objectType("ob1") { "ob1$it" }
            objectType("ob2") { "ob2$it" }
            place("ob11") {   }
                .arcTo(transition { })
                .connectTo(subgraph {
                    this.inNode
                        .arcTo(place("ob21"))
                        .variableArcTo(transition { })
                        .variableArcTo(outNode)
                })
                .connectToLeftOf(place("ob22"))
        }.requireConsistentOCNet()

        val nodeCacher = NodesCacherVisitorDFS()

        nodeCacher.collectAllNodes(ocNet)
        val collectedNodes = nodeCacher.getCachedNodes()
        val allNodeLabels = collectedNodes.allNodeLabels()
        assertEquals(5, allNodeLabels.size, message = "expected 3 collected nodes")
        assertContains(allNodeLabels, "ob11")
        assertContains(allNodeLabels, "ob21")
        assertContains(allNodeLabels, "ob22")
        assertContains(allNodeLabels, "t1")
        assertContains(allNodeLabels, "t2")
    }
}

