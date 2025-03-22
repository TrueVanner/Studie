package nl.tue.appdev.studie;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.welcome);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.welcome), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        Button login = findViewById(R.id.button_login);
        Button signup = findViewById(R.id.button_signup);

        signup.setOnClickListener(this);
        login.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            Intent toHome = new Intent(WelcomeActivity.this, HomeActivity.class);
            startActivity(toHome);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent toLogin = new Intent(WelcomeActivity.this, LoginActivity.class);
        if (id == R.id.button_login) {
            startActivity(toLogin);
        } else {
            String toastText = "Undefined request";
            if (id == R.id.button_signup) {
                toastText = "Go to Sign Up";
            }
            Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
        }
    }
}