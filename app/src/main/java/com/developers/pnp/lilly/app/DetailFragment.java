package com.developers.pnp.lilly.app;

/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.developers.pnp.lilly.app.data.PlacesContract;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    static final String DETAIL_URI = "URI";
    // These indices are tied to PLACES_COLUMNS.  If PLACES_COLUMNS changes, these
    // must change.
    static final int COL_PLACE_ID = 0;
    static final int COL_PLACE_REF_ID = 1;
    static final int COL_PLACE_NAME = 2;
    static final int COL_PLACE_LAT = 3;
    static final int COL_PLACE_LNG = 4;
    static final int COL_PLACE_RATING = 5;
    static final int COL_PLACE_TYPE = 6;
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final String PLACE_SHARE_HASHTAG = " #LillyApp";
    private static final int DETAIL_LOADER = 0;
    private static final String[] DETAIL_COLUMNS = {

            PlacesContract.PlaceEntry.TABLE_NAME + "." + PlacesContract.PlaceEntry._ID,
            PlacesContract.PlaceEntry.COLUMN_GOOGLE_REF,
            PlacesContract.PlaceEntry.COLUMN_NAME,
            PlacesContract.PlaceEntry.COLUMN_LAT,
            PlacesContract.PlaceEntry.COLUMN_LNG,
            PlacesContract.PlaceEntry.COLUMN_RATING,
            PlacesContract.PlaceEntry.COLUMN_TYPE,

    };
    private ShareActionProvider mShareActionProvider;
    private String mPlace;
    private Uri mUri;
    private ImageView mIconView;
    private TextView mNameView;
    private TextView mTypeView;
    private TextView mRatingView;

    private TextView mLatLngView;
    private RatingBar mRatingBarView;

    private SupportMapFragment mGoogleMapFrag;


    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mIconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        mNameView = (TextView) rootView.findViewById(R.id.detail_name_textview);
        mTypeView = (TextView) rootView.findViewById(R.id.detail_type_textview);
        mRatingView = (TextView) rootView.findViewById(R.id.detail_rating_textview);
        mLatLngView = (TextView) rootView.findViewById(R.id.detail_latlng_textview);

        mRatingBarView = (RatingBar) rootView.findViewById(R.id.rating_bar_view);

        mGoogleMapFrag = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mGoogleMapFrag != null) mGoogleMapFrag.getView().setVisibility(View.INVISIBLE);


        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (mPlace != null) {
            mShareActionProvider.setShareIntent(createSharePlacetIntent());
        }
    }

    private Intent createSharePlacetIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mPlace + PLACE_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if ( null != mUri ) {
            if (null != args) Log.e(LOG_TAG, "Passed args Bundle: " + args.toString());

            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return  new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data != null && data.moveToFirst()) {
            // Read weather condition ID from cursor
            String type = data.getString(COL_PLACE_TYPE);
            String formattedType = Utility.getFormattedType(type);
            // Use placeholder ImagetoString
            mIconView.setImageResource(Utility.getImageFromType(type));

            String name = data.getString(COL_PLACE_NAME);
            mNameView.setText(name);

            mIconView.setContentDescription(name);

            mTypeView.setText(formattedType);

            Float rating = data.getFloat(COL_PLACE_RATING);
            mRatingView.setText("Rating: " + String.format("%.1f", rating));

            mRatingBarView.setRating(rating);
            mRatingBarView.setVisibility(View.VISIBLE);


            String lat = data.getString(COL_PLACE_LAT);
            String lng = data.getString(COL_PLACE_LNG);

            mLatLngView.setText("Geolocation: " + lat + ", " + lng);


            LatLng latlng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

            mGoogleMapFrag.getView().setVisibility(View.VISIBLE);

            GoogleMap mGoogleMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMap();



            mGoogleMap.setMyLocationEnabled(true);
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 16));


            Marker newmarker = mGoogleMap.addMarker(new MarkerOptions().position(latlng).title(name).icon(BitmapDescriptorFactory.fromResource(R.drawable.map_icon2)));

            UiSettings uiSettings = mGoogleMap.getUiSettings();
            uiSettings.setMapToolbarEnabled(true);
            // We still need this for the share intent
            mPlace = String.format("Check out %s %s place. It has %s stars and I'm near it!", name, Utility.getFormattedType(type), rating);

            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createSharePlacetIntent());
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }
}
