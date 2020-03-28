package com.example.beepbeep;

/*
 Title: order history test
 Author: Junqi Zou, Lyuyang Wang
 Date: 2020/03/27
*/

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
public class RiderMapTest {
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
     * Enter empty Start location will send a error message
     *
     * @throws Exception
     */
    @Test
    public void testEmptyStartLocation() throws Exception{

        solo.enterText((EditText) solo.getView(R.id.Login_inputUsername), "DoNotDelete");
        solo.enterText((EditText) solo.getView(R.id.Login_inputPassword), "1234qwer");
        solo.clickOnButton("Login");
        solo.assertCurrentActivity("Wrong Activity",RiderMapActivity.class);

        //test empty start location
        solo.clickOnText("Enter the pickup location");;
        solo.typeText(0, "Hub Mall");
        solo.clickOnText("112 Street Northwest");
        solo.clickOnText("Hub Mall");
        assertTrue(solo.waitForText("Please enter the pickup location or destination", 1, 2000));
    }

    /**
     *  Enter empty destination will send a error message
     *
     * @throws Exception
     */
    @Test
    public void testEmptyDestination() throws Exception{

        solo.enterText((EditText) solo.getView(R.id.Login_inputUsername), "DoNotDelete");
        solo.enterText((EditText) solo.getView(R.id.Login_inputPassword), "1234qwer");
        solo.clickOnButton("Login");
        solo.assertCurrentActivity("Wrong Activity",RiderMapActivity.class);

        //test empty destination
        solo.clickOnText("Enter the destination");
        solo.typeText(0, "Hub Mall");
        solo.clickOnText("Hub Mall");
        solo.clickOnText("CONFIRM");
        assertTrue(solo.waitForText("Please enter the pickup location or destination", 1, 2000));
    }

    /**
     * Enter empty destination and empty start location will send a error message
     *
     * @throws Exception
     */
    @Test
    public void testEmptyDS() throws Exception{

        solo.enterText((EditText) solo.getView(R.id.Login_inputUsername), "DoNotDelete");
        solo.enterText((EditText) solo.getView(R.id.Login_inputPassword), "1234qwer");
        solo.clickOnButton("Login");
        solo.assertCurrentActivity("Wrong Activity",RiderMapActivity.class);

        solo.clickOnText("CONFIRM");
        assertTrue(solo.waitForText("Please enter the pickup location or destination", 1, 2000));
    }

    /**
     * Enter the start location and destination and test
     *
     * @throws Exception
     */

    @Test
    public void testSendRequest() throws Exception{
        solo.enterText((EditText) solo.getView(R.id.Login_inputUsername), "DoNotDelete");
        solo.enterText((EditText) solo.getView(R.id.Login_inputPassword), "1234qwer");
        solo.clickOnButton("Login");
        solo.assertCurrentActivity("Wrong Activity",RiderMapActivity.class);

        //test empty destination
        solo.clickOnText("Enter the pickup location");
        solo.typeText(0, "Hub Mall");
        solo.clickOnText("Hub Mall");

        //test empty destination
        solo.clickOnText("Enter the destination");
        solo.typeText(0, "West Edmonton Mall");
        solo.clickOnText("West Edmonton Mall");

        //test after press the first confirm button
        solo.clickOnText("CONFIRM");

        assertTrue(solo.waitForText("Start: 9002 112 St NW, Edmonton, AB T6G 2C5, Canada", 1, 2000));
        assertTrue(solo.waitForText("End: 8770 170 St NW, Edmonton, AB T5T 3J7, Canada", 1, 2000));
        assertTrue(solo.waitForText("Price: 20", 1, 2000));


    }


}
