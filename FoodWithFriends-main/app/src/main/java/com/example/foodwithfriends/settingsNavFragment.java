package com.example.foodwithfriends;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import static com.google.firebase.firestore.FieldValue.delete;


public class settingsNavFragment extends Fragment {

    private SeekBar mSeekBarDistance;
    private Button mSaveButton, mBtnDelete;
    private EditText mEditBio, mEditName;
    private TextView mUserName, mUserBio, mDistance;
    private RadioGroup radioSex;
    private RadioButton radioSexButton;

    private static final String TAG = "settingsFragment";


    public settingsNavFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        super.onCreate(savedInstanceState);

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        mEditName = v.findViewById(R.id.name);
        mEditBio = v.findViewById(R.id.bio);

        mUserName = v.findViewById(R.id.userName);
        mUserBio = v.findViewById(R.id.userBio);
        mDistance = v.findViewById(R.id.distanceTextView);

        radioSex = v.findViewById(R.id.radioSex);
        radioSex.clearCheck();

        mSaveButton = v.findViewById(R.id.save);
        mBtnDelete = v.findViewById(R.id.btnDelete);

        mSeekBarDistance = v.findViewById(R.id.seekBarDistance);

        final Dialog deleteDialog = new Dialog(getActivity());

        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        DocumentReference docRef = db.collection("users").document(FirebaseAuth.getInstance()
                .getCurrentUser().getUid());
        docRef.get(Source.CACHE).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        final String name = (String)document.get("name");
                        final String bio = (String)document.get("bio");
                        final long initialDistance = (long)document.get("maxDistance");

                        mUserName.setText(name);
                        mUserBio.setText(bio);
                        //getResources().getString(R.string.gender)
                        mDistance.setText(getResources().getString(R.string.max_distance) +  " " + String.valueOf(initialDistance) + " mi");
                        mSeekBarDistance.setProgress((int)initialDistance);
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        mSeekBarDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mDistance.setText(getResources().getString(R.string.max_distance) + " " + String.valueOf(progress) + " mi");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = mEditName.getText().toString();
                String bio = mEditBio.getText().toString();
                long maxDistance = (long)mSeekBarDistance.getProgress();

                if(name!=null && !name.equals("")){
                    db.collection("users").document(FirebaseAuth.getInstance()
                            .getCurrentUser().getUid()).update("name", name);
                }
                if(bio!=null && !bio.equals("")){
                    db.collection("users").document(FirebaseAuth.getInstance()
                            .getCurrentUser().getUid()).update("bio", bio);
                }
                int radioId = radioSex.getCheckedRadioButtonId();
                if(radioId!=-1){
                    radioSexButton = getView().findViewById(radioId);

                    String gender = radioSexButton.getText().toString();

                    db.collection("users").document(FirebaseAuth.getInstance()
                            .getCurrentUser().getUid()).update("gender", gender);
                }

                db.collection("users").document(FirebaseAuth.getInstance()
                        .getCurrentUser().getUid()).update("maxDistance", maxDistance);
            }
        });


        mBtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).delete();

                currentUser.delete()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "User Account Deleted.");
                                }
                            }
                        });

                //Take user to login screen once they have deleted the account
                switchToLogin();

            }


        });
        return v;
    }


    private void switchToLogin() {

        // Take user back to login correctly when delete button hit from settings nav fragment.
        Intent in = new Intent(getActivity(), MainActivity.class);
        startActivity(in);

    }

}

