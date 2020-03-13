package com.example.beepbeep;

import android.app.Activity;
import android.widget.EditText;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.robotium.solo.Solo;

@RunWith(AndroidJUnit4.class)
public class LoginTest {
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
     * test login button when all input is empty
     * @throws Exception
     */
    @Test
    public void testLoginButtonNoEntry() throws Exception {
        solo.assertCurrentActivity("Wrong Activity", Login.class);
        solo.clickOnButton("Login");
        solo.assertCurrentActivity("Wrong Activity", Login.class);
    }

    /**
     * test login button when all input is correct
     * @throws Exception

    @Test
    public void TestLoginButtonCorrectEntry() throws Exception {
        solo.assertCurrentActivity("Wrong Activity", Login.class);
        solo.enterText((EditText) solo.getView(R.id.Login_inputUsername), "DoNotDelete");
        solo.enterText((EditText) solo.getView(R.id.Login_inputPassword), "1234qwer");
        solo.clickOnButton("Login");
        solo.assertCurrentActivity("Wrong Activity", MapsActivity.class);
    }*/

    /**
     * test login button when password input is incorrect
     * @throws Exception
     */
    @Test
    public void TestLoginButtonIncorrectPasswordEntry() throws Exception {
        solo.assertCurrentActivity("Wrong Activity", Login.class);
        solo.enterText((EditText) solo.getView(R.id.Login_inputUsername), "DoNotDelete");
        solo.enterText((EditText) solo.getView(R.id.Login_inputPassword), "1234qwe2");
        solo.clickOnButton("Login");
        solo.assertCurrentActivity("Wrong Activity", Login.class);
    }

    /**
     * test login button when username input is incorrect
     * @throws Exception
     */
    @Test
    public void TestLoginButtonIncorrectUsernameEntry() throws Exception {
        solo.assertCurrentActivity("Wrong Activity", Login.class);
        solo.enterText((EditText) solo.getView(R.id.Login_inputUsername), "asda");
        solo.enterText((EditText) solo.getView(R.id.Login_inputPassword), "1234qwe2");
        solo.clickOnButton("Login");
        solo.assertCurrentActivity("Wrong Activity", Login.class);
    }

    @Test
    public void TestSignUp() throws Exception {
        solo.assertCurrentActivity("Wrong Activity", Login.class);
        solo.clickOnText("Don\\'t have an account?");
        solo.assertCurrentActivity("Wrong Activity", Signup.class);
    }

    /**
     * Closes the activity after each test
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception{
        solo.finishOpenedActivities();
    }

}
