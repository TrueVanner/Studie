package nl.tue.appdev.studie;

import static org.junit.Assert.assertNotNull;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class) // This annotation is used to run the test on an Android device or emulator.
public class SignupActivityTest {
    @Test
    public void testActivityLaunches() {
        // Launch the activity using ActivityScenario
        try (ActivityScenario<SignupActivity> scenario = ActivityScenario.launch(SignupActivity.class)) {
            // If the activity was launched successfully, the scenario should not be null.
            assertNotNull(scenario);
        }
    }
}
