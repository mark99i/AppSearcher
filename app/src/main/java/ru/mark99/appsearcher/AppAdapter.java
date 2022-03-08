package ru.mark99.appsearcher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

class AppAdapter extends BaseAdapter {
   public LayoutInflater layoutInflater;
   public ArrayList<AppItem> apps;

   AppAdapter(Context context, ArrayList<AppItem> apps){
      layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      this.apps = apps;
   }

   @Override
   public int getCount() {
      return apps.size();
   }

   @Override
   public Object getItem(int position) {
      return position;
   }

   @Override
   public long getItemId(int position) {
      return position;
   }

   @Override
   public View getView(int position, View convertView, ViewGroup parent) {
      ViewHolder listViewHolder;
      if(convertView == null){
         listViewHolder = new ViewHolder();
         convertView = layoutInflater.inflate(R.layout.app_item, parent, false);

         listViewHolder.textInListView = convertView.findViewById(R.id.list_app_name);
         listViewHolder.imageInListView = convertView.findViewById(R.id.list_app_icon);
         convertView.setTag(listViewHolder);
      }else{
         listViewHolder = (ViewHolder)convertView.getTag();
      }
      listViewHolder.textInListView.setText(apps.get(position).getName());
      listViewHolder.imageInListView.setImageDrawable(apps.get(position).getIcon());

      return convertView;
   }

   static class ViewHolder {
      TextView textInListView;
      ImageView imageInListView;
   }
}
