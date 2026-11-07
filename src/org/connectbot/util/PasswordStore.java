/*
 * ConnectBot: simple, powerful, open-source SSH client for Android
 * Copyright 2026 Kenny Root
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.connectbot.util;

import java.security.SecureRandom;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;

/**
 * Encrypts saved SSH passwords with a per-install key kept in private
 * preferences. This is not hardware-backed encryption: someone with root
 * access to the device can still recover the key and the passwords.
 */
public final class PasswordStore {
	private static final String TAG = "ConnectBot.PasswordStore";
	private static final String PREFS = "password_store";
	private static final String PREF_KEY = "device_key";
	private static final Object lock = new Object();

	private PasswordStore() {
	}

	private static String toHex(byte[] bytes) {
		StringBuilder builder = new StringBuilder(bytes.length * 2);
		for (byte b : bytes) {
			builder.append(Character.forDigit((b >> 4) & 0xf, 16));
			builder.append(Character.forDigit(b & 0xf, 16));
		}
		return builder.toString();
	}

	private static String getSecret(Context context) {
		synchronized (lock) {
			SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
			String key = prefs.getString(PREF_KEY, null);
			if (key == null) {
				byte[] random = new byte[32];
				new SecureRandom().nextBytes(random);
				key = toHex(random);
				prefs.edit().putString(PREF_KEY, key).commit();
			}

			String androidId = Settings.Secure.getString(context.getContentResolver(),
					Settings.Secure.ANDROID_ID);
			return key + ":" + (androidId == null ? "" : androidId);
		}
	}

	public static byte[] encrypt(Context context, String password) {
		try {
			return PubkeyUtils.encrypt(password.getBytes("UTF-8"), getSecret(context));
		} catch (Exception e) {
			Log.e(TAG, "Could not encrypt password", e);
			return null;
		}
	}

	public static String decrypt(Context context, byte[] encryptedPassword) {
		if (encryptedPassword == null || encryptedPassword.length == 0)
			return null;

		try {
			return new String(PubkeyUtils.decrypt(encryptedPassword, getSecret(context)), "UTF-8");
		} catch (Exception e) {
			Log.e(TAG, "Could not decrypt saved password", e);
			return null;
		}
	}
}
