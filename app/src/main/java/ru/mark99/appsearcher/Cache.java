package ru.mark99.appsearcher;

import android.content.Context;
import android.util.Log;

import androidx.room.Room;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

class Cache {
   public static WeakReference<Context> ContextLink = null;
   CachedItemsDatabase db = null;

   @SuppressWarnings("ResultOfMethodCallIgnored")
   Cache(Context context){
      ContextLink = new WeakReference<>(context);
      boolean res = openDatabase();
      if (!res){
         String path = context.getDatabasePath("cacheditems").toString();
         File file = new File(path);
         if (file.exists())
            file.delete();
         openDatabase();
      }
   }

   private boolean openDatabase(){
      this.db = Room.databaseBuilder(Cache.ContextLink.get(), CachedItemsDatabase.class, "cacheditems").allowMainThreadQueries().build();
      try {
         this.db.inListDao().findByName("111111111111111");
      } catch (Exception e){
         this.db.close();
         return false;
      }
      return true;
   }

   void saveToCacheFull(List<ItemInList> items){
      ItemInList.ItemInListDao dao = this.db.inListDao();
      dao.deleteAll();
      dao.insertAll(items);
      Log.i("CacheDB", "Saved " + items.size() + " positions");
   }
}
