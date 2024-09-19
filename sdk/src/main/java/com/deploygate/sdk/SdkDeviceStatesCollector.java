package com.deploygate.sdk;

import android.os.Build;

import com.deploygate.sdk.internal.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

final class SdkDeviceStatesCollector {

    private final JSONObject states = new JSONObject();

    public String getJSONString() {
        return states.toString();
    }

    public void collectLocale() {
        Locale defaultLocale = Locale.getDefault();
        String localeClassName = Locale.class.getName();
        putState(localeClassName, "toString", defaultLocale.toString());
        putState(localeClassName, "getLanguage", defaultLocale.getLanguage());
        putState(localeClassName, "getCountry", defaultLocale.getCountry());
        putState(localeClassName, "getVariant", defaultLocale.getVariant());
        putState(localeClassName, "getDisplayCountry", defaultLocale.getDisplayCountry());
        putState(localeClassName, "getDisplayLanguage", defaultLocale.getDisplayLanguage());
        putState(localeClassName, "getDisplayName", defaultLocale.getDisplayName());
        putState(localeClassName, "getDisplayVariant", defaultLocale.getDisplayVariant());
        putState(localeClassName, "getISO3Country", defaultLocale.getISO3Country());
        putState(localeClassName, "getISO3Language", defaultLocale.getISO3Language());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            putState(localeClassName, "getDisplayScript", defaultLocale.getDisplayScript());
            putState(localeClassName, "toLanguageTag", defaultLocale.toLanguageTag());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            putState(localeClassName, "getScript", defaultLocale.getScript());
        }
    }

    private void putState(String fqcn, String paramName, Object data) {
        String key = String.format("%s$%s", fqcn, paramName);
        try {
            states.put(key, data);
        } catch (JSONException e) {
            Logger.w(e, "Failed to put info: key=%s, value=%s", key, data);
        }
    }
}
