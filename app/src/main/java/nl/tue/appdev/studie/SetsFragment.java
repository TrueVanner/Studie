package nl.tue.appdev.studie;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SetsFragment extends Fragment {

    private static final String TAG = "SetsFragment";

    private View view;

    private LinearLayout setContainer;

    private FirebaseAuth mAuth;
    private Map<String, Object> userDocument;
    private String groupId;
    private ArrayList<String> flashcardset_ids = new ArrayList<>();
    private ArrayList<String> flashcard_ids = new ArrayList<>();
    private ArrayList<Flashcardset> flashcardsets = new ArrayList<>();

    private boolean refresh = true;

    public void retrieveFlashcardsetData() {
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (String flashcardset_id : flashcardset_ids) {
            // Get flashcardset data using the flashcardset ID
            db.collection("flashcardsets")
                .document(flashcardset_id)
                .get(Source.SERVER)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            userDocument = document.getData();
                            assert userDocument != null;
                            String title = (String) userDocument.get("title");
                            List<String> flashcard_ids_list = (List<String>) userDocument.get("flashcards");
                            flashcard_ids = new ArrayList<>(flashcard_ids_list);
                            String author = (String) userDocument.get("author");
                            Log.d(TAG,  title + " " + flashcard_ids + " " + author);

                            Flashcardset s = new Flashcardset(flashcardset_id, title, flashcard_ids, author);
                            flashcardsets.add(s);

                            displayFlashcardsets();
                        } else {
                            Log.e(TAG, "No such document");
                        }
                    } else {
                        Log.e(TAG, "get failed with ", task.getException());
                    }
            });
        }
    }

    public void retrieveFlashcardsets() {
        // Clear the list
        flashcardsets = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get flashcards of the group
        DocumentReference docRef = db.collection("groups").document(groupId);
        docRef.get(Source.SERVER).addOnCompleteListener(requireActivity(), task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    userDocument = document.getData();
                    assert userDocument != null;
                    List<String> flashcardset_ids_list = (List<String>) userDocument.get("flashcardsets");
                    flashcardset_ids = new ArrayList<>(flashcardset_ids_list);
                    Log.d(TAG, String.valueOf(flashcardset_ids));

                    retrieveFlashcardsetData();
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    public void displayFlashcardsets() {
        setContainer.removeAllViews();

        for (Flashcardset s : flashcardsets) {
            String id = s.getId();
            String title = s.getTitle();
            String size = s.getSize() + " Qs.";

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

            // Question text (75% width)
            TextView titleText = new TextView(getContext());
            titleText.setId(View.generateViewId());
            titleText.setText(title);
            titleText.setMaxLines(2);
            titleText.setTextColor(Color.WHITE);
            titleText.setTextSize(16);
            titleText.setTypeface(null, Typeface.BOLD);

            ConstraintLayout.LayoutParams titleTextParams = new ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
            titleTextParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            titleTextParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            titleTextParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            titleTextParams.width = 0;
            titleTextParams.matchConstraintPercentWidth = 0.75f;
            titleText.setLayoutParams(titleTextParams);
            titleText.setEllipsize(TextUtils.TruncateAt.END);

            // Author text (20% width)
            TextView sizeText = new TextView(getContext());
            sizeText.setId(View.generateViewId());
            sizeText.setText(size);
            sizeText.setMaxLines(1);
            sizeText.setTextColor(Color.WHITE);
            sizeText.setTextSize(16);

            ConstraintLayout.LayoutParams sizeTextParams = new ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
            sizeTextParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
            sizeTextParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            sizeTextParams.width = 0;
            sizeTextParams.matchConstraintPercentWidth = 0.2f; // 20% of parent width
            sizeText.setLayoutParams(sizeTextParams);
            sizeText.setEllipsize(TextUtils.TruncateAt.END);
            sizeText.setGravity(Gravity.END);
            sizeText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);

            // Icon (Top-Right Corner)
            ImageView icon = new ImageView(getContext());
            icon.setId(View.generateViewId());
            icon.setImageResource(R.drawable.question_mark);
            float density = getContext().getResources().getDisplayMetrics().density;
            int width = (int) (20.0 * density);
            int height = (int) (20.0 * density);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width, height);
            icon.setLayoutParams(params);

            // Add views to buttonLayout
            buttonLayout.addView(titleText);
            buttonLayout.addView(sizeText);
            buttonLayout.addView(icon);

            // Add button and layout to FrameLayout
            buttonContainer.addView(customButton); // Button is in the background
            buttonContainer.addView(buttonLayout); // Text and Icon are on top

            // Add FrameLayout to parent layout
            setContainer.addView(buttonContainer);

            // Set Constraints
            ConstraintSet set = new ConstraintSet();
            set.clone(buttonLayout);

            set.connect(titleText.getId(), ConstraintSet.START, buttonLayout.getId(), ConstraintSet.START, 16);
            set.connect(titleText.getId(), ConstraintSet.TOP, buttonLayout.getId(), ConstraintSet.TOP, 16);
            set.connect(titleText.getId(), ConstraintSet.BOTTOM, buttonLayout.getId(), ConstraintSet.BOTTOM, 26);
            set.constrainWidth(titleText.getId(), 0);
            set.setHorizontalWeight(titleText.getId(), 0.75f);

            set.connect(sizeText.getId(), ConstraintSet.END, buttonLayout.getId(), ConstraintSet.END, 16);
            set.connect(sizeText.getId(), ConstraintSet.BOTTOM, buttonLayout.getId(), ConstraintSet.BOTTOM, 26);
            set.constrainWidth(sizeText.getId(), 0);
            set.setHorizontalWeight(sizeText.getId(), 0.2f);

            set.connect(icon.getId(), ConstraintSet.END, buttonLayout.getId(), ConstraintSet.END, 16);
            set.connect(icon.getId(), ConstraintSet.TOP, buttonLayout.getId(), ConstraintSet.TOP, 16);

            set.applyTo(buttonLayout);

            // Set OnClickListener for the FrameLayout
            customButton.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), ViewFlashcardSetActivity.class);
                intent.putExtra("flashcardSetId", id);
                intent.putExtra("group_id", groupId);
                startActivity(intent);
//                Toast.makeText(getContext(), "Flashcardset " + id + " clicked", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            groupId = getArguments().getString("id");
            Log.d(TAG, "Received Data: " + groupId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        refresh = false;

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sets, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setContainer = view.findViewById(R.id.set_view_container);
        retrieveFlashcardsets();

        Button createButton = view.findViewById(R.id.set_create);
        createButton.setOnClickListener(v -> {
            Intent toCreate = new Intent(getActivity(), CreateFlashcardSetActivity.class);
            toCreate.putExtra("group_id", groupId);
            startActivity(toCreate);
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (refresh) {
            retrieveFlashcardsets();
        } else {
            refresh = true;
        }
    }
}