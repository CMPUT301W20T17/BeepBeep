package com.example.beepbeep;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class driverConfirmDialog extends AppCompatDialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final String[] asd = getArguments().getStringArray("key");
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        final LinearLayout changeLayout = getActivity().findViewById(R.id.invis_linear);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Request Confirm")
                .setMessage("Accept this request?")
                .setPositiveButton("Sure", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getActivity(), "Request Accepted!", Toast.LENGTH_LONG).show();
                        changeLayout.setVisibility(View.INVISIBLE);
                        Map<String, Object> docData = new HashMap<>();
                        docData.put("DriverID",asd[1]);

                        db.collection("Requests")
                                .document(asd[0])
                                .update(docData);


                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        return builder.create();
    }
}
