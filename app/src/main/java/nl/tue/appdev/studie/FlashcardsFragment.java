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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class FlashcardsFragment extends Fragment {

    private static final String TAG = "FlashcardsFragment";

    private View view;

    private LinearLayout flashcardContainer;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Map<String, Object> groupData;
    private String groupId;
    private final ArrayList<Flashcard> flashcards = new ArrayList<>();

    private boolean startedLoading = false;
    public void retrieveFlashcards() {
        startedLoading = true;
        // Clear the list
        flashcards.clear();

        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get flashcards of the group
        db.collection("groups").document(groupId)
                .get(Source.SERVER)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            groupData = document.getData();
                            assert groupData != null;
                            retrieveFlashcardData((List<String>) groupData.get("flashcards"));
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                });
    }
    public void retrieveFlashcardData(List<String> flashcardIds) {
        flashcards.clear();

        for (String flashcard_id : flashcardIds) {
            // Get flashcard data using the flashcard ID
            db.collection("flashcards")
                .document(flashcard_id)
                .get(Source.SERVER)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            groupData = document.getData();
                            assert groupData != null;
                            String question = (String) groupData.get("question");
                            String answer = (String) groupData.get("answer");
                            String author = (String) groupData.get("author");
                            Log.d(TAG,  question + " " + answer + " " + author);

                            flashcards.add(new Flashcard(flashcard_id, question, answer, author));

                            displayFlashcards();
                        } else {
                            Log.e(TAG, "No such document");
                        }
                    } else {
                        Log.e(TAG, "get failed with ", task.getException());
                    }
            });
        }

        // i know this isn't async but should be good enough
        startedLoading = false;
    }

    public void displayFlashcards() {
        flashcardContainer.removeAllViews();

        for (Flashcard f : flashcards) {
            String id = f.getId();
            String question = f.getQuestion();
            String author = f.getAuthor();

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
            TextView questionText = new TextView(getContext());
            questionText.setId(View.generateViewId());
            questionText.setText(question);
            questionText.setMaxLines(2);
            questionText.setTextColor(Color.WHITE);
            questionText.setTextSize(16);
            questionText.setTypeface(null, Typeface.BOLD);

            ConstraintLayout.LayoutParams questionTextParams = new ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
            questionTextParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            questionTextParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            questionTextParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            questionTextParams.width = 0;
            questionTextParams.matchConstraintPercentWidth = 0.75f;
            questionText.setLayoutParams(questionTextParams);
            questionText.setEllipsize(TextUtils.TruncateAt.END);

            // Author text (20% width)
            TextView authorText = new TextView(getContext());
            authorText.setId(View.generateViewId());
            authorText.setText(author);
            authorText.setMaxLines(1);
            authorText.setTextColor(Color.WHITE);
            authorText.setTextSize(16);

            ConstraintLayout.LayoutParams authorTextParams = new ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
            authorTextParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
            authorTextParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            authorTextParams.width = 0;
            authorTextParams.matchConstraintPercentWidth = 0.2f; // 20% of parent width
            authorText.setLayoutParams(authorTextParams);
            authorText.setEllipsize(TextUtils.TruncateAt.END);
            authorText.setGravity(Gravity.END);
            authorText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);

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
            buttonLayout.addView(questionText);
            buttonLayout.addView(authorText);
            buttonLayout.addView(icon);

            // Add button and layout to FrameLayout
            buttonContainer.addView(customButton); // Button is in the background
            buttonContainer.addView(buttonLayout); // Text and Icon are on top

            // Add FrameLayout to parent layout
            flashcardContainer.addView(buttonContainer);

            // Set Constraints
            ConstraintSet set = new ConstraintSet();
            set.clone(buttonLayout);

            set.connect(questionText.getId(), ConstraintSet.START, buttonLayout.getId(), ConstraintSet.START, 16);
            set.connect(questionText.getId(), ConstraintSet.TOP, buttonLayout.getId(), ConstraintSet.TOP, 16);
            set.connect(questionText.getId(), ConstraintSet.BOTTOM, buttonLayout.getId(), ConstraintSet.BOTTOM, 26);
            set.constrainWidth(questionText.getId(), 0);
            set.setHorizontalWeight(questionText.getId(), 0.75f);

            set.connect(authorText.getId(), ConstraintSet.END, buttonLayout.getId(), ConstraintSet.END, 16);
            set.connect(authorText.getId(), ConstraintSet.BOTTOM, buttonLayout.getId(), ConstraintSet.BOTTOM, 26);
            set.constrainWidth(authorText.getId(), 0);
            set.setHorizontalWeight(authorText.getId(), 0.2f);

            set.connect(icon.getId(), ConstraintSet.END, buttonLayout.getId(), ConstraintSet.END, 16);
            set.connect(icon.getId(), ConstraintSet.TOP, buttonLayout.getId(), ConstraintSet.TOP, 16);

            set.applyTo(buttonLayout);

            // Set OnClickListener for the FrameLayout
            customButton.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), ViewFlashcardActivity.class);
                intent.putExtra("flashcard_id", id);
                intent.putExtra("group_id", groupId);
                startActivity(intent);
//                Toast.makeText(getContext(), "Flashcard " + id + " clicked", Toast.LENGTH_SHORT).show();
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

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
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
        retrieveFlashcards();

        Button createButton = view.findViewById(R.id.fc_create);
        createButton.setOnClickListener(v -> {
            Intent toCreate = new Intent(getActivity(), CreateFlashcardActivity.class);
            toCreate.putExtra("id", groupId);
            startActivity(toCreate);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!startedLoading) { retrieveFlashcards(); }
    }
}