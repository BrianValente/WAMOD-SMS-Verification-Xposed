package com.wamod.xposed;

import android.app.Activity;
import android.app.AndroidAppHelper;
import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by brianvalente on 2/12/16.
 */
public class WAMOD implements IXposedHookLoadPackage {
    Context mContext = null;

    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.whatsapp")) return;

        XposedHelpers.findAndHookMethod("com.whatsapp.Main", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                // this will be called before the clock was updated by the original method
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                // this will be called after the clock was updated by the original method
                Log.i("XPOSED_WAMOD", "Loading...");

                mContext = (Activity) param.thisObject;
            }
        });


        XposedHelpers.findAndHookMethod("com.whatsapp.aya", lpparam.classLoader, "a", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                byte[] result = (byte[]) param.getResult();
                toTxt(Base64.encodeToString(result, Base64.DEFAULT));
            }
        });



        /*Class b9 = XposedHelpers.findClass("com.whatsapp.b9", lpparam.classLoader);
        XposedHelpers.findAndHookMethod(b9, "a", byte[].class, byte[].class, int.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                byte[] bytes1 = (byte[]) param.args[0];
                byte[] bytes2 = (byte[]) param.args[1];
                int    int1   = (int)    param.args[2];
                int    int2   = (int)    param.args[3];

                String message = "com.whatsapp.b9.a, args:\n" + Base64.encodeToString(bytes1, Base64.DEFAULT) + "\n" + Base64.encodeToString(bytes2, Base64.DEFAULT) + "\n" + int1 + "\n" + int2;
                Log.i("XPOSED_WAMOD", message);

                toTxt(message);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mSecretKey = (SecretKey) param.getResult();
                toTxt(Base64.encodeToString(mSecretKey.getEncoded(), Base64.DEFAULT));
            }
        });*/

    }

    public static void toTxt(String str) {
        try
        {
            File root = new File(Environment.getExternalStorageDirectory(), "Notes");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, "wamod.txt");
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(str);
            writer.flush();
            writer.close();
            Log.i("XPOSED_WAMOD", "File saved! " + gpxfile.getAbsolutePath());
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void logMac(Mac mac) {
        Log.i("XPOSED_WAMOD", Base64.encodeToString(mac.doFinal(), Base64.DEFAULT));
    }

    public static SecretKey getSecretKey() {
        return new SecretKey() {
            @Override
            public String getAlgorithm() {
                return "PBKDF2WithHmacSHA1And8bit";
            }

            @Override
            public String getFormat() {
                return "RAW";
            }

            @Override
            public byte[] getEncoded() {
                byte[] bytes = Base64.decode("eQV5aq/Cg63Gsq1sshN9T3gh+UUp0wIw0xgHYT1bnCjEqOJQKCRrWxdAe2yvsDeCJL+Y4G3PRD2H\n" +
                        "UF7oUgiGow==", Base64.DEFAULT);
                return bytes;
            }
        };
    }

    public static Signature[] getSignature() {
        Signature[] LeakedSignatureArray = new Signature[1];
        String LeakedSignature_String = "30820332308202f0a00302010202044c2536a4300b06072a8648ce3804030500307c310b3009060355040613025553311330110603550408130a43616c69666f726e6961311430120603550407130b53616e746120436c61726131163014060355040a130d576861747341707020496e632e31143012060355040b130b456e67696e656572696e67311430120603550403130b427269616e204163746f6e301e170d3130303632353233303731365a170d3434303231353233303731365a307c310b3009060355040613025553311330110603550408130a43616c69666f726e6961311430120603550407130b53616e746120436c61726131163014060355040a130d576861747341707020496e632e31143012060355040b130b456e67696e656572696e67311430120603550403130b427269616e204163746f6e308201b83082012c06072a8648ce3804013082011f02818100fd7f53811d75122952df4a9c2eece4e7f611b7523cef4400c31e3f80b6512669455d402251fb593d8d58fabfc5f5ba30f6cb9b556cd7813b801d346ff26660b76b9950a5a49f9fe8047b1022c24fbba9d7feb7c61bf83b57e7c6a8a6150f04fb83f6d3c51ec3023554135a169132f675f3ae2b61d72aeff22203199dd14801c70215009760508f15230bccb292b982a2eb840bf0581cf502818100f7e1a085d69b3ddecbbcab5c36b857b97994afbbfa3aea82f9574c0b3d0782675159578ebad4594fe67107108180b449167123e84c281613b7cf09328cc8a6e13c167a8b547c8d28e0a3ae1e2bb3a675916ea37f0bfa213562f1fb627a01243bcca4f1bea8519089a883dfe15ae59f06928b665e807b552564014c3bfecf492a0381850002818100d1198b4b81687bcf246d41a8a725f0a989a51bce326e84c828e1f556648bd71da487054d6de70fff4b49432b6862aa48fc2a93161b2c15a2ff5e671672dfb576e9d12aaff7369b9a99d04fb29d2bbbb2a503ee41b1ff37887064f41fe2805609063500a8e547349282d15981cdb58a08bede51dd7e9867295b3dfb45ffc6b259300b06072a8648ce3804030500032f00302c021400a602a7477acf841077237be090df436582ca2f0214350ce0268d07e71e55774ab4eacd4d071cd1efad";
        LeakedSignatureArray[0] = new Signature(LeakedSignature_String);
        return LeakedSignatureArray;
    }

    public static byte[] getb9() {
        byte[] official = Base64.decode("ACkLRN4OqtS0sFb/1aGVDQ==", Base64.DEFAULT);
        return official;
    }
}
