package com.deploygate.sdk;

import android.os.Build;
import android.util.Pair;

import com.deploygate.sdk.internal.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class SdkDeviceStatesCollector {

    private final JSONObject states = new JSONObject();

    public String getJSONString() {
        return states.toString();
    }

    public void collectLocale() {
        Locale defaultLocale = Locale.getDefault();
        ArrayList<Pair<String, Object>> data = new ArrayList<>();
        data.add(new Pair<String, Object>("toString", defaultLocale.toString()));
        data.add(new Pair<String, Object>("getLanguage", defaultLocale.getLanguage()));
        data.add(new Pair<String, Object>("getCountry", defaultLocale.getCountry()));
        data.add(new Pair<String, Object>("getVariant", defaultLocale.getVariant()));
        data.add(new Pair<String, Object>("getDisplayCountry", defaultLocale.getDisplayCountry()));
        data.add(new Pair<String, Object>("getDisplayLanguage", defaultLocale.getDisplayLanguage()));
        data.add(new Pair<String, Object>("getDisplayName", defaultLocale.getDisplayName()));
        data.add(new Pair<String, Object>("getDisplayVariant", defaultLocale.getDisplayVariant()));
        data.add(new Pair<String, Object>("getISO3Country", defaultLocale.getISO3Country()));
        data.add(new Pair<String, Object>("getISO3Language", defaultLocale.getISO3Language()));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            data.add(new Pair<String, Object>("getDisplayScript", defaultLocale.getDisplayScript()));
            data.add(new Pair<String, Object>("toLanguageTag", defaultLocale.toLanguageTag()));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            data.add(new Pair<String, Object>("getScript", defaultLocale.getScript()));
        }

        putAll(Locale.class.getName(), data);
    }

    private void putAll(String fqcn, List<Pair<String, Object>> data) {
        for (Pair<String, Object> pair : data) {
            String key = String.format("%s$%s", fqcn, pair.first);
            try {
                states.put(key, pair.second);
            } catch (JSONException e) {
                Logger.w(e, "Failed to put info: key=%s, value=%s", key, pair.second);
            }
        }

    }
}
