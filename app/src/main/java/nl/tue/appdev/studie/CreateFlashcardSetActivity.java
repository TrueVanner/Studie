package nl.tue.appdev.studie;

import static java.security.AccessController.getContext;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Activity for creating a flashcard set of chosen flashcards within a specific group.
 */
public class CreateFlashcardSetActivity extends AppCompatActivity {

    private static final String TAG = "CreateFlashcardSetActivity";

    private String groupId;
    private EditText setNameInput;
    private RecyclerView recyclerView;
    private FlashcardSelectAdapter adapter;
    private ArrayList<Flashcard> flashcardList;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard_set_create);

        setNameInput = findViewById(R.id.fc_set_name_input);
        recyclerView = findViewById(R.id.fc_set_recycler_view);
        ImageButton createButton = findViewById(R.id.fc_set_create_button);
        ImageButton backButton = findViewById(R.id.fc_set_create_back_button);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        flashcardList = new ArrayList<>();
        adapter = new FlashcardSelectAdapter(flashcardList);
        recyclerView.setAdapter(adapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("group_id")) {
            groupId = intent.getStringExtra("group_id");
        }

        if (groupId == null) {
            Toast.makeText(this, "Group ID not found", Toast.LENGTH_SHORT).show();
//            finish();
            return;
        }

        loadFlashcards();

        createButton.setOnClickListener(v -> createFlashcardSet());
        backButton.setOnClickListener(v -> finish());
    }

    /**
     * Loads the flashcards associated with the group from the database.
     */
    private void loadFlashcards() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("groups")
                .document(groupId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> flashcardIds = (List<String>) documentSnapshot.get("flashcards");

                        if (flashcardIds != null && !flashcardIds.isEmpty()) {
                            fetchFlashcardsByIds(flashcardIds, db);
                        } else {
                            Toast.makeText(CreateFlashcardSetActivity.this, "No flashcards in this group", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(CreateFlashcardSetActivity.this, "Group not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreateFlashcardSetActivity.this, "Failed to load group data", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Fetches flashcard IDs from the database and converts them to Flashcard objects.
     * @param flashcardIds ID strings of the flashcards in the group
     * @param db Database main reference
     */
    private void fetchFlashcardsByIds(List<String> flashcardIds, FirebaseFirestore db) {
        flashcardList.clear();
        for (String flashcardId : flashcardIds) {
            db.collection("flashcards")
                    .document(flashcardId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Flashcard flashcard = new Flashcard(flashcardId, documentSnapshot.getString("question"), documentSnapshot.getString("answer"), documentSnapshot.getString("author"));
                            if (flashcard != null) {
                                Log.d(TAG, "Fetched flashcard: " + flashcard.getQuestion());
                                flashcardList.add(flashcard);
                                Log.d(TAG, "Loaded flashcards: " + flashcardList.size());
                                adapter.notifyDataSetChanged();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(CreateFlashcardSetActivity.this, "Failed to load flashcards", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    /**
     * Creates a new flashcard set with the selected flashcards, storing it both globally and within the group fields.
     */
    private void createFlashcardSet() {
        String setName = setNameInput.getText().toString().trim();
        if (setName.isEmpty()) {
            Toast.makeText(this, "Enter a set name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        List<String> selectedFlashcardIds = adapter.getSelectedFlashcards();
        if (selectedFlashcardIds.isEmpty()) {
            Toast.makeText(this, "Select at least one flashcard", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference newSetRef = db.collection("flashcardsets").document();
        String setId = newSetRef.getId(); // unique setId

        // create a new flashcard set in the flashcardsets collection
        HashMap<String, Object> flashcardSetData = new HashMap<>();
        flashcardSetData.put("title", setName);
        flashcardSetData.put("author", currentUser.getUid());
        flashcardSetData.put("flashcards", selectedFlashcardIds);

        newSetRef.set(flashcardSetData)
                .addOnSuccessListener(aVoid -> {
                    db.collection("groups").document(groupId)
                            .get()
                            .addOnSuccessListener(groupDoc -> {
                                if (groupDoc.exists()) {
                                    List<String> flashcardSets = (List<String>) groupDoc.get("flashcardsets");
                                    if (flashcardSets == null) {
                                        flashcardSets = new ArrayList<>(); // error prevention
                                    }
                                    // add the new flashcard set ID to the flashcardsets array in the group
                                    flashcardSets.add(setId);

                                    db.collection("groups").document(groupId)
                                            .update("flashcardsets", flashcardSets)
                                            .addOnSuccessListener(aVoid1 -> {
                                                Toast.makeText(CreateFlashcardSetActivity.this, "Flashcard set created", Toast.LENGTH_SHORT).show();
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(CreateFlashcardSetActivity.this, "Failed to update group", Toast.LENGTH_SHORT).show();
                                            });
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(CreateFlashcardSetActivity.this, "Failed to retrieve group data", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreateFlashcardSetActivity.this, "Failed to create set", Toast.LENGTH_SHORT).show();
                });
    }
}