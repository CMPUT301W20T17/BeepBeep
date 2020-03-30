package com.example.beepbeep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

/*
 Title: Edit  profile
 Author: Junqi Zou, Lyuyang Wang
 Date: 2020/03/06
 Last edited: 2020/03/12
*/

/**
 *Edit the user profile, which includes email and phone
 */

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";
    String email;
    FirebaseFirestore db;
    private ImageView imageView;
    private Uri filePath;
    FirebaseStorage storage;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Button saveButton = findViewById(R.id.save_button);
        Button cancelButton = findViewById(R.id.cancel_button);
        imageView = findViewById(R.id.profile_view_photo);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        //When Image is clicked make sure all permissions are satisfied and warn and prompt user for permission if it was previously disabled.
        /*
            Title: Checking Permission for read external storage
            Author: Jonathan Martins, Android Coding
            Date: 3/30/2020
            Last edited: 3/30/2020
            Availability: https://www.youtube.com/watch?v=AyhkpvQwFsI&t=606s
         */
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(EditProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        //Create AlertDialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                        builder.setTitle("Grant those permission");
                        builder.setMessage("Read storage");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(EditProfileActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
                            }
                        });
                        builder.setNegativeButton("Cancel", null);
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    } else {
                        ActivityCompat.requestPermissions(EditProfileActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
                    }
                }else {
                    //When permission are already granted
                    selectImage(EditProfileActivity.this);
                }
            }
        });

        Intent intent = getIntent();
        final String profileName = intent.getStringExtra("profile_name");
        db = FirebaseFirestore.getInstance();

        //when finished edit, set email and phone to the data stored in fireStore
        DocumentReference userRef = db.collection("Accounts").document(profileName);
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    EditText phoneEditText = findViewById(R.id.phone_editText);
                    EditText emailEditText = findViewById(R.id.email_editText);
                    DocumentSnapshot doc = task.getResult();
                    email = (doc.get("email")).toString();
                    String phone = (doc.get("phone")).toString();
                    emailEditText.setText(email);
                    phoneEditText.setText(phone);
                    //retrieves the profile image from the database so that the editprofile imageview contains the photo.
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageReference = storage.getReference().child("profileImages/"+ email);
                    try{
                        final File file = File.createTempFile("image","jpg");
                        storageReference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        //when click save button, the app should check if the new input is valid or not
        //if the new input is valid, then fireStore, hared preference and view profile page should update
        //if the new input is invalid, the error shows up and user can reedit
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText phoneEditText = findViewById(R.id.phone_editText);
                EditText emailEditText = findViewById(R.id.email_editText);
                final String phoneEdit = phoneEditText.getText().toString();
                final String emailEdit = emailEditText.getText().toString();

                //check if the input is valid
                boolean phoneValid = Signup.validPhone(phoneEdit);
                final boolean emailValid = Signup.validEmail(emailEdit);

                //update phone and email to fireStore
                if (phoneValid && emailValid){
                    DocumentReference userRef = db.collection("Accounts").document(profileName);
                    userRef.update("phone", phoneEdit,
                            "email", emailEdit).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                finish();
                            }
                        }
                    });

                    final SharedPreferences sharedPref = EditProfileActivity.this.getSharedPreferences("identity", Context.MODE_PRIVATE);

                    sharedPref.edit().putString("email", emailEdit).apply();
                    sharedPref.edit().putString("phone", phoneEdit).apply();
                }
                else{
                    //input invalid, shows error message
                    //invalid input email
                    if (!emailValid){
                        String message = "Your email is not valid.";
                        Toast.makeText(EditProfileActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                    //invalid input phone
                    if(!phoneValid){
                        String message = "Your phone is not valid.";
                        Toast.makeText(EditProfileActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
            }
        });
    }

/*
 Title: select image for profile picture
 Author: Jonathan Martins, Hasangi Thathsarani
 Date: 2020/03/25
 Last edited: 2020/03/25
 Availability: https://github.com/Cassendra4/ImageCaptureExample
*/
    /**
     * selectImage enables the user to choose how he/she want to upload the picture.
     */

    private void selectImage(Context context) {
        final CharSequence[] options = {"Choose from Gallery", "Cancel"};
        //create alertdialog to ask whether user wants to choose their profile picture from their gallery or cancel
        //Currently in version 1.0, in version 2.0 implementing take picture.
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose your profile picture");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Choose from Gallery")) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickPhoto.setType("image/*");
                    startActivityForResult(pickPhoto, 1);//one can be replaced with any action code

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    //What happens when user clicks one of the options
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                filePath = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                    imageView.setImageBitmap(bitmap);
                    uploadImage();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
/*
 Title: upload/select image for profile picture
 Author: Jonathan Martins, EDMT Dev
 Date: 2020/03/28
 Last edited: 2020/03/30
 Availability: https://www.youtube.com/watch?v=h62bcMwahTU&t=823s
*/
    private void uploadImage(){
        if(filePath != null) {
            //ref is the path that follows firestorage.
            StorageReference ref = storageReference.child("profileImages/" + email);
            //putfile allows you to put the picture into the path of the ref.
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(EditProfileActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditProfileActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(EditProfileActivity.this,"In Progress", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}