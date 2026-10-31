/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender.rolling.action;

import java.io.IOException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.rolling.action.Action;
import org.apache.logging.log4j.status.StatusLogger;

public abstract class AbstractAction
implements Action {
    protected static final Logger LOGGER = StatusLogger.getLogger();
    private boolean complete = false;
    private boolean interrupted = false;

    @Override
    public abstract boolean execute();

    @Override
    public synchronized void run() {
        if (!this.interrupted) {
            try {
                this.execute();
            }
            catch (IOException | RuntimeException exception) {
                this.reportException(exception);
            }
            catch (Error error) {
                this.reportException(new RuntimeException(error));
            }
            this.complete = true;
            this.interrupted = true;
        }
    }

    @Override
    public synchronized void close() {
        this.interrupted = true;
    }

    @Override
    public boolean isComplete() {
        return this.complete;
    }

    public boolean isInterrupted() {
        return this.interrupted;
    }

    protected void reportException(Exception exception) {
        LOGGER.warn("Exception reported by action '{}'", (Object)this.getClass(), (Object)exception);
    }
}

