package ru.misterpotz

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.misterpotz.convert.BatchIterator
import java.lang.IllegalStateException

class BatchIteratorTest {
    @Test
    fun `batches work ok`() {
        val batchIterator = BatchIterator(100, 100)
        Assertions.assertTrue(batchIterator.hasNext())
        Assertions.assertTrue(batchIterator.next() == 0..99L)
        Assertions.assertFalse(batchIterator.hasNext())
    }

    @Test
    fun `batches work on empty`() {
        val batchIterator = BatchIterator(100, 0)
        Assertions.assertTrue(batchIterator.hasNext().not())
    }

    @Test
    fun `batch coerces size`() {
        val batchIterator = BatchIterator(20, 4)
        Assertions.assertTrue(batchIterator.hasNext())
        Assertions.assertEquals(0..3L, batchIterator.next())
        Assertions.assertFalse(batchIterator.hasNext())
    }

    @Test
    fun `double take`() {
        val batchIterator = BatchIterator(5, 20)
        Assertions.assertTrue(batchIterator.hasNext())
        Assertions.assertEquals(0..4L, batchIterator.next())
        Assertions.assertEquals(5..9L, batchIterator.next())
        Assertions.assertEquals(10..14L, batchIterator.next())
        Assertions.assertEquals(15..19L, batchIterator.next())
        Assertions.assertThrows(IllegalStateException::class.java) {
            batchIterator.next()
        }
    }
}