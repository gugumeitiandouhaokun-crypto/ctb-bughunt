/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.message;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.core.message.ExtendedThreadInformation;
import org.apache.logging.log4j.message.ThreadDumpMessage;
import org.apache.logging.log4j.message.ThreadInformation;

public class ExtendedThreadInfoFactory
implements ThreadDumpMessage.ThreadInfoFactory {
    public ExtendedThreadInfoFactory() {
        Method[] methodArray = ThreadInfo.class.getMethods();
        boolean bl = true;
        int n = methodArray.length;
        for (int j = 0; j < n; ++j) {
            if (!methodArray[j].getName().equals("getLockInfo")) continue;
            bl = false;
            break;
        }
        if (bl) {
            throw new IllegalStateException();
        }
    }

    @Override
    public Map<ThreadInformation, StackTraceElement[]> createThreadInfo() {
        ThreadInfo[] threadInfoArray = ManagementFactory.getThreadMXBean().dumpAllThreads(true, true);
        HashMap<ThreadInformation, StackTraceElement[]> hashMap = new HashMap<ThreadInformation, StackTraceElement[]>(threadInfoArray.length);
        for (ThreadInfo threadInfo : threadInfoArray) {
            hashMap.put(new ExtendedThreadInformation(threadInfo), threadInfo.getStackTrace());
        }
        return hashMap;
    }
}

