package nl.tue.appdev.studie;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Source;

import java.util.HashMap;

import nl.tue.appdev.studie.databinding.FragmentFirstBinding;

public class MembersFragment extends Fragment {

    private static final String TAG = "MembersFragment";
    private FragmentFirstBinding binding;

    private String groupId;

    private LinearLayout buttonContainer;

    public void updateGroupId(String id) {
        this.groupId = id;
    }

    public void addMember(String name) {
        // Create a button and set attributes
        Button button = new Button(getContext());
        button.setText(name);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                150
        );
        params.setMargins(0, 0, 0, 20);
        button.setLayoutParams(params);
        button.setTextColor(Color.WHITE);
        button.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        button.setPadding(30, 0, 0, 0);
        button.setAllCaps(false);
        button.setTextSize(17);
        button.setSingleLine(true);
        button.setEllipsize(TextUtils.TruncateAt.END);

        // Set background
        Drawable background = ContextCompat.getDrawable(getContext(), R.drawable.button_simple);
        button.setBackground(background);

        // Add button to view
        buttonContainer.addView(button);
    }

    public void displayMembers() {
        // Remove the old set of buttons
        buttonContainer.removeAllViews();

        if (groupId != null) {
            Log.d(TAG, groupId);
        }

        // Get user information
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
            .get(Source.SERVER)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, "Document ID: " + document.getId());
                        String name = (String) document.getData().get("name");
                        HashMap<String, String> groups = (HashMap<String, String>) document.getData().get("groups");
                        Log.d(TAG, "Name: " + name + "; Groups: " + groups);

                        if (groups.containsKey(groupId)) {
                            addMember(name);
                        }
                    }
                } else {
                    Log.e(TAG, "Error getting documents", task.getException());
                }
        });

        /*

        // Generate a list of buttons for the groups the user can join
        for (String group_name : groupNames) {
            // Get group ID and name from the entry in the hashmap
            String key = "default";

            for (Map.Entry<String, String> item : groups.entrySet()) {
                if (item.getValue().equals(group_name)) {
                    key = item.getKey();
                }
            }
            final String group_id = key;

            // Only show a button if it matches the query
            if (group_name.toLowerCase().contains(query.toLowerCase())) {

                // Create a button and set attributes
                Button button = new Button(getContext());
                button.setText(group_name);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        150
                );
                params.setMargins(0, 0, 0, 20);
                button.setLayoutParams(params);
                button.setTextColor(Color.WHITE);
                button.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                button.setPadding(30, 0, 0, 0);
                button.setAllCaps(false);
                button.setTextSize(17);
                button.setSingleLine(true);
                button.setEllipsize(TextUtils.TruncateAt.END);

                // Set background
                Drawable background = ContextCompat.getDrawable(getContext(), R.drawable.button_simple);
                button.setBackground(background);

                // Add button to view
                buttonContainer.addView(button);
            }
        }
        */

    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_members, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        buttonContainer = view.findViewById(R.id.members_container);
        displayMembers();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
