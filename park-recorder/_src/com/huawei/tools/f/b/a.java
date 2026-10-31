/*
 * Decompiled with CFR 0.152.
 */
package com.huawei.tools.f.b;

import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

public final class a
implements Runnable {
    private int port;
    private DatagramSocket au;
    private boolean running;
    public Thread av;
    private Thread aw;
    public static List<com.huawei.tools.f.a.a> ax = new ArrayList<com.huawei.tools.f.a.a>();

    public a(int n) {
        this.port = n;
        a a2 = this;
        this.au = new DatagramSocket(a2.port);
        a2.aw = new Thread((Runnable)a2, "server_process");
        a2.aw.start();
    }

    @Override
    public final void run() {
        this.running = true;
        System.out.println("Server started on port " + this.port);
    }

    static /* synthetic */ DatagramSocket a(a a2) {
        return a2.au;
    }

    static /* synthetic */ boolean b(a a2) {
        return a2.running;
    }

    public static interface a {
        public String run();
    }
}

