package nl.tue.appdev.studie;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.intellij.lang.annotations.RegExp;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.*;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SignupActivity";
    private FirebaseAuth mAuth;
    private EditText nameInput, emailInput, passwordInput;

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
        Button signup = findViewById(R.id.button_sign_up);

        signup.setOnClickListener(this);
    }

    enum UpdateType { BAD_NAME, BAD_PASSWORD, BAD_EMAIL, BAD_AUTH }

    public void updateUI(UpdateType updateType) {
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
        if (id == R.id.signup_button_create) {
            String name = nameInput.getText().toString();
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();

            // Check if fields are correct
            if (name.isBlank()) {
                updateUI(UpdateType.BAD_NAME);
                return;
            }
            if(!email.matches("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$")) {
                updateUI(UpdateType.BAD_EMAIL);
                return;
            }
            if (password.matches("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$")) {
                updateUI(UpdateType.BAD_PASSWORD);
                return;
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            mAuth.createUserWithEmailAndPassword(email.trim(), password.trim())
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");

                            FirebaseUser user = task.getResult().getUser();
                            assert user != null;

                            Map<String, Object> userData = new HashMap<>();
                            userData.put("name", name.trim());
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
        } else {
            Toast.makeText(this, "Undefined request", Toast.LENGTH_SHORT).show();
        }
    }
}