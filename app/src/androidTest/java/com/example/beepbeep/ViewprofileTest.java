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
        //Logging in
        solo.enterText((EditText) solo.getView(R.id.Login_inputUsername), "DoNotDelete");
        solo.enterText((EditText) solo.getView(R.id.Login_inputPassword), "1234qwer");
        solo.clickOnButton("Login");

        //Go to main menu
        solo.clickOnView(solo.getView(R.id.bentoView));
        solo.assertCurrentActivity("Wrong Activity", Menu.class);

        //Go to view profile
        solo.clickOnText("Profile");
        solo.assertCurrentActivity("Wrong Activity", ViewProfile.class);

        assertTrue(solo.waitForText("test@test.com", 1, 2000));
        assertTrue(solo.waitForText("2222222222", 1, 2000));
        assertTrue(solo.waitForText("Rider", 1, 2000));


        //See if can switch to edit profile
        solo.clickOnView(solo.getView(R.id.edit_profile_button));
        solo.assertCurrentActivity("Wrong Activity", EditProfileActivity.class);

        //Check if logout button is working
        if(solo.waitForText("DoNotDelete",1,2000)) {
            solo.clickOnView(solo.getView(R.id.logout_button));
            solo.assertCurrentActivity("Wrong Activity", SignOut.class);
        }
        else{
            solo.clickOnView(solo.getView(R.id.email_button));
            solo.assertCurrentActivity("Wrong Activity",EmailUser.class);
            solo.enterText((EditText) solo.getView(R.id.editsubject), "test");
            solo.enterText((EditText) solo.getView(R.id.editmessage), "messageTest");
            solo.clickOnButton("Send");
        }
    }
}
