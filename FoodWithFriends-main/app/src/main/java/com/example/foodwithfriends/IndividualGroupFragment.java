package com.example.foodwithfriends;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class IndividualGroupFragment extends Fragment {

    final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public TextView nameText;
    public TextView descText;

    String name;
    String desc;
    String id;

    private Button mJoinButton;

    public IndividualGroupFragment(String name, String desc, String id) {
        this.name = name;
        this.desc = desc;
        this.id = id;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_individual_group, container, false);

        nameText = v.findViewById(R.id.group_name);
        descText = v.findViewById(R.id.group_description);
        mJoinButton = v.findViewById(R.id.join_button);

        nameText.setText(name);
        descText.setText(desc);

        mJoinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DocumentReference groupRef = db.collection("groups").document(id);
                DocumentReference userRef = db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());

                groupRef.update("users", FieldValue.arrayUnion(FirebaseAuth.getInstance().getCurrentUser().getUid()));
                userRef.update("groups", FieldValue.arrayUnion(id));
            }
        });

        return v;
    }
}
