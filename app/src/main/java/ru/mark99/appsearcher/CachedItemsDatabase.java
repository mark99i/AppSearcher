package ru.mark99.appsearcher;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {ItemInList.class}, version = 1, exportSchema = false)
public abstract class CachedItemsDatabase extends RoomDatabase {
    public abstract ItemInList.ItemInListDao inListDao();
}
