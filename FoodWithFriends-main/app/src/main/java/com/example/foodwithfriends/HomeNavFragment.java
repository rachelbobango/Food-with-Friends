package com.example.foodwithfriends;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomePageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeNavFragment extends Fragment {

    EditText groupName;
    EditText groupDescription;
    Button save;

    private static final String TAG = "homeFragment";

    public HomeNavFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home_page, container, false);

        TextView mLocationMsg = v.findViewById(R.id.locationMsg);
        mLocationMsg.setVisibility(View.GONE);

        super.onCreate(savedInstanceState);

        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(FirebaseAuth.getInstance()
                .getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        GeoPoint userLocation = (GeoPoint)document.get("location");

                        if (userLocation != null) {
                            long maxDistance = (long) document.get("maxDistance");
                            displayGroups(userLocation, maxDistance);
                        } else {
                            mLocationMsg.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        });



        return v;
    }

    private void displayGroups(GeoPoint userLocation, long maxDistance) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("groups")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();

                        Fragment firstFragment = new CreateGroupFragment();
                        ft.add(R.id.group_container, firstFragment);

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());

                                GeoPoint groupLocation = (GeoPoint)document.get("location");
                                int distanceToGroup = getDistance(userLocation, groupLocation);
                                if (distanceToGroup > maxDistance) continue;

                                String id = document.getId();
                                //String name ="Group: "+ (String)document.get("groupName");
                                String name = getResources().getString(R.string.group) + " " +  (String)document.get("groupName") + " (" + distanceToGroup + " mi)";
                                String desc = getResources().getString(R.string.description)+ " " +(String)document.get("groupDescription");

                                Fragment newFragment = new IndividualGroupFragment(name, desc, id);

                                ft.add(R.id.group_container, newFragment);
                            }
                            ft.commit();

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    public int getDistance(GeoPoint userLocation, GeoPoint groupLocation) {
        double userLat = userLocation.getLatitude();
        double userLon = userLocation.getLongitude();

        double groupLat = groupLocation.getLatitude();
        double groupLon = groupLocation.getLongitude();

        if ((userLat == groupLat) && (userLon == groupLon)) return 1;

        double theta = userLon - groupLon;
        double dist = Math.sin(Math.toRadians(userLat)) * Math.sin(Math.toRadians(groupLat)) + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(groupLat)) * Math.cos(Math.toRadians(theta));
        dist = Math.acos(dist);
        dist = Math.toDegrees(dist);
        dist = dist * 60 * 1.1515;

        if ((int)dist < 1) return 1;

        return (int)dist;
    }

}