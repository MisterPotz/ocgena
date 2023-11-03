package model

import dsl.OCNetFacadeBuilder
import ru.misterpotz.ocgena.registries.PlaceToObjectTypeRegistry
import ru.misterpotz.ocgena.registries.PlaceTypeRegistry
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test


@Disabled
class NodeCacherVisitorDFSTest {
    private val defaultPlaceToObjectTypeRegistry = PlaceToObjectTypeRegistry.build()
    private val defaultPlaceTypeRegistry = PlaceTypeRegistry.build {
        inputPlaces("p1")
        outputPlaces("p2")
    }

    @Test
    fun checkSimpleNetIsCached() {
//        val ocNetFacadeBuilder = OCNetFacadeBuilder()
//        val placeToObjectTypeRegistry = PlaceToObjectTypeRegistry.build()
//        val ocNet = ocNetFacadeBuilder.tryBuildModelFromDSl(
//            placeToObjectTypeRegistry,
//            placeTypeRegistry = defaultPlaceTypeRegistry
//        ) {
//            place { }
//                .arcTo(transition { })
//                .arcTo(place { })
//        }.requireConsistentOCNet()
//
//        val nodeCacher = NodesCacherVisitorDFS()
//
//        nodeCacher.collectAllNodes(ocNet)
//        val collectedNodes = nodeCacher.getCachedNodes()
//        val allNodeLabels = collectedNodes.allNodeLabels()
//        assertEquals(3, allNodeLabels.size, message = "expected 3 collected nodes")
//        assertContains(allNodeLabels, "p1")
//        assertContains(allNodeLabels, "p2")
//        assertContains(allNodeLabels, "t1")
    }

    @Test
    fun checkComplexOcNetCacher() {
//        val ocNetFacadeBuilder = OCNetFacadeBuilder()
//        val ocNet = ocNetFacadeBuilder.tryBuildModelFromDSl(
//            defaultPlaceToObjectTypeRegistry,
//            PlaceTypeRegistry.build {
//                outputPlaces("p3")
//                inputPlaces("p1")
//            }) {
//            place { }
//                .arcTo(transition { })
//                .connectTo(subgraph {
//                    this.inNode
//                        .arcTo(place { })
//                        .variableArcTo(transition { })
//                        .variableArcTo(outNode)
//                })
//                .connectToLeftOf(place { })
//        }.requireConsistentOCNet()
//
//        val nodeCacher = NodesCacherVisitorDFS()
//
//        nodeCacher.collectAllNodes(ocNet)
//        val collectedNodes = nodeCacher.getCachedNodes()
//        val allNodeLabels = collectedNodes.allNodeLabels()
//        assertEquals(5, allNodeLabels.size, message = "expected 3 collected nodes")
//        assertContains(allNodeLabels, "p1")
//        assertContains(allNodeLabels, "p2")
//        assertContains(allNodeLabels, "p3")
//        assertContains(allNodeLabels, "t1")
//        assertContains(allNodeLabels, "t2")
    }

    @Test
    fun checkComplexOcNetCacherObjectTypes() {
//        val ocNetFacadeBuilder = OCNetFacadeBuilder()
//        val ocNet = ocNetFacadeBuilder.tryBuildModelFromDSl(
//            placeToObjectTypeRegistry = PlaceToObjectTypeRegistry.build {
//                objectType("ob1", "ob11")
//                objectType("ob2", "ob21 ob22")
//            },
//            placeTypeRegistry = PlaceTypeRegistry.build {
//                inputPlaces("ob11")
//                outputPlaces("ob22")
//            }
//        ) {
//            objectType("ob1") { "ob1$it" }
//            objectType("ob2") { "ob2$it" }
//            place("ob11") {   }
//                .arcTo(transition { })
//                .connectTo(subgraph {
//                    this.inNode
//                        .arcTo(place("ob21"))
//                        .variableArcTo(transition { })
//                        .variableArcTo(outNode)
//                })
//                .connectToLeftOf(place("ob22"))
//        }.requireConsistentOCNet()
//
//        val nodeCacher = NodesCacherVisitorDFS()
//
//        nodeCacher.collectAllNodes(ocNet)
//        val collectedNodes = nodeCacher.getCachedNodes()
//        val allNodeLabels = collectedNodes.allNodeLabels()
//        assertEquals(5, allNodeLabels.size, message = "expected 3 collected nodes")
//        assertContains(allNodeLabels, "ob11")
//        assertContains(allNodeLabels, "ob21")
//        assertContains(allNodeLabels, "ob22")
//        assertContains(allNodeLabels, "t1")
//        assertContains(allNodeLabels, "t2")
    }
}

