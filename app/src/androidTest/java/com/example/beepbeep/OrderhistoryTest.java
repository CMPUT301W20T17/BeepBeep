package com.example.beepbeep;

/*
 Title: order history test
 Author: Junqi Zou, Lyuyang Wang
 Date: 2020/03/27
*/

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
        solo.enterText((EditText) solo.getView(R.id.Login_inputUsername), "DoNotDelete");
        solo.enterText((EditText) solo.getView(R.id.Login_inputPassword), "1234qwer");
        solo.clickOnButton("Login");

        solo.clickOnView(solo.getView(R.id.bentoView));
        solo.assertCurrentActivity("Wrong Activity", Menu.class);

        solo.clickOnView(solo.getView(R.id.historyMenu));
        solo.assertCurrentActivity("Wrong Activity", OrderHistoryActivity.class);

        assertTrue(solo.waitForText("234", 1, 2000));
        assertTrue(solo.waitForText("Fri Mar 27 11:41:45 MDT 2020", 1, 2000));
        assertTrue(solo.waitForText("", 1, 2000));
        assertTrue(solo.waitForText("10125 109 St NW, Edmonton, AB T5J 3M5, Canada", 1, 2000));
        assertTrue(solo.waitForText("100 Princess Rd, Hulme, Manchester M15 5AS, UK", 1, 2000));
        assertTrue(solo.waitForText("10.0 CAD", 1, 2000));
        assertTrue(solo.waitForText("active", 1, 2000));
    }


    @Test
    public void testViewButton() throws Exception{
        solo.enterText((EditText) solo.getView(R.id.Login_inputUsername), "DoNotDelete");
        solo.enterText((EditText) solo.getView(R.id.Login_inputPassword), "1234qwer");
        solo.clickOnButton("Login");

        solo.clickOnView(solo.getView(R.id.bentoView));
        solo.assertCurrentActivity("Wrong Activity", Menu.class);

        solo.clickOnView(solo.getView(R.id.historyMenu));
        solo.assertCurrentActivity("Wrong Activity", OrderHistoryActivity.class);

        solo.clickOnView(solo.getView(R.id.view_contact_button));
        solo.assertCurrentActivity("Wrong Activity", ViewProfile.class);

        assertTrue(solo.waitForText("234", 1, 2000));
        assertTrue(solo.waitForText("249176381", 1, 2000));
        assertTrue(solo.waitForText("abc@111.11", 1, 2000));
    }


}
