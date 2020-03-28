package com.example.beepbeep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import io.opencensus.tags.Tag;

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

        FirebaseFirestore db;

        private ImageView imageView;
        private static final String TAG = "EditProfileActivity";
        String PROFILE_IMAGE_URL = null;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_edit_profile);
            Button saveButton = findViewById(R.id.save_button);
            Button cancelButton = findViewById(R.id.cancel_button);
            imageView = (ImageView) findViewById(R.id.profile_view_photo);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectImage(EditProfileActivity.this);
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
                        String email = (doc.get("email")).toString();
                        String phone = (doc.get("phone")).toString();
                        emailEditText.setText(email);
                        phoneEditText.setText(phone);
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
                    if (phoneValid && emailValid) {
                        DocumentReference userRef = db.collection("Accounts").document(profileName);
                        userRef.update("phone", phoneEdit,
                                "email", emailEdit).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    finish();
                                }
                            }
                        });

                        final SharedPreferences sharedPref = EditProfileActivity.this.getSharedPreferences("identity", Context.MODE_PRIVATE);

                        sharedPref.edit().putString("email", emailEdit).apply();
                        sharedPref.edit().putString("phone", phoneEdit).apply();
                    } else {
                        //input invalid, shows error message
                        //invalid input email
                        if (!emailValid) {
                            String message = "Your email is not valid.";
                            Toast.makeText(EditProfileActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                        //invalid input phone
                        if (!phoneValid) {
                            String message = "Your phone is not valid.";
                            Toast.makeText(EditProfileActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
            /*
 Title: upload/select image for profile picture
 Author: Jonathan Martins, Hasangi Thathsarani
 Date: 2020/03/25
 Last edited: 2020/03/25
 Availability: https://github.com/Cassendra4/ImageCaptureExample
*/
/**
 * selectImage enables the user to choose how he/she want to upload the picture.
 */
            private void selectImage(Context context) {
                final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Choose picture");

                builder.setItems(options, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int item) {

                        if (options[item].equals("Take Photo")) {
                            Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            if (takePicture.resolveActivity(getPackageManager())!= null) {
                                startActivityForResult(takePicture, 0);
                            }
                        } else if (options[item].equals("Choose from Gallery")) {
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(pickPhoto, 1);//one can be replaced with any action code

                        } else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            }


            @Override
            protected void onActivityResult(int requestCode, int resultCode, Intent data) {
                super.onActivityResult(requestCode, resultCode, data);
                if (resultCode != RESULT_CANCELED) {
                    switch (requestCode) {
                        case 0:
                            if (resultCode == RESULT_OK && data != null) {
                                Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                                imageView.setImageBitmap(selectedImage);
                                handleUpload(selectedImage);

                            }
                            break;
                        case 1:
                            if (resultCode == RESULT_OK && data != null) {
                                Uri selectedImage = data.getData();
                                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                                if (selectedImage != null) {
                                    Cursor cursor = getContentResolver().query(selectedImage,
                                            filePathColumn, null, null, null);
                                    if (cursor != null) {
                                        cursor.moveToFirst();
                                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                        String picturePath = cursor.getString(columnIndex);
                                        imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                                        handleUpload(BitmapFactory.decodeFile(picturePath));
                                        cursor.close();
                                    }
                                }

                            }
                            break;
                    }
                }
            }
            private void handleUpload(Bitmap bitmap){
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                String uid = email;
                final StorageReference reference = FirebaseStorage.getInstance().getReference().child("profileImages").child(uid + ".jpeg");
                reference.putBytes(baos.toByteArray())
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                getDownloadUrl(reference);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "onFailure: ",e.getCause() );
                            }
                        });
        }
        private void getDownloadUrl(StorageReference reference){
            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Log.d(TAG, "onSuccess: " + uri);
                    setUserProfileUrl(uri);
                }
            });
        }

        private void setUserProfileUrl(Uri uri) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                        .setPhotoUri(uri)
                        .build();
                user.updateProfile(request)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(EditProfileActivity.this, "Updated successfully", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EditProfileActivity.this, "Profile image failed...", Toast.LENGTH_SHORT).show();
                            }
                        });
        }
    }


