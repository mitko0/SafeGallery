package com.example.safegallery.activities;

import android.os.Bundle;
import com.example.safegallery.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import com.example.safegallery.tabs.main.SectionsPagerAdapter;

public class TabbedActivity extends AppCompatActivity {

    SectionsPagerAdapter sectionsPagerAdapter;
    ViewPager viewPager;
    TabLayout tabs;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);

        this.init();
        this.viewPager.setAdapter(this.sectionsPagerAdapter);
        this.tabs.setupWithViewPager(this.viewPager);

        this.fab.setOnClickListener(view -> Snackbar.make(view,
                "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());
    }

    private void init() {
        this.sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        this.viewPager = findViewById(R.id.view_pager);
        this.tabs = findViewById(R.id.tabs);
        this.fab = findViewById(R.id.fab);
    }
}