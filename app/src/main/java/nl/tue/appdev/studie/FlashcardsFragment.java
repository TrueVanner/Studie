package nl.tue.appdev.studie;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;


public class FlashcardsFragment extends Fragment {

    private static final String TAG = "FlashcardsFragment";

    private View view;

    private LinearLayout flashcardContainer;

    public void displayFlashcards() {
        for (int i = 0; i < 20; i++) {
            // Create a FrameLayout to act as a button container
            FrameLayout buttonContainer = new FrameLayout(getContext());
            buttonContainer.setId(View.generateViewId());
            buttonContainer.setLayoutParams(new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT, // Full width
                    170
            ));

            // Create a MaterialButton inside the FrameLayout
            MaterialButton customButton = new MaterialButton(getContext());
            customButton.setId(View.generateViewId());
            customButton.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    150
            ));
            customButton.setText("");

            // Set background
            Drawable background = ContextCompat.getDrawable(getContext(), R.drawable.button_simple);
            assert background != null;
            customButton.setBackgroundTintList(null);
            customButton.setBackground(background);

            // Create a ConstraintLayout inside the FrameLayout
            ConstraintLayout buttonLayout = new ConstraintLayout(getContext());
            buttonLayout.setId(View.generateViewId());
            buttonLayout.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            ));

            // Left Text (75% width)
            TextView leftText = new TextView(getContext());
            leftText.setId(View.generateViewId());
            leftText.setText("Left Side Text");
            leftText.setMaxLines(2);
            leftText.setTextColor(Color.WHITE);
            leftText.setTextSize(16);

            ConstraintLayout.LayoutParams leftTextParams = new ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
            leftTextParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            leftTextParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            leftTextParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            leftTextParams.width = 0;
            leftTextParams.matchConstraintPercentWidth = 0.75f;
            leftText.setLayoutParams(leftTextParams);
            leftText.setEllipsize(TextUtils.TruncateAt.END);

            // Right Text (20% width)
            TextView rightText = new TextView(getContext());
            rightText.setId(View.generateViewId());
            rightText.setText("Right Text");
            rightText.setMaxLines(1);
            rightText.setTextColor(Color.WHITE);
            rightText.setTextSize(16);

            ConstraintLayout.LayoutParams rightTextParams = new ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
            rightTextParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
            rightTextParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            rightTextParams.width = 0;
            rightTextParams.matchConstraintPercentWidth = 0.2f; // 20% of parent width
            rightText.setLayoutParams(rightTextParams);
            rightText.setEllipsize(TextUtils.TruncateAt.END);
            rightText.setGravity(Gravity.END);
            rightText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);

            // Icon (Top-Right Corner)
            ImageView icon = new ImageView(getContext());
            icon.setId(View.generateViewId());
            icon.setImageResource(R.drawable.user);
            float density = getContext().getResources().getDisplayMetrics().density;
            int width = (int) (20.0 * density);
            int height = (int) (20.0 * density);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width, height);
            icon.setLayoutParams(params);

            // Add views to buttonLayout
            buttonLayout.addView(leftText);
            buttonLayout.addView(rightText);
            buttonLayout.addView(icon);

            // Add button and layout to FrameLayout
            buttonContainer.addView(customButton); // Button is in the background
            buttonContainer.addView(buttonLayout); // Text and Icon are on top

            // Add FrameLayout to parent layout
            flashcardContainer.addView(buttonContainer);

            // Set Constraints
            ConstraintSet set = new ConstraintSet();
            set.clone(buttonLayout);

            set.connect(leftText.getId(), ConstraintSet.START, buttonLayout.getId(), ConstraintSet.START, 16);
            set.connect(leftText.getId(), ConstraintSet.TOP, buttonLayout.getId(), ConstraintSet.TOP, 16);
            set.connect(leftText.getId(), ConstraintSet.BOTTOM, buttonLayout.getId(), ConstraintSet.BOTTOM, 26);
            set.constrainWidth(leftText.getId(), 0);
            set.setHorizontalWeight(leftText.getId(), 0.75f);

            set.connect(rightText.getId(), ConstraintSet.END, buttonLayout.getId(), ConstraintSet.END, 16);
            set.connect(rightText.getId(), ConstraintSet.BOTTOM, buttonLayout.getId(), ConstraintSet.BOTTOM, 26);
            set.constrainWidth(rightText.getId(), 0);
            set.setHorizontalWeight(rightText.getId(), 0.2f);

            set.connect(icon.getId(), ConstraintSet.END, buttonLayout.getId(), ConstraintSet.END, 16);
            set.connect(icon.getId(), ConstraintSet.TOP, buttonLayout.getId(), ConstraintSet.TOP, 16);

            set.applyTo(buttonLayout);

            // Set OnClickListener for the FrameLayout
            customButton.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Button clicked!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_flashcards, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        flashcardContainer = view.findViewById(R.id.fc_view_container);
        displayFlashcards();

        Button createButton = view.findViewById(R.id.fc_create);
        createButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateFlashcardActivity.class);
            startActivity(intent);
        });
    }

}