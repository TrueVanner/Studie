package nl.tue.appdev.studie;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import nl.tue.appdev.studie.databinding.FragmentFirstBinding;

import java.util.Vector;

public class HomeGroupviewFragment extends Fragment {

    private FragmentFirstBinding binding;
    private Vector<String> groups = new Vector<>();

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        groups.add("WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW");
        groups.add("2INC0 Operating Systems");
        groups.add("Group A");
        groups.add("Group B");
        groups.add("Group C");
        groups.add("Group D");
        groups.add("Group E");
        groups.add("Group F");
        groups.add("Group G");
        groups.add("Group H");
        groups.add("Group I");
        groups.add("Group J");
        groups.add("Group K");
        groups.add("Group L");
        groups.add("Group M");
        groups.add("Group N");

        View view = inflater.inflate(R.layout.fragment_homegroupview, container, false);

        LinearLayout buttonContainer = view.findViewById(R.id.group_view_container);

        // Generate a list of buttons for the groups the user has joined
        for (String group : groups) {
            // Create a button and set attributes
            Button button = new Button(getContext());
            button.setText(group);
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

            // TODO: add transition to button
            button.setOnClickListener(v ->
                    Toast.makeText(getContext(), group, Toast.LENGTH_SHORT).show()
            );

            // Add button to view
            buttonContainer.addView(button);
        }

        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}