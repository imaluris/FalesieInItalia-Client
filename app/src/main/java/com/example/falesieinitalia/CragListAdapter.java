package com.example.falesieinitalia;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Locale;

public class CragListAdapter extends ArrayAdapter<Crag> {

    private static final String TAG = "CragListAdapter";

    private Context mContext;
    int mResource;
    private ArrayList<Crag> myList;  // for loading main list
    private ArrayList<Crag> arraylist=null;  // for loading  filter data

    public CragListAdapter(Context context, int resource, ArrayList<Crag> objects) {
        super(context,resource,objects);
        mContext=context;
        mResource=resource;
        myList=objects;
        this.arraylist = new ArrayList<Crag>();
        this.arraylist.addAll(myList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String name = getItem(position).getName();
        String city = getItem(position).getCity();
        String region = getItem(position).getRegion();
        String description = getItem(position).getDescription();
        String image = getItem(position).getImage();
        String type = getItem(position).getType();

        //Crag crag = new Crag(name, city, region, description, image, type);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        TextView cragName = (TextView) convertView.findViewById(R.id.cragName);
        TextView cragCity = (TextView) convertView.findViewById(R.id.cragCity);
        TextView cragRegion = (TextView) convertView.findViewById(R.id.cragRegion);

        cragName.setText(name);
        cragCity.setText(city);
        cragRegion.setText(region);

        return convertView;

    }

    public void filter(String charText){
        charText = charText.toLowerCase(Locale.getDefault());
        myList.clear();
        if (charText.length() == 0) {
            myList.addAll(arraylist);
        }
        else
        {
            for (Crag wp : arraylist) {
                if (wp.getName().toLowerCase(Locale.getDefault()).contains(charText)) {
                    myList.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }
}