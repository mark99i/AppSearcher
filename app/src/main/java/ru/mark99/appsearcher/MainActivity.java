package ru.mark99.appsearcher;

import static ru.mark99.appsearcher.Utils.openContact;
import static ru.mark99.appsearcher.Utils.searchQuery;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;

    Button reloadCache;
    ProgressBar loadingPB;
    EditText textInput;
    ListView listView;
    SwitchMaterial systemAppVisible;
    SwitchMaterial recentlyAppVisible;
    SwitchMaterial useGoogle;
    SwitchMaterial useContacts;
    LinearLayoutCompat recentlyAppsLayout;
    ArrayList<ItemInList> fullInstalledApps;
    ArrayList<ItemInList> filteredInstalledApps;
    ArrayList<ImageView> fastStartAppsIV = new ArrayList<>();

    AppAdapter adapter;
    FastStart fastStartApps = new FastStart();

    Cache cache;

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
        reloadCache = findViewById(R.id.main_reload_cache);
        useContacts = findViewById(R.id.main_load_contacts);

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
        useContacts.setChecked(
                sharedPreferences.getBoolean("useContacts", false) &&
                checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED);

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
        useContacts.setOnCheckedChangeListener((compoundButton, b) -> checkAndRequestContactsPermissions());
        reloadCache.setOnClickListener(view -> new Thread(() -> {
            cache.db.inListDao().deleteAll();
            loadInstalledAppsAsync();
        }).start());

        new Thread(this::loadInstalledAppsAsync).start();
    }

    private void checkAndRequestContactsPermissions(){
        if (useContacts.isChecked() && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 1999);
            return;
        }
        onSwitchesChanged();
    }

    private void onSwitchesChanged(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("showSystem", systemAppVisible.isChecked());
        editor.putBoolean("showRecentlyApps", recentlyAppVisible.isChecked());
        editor.putBoolean("useGoogle", useGoogle.isChecked());
        editor.putBoolean("useContacts", useContacts.isChecked());
        editor.apply();
        new Thread(this::loadInstalledAppsAsync).start();
    }

    @SuppressWarnings("CommentedOutCode")
    private void onInputTextChanged(){
        String query = textInput.getText().toString();
        if (fullInstalledApps == null || fullInstalledApps.size() == 0) return;

        if (query.contains("\n")) {
            if (filteredInstalledApps.size() == 0) return;
            onItemInListClick(filteredInstalledApps.get(0));   // Handle Enter: start first app in list
        }

        boolean sysApps = sharedPreferences.getBoolean("showSystem", false);
        boolean contacts = sharedPreferences.getBoolean("useContacts", false);

        filteredInstalledApps.clear();
        //filteredInstalledApps.addAll(cache.ge(query, sharedPreferences.getBoolean("showSystem", false)));

        for (ItemInList item : fullInstalledApps) {
            if (item.name.toLowerCase().contains(query.toLowerCase())) {
                if (item.type == ItemInList.Type.Contact && !contacts) continue;
                if (item.type == ItemInList.Type.SystemApp && !sysApps) continue;
                filteredInstalledApps.add(item);
            }
        }

        if (query.length() > 0){
            // Search in internet item
            ItemInList item = new ItemInList();
            item.icon = AppCompatResources.getDrawable(this, R.drawable.ic_search_internet_24);
            item.name = "Search in the Internet";
            item.type = ItemInList.Type.SearchInInternet;
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
        this.runOnUiThread(() -> {
            loadingPB.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            recentlyAppsLayout.setVisibility(View.GONE);
            systemAppVisible.setEnabled(false);
            recentlyAppVisible.setEnabled(false);
            useContacts.setEnabled(false);
            reloadCache.setEnabled(false);
        });

        boolean recenltyEnabled = sharedPreferences.getBoolean("showRecentlyApps", true);

        cache = new Cache(this);

        ArrayList<ItemInList> loaded = cache.resolveQuery("", true);
        if (loaded.size() == 0)
        {
            Log.i("loadInstalledAppsAsync", "Load cache is 0 positions, full info loading...");
            loaded = Utils.getInstalledApps(this, true);
            if (useContacts.isChecked())
                loaded.addAll(Utils.getContacts(this));
            cache.saveToCacheFull(loaded);
        }

        fastStartApps.load(sharedPreferences, loaded);

        ArrayList<ItemInList> finalLoaded = loaded;
        this.runOnUiThread(() -> {
            fullInstalledApps = finalLoaded;
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
        this.runOnUiThread(() -> useContacts.setEnabled(true));
        this.runOnUiThread(() -> reloadCache.setEnabled(true));
    }

    @Override
    protected void onStart() {
        super.onStart();
        textInput.requestFocus();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != 1999) return;

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
            useContacts.setChecked(true);
            onSwitchesChanged();
            reloadCache.performClick();
        } else {
            useContacts.setChecked(false);
            Toast.makeText(this, "No read_contacts permissions", Toast.LENGTH_SHORT).show();
        }
    }
}