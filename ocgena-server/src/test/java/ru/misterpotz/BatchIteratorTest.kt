package ru.misterpotz

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ru.misterpotz.convert.BatchIterator
import ru.misterpotz.plugins.configureRouting
import ru.misterpotz.plugins.configureSerialization
import kotlin.test.assertEquals

class BatchIteratorTest {
    @Test
    fun kekpLication() = testApplication {
        application {
            configureRouting()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello World!", bodyAsText())
        }
    }

    @Test
    fun testRoot() = testApplication {
        application {
            configureRouting()
            configureSerialization()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello World!", bodyAsText())
        }
    }

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