package com.huanchengfly.tieba.post.api

import org.junit.Assert.assertEquals
import org.junit.Test

class UtilsTest {
    @Test
    fun getCookieJoinsNonEmptyCookiesWithEqualsSeparator() {
        val cookie = getCookie(
            "BDUSS" to { "bduss-value" },
            "STOKEN" to { "" },
            "BAIDUZID" to { null },
            "CUID" to { "cuid-value" }
        )

        assertEquals("BDUSS=bduss-value; CUID=cuid-value", cookie)
    }
}
