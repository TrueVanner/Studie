package nl.tue.appdev.studie;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
//import java.util.logging.Logger;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "HomeActivity";

    private FirebaseAuth mAuth;

    private Map<String, Object> userDocument;

    private HashMap<String, String> groups; // ID, name

    private EditText searchbar;
    private String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.studie_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.studie_home), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Create a group view fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        HomeGroupviewFragment groupview = new HomeGroupviewFragment();

        // Add 'go to account' button
        Button accountButton = findViewById(R.id.home_account);
        accountButton.setOnClickListener(this);

        // Add searchbar

        searchbar = findViewById(R.id.searchbar_home);
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

        // Get groups that user is in
        FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        String userID = user.getUid();
        DocumentReference docRef = db.collection("users").document(userID);
        docRef.get().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    userDocument = document.getData();
                    assert userDocument != null;
                    groups = (HashMap<String, String>) userDocument.get("groups");
                    Log.d(TAG, String.valueOf(groups));

                    // Pass the hashmap of groups to the fragment
                    if (groupview != null) {
                        groupview.updateGroups(groups);

                        fragmentManager.beginTransaction()
                                .replace(R.id.fragment_group_view, groupview)
                                .commit();
                    } else {
                        Log.d(TAG, "fragment is null");
                    }
                } else {
                    Log.d(TAG, "No such document");
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
        if (id == R.id.home_account) {
            Intent toAccount = new Intent(HomeActivity.this, AccountActivity.class);
            startActivity(toAccount);
        }
    }
}