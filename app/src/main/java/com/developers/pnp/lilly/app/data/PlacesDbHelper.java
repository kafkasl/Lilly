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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import com.developers.pnp.lilly.app.data.PlacesContract.PlaceEntry;
import com.developers.pnp.lilly.app.data.PlacesContract.EvaluationEntry;

/**
 * Manages a local database for weather data.
 */
public class PlacesDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 3;

    static final String DATABASE_NAME = "lilly.db";

    public PlacesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_PLACES_TABLE = "CREATE TABLE " + PlaceEntry.TABLE_NAME + " (" +
                // Why AutoIncrement here, and not above?
                // Unique keys will be auto-generated in either case.  But for weather
                // forecasting, it's reasonable to assume the user will want information
                // for a certain date and all dates *following*, so the forecast data
                // should be sorted accordingly.
                PlaceEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // the ID of the location entry associated with this weather data
                PlaceEntry.COLUMN_GOOGLE_REF + " TEXT NOT NULL, " +
                PlaceEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                PlaceEntry.COLUMN_LAT + " REAL NOT NULL, " +
                PlaceEntry.COLUMN_LNG + " REAL NOT NULL, " +

                PlaceEntry.COLUMN_RATING + " REAL, " +
                PlaceEntry.COLUMN_TYPE + " TEXT)";

//                // To assure the application have just one weather entry per day
//                // per location, it's created a UNIQUE constraint with REPLACE strategy
//                " UNIQUE (" + PlaceEntry.COLUMN_DATE + ", " +
//                PlaceEntry.COLUMN_LOC_KEY + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_PLACES_TABLE);


        final String SQL_CREATE_EVALUATION_TABLE = "CREATE TABLE " + EvaluationEntry.TABLE_NAME + " (" +
                // Why AutoIncrement here, and not above?
                // Unique keys will be auto-generated in either case.  But for weather
                // forecasting, it's reasonable to assume the user will want information
                // for a certain date and all dates *following*, so the forecast data
                // should be sorted accordingly.
                EvaluationEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // the ID of the Place entry associated with this evaluation data
                EvaluationEntry.COLUMN_PLACE_KEY + " INTEGER NOT NULL, " +
                EvaluationEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +

            // Set up the place column as a foreign key to places table.
                " FOREIGN KEY (" + EvaluationEntry.COLUMN_PLACE_KEY + ") REFERENCES " +
                PlaceEntry.TABLE_NAME + " (" + PlaceEntry._ID + "))";


        sqLiteDatabase.execSQL(SQL_CREATE_EVALUATION_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PlaceEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
