package net.mbonnin

import net.mbonnin.paugmeetup.Downloader
import net.mbonnin.paugmeetup.PaugAnalyzer
import org.junit.Test

class TestDetector {
    @Test
    fun download() {
        Downloader().run()
    }

    @Test
    fun analyze() {
        PaugAnalyzer().run()
    }
}