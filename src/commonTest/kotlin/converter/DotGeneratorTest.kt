package converter

import dsl.OCNetFacadeBuilder
import model.OCNetElementsImpl
import utils.mprintln
import kotlin.test.Test
import kotlin.test.assertEquals

class DotGeneratorTest {
    @Test
    fun simpleDotGenerator() {
        val ocNetFacadeBuilder = OCNetFacadeBuilder()
        val ocNet = ocNetFacadeBuilder.tryBuildModel {
            place { }
                .arcTo(transition { })
                .variableArcTo(place { })
        }

        val ocGraphvizConverter = OCGraphvizGenerator(
            originalOCDOtDeclaration = DefaultOCNetDeclaration(),
            ocNetElements = ocNet.ocNetElements,
            ocnetErrorService = EmptyOCNetErrorService
        )

        mprintln(ocGraphvizConverter.compileDigraphStatements().trimEnd('\n'))
        assertEquals(
            """
            |p1 [  ];
            |p2 [  ];
            |t1 [  ];
            |p1 -> t1 [ color="black" ]
            |t1 -> p2 [ color="black:white:black" ]
            """.trimMargin(),
            ocGraphvizConverter.compileDigraphStatements().trimEnd('\n')
        )
    }

    @Test
    fun complexDotGenerator() {
        val ocNetFacadeBuilder = OCNetFacadeBuilder()
        ocNetFacadeBuilder.tryBuildModel {
            place("place1")
                .variableArcTo(transition("tr1"))
                .ar
                .arcTo(place("place2"))
                .variableArcTo()

        }
    }
}
