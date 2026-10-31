/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.message;

import java.lang.management.LockInfo;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import org.apache.logging.log4j.message.ThreadInformation;
import org.apache.logging.log4j.util.StringBuilders;

class ExtendedThreadInformation
implements ThreadInformation {
    private final ThreadInfo threadInfo;

    ExtendedThreadInformation(ThreadInfo threadInfo) {
        this.threadInfo = threadInfo;
    }

    @Override
    public void printThreadInfo(StringBuilder stringBuilder) {
        StringBuilders.appendDqValue(stringBuilder, this.threadInfo.getThreadName());
        stringBuilder.append(" Id=").append(this.threadInfo.getThreadId()).append(' ');
        this.formatState(stringBuilder, this.threadInfo);
        if (this.threadInfo.isSuspended()) {
            stringBuilder.append(" (suspended)");
        }
        if (this.threadInfo.isInNative()) {
            stringBuilder.append(" (in native)");
        }
        stringBuilder.append('\n');
    }

    /*
     * WARNING - void declaration
     */
    @Override
    public void printStack(StringBuilder stringBuilder, StackTraceElement[] objectArray) {
        Object object;
        int n2 = 0;
        for (Object stackTraceElement : objectArray) {
            stringBuilder.append("\tat ").append(((StackTraceElement)stackTraceElement).toString());
            stringBuilder.append('\n');
            if (n2 == 0 && this.threadInfo.getLockInfo() != null) {
                object = this.threadInfo.getThreadState();
                switch (1.$SwitchMap$java$lang$Thread$State[((Enum)object).ordinal()]) {
                    case 1: {
                        stringBuilder.append("\t-  blocked on ");
                        this.formatLock(stringBuilder, this.threadInfo.getLockInfo());
                        stringBuilder.append('\n');
                        break;
                    }
                    case 2: {
                        stringBuilder.append("\t-  waiting on ");
                        this.formatLock(stringBuilder, this.threadInfo.getLockInfo());
                        stringBuilder.append('\n');
                        break;
                    }
                    case 3: {
                        stringBuilder.append("\t-  waiting on ");
                        this.formatLock(stringBuilder, this.threadInfo.getLockInfo());
                        stringBuilder.append('\n');
                    }
                }
            }
            for (MonitorInfo monitorInfo : this.threadInfo.getLockedMonitors()) {
                if (monitorInfo.getLockedStackDepth() != n2) continue;
                stringBuilder.append("\t-  locked ");
                this.formatLock(stringBuilder, monitorInfo);
                stringBuilder.append('\n');
            }
            ++n2;
        }
        objectArray = this.threadInfo.getLockedSynchronizers();
        if (objectArray.length > 0) {
            void var6_10;
            stringBuilder.append("\n\tNumber of locked synchronizers = ").append(objectArray.length).append('\n');
            Object[] objectArray2 = objectArray;
            int n3 = objectArray.length;
            boolean bl = false;
            while (var6_10 < n3) {
                object = objectArray2[var6_10];
                stringBuilder.append("\t- ");
                this.formatLock(stringBuilder, (LockInfo)object);
                stringBuilder.append('\n');
                ++var6_10;
            }
        }
    }

    private void formatLock(StringBuilder stringBuilder, LockInfo lockInfo) {
        stringBuilder.append('<').append(lockInfo.getIdentityHashCode()).append("> (a ");
        stringBuilder.append(lockInfo.getClassName()).append(')');
    }

    private void formatState(StringBuilder stringBuilder, ThreadInfo threadInfo) {
        Object object = threadInfo.getThreadState();
        stringBuilder.append(object);
        switch (1.$SwitchMap$java$lang$Thread$State[((Enum)object).ordinal()]) {
            case 1: {
                stringBuilder.append(" (on object monitor owned by \"");
                stringBuilder.append(threadInfo.getLockOwnerName()).append("\" Id=").append(threadInfo.getLockOwnerId()).append(')');
                return;
            }
            case 2: {
                object = threadInfo.getStackTrace()[0];
                String string = ((StackTraceElement)object).getClassName();
                object = ((StackTraceElement)object).getMethodName();
                if (string.equals("java.lang.Object") && ((String)object).equals("wait")) {
                    stringBuilder.append(" (on object monitor");
                    if (threadInfo.getLockOwnerName() != null) {
                        stringBuilder.append(" owned by \"");
                        stringBuilder.append(threadInfo.getLockOwnerName()).append("\" Id=").append(threadInfo.getLockOwnerId());
                    }
                    stringBuilder.append(')');
                    return;
                }
                if (string.equals("java.lang.Thread") && ((String)object).equals("join")) {
                    stringBuilder.append(" (on completion of thread ").append(threadInfo.getLockOwnerId()).append(')');
                    return;
                }
                stringBuilder.append(" (parking for lock");
                if (threadInfo.getLockOwnerName() != null) {
                    stringBuilder.append(" owned by \"");
                    stringBuilder.append(threadInfo.getLockOwnerName()).append("\" Id=").append(threadInfo.getLockOwnerId());
                }
                stringBuilder.append(')');
                return;
            }
            case 3: {
                object = threadInfo.getStackTrace()[0];
                String string = ((StackTraceElement)object).getClassName();
                object = ((StackTraceElement)object).getMethodName();
                if (string.equals("java.lang.Object") && ((String)object).equals("wait")) {
                    stringBuilder.append(" (on object monitor");
                    if (threadInfo.getLockOwnerName() != null) {
                        stringBuilder.append(" owned by \"");
                        stringBuilder.append(threadInfo.getLockOwnerName()).append("\" Id=").append(threadInfo.getLockOwnerId());
                    }
                    stringBuilder.append(')');
                    return;
                }
                if (string.equals("java.lang.Thread") && ((String)object).equals("sleep")) {
                    stringBuilder.append(" (sleeping)");
                    return;
                }
                if (string.equals("java.lang.Thread") && ((String)object).equals("join")) {
                    stringBuilder.append(" (on completion of thread ").append(threadInfo.getLockOwnerId()).append(')');
                    return;
                }
                stringBuilder.append(" (parking for lock");
                if (threadInfo.getLockOwnerName() != null) {
                    stringBuilder.append(" owned by \"");
                    stringBuilder.append(threadInfo.getLockOwnerName()).append("\" Id=").append(threadInfo.getLockOwnerId());
                }
                stringBuilder.append(')');
            }
        }
    }
}

