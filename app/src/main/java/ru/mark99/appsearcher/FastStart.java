package ru.mark99.appsearcher;

import static ru.mark99.appsearcher.Utils.findByPackage;
import static ru.mark99.appsearcher.Utils.findByUri;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.appcompat.content.res.AppCompatResources;

import java.util.ArrayList;
import java.util.Collections;

class FastStart {
   Drawable noneAppIcon;
   ArrayList<ItemInList> storage = new ArrayList<>();

   void load(Context context, SharedPreferences sp, ArrayList<ItemInList> fullList) {
      noneAppIcon = AppCompatResources.getDrawable(context, R.drawable.ic_baseline_texture_24);
      if (noneAppIcon != null) noneAppIcon.setAlpha(80);

      String strApps = sp.getString("fast_start_apps", "");
      String[] arrStrApps = strApps.split(",");

      storage.clear();
      for (String packageName : arrStrApps) {
         ItemInList app;

         if (packageName.startsWith("[contact]")){
            packageName = packageName.substring(9);
            app = findByUri(fullList, packageName);
         } else
            app = findByPackage(fullList, packageName);

         if (app == null) continue;

         storage.add(app);

         if (storage.size() == 5) break;
      }
   }

   void applyToActivity(ArrayList<ImageView> imageViews){
      ArrayList<ItemInList> applyingList = new ArrayList<>(storage);
      Collections.reverse(applyingList);

      for (int i = 0; i < 5; i++) {
         ImageView image = imageViews.get(i);

         if (i >= applyingList.size()){
            image.setImageDrawable(noneAppIcon);
            image.setTag(null);
         }
         else {
            ItemInList app = applyingList.get(i);
            image.setImageDrawable(app.icon);
            image.setTag(app);
         }
      }
   }

   void addNewItemAndSave(SharedPreferences sp, ItemInList item){
      if (!storage.contains(item))
         storage.add(item);

      while (storage.size() > 5)
         storage.remove(0);

      save(sp);
   }

   private void save(SharedPreferences sp){
      ArrayList<String> arrStrApp = new ArrayList<>();
      for (ItemInList app : storage){
         switch (app.type){
            case SystemApp:
            case App:
               arrStrApp.add(app.packageName);
               break;
            case Contact:
               arrStrApp.add("[contact]" + app.uri.toString());
               break;
         }
      }

      SharedPreferences.Editor editor = sp.edit();
      String strApps = String.join(",", arrStrApp);
      editor.putString("fast_start_apps", strApps);
      editor.apply();
   }

   void removeItem(SharedPreferences sp, ItemInList item){
      storage.remove(item);
      save(sp);
   }
}
