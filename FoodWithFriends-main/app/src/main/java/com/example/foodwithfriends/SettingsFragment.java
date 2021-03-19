package com.example.foodwithfriends;

import android.app.Dialog;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import static com.google.firebase.firestore.FieldValue.delete;

public class SettingsFragment extends Fragment {

    private Button mSaveButton, mBtnDelete;
    private EditText mEditBio, mEditName;
    private RadioGroup radioSex;
    private RadioButton radioSexButton;

    private static final String TAG = "settingsFragment";


    public SettingsFragment() {
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

        radioSex = v.findViewById(R.id.radioSex);
        radioSex.clearCheck();

        mSaveButton = v.findViewById(R.id.save);
        mBtnDelete = v.findViewById(R.id.btnDelete);

        final Dialog deleteDialog = new Dialog(getActivity());

        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = mEditName.getText().toString();
                String bio = mEditBio.getText().toString();

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

        FragmentManager fm = getParentFragmentManager();
        LoginFragment fragment = new LoginFragment();

        fm.beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }

}
