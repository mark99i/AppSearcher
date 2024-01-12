package ru.mark99.appsearcher;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;

import androidx.appcompat.content.res.AppCompatResources;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

class Utils {

   @SuppressLint("Range")
   public static ArrayList<ItemInList> getContacts(Context context) {

      ArrayList<ItemInList> list = new ArrayList<>();

      ContentResolver contentResolver = context.getContentResolver();
      Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
      if (cursor.getCount() > 0) {
         while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
               Cursor cursorInfo = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                       ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
               InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(),
                       ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(id)));

               Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(id));

               Bitmap photo = null;
               if (inputStream != null) {
                  photo = BitmapFactory.decodeStream(inputStream);

               }
               while (cursorInfo.moveToNext()) {
                  ItemInList info = new ItemInList();
                  info.type = ItemInList.Type.Contact;
                  info.uri = person;
                  info.name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                  if (findByName(list, info.name) != null) continue;
                  info.number = cursorInfo.getString(cursorInfo.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                  info.icon = inputStream != null ?
                          new BitmapDrawable(context.getResources(), photo) :
                          AppCompatResources.getDrawable(context, R.drawable.ic_baseline_contact_phone_24);
                  list.add(info);
               }

               cursorInfo.close();
            }
         }
         cursor.close();
      }
      return list;
   }
   
   public static ArrayList<ItemInList> getInstalledApps(Context context, boolean scanSystemApps) {
      PackageManager pm = context.getPackageManager();
      ArrayList<ItemInList> apps = new ArrayList<>();
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
         if (pm.getLaunchIntentForPackage(packages) == null) continue;

         ItemInList item = new ItemInList();
         item.type = isSystem ? ItemInList.Type.SystemApp : ItemInList.Type.App;
         item.name = appName;
         item.icon = icon;
         item.packageName = packages;
         apps.add(item);
      }

      return apps;
   }

   private static boolean isSystemPackage(PackageInfo pkgInfo) {
      return (pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
   }

   public static ItemInList findByPackage(ArrayList<ItemInList> fullList, String pack){
      for (ItemInList item : fullList) {
         if (item.packageName.length() == 0) continue;

         if (Objects.equals(item.packageName, pack))
            return item;
      }
      return null;
   }

   public static ItemInList findByUri(ArrayList<ItemInList> fullList, String strUri){
      for (ItemInList item : fullList) {
         if (item.uri == null) continue;
         if (Objects.equals(item.uri.toString(), strUri))
            return item;
      }
      return null;
   }

   public static ItemInList findByName(ArrayList<ItemInList> fullList, String name){
      for (ItemInList item : fullList) {
         if (Objects.equals(item.name, name))
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
         i.addFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP);
         i.setData(Uri.parse(url));
         context.startActivity(i);
      } catch (UnsupportedEncodingException ignored) {}
   }

   public static void openContact(Context context, ItemInList item){
      Intent intent = new Intent(Intent.ACTION_VIEW, item.uri);
      intent.addFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP);
      if (intent.resolveActivity(context.getPackageManager()) != null) {
         context.startActivity(intent);
      }
   }

   public static void openApp(Context context, ItemInList item){
      Intent intent = context.getPackageManager().getLaunchIntentForPackage(item.packageName);
      if(intent != null){
         intent.addFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP);
         context.startActivity(intent);
      }
      else
         Toasty.error(context, item.name + " can't be open (no have launch intent)",
                 Toasty.LENGTH_SHORT).show();
   }
}
