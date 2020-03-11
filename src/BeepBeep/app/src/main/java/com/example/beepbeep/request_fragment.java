package com.example.beepbeep;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import static android.content.Context.MODE_PRIVATE;

public class request_fragment extends DialogFragment {
    private TextView show_start;
    private TextView show_end;
    private TextView show_price;
    //set filestore related variables
    private FirebaseFirestore db;
    final String TAG = "Requests";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInsatanceState){
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.request_confirm,null);
        show_start = view.findViewById(R.id.show_pickup);
        show_end = view.findViewById(R.id.show_destination);
        show_price = view.findViewById(R.id.show_price);
        //get shared preference and user now
        final SharedPreferences sharedPref = this.getActivity().getSharedPreferences("identity", MODE_PRIVATE);
        final String username = sharedPref.getString("username", "");
        //get firesotre information about the request
        //TODO: firestore structure change?
        //final DocumentReference docIdRef = db.collection("Requests").document(username);
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference RequestIdInf = db.collection("Requests").document("id123456");
        RequestIdInf.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        final String destination = document.get("Destination").toString();
                        final String pickup = document.get("PickUpPoint").toString();
                        final String price = document.get("Price").toString();
                        show_start.setText(pickup);
                        show_end.setText(destination);
                        show_price.setText(price);

                    }
                    else{
                        Log.d(TAG, "No such document");
                    }
                }
                else{
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder
                .setView(view)
                .setTitle("Request information confirm")
                .setPositiveButton("Confirm",null)
                .setNegativeButton("Cancel",null)
                .create();
    }


}
