package nl.tue.appdev.studie;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
        if (mAuth.getCurrentUser() == null) { // redirect to login screen if user is not logged in
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }

        flashcardStateTextView = findViewById(R.id.question_text);
        flipButton = findViewById(R.id.fc_view_flip_button);
        backButton = findViewById(R.id.fc_view_back_button);

        flipButton.setOnClickListener(this);
        backButton.setOnClickListener(this);

        Intent intent = getIntent();

        groupId = intent.getStringExtra("group_id");
        flashcardId = intent.getStringExtra("flashcard_id");

        if (flashcardId != null) {
//            loadFlashcardData(flashcardId);
        } else {
            Log.e(TAG, "No flashcard ID");
        }

        createTestFlashcard();

    }

    private void loadFlashcardData(String flashcardId) {
        DatabaseReference flashcardRef = FirebaseDatabase.getInstance().getReference("flashcards").child(flashcardId);
        Log.e(TAG, "datadatadatadatadatadatadatadatadatadatadatadatadatadatadata");
        flashcardRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    questionText = snapshot.child("question").getValue(String.class);
                    answerText = snapshot.child("answer").getValue(String.class);

                    FlashcardFragment flashcardFragment = FlashcardFragment.newInstance(questionText, answerText);

                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_flashcard, flashcardFragment).commit();
                } else {
                    Log.e(TAG, "Flashcard not found");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }

    private void createTestFlashcard() {
        String testFlashcardId = "test_fc_1";
        String testQuestionText = "Test Question?";
        String testAnswerText = "Test Answer.";

        FlashcardFragment flashcardFragment = FlashcardFragment.newInstance(testQuestionText, testAnswerText);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_flashcard, flashcardFragment).commit();
    }

    private void flipFlashcard() {
        FlashcardFragment flashcardFragment = (FlashcardFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_flashcard);
        isShowingQuestion = !isShowingQuestion;
        if (flashcardFragment != null) {
            flashcardFragment.updateFlashcardContent(isShowingQuestion);
            if (isShowingQuestion) {
                flashcardStateTextView.setText("Question");
            } else {
                flashcardStateTextView.setText("Answer");
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fc_view_flip_button) {
            flipFlashcard();
        } else if (v.getId() == R.id.fc_view_back_button) {
            Intent intent = new Intent(ViewFlashcardActivity.this, GroupActivity.class);
            intent.putExtra("id", groupId);
            startActivity(intent);
            finish();
        }
    }
}
