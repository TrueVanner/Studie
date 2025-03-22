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

import java.util.logging.Logger;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

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

        Button account = findViewById(R.id.account_button);
        Button createGroup = findViewById(R.id.create_group_button);
        Button joinGroup = findViewById(R.id.join_group_button);

        Button groupA = findViewById(R.id.temp_groupA_button);
        Button groupB = findViewById(R.id.temp_groupB_button);
        Button groupC = findViewById(R.id.temp_groupC_button);
        Button search = findViewById(R.id.temp_search_button);

        account.setOnClickListener(this);
        createGroup.setOnClickListener(this);
        joinGroup.setOnClickListener(this);
        groupA.setOnClickListener(this);
        groupB.setOnClickListener(this);
        groupC.setOnClickListener(this);
        search.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent toAccount = new Intent(HomeActivity.this, AccountActivity.class);
//        Intent toCreateGroup = new Intent();
//        Intent toJoinGroup = new Intent();
        Intent toGroup = new Intent(HomeActivity.this, GroupActivity.class);
//        Intent toSearch = new Intent();

        if (id == R.id.temp_groupA_button || id == R.id.temp_groupB_button || id == R.id.temp_groupC_button) {
            try {
                startActivity(toGroup);
            } catch (Exception e) {
                Logger.getLogger(HomeActivity.class.getName()).severe("Error switching to group activity");
            }
        } else if(id == R.id.account_button) {
            startActivity(toAccount);
        } else {
            String toastText = "Undefined request";
            if(id == R.id.create_group_button) {
                toastText = "Go to Create Group";
            }
            if(id == R.id.join_group_button) {
                toastText = "Go to Join Group";
            }
            if(id == R.id.temp_search_button) {
                toastText = "Go to Search";
            }
            Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
        }
    }
}