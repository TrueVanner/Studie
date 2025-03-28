package nl.tue.appdev.studie;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SignupActivity";
    private FirebaseAuth mAuth;
    private EditText nameInput;
    private EditText emailInput;
    private EditText passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signup), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        nameInput = findViewById(R.id.input_name);
        emailInput = findViewById(R.id.signup_input_email);
        passwordInput = findViewById(R.id.signup_input_password);
        ImageButton signup = (ImageButton) findViewById(R.id.button_sign_up);
        TextView login = (TextView) findViewById(R.id.hyperlink_to_login);

        signup.setOnClickListener(this);
        login.setOnClickListener(this);
    }

    enum UpdateType { BAD_NAME, BAD_PASSWORD, BAD_EMAIL, BAD_AUTH }

    private void updateUI(UpdateType updateType) {
        switch(updateType) {
            case BAD_NAME:
                nameInput.setError("Name can't be empty!");
                break;
            case BAD_PASSWORD:
                passwordInput.setError("Password can't be empty!");
                break;
            case BAD_EMAIL:
                emailInput.setError("Email can't be empty!");
                break;
            case BAD_AUTH:
                Toast.makeText(SignupActivity.this, "Authentication failed.",
                        Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent toHome = new Intent(SignupActivity.this, HomeActivity.class);
        Intent tologin = new Intent(SignupActivity.this, LoginActivity.class);
        Log.d(TAG, "test1");
        if (id == R.id.button_sign_up) {
            String name = nameInput.getText().toString();
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();

            // Check if fields are correct
            if (name.isBlank()) {
                updateUI(UpdateType.BAD_NAME);
                return;
            }
            if(!email.toLowerCase().matches("[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,6}")) {
                updateUI(UpdateType.BAD_EMAIL);
                return;
            }
            if (password.matches("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$")) {
                updateUI(UpdateType.BAD_PASSWORD);
                return;
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Log.d(TAG, "test2");
            mAuth.createUserWithEmailAndPassword(email.trim(), password.trim())
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");

                            FirebaseUser user = task.getResult().getUser();
                            assert user != null;

                            Map<String, Object> userData = new HashMap<>();
                            userData.put("name", name.trim());
                            userData.put("groups", new HashMap<>());
                            db.collection("users")
                                    .document(user.getUid())
                                    .set(userData)
                                    .addOnSuccessListener(documentReference -> {
                                        Log.d(TAG, "addUserToFirestore:success");
                                        startActivity(toHome);
                                    });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignupActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        } else if (id==R.id.hyperlink_to_login){
            startActivity(tologin);
        } else {
            Toast.makeText(this, "Undefined request", Toast.LENGTH_SHORT).show();
        }
    }
}