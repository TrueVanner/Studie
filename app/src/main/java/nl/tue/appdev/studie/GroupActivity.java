package nl.tue.appdev.studie;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import nl.tue.appdev.studie.databinding.ActivityGroupBinding;

public class GroupActivity extends AppCompatActivity {

    private String TAG = "GroupActivity";

    private ActivityGroupBinding binding;
    TabLayout tabLayout;
    ViewPager viewPager;

    String groupId;

    private FirebaseAuth mAuth;

    private Map<String, Object> userDocument;

    private Vector<Flashcard> flashcards = new Vector<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get group id from home screen intent
        Intent intent = getIntent();
        groupId = intent.getStringExtra("id");

        if (groupId != null) {
            Log.d(TAG, groupId);
        }

        binding = ActivityGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tabLayout = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.view_pager);

        // add tabs
        tabLayout.addTab(tabLayout.newTab().setText("Notes"));
        tabLayout.addTab(tabLayout.newTab().setText("Flashcards"));
        tabLayout.addTab(tabLayout.newTab().setText("Sets"));

        // set width to screen width
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        GroupPagerAdapter groupPagerAdapter = new GroupPagerAdapter(this, getSupportFragmentManager(), tabLayout.getTabCount(), groupId);
        viewPager = binding.viewPager;
        viewPager.setAdapter(groupPagerAdapter);
        tabLayout = binding.tabs;
        tabLayout.setupWithViewPager(viewPager);
        FloatingActionButton manage_fab = binding.manageFab;
        FloatingActionButton back_fab = binding.backFab;

        // connect viewpager changes to tab layout
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        // connect tab layout changes to viewpager
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        manage_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toManage = new Intent(GroupActivity.this, ManageGroupActivity.class);
                toManage.putExtra("id", groupId);
                startActivity(toManage);
            }
        });
        back_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toHome = new Intent(GroupActivity.this, HomeActivity.class);
                startActivity(toHome);
            }
        });

    }
}