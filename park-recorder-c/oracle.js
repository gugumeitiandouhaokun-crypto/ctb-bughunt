// jjs -cp <jar> oracle.js -- <config.properties> <oracle端口>
// 与被测 jar 同进程运行：先按 jar 自身入口启动 UDP 服务，再在 oracle 端口上应答状态查询（只读，不改动被测状态）。
var cfg = arguments[0];
var oport = parseInt(arguments[1]);

var App = Java.type('com.huawei.tools.Application');
App.main(Java.to([cfg], 'java.lang.String[]'));

var D = Java.type('com.huawei.tools.h.d');
var FCls = Java.type('com.huawei.tools.g.a.f').class;
var LongT = java.lang.Long.TYPE;
var C = java.lang.Class.forName('com.huawei.tools.g.b.a.c');
var feeFull = C.getDeclaredMethod('b', FCls);
feeFull.setAccessible(true);
var feeFrom = C.getDeclaredMethod('a', FCls, LongT);
feeFrom.setAccessible(true);
var LocalDateTime = Java.type('java.time.LocalDateTime');
var Fmt = Java.type('java.time.format.DateTimeFormatter');
var JMath = Java.type('java.lang.Math');
var JString = Java.type('java.lang.String');

function r2(x) { return JMath.round(x * 100.0) / 100.0; }
function f2(x) { return JString.format('%.2f', r2(x)); }

function account(plate) {
    if (!D.ce.containsKey(plate)) return null;
    return D.cd.get(D.ce.get(plate));
}

function due(sid) {
    var f = D.cg.get(sid);
    if (f == null || f.bf > 0) return {ok: false};
    var paid = 'paid' == f.bt && f.bw > 0;
    var d2;
    if (paid) d2 = (D.cb - f.bw > 900) ? feeFrom.invoke(null, f, java.lang.Long.valueOf(f.bw)) : 0.0;
    else d2 = feeFull.invoke(null, f) - f.bu;
    var acct = account(f.aB);
    return {
        ok: true, due: d2, paid: paid, member: acct != null,
        balance: acct != null ? acct.aS : 0.0, gift: acct != null ? acct.aT : 0.0, giftAtEntry: f.bx
    };
}

function revenue(scope, type) {
    var today = LocalDateTime.of(2026, 1, 1, 0, 0).plusSeconds(D.cb).format(Fmt.ofPattern('yyyy-MM-dd'));
    var sum = 0.0;
    var it = D.cs.entrySet().iterator();
    while (it.hasNext()) {
        var e = it.next();
        var k = e.getKey();
        var hit = scope == 'day' ? k == today : scope == 'month' ? k.substring(0, 7) == today.substring(0, 7) : k.substring(0, 4) == today.substring(0, 4);
        if (!hit) continue;
        var v = e.getValue();
        sum += type == 'recharge' ? v.bE : type == 'monthcard' ? v.bF : type == 'temppay' ? v.bD : v.bG;
    }
    return f2(sum);
}

function stats() {
    var e = D.ci.get('P1');
    var out = [];
    ['S', 'M', 'L'].forEach(function (t) { out.push(e.bb.get(t).get() + '|' + e.bc.get(t).get()); });
    return out.join(',');
}

function occupied() {
    var list = [];
    var it = D.cj.values().iterator();
    while (it.hasNext()) {
        var g = it.next();
        if ('U' == g.aN) list.push(String(g.bg));
    }
    list.sort();
    return list.join('|');
}

function handle(line) {
    var p = line.split(' ');
    switch (p[0]) {
        case 'due': return JSON.stringify(due(p[1]));
        case 'revenue': return revenue(p[1], p[2]);
        case 'stats': return stats();
        case 'occupied': return occupied();
        case 'ping': return 'pong';
    }
    return 'unknown';
}

var sock = new java.net.DatagramSocket(oport, java.net.InetAddress.getByName('127.0.0.1'));
var buf = new (Java.type('byte[]'))(65536);
while (true) {
    var pkt = new java.net.DatagramPacket(buf, buf.length);
    sock.receive(pkt);
    var req = new JString(pkt.getData(), 0, pkt.getLength(), 'UTF-8');
    var resp;
    try {
        resp = handle(String(req));
    } catch (ex) {
        resp = 'error ' + ex;
    }
    var bytes = new JString(String(resp)).getBytes('UTF-8');
    sock.send(new java.net.DatagramPacket(bytes, bytes.length, pkt.getAddress(), pkt.getPort()));
}
