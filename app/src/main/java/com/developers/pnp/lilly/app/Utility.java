package com.developers.pnp.lilly.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.developers.pnp.lilly.app.data.PlacesContract;

import java.util.Arrays;

public class Utility {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final int NUMBER_OF_TYPES = 7;
    private static final String[] FOOD = {"food", "meal_delivery", "meal_takeaway", "restaurant"};
    private static final String[] CINEMA= {"movie_theater"};
    private static final String[] ATM = {"atm", "bank"};
    private static final String[] DISCO = {"night_club"};
    private static final String[] DRINKS = {"bar", "cafe", "liquor_store"};
    private static final String[] SHOPPING = {"book_store", "clothing_store", "florist", "furniture_store","jewelry_store", "shoe_store", "shopping_mall"};
    private static final String[] LEISURE = {"casino", "library", "museum", "park", "spa", "stadium", "zoo", "bowling_alley"};



    // RETURN CORRECT IMAGE TYPE
    public static int getImageFromType(String type) {
        if (type.equals(PlacesContract.CINEMA))
            return R.drawable.cinema;
        if (type.equals(PlacesContract.ATM))
            return R.drawable.atm;
        if (type.equals(PlacesContract.DISCO))
            return R.drawable.disco;
        if (type.equals(PlacesContract.DRINKS))
            return R.drawable.drinks;
        if (type.equals(PlacesContract.FOOD))
            return R.drawable.food;
        if (type.equals(PlacesContract.SHOPPING))
            return R.drawable.shopping;
        if (type.equals(PlacesContract.LEISURE))
            return R.drawable.leisure;
        return -1;
    }

    public static String getBestFittingType(String[] types){
        String type = "";
        if (types.length > 0){
            int i = 0;
            while (type.isEmpty() && i < types.length){
                if (Arrays.asList(FOOD).contains(types[i]))
                    type = PlacesContract.FOOD;
                else if (Arrays.asList(CINEMA).contains(types[i]))
                    type = PlacesContract.CINEMA;
                else if (Arrays.asList(ATM).contains(types[i]))
                    type = PlacesContract.ATM;
                else if (Arrays.asList(DISCO).contains(types[i]))
                    type = PlacesContract.DISCO;
                else if (Arrays.asList(DRINKS).contains(types[i]))
                    type = PlacesContract.DRINKS;
                else if (Arrays.asList(SHOPPING).contains(types[i]))
                    type = PlacesContract.SHOPPING;
                else if (Arrays.asList(LEISURE).contains(types[i]))
                    type = PlacesContract.LEISURE;

                ++i;
            }
        }
        return type;
    }

    public static String getFormattedType(String type) {
        type = type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase();
        return type.replace('_', ' ');
    }

//    public static LatLng getLastLocation(Context context){
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//        String defValue = "0.0";
//        String lat = prefs.getString(context.getString(R.string.last_lat_key), defValue);
//        String lng = prefs.getString(context.getString(R.string.last_lng_key), defValue);
//
//        return new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
//    }
//
//    public static void setLastLocation(Context context, LatLng lastLocation){
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//
//        String lat = Double.toString(lastLocation.latitude);
//        String lng = Double.toString(lastLocation.longitude);
//
//        prefs.edit().putString(context.getString(R.string.last_lat_key), lat).apply();
//        prefs.edit().putString(context.getString(R.string.last_lng_key), lng).apply();
//
//    }

    public static String[] getPreferencesTypes(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String[] type_keys = new String[NUMBER_OF_TYPES];
        type_keys[0] = context.getString(R.string.shopping_key);
        type_keys[1] = context.getString(R.string.disco_key);
        type_keys[2] = context.getString(R.string.cinema_key);
        type_keys[3] = context.getString(R.string.food_key);
        type_keys[4] = context.getString(R.string.drink_key);
        type_keys[5] = context.getString(R.string.atm_key);
        type_keys[6] = context.getString(R.string.leisure_key);

        String[] types = new String[NUMBER_OF_TYPES];
        types[0] = PlacesContract.SHOPPING;
        types[1] = PlacesContract.DISCO;
        types[2] = PlacesContract.CINEMA;
        types[3] = PlacesContract.FOOD;
        types[4] = PlacesContract.DRINKS;
        types[5] = PlacesContract.ATM;
        types[6] = PlacesContract.LEISURE;

        String[] selectedTypes = new String[NUMBER_OF_TYPES];

        for (int i = 0; i < type_keys.length; ++i){
            if (prefs.getBoolean(type_keys[i], true)){
                selectedTypes[i] = types[i];
            }
            else {
                selectedTypes[i] = "all";
            }
        }


        return selectedTypes;
    }


//    public static String getPreferredLocation(Context context) {
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//        return prefs.getString(context.getString(R.string.pref_location_key),
//                context.getString(R.string.pref_location_default));
//    }


}