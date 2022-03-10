package ru.mark99.appsearcher;

import static ru.mark99.appsearcher.Utils.findByPackage;

import android.content.SharedPreferences;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collections;

class FastStart {
   ArrayList<ItemInList> storage = new ArrayList<>();

   void load(SharedPreferences sp, ArrayList<ItemInList> fullList) {
      String strApps = sp.getString("fast_start_apps", "");
      String[] arrStrApps = strApps.split(",");

      storage.clear();
      for (String packageName : arrStrApps) {
         ItemInList app = findByPackage(fullList, packageName);
         if (app == null) continue;

         storage.add(app);

         if (storage.size() == 5) break;
      }
   }

   void applyToActivity(ArrayList<ImageView> imageViews){
      ArrayList<ItemInList> applyingList = new ArrayList<>(storage);
      Collections.reverse(applyingList);

      for (int i = 0; i < applyingList.size(); i++) {
         ImageView image = imageViews.get(i);
         ItemInList app = applyingList.get(i);
         image.setImageDrawable(app.icon);
         image.setTag(app);
      }
   }

   void addNewItemAndSave(SharedPreferences sp, ItemInList item){
      if (!storage.contains(item))
         storage.add(item);

      while (storage.size() > 5)
         storage.remove(0);

      ArrayList<String> arrStrApp = new ArrayList<>();
      for (ItemInList app : storage)
         arrStrApp.add(app.packageName);

      SharedPreferences.Editor editor = sp.edit();
      String strApps = String.join(",", arrStrApp);
      editor.putString("fast_start_apps", strApps);
      editor.apply();
   }
}
