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

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.developers.pnp.lilly.app.Utility;

public class PlacesProvider extends ContentProvider {
    static final int PLACES = 111;
    static final int PLACE_FROM_GOOGLE_ID = 113;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    //place.google_ref = ?
    private static final String sPlaceByRefSelection =
            PlacesContract.PlaceEntry.TABLE_NAME +
                    "." + PlacesContract.PlaceEntry.COLUMN_GOOGLE_REF + " = ? ";

    private static final String sPreferredTypesSelection =
            PlacesContract.PlaceEntry.TABLE_NAME +
                    "." + PlacesContract.PlaceEntry.COLUMN_TYPE + " IN (?,?,?,?,?,?,?)";
    private PlacesDbHelper mOpenHelper;

    static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        final String authority = PlacesContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, PlacesContract.PATH_PLACES, PLACES);
        matcher.addURI(authority, PlacesContract.PATH_PLACES + "/*", PLACE_FROM_GOOGLE_ID);


        Uri added = Uri.parse("content://com.developers.pnp.lilly.app").buildUpon().appendPath("places").build();


        return matcher;
    }

    private Cursor getPlaceByGoogleRef(Uri uri, String[] projection, String selection, String[] selectionArgs,
                                       String sortOrder) {
        selection = sPlaceByRefSelection;
        selectionArgs = new String[] {uri.getLastPathSegment()};

        return mOpenHelper.getReadableDatabase().query(
                PlacesContract.PlaceEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    private Cursor getPlacesByPreferredTypes(Uri uri, String[] projection, String selection, String[] selectionArgs,
                                       String sortOrder) {
        selection = sPreferredTypesSelection;
        selectionArgs = Utility.getPreferencesTypes(getContext());

        return mOpenHelper.getReadableDatabase().query(
                PlacesContract.PlaceEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new PlacesDbHelper(getContext());
        return true;
    }


    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PLACES:
                return PlacesContract.PlaceEntry.CONTENT_TYPE;
            case PLACE_FROM_GOOGLE_ID:
                return PlacesContract.PlaceEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        Cursor retCursor;

        switch (sUriMatcher.match(uri)) {

            case PLACES:
                retCursor = getPlacesByPreferredTypes(uri, projection, selection, selectionArgs, sortOrder);
                break;
            case PLACE_FROM_GOOGLE_ID:
                retCursor = getPlaceByGoogleRef(uri, projection, selection, selectionArgs, sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);


        Uri returnUri;

        switch (match) {
            case PLACES:
                long _id = db.insert(PlacesContract.PlaceEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = PlacesContract.PlaceEntry.buildPlaceUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri + "\nAnd match " + match + "\nPLACES_CODE: " + PLACES);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Student: Start by getting a writable database
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        Uri returnUri;
        int deletedRows;

        // if a null is passed we understand that all rows should e deleted
        if (selection == null) selection = "1";
        switch(match){
            case PLACES:
                deletedRows = db.delete(PlacesContract.PlaceEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (deletedRows > 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return deletedRows;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int updatedRows;

        switch (match) {
            case PLACES:
                updatedRows = db.update(PlacesContract.PlaceEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (updatedRows > 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return updatedRows;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PLACES:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(PlacesContract.PlaceEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }


    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}