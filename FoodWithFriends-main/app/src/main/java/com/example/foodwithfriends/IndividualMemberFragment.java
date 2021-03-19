package com.example.foodwithfriends;

import android.content.Context;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static com.bumptech.glide.load.engine.DiskCacheStrategy.AUTOMATIC;

public class IndividualMemberFragment extends Fragment {
    final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public TextView nameText;
    public TextView genderText;
    public TextView bioText;
    public TextView contactText;
    public ImageView memberImage;

    String name;
    String gender;
    String bio;
    String contact;
    String userId;

    public IndividualMemberFragment(String name,  String gender, String bio, String contact, String userId) {
        this.name = name;
        this.gender = gender;
        this.bio = bio;
        this.contact= contact;
        this.userId = userId;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_individual_member, container, false);

        nameText = v.findViewById(R.id.member_name);
        genderText = v.findViewById(R.id.member_gender);
        bioText = v.findViewById(R.id.member_bio);
        contactText = v.findViewById(R.id.member_contact);

        memberImage = v.findViewById(R.id.member_image);
        
        memberImage.setImageResource(R.drawable.profpic);

        FirebaseStorage storage = FirebaseStorage.getInstance();

        Task<Uri> uriTask= storage.getReference("images/"+userId).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri uri = task.getResult();

                    RequestOptions myOptions = new RequestOptions()
                            .centerCrop();
                    Glide.with(IndividualMemberFragment.this)
                            .load(uri)
                            .placeholder(R.drawable.profpic)
                            .into(memberImage);
                }

            }
        });

        nameText.setText(name);
        genderText.setText(gender);
        bioText.setText(bio);
        contactText.setText(contact);

        return v;
    }

}
