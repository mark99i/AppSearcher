package ru.mark99.appsearcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.room.Dao;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Objects;

@Entity(tableName = "cachedItems")
@TypeConverters({ItemInList.TConverter.class})
public class ItemInList {
    enum Type {
        SystemApp,
        App,
        Contact,
        SearchInInternet,
        ReloadCache
    }

    @PrimaryKey(autoGenerate = true) int id;
    @NonNull String name = "";
    @NonNull String number = "";
    Drawable icon;
    @NonNull String packageName = "";
    Uri uri;
    Type type;

    static ItemInList getSearchInInternetItem(Context context){
        ItemInList item = new ItemInList();
        item.icon = AppCompatResources.getDrawable(context, R.drawable.ic_search_internet_24);
        item.name = "Search in the Internet";
        item.type = ItemInList.Type.SearchInInternet;
        return item;
    }

    static ItemInList getReloadCacheItem(Context context){
        ItemInList item = new ItemInList();
        item.icon = AppCompatResources.getDrawable(context, R.drawable.ic_baseline_autorenew_24);
        item.name = "Reload cache";
        item.type = Type.ReloadCache;
        return item;
    }

    @Dao
    public interface ItemInListDao {
        @Query("SELECT * FROM cachedItems")
        List<ItemInList> getAll();

        @Query("SELECT * FROM cachedItems WHERE name LIKE '%' || :name || '%'")
        List<ItemInList> findByName(String name);

        @Insert
        void insertAll(List<ItemInList> item);

        @Query("DELETE FROM cachedItems")
        void deleteAll();
    }

    public static class TConverter {
        @TypeConverter
        public byte[] DrawableToBytes(Drawable drawable){
            Context context = Cache.ContextLink.get();
            if (context == null){
                Log.e("BytestoDrawable", "context == null, returns null");
                return null;
            }

            final Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            final Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            return stream.toByteArray();
        }

        @TypeConverter
        public Drawable BytestoDrawable(byte[] bytes){
            Context context = Cache.ContextLink.get();
            if (context == null){
                Log.e("BytestoDrawable", "context == null, returns null");
                return null;
            }
            return new BitmapDrawable(context.getResources(), BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
        }

        @TypeConverter
        public String UriToString(Uri uri){
            if (uri == null) return "";
            return uri.toString();
        }

        @TypeConverter
        public Uri StringToUri(String str){
            if (Objects.equals(str, "")) return null;
            return Uri.parse(str);
        }
    }
}

