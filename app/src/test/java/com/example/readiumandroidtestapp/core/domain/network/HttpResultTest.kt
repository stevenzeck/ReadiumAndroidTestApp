package com.example.readiumandroidtestapp.core.domain.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class HttpResultTest {

    @Test
    fun `equals returns true for same properties`() {
        val result1 = HttpResult(body = byteArrayOf(1, 2, 3), contentType = "text/plain")
        val result2 = HttpResult(body = byteArrayOf(1, 2, 3), contentType = "text/plain")

        assertEquals(result1, result2)
    }

    @Test
    fun `equals returns false for different body`() {
        val result1 = HttpResult(body = byteArrayOf(1, 2, 3), contentType = "text/plain")
        val result2 = HttpResult(body = byteArrayOf(1, 2, 4), contentType = "text/plain")

        assertNotEquals(result1, result2)
    }

    @Test
    fun `equals returns false for different contentType`() {
        val result1 = HttpResult(body = byteArrayOf(1, 2, 3), contentType = "text/plain")
        val result2 = HttpResult(body = byteArrayOf(1, 2, 3), contentType = "application/json")

        assertNotEquals(result1, result2)
    }

    @Test
    fun `hashCode is consistent`() {
        val result1 = HttpResult(body = byteArrayOf(1, 2, 3), contentType = "text/plain")
        val result2 = HttpResult(body = byteArrayOf(1, 2, 3), contentType = "text/plain")

        assertEquals(result1.hashCode(), result2.hashCode())
    }
}
