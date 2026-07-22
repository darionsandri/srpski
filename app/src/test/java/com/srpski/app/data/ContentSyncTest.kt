package com.srpski.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ContentSyncTest {

    @Test fun joinUrlKeepsBaseDirectoryAndAppendsRelative() {
        assertEquals(
            "https://x.io/packs/u02/unit02.json",
            ContentSync.joinUrl("https://x.io/packs/u02/manifest.json", "unit02.json")
        )
    }

    @Test fun joinUrlStripsLeadingSlashOnRelative() {
        assertEquals(
            "https://x.io/packs/u02/unit02.json",
            ContentSync.joinUrl("https://x.io/packs/u02/manifest.json", "/unit02.json")
        )
    }

    @Test fun sha256HexIsKnownVector() {
        // Empty string SHA-256 known vector.
        assertEquals(
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            ContentSync.sha256Hex(ByteArray(0))
        )
    }
}
