/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.h;

import com.huawei.tools.d.a;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;

public final class e {
    /*
     * Unable to fully structure code
     */
    public static String z(String var0, String var1_5) {
        block14: {
            var2_7 = new Properties();
            var3_8 = null;
            try {
                if (StringUtils.isBlank(var0)) {
                    var0 = "";
                    return var0;
                }
                var3_8 = new BufferedInputStream(new FileInputStream(var0));
                var2_7.load(var3_8);
                ** try [egrp 2[TRYBLOCK] [0 : 43->50)] { 
            }
            catch (Exception var0_2) {
                a.info("\u8bfb\u53d6properties\u65f6\u51fa\u73b0\u5f02\u5e38\uff1a".concat(String.valueOf(var0_2)));
                break block14;
            }
            finally {
                try {
                    if (var3_8 != null) {
                        var3_8.close();
                    }
                }
                catch (IOException var0_3) {
                    a.info("\u8bfb\u53d6properties\u65f6\u51fa\u73b0\u5f02\u5e38\uff1a".concat(String.valueOf(var0_3)));
                }
            }
lbl-1000:
            // 1 sources

            {
                var3_8.close();
            }
lbl12:
            // 1 sources

            catch (IOException var0_1) {
                a.info("\u8bfb\u53d6properties\u65f6\u51fa\u73b0\u5f02\u5e38\uff1a".concat(String.valueOf(var0_1)));
            }
            {
            }
        }
        return var2_7.getProperty(var1_5);
    }
}

