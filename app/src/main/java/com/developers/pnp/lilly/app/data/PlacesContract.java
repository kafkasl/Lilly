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
package com.developers.pnp.lilly.app.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import com.google.android.gms.maps.model.LatLng;

/**
 * Defines table and column names for the weather database.
 */
public class PlacesContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the

    public static final String FOOD = "food";
    public static final String CINEMA= "cinema";
    public static final String ATM = "atm";
    public static final String DISCO = "night club";
    public static final String DRINKS = "drinks";
    public static final String SHOPPING = "shopping";
    public static final String LEISURE = "leisure";
    // device.
    public static final String CONTENT_AUTHORITY = "com.developers.pnp.lilly.app";

    public static final String EVALUATED_SIGNAL = "evaluated";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PLACES = "places";
    public static final String PATH_PLACES_EVALUATED = PATH_PLACES + "/" + EVALUATED_SIGNAL;

    public static final String PATH_EVALUATIONS = "evaluations";


    public static final class PlaceEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLACES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PLACES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PLACES ;

        // Table name
        public static final String TABLE_NAME = "places";

        public static final String COLUMN_GOOGLE_REF = "place_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_LAT = "lat";
        public static final String COLUMN_LNG = "lng";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_TYPE = "type";


        public static Uri buildPlaces() {
            return CONTENT_URI;
        }

        public static Uri buildEvaluatedUri(){
            return BASE_CONTENT_URI.buildUpon().appendPath(EVALUATED_SIGNAL).build();
        }

        public static Uri buildPlaceUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildPlaceFromGoogleID(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }

        public static Uri buildPlacesFromLocation(LatLng latlng) {
            return CONTENT_URI.buildUpon().appendPath(latlng.toString()).build();
        }
    }

    public static final class EvaluationEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_EVALUATIONS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_EVALUATIONS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_EVALUATIONS;

        // Table name
        public static final String TABLE_NAME = "evaluations";

        // Column with the foreign key into the places table.
        public static final String COLUMN_PLACE_KEY = "places_id";

        public static final String COLUMN_DESCRIPTION = "description";


        public static Uri buildEvaluation() {
            return CONTENT_URI;
        }

        public static Uri buildEvaluationUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

        /*
            Student: Fill in this buildWeatherLocation function

        public static Uri buildWeatherLocation(String locationSetting) {

            return CONTENT_URI.buildUpon().appendPath(locationSetting).build();
        }

//        public static Uri buildWeatherLocationWithStartDate(
//                String locationSetting, long startDate) {
//                String locationSetting, long startDate) {
//            long normalizedDate = normalizeDate(startDate);
//            return CONTENT_URI.buildUpon().appendPath(locationSetting)
//                    .appendQueryParameter(COLUMN_DATE, Long.toString(normalizedDate)).build();
//        }
//
//        public static Uri buildWeatherLocationWithDate(String locationSetting, long date) {
//            return CONTENT_URI.buildUpon().appendPath(locationSetting)
//                    .appendPath(Long.toString(normalizeDate(date))).build();
//        }


        public static String getLocationSettingFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static long getDateFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(2));
        }

        public static long getStartDateFromUri(Uri uri) {
            String dateString = uri.getQueryParameter(COLUMN_DATE);
            if (null != dateString && dateString.length() > 0)
                return Long.parseLong(dateString);
            else
                return 0;
        }
    }
    */
}