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

/**
 * Activity to view and interact with a flashcard.
 * Allows flipping between the question and answer.
 */
public class ViewFlashcardActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ViewFlashcardActivity";

    private String groupId;
    private String flashcardId;
    private String questionText;
    private String answerText;
    private boolean isShowingQuestion = true;
    private TextView flashcardStateTextView;
    private ImageButton flipButton;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard_view);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        // redirect to login screen if user is not logged in
        if (mAuth.getCurrentUser() == null) {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            // disposal of the flashcard view
            finish();
        }
        // assignation of the screen elements in flashcard view
        flashcardStateTextView = findViewById(R.id.question_text);
        flipButton = findViewById(R.id.fc_view_flip_button);
        backButton = findViewById(R.id.fc_view_back_button);
        // buttons onClick handlers
        flipButton.setOnClickListener(this);
        backButton.setOnClickListener(this);

        Intent intent = getIntent();
        // retrieve the id of the group that the flashcard is in
        groupId = intent.getStringExtra("group_id");
        // retrieve the id of the flashcard to be viewed
        flashcardId = intent.getStringExtra("flashcard_id");

        // prevention of null fetches from the database
        if (flashcardId != null) {
            loadFlashcardData(flashcardId);
        } else {
            Log.e(TAG, "No flashcard ID");
        }
    }

    /**
     * Loads flashcard data from the database based on the given flashcard ID.
     *
     * @param flashcardId The ID of the flashcard to be loaded.
     */
    private void loadFlashcardData(String flashcardId) {
        // get firestore instance for fetching data
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // get the flashcard data from the database
        db.collection("flashcards")
                .document(flashcardId).get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // store the question and answer fields of the flashcard
                        questionText = documentSnapshot.getString("question");
                        answerText = documentSnapshot.getString("answer");
                        // set the new fragment view
                        FlashcardFragment flashcardFragment = FlashcardFragment.newInstance(questionText, answerText);
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_flashcard, flashcardFragment).commit();
                    } else {
                        Log.e(TAG, "Flashcard not found");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Firestore error: " + e.getMessage()));
    }

    /**
     * Flips the flashcard between the question and answer viewports.
     */
    private void flipFlashcard() {
        // retrieve flashcard viewport fragment
        FlashcardFragment flashcardFragment = (FlashcardFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_flashcard);
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
     * Handles click events for the flip and back buttons.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fc_view_flip_button) {
            // 'flips' the currently viewed flashcard, swapping between 'question' and 'answer' sides
            flipFlashcard();
        } else if (v.getId() == R.id.fc_view_back_button) {
            // returns back to the group screen of the group the set is in by passing the intent with the id of the group to the GroupActivity
            Intent intent = new Intent(ViewFlashcardActivity.this, GroupActivity.class);
            intent.putExtra("id", groupId);
            startActivity(intent);
            // disposal of the flashcard view
            finish();
        }
    }
}
