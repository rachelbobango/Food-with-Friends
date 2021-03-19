package com.example.foodwithfriends;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.LocaleList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.Locale;

public class GroupsNavFragment extends Fragment {

    private static final String TAG = "groupFragment";
    List<String> groups;
    Locale defaultInputText;

    public GroupsNavFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_groups_nav, container, false);

        super.onCreate(savedInstanceState);

        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();

        db.collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            groups = (List<String>) document.get("groups");
                            Log.d(TAG, document.getId() + " => " + document.getData());
                            if (groups != null) {
                                Log.d(TAG, groups.toString());

                                db.collection("groups")
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                                defaultInputText = getResources().getConfiguration().locale;
                                                Log.d(TAG, defaultInputText.toString());
                                                //Fragment firstFragment = new CreateText("Your Groups");
                                                Fragment firstFragment = new CreateText(getResources().getString(R.string.your_groups));
                                                ft.add(R.id.your_groups_container, firstFragment);
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        Log.d(TAG, document.getId() + " => " + document.getData());

                                                        String id = document.getId();
                                                        if (groups.contains(id)) {
                                                            //String name = "Group: " + (String) document.get("groupName");
                                                            String name = getResources().getString(R.string.group) + " " + (String) document.get("groupName");
                                                            //String desc = "Description: " + (String) document.get("groupDescription");
                                                            String desc = getResources().getString(R.string.description) + " " + (String) document.get("groupDescription");

                                                            Log.d(TAG, name + " " + desc);
                                                            Fragment newFragment = new YourIndividualGroupFragment(name, desc, id);
                                                            ft.add(R.id.your_groups_container, newFragment);
                                                        }
                                                    }
                                                    Log.d(TAG, "commit");
                                                    ft.commit();

                                                } else {
                                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                                }
                                            }
                                        });
                            }
                        } else {
                        }

                    }

                });

        return v;
    }
}