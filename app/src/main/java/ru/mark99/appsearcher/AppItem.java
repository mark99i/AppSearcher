package ru.mark99.appsearcher;

import android.graphics.drawable.Drawable;

class AppItem {
    private final String name;
    private final Drawable icon;
    private final String packages;
    private final Boolean isSystem;

    public AppItem(String name, Drawable icon, String packages, boolean isSystem) {
        this.name = name;
        this.icon = icon;
        this.packages = packages;
        this.isSystem = isSystem;
    }
    public String getName() {
        return name;
    }
    public Drawable getIcon() {
        return icon;
    }
    public String getPackages() {
        return packages;
    }
    public Boolean getIsSystem() {
        return isSystem;
    }
}
