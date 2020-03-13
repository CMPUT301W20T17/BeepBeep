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

import java.util.Random;

@RunWith(AndroidJUnit4.class)
public class SignupTest {
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
     * Test sign up button with all entry empty
     * @throws Exception
     */
    @Test
    public void TestSignUpEmptyEntry() throws Exception {
        solo.clickOnText("Don\\'t have an account?");
        solo.assertCurrentActivity("Wrong Activity", Signup.class);
        solo.clickOnButton("Signup");
        solo.assertCurrentActivity("Wrong Activity", Signup.class);
    }

    /**
     * Test sign up button with all entry correctly entered
     * @throws Exception
     */
    @Test
    public void TestSignUpCorrectEntry() throws Exception {
        solo.clickOnText("Don\\'t have an account?");
        solo.assertCurrentActivity("Wrong Activity", Signup.class);
        solo.enterText((EditText) solo.getView(R.id.Signup_Usernameinput), randomUsername());
        solo.enterText((EditText) solo.getView(R.id.Signup_phoneInput), "3333333333");
        solo.enterText((EditText) solo.getView(R.id.Signup_emailInput), "abc@abc.com");
        solo.enterText((EditText) solo.getView(R.id.Signup_PasswordInput), "1234qwer");
        solo.enterText((EditText) solo.getView(R.id.Signup_ConfirmPasswordInput), "1234qwer");
        solo.clickOnButton("Signup");
        solo.assertCurrentActivity("Wrong Activity", Login.class);
    }

    /**
     * Test sign up button with email incorrectly entered
     * @throws Exception
     */
    @Test
    public void TestSignUpIncorrectEmailEntry() throws Exception {
        solo.clickOnText("Don\\'t have an account?");
        solo.assertCurrentActivity("Wrong Activity", Signup.class);
        solo.enterText((EditText) solo.getView(R.id.Signup_Usernameinput), randomUsername());
        solo.enterText((EditText) solo.getView(R.id.Signup_phoneInput), "3333333333");
        solo.enterText((EditText) solo.getView(R.id.Signup_emailInput), "2134asdf");
        solo.enterText((EditText) solo.getView(R.id.Signup_PasswordInput), "1234qwer");
        solo.enterText((EditText) solo.getView(R.id.Signup_ConfirmPasswordInput), "1234qwer");
        solo.clickOnButton("Signup");
        solo.assertCurrentActivity("Wrong Activity", Signup.class);
    }

    /**
     * Test sign up button with phone incorrectly entered
     * @throws Exception
     */
    @Test
    public void TestSignUpIncorrectPhoneEntry() throws Exception {
        solo.clickOnText("Don\\'t have an account?");
        solo.assertCurrentActivity("Wrong Activity", Signup.class);
        solo.enterText((EditText) solo.getView(R.id.Signup_Usernameinput), randomUsername());
        solo.enterText((EditText) solo.getView(R.id.Signup_phoneInput), "1513254asdg");
        solo.enterText((EditText) solo.getView(R.id.Signup_emailInput), "abc@abc.com");
        solo.enterText((EditText) solo.getView(R.id.Signup_PasswordInput), "1234qwer");
        solo.enterText((EditText) solo.getView(R.id.Signup_ConfirmPasswordInput), "1234qwer");
        solo.clickOnButton("Signup");
        solo.assertCurrentActivity("Wrong Activity", Signup.class);
    }

    /**
     * Test sign up button with password incorrectly entered
     * @throws Exception
     */
    @Test
    public void TestSignUpIncorrectPasswordEntry() throws Exception {
        solo.clickOnText("Don\\'t have an account?");
        solo.assertCurrentActivity("Wrong Activity", Signup.class);
        solo.enterText((EditText) solo.getView(R.id.Signup_Usernameinput), randomUsername());
        solo.enterText((EditText) solo.getView(R.id.Signup_phoneInput), "3333333333");
        solo.enterText((EditText) solo.getView(R.id.Signup_emailInput), "abc@abc.com");
        solo.enterText((EditText) solo.getView(R.id.Signup_PasswordInput), "23");
        solo.enterText((EditText) solo.getView(R.id.Signup_ConfirmPasswordInput), "23");
        solo.clickOnButton("Signup");
        solo.assertCurrentActivity("Wrong Activity", Signup.class);
    }

    /**
     * Test sign up button with mismatch password entered
     * @throws Exception
     */
    @Test
    public void TestSignUpMismatchPasswordEntry() throws Exception {
        solo.clickOnText("Don\\'t have an account?");
        solo.assertCurrentActivity("Wrong Activity", Signup.class);
        solo.enterText((EditText) solo.getView(R.id.Signup_Usernameinput), randomUsername());
        solo.enterText((EditText) solo.getView(R.id.Signup_phoneInput), "3333333333");
        solo.enterText((EditText) solo.getView(R.id.Signup_emailInput), "abc@abc.com");
        solo.enterText((EditText) solo.getView(R.id.Signup_PasswordInput), "23");
        solo.enterText((EditText) solo.getView(R.id.Signup_ConfirmPasswordInput), "2");
        solo.clickOnButton("Signup");
        solo.assertCurrentActivity("Wrong Activity", Signup.class);
    }

    /**
     * Test sign up button with duplicated username entered
     * @throws Exception
     */
    @Test
    public void TestSignUpDuplicateUsernameEntry() throws Exception {
        solo.clickOnText("Don\\'t have an account?");
        solo.assertCurrentActivity("Wrong Activity", Signup.class);
        solo.enterText((EditText) solo.getView(R.id.Signup_Usernameinput), "DoNotDelete");
        solo.enterText((EditText) solo.getView(R.id.Signup_phoneInput), "3333333333");
        solo.enterText((EditText) solo.getView(R.id.Signup_emailInput), "abc@abc.com");
        solo.enterText((EditText) solo.getView(R.id.Signup_PasswordInput), "1111111111");
        solo.enterText((EditText) solo.getView(R.id.Signup_ConfirmPasswordInput), "1111111111");
        solo.clickOnButton("Signup");
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

    /**
     * generates random username
     * @return username
     */
    String randomUsername(){
        int leftLimit = 97;
        int rightLimit = 122;
        int targetStringLength = 10;
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        return buffer.toString();
    }


}
