package ru.mark99.appsearcher;

import static ru.mark99.appsearcher.Utils.findByPackage;

import android.content.SharedPreferences;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collections;

class FastStartApps {
   ArrayList<AppItem> storage = new ArrayList<>();

   void load(SharedPreferences sp, ArrayList<AppItem> fullList) {
      String strApps = sp.getString("fast_start_apps", "");
      String[] arrStrApps = strApps.split(",");

      storage.clear();
      for (String packageName : arrStrApps) {
         AppItem app = findByPackage(fullList, packageName);
         if (app == null) continue;

         storage.add(app);

         if (storage.size() == 5) break;
      }
   }

   void applyToActivity(ArrayList<ImageView> imageViews){
      ArrayList<AppItem> applyingList = new ArrayList<>(storage);
      Collections.reverse(applyingList);

      for (int i = 0; i < applyingList.size(); i++) {
         ImageView image = imageViews.get(i);
         AppItem app = applyingList.get(i);
         image.setImageDrawable(app.getIcon());
         image.setTag(app.getPackages());
      }
   }

   void addNewItemAndSave(SharedPreferences sp, AppItem item){
      if (!storage.contains(item))
         storage.add(item);

      while (storage.size() > 5)
         storage.remove(0);

      ArrayList<String> arrStrApp = new ArrayList<>();
      for (AppItem app : storage)
         arrStrApp.add(app.getPackages());

      SharedPreferences.Editor editor = sp.edit();
      String strApps = String.join(",", arrStrApp);
      editor.putString("fast_start_apps", strApps);
      editor.apply();
   }
}
