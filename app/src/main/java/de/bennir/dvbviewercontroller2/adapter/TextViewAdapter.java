package de.bennir.dvbviewercontroller2.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class TextViewAdapter extends ArrayAdapter<String> {
    private Context mContext;
    private int mResource;
    private List<String> mItems;

    static class ViewHolder {
        TextView text;
    }

    public TextViewAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);

        mContext = context;
        mResource = resource;
        mItems = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder holder; // to reference the child views for later actions

        if (v == null) {
            v = LayoutInflater.from(parent.getContext()).inflate(mResource, parent, false);
            // cache view fields into the holder

            holder = new ViewHolder();
            holder.text = (TextView) v;
            // associate the holder with the view for later lookup
            v.setTag(holder);
        } else {
            // view already exists, get the holder instance from the view
            holder = (ViewHolder) v.getTag();
        }

        Typeface tf = Typeface.createFromAsset(mContext.getAssets(),"fonts/Roboto-Medium.ttf");
        holder.text.setTypeface(tf);
        holder.text.setText(mItems.get(position));

        return v;
    }
}