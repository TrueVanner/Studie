package nl.tue.appdev.studie;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.qrcode.encoder.QRCode;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
//import java.util.logging.Logger;

public class JoinActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "JoinActivity";

    private FirebaseAuth mAuth;

    private HashMap<String, String> groups = new HashMap<String, String>(); // ID, name

    private EditText searchbar;
    private String query;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_join);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_join), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Create a group view fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        JoinGroupviewFragment groupview = new JoinGroupviewFragment();

        // Add back button
        ImageButton back = findViewById(R.id.join_back_button);
        back.setOnClickListener(this);
        // Add QR code button
        ImageButton qr = findViewById(R.id.qr_icon_button);
        qr.setOnClickListener(this);

        // Add searchbar
        searchbar = findViewById(R.id.searchbar_join);
        searchbar.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                Log.d(TAG, "Pressed: " + keyCode);
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    query = searchbar.getText().toString();
                    Log.d(TAG, "Query: " + query);
                    groupview.updateQuery(query);
                    return true;
                }
            }
            return false;
        });

        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference collectionRef = db.collection("groups");
        collectionRef.get().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Loop through all groups and add the public ones to a hashmap
                for (DocumentSnapshot document : task.getResult()) {
                    // Get document ID and fields
                    String docId = document.getId();
                    String name = document.getString("name");
                    boolean isPublic = document.getBoolean("isPublic");
                    Log.d(TAG, "ID: " + docId + ", Name: " + name + ", Public: " + isPublic);

                    if (isPublic) {
                        groups.put(docId, name);
                    }
                }

                // Pass the hashmap of groups to the fragment
                if (groupview != null) {
                    Log.d(TAG, String.valueOf(groups));
                    groupview.updateGroups(groups);

                    fragmentManager.beginTransaction()
                            .replace(R.id.fragment_join_view, groupview)
                            .commit();
                } else {
                    Log.d(TAG, "fragment is null");
                }

            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.join_back_button) {
            Intent toHome = new Intent(JoinActivity.this, HomeActivity.class);
            startActivity(toHome);
        } else if (id == R.id.qr_icon_button) {
            // TODO: update activity im not sure if this is the correct one lol
            Intent toQR = new Intent(JoinActivity.this, QRCodeActivity.class);
            startActivity(toQR);
        }
    }
}