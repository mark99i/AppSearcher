package ru.mark99.appsearcher;

import android.graphics.drawable.Drawable;
import android.net.Uri;

class ItemInList {
    enum Type {
        SystemApp,
        App,
        Contact,
        SearchInInternet
    }

    String id;
    String name;
    String number;
    Drawable icon;
    String packageName;
    Uri uri;
    Type type;

    public ItemInList(){}

    public ItemInList(String name, Drawable icon, String packageName, Type type) {
        this.name = name;
        this.icon = icon;
        this.packageName = packageName;
        this.type = type;
    }
}
