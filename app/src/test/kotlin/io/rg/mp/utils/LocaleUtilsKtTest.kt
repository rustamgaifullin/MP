package io.rg.mp.utils

import org.junit.Test
import java.util.Locale
import kotlin.test.assertEquals

class LocaleUtilsKtTest {

    @Test
    fun `should create instance of locale based on input string`() {
        var locale = createLocale("en_GB")

        assertEquals("en", locale.language)
        assertEquals("GB", locale.country)

        locale = createLocale("en")
        assertEquals("en", locale.language)
        assertEquals("", locale.country)

        locale = createLocale("")
        assertEquals(Locale.getDefault(), locale)
    }
}