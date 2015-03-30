package com.crypho.plugins;

import android.util.Log;
import android.util.Base64;

import android.content.Context;
import android.content.Intent;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONException;
import org.json.JSONObject;
import javax.crypto.Cipher;

public class SecureStorage extends CordovaPlugin {
    private static final String TAG = "SecureStorage";

    private String ALIAS = null;

    private CallbackContext inιtializationContext;
    private boolean inιtializing = false;

    @Override
    public void onResume(boolean multitasking) {
        if (inιtializing) {
            inιtializing = false;
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    try {
                        if (!RSA.isEntryAvailable(ALIAS)) {
                            RSA.createKeyPair(getContext(), ALIAS);
                        }
                        inιtializationContext.success();
                    } catch (Exception e) {
                        Log.e(TAG, "Init failed :", e);
                        inιtializationContext.error(e.getMessage());
                    }
                }
            });
        }
    }

    @Override
    public boolean execute(String action, CordovaArgs args, final CallbackContext callbackContext) throws JSONException {
        if ("init".equals(action)) {
            inιtializing = true;
            inιtializationContext = callbackContext;
            ALIAS = getContext().getPackageName() + "." + args.getString(0);
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    Intent intent = new Intent("com.android.credentials.UNLOCK");
                    startActivity(intent);
                }
            });
            return true;
        }
        if ("encrypt".equals(action)) {
            final String encryptMe = args.getString(0);
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    try {
                        Cipher encKeyCipher = RSA.createCipher(Cipher.ENCRYPT_MODE, ALIAS);
                        JSONObject result = AES.encrypt(encryptMe.getBytes(), encKeyCipher);
                        callbackContext.success(result);
                    } catch (Exception e) {
                        Log.e(TAG, "Encrypt failed :", e);
                        callbackContext.error(e.getMessage());
                    }
                }
            });
            return true;
        }
        if ("decrypt".equals(action)) {
            final byte[] encKey = args.getArrayBuffer(0);
            final byte[] iv = args.getArrayBuffer(1);
            final byte[] decryptMe = args.getArrayBuffer(2);
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    try {
                        byte[] key = RSA.decrypt(encKey, ALIAS);
                        String decrypted = new String(AES.decrypt(decryptMe, key, iv));
                        callbackContext.success(decrypted);
                    } catch (Exception e) {
                        Log.e(TAG, "Decrypt failed :", e);
                        callbackContext.error(e.getMessage());
                    }
                }
            });
            return true;
        }
        if ("encrypt_rsa".equals(action)) {
            final String encryptMe = args.getString(0);
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    try {
                        byte[] encrypted = RSA.encrypt(encryptMe.getBytes(), ALIAS);
                        callbackContext.success(Base64.encodeToString(encrypted, Base64.DEFAULT));
                    } catch (Exception e) {
                        Log.e(TAG, "Encrypt failed :", e);
                        callbackContext.error(e.getMessage());
                    }
                }
            });
            return true;
        }
        if ("decrypt_rsa".equals(action)) {
            final byte[] decryptMe = args.getArrayBuffer(0);
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    try {
                        byte[] decrypted = RSA.decrypt(decryptMe, ALIAS);
                        callbackContext.success(new String (decrypted));
                    } catch (Exception e) {
                        Log.e(TAG, "Decrypt failed :", e);
                        callbackContext.error(e.getMessage());
                    }
                }
            });
            return true;
        }
        return false;
    }

    private Context getContext(){
        return cordova.getActivity().getApplicationContext();
    }

    private void startActivity(Intent intent){
        cordova.getActivity().startActivity(intent);
    }
}