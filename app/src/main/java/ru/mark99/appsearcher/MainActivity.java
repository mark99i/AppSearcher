package ru.mark99.appsearcher;

import static ru.mark99.appsearcher.Utils.openApp;
import static ru.mark99.appsearcher.Utils.openContact;
import static ru.mark99.appsearcher.Utils.searchQuery;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.Arrays;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;

    ProgressBar loadingPB;
    EditText textInput;
    ListView listView;
    SwitchMaterial systemAppVisible;
    SwitchMaterial recentlyAppVisible;
    SwitchMaterial useGoogle;
    SwitchMaterial useContacts;
    SwitchMaterial useCache;
    LinearLayoutCompat recentlyAppsLayout;
    ArrayList<ItemInList> fullInstalledApps;
    ArrayList<ItemInList> filteredInstalledApps;
    ArrayList<ImageView> fastStartAppsIV = new ArrayList<>();

    AppAdapter adapter;
    FastStart fastStartApps = new FastStart();

    Cache cache;

    boolean requestPermissionMode = false;

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
        useContacts = findViewById(R.id.main_load_contacts);
        useCache = findViewById(R.id.main_use_cache);

        fastStartAppsIV = new ArrayList<>(Arrays.asList(
                findViewById(R.id.main_fast_start1),
                findViewById(R.id.main_fast_start2),
                findViewById(R.id.main_fast_start3),
                findViewById(R.id.main_fast_start4),
                findViewById(R.id.main_fast_start5)
        ));

        for (ImageView imageView : fastStartAppsIV){
            imageView.setOnClickListener(view -> {
                if (view.getTag() == null) return;
                onItemInListClick((ItemInList) view.getTag());
            });

            imageView.setOnLongClickListener(view -> {
                if (view.getTag() == null) return false;
                fastStartApps.removeItem(sharedPreferences, (ItemInList) view.getTag());
                fastStartApps.applyToActivity(fastStartAppsIV);
                return true;
            });
        }

        systemAppVisible.setChecked(sharedPreferences.getBoolean("showSystem", false));
        recentlyAppVisible.setChecked(sharedPreferences.getBoolean("showRecentlyApps", true));
        useGoogle.setChecked(sharedPreferences.getBoolean("useGoogle", false));
        useCache.setChecked(sharedPreferences.getBoolean("useCache", true));
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
        systemAppVisible.setOnCheckedChangeListener((compoundButton, b) -> onSwitchesChanged(true));
        recentlyAppVisible.setOnCheckedChangeListener((compoundButton, b) -> onSwitchesChanged(false));
        useGoogle.setOnCheckedChangeListener((compoundButton, b) -> onSwitchesChanged(false));
        useCache.setOnCheckedChangeListener((compoundButton, b) -> onSwitchesChanged(true));
        useContacts.setOnCheckedChangeListener((compoundButton, b) -> checkAndRequestContactsPermissions());

        new Thread(this::loadInstalledAppsAsync).start();
    }

    private void checkAndRequestContactsPermissions(){
        if (useContacts.isChecked() && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            requestPermissionMode = true;
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 1999);
            return;
        }
        onSwitchesChanged(true);
    }

    private void reloadCache(){
        new Thread(() -> {
            cache.db.inListDao().deleteAll();
            loadInstalledAppsAsync();
        }).start();
    }

    private void onSwitchesChanged(boolean needReload) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("showSystem", systemAppVisible.isChecked());
        editor.putBoolean("showRecentlyApps", recentlyAppVisible.isChecked());
        editor.putBoolean("useGoogle", useGoogle.isChecked());
        editor.putBoolean("useContacts", useContacts.isChecked());
        editor.putBoolean("useCache", useCache.isChecked());
        editor.apply();
        recentlyAppsLayout.setVisibility(recentlyAppVisible.isChecked() ? View.VISIBLE : View.GONE);
        if (needReload) reloadCache();

        if (!useCache.isChecked() && useContacts.isChecked())
            Toasty.warning(this, "Using contacts search without using the cache can take a long time", Toasty.LENGTH_LONG).show();
    }

    private void onInputTextChanged(){
        String query = textInput.getText().toString();
        if (fullInstalledApps == null || fullInstalledApps.size() == 0) return;

        if (query.contains("\n")) {
            if (filteredInstalledApps.size() == 0) return;
            onItemInListClick(filteredInstalledApps.get(0));   // Handle Enter: start first app in list
        }

        boolean sysApps = systemAppVisible.isChecked();
        boolean contacts = useContacts.isChecked();

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
            // Search in internet and reload cache item
            filteredInstalledApps.add(ItemInList.getSearchInInternetItem(this));
            filteredInstalledApps.add(ItemInList.getReloadCacheItem(this));
        }

        adapter.notifyDataSetChanged();
    }

    private void onItemInListClick(ItemInList item){
        switch (item.type){
            case SearchInInternet:
                searchQuery(this, textInput.getText().toString(), useGoogle.isChecked());
                break;

            case Contact:
                openContact(this, item);
                break;

            case App:
            case SystemApp:
                openApp(this, item);
                break;

            case ReloadCache:
                reloadCache();
                break;
        }

        if (item.type == ItemInList.Type.App || item.type == ItemInList.Type.SystemApp || item.type == ItemInList.Type.Contact){
            fastStartApps.addNewItemAndSave(sharedPreferences, item);
        }
    }

    private void loadInstalledAppsAsync(){
        // disable ui
        this.runOnUiThread(() -> {
            loadingPB.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            recentlyAppsLayout.setVisibility(View.GONE);
            systemAppVisible.setEnabled(false);
            recentlyAppVisible.setEnabled(false);
            useContacts.setEnabled(false);
            useCache.setEnabled(false);
        });

        if (cache == null) cache = new Cache(this);

        ArrayList<ItemInList> loaded;

        if (useCache.isChecked()){
            loaded = new ArrayList<>(cache.db.inListDao().getAll());
            if (loaded.size() == 0)
            {
                Log.i("loadInstalledAppsAsync", "Load cache is 0 positions, full info loading...");
                loaded = Utils.getInstalledApps(this, true);
                if (useContacts.isChecked())
                    loaded.addAll(Utils.getContacts(this));
                cache.saveToCacheFull(loaded);
            }
        }
        else
        {
            loaded = Utils.getInstalledApps(this, systemAppVisible.isChecked());
            if (useContacts.isChecked())
                loaded.addAll(Utils.getContacts(this));
        }


        fastStartApps.load(this, sharedPreferences, loaded);

        ArrayList<ItemInList> finalLoaded = loaded;
        this.runOnUiThread(() -> {
            fullInstalledApps = finalLoaded;
            onInputTextChanged();
            fastStartApps.applyToActivity(fastStartAppsIV);
        });

        // enable ui
        this.runOnUiThread(() -> {
            recentlyAppsLayout.setVisibility(recentlyAppVisible.isChecked() ? View.VISIBLE : View.GONE);
            loadingPB.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            systemAppVisible.setEnabled(true);
            recentlyAppVisible.setEnabled(true);
            useContacts.setEnabled(true);
            useCache.setEnabled(true);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        textInput.requestFocus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!requestPermissionMode)
            finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != 1999) return;

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
            useContacts.setChecked(true);
            onSwitchesChanged(true);
        } else {
            useContacts.setChecked(false);
            Toasty.error(this, "No read_contacts permissions", Toasty.LENGTH_SHORT).show();
        }

        requestPermissionMode = false;
    }
}