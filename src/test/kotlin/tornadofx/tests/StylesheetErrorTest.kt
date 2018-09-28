package tornadofx.tests

import javafx.stage.Stage
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.testfx.api.FxAssert.verifyThat
import org.testfx.api.FxToolkit
import org.testfx.matcher.control.LabeledMatchers.hasText
import tornadofx.App
import tornadofx.FX
import tornadofx.testapps.Styles
import tornadofx.testapps.StylesheetErrorView

class StylesheetErrorTest {

    companion object {
        val app = App(StylesheetErrorView::class, Styles::class)
        @JvmStatic
        @BeforeClass
        fun before() {
            val primaryStage: Stage = FxToolkit.registerPrimaryStage()
            // This will cause a stylesheet error, but should not crash the application.
            FX.registerApplication(FxToolkit.setupApplication { app }, primaryStage)
        }

        @JvmStatic
        @AfterClass
        fun after() {
            FxToolkit.cleanupApplication(app)
        }
    }

    @Test
    fun `should start application with wrong stylesheet without crashing`() {
        verifyThat(".my-button", hasText("Click here"))
    }
}
