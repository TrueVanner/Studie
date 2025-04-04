package nl.tue.appdev.studie;

import static org.junit.Assert.assertNotNull;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class GroupCreationActivityTest{

    @Test
    public void testGroupActivityLaunches(){
            try (ActivityScenario<GroupCreationActivity> scenario = ActivityScenario.launch(GroupCreationActivity.class)) {
                assertNotNull(scenario);
            }
    }

}