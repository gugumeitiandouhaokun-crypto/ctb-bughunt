/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.spi;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.spi.MutableThreadContextStack;
import org.apache.logging.log4j.spi.ThreadContextStack;
import org.apache.logging.log4j.util.StringBuilderFormattable;
import org.apache.logging.log4j.util.StringBuilders;

public class DefaultThreadContextStack
implements ThreadContextStack,
StringBuilderFormattable {
    private static final long serialVersionUID = 5050501L;
    private static final ThreadLocal<MutableThreadContextStack> STACK = new ThreadLocal();
    private final boolean useStack;

    public DefaultThreadContextStack(boolean bl) {
        this.useStack = bl;
    }

    private MutableThreadContextStack getNonNullStackCopy() {
        MutableThreadContextStack mutableThreadContextStack = STACK.get();
        return mutableThreadContextStack == null ? new MutableThreadContextStack() : mutableThreadContextStack.copy();
    }

    @Override
    public boolean add(String string) {
        if (!this.useStack) {
            return false;
        }
        MutableThreadContextStack mutableThreadContextStack = this.getNonNullStackCopy();
        mutableThreadContextStack.add(string);
        mutableThreadContextStack.freeze();
        STACK.set(mutableThreadContextStack);
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends String> collection) {
        if (!this.useStack || collection.isEmpty()) {
            return false;
        }
        MutableThreadContextStack mutableThreadContextStack = this.getNonNullStackCopy();
        mutableThreadContextStack.addAll(collection);
        mutableThreadContextStack.freeze();
        STACK.set(mutableThreadContextStack);
        return true;
    }

    @Override
    public List<String> asList() {
        MutableThreadContextStack mutableThreadContextStack = STACK.get();
        if (mutableThreadContextStack == null) {
            return Collections.emptyList();
        }
        return mutableThreadContextStack.asList();
    }

    @Override
    public void clear() {
        STACK.remove();
    }

    @Override
    public boolean contains(Object object) {
        MutableThreadContextStack mutableThreadContextStack = STACK.get();
        return mutableThreadContextStack != null && mutableThreadContextStack.contains(object);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        if (collection.isEmpty()) {
            return true;
        }
        MutableThreadContextStack mutableThreadContextStack = STACK.get();
        return mutableThreadContextStack != null && mutableThreadContextStack.containsAll(collection);
    }

    @Override
    public ThreadContextStack copy() {
        MutableThreadContextStack mutableThreadContextStack;
        if (!this.useStack || (mutableThreadContextStack = STACK.get()) == null) {
            return new MutableThreadContextStack();
        }
        return mutableThreadContextStack.copy();
    }

    @Override
    public boolean equals(Object object) {
        ThreadContextStack threadContextStack;
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (object instanceof DefaultThreadContextStack) {
            threadContextStack = (DefaultThreadContextStack)object;
            if (this.useStack != threadContextStack.useStack) {
                return false;
            }
        }
        if (!(object instanceof ThreadContextStack)) {
            return false;
        }
        threadContextStack = (ThreadContextStack)object;
        object = STACK.get();
        if (object == null) {
            return false;
        }
        return ((MutableThreadContextStack)object).equals(threadContextStack);
    }

    @Override
    public int getDepth() {
        MutableThreadContextStack mutableThreadContextStack = STACK.get();
        if (mutableThreadContextStack == null) {
            return 0;
        }
        return mutableThreadContextStack.getDepth();
    }

    @Override
    public int hashCode() {
        MutableThreadContextStack mutableThreadContextStack = STACK.get();
        return 31 + (mutableThreadContextStack == null ? 0 : mutableThreadContextStack.hashCode());
    }

    @Override
    public boolean isEmpty() {
        MutableThreadContextStack mutableThreadContextStack = STACK.get();
        return mutableThreadContextStack == null || mutableThreadContextStack.isEmpty();
    }

    @Override
    public Iterator<String> iterator() {
        MutableThreadContextStack mutableThreadContextStack = STACK.get();
        if (mutableThreadContextStack == null) {
            return Collections.emptyList().iterator();
        }
        return mutableThreadContextStack.iterator();
    }

    @Override
    public String peek() {
        MutableThreadContextStack mutableThreadContextStack = STACK.get();
        if (mutableThreadContextStack == null || mutableThreadContextStack.size() == 0) {
            return "";
        }
        return mutableThreadContextStack.peek();
    }

    @Override
    public String pop() {
        if (!this.useStack) {
            return "";
        }
        MutableThreadContextStack mutableThreadContextStack = STACK.get();
        if (mutableThreadContextStack == null || mutableThreadContextStack.size() == 0) {
            return "";
        }
        mutableThreadContextStack = (MutableThreadContextStack)mutableThreadContextStack.copy();
        String string = mutableThreadContextStack.pop();
        mutableThreadContextStack.freeze();
        STACK.set(mutableThreadContextStack);
        return string;
    }

    @Override
    public void push(String string) {
        if (!this.useStack) {
            return;
        }
        this.add(string);
    }

    @Override
    public boolean remove(Object object) {
        if (!this.useStack) {
            return false;
        }
        MutableThreadContextStack mutableThreadContextStack = STACK.get();
        if (mutableThreadContextStack == null || mutableThreadContextStack.size() == 0) {
            return false;
        }
        mutableThreadContextStack = (MutableThreadContextStack)mutableThreadContextStack.copy();
        boolean bl = mutableThreadContextStack.remove(object);
        mutableThreadContextStack.freeze();
        STACK.set(mutableThreadContextStack);
        return bl;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        if (!this.useStack || collection.isEmpty()) {
            return false;
        }
        MutableThreadContextStack mutableThreadContextStack = STACK.get();
        if (mutableThreadContextStack == null || mutableThreadContextStack.isEmpty()) {
            return false;
        }
        mutableThreadContextStack = (MutableThreadContextStack)mutableThreadContextStack.copy();
        boolean bl = mutableThreadContextStack.removeAll(collection);
        mutableThreadContextStack.freeze();
        STACK.set(mutableThreadContextStack);
        return bl;
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        if (!this.useStack || collection.isEmpty()) {
            return false;
        }
        MutableThreadContextStack mutableThreadContextStack = STACK.get();
        if (mutableThreadContextStack == null || mutableThreadContextStack.isEmpty()) {
            return false;
        }
        mutableThreadContextStack = (MutableThreadContextStack)mutableThreadContextStack.copy();
        boolean bl = mutableThreadContextStack.retainAll(collection);
        mutableThreadContextStack.freeze();
        STACK.set(mutableThreadContextStack);
        return bl;
    }

    @Override
    public int size() {
        MutableThreadContextStack mutableThreadContextStack = STACK.get();
        if (mutableThreadContextStack == null) {
            return 0;
        }
        return mutableThreadContextStack.size();
    }

    @Override
    public Object[] toArray() {
        MutableThreadContextStack mutableThreadContextStack = STACK.get();
        if (mutableThreadContextStack == null) {
            return new String[0];
        }
        MutableThreadContextStack mutableThreadContextStack2 = mutableThreadContextStack;
        return mutableThreadContextStack2.toArray(new Object[mutableThreadContextStack2.size()]);
    }

    @Override
    public <T> T[] toArray(T[] TArray) {
        MutableThreadContextStack mutableThreadContextStack = STACK.get();
        if (mutableThreadContextStack == null) {
            if (TArray.length > 0) {
                TArray[0] = null;
            }
            return TArray;
        }
        return mutableThreadContextStack.toArray(TArray);
    }

    public String toString() {
        MutableThreadContextStack mutableThreadContextStack = STACK.get();
        if (mutableThreadContextStack == null) {
            return "[]";
        }
        return mutableThreadContextStack.toString();
    }

    @Override
    public void formatTo(StringBuilder stringBuilder) {
        MutableThreadContextStack mutableThreadContextStack = STACK.get();
        if (mutableThreadContextStack == null) {
            stringBuilder.append("[]");
            return;
        }
        StringBuilders.appendValue(stringBuilder, mutableThreadContextStack);
    }

    @Override
    public void trim(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Maximum stack depth cannot be negative");
        }
        MutableThreadContextStack mutableThreadContextStack = STACK.get();
        if (mutableThreadContextStack == null) {
            return;
        }
        mutableThreadContextStack = (MutableThreadContextStack)mutableThreadContextStack.copy();
        mutableThreadContextStack.trim(n);
        mutableThreadContextStack.freeze();
        STACK.set(mutableThreadContextStack);
    }

    @Override
    public ThreadContext.ContextStack getImmutableStackOrNull() {
        return STACK.get();
    }
}

