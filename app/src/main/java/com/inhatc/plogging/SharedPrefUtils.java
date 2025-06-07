package com.inhatc.plogging;

import android.content.Context;

public class SharedPrefUtils {
    private static final String PREF_NAME = "profile";

    public static String getName(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString("name", "이름");
    }
    public static float getHeight(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getFloat("height", 0f);
    }
    public static float getWeight(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getFloat("weight", 0f);
    }
    public static void setName(Context ctx, String name) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().putString("name", name).apply();
    }
    public static void setHeight(Context ctx, float height) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().putFloat("height", height).apply();
    }
    public static void setWeight(Context ctx, float weight) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().putFloat("weight", weight).apply();
    }
}
