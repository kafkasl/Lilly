package com.developers.pnp.lilly.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * {@link PlacesAdapter} exposes a list of weather forecasts
 * from a {@link Cursor} to a {@link android.widget.ListView}.
 */
public class PlacesAdapter extends CursorAdapter {

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView nameView;
        public final TextView typeView;
        public final TextView ratingView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            nameView = (TextView) view.findViewById(R.id.list_item_name_textview);
            typeView = (TextView) view.findViewById(R.id.list_item_type_textview);
            ratingView = (TextView) view.findViewById(R.id.list_item_rating_textview);
        }
    }

    public PlacesAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Choose the layout type
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        layoutId = R.layout.list_item_places;

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int imageType = Utility.getImageFromType(cursor.getString(PlacesFragment.COL_PLACE_TYPE));

        viewHolder.iconView.setImageResource(imageType);

        String placeName = cursor.getString(PlacesFragment.COL_PLACE_NAME);
        viewHolder.nameView.setText(placeName);

        // Read weather forecast from cursor
        String type = Utility.getFormattedType(cursor.getString(PlacesFragment.COL_PLACE_TYPE));

        // Find TextView and set weather forecast on it
        viewHolder.typeView.setText(type);

        viewHolder.iconView.setContentDescription(placeName);

        // Read high temperature from cursor
        double rating = cursor.getDouble(PlacesFragment.COL_PLACE_RATING);
        viewHolder.ratingView.setText(Double.toString(rating));

    }

    @Override
    public int getViewTypeCount() {
        // All places of the list have the same style
        return 1;
    }
}