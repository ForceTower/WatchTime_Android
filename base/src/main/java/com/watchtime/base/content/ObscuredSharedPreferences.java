package com.watchtime.base.content;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Base64;

import java.util.Map;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/**
 * Created by João Paulo on 23/01/2017.
 */

public class ObscuredSharedPreferences implements SharedPreferences {
    private static final String UTF8 = "utf-8";
    private static final char[] PASSWORD = new char[]{0x8F, 0xF8, 0xDD, 0xAF, 0xCC, 0x69, 0x96, 0xFF};

    private SharedPreferences delegated;
    private Context context;

    public ObscuredSharedPreferences(Context context, SharedPreferences delegate) {
        this.delegated = delegate;
        this.context = context;
    }

    @Override
    public Map<String, ?> getAll() {
        throw new UnsupportedOperationException("Method not Implemented: ObscuredSharedPreferences::getAll()");
    }

    @Nullable
    @Override
    public String getString(String key, String defaultValue) {
        final String v = delegated.getString(key, null);
        return v != null ? decrypt(v) : defaultValue;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public Set<String> getStringSet(String s, Set<String> strings) {
        return delegated.getStringSet(s, strings);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        final String v = delegated.getString(key, null);
        return v != null ? Integer.parseInt(decrypt(v)) : defaultValue;
    }

    @Override
    public long getLong(String key, long defaultValue) {
        final String v = delegated.getString(key, null);
        return v != null ? Long.parseLong(decrypt(v)) : defaultValue;
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        final String v = delegated.getString(key, null);
        return v != null ? Float.parseFloat(decrypt(v)) : defaultValue;
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        final String v = delegated.getString(key, null);
        return v != null ? Boolean.parseBoolean(decrypt(v)) : defaultValue;
    }

    @Override
    public boolean contains(String s) {
        return delegated.contains(s);
    }

    @Override
    public Editor edit() {
        return new EditorImpl();
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        delegated.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        delegated.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    private class EditorImpl implements SharedPreferences.Editor {
        private SharedPreferences.Editor delegate;

        EditorImpl() {
            delegate = ObscuredSharedPreferences.this.delegated.edit();
        }

        @Override
        public Editor putString(String key, String value) {
            delegate.putString(key, encrypt(value));
            return this;
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public Editor putStringSet(String key, Set<String> set) {
            delegate.putStringSet(key, set);
            return this;
        }

        @Override
        public Editor putInt(String key, int value) {
            delegate.putString(key, encrypt(Integer.toString(value)));
            return this;
        }

        @Override
        public Editor putLong(String key, long value) {
            delegate.putString(key, encrypt(Long.toString(value)));
            return this;
        }

        @Override
        public Editor putFloat(String key, float value) {
            delegate.putString(key, encrypt(Float.toString(value)));
            return this;
        }

        @Override
        public Editor putBoolean(String key, boolean value) {
            delegate.putString(key, encrypt(Boolean.toString(value)));
            return this;
        }

        @Override
        public Editor remove(String key) {
            delegate.remove(key);
            return this;
        }

        @Override
        public Editor clear() {
            delegate.clear();
            return this;
        }

        @Override
        public boolean commit() {
            return delegate.commit();
        }

        @Override
        public void apply() {
            delegate.apply();
        }
    }

    private String encrypt(String value) {
        try {
            final byte[] bytes = value != null ? value.getBytes(UTF8) : new byte[0];
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            SecretKey key = keyFactory.generateSecret(new PBEKeySpec(PASSWORD));
            Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
            pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(Settings.Secure.getString(context.getContentResolver(), Settings.System.ANDROID_ID).getBytes(UTF8), 20));
            return new String(Base64.encode(pbeCipher.doFinal(bytes), Base64.NO_WRAP), UTF8);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String decrypt(String value) {
        try {
            final byte[] bytes = value != null ? Base64.decode(value, Base64.DEFAULT) : new byte[0];
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            SecretKey key = keyFactory.generateSecret(new PBEKeySpec(PASSWORD));
            Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
            pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(Settings.Secure.getString(context.getContentResolver(), Settings.System.ANDROID_ID).getBytes(UTF8), 20));
            return new String(pbeCipher.doFinal(bytes), UTF8);
        } catch (Exception e) {
            delegated.edit().clear().apply();
            return null;
        }
    }
}

