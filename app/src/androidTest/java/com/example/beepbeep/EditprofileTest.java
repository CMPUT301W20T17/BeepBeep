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
public class EditprofileTest {
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

    @Test
    public void testback() throws Exception{
        solo.enterText((EditText) solo.getView(R.id.Login_inputUsername), "DoNotDelete");
        solo.enterText((EditText) solo.getView(R.id.Login_inputPassword), "1234qwer");
        solo.clickOnButton("Login");
        solo.clickOnButton("Profile");
        //need som change to switch to ViewProfile activity ***********************
        solo.clickOnButton("Edit");

        //set back to original data

        solo.enterText((EditText) solo.getView(R.id.email_editText), "");
        solo.enterText((EditText) solo.getView(R.id.phone_editText), "");

        solo.enterText((EditText) solo.getView(R.id.email_editText), "test@test.com");
        solo.enterText((EditText) solo.getView(R.id.phone_editText), "2222222222");
        solo.clickOnButton("SAVE");

    }

    /**
     * Log in, and then test if user can see the profile to edit phone and email
     * @throws Exception
     */
    @Test
    public void testEditProfile() throws Exception{
        solo.enterText((EditText) solo.getView(R.id.Login_inputUsername), "DoNotDelete");
        solo.enterText((EditText) solo.getView(R.id.Login_inputPassword), "1234qwer");
        solo.clickOnButton("Login");
        solo.clickOnButton("Profile");
        solo.clickOnButton("Edit");

        solo.assertCurrentActivity("Wrong Activity", EditProfileActivity.class);

        assertTrue(solo.waitForText("test1@test1.com", 1, 2000));
        assertTrue(solo.waitForText("3333333333", 1, 2000));
    }

    /**
     * Log in, in the edit profile, click save button to check if the view changed
     * @throws Exception
     */
    @Test
    public void testSaveButton() throws Exception{
        solo.enterText((EditText) solo.getView(R.id.Login_inputUsername), "DoNotDelete");
        solo.enterText((EditText) solo.getView(R.id.Login_inputPassword), "1234qwer");
        solo.clickOnButton("Login");
        solo.clickOnButton("Profile");
        //need som change to switch to ViewProfile activity ***********************
        solo.clickOnButton("Edit");

        solo.enterText((EditText) solo.getView(R.id.email_editText), "");
        solo.enterText((EditText) solo.getView(R.id.phone_editText), "");

        solo.enterText((EditText) solo.getView(R.id.email_editText), "test1@test1.com");
        solo.enterText((EditText) solo.getView(R.id.phone_editText), "3333333333");

        solo.clickOnButton("SAVE");

        solo.assertCurrentActivity("Wrong Activity", EditProfileActivity.class);

        assertTrue(solo.waitForText("test1@test1.com", 1, 2000));
        assertTrue(solo.waitForText("3333333333", 1, 2000));

    }

    /**
     * Log in, in the edit profile, click cancel button to check if the view  not changed
     * @throws Exception
     */
    @Test
    public void testCancelButton() throws Exception{
        solo.enterText((EditText) solo.getView(R.id.Login_inputUsername), "DoNotDelete");
        solo.enterText((EditText) solo.getView(R.id.Login_inputPassword), "1234qwer");
        solo.clickOnButton("Login");
        solo.clickOnButton("Profile");
        //need som change to switch to ViewProfile activity ***********************
        solo.clickOnButton("Edit");

        solo.enterText((EditText) solo.getView(R.id.email_editText), "");
        solo.enterText((EditText) solo.getView(R.id.phone_editText), "");
        solo.enterText((EditText) solo.getView(R.id.email_editText), "test2@test2.com");
        solo.enterText((EditText) solo.getView(R.id.phone_editText), "44444444444");

        solo.clickOnButton("CANCEL");

        solo.assertCurrentActivity("Wrong Activity", EditProfileActivity.class);

        assertTrue(solo.waitForText("test@test.com", 1, 2000));
        assertTrue(solo.waitForText("2222222222", 1, 2000));

    }

}
