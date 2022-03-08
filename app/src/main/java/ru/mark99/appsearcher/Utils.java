package ru.mark99.appsearcher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class Utils {

   public static ArrayList<AppItem> getInstalledApps(Context context, boolean scanSystemApps) {
      PackageManager pm = context.getPackageManager();
      ArrayList<AppItem> apps = new ArrayList<>();
      String thisAppPackage = context.getPackageName();

      List<PackageInfo> packs = pm.getInstalledPackages(0);
      for (int i = 0; i < packs.size(); i++) {
         PackageInfo p = packs.get(i);

         boolean isSystem = isSystemPackage(p);
         if (!scanSystemApps && isSystem) continue;

         String appName = p.applicationInfo.loadLabel(pm).toString();
         Drawable icon = p.applicationInfo.loadIcon(pm);
         String packages = p.applicationInfo.packageName;
         if (Objects.equals(thisAppPackage, packages)) continue;

         apps.add(new AppItem(appName, icon, packages, isSystem));
      }

      return apps;
   }

   private static boolean isSystemPackage(PackageInfo pkgInfo) {
      return (pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
   }

   public static AppItem findByPackage(ArrayList<AppItem> fullList, String pack){
      for (AppItem item : fullList) {
         if (Objects.equals(item.getPackages(), pack))
            return item;
      }
      return null;
   }

   public static void searchQuery(Context context, String query, boolean useGoogle){
      String urlPrefix = useGoogle ?
              "https://www.google.com/search?q=" :
              "https://yandex.ru/search/?text=";

      try {
         String url = urlPrefix + URLEncoder.encode(query, "UTF-8");
         Intent i = new Intent(Intent.ACTION_VIEW);
         i.setData(Uri.parse(url));
         context.startActivity(i);
      } catch (UnsupportedEncodingException ignored) {}
   }
}
