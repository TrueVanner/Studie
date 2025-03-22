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

public class AccountActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.fragment_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.account), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button back = findViewById(R.id.back_button_account);
        Button logout = findViewById(R.id.logout_button_account);

        back.setOnClickListener(this);
        logout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent toHome = new Intent(AccountActivity.this, HomeActivity.class);
        Intent toWelcome = new Intent(AccountActivity.this, WelcomeActivity.class);
        if (id == R.id.back_button_account) {
            startActivity(toHome);
        } else if (id == R.id.logout_button_account) {
                startActivity(toWelcome);
        } else {
            String toastText = "Undefined request";
            Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
        }
    }
}