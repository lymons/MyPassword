package cn.xing.mypassword.utils;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.annotation.SuppressLint;
import android.util.Base64;
import android.util.Log;

/**
 * AES加密工具
 */
public class AES {
    
    public static final String TAG = "AES";
    public static final String version = "10*";
    public static final String versionNumber = "10";
    public static final char delimiter = '*';
    public static final int seedLength = 8;
	/**
	 * AES加密
	 */
	public static String encrypt(String cleartext, String seed) throws Exception {
		byte[] rawKey = getRawKey(seed.getBytes());
		byte[] result = encrypt(rawKey, cleartext.getBytes());
		return Base64.encodeToString(result, 0);
	}
	
	public static String encrypt(String cleartext) throws Exception  {
	    StringRandom sr = StringRandom.getInstance();
        String seed = sr.getString(seedLength);
	    String encodeString = encrypt(cleartext, seed);
	    return (version+encodeString+seed);
	}

	/**
	 * AES解密
	 */
	public static String decrypt(String encrypted, String seed) throws Exception {
		byte[] rawKey = getRawKey(seed.getBytes());
		byte[] enc = Base64.decode(encrypted, 0);
		byte[] result = decrypt(rawKey, enc);
		return new String(result);
	}
	
	public static String decrypt(String encrypted) throws Exception {
	    if (encrypted == null || encrypted.length() <= version.length()) {
            return encrypted;
        }
        String v = encrypted.substring(0, version.length());
        if (v.charAt(v.length()-1) != delimiter) {
            Log.e(TAG, encrypted + " <may be decrypt by seed> ");
            return encrypted;
        }
        v = v.substring(0, version.length()-1);
        int r = v.compareTo(versionNumber);
        if (r > 0) {
            Log.e(TAG, encrypted + " <encrypt version is new, use the newest app please> ");
            return null;
        } else if (r < 0) {
            Log.e(TAG, encrypted + " <encrypt version is old, will be convert to newest version> ");
            return null;
        }
        String seed = encrypted.substring(encrypted.length()-seedLength);
        String pure = encrypted.substring(version.length(), encrypted.length()-seedLength-1);
        return decrypt(pure, seed);
    }

	@SuppressLint("TrulyRandom")
	private static byte[] getRawKey(byte[] seed) throws Exception {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
		sr.setSeed(seed);
		kgen.init(128, sr);
		SecretKey skey = kgen.generateKey();
		byte[] raw = skey.getEncoded();
		return raw;
	}

	private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		byte[] encrypted = cipher.doFinal(clear);
		return encrypted;
	}

	private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		byte[] decrypted = cipher.doFinal(encrypted);
		return decrypted;
	}
}
