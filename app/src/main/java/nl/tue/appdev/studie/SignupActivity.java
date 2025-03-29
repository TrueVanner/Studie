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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SignupActivity";
    private FirebaseAuth mAuth;
    private EditText nameInput;
    private EditText emailInput;
    private EditText passwordInput;
    private boolean usernameAvailable = true;



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

    enum UpdateType { BAD_NAME, BAD_PASSWORD, BAD_EMAIL, BAD_AUTH, INSECURE_PW, USERNAME_TAKEN }

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
            case USERNAME_TAKEN:
                nameInput.setError("This username is already used by another user.");
                break;
            case INSECURE_PW:
                passwordInput.setError("Password is insecure.");
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent toHome = new Intent(SignupActivity.this, HomeActivity.class);
        Intent tologin = new Intent(SignupActivity.this, LoginActivity.class);
        if (id == R.id.button_sign_up) {
            String name = nameInput.getText().toString();
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Check if fields are correct
            if (name.isBlank()) {
                updateUI(UpdateType.BAD_NAME);
                return;
            }
            if (password.isBlank()) {
                updateUI((UpdateType.BAD_PASSWORD));
                return;
            }
            if(!email.toLowerCase().matches("[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,6}")) {
                updateUI(UpdateType.BAD_EMAIL);
                return;
            }
            if (!password.matches("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{10,}$")) {
                updateUI(UpdateType.INSECURE_PW);
                return;
            }

            // Check if email and username are already used
//            db.collection("users").whereEqualTo("name", name).get()
//                            .addOnCompleteListener(this,task -> {
//                                if (task.isSuccessful()) {
//                                    for (QueryDocumentSnapshot document : task.getResult()) {
//                                        Log.d(TAG, document.getId() + " => " + document.getData());
//                                        usernameAvailable = false;
//                                    }
//                                } else {
//                                    Log.d(TAG, "Query failed");
//                                }
//                            });
//
//            if (!usernameAvailable) {
//                updateUI(UpdateType.USERNAME_TAKEN);
//                return;
//            }

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