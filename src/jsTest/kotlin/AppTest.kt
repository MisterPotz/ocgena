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
}
