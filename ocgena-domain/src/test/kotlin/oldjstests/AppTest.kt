package oldjstests

import org.junit.jupiter.api.Test

class AppTest {
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
    }
}
