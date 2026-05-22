package io.github.suriyadi15.qrishook.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppVersionTest {
    @Test
    fun `compares newer release with v prefix`() {
        assertTrue(AppVersion.compare("v0.2.0", "0.1.0") > 0)
    }

    @Test
    fun `treats v prefix as equal`() {
        assertEquals(0, AppVersion.compare("v0.1.0", "0.1.0"))
    }

    @Test
    fun `treats missing patch as zero`() {
        assertEquals(0, AppVersion.compare("1.2", "1.2.0"))
    }

    @Test
    fun `ignores prerelease and build suffixes`() {
        assertEquals(0, AppVersion.compare("1.2.3-beta+7", "1.2.3"))
    }

    @Test
    fun `invalid version does not report newer or older`() {
        assertEquals(0, AppVersion.compare("latest", "0.1.0"))
    }
}
