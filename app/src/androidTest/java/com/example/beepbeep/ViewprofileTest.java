package com.example.beepbeep;

import android.app.Activity;
import android.view.View;
import android.widget.EditText;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.robotium.solo.Solo;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.TestCase.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ViewprofileTest {
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
     * Log in, and then test if user can see his/her own profile, and make sure the proper info show up
     * @throws Exception
     */
    @Test
    public void testViewProfile() throws Exception{
        solo.enterText((EditText) solo.getView(R.id.Login_inputUsername), "DoNotDelete");
        solo.enterText((EditText) solo.getView(R.id.Login_inputPassword), "1234qwer");
        solo.clickOnButton("Login");
        solo.clickOnButton("Profile");
        solo.assertCurrentActivity("Wrong Activity", ViewProfile.class);

        assertTrue(solo.waitForText("test@test.com", 1, 2000));
        assertTrue(solo.waitForText("2222222222", 1, 2000));
        assertTrue(solo.waitForText("Rider", 1, 2000));
    }

    @Test
    public void testEditButton() throws Exception{
        solo.enterText((EditText) solo.getView(R.id.Login_inputUsername), "DoNotDelete");
        solo.enterText((EditText) solo.getView(R.id.Login_inputPassword), "1234qwer");
        solo.clickOnButton("Login");
        solo.clickOnButton("Profile");
        //need som change to switch to ViewProfile activity ***********************
        solo.assertCurrentActivity("Wrong Activity", ViewProfile.class);
        solo.clickOnButton("Edit");
        solo.assertCurrentActivity("Wrong Activity", EditProfileActivity.class);
    }
}
