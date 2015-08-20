
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

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.developers.pnp.lilly.app.data.PlacesContract.PlaceEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

public class FetchPlacesTask extends AsyncTask<String, Void, Void> {

    // Request code to use when launching the resolution activity

    private final String LOG_TAG = FetchPlacesTask.class.getSimpleName();
    private final Context mContext;
    private final String NEXT_TOKEN_EMPTY = "empty";
    private final String OVER_QUERY_LIMIT = "OVER_QUERY_LIMIT";


    private Vector<ContentValues> mCVVector =  new Vector<ContentValues>();



    private boolean DEBUG = true;

    public FetchPlacesTask(Context context) {
        mContext = context;
    }

    private String getPlacesDataFromJson(String placesJsonStr, String calledToken)
            throws JSONException {



        // this boolean checks wether there are more pages to call
        String pageToken;

        // These are the names of the JSON objects that need to be extracted.

        // All results info
        final String PLACES_LIST = "results";
        final String PLACES_NEXT_TOKEN = "next_page_token";
        final String STATUS = "status";

        final String INVALID_REQUEST = "INVALID_REQUEST";

        // Location information
        final String PLACES_LOCATION = "geometry";
        final String PLACES_GEO_INFO = "location";
        final String PLACES_LAT = "lat";
        final String PLACES_LONG = "lng";
        final String PLACES_NAME = "name";
        final String PLACES_ID = "place_id";
        final String PLACES_RATING = "rating";

        // List of the types
        final String PLACES_TYPES = "types";

        JSONObject placesJson = new JSONObject(placesJsonStr);

        try {
            pageToken = placesJson.getString(PLACES_NEXT_TOKEN);
        } catch (JSONException e) {
            pageToken = NEXT_TOKEN_EMPTY;
        }

        String status = "unknown";
        try {
            status = placesJson.getString(STATUS);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Exception " + e.getMessage());
        }

        if (status.equals(INVALID_REQUEST)){
            pageToken = calledToken;
        }
        else if (status.equals(OVER_QUERY_LIMIT)){
            Log.e(LOG_TAG, "Reached maximum api usage.");
        }

        JSONArray placesArray = placesJson.getJSONArray(PLACES_LIST);

        // Insert the new places information into the database
        Vector<ContentValues> cVVector = new Vector<ContentValues>(placesArray.length());



        for(int i = 0; i < placesArray.length(); i++) {
            // These are the values that will be collected.
            boolean placeToInsert = false;
            String id;
            String name;
            double lat, lng;
            double rating = -1;
            String type = "";


            JSONObject place = placesArray.getJSONObject(i);


            JSONArray typesArray = place.getJSONArray(PLACES_TYPES);

            id = place.getString(PLACES_ID);
            name = place.getString(PLACES_NAME);

            JSONObject placeLoc = (place.getJSONObject(PLACES_LOCATION)).getJSONObject(PLACES_GEO_INFO);
            lat = placeLoc.getDouble(PLACES_LAT);
            lng = placeLoc.getDouble(PLACES_LONG);

            try {
                rating = place.getDouble(PLACES_RATING);
            } catch (JSONException e) {
            }

            try {
                String[] types = new String[typesArray.length()];
                for (int j = 0; j < typesArray.length(); ++j) {
                    String typeVal = typesArray.getString(j);

                    types[j] = typeVal;
                }

                type = Utility.getBestFittingType(types);

            } catch (JSONException e) {
            }

            // If type is null it means the place has no category and therefore shouldn't be displayed
            if (!type.isEmpty()) {

                ContentValues placeValues = new ContentValues();

                placeValues.put(PlaceEntry.COLUMN_GOOGLE_REF, id);
                placeValues.put(PlaceEntry.COLUMN_NAME, name);
                placeValues.put(PlaceEntry.COLUMN_LAT, lat);
                placeValues.put(PlaceEntry.COLUMN_LNG, lng);
                if (rating > 0) placeValues.put(PlaceEntry.COLUMN_RATING, rating);
                placeValues.put(PlaceEntry.COLUMN_TYPE, type);

                cVVector.add(placeValues);
            }
        }
        if (cVVector.size() > 0) {
            mCVVector.addAll(cVVector);
        }

        return pageToken;
    }

    protected Void doInBackground(String... params) {

        if (params.length == 0) {
            return null;
        }

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String placesJsonStr;
        String nextToken = NEXT_TOKEN_EMPTY;

        int radius = 200;
        boolean thereIsData = true;


        while (thereIsData) {
            try {

                final String FORECAST_BASE_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
                final String KEY_PARAM = "key";
                final String QUERY_PARAM = "location";
                final String RADIUS_PARAM = "radius";
                final String NEXT_TOKEN_PARAM = "pagetoken";

                Uri builtUri;

                if (!nextToken.equals(NEXT_TOKEN_EMPTY)){
                    builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                            .appendQueryParameter(KEY_PARAM, "AIzaSyDlzVIMSaE_Xv1pU_RvncJN-yy9oAia7TE")
                            .appendQueryParameter(QUERY_PARAM, params[0])
                            .appendQueryParameter(RADIUS_PARAM, Integer.toString(radius))
                            .appendQueryParameter(NEXT_TOKEN_PARAM, nextToken).build();
                } else {
                    builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                            .appendQueryParameter(KEY_PARAM, "AIzaSyDlzVIMSaE_Xv1pU_RvncJN-yy9oAia7TE")
                            .appendQueryParameter(QUERY_PARAM, params[0])
                            .appendQueryParameter(RADIUS_PARAM, Integer.toString(radius)).build();
                }


                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {

                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }

                placesJsonStr = buffer.toString();


                if (thereIsData)
                    nextToken = getPlacesDataFromJson(placesJsonStr, nextToken);



                thereIsData = !nextToken.equals(NEXT_TOKEN_EMPTY);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error. ", e);

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        int inserted = 0;
        int deleted = 0;

        // add to database
        if ( mCVVector.size() > 0 ) {
            ContentValues[] cvArray = new ContentValues[mCVVector.size()];
            mCVVector.toArray(cvArray);
            deleted = mContext.getContentResolver().delete(PlaceEntry.CONTENT_URI, null, null);
            inserted = mContext.getContentResolver().bulkInsert(PlaceEntry.CONTENT_URI, cvArray);
        }


        return null;
    }
}