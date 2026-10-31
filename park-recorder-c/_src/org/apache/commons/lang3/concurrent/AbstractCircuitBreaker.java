/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.concurrent;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang3.concurrent.CircuitBreaker;

public abstract class AbstractCircuitBreaker<T>
implements CircuitBreaker<T> {
    public static final String PROPERTY_NAME = "open";
    protected final AtomicReference<State> state = new AtomicReference<State>(State.CLOSED);
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    @Override
    public boolean isOpen() {
        return AbstractCircuitBreaker.isOpen(this.state.get());
    }

    @Override
    public boolean isClosed() {
        return !this.isOpen();
    }

    @Override
    public abstract boolean checkState();

    @Override
    public abstract boolean incrementAndCheckState(T var1);

    @Override
    public void close() {
        this.changeState(State.CLOSED);
    }

    @Override
    public void open() {
        this.changeState(State.OPEN);
    }

    protected static boolean isOpen(State state) {
        return state == State.OPEN;
    }

    protected void changeState(State state) {
        if (this.state.compareAndSet(state.oppositeState(), state)) {
            this.changeSupport.firePropertyChange(PROPERTY_NAME, !AbstractCircuitBreaker.isOpen(state), AbstractCircuitBreaker.isOpen(state));
        }
    }

    public void addChangeListener(PropertyChangeListener propertyChangeListener) {
        this.changeSupport.addPropertyChangeListener(propertyChangeListener);
    }

    public void removeChangeListener(PropertyChangeListener propertyChangeListener) {
        this.changeSupport.removePropertyChangeListener(propertyChangeListener);
    }

    protected static enum State {
        CLOSED{

            @Override
            public final State oppositeState() {
                return OPEN;
            }
        }
        ,
        OPEN{

            @Override
            public final State oppositeState() {
                return CLOSED;
            }
        };


        public abstract State oppositeState();
    }
}

