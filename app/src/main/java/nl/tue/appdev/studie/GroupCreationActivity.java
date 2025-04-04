package nl.tue.appdev.studie;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GroupCreationActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{
    private static final String TAG = "GroupCreationActivity";
    public ToggleButton togglePrivate;
    public ToggleButton togglePublic;
    private EditText groupName;
    private EditText groupCode;
    private final ArrayList<Flashcard> flashcards = new ArrayList<>();
    private boolean groupnameAvailable = true;
    Map<String,Object> groupGet = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_group_creation);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.group_creation), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        groupName=findViewById(R.id.group_name);
        groupCode =findViewById(R.id.group_course_code);

        ImageButton groupBack = findViewById(R.id.create_group_back_button);
        ImageButton createGroup = findViewById(R.id.button_create_group);
        togglePrivate = findViewById(R.id.toggle_private);
        togglePublic = findViewById(R.id.toggle_public);

        togglePrivate.setChecked(true);
        togglePublic.setChecked(false);


        togglePublic.setOnCheckedChangeListener(this);
        togglePrivate.setOnCheckedChangeListener(this);
        createGroup.setOnClickListener(this);
        groupBack.setOnClickListener(this);
    }
    //Basically onClick, but for checks when a toggle button's check state is changed
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
    //Sees if changing the check state would make both buttons be on or off:
        //If the check state is gonna change for the private toggle button
        if (id == R.id.toggle_private) {
            //And the private toggle would turn off/unchecked
            if (!togglePrivate.isChecked()) {
                //And the public toggle is off/unchecked (so both would be off/unchecked)
                if (!togglePublic.isChecked()) {
                    //Force private to remain checked
                    togglePrivate.setChecked(true);
                }
            //If the private toggle would turn on/check
            } else {
                //Turn off/uncheck the public toggle
                togglePublic.setChecked(false);
            }
        //Same logic as before, but with the public toggle modifying instead of the private one
        } else if (id == R.id.toggle_public) {
            if (!togglePublic.isChecked()) {
                if (!togglePrivate.isChecked()) {
                    togglePublic.setChecked(true);
                }
            } else {
                togglePrivate.setChecked(false);
            }
        }
    }
    @Override
    public void onClick(View v) {
        int id = v.getId();
        String name = groupName.getText().toString();
        String code = groupCode.getText().toString();
        Intent toHome = new Intent(GroupCreationActivity.this, HomeActivity.class);
        if(id==R.id.button_create_group) {
            if (name.isBlank()) {
                //If the user tries to create a group without a name, update the UI to give them a warning
                updateUI(UpdateType.GROUPNAME_EMPTY);
            }
            else if (!togglePublic.isChecked() && !togglePrivate.isChecked()
                    || togglePublic.isChecked() && togglePrivate.isChecked()) {
                //If the user somehow manages to toggle both the public and private button, or neither public nor private,
                //update the UI to throw a message
                updateUI(UpdateType.PRIVATE_AND_PUBLIC);
            } else {
                //If nothing goes wrong, attempt to create the group
                tryCreateGroup(name, code, togglePublic.isChecked());
            }
        }
        else if(id==R.id.create_group_back_button){
            //If the user presses the back arrow, it brings them to the home page
            startActivity(toHome);
        }
    }
    //Function to create and add a group to the database with the parameters given by the user
    private void addDataToFirestore(String name, String code, boolean isPublic){
        final FirebaseFirestore db=FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        String userID = user.getUid();
        DocumentReference userRef = db.collection("users").document(userID);
        CollectionReference groupRef=FirebaseFirestore.getInstance().collection("groups");
        Intent toGroup = new Intent(GroupCreationActivity.this, GroupActivity.class);
        CollectionReference dbGroups = db.collection("groups");
        Groups groups = new Groups(name, code, isPublic);
        dbGroups.add(groups).addOnSuccessListener(documentReference -> {
                //Using a hashmap to retrieve all the group information so that the given groupID is of the created group's
                groupGet.put("code", code);
                groupGet.put("flashcards", flashcards);
                groupGet.put("flashcardsets", flashcards);
                groupGet.put("isPublic", isPublic);
                groupGet.put("name", name);
                groupGet.put("notes", flashcards);
                DocumentReference groupDocRef = groupRef.document();
                groupDocRef.set(groupGet);
                String groupId=groupDocRef.getId();
                //Update the user's groups array so that it contains the newly created group
                userRef.update("groups." + groupId, name);
                //After group creation, send the user to their newly created group's view
                toGroup.putExtra("id", groupId);
                startActivity(toGroup);
        });
    }

    enum UpdateType { GROUPNAME_TAKEN, GROUPNAME_EMPTY, PRIVATE_AND_PUBLIC }

    private void updateUI(GroupCreationActivity.UpdateType updateType) {
        switch (updateType) {
            case GROUPNAME_EMPTY:
                groupName.setError("Group name can't be empty!");
                break;
            case GROUPNAME_TAKEN:
                groupName.setError("Group name already exists.");
                break;
            case PRIVATE_AND_PUBLIC:
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void tryCreateGroup(String name, String code, boolean isPublic) {
        final FirebaseFirestore db=FirebaseFirestore.getInstance();
        db.collection("groups")
                .whereEqualTo("name", name)
                .get(Source.SERVER)
                .addOnCompleteListener(this, task -> {
                    Log.d(TAG, "task succesful");
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, document.getId() + " => " + document.getData());
                            groupnameAvailable = false;
                        }

                        if (groupnameAvailable) {
                            addDataToFirestore(name, code, isPublic);
                        } else {
                            updateUI(GroupCreationActivity.UpdateType.GROUPNAME_TAKEN);
                            groupnameAvailable = true;
                        }
                    } else {
                        Log.d(TAG, "Query failed");
                    }
                });
    }
}