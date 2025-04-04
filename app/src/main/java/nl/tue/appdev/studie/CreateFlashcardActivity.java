package nl.tue.appdev.studie;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class CreateFlashcardActivity extends AppCompatActivity {

    String TAG = "CreateFlashcardActivity";
    String groupId = "";
    private FirebaseAuth mAuth;
    private Map<String, Object> userDocument;
    List<String> setIds = new ArrayList<>();
    List<String> sets = new ArrayList<>();
    HashMap<String, String> titleToId = new HashMap<>();

    public void checkIfBothFinished(AtomicBoolean a, AtomicBoolean b) {
        if (a.get() && b.get()) {
            finish();
        }
    }

    public void addFlashcardToSetAndGroup(String cardId, String setId) {
        Log.d(TAG, cardId + " " + setId);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        AtomicBoolean addedToSet = new AtomicBoolean(false);
        AtomicBoolean addedToGroup = new AtomicBoolean(false);

        // Reference to the document in the "flashcardsets" collection
        if (!(setId.equals("None"))) {
            db.collection("flashcardsets")
                    .document(setId)
                    .update("flashcards", FieldValue.arrayUnion(cardId))
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Card ID added successfully!");
                        addedToSet.set(true);
                        checkIfBothFinished(addedToSet, addedToGroup);
                    })
                    .addOnFailureListener(e -> {
                        Log.d(TAG, "Error adding card ID: " + e.getMessage());
                        addedToSet.set(true);
                        checkIfBothFinished(addedToSet, addedToGroup);
                    });
        } else {
            // If no set is selected, skip this section
            addedToSet.set(true);
        }

        // Reference to the document in the "groups" collection
        db.collection("groups")
                .document(groupId)
                .update("flashcards", FieldValue.arrayUnion(cardId))
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Card ID added successfully!");
                    addedToGroup.set(true);
                    checkIfBothFinished(addedToSet, addedToGroup);
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "Error adding card ID: " + e.getMessage());
                    addedToGroup.set(true);
                    checkIfBothFinished(addedToSet, addedToGroup);
                });
    }

    public void createFlashcard(String question, String answer, String author, String setTitle) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a flashcard object
        Map<String, Object> flashcard = new HashMap<>();
        flashcard.put("question", question);
        flashcard.put("answer", answer);
        flashcard.put("author", author);
        flashcard.put("question_file", "");

        // Add to Firestore with a random ID
        db.collection("flashcards")
                .add(flashcard)
                .addOnSuccessListener(documentReference -> {
                    String cardId = documentReference.getId();
                    Log.d(TAG, "Flashcard added with ID: " + documentReference.getId());

                    // Get the ID of the set that the new flashcard should be in
                    String setId = "None";
                    if (!(setTitle.equals("None"))) {
                        setId = titleToId.get(setTitle);
                    }
                    addFlashcardToSetAndGroup(cardId, setId);
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "Error adding flashcard: " + e.getMessage());
                });
    }

    public void setFlashcardData() {
        EditText questionText = findViewById(R.id.question_edit_text);
        String question = questionText.getText().toString();
        EditText answerText = findViewById(R.id.answer_edit_text);
        String answer = answerText.getText().toString();
        Spinner spinnerSets = findViewById(R.id.spinner_sets);
        String setTitle = spinnerSets.getSelectedItem().toString();

        // Get author name
        FirebaseUser user;
        user = mAuth.getCurrentUser();
        assert user != null;
        String userID = user.getUid();
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(userID)
                .get(Source.SERVER)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            Map<String, Object> userDocument = document.getData();
                            assert userDocument != null;
                            String author = (String) userDocument.get("name");
                            assert author != null;
                            Log.d(TAG, question + " " + answer + " " + author + " " + setTitle);

                            createFlashcard(question, answer, author, setTitle);
                        } else {
                            Log.e(TAG, "No such document");
                        }
                    } else {
                        Log.e(TAG, "get failed with ", task.getException());
                    }
                });
    }

    public void displayFlashcardsetTitles() {
        Spinner spinnerSets = findViewById(R.id.spinner_sets);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sets);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSets.setAdapter(adapter);
    }

    public void retrieveFlashcardsetData() {
        sets.add("None");

        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (String setId : setIds) {
            // Get flashcard data using the flashcard ID
            db.collection("flashcardsets")
                    .document(setId)
                    .get(Source.SERVER)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                userDocument = document.getData();
                                assert userDocument != null;
                                String title = (String) userDocument.get("title");
                                Log.d(TAG,  setId + " " + title);

                                sets.add(title);
                                titleToId.put(title, setId);
                                displayFlashcardsetTitles();
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
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get flashcardsets of the group
        DocumentReference docRef = db.collection("groups").document(groupId);
        docRef.get(Source.SERVER).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    userDocument = document.getData();
                    assert userDocument != null;
                    setIds = (List<String>) userDocument.get("flashcardsets");

                    Log.d(TAG, String.valueOf(setIds));

                    retrieveFlashcardsetData();
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            groupId = extras.getString("id");
            Log.d(TAG, "Received Data: " + groupId);
        }

        setContentView(R.layout.activity_create_flashcard);

        ImageButton backButton = findViewById(R.id.create_fc_back_button);
        backButton.setOnClickListener(v -> finish());
        ImageButton createButton = findViewById(R.id.fc_create);
        createButton.setOnClickListener(v -> setFlashcardData());

        retrieveFlashcardsets();
    }
}