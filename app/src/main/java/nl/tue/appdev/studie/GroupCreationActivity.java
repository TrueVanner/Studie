package nl.tue.appdev.studie;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class GroupCreationActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{
    private ToggleButton toggle_private;
    private ToggleButton toggle_public;
    private EditText group_name;
    private EditText group_code;
    private FirebaseFirestore db;
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

        db = FirebaseFirestore.getInstance();

        group_name=findViewById(R.id.group_name);
        group_code=findViewById(R.id.group_course_code);

        ImageButton group_back = (ImageButton) findViewById(R.id.create_group_back_button);
        ImageButton create_group = (ImageButton) findViewById(R.id.button_create_group);
        toggle_private = (ToggleButton) findViewById(R.id.toggle_private);
        toggle_public = (ToggleButton) findViewById(R.id.toggle_public);

        toggle_private.setChecked(true);
        toggle_public.setChecked(false);


        toggle_public.setOnCheckedChangeListener(this);
        toggle_private.setOnCheckedChangeListener(this);
        create_group.setOnClickListener(this);
        group_back.setOnClickListener(this);
    }
        //Basically onClick, but for checks when a toggle button's check state is changed
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            //Prepare for an unholy abomination brought to you by sleep deprivation
            int id = buttonView.getId();
        //Sees if changing the check state would make both buttons be on or off:
            //If the check state is gonna change for the private toggle button
            if (id == R.id.toggle_private) {
                //And the private toggle would turn off/unchecked
                if (!toggle_private.isChecked()) {
                    //And the public toggle is off/unchecked (so both would be off/unchecked)
                    if (!toggle_public.isChecked()) {
                        //Force private to remain checked
                        toggle_private.setChecked(true);
                    }
                //If the private toggle would turn on/check
                } else {
                    //Turn off/uncheck the public toggle
                    toggle_public.setChecked(false);
                }
            //Same logic as before, but with the public toggle modifying instead of the private one
            } else if (id == R.id.toggle_public) {
                if (!toggle_public.isChecked()) {
                    if (!toggle_private.isChecked()) {
                        toggle_public.setChecked(true);
                    }
                } else {
                    toggle_private.setChecked(false);
                }
            }
        }
        @Override
        public void onClick(View v) {
            int id = v.getId();
            String name = group_name.getText().toString();
            String code = group_code.getText().toString();
            Intent toGroup = new Intent(GroupCreationActivity.this, HomeActivity.class);
            Intent toTest = new Intent(GroupCreationActivity.this, AccountActivity.class);
            Intent toHome = new Intent(GroupCreationActivity.this, HomeActivity.class);
            if(id==R.id.button_create_group) {
                if (TextUtils.isEmpty(name)) {
                    group_name.setError("Please set a Group Name");
                } else if (!toggle_public.isChecked() && !toggle_private.isChecked()
                        || toggle_public.isChecked() && toggle_private.isChecked()) {
                    Toast.makeText(this, "You fucked smth up", Toast.LENGTH_SHORT).show();
                } else {
                    addDataToFirestore(name, code, toggle_public.isChecked());
                    startActivity(toGroup);
                }
            }
            else if(id==R.id.create_group_back_button){
                startActivity(toHome);
            } else {
                String toastText = "milbei";
                Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
            }
        }

    private void addDataToFirestore(String name, String code, boolean isPublic){
        CollectionReference dbGroups = db.collection("groups");

        Groups groups = new Groups(name, code, isPublic);

        dbGroups.add(groups).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(GroupCreationActivity.this, "Group created successfully!",
                        Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupCreationActivity.this, "Failed to create group",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}