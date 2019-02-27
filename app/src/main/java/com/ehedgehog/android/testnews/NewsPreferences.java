package com.ehedgehog.android.testnews;

import android.content.Context;
import android.preference.PreferenceManager;

public class NewsPreferences {

    private static final String PREF_CATEGORY = "category";
    private static final String PREF_COUNTRY = "country";
    private static final String PREF_QUERY = "query";

    private static final int DEFAULT_COUNTRY_POSITION = 51;

    public static int getStoredCategory(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PREF_CATEGORY, 0);
    }

    public static void setStoredCategory(Context context, int category) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(PREF_CATEGORY, category)
                .apply();
    }

    public static int getStoredCountry(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PREF_COUNTRY, DEFAULT_COUNTRY_POSITION);
    }

    public static void setStoredCountry(Context context, int country) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(PREF_COUNTRY, country)
                .apply();
    }

}
