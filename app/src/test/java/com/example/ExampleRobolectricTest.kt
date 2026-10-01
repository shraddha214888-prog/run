package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.EmergencyPriority
import com.example.data.model.SignalState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("૧૦૮ રક્ષક", appName)
  }

  @Test
  fun `emergency priority definitions are valid`() {
    val critical = EmergencyPriority.CRITICAL
    assertEquals("અત્યંત ગંભીર (કોડ રેડ)", critical.titleGujarati)
    assertEquals(3, critical.sirenIntensity)
  }

  @Test
  fun `signal state preemption verification`() {
    val state = SignalState.PREEMPTED_GREEN
    assertNotNull(state)
  }
}

