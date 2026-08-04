package com.example.supporthub.features.authentication

import com.example.supporthub.features.authentication.utils.StartupDestination
import com.example.supporthub.features.authentication.utils.StartupFlowStateMachine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StartupFlowSplashTimingTest {

    @Test
    fun `startup data ready does not navigate until splash minimum display is completed`() {
        val resolved = StartupFlowStateMachine.onStartupDataLoaded(
            current = StartupFlowStateMachine.initial(),
            destination = StartupDestination.Login,
            user = null
        )

        val splashStarted = StartupFlowStateMachine.onSplashAnimationFinished(resolved)

        assertTrue(splashStarted.showSplash)
        assertNull(splashStarted.navigationTarget)

        val readyToNavigate = StartupFlowStateMachine.onMinimumSplashDurationElapsed(splashStarted)

        assertEquals(StartupDestination.Login, readyToNavigate.navigationTarget)
    }
}
