package com.example.foodwithfriends;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

public class YourIndividualGroupFragment extends Fragment{
    final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public TextView nameText;
    public TextView descText;

    String TAG = "error";

    String name;
    String desc;
    String id;

    private Button mInfoButton;

    public YourIndividualGroupFragment(String name, String desc, String id) {
        this.name = name;
        this.desc = desc;
        this.id= id;
    }

    //@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_individual_group, container, false);

        nameText = v.findViewById(R.id.group_name);
        descText = v.findViewById(R.id.group_description);
        mInfoButton = v.findViewById(R.id.join_button);

        nameText.setText(name);
        descText.setText(desc);

        //mInfoButton.setText("member info");
        mInfoButton.setText(getResources().getString(R.string.member_information));

        preloadUserImages();

        mInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switchToInfo();
            }
        });

        return v;
    }

    private void preloadUserImages(){

        FirebaseStorage storage = FirebaseStorage.getInstance();

        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Task<Uri> uriTask= storage.getReference("images/"+document.getId()).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            Uri uri = task.getResult();

                                            RequestOptions myOptions = new RequestOptions()
                                                    .centerCrop();
                                            /*Glide.with(YourIndividualGroupFragment.this)
                                                    .load(uri)
                                                    .preload();*/
                                        }

                                    }
                                });


                            }

                        } else {

                        }
                    }
                });
    }

    private void switchToInfo() {

        Log.d(TAG, "1");
        FragmentManager fm = getParentFragmentManager();
        MemberInfoFragment fragment = new MemberInfoFragment(id);
        Log.d(TAG, "2");

        fm.beginTransaction().replace(R.id.your_groups_container, fragment).commit();
        Log.d(TAG, "3");

    }
}
