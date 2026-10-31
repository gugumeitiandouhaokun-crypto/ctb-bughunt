/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.util.MessageSupplier;
import org.apache.logging.log4j.util.Supplier;

public final class LambdaUtil {
    private LambdaUtil() {
    }

    public static Object[] getAll(Supplier<?> ... supplierArray) {
        if (supplierArray == null) {
            return null;
        }
        Object[] objectArray = new Object[supplierArray.length];
        for (int j = 0; j < objectArray.length; ++j) {
            objectArray[j] = LambdaUtil.get(supplierArray[j]);
        }
        return objectArray;
    }

    public static Object get(Supplier<?> supplier) {
        if (supplier == null) {
            return null;
        }
        if ((supplier = supplier.get()) instanceof Message) {
            return ((Message)((Object)supplier)).getFormattedMessage();
        }
        return supplier;
    }

    public static Message get(MessageSupplier messageSupplier) {
        if (messageSupplier == null) {
            return null;
        }
        return messageSupplier.get();
    }

    public static Message getMessage(Supplier<?> supplier, MessageFactory messageFactory) {
        if (supplier == null) {
            return null;
        }
        if ((supplier = supplier.get()) instanceof Message) {
            return (Message)((Object)supplier);
        }
        return messageFactory.newMessage(supplier);
    }
}

