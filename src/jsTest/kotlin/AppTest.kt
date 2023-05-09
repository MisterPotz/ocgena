import converter.DefaultErrorReporterContainer
import converter.OcDotParserV2
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class AppTest {
    @Test
    fun thingsShouldWork() {
        assertEquals(listOf(1,2,3).reversed(), listOf(3,2,1))
    }

    @Test
    fun thingsShouldBreak() {
        assertFails {
            assertEquals(listOf(1,2,3).reversed(), listOf(1,2,3))
        }
    }

    @Test
    fun thing() {
        val simpleOcNet = """           
             ocnet { 
                places { 
                    p1 p2
                }

                transitions { 
                    t1
                }

                p1 10=> t1 1-> p2 [  ];

                subgraph s1 { 
                    subgraph ss1 { 

                    }
                } 3-> t1 2-> { p1 p2 }
            }""".trimIndent().prependIndent()

        val errorContainer = DefaultErrorReporterContainer()
        val ocdotParser = OcDotParserV2(errorContainer)

        val dslElements = ocdotParser.parse(simpleOcNet)
        val errors = errorContainer.collectReport()
//        dslElements?.savedEdgeBlocks
    }
}
