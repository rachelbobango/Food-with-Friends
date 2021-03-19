package com.example.foodwithfriends;


import android.app.Activity;
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
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class LoginFragment extends Fragment {

    final long INITIAL_DISTANCE = 20;

    private Button mBtnLogin;
    private EditText mEditPhoneNumber;
    public String phoneNumber;

    private static final String TAG = "loginFragment";

    public LoginFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_login, container, false);

        super.onCreate(savedInstanceState);

        mBtnLogin = v.findViewById(R.id.btnLogin);
        mEditPhoneNumber = v.findViewById(R.id.editPhoneNumber);
        final Dialog dialog = new Dialog(getActivity());


        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String baseNumber = mEditPhoneNumber.getText().toString();
                phoneNumber = getPhoneNumber(baseNumber);
                if (phoneNumber.isEmpty()) {
                    Toast.makeText(getActivity(), "Enter your phone number", Toast.LENGTH_SHORT).show();
                } else {
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber, 60, TimeUnit.SECONDS, getActivity(), new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                @Override
                                public void onVerificationCompleted(PhoneAuthCredential credential) {
                                    signInUser(credential, dialog);
                                }

                                @Override
                                public void onVerificationFailed(FirebaseException e) {
                                    Log.w(TAG, "onVerificationFailed: " + e.getLocalizedMessage());
                                }

                                @Override
                                public void onCodeSent(final String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                                    super.onCodeSent(verificationId, token);

                                    dialog.setContentView(R.layout.verify_popup);

                                    final EditText editVerification = dialog.findViewById(R.id.editVerification);
                                    Button btnVerify = dialog.findViewById(R.id.btnVerify);
                                    btnVerify.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            String verificationCode = editVerification.getText().toString();
                                            if (verificationId.isEmpty()) return;

                                            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, verificationCode);
                                            signInUser(credential, dialog);
                                        }
                                    });
                                    dialog.show();
                                    //Might fix the notification dismissal bug.
                                    dialog.setCancelable(false);
                                }
                            });
                }
            }

        });

        return v;
    }

    public String getPhoneNumber(String baseNumber){
        String fullNumber = "+1"+baseNumber;
        return fullNumber;
    }

    private void signInUser(PhoneAuthCredential credential, final Dialog dialog) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {

                            boolean isNew = task.getResult().getAdditionalUserInfo().isNewUser();
                            Log.d("MyTag", "onComplete: " + (isNew ? "new user" : "old user"));

                            FirebaseFirestore db = FirebaseFirestore.getInstance();

                            DocumentReference docRef = db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());

                            if(isNew){
                                User user = new User();
                                db.collection("users")
                                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .set(user)
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "Error adding document", e);
                                            }
                                        });
                                db.collection("users").document(FirebaseAuth.getInstance()
                                        .getCurrentUser().getUid()).update("phoneNumber", phoneNumber);
                                dialog.dismiss();
                                docRef.update("groups", null);
                                docRef.update("maxDistance", INITIAL_DISTANCE);
                                docRef.update("location", null);
                                switchToSignUp();
                            }else {
                                dialog.dismiss();

                                Source source = Source.CACHE;
                                docRef.get(/*source*/).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()){
                                            DocumentSnapshot document = task.getResult();

                                            //Fetch name field from document
                                            String name = document.getString("name");

                                            //If name field is null, redirect user to signUp fragment to complete their profile
                                            if(name == null){
                                                switchToSignUp();
                                            } else {

                                                Intent navigation = new Intent(getView().getContext(), NavigationActivity.class);
                                                getView().getContext().startActivity(navigation);
                                            }
                                        }
                                    }
                                });

                            }
                        } else{
                            Log.d(TAG, "onComplete:" + task.getException().getLocalizedMessage());
                        }

                    }
                });

    }

    private void switchToSignUp() {

        FragmentManager fm = getParentFragmentManager();
        SignUpFragment fragment = new SignUpFragment();

        fm.beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }
}