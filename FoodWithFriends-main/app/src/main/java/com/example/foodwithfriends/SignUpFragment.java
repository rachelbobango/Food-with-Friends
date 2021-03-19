package com.example.foodwithfriends;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpFragment extends Fragment{

    Button mSaveButton;
    EditText mEditName;
    EditText mEditBio;
    private RadioGroup radioSex;
    private RadioButton radioSexButton;

    private static final String TAG = "signUpFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_signup, container, false);

        mSaveButton = v.findViewById(R.id.saveButton);
        mEditName = v.findViewById(R.id.editName);
        mEditBio = v.findViewById(R.id.editBio);

        radioSex = v.findViewById(R.id.radioSex);
        radioSex.clearCheck();

        mSaveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String name = mEditName.getText().toString();
                String bio = mEditBio.getText().toString();

                int radioId = radioSex.getCheckedRadioButtonId();
                radioSexButton = getView().findViewById(radioId);

                String gender = radioSexButton.getText().toString();
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                db.collection("users")
                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid()).update(
                        "name", name, "bio", bio, "gender", gender);

                switchToPhotos();
            }
        });
    return v;


    }

    private void switchToPhotos() {

        FragmentManager fm = getParentFragmentManager();
        PhotoUploadFragment fragment = new PhotoUploadFragment();

        fm.beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }

}