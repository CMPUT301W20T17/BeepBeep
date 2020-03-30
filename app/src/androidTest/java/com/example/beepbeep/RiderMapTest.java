package com.example.beepbeep;

/*
 Title: order history test
 Author: Junqi Zou, Lyuyang Wang
 Date: 2020/03/27
*/

import android.app.Activity;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.robotium.solo.Solo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.List;

import static com.google.common.collect.Iterables.size;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
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
     * Enter empty destination and empty start location will send a error message
     *
     * @throws Exception
     */
    @Test
    public void testEmpty() throws Exception{

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
    public void testCancelButton () throws Exception{
        solo.enterText((EditText) solo.getView(R.id.Login_inputUsername), "DoNotDelete");
        solo.enterText((EditText) solo.getView(R.id.Login_inputPassword), "1234qwer");
        solo.clickOnButton("Login");
        solo.assertCurrentActivity("Wrong Activity",RiderMapActivity.class);

        //test empty destination
        solo.clickOnText("Enter the destination");
        solo.typeText(0, "West Edmonton Mall");
        solo.clickOnText("170 Street Northwest");

        //test after press the first confirm button
        solo.clickOnText("CONFIRM");

        assertTrue(solo.waitForText("End: 8770 170 St NW, Edmonton, AB T5T 3J7, Canada", 1, 2000));
        solo.clickOnText("Confirm");

        solo.clickOnText("CANCEL");

        //Go to order history and see there is no order containing address of west edmonton mall
        solo.clickOnView(solo.getView(R.id.bentoView));
        solo.assertCurrentActivity("Wrong Activity", Menu.class);

        solo.clickOnView(solo.getView(R.id.historyMenu));
        solo.assertCurrentActivity("Wrong Activity", OrderHistoryActivity.class);

        solo.clickOnView(solo.getView(R.id.history_order_refreshButton));

        assertFalse("the address was found", solo.searchText("8770 170 St NW, Edmonton, AB T5T 3J7, Canada", true));

    }

    @Test
    public void testConfirmCompleteButton () throws Exception{
        solo.enterText((EditText) solo.getView(R.id.Login_inputUsername), "DoNotDelete");
        solo.enterText((EditText) solo.getView(R.id.Login_inputPassword), "1234qwer");
        solo.clickOnButton("Login");
        solo.assertCurrentActivity("Wrong Activity",RiderMapActivity.class);

        /*//test empty destination
        solo.clickOnText("Enter the pickup location");
        solo.typeText(0, "Hub Mall");
        solo.clickOnText("Hub Mall"); */

        //test empty destination
        solo.clickOnText("Enter the destination");
        solo.typeText(0, "West Edmonton Mall");
        solo.clickOnText("170 Street Northwest");

        //test after press the first confirm button
        solo.clickOnText("CONFIRM");

        solo.clickOnText("Confirm");

        //assertTrue(solo.waitForText("Start: 9002 112 St NW, Edmonton, AB T6G 2C5, Canada", 1, 2000));
        assertTrue(solo.waitForText("End: 8770 170 St NW, Edmonton, AB T5T 3J7, Canada", 1, 2000));
        //assertTrue(solo.waitForText("Price: 20", 1, 2000));


        //go to fires store and check the order if is added
        /*
        FirebaseFirestore db;
        db = FirebaseFirestore.getInstance();
        DocumentReference userInfo = db.collection("Accounts").document("DoNotDelete");

        userInfo.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot doc = task.getResult();
                    List<String> orders = (List<String>) doc.get("order");
                    assertEquals(size(orders),2);

                    //get the current order
                    String orderIndex = orders.get(0);
                    FirebaseFirestore db;
                    db = FirebaseFirestore.getInstance();

                    DocumentReference userInfo2 = db.collection("Requests").document(orderIndex);
                    userInfo2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>(){
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                //check the destination
                            }
                        }
                    });

                }

            }
        }); */

        //use order history to check if the data upload

        //Go to order history and see there is no order containing address of west edmonton mall
        solo.clickOnView(solo.getView(R.id.bentoView));
        solo.assertCurrentActivity("Wrong Activity", Menu.class);

        solo.clickOnView(solo.getView(R.id.historyMenu));
        solo.assertCurrentActivity("Wrong Activity", OrderHistoryActivity.class);

        solo.clickOnView(solo.getView(R.id.history_order_refreshButton));

        //type before driver pick need to change the type
        assertTrue("the address was not found", solo.searchText("8770 170 St NW, Edmonton, AB T5T 3J7, Canada", true));
        assertTrue("type error", solo.searchText("active", true));

        solo.goBack();
        solo.goBack();

        solo.clickOnText("COMPLETE");

        solo.clickOnText("Confirm");

        //Go to order history and see there is no order containing address of west edmonton mall
        solo.clickOnView(solo.getView(R.id.bentoView));
        solo.assertCurrentActivity("Wrong Activity", Menu.class);

        solo.clickOnView(solo.getView(R.id.historyMenu));
        solo.assertCurrentActivity("Wrong Activity", OrderHistoryActivity.class);

        solo.clickOnView(solo.getView(R.id.history_order_refreshButton));

        //type before driver pick need to change the type need to change the type
        assertTrue("the address was not found", solo.searchText("8770 170 St NW, Edmonton, AB T5T 3J7, Canada", true));
        assertTrue("type error", solo.searchText("active", true));



    }



}
