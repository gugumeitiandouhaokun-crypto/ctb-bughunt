/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.rolling.action;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.core.appender.rolling.action.AbstractAction;
import org.apache.logging.log4j.core.appender.rolling.action.Action;

public class CompositeAction
extends AbstractAction {
    private final Action[] actions;
    private final boolean stopOnError;

    public CompositeAction(List<Action> list, boolean bl) {
        this.actions = new Action[list.size()];
        list.toArray(this.actions);
        this.stopOnError = bl;
    }

    @Override
    public void run() {
        try {
            this.execute();
            return;
        }
        catch (IOException iOException) {
            LOGGER.warn("Exception during file rollover.", (Throwable)iOException);
            return;
        }
    }

    @Override
    public boolean execute() {
        if (this.stopOnError) {
            Action[] actionArray = this.actions;
            int n = this.actions.length;
            for (int i2 = 0; i2 < n; ++i2) {
                if (actionArray[i2].execute()) continue;
                return false;
            }
            return true;
        }
        boolean bl = true;
        IOException iOException = null;
        Action[] actionArray = this.actions;
        int n = this.actions.length;
        for (int i3 = 0; i3 < n; ++i3) {
            Action action = actionArray[i3];
            try {
                bl &= action.execute();
                continue;
            }
            catch (IOException iOException2) {
                bl = false;
                if (iOException != null) continue;
                iOException = iOException2;
            }
        }
        if (iOException != null) {
            throw iOException;
        }
        return bl;
    }

    public String toString() {
        return CompositeAction.class.getSimpleName() + Arrays.toString(this.actions);
    }

    public Action[] getActions() {
        return this.actions;
    }

    public boolean isStopOnError() {
        return this.stopOnError;
    }
}

