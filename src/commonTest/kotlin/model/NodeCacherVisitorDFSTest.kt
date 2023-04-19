package model

import dsl.OCNetFacadeBuilder
import model.utils.NodesCacherVisitorDFS
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class NodeCacherVisitorDFSTest {
    @Test
    fun checkSimpleNetIsCached() {
        val ocNetFacadeBuilder = OCNetFacadeBuilder()
        val ocNet = ocNetFacadeBuilder.tryBuildModel {
            place {
                placeType = PlaceType.INPUT
            }
                .arcTo(transition { })
                .arcTo(place { placeType = PlaceType.OUTPUT })
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
        val ocNet = ocNetFacadeBuilder.tryBuildModel {
            place {
                placeType = PlaceType.INPUT
            }
                .arcTo(transition { })
                .connectTo(subgraph {
                    this.inNode
                        .arcTo(place {  })
                        .variableArcTo(transition { })
                        .variableArcTo(outNode)
                })
                .connectToLeftOf(place { placeType = PlaceType.OUTPUT })
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
        val ocNet = ocNetFacadeBuilder.tryBuildModel {
            objectType("ob1") { "ob1$it" }
            objectType("ob2") { "ob2$it" }
            place {
                placeType = PlaceType.INPUT
                objectType = objectType("ob1")
            }
                .arcTo(transition { })
                .connectTo(subgraph {
                    this.inNode
                        .arcTo(place {
                            objectType = objectType("ob2")
                        })
                        .variableArcTo(transition { })
                        .variableArcTo(outNode)
                })
                .connectToLeftOf(place { placeType = PlaceType.OUTPUT; objectType = objectType("ob2")})
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

