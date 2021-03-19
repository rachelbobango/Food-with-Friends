package com.example.foodwithfriends;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public class PhotoUploadFragment extends Fragment {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    Button mAddPhotoButton;
    Button mTakePhotoButton;
    Button mNextButton;

    TextView mPermissionsMessage;
    Button mAccept;
    Button mDecline;
    ImageView mPhoto;
    private Uri imageUri;
    private URL imageUrl;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private static final String TAG = "photoUploadFragment";


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photo_upload, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAddPhotoButton = getView().findViewById(R.id.add_photo_button);
        mTakePhotoButton = getView().findViewById(R.id.take_photo_button);
        mNextButton = getView().findViewById(R.id.next_button);
        mPhoto = getView().findViewById(R.id.upload_image);

        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.permissions_popup);

        mAccept = dialog.findViewById(R.id.btnAccept);
        mDecline = dialog.findViewById(R.id.btnDecline);
        mPermissionsMessage = dialog.findViewById(R.id.permissionsMessage);
        //mPermissionsMessage.setText("Food With Friends would like to access your photos");
        mPermissionsMessage.setText(getResources().getString(R.string.access_photos_dialog));


        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        mAddPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //mPermissionsMessage.setText("Food With Friends would like to access your photos");
                mPermissionsMessage.setText(getResources().getString(R.string.access_photos_dialog));

                dialog.show();

                mAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ChoosePicture();
                        dialog.dismiss();
                    }
                });

                mDecline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });


        mTakePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //mPermissionsMessage.setText("Food With Friends would like to access your camera");
                mPermissionsMessage.setText(getResources().getString(R.string.access_camera_dialog));


                mAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TakePicture();
                        dialog.dismiss();
                    }
                });
                mDecline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(getActivity(), NavigationActivity.class);
                startActivity(in);

                // before navbar: switchToSettings();
            }
        });
    }

    private void ChoosePicture(){
        Intent choosePictureIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        try{
            startActivityForResult(choosePictureIntent, 1);
        }catch(ActivityNotFoundException e){
            //error
        }
    }

    private void TakePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, 2);
        } catch (ActivityNotFoundException e) {
            // error
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode== Activity.RESULT_OK && data!=null && data.getData()!=null){
            imageUri = data.getData();
            try {
                imageUrl = new URL(imageUri.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            mPhoto.setImageURI(imageUri);
            uploadPicture();
        }
        if(requestCode==2 && resultCode==Activity.RESULT_OK){
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mPhoto.setImageBitmap(imageBitmap);
            imageUri = getImageUri(this.getContext(), imageBitmap);
            //crashes here ^^^
            uploadPicture();
            //gs://foodwithfriends-b1b60.appspot.com/images/95b6b8be-9c88-4efb-a9a4-587c3fc32454
        }
    }

    private void uploadPicture(){
        // USELESS, Doesn't allow any relationship between user and profile picture final String randomKey = UUID.randomUUID().toString();
        final String key = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG, "current UUID" + key);
        StorageReference riversRef = storageReference.child("images/" + key);
        //
        riversRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //yay
                        String downloadUrl = riversRef.child("images/" + key).getDownloadUrl().toString();
                        final FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).update("profilePicRef", key, "downloadPicUrl", downloadUrl);
                        //riversRef.child()
                        Log.d(TAG, "downloadRef uploaded: " + downloadUrl);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        //error
                    }
                });
    }


    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    //Will be changed to SwitchToMain, once navbar is working
    private void switchToSettings() {

        FragmentManager fm = getParentFragmentManager();
        SettingsFragment fragment = new SettingsFragment();


        fm.beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }
}
