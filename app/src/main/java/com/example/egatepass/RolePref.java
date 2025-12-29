package com.example.egatepass;

import android.content.Context;
import android.content.SharedPreferences;

public class RolePref {
    private static final String PREFS = "egatepass_prefs";
    private static final String KEY_ROLE = "role";
    private static final String KEY_EMAIL = "email";

    public static void setRole(Context ctx, String role) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_ROLE, role).apply();
    }

    public static String getRole(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return sp.getString(KEY_ROLE, null);
    }

    public static boolean isAdmin(Context ctx) {
        String role = getRole(ctx);
        return role != null && role.equals("admin");
    }

    public static void setEmail(Context ctx, String email) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_EMAIL, email).apply();
    }

    public static String getEmail(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return sp.getString(KEY_EMAIL, null);
    }

    public static void clear(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().clear().apply();
    }
}
