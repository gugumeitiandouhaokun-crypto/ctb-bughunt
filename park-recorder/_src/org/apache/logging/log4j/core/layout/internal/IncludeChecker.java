/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.layout.internal;

import java.util.List;
import org.apache.logging.log4j.core.layout.internal.ListChecker;

public class IncludeChecker
implements ListChecker {
    private final List<String> list;

    public IncludeChecker(List<String> list) {
        this.list = list;
    }

    @Override
    public boolean check(String string) {
        return this.list.contains(string);
    }

    public String toString() {
        return "ThreadContextIncludes=" + this.list.toString();
    }
}

