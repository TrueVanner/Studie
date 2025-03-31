package nl.tue.appdev.studie;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.qrcode.encoder.QRCode;

import java.util.HashMap;
import java.util.Map;

public class ManageGroupActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "ManageGroupActivity";

    private String groupId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_group);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_manage_group), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // get group id from group screen intent
        Intent intent = getIntent();
        groupId = intent.getStringExtra("id");

        // Create a members fragment
        MembersFragment members = new MembersFragment();

        // Add back button
        ImageButton back = findViewById(R.id.manage_back_button);
        back.setOnClickListener(this);
        // Add QR code button
        Button qr = findViewById(R.id.show_qr);
        qr.setOnClickListener(this);

        // Set group name as title
        TextView groupNameText = findViewById(R.id.group_name_manage_screen);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference userRef = db.collection("groups").document(groupId);
        userRef.get().addOnCompleteListener(this,task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    String groupName = (String) document.getData().get("name");
                    groupNameText.setText(groupName);
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });

        FragmentManager fragmentManager = getSupportFragmentManager();

        if (groupId != null) {
            Log.d(TAG, groupId);
        }
        members.updateGroupId(groupId);

        fragmentManager.beginTransaction()
                .replace(R.id.fragment_members, members)
                .commit();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.manage_back_button) {
            Intent toGroup = new Intent(ManageGroupActivity.this, GroupActivity.class);
            toGroup.putExtra("id", groupId);
            startActivity(toGroup);
        } else if (id == R.id.show_qr) {
            Intent toQR = new Intent(ManageGroupActivity.this, QRCodeActivity.class);
            toQR.putExtra("id", groupId);
            startActivity(toQR);
        }
    }
}
