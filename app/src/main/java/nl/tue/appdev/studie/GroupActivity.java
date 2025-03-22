package nl.tue.appdev.studie;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import nl.tue.appdev.studie.databinding.ActivityGroupBinding;

public class GroupActivity extends AppCompatActivity {

    private ActivityGroupBinding binding;
    TabLayout tabLayout;
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        GroupPagerAdapter groupPagerAdapter = new GroupPagerAdapter(this, getSupportFragmentManager(), tabLayout.getTabCount());
        //viewPager = binding.viewPager;
        viewPager.setAdapter(groupPagerAdapter);
        //tabLayout = binding.tabs;
        tabLayout.setupWithViewPager(viewPager);
        FloatingActionButton fab = binding.fab;

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

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .setAnchorView(R.id.fab).show();
            }
        });
    }
}