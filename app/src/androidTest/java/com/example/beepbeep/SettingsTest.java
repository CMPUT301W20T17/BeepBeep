package com.example.beepbeep;

import android.app.Activity;
import android.widget.EditText;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.robotium.solo.Solo;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SettingsTest {
    private Solo solo;
    @Rule
    public ActivityTestRule<Login> rule =
            new ActivityTestRule<>(Login.class, true, true);

    /**
     * Runs before all tests and creates solo instance.
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        solo = new Solo(InstrumentationRegistry.getInstrumentation(), rule.getActivity());
    }

    /**
     * Gets the Activity
     *
     * @throws Exception
     */
    @Test
    public void start() throws Exception {
        Activity activity = rule.getActivity();
    }

    /**
     * Log in, and then test if user can use the settings
     * @throws Exception
     */
    @Test
    public void testSettings() throws Exception {
        //Logging in
        solo.enterText((EditText) solo.getView(R.id.Login_inputUsername), "DoNotDelete");
        solo.enterText((EditText) solo.getView(R.id.Login_inputPassword), "1234qwer");
        solo.clickOnButton("Login");

        //Go to main menu
        solo.clickOnView(solo.getView(R.id.bentoView));
        solo.assertCurrentActivity("Wrong Activity", Menu.class);

        //Go to view profile
        solo.clickOnText("Settings");
        solo.assertCurrentActivity("Wrong Activity", Setting.class);
    }
}
