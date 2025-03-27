package nl.tue.appdev.studie;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private static final String TAG = "LoginActivity";

    private EditText emailInput;
    private EditText passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        emailInput = findViewById(R.id.login_input_email);
        passwordInput = findViewById(R.id.login_input_password);

        ImageButton login = (ImageButton) findViewById(R.id.button_log_in);
        TextView gotoSignup = (TextView) findViewById(R.id.hyperlink_to_signup);

        login.setOnClickListener(this);
        gotoSignup.setOnClickListener(this);
    }

    public enum UpdateType { BAD_EMAIL, BAD_PASSWORD, LOGIN_FAILED }

    private void updateUI(UpdateType updateType) {
        switch(updateType) {
            case BAD_EMAIL:
                emailInput.setError("Please enter your email address.");
                break;
            case BAD_PASSWORD:
                passwordInput.setError("Please enter your password.");
                break;
            case LOGIN_FAILED:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent toHome = new Intent(LoginActivity.this, HomeActivity.class);
        Intent toSignup = new Intent(LoginActivity.this, SignupActivity.class);
        if (id == R.id.button_log_in) {
            // Read user inputs
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();

            // Check if fields are nonempty
            if (email.isBlank()) {
                updateUI(UpdateType.BAD_EMAIL);
                return;
            }
            if (password.isBlank()) {
                updateUI(UpdateType.BAD_PASSWORD);
                return;
            }

            // Fields are nonempty, try to login user
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(LoginActivity.class.getSimpleName(), "signInWithEmail:success");
                            startActivity(toHome);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(UpdateType.LOGIN_FAILED);
                        }
                    });
        } else if (id == R.id.hyperlink_to_signup) {
            startActivity(toSignup);
        }
    }
}