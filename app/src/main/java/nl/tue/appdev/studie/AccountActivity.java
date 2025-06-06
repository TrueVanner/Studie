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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.util.Map;

public class AccountActivity extends AppCompatActivity implements View.OnClickListener {
    //SonarQube is crying over the TAG declaration, but I think this looks better
    private final String TAG = "AccountActivity";

    private FirebaseAuth mAuth;

    private String name;

    private TextView nameView;
    private TextView emailView;


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
        setupUI();
    }

    public void setupUI() {
        //Declarations for the UI elements
        ImageButton back = findViewById(R.id.account_back_button);
        Button logout = findViewById(R.id.account_logout_button);
        //setting OnClickListeners
        back.setOnClickListener(this);
        logout.setOnClickListener(this);

        nameView = findViewById(R.id.account_name_filled);
        emailView = findViewById(R.id.account_email_filled);

        //Setting up the firebase declarations
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Update name view
        FirebaseUser user;
        user = mAuth.getCurrentUser();
        assert user != null;
        String userID = user.getUid();
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
                            name = (String) userDocument.get("name");
                            assert name != null;
                            nameView.setText(name);
                            Log.d(TAG, name);
                        } else {
                            Log.e(TAG, "No such document");
                        }
                    } else {
                        Log.e(TAG, "get failed with ", task.getException());
                    }
                });
        // Update email view
        String email;
        email = user.getEmail();
        emailView.setText(email);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent toHome = new Intent(AccountActivity.this, HomeActivity.class);
        Intent toWelcome = new Intent(AccountActivity.this, WelcomeActivity.class);
        if (id == R.id.account_back_button) {
            //If the back button is pressed, the user is sent to the home activity
            startActivity(toHome);
        } else if (id == R.id.account_logout_button) {
            //If the logout button is pressed, the user gets signed out and is returned
            //to the welcome activity
            mAuth.signOut();
            startActivity(toWelcome);
        } else {
            //If the app receives an unknown onclick, throw the following error message
            String toastText = "Undefined request";
            Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
        }
    }
}