package com.example.beepbeep;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static android.content.Context.MODE_PRIVATE;

class OrderRecordManager {
    private Context context;
    private String username;
    private FirebaseFirestore db;

    // constructor assign context, get username, start db
    OrderRecordManager(Context context){
        this.context = context;

        // get username
        final SharedPreferences sharedPref = context.getSharedPreferences("identity", MODE_PRIVATE);
        this.username = sharedPref.getString("username", "");

        // initialize firebase
        this.db = FirebaseFirestore.getInstance();
    }

    void saveRecord(){
        final String filename = "OrderRecord.json"; // filename

        // write json beginning
        try (FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE)) {
            fos.write("{\"orders\":[".getBytes()); // json array start
        }catch (IOException e){
            Log.d("Error 100", Objects.requireNonNull(e.getMessage()));

        }

        // get all order associated with the user
        DocumentReference userInfo = db.collection("Accounts").document(this.username);
        userInfo.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    List<String> orders = (List<String>) document.get("order");
                    if(orders != null && !orders.isEmpty()){
                        for(int i = orders.size() - 1; i >= 0; i--){
                            String orderID = orders.get(i);
                            DocumentReference orderInfo = db.collection("Requests").document(orderID);
                            orderInfo.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()){
                                        DocumentSnapshot order = task.getResult();
                                        if(order != null && order.exists()){
                                            Order o = order.toObject(Order.class);
                                            try (FileOutputStream fos = context.openFileOutput(filename, Context.MODE_APPEND)){
                                                String object = String.format(Locale.CANADA,
                                                        "{\"Destination\":\"%s\",\"DriverID\":\"%s\",\"FinishTime\":\"%s\",\"PickUpPoint\":\"%s\",\"Price\":\"%f\",\"RiderID\":\"%s\",\"StartTime\":\"%s\",\"Type\":\"%s\"},",
                                                        o.getDestination().toString(), o.getDriverID(), o.getFinishTime(), o.getPickupPoint().toString(), o.getPrice(), o.getRiderID(), o.getStartTime() ,o.getType());
                                                fos.write(object.getBytes()); // write to file
                                            }catch (Exception e){
                                                Log.d("Error 101", Objects.requireNonNull(e.getMessage()));
                                            }
                                        }
                                    }else{
                                        Log.d("Error Getting Order Info from Requests","");
                                    }
                                }
                            });
                        }
                    }else{
                        Log.d("Error Getting Order Info from Account","");
                    }
                }else {
                    Log.d("Error Getting Order Info from Account", "");
                }
            }
        });
    }

    ArrayList<Order> getRecord(){
        JSONArray jsonArray = null;
        final String filename = "OrderRecord.json";
        try (FileInputStream fis = context.openFileInput(filename)){
            InputStreamReader inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8);
            StringBuilder stringBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                String line = reader.readLine(); // read a line
                while (line != null) {
                    stringBuilder.append(line); // add line to String
                    line = reader.readLine(); // read another line
                }
            } catch (IOException e) {
                Log.d("Error 105", Objects.requireNonNull(e.getMessage()));
            } finally { // construct string into JSONObject
                stringBuilder.append("{}]}");
                String contents = stringBuilder.toString();
                JSONObject jsonObject = new JSONObject(contents);
                jsonArray = jsonObject.getJSONArray("orders");
            }
        }catch (Exception e){
            Log.d("Error 102", Objects.requireNonNull(e.getMessage()));
        }

        ArrayList<Order> ret = new ArrayList<>();
        try{
            assert jsonArray != null;
            for(int i = 0; i < jsonArray.length()-1; ++i){
                JSONObject arrayObject = jsonArray.getJSONObject(i);
                String Destination = arrayObject.getString("Destination");
                GeoPoint d = toGeoPoint(Destination);
                String PickUpPoint = arrayObject.getString("PickUpPoint");
                GeoPoint p = toGeoPoint(PickUpPoint);
                Order o = new Order(
                        this.username,
                        arrayObject.getString("DriverID"),
                        arrayObject.getString("RiderID"),
                        arrayObject.getString("StartTime"),
                        arrayObject.getString("FinishTime"),
                        Double.parseDouble(arrayObject.getString("Price")),
                        p,
                        d,
                        arrayObject.getString("Type")
                );
                ret.add(o); // add the newly constructed record into records
            }
        }catch (Exception e){
            Log.d("Error 104", Objects.requireNonNull(e.getMessage()));
        }
        return ret;
    }

    private static GeoPoint toGeoPoint(String s){
        String pattern = "([-]?\\d*\\.\\d*)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(s);

        double lat = 0;
        double lon = 0;
        if (m.find()) {
            lat = Double.parseDouble(m.group());
        }
        if (m.find()){
            lon = Double.parseDouble(m.group());
        }
        GeoPoint p = new GeoPoint(lat, lon);
        return p;
    }

}
