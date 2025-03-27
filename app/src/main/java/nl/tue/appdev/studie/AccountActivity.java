package nl.tue.appdev.studie;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AccountActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG = "AccountActivity";

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private String name, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_account), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton back = findViewById(R.id.account_back_button);
        Button logout = findViewById(R.id.account_logout_button);

        back.setOnClickListener(this);
        logout.setOnClickListener(this);

        TextView nameView = findViewById(R.id.account_name_filled);
        TextView emailView = findViewById(R.id.account_email_filled);

        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Update name view
        user = mAuth.getCurrentUser();
        assert user != null;
        String userID = user.getUid();
        DocumentReference docRef = db.collection("users").document(userID);
        docRef.get().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    Map<String, Object> userDocument = document.getData();
                    assert userDocument != null;
                    name = (String) userDocument.get("name");
                    assert name != null;
                    nameView.setText(name);
                    Log.d(TAG, name);
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });

        // Update email view
        email = user.getEmail();
        emailView.setText(email);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent toHome = new Intent(AccountActivity.this, HomeActivity.class);
        Intent toWelcome = new Intent(AccountActivity.this, WelcomeActivity.class);
        if (id == R.id.account_back_button) {
            startActivity(toHome);
        } else if (id == R.id.account_logout_button) {
            mAuth.signOut();
            startActivity(toWelcome);
        } else {
            String toastText = "Undefined request";
            Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
        }
    }
}