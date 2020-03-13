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

import static junit.framework.TestCase.assertTrue;

@RunWith(AndroidJUnit4.class)
public class OrderhistoryTest {
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
    public void testViewOrderHistory() throws Exception{
        solo.enterText((EditText) solo.getView(R.id.Login_inputUsername), "123");
        solo.enterText((EditText) solo.getView(R.id.Login_inputPassword), "A123456");
        solo.clickOnButton("Login");
        solo.clickOnButton("OrderHistory");
        //Switch to OrderHistory activity ***********************
        solo.assertCurrentActivity("Wrong Activity", OrderHistoryActivity.class);

        assertTrue(solo.waitForText("234", 1, 2000));
        assertTrue(solo.waitForText("121212", 1, 2000));
        assertTrue(solo.waitForText("Mali Prefecture", 1, 2000));
        assertTrue(solo.waitForText("Atlantic Ocean", 1, 2000));
        assertTrue(solo.waitForText("10.0 CAD", 1, 2000));
        assertTrue(solo.waitForText("active", 1, 2000));
    }

    @Test
    public void testViewButton() throws Exception{
        solo.enterText((EditText) solo.getView(R.id.Login_inputUsername), "123");
        solo.enterText((EditText) solo.getView(R.id.Login_inputPassword), "A123456");
        solo.clickOnButton("Login");

        //Switch to OrderHistory activity ***********************
        solo.clickOnButton("OrderHistory");
        solo.assertCurrentActivity("Wrong Activity", OrderHistoryActivity.class);

        //Switch to ViewProfile activity ***********************
        solo.clickOnButton("VIEW");
        solo.assertCurrentActivity("Wrong Activity", ViewProfile.class);

        assertTrue(solo.waitForText("234", 1, 2000));
        assertTrue(solo.waitForText("249176381", 1, 2000));
        assertTrue(solo.waitForText("abc@111.11", 1, 2000));
    }
}
