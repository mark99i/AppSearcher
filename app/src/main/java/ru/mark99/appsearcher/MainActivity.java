package ru.mark99.appsearcher;

import static ru.mark99.appsearcher.Utils.openContact;
import static ru.mark99.appsearcher.Utils.searchQuery;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;

    ProgressBar loadingPB;
    EditText textInput;
    ListView listView;
    SwitchMaterial systemAppVisible;
    SwitchMaterial recentlyAppVisible;
    SwitchMaterial useGoogle;
    LinearLayoutCompat recentlyAppsLayout;
    ArrayList<ItemInList> fullInstalledApps;
    ArrayList<ItemInList> filteredInstalledApps;
    ArrayList<ImageView> fastStartAppsIV = new ArrayList<>();

    AppAdapter adapter;
    FastStart fastStartApps = new FastStart();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.hide();

        sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE);

        textInput = findViewById(R.id.main_input);
        listView = findViewById(R.id.main_list);
        systemAppVisible = findViewById(R.id.main_system_app_visible);
        recentlyAppVisible = findViewById(R.id.main_recently_apps);
        useGoogle = findViewById(R.id.main_use_google);
        recentlyAppsLayout = findViewById(R.id.main_recently_apps_layout);
        loadingPB = findViewById(R.id.main_loading_progress_bar);

        fastStartAppsIV = new ArrayList<>(Arrays.asList(
                findViewById(R.id.main_fast_start1),
                findViewById(R.id.main_fast_start2),
                findViewById(R.id.main_fast_start3),
                findViewById(R.id.main_fast_start4),
                findViewById(R.id.main_fast_start5)
        ));

        for (ImageView imageView : fastStartAppsIV){
            imageView.setOnClickListener(view -> {
                if (view.getTag() != null){
                    onItemInListClick((ItemInList) view.getTag());
                }
            });
        }

        systemAppVisible.setChecked(sharedPreferences.getBoolean("showSystem", false));
        recentlyAppVisible.setChecked(sharedPreferences.getBoolean("showRecentlyApps", true));
        useGoogle.setChecked(sharedPreferences.getBoolean("useGoogle", false));

        filteredInstalledApps = new ArrayList<>();
        adapter = new AppAdapter(this, filteredInstalledApps);
        listView.setAdapter(adapter);

        textInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                onInputTextChanged();
            }
        });
        listView.setOnItemClickListener((adapterView, view, i, l) -> onItemInListClick(filteredInstalledApps.get(i)));
        systemAppVisible.setOnCheckedChangeListener((compoundButton, b) -> onSwitchesChanged());
        recentlyAppVisible.setOnCheckedChangeListener((compoundButton, b) -> onSwitchesChanged());
        useGoogle.setOnCheckedChangeListener((compoundButton, b) -> onSwitchesChanged());

        new Thread(this::loadInstalledAppsAsync).start();
    }

    private void onSwitchesChanged(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("showSystem", systemAppVisible.isChecked());
        editor.putBoolean("showRecentlyApps", recentlyAppVisible.isChecked());
        editor.putBoolean("useGoogle", useGoogle.isChecked());
        editor.apply();
        new Thread(this::loadInstalledAppsAsync).start();
    }

    private void onInputTextChanged(){
        String query = textInput.getText().toString();
        if (fullInstalledApps == null || fullInstalledApps.size() == 0) return;

        if (query.contains("\n")) {
            if (filteredInstalledApps.size() == 0) return;
            onItemInListClick(filteredInstalledApps.get(0));   // Handle Enter: start first app in list
        }

        filteredInstalledApps.clear();
        for (ItemInList ItemInList : fullInstalledApps) {
            if (ItemInList.name.toLowerCase().contains(query.toLowerCase()))
                filteredInstalledApps.add(ItemInList);
        }

        if (query.length() > 0){
            // Search in internet item
            ItemInList item = new ItemInList(
                    "Search in the Internet",
                    AppCompatResources.getDrawable(this, R.drawable.ic_search_internet_24),
                    "_search_", ItemInList.Type.SearchInInternet);
            filteredInstalledApps.add(item);
        }

        adapter.notifyDataSetChanged();
    }

    private void onItemInListClick(ItemInList clicked){
        if (clicked.type == ItemInList.Type.SearchInInternet){
            searchQuery(this, textInput.getText().toString(), sharedPreferences.getBoolean("useGoogle", false));
            finish();
            return;
        }

        if (clicked.type == ItemInList.Type.Contact){
            openContact(this, clicked.uri);
            finish();
            return;
        }

        Intent intent = getPackageManager().getLaunchIntentForPackage(clicked.packageName);
        if(intent != null){
            startActivity(intent);
            fastStartApps.addNewItemAndSave(sharedPreferences, clicked);
            finish();
        }
        else {
            Toast.makeText(this, clicked.name + " can't be open (no have launch intent)",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void loadInstalledAppsAsync(){
        this.runOnUiThread(() -> loadingPB.setVisibility(View.VISIBLE));
        this.runOnUiThread(() -> listView.setVisibility(View.GONE));
        this.runOnUiThread(() -> recentlyAppsLayout.setVisibility(View.GONE));
        this.runOnUiThread(() -> systemAppVisible.setEnabled(false));
        this.runOnUiThread(() -> recentlyAppVisible.setEnabled(false));

        boolean recenltyEnabled = sharedPreferences.getBoolean("showRecentlyApps", true);

        ArrayList<ItemInList> loaded = Utils.getInstalledApps(this, sharedPreferences.getBoolean("showSystem", false));
        //ArrayList<ItemInList> loaded = Utils.getContacts(this);
        fastStartApps.load(sharedPreferences, loaded);

        this.runOnUiThread(() -> {
            fullInstalledApps = loaded;
            onInputTextChanged();

            if (recenltyEnabled){
                fastStartApps.applyToActivity(fastStartAppsIV);
                this.runOnUiThread(() -> recentlyAppsLayout.setVisibility(View.VISIBLE));
            }
            else{
                this.runOnUiThread(() -> recentlyAppsLayout.setVisibility(View.GONE));
            }
        });

        this.runOnUiThread(() -> loadingPB.setVisibility(View.GONE));
        this.runOnUiThread(() -> listView.setVisibility(View.VISIBLE));
        this.runOnUiThread(() -> systemAppVisible.setEnabled(true));
        this.runOnUiThread(() -> recentlyAppVisible.setEnabled(true));
    }

    @Override
    protected void onStart() {
        super.onStart();
        textInput.requestFocus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}