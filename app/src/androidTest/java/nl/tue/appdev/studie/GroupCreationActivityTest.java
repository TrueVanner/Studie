package nl.tue.appdev.studie;

import static org.junit.Assert.assertNotNull;

import android.widget.Button;

import androidx.test.core.app.ActivityScenario;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class GroupCreationActivityTest {
    @Test
    public void testActivityLaunches() {
        // Launch the activity using ActivityScenario
        try (ActivityScenario<GroupCreationActivity> scenario = ActivityScenario.launch(GroupCreationActivity.class)) {
            // If the activity was launched successfully, the scenario should not be null.
            assertNotNull(scenario);
        }
    }
}