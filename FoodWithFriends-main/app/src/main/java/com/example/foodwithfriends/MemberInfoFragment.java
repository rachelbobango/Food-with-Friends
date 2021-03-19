package com.example.foodwithfriends;

import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;


import java.util.List;

public class MemberInfoFragment extends Fragment {
    private static final String TAG = "error";

    final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public TextView nameText;
    public TextView bioText;
    private Button mBackButton;
    private Button mDeleteButton;
    private Button mLeaveButton;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    List<String> members;
    String id;

    public MemberInfoFragment(String id) {
        this.id = id;
    }

    //@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_member_info, container, false);

        nameText = v.findViewById(R.id.member_name);
        bioText = v.findViewById(R.id.member_bio);
        mBackButton = v.findViewById(R.id.backBtn);
        mDeleteButton = v.findViewById(R.id.deleteBtn);
        mLeaveButton = v.findViewById(R.id.leaveBtn);

        mDeleteButton.setVisibility(View.GONE);
        Log.d(TAG, "4");
        Log.d(TAG, id);
        db.collection("groups")
                .document(id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "5");

                            DocumentSnapshot document = task.getResult();
                            members = (List<String>) document.get("users");
                            Log.d(TAG, document.getId() + " => " + document.getData());
                            Log.d(TAG, members.toString());

                            String ownerId = (String)document.get("owner");
                            if(ownerId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                mDeleteButton.setVisibility(View.VISIBLE);
                                mLeaveButton.setVisibility(View.GONE);
                            }
                            Log.d(TAG, "6");

                            db.collection("users")
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();


                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "7");

                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    Log.d(TAG, document.getId() + " => " + document.getData());

                                                    String userId = document.getId();
                                                    if(members.contains(userId)){
                                                        Log.d(TAG, "8");

                                                        //String name ="Name: "+ (String)document.get("name");
                                                        String name = getResources().getString(R.string.name) +": " + (String)document.get("name");
                                                        String bio = getResources().getString(R.string.user_bio) + ": " +(String)document.get("bio");
                                                        String contact = getResources().getString(R.string.contact)+ ": " +(String)document.get("phoneNumber");
                                                        String gender = getResources().getString(R.string.gender) + ": " +(String)document.get("gender");

                                                        Log.d(TAG, name+ " "+bio);
                                                        Fragment newFragment = new IndividualMemberFragment(name, gender, bio, contact, userId);
                                                        ft.add(R.id.group_container, newFragment);
                                                    }
                                                }
                                                Log.d(TAG, "9");

                                                Log.d(TAG, "commit");
                                                ft.commit();

                                            } else {
                                                Log.d(TAG, "Error getting documents: ", task.getException());
                                            }
                                        }
                                    });

                        } else {
                        }

                    }

                });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchToGroups();
            }
        });

        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.collection("users")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Log.d(TAG, document.getId() + " => " + document.getData());

                                        String userId = document.getId();
                                        if(members.contains(userId)){
                                            DocumentReference userRef = db.collection("users").document(userId);
                                            userRef.update("groups", FieldValue.arrayRemove(id));
                                        }
                                    }

                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });

                db.collection("groups").document(id).delete();
                switchToGroups();
            }
        });

        mLeaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.collection("groups")
                        .document(id)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();

                                    String userId = (String)FirebaseAuth.getInstance().getCurrentUser().getUid();

                                    DocumentReference groupRef = db.collection("groups").document(id);
                                    groupRef.update("users", FieldValue.arrayRemove(userId));

                                    DocumentReference userRef = db.collection("users").document(userId);
                                    userRef.update("groups", FieldValue.arrayRemove(id));

                                } else {
                                }

                            }

                        });
                switchToGroups();
            }
        });

        return v;
    }
    private void switchToGroups() {

        FragmentManager fm = getParentFragmentManager();
        GroupsNavFragment fragment = new GroupsNavFragment();

        fm.beginTransaction().replace(R.id.your_groups_container, fragment).commit();
    }
}
