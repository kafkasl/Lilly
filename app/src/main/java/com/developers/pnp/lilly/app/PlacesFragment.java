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
 package com.developers.pnp.lilly.app;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.developers.pnp.lilly.app.data.PlacesContract;
import com.google.android.gms.maps.model.LatLng;

public class PlacesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // These indices are tied to PLACES_COLUMNS.  If PLACES_COLUMNS changes, these
    // must change.
    static final int COL_PLACE_ID = 0;
    static final int COL_PLACE_REF_ID = 1;
    static final int COL_PLACE_NAME = 2;
    static final int COL_PLACE_LAT = 3;
    static final int COL_PLACE_LNG = 4;
    static final int COL_PLACE_RATING = 5;
    static final int COL_PLACE_TYPE = 6;
    private static final String SELECTED_KEY = "selected_position";
    private static final int FORECAST_LOADER = 0;
    private static final String[] PLACES_COLUMNS = {

        PlacesContract.PlaceEntry.TABLE_NAME + "." + PlacesContract.PlaceEntry._ID,
        PlacesContract.PlaceEntry.COLUMN_GOOGLE_REF,
        PlacesContract.PlaceEntry.COLUMN_NAME,
        PlacesContract.PlaceEntry.COLUMN_LAT,
        PlacesContract.PlaceEntry.COLUMN_LNG,
        PlacesContract.PlaceEntry.COLUMN_RATING,
        PlacesContract.PlaceEntry.COLUMN_TYPE
    };
    private PlacesAdapter mPlacesAdapter;
    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;

    public PlacesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.placesfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updatePlaces();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // The PlacesAdapter will take data from a source and
        // use it to populate the ListView it's attached to.
        mPlacesAdapter = new PlacesAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        mListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        mListView.setAdapter(mPlacesAdapter);
        // We'll call our MainActivity
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.

                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    ((Callback) getActivity())
                            .onItemSelected(PlacesContract.PlaceEntry.buildPlaceFromGoogleID(
                                    cursor.getString(COL_PLACE_REF_ID)
                            ));
                }

                mPosition = position;
            }
        });


        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    // since we read the location when we create the loader, all we need to do is restart things
    void onLocationChanged( ) {
        updatePlaces();
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    private void updatePlaces() {
        LatLng latlngLocation = ((myLatLngProvider)getActivity()).getCurrentLocation();

        if (latlngLocation != null) {
            FetchPlacesTask weatherTask = new FetchPlacesTask(getActivity());
            weatherTask.execute(latlngLocation.latitude + "," + latlngLocation.longitude);
        } else if (!((myLatLngProvider)getActivity()).isLocationEnabled(getActivity())){
            CharSequence text = "Please enable location services for updated content";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(getActivity(), text, duration);
            toast.show();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        LatLng latlngLocation = ((myLatLngProvider)getActivity()).getCurrentLocation();
        Uri placesForLocationUri;
       if (latlngLocation != null) {
          placesForLocationUri = PlacesContract.PlaceEntry.buildPlacesFromLocation(
                   latlngLocation);
       } else {
           placesForLocationUri = PlacesContract.PlaceEntry.buildPlaces();
       }

        return new CursorLoader(getActivity(),
                placesForLocationUri,
                PLACES_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mPlacesAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mListView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mPlacesAdapter.swapCursor(null);
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        void onItemSelected(Uri dateUri);
    }

    public interface myLatLngProvider {
        LatLng getCurrentLocation();

        boolean isLocationEnabled(Context context);
    }
}