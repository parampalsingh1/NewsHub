package com.cosc3p97.newshub;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.cosc3p97.newshub.dao.AppDatabase;
import com.cosc3p97.newshub.fragments.BookmarkFragment;
import com.cosc3p97.newshub.fragments.EntertainmentFragment;
import com.cosc3p97.newshub.fragments.FilterFragment;
import com.cosc3p97.newshub.fragments.HealthFragment;
import com.cosc3p97.newshub.fragments.HomeFragment;
import com.cosc3p97.newshub.fragments.ScienceFragment;
import com.cosc3p97.newshub.fragments.SearchFragment;
import com.cosc3p97.newshub.fragments.SettingsFragment;
import com.cosc3p97.newshub.fragments.SportsFragment;
import com.cosc3p97.newshub.models.Model;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private BottomNavigationView bottomNavigationView;
    private ImageView bookmarkIcon, settingsIcon, filterIcon;
    private SearchView searchView;
    private String lastQuery; // Store the last search query

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bookmarkIcon = findViewById(R.id.bookmarkIcon);
        settingsIcon = findViewById(R.id.settingsIcon);
        searchView = findViewById(R.id.searchView);
        filterIcon = findViewById(R.id.filterIcon);

        // Initially hide the filter icon
        filterIcon.setVisibility(ImageView.GONE);

        // Load the default fragment (e.g., HomeFragment)
        loadFragment(new HomeFragment());

        bottomNavigationView.setOnItemSelectedListener(item -> {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

            if (item.getItemId() == R.id.nav_home) {
                ft.replace(R.id.content, new HomeFragment());
            } else if (item.getItemId() == R.id.nav_science) {
                ft.replace(R.id.content, new ScienceFragment());
            } else if (item.getItemId() == R.id.nav_sports) {
                ft.replace(R.id.content, new SportsFragment());
            } else if (item.getItemId() == R.id.nav_health) {
                ft.replace(R.id.content, new HealthFragment());
            } else if (item.getItemId() == R.id.nav_entertainment) {
                ft.replace(R.id.content, new EntertainmentFragment());
            }

            ft.commit();
            return true;
        });


        bookmarkIcon.setOnClickListener(v -> {
            new Thread(() -> {
                List<Model> bookmarks = AppDatabase.getDatabase(MainActivity.this).bookmarkDao().getAllBookmarks();
                if (bookmarks != null && !bookmarks.isEmpty()) {
                    runOnUiThread(() -> loadFragment(new BookmarkFragment()));
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "No bookmarks available.", Toast.LENGTH_SHORT).show()
                    );
                }
            }).start();
        });
        settingsIcon.setOnClickListener(v -> {
            loadFragment(new SettingsFragment());
        });
        settingsIcon.setOnClickListener(v -> loadFragment(new SettingsFragment()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.trim().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Search query cannot be empty", Toast.LENGTH_SHORT).show();
                    return false;
                }
                loadSearchFragment(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        filterIcon.setOnClickListener(v -> {
            if (!isFinishing() && !isDestroyed()) {
                showFilters(lastQuery); // Pass the last query to the filter fragment
            } else {
                Toast.makeText(MainActivity.this, "Activity is not in a valid state to show popup", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showFilters(String query) {
        FilterFragment filterFragment = FilterFragment.newInstance(query); // Pass the query
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content, filterFragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    public void loadSearchFragment(String query) {
        lastQuery = query; // Save the last query for filters
        SearchFragment searchFragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString("query", query); // Pass the query to the search fragment
        searchFragment.setArguments(args);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content, searchFragment);
        ft.commit();

        updateFilterIconVisibility(true);
    }

    private void updateFilterIconVisibility(boolean hasResults) {
        if (hasResults) {
            filterIcon.setVisibility(ImageView.VISIBLE);
        } else {
            filterIcon.setVisibility(ImageView.GONE);
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content, fragment);
        ft.commit();
    }
}
