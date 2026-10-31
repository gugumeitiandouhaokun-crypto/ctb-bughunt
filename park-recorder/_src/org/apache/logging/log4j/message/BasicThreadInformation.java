/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.message;

import org.apache.logging.log4j.message.ThreadInformation;
import org.apache.logging.log4j.util.StringBuilders;

class BasicThreadInformation
implements ThreadInformation {
    private static final int HASH_SHIFT = 32;
    private static final int HASH_MULTIPLIER = 31;
    private final long id;
    private final String name;
    private final String longName;
    private final Thread.State state;
    private final int priority;
    private final boolean isAlive;
    private final boolean isDaemon;
    private final String threadGroupName;

    BasicThreadInformation(Thread object) {
        this.id = ((Thread)object).getId();
        this.name = ((Thread)object).getName();
        this.longName = ((Thread)object).toString();
        this.state = ((Thread)object).getState();
        this.priority = ((Thread)object).getPriority();
        this.isAlive = ((Thread)object).isAlive();
        this.isDaemon = ((Thread)object).isDaemon();
        object = ((Thread)object).getThreadGroup();
        this.threadGroupName = object == null ? null : ((ThreadGroup)object).getName();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        object = (BasicThreadInformation)object;
        if (this.id != ((BasicThreadInformation)object).id) {
            return false;
        }
        return !(this.name != null ? !this.name.equals(((BasicThreadInformation)object).name) : ((BasicThreadInformation)object).name != null);
    }

    public int hashCode() {
        int n = (int)(this.id ^ this.id >>> 32);
        return 31 * n + (this.name != null ? this.name.hashCode() : 0);
    }

    @Override
    public void printThreadInfo(StringBuilder stringBuilder) {
        StringBuilders.appendDqValue(stringBuilder, this.name).append(' ');
        if (this.isDaemon) {
            stringBuilder.append("daemon ");
        }
        stringBuilder.append("prio=").append(this.priority).append(" tid=").append(this.id).append(' ');
        if (this.threadGroupName != null) {
            StringBuilders.appendKeyDqValue(stringBuilder, "group", this.threadGroupName);
        }
        stringBuilder.append('\n');
        stringBuilder.append("\tThread state: ").append(this.state.name()).append('\n');
    }

    @Override
    public void printStack(StringBuilder stringBuilder, StackTraceElement[] stackTraceElementArray) {
        for (StackTraceElement stackTraceElement : stackTraceElementArray) {
            stringBuilder.append("\tat ").append(stackTraceElement).append('\n');
        }
    }
}

