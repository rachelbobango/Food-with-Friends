package com.example.foodwithfriends;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import org.w3c.dom.Document;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CreateGroupFragment extends Fragment {

    EditText groupName;
    EditText groupDescription;
    Button save;

    public CreateGroupFragment( ) {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_create_group, container, false);

        groupName = v.findViewById(R.id.group_name);
        groupDescription = v.findViewById(R.id.group_description);

        save = v.findViewById(R.id.save);
        final FirebaseFirestore db = FirebaseFirestore.getInstance();


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                db.collection("users").document(FirebaseAuth.getInstance()
                        .getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String name = groupName.getText().toString();
                                String desc = groupDescription.getText().toString();
                                Map<String, Object> groupInfo = new HashMap<>();
                                groupInfo.put("groupName", name);
                                groupInfo.put("groupDescription", desc);
                                groupInfo.put("owner", FirebaseAuth.getInstance()
                                        .getCurrentUser().getUid());
                                groupInfo.put("users", Arrays.asList(FirebaseAuth.getInstance()
                                        .getCurrentUser().getUid()));
                                groupInfo.put("location", (GeoPoint)document.get("location"));

                                DocumentReference newGroupRef = db.collection("groups").document();
                                newGroupRef.set(groupInfo);

                                DocumentReference userRef = db.collection("users").document(FirebaseAuth.getInstance()
                                        .getCurrentUser().getUid());

                                userRef.update("groups", FieldValue.arrayUnion(newGroupRef.getId()));


                            }
                        }
                    }
                });
            }
        });

        return v;
    }

}
