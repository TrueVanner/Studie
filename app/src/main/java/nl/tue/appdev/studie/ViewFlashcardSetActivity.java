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
        // redirect to login screen if user is not logged in
        if (mAuth.getCurrentUser() == null) {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            // disposal of the set view
            finish();
        }
        // assignation of the screen elements in flashcard set view
        questionNrTextView = findViewById(R.id.fc_set_view_question_nr_text);
        flashcardStateTextView = findViewById(R.id.fc_set_view_question_text);
        flipButton = findViewById(R.id.fc_set_view_flip_button);
        backButton = findViewById(R.id.fc_set_view_back_button);
        prevButton = findViewById(R.id.fc_set_view_prev_button);
        nextButton = findViewById(R.id.fc_set_view_next_button);
        // buttons onClick handlers
        flipButton.setOnClickListener(this);
        backButton.setOnClickListener(this);
        prevButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);

        Intent intent = getIntent();
        // retrieve the id of the group that the set is in
        groupId = intent.getStringExtra("group_id");
        // retrieve the id of the flashcard set to be viewed
        flashcardSetId = intent.getStringExtra("flashcardSetId");

        // prevention of null fetches from the database
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
        // get the flashcard data from the database
        db.collection("flashcardsets").document(flashcardSetId)
                .get(Source.SERVER)
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.e(TAG, "DocumentSnapshot Data: " + documentSnapshot.getData());
                        // store the ids of the flashcards in the set
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

    /**
     * Retrieves flashcard data in the database from individual flashcards inside a set, creating Flashcard objects to make future retrievals easier in the process.
     * @param flashcardIds a list of flashcard ids inside a flashcard set
     */
    private void retrieveFlashcardSetData(List<String> flashcardIds) {
        flashcards.clear();
        // adding dummy flashcards to prevent issues in loading more indexes than there is
        for(int i = 0; i < expectedFlashcardCount; i++) { flashcards.add(null); }

        for (int i = 0; i < flashcardIds.size(); i++) {
            String flashcardId = flashcardIds.get(i);
            int flashcardIndex = i;
            // retrieving flashcard data
            db.collection("flashcards").document(flashcardId).get(Source.SERVER).addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {

                    String question = documentSnapshot.getString("question");
                    String answer = documentSnapshot.getString("answer");
                    String author = documentSnapshot.getString("author");
                    // creating a Flashcard object based on flashcard data
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
            // flaschard object retrieval
            Flashcard currentFlashcard = flashcards.get(index);
            updateQuestionNr();
            // reset flaschard context view and state text to question
            isShowingQuestion = true;
            flashcardStateTextView.setText("Question");
            // set the new fragment view
            FlashcardFragment flashcardFragment = FlashcardFragment.newInstance(currentFlashcard.getQuestion(), currentFlashcard.getAnswer());
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_flashcard_view_fc_set, flashcardFragment).commit();
        }
    }

    /**
     * In the interval of the flashcard set size, switches to the (if existing) previous flashcard in the set and loads it on the activity.
     */
    private void goToPreviousFlashcard() {
        // flashcard has to not be the first in the set
        if (currentFlashcardIndex > 0) {
            currentFlashcardIndex--;
            // load the flashcard in the fragment
            loadFlashcard(currentFlashcardIndex);
        }
    }

    /**
     * In the interval of the flashcard set size, switches to the (if existing) next flashcard in the set and loads it on the activity.
     */
    private void goToNextFlashcard() {
        // flashcard has to not be the last in the set
        if (currentFlashcardIndex < flashcards.size() - 1) {
            currentFlashcardIndex++;
            // load the flashcard in the fragment
            loadFlashcard(currentFlashcardIndex);
        }
    }

    /**
     * Switches between the currently viewed flashcard's question viewport and answer viewport.
     * (isShowingQuestion does not retain between different flashcards in the set, resetting to 'true' when switched)
     */
    private void flipFlashcard() {
        // retrieve flashcard viewport fragment
        FlashcardFragment flashcardFragment = (FlashcardFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_flashcard_view_fc_set);
        // update boolean variables
        isShowingQuestion = !isShowingQuestion;
        if (flashcardFragment != null) {
            // display the question or the answer
            flashcardFragment.updateFlashcardContent(isShowingQuestion);
            // update the flashcard state text at the top of the view
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
        // update "Card X of X" text at the top of the screen
        questionNrTextView.setText("Card " + (currentFlashcardIndex + 1) + " of " + flashcards.size());
        // update the card state text
        flashcardStateTextView.setText("Question");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fc_set_view_flip_button) {
            // 'flips' the currently viewed flashcard in the set, swapping between 'question' and 'answer' sides
            flipFlashcard();
        } else if (v.getId() == R.id.fc_set_view_back_button) {
            // returns back to the group screen of the group the set is in by passing the intent with the id of the group to the GroupActivity
            Intent intent = new Intent(ViewFlashcardSetActivity.this, GroupActivity.class);
            intent.putExtra("id", groupId);
            startActivity(intent);
            // disposal of the set view
            finish();
        } else if (v.getId() == R.id.fc_set_view_prev_button) {
            // flashcard navigation to the previous index in the set
            goToPreviousFlashcard();
        } else if (v.getId() == R.id.fc_set_view_next_button) {
            // flashcard navigation to the next index in the set
            goToNextFlashcard();
        }
    }
}
