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

import nl.tue.appdev.studie.databinding.FragmentFirstBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;

public class HomeGroupviewFragment extends Fragment {

    private static final String TAG = "HomeGroupviewFragment";
    private FragmentFirstBinding binding;
    private HashMap<String, String> groups = new HashMap<>();
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

        // Sort groups by name
        List<String> groupNames = new ArrayList<>(groups.values());
        groupNames.sort(String.CASE_INSENSITIVE_ORDER);

        // Generate a list of buttons for the groups the user has joined
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

                button.setOnClickListener(v -> {
                    Toast.makeText(getContext(), group_name, Toast.LENGTH_SHORT).show();
                    Intent toGroup = new Intent(getActivity(), GroupActivity.class);
                    toGroup.putExtra("id", group_id);
                    startActivity(toGroup);
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
        View view = inflater.inflate(R.layout.fragment_homegroupview, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        buttonContainer = view.findViewById(R.id.group_view_container);
        displayGroups();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}