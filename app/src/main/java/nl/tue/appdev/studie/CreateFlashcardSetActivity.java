package nl.tue.appdev.studie;

import static java.security.AccessController.getContext;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CreateFlashcardSetActivity extends AppCompatActivity {

    private String groupId;
    private EditText setNameInput;
    private RecyclerView recyclerView;
    private FlashcardSelectAdapter adapter;
    private List<Flashcard> flashcardList;
    private DatabaseReference groupFlashcardsRef, globalFlashcardSetsRef, groupFlashcardSetsRef;
    private FirebaseUser currentUser;

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

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("group_id")) {
            groupId = intent.getStringExtra("group_id");
        }

        if (groupId == null) {
            Toast.makeText(this, "Group ID not found", Toast.LENGTH_SHORT).show();
//            finish();
            return;
        }

        // group's flashcards reference
        groupFlashcardsRef = FirebaseDatabase.getInstance().getReference("groups").child(groupId).child("flashcards");
        // global flashcard sets reference
        globalFlashcardSetsRef = FirebaseDatabase.getInstance().getReference("flashcard_sets");
        // group's flashcard sets reference
        groupFlashcardSetsRef = FirebaseDatabase.getInstance().getReference("groups").child(groupId).child("flashcardsets");
        loadFlashcards();
        createButton.setOnClickListener(v -> createFlashcardSet());
        backButton.setOnClickListener(v -> {
            Intent back = new Intent(CreateFlashcardSetActivity.this, GroupActivity.class);
            back.putExtra("group_id", groupId);
            startActivity(back);
            finish();
        });
    }

    private void loadFlashcards() {
        groupFlashcardsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                flashcardList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Flashcard flashcard = dataSnapshot.getValue(Flashcard.class);
                    if (flashcard != null) {
                        flashcardList.add(flashcard);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CreateFlashcardSetActivity.this, "Failed to load flashcards", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createFlashcardSet() {
        String setName = setNameInput.getText().toString().trim();
        if (setName.isEmpty()) {
            Toast.makeText(this, "Please enter a set name", Toast.LENGTH_SHORT).show();
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

        DatabaseReference newSetRef = globalFlashcardSetsRef.push();
        String setId = newSetRef.getKey();

        HashMap<String, Object> flashcardSetData = new HashMap<>();
        flashcardSetData.put("setId", setId);
        flashcardSetData.put("name", setName);
        flashcardSetData.put("authorId", currentUser.getUid());
        flashcardSetData.put("flashcards", selectedFlashcardIds);

        newSetRef.setValue(flashcardSetData).addOnSuccessListener(unused -> {
            groupFlashcardSetsRef.child(setId).setValue(flashcardSetData)
                    .addOnSuccessListener(unused1 -> {
                        Toast.makeText(this, "Flashcard set created", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to add set to group", Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to create set", Toast.LENGTH_SHORT).show());
    }
}
