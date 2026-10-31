/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util;

import java.net.URLConnection;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.AuthorizationProvider;
import org.apache.logging.log4j.core.util.PasswordDecryptor;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.Base64Util;
import org.apache.logging.log4j.util.LoaderUtil;
import org.apache.logging.log4j.util.PropertiesUtil;

public class BasicAuthorizationProvider
implements AuthorizationProvider {
    private static final String[] PREFIXES = new String[]{"log4j2.config.", "logging.auth."};
    private static final String AUTH_USER_NAME = "username";
    private static final String AUTH_PASSWORD = "password";
    private static final String AUTH_PASSWORD_DECRYPTOR = "passwordDecryptor";
    public static final String CONFIG_USER_NAME = "log4j2.configurationUserName";
    public static final String CONFIG_PASSWORD = "log4j2.configurationPassword";
    public static final String PASSWORD_DECRYPTOR = "log4j2.passwordDecryptor";
    private static Logger LOGGER = StatusLogger.getLogger();
    private String authString = null;

    public BasicAuthorizationProvider(PropertiesUtil object) {
        String string = ((PropertiesUtil)object).getStringProperty(PREFIXES, AUTH_USER_NAME, () -> BasicAuthorizationProvider.lambda$new$0((PropertiesUtil)object));
        String string2 = ((PropertiesUtil)object).getStringProperty(PREFIXES, AUTH_PASSWORD, () -> BasicAuthorizationProvider.lambda$new$1((PropertiesUtil)object));
        object = ((PropertiesUtil)object).getStringProperty(PREFIXES, AUTH_PASSWORD_DECRYPTOR, () -> BasicAuthorizationProvider.lambda$new$2((PropertiesUtil)object));
        if (object != null) {
            try {
                object = LoaderUtil.newInstanceOf((String)object);
                if (object instanceof PasswordDecryptor) {
                    string2 = ((PasswordDecryptor)object).decryptPassword(string2);
                }
            }
            catch (Exception exception) {
                LOGGER.warn("Unable to decrypt password.", (Throwable)exception);
            }
        }
        if (string != null && string2 != null) {
            this.authString = "Basic " + Base64Util.encode(string + ":" + string2);
        }
    }

    @Override
    public void addAuthorization(URLConnection uRLConnection) {
        if (this.authString != null) {
            uRLConnection.setRequestProperty("Authorization", this.authString);
        }
    }

    private static /* synthetic */ String lambda$new$2(PropertiesUtil propertiesUtil) {
        return propertiesUtil.getStringProperty(PASSWORD_DECRYPTOR);
    }

    private static /* synthetic */ String lambda$new$1(PropertiesUtil propertiesUtil) {
        return propertiesUtil.getStringProperty(CONFIG_PASSWORD);
    }

    private static /* synthetic */ String lambda$new$0(PropertiesUtil propertiesUtil) {
        return propertiesUtil.getStringProperty(CONFIG_USER_NAME);
    }
}

