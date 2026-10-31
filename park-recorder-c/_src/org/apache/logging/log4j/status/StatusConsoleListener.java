/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.status;

import java.io.PrintStream;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.status.StatusData;
import org.apache.logging.log4j.status.StatusListener;

public class StatusConsoleListener
implements StatusListener {
    private Level level = Level.FATAL;
    private String[] filters;
    private final PrintStream stream;

    public StatusConsoleListener(Level level) {
        this(level, System.out);
    }

    public StatusConsoleListener(Level level, PrintStream printStream) {
        if (printStream == null) {
            throw new IllegalArgumentException("You must provide a stream to use for this listener.");
        }
        this.level = level;
        this.stream = printStream;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    @Override
    public Level getStatusLevel() {
        return this.level;
    }

    @Override
    public void log(StatusData statusData) {
        if (!this.filtered(statusData)) {
            this.stream.println(statusData.getFormattedStatus());
        }
    }

    public void setFilters(String ... stringArray) {
        this.filters = stringArray;
    }

    private boolean filtered(StatusData object) {
        if (this.filters == null) {
            return false;
        }
        object = ((StatusData)object).getStackTraceElement().getClassName();
        String[] stringArray = this.filters;
        int n = this.filters.length;
        for (int i2 = 0; i2 < n; ++i2) {
            String string = stringArray[i2];
            if (!((String)object).startsWith(string)) continue;
            return true;
        }
        return false;
    }

    @Override
    public void close() {
        if (this.stream != System.out && this.stream != System.err) {
            this.stream.close();
        }
    }
}

