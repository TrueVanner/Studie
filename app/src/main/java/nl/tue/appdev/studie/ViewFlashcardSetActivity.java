package nl.tue.appdev.studie;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to view and interact with a flashcard set. Allows browsing through flashcards in the set and flipping between the question and answer of the selected flashcard.
 */
public class ViewFlashcardSetActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ViewFlashcardSetActivity";

    private String groupId;
    private String flashcardSetId;
    private final List<Flashcard> flashcards = new ArrayList<>();
    private boolean isShowingQuestion = true;
    private int currentFlashcardIndex = 0;
    private int loadedFlashcardCount = 0;
    private int expectedFlashcardCount = 0;
    private TextView questionNrTextView;
    private TextView flashcardStateTextView;
    private ImageButton flipButton;
    private ImageButton backButton;
    private ImageButton prevButton;
    private ImageButton nextButton;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard_set_view);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        if (mAuth.getCurrentUser() == null) { // redirect to login screen if user is not logged in
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }

        questionNrTextView = findViewById(R.id.fc_set_view_question_nr_text);
        flashcardStateTextView = findViewById(R.id.fc_set_view_question_text);
        flipButton = findViewById(R.id.fc_set_view_flip_button);
        backButton = findViewById(R.id.fc_set_view_back_button);
        prevButton = findViewById(R.id.fc_set_view_prev_button);
        nextButton = findViewById(R.id.fc_set_view_next_button);

        flipButton.setOnClickListener(this);
        backButton.setOnClickListener(this);
        prevButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);

        Intent intent = getIntent();

        groupId = intent.getStringExtra("group_id");
        flashcardSetId = intent.getStringExtra("flashcardSetId");

        if (flashcardSetId != null) {
            loadFlashcardSet(flashcardSetId);
        } else {
            Log.e(TAG, "No flashcard set ID");
        }
    }

    /**
     * Loads the flashcards from the flashcard set with given flashcardSetId included in the intent entry.
     * @param flashcardSetId passed down flashcard set id with the intent, to fetch from the database
     */
    private void loadFlashcardSet(String flashcardSetId) {
        db.collection("flashcardsets").document(flashcardSetId)
                .get(Source.SERVER)
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.e(TAG, "DocumentSnapshot Data: " + documentSnapshot.getData());

                        List<String> flashcardIds = (List<String>) documentSnapshot.get("flashcards");

                        if (flashcardIds != null) {
                            expectedFlashcardCount = flashcardIds.size();
                            retrieveFlashcardSetData(flashcardIds);
                        } else {
                            Log.e(TAG, "Flashcards field is null or missing");
                        }
                    } else {
                        Log.e(TAG, "Flashcard set not found in the database");
                    }
        }).addOnFailureListener(e -> Log.e(TAG, "Error fetching flashcard set", e));
    }

    private void retrieveFlashcardSetData(List<String> flashcardIds) {
        flashcards.clear();
        for(int i = 0; i < expectedFlashcardCount; i++) { flashcards.add(null); }

        for (int i = 0; i < flashcardIds.size(); i++) {
            String flashcardId = flashcardIds.get(i);
            int flashcardIndex = i;
            db.collection("flashcards")
                    .document(flashcardId)
                    .get(Source.SERVER)
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String question = documentSnapshot.getString("question");
                            String answer = documentSnapshot.getString("answer");
                            String author = documentSnapshot.getString("author");

                            Flashcard flashcard = new Flashcard(flashcardId, question, answer, author);
                            flashcards.set(flashcardIndex, flashcard);

                            // Update counter
                            loadedFlashcardCount++;

                            // Check if all flashcards have been loaded
                            if (loadedFlashcardCount == expectedFlashcardCount) {
                                loadFlashcard(0); // Load the first flashcard
                            }
                        } else {
                            Log.e(TAG, "Flashcard with ID <" + flashcardId + "> not found in the database");
                        }
                    }).addOnFailureListener(e -> Log.e(TAG, "Error fetching flashcard: " + flashcardId, e));
        }
    }


    /**
     * Loads the flashcard upon the flashcard view fragment, based on passed argument of selected flashcard index in the set.
     * @param index selected flashcard index from the set
     */
    private void loadFlashcard(int index) {
        if (index >= 0 && index < flashcards.size()) {
            Flashcard currentFlashcard = flashcards.get(index);
            updateQuestionNr();
            isShowingQuestion = true;
            flashcardStateTextView.setText("Question");

            FlashcardFragment flashcardFragment = FlashcardFragment.newInstance(currentFlashcard.getQuestion(), currentFlashcard.getAnswer());
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_flashcard_view_fc_set, flashcardFragment).commit();
        }
    }

    /**
     * TESTING: A test method to create a local flashcard set to display in the activity fragment.
     */
    private void createTestFlashcardSet() {
        Flashcard flashcard1 = new Flashcard("test_flashcard_1", "Hotel?", "Trivago.", "a1");
        Flashcard flashcard2 = new Flashcard("test_flashcard_2", "q2", "a2", "a2");
        flashcards.add(flashcard1);
        flashcards.add(flashcard2);
        loadFlashcard(currentFlashcardIndex);

        FlashcardFragment flashcardFragment = FlashcardFragment.newInstance(flashcards.get(currentFlashcardIndex).getQuestion(), flashcards.get(currentFlashcardIndex).getAnswer());
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_flashcard_view_fc_set, flashcardFragment).commit();

        updateQuestionNr();
    }

    /**
     * In the interval of the flashcard set size, switches to the (if existing) previous flashcard in the set and loads it on the activity.
     */
    private void goToPreviousFlashcard() {
        if (currentFlashcardIndex > 0) {
            currentFlashcardIndex--;
            loadFlashcard(currentFlashcardIndex);
        }
    }

    /**
     * In the interval of the flashcard set size, switches to the (if existing) next flashcard in the set and loads it on the activity.
     */
    private void goToNextFlashcard() {
        if (currentFlashcardIndex < flashcards.size() - 1) {
            currentFlashcardIndex++;
            loadFlashcard(currentFlashcardIndex);
        }
    }

    /**
     * Switches between the currently viewed flashcard's question viewport and answer viewport.
     * (isShowingQuestion does not retain between different flashcards in the set, resetting to 'true' when switched)
     */
    private void flipFlashcard() {
        FlashcardFragment flashcardFragment = (FlashcardFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_flashcard_view_fc_set);
        isShowingQuestion = !isShowingQuestion;
        if (flashcardFragment != null) {
            flashcardFragment.updateFlashcardContent(isShowingQuestion); // display the question or the answer
            if (isShowingQuestion) {
                flashcardStateTextView.setText("Question");
            } else {
                flashcardStateTextView.setText("Answer");
            }
        }
    }

    /**
     * Updates the flashcard number display within the flashcard set view and resets the flashcard state viewport (back to question).
     */
    private void updateQuestionNr() {
        questionNrTextView.setText("Card " + (currentFlashcardIndex + 1) + " of " + flashcards.size());
        flashcardStateTextView.setText("Question");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fc_set_view_flip_button) {
            flipFlashcard();
        } else if (v.getId() == R.id.fc_set_view_back_button) {
            Intent intent = new Intent(ViewFlashcardSetActivity.this, GroupActivity.class);
            intent.putExtra("id", groupId);
            startActivity(intent);
            finish();
        } else if (v.getId() == R.id.fc_set_view_prev_button) {
            goToPreviousFlashcard();
        } else if (v.getId() == R.id.fc_set_view_next_button) {
            goToNextFlashcard();
        }
    }
}
