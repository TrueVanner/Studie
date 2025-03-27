package nl.tue.appdev.studie;

import android.content.Intent;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import nl.tue.appdev.studie.databinding.FragmentFirstBinding;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class JoinGroupviewFragment extends Fragment {

    private static final String TAG = "JoinGroupviewFragment";
    private FragmentFirstBinding binding;

    private FirebaseAuth mAuth;
    private HashMap<String, String> groups = new HashMap<String, String>();
    private String query = "";

    private LinearLayout buttonContainer;

    public void updateGroups(HashMap<String, String> g) {
        groups = g;
        Log.d(TAG, String.valueOf(g));
    }

    public void updateQuery(String q) {
        query = q;
        Log.d(TAG, q);
        displayGroups();
    }

    public void displayGroups() {
        // Remove the old set of buttons
        buttonContainer.removeAllViews();

        // Generate a list of buttons for the groups the user is able to join
        for (Map.Entry<String, String> entry : groups.entrySet()) {
            // Get group ID and name from the entry in the hashmap
            String group_id = entry.getKey();
            String group_name = entry.getValue();

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

                button.setOnClickListener(v -> {
                    mAuth = FirebaseAuth.getInstance();
                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    FirebaseUser user = mAuth.getCurrentUser();
                    assert user != null;
                    String userID = user.getUid();

                    DocumentReference userRef = db.collection("users").document(userID);

                    // Add the user to the group when the button is clicked
                    userRef.update("groups." + group_id, group_name)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "User added to group successfully");
                                Toast.makeText(getContext(), group_name + " joined!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error adding user to group", e);
                                Toast.makeText(getContext(), "Something went wrong...", Toast.LENGTH_SHORT).show();
                            });
                });

                // Add button to view
                buttonContainer.addView(button);
            }
        }
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_joingroupview, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        buttonContainer = view.findViewById(R.id.group_join_container);
        displayGroups();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}