package com.example.safegallery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.example.safegallery.Constants;
import com.example.safegallery.R;
import com.example.safegallery.tabs.data.DataType;
import com.example.safegallery.tabs.data.StorageData;
import com.example.safegallery.dialogs.PasswordDialog;
import com.example.safegallery.tabs.fragments.ParentFragment;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import com.example.safegallery.tabs.main.SectionsPagerAdapter;

import java.io.File;

public class TabbedActivity extends AppCompatActivity {

    SectionsPagerAdapter sectionsPagerAdapter;
    ViewPager viewPager;
    TabLayout tabs;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);

        this.init();
        this.viewPager.setAdapter(this.sectionsPagerAdapter);
        this.tabs.setupWithViewPager(this.viewPager);

        this.setSupportActionBar(this.toolbar);
        this.createSafeFolders();
        this.passwordUpdate();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = this.getSupportFragmentManager();
        for (Fragment fragment : fm.getFragments()) {
            if (fragment instanceof ParentFragment && this.viewPager.getCurrentItem() == ((ParentFragment) fragment).getPosition()) {
                FragmentManager childFm = fragment.getChildFragmentManager();
                if (childFm.getBackStackEntryCount() > 0) {
                    childFm.popBackStack();
                    return;
                }
            }
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.changePassword:
                PasswordDialog passwordDialog = new PasswordDialog();
                passwordDialog.show(this.getSupportFragmentManager(), "password dialog");
                return true;
            case R.id.refreshData:
                /*FragmentManager fm = this.getSupportFragmentManager();
                for (Fragment fragment : fm.getFragments()) {
                    if (fragment instanceof ParentFragment && this.viewPager.getCurrentItem() == ((ParentFragment) fragment).getPosition()) {
                        FragmentManager childFm = fragment.getChildFragmentManager();
                        if (childFm.getBackStackEntryCount() > 0) {
                            childFm.popBackStack();
                            return;
                        }
                    }
                }*/
            default:
                Toast.makeText(this, ":D", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void init() {
        this.toolbar = findViewById(R.id.toolbar);
        this.sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        this.viewPager = findViewById(R.id.view_pager);
        this.tabs = findViewById(R.id.tabs);
    }

    private void createSafeFolders() {
        File tmpFolder = new File(StorageData.TMP_FOLDER);
        //noinspection ResultOfMethodCallIgnored
        tmpFolder.mkdirs();

        File intFolder = new File(StorageData.INTRUDERS_FOLDER);
        //noinspection ResultOfMethodCallIgnored
        intFolder.mkdirs();

        for (DataType dataType : DataType.values()) {
            String path = StorageData.APP_SAFE_DATA_PATH + "Safe" + dataType.name();
            File file = new File(path);
            //noinspection ResultOfMethodCallIgnored
            file.mkdirs();
        }
    }

    private void passwordUpdate() {
        Intent intent = this.getIntent();
        if (intent.getBooleanExtra(Constants.SHOW_PASSWORD_DIALOG, true)) {
            PasswordDialog passwordDialog = new PasswordDialog();
            passwordDialog.show(this.getSupportFragmentManager(), "password dialog");
        }
    }
}