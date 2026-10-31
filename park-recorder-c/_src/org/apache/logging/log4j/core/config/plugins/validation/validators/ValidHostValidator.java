/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.plugins.validation.validators;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.plugins.validation.ConstraintValidator;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.ValidHost;
import org.apache.logging.log4j.status.StatusLogger;

public class ValidHostValidator
implements ConstraintValidator<ValidHost> {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private ValidHost annotation;

    @Override
    public void initialize(ValidHost validHost) {
        this.annotation = validHost;
    }

    @Override
    public boolean isValid(String string, Object object) {
        if (object == null) {
            LOGGER.error(this.annotation.message());
            return false;
        }
        if (object instanceof InetAddress) {
            return true;
        }
        try {
            InetAddress.getByName(object.toString());
            return true;
        }
        catch (UnknownHostException unknownHostException) {
            LOGGER.error(this.annotation.message(), (Throwable)unknownHostException);
            return false;
        }
    }
}

