/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.plugins.validation.validators;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.plugins.validation.ConstraintValidator;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.util.Assert;
import org.apache.logging.log4j.status.StatusLogger;

public class RequiredValidator
implements ConstraintValidator<Required> {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private Required annotation;

    @Override
    public void initialize(Required required) {
        this.annotation = required;
    }

    @Override
    public boolean isValid(String string, Object object) {
        return Assert.isNonEmpty(object) || this.err(string);
    }

    private boolean err(String string) {
        LOGGER.error(this.annotation.message() + ": " + string);
        return false;
    }
}

