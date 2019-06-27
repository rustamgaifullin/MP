package io.rg.mp.utils

import org.junit.Test
import kotlin.test.assertEquals

class StringUtilTest {
    @Test
    fun `should convert string to double`() {
        assertEquals(100.25, "100,25 zl".extractDouble())
    }
}