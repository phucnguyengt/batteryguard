package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.model.BatteryInfo
import com.example.model.BatteryStatus
import com.example.ui.components.BatteryGauge
import com.example.ui.theme.BatteryGuardTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent {
      BatteryGuardTheme {
        BatteryGauge(
          batteryInfo = BatteryInfo(
            level = 80,
            temperature = 32.5f,
            voltage = 4.15f,
            currentMa = 0,
            status = BatteryStatus.NOT_CHARGING_BYPASS,
            isBypassActive = true,
            isPlugged = true
          )
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

