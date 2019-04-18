package io.rg.mp.utils

import org.junit.Test
import java.util.Locale
import kotlin.test.assertEquals

class LocaleUtilsKtTest {

    @Test
    fun `should create instance of locale based on input string`() {
        var locale = getLocaleInstance("en_GB")

        assertEquals("en", locale.language)
        assertEquals("GB", locale.country)

        locale = getLocaleInstance("en")
        assertEquals("en", locale.language)
        assertEquals("", locale.country)

        locale = getLocaleInstance("")
        assertEquals(Locale.getDefault(), locale)
    }
}