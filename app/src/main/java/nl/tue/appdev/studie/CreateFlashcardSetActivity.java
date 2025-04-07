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

        Intent intent = getIntent();
        // retrieve the id of the group that the set is in
        if (intent != null && intent.hasExtra("group_id")) {
            groupId = intent.getStringExtra("group_id");
        }

        // redirect to login screen if user is not logged in
        if (groupId == null) {
            Toast.makeText(this, "Group ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // assignation of the screen elements in flashcard set create view
        setNameInput = findViewById(R.id.fc_set_name_input);
        recyclerView = findViewById(R.id.fc_set_recycler_view);
        ImageButton createButton = findViewById(R.id.fc_set_create_button);
        ImageButton backButton = findViewById(R.id.fc_set_create_back_button);
        // buttons onClick handlers
        createButton.setOnClickListener(v -> createFlashcardSet());
        backButton.setOnClickListener(v -> finish());
        // assignation and initialization of set create components
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        flashcardList = new ArrayList<>();
        adapter = new FlashcardSelectAdapter(flashcardList);
        recyclerView.setAdapter(adapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        loadFlashcards();
    }

    /**
     * Loads the flashcards associated with the group from the database.
     */
    private void loadFlashcards() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // get the group data from the database
        db.collection("groups").document(groupId).get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // load the flashcards associated with the group from the database
                        List<String> flashcardIds = (List<String>) documentSnapshot.get("flashcards");
                        if (flashcardIds != null && !flashcardIds.isEmpty()) {
                            // prevention of fetching null flashcards
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
            // get the flashcard data from the database
            db.collection("flashcards").document(flashcardId).get().addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // creating a Flashcard object based on flashcard data
                            Flashcard flashcard = new Flashcard(flashcardId, documentSnapshot.getString("question"), documentSnapshot.getString("answer"), documentSnapshot.getString("author"));
                            if (flashcard != null) {
                                flashcardList.add(flashcard);
                                // update the data of the adapter that regulates flashcard list view
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
        // get the title for the flashcard set from the input field
        String setName = setNameInput.getText().toString().trim();
        if (setName.isEmpty()) {
            // input field must contain text
            Toast.makeText(this, "Enter a set name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentUser == null) {
            // user must be logged in
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        // get the flashcard ids from the selection/view adapter
        List<String> selectedFlashcardIds = adapter.getSelectedFlashcards();
        if (selectedFlashcardIds.isEmpty()) {
            // at least one flashcard must be selected
            Toast.makeText(this, "Select at least one flashcard", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // a new set document reference in the 'flashcardsets' global collection
        DocumentReference newSetRef = db.collection("flashcardsets").document();
        // unique setId
        String setId = newSetRef.getId();
        // create a new flashcard set in the collection
        HashMap<String, Object> flashcardSetData = new HashMap<>();
        flashcardSetData.put("title", setName);
        flashcardSetData.put("author", currentUser.getUid());
        flashcardSetData.put("flashcards", selectedFlashcardIds);
        // get the group that the set will be in from the intent
        newSetRef.set(flashcardSetData).addOnSuccessListener(aVoid -> {
            db.collection("groups").document(groupId).get().addOnSuccessListener(groupDoc -> {
                if (groupDoc.exists()) {
                    List<String> flashcardSets = (List<String>) groupDoc.get("flashcardsets");
                    // error prevention
                    if (flashcardSets == null) {
                        flashcardSets = new ArrayList<>();
                    }
                    // add the new flashcard set ID to the 'flashcardsets' array in the group collection
                    flashcardSets.add(setId);
                    db.collection("groups").document(groupId).update("flashcardsets", flashcardSets).addOnSuccessListener(aVoid1 -> {
                        // if successful
                        Toast.makeText(CreateFlashcardSetActivity.this, "Flashcard set created", Toast.LENGTH_SHORT).show();
                        finish();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(CreateFlashcardSetActivity.this, "Failed to update group", Toast.LENGTH_SHORT).show();
                    });
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(CreateFlashcardSetActivity.this, "Failed to retrieve group data", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(CreateFlashcardSetActivity.this, "Failed to create set", Toast.LENGTH_SHORT).show();
        });
    }
}