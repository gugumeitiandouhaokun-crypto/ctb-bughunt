/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.h;

import com.huawei.tools.h.f;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class a {
    static byte[] cX = a.u();
    static byte[] cY = new byte[]{-28, -86, 85, -120, 20, 109, 96, 71, -77, -3, 124, 16, 111, -56, 5, -28};
    static String cZ = "2022-04-01 00:00:00";
    static Base64.Decoder da = Base64.getDecoder();
    static Base64.Encoder db = Base64.getEncoder();

    private static byte[] u() {
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
        }
        catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            noSuchAlgorithmException.printStackTrace();
        }
        keyGenerator.init(128);
        return keyGenerator.generateKey().getEncoded();
    }

    private static byte[] a(byte[] byArray, byte[] byArray2) {
        byte[] byArray3 = new byte[4];
        System.arraycopy(byArray, 0, byArray3, 0, 4);
        int n = f.a(byArray3);
        byte[] byArray4 = new byte[n];
        System.arraycopy(byArray, 12, byArray4, 0, n);
        System.arraycopy(byArray, 4, byArray3, 0, 4);
        int n2 = f.a(byArray3);
        System.arraycopy(byArray, 8, byArray3, 0, 4);
        int n3 = f.a(byArray3);
        byte[] byArray5 = n3 == 0 ? null : new byte[n3];
        if (byArray5 != null) {
            System.arraycopy(byArray, 12 + n, byArray5, 0, n3);
        }
        byte[] byArray6 = new byte[byArray.length - n - n3 - 12];
        System.arraycopy(byArray, 12 + n + n3, byArray6, 0, byArray6.length);
        return a.a(byArray6, byArray2, byArray4, n2, byArray5);
    }

    private static byte[] a(byte[] byArray, byte[] object, byte[] object2, int n, byte[] byArray2) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/PKCS5Padding");
            object = new SecretKeySpec((byte[])object, "AES");
            object2 = new GCMParameterSpec(n * 8, (byte[])object2);
            cipher.init(2, (Key)object, (AlgorithmParameterSpec)object2);
            if (byArray2 != null) {
                cipher.updateAAD(byArray2);
            }
            return cipher.doFinal(byArray);
        }
        catch (Exception exception) {
            return null;
        }
    }

    public static String aj(String string) {
        return new String(a.a(da.decode(string), cY), StandardCharsets.UTF_8);
    }
}

