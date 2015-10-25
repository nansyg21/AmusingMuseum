package com.asus_s550cb.theo.museum;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter
{
    public ArrayList<String> values;            //useful link: http://www.technotalkative.com/android-multi-column-listview/
    Activity activity;

    public CustomAdapter(Activity activity,  ArrayList<String> _values) {
        super();
        this.activity = activity;
        this.values = _values;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return values.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return values.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    private class ViewHolder {
        TextView nameView;
        TextView idView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        LayoutInflater inflater =  activity.getLayoutInflater();

        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.activity_row_of_list_view, null);
            holder = new ViewHolder();
            holder.nameView = (TextView) convertView.findViewById(R.id.museum_name_column);

            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.nameView.setText(position+"   "+values.get(position));
        return convertView;
    }

}