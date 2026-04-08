package com.kiwi.observability.util;

public final class MetricKeys {

    private MetricKeys() {}

    // connection metrics
    public static final String CON_DRAIN_TIMEOUTS = "con.draintimeouts";
    public static final String CON_PENDING_RESPONSES = "con.pendingresponses";
    public static final String CON_READER_THREAD_ACTIVE = "con.readerthreadactive";
    public static final String CON_TOTAL_CONNECTIONS = "con.totalconnections";
    public static final String CON_ACCEPTED = "con.accepted";
    public static final String CON_CLOSED = "con.closed";
    public static final String CON_REFUSED = "con.refused";
    public static final String CON_CURRENT = "con.current";

    // byte metrics
    public static final String BYTES_IN = "bytes.in";
    public static final String BYTES_OUT = "bytes.out";

    // command metrics
    public static final String CMD_GET = "cmd.get";
    public static final String CMD_SET = "cmd.set";
    public static final String CMD_DEL = "cmd.del";
    public static final String CMD_EXT = "cmd.ext";
    public static final String CMD_INF = "cmd.inf";
    public static final String CMD_PING = "cmd.ping";
    public static final String CMD_EXPIRE = "cmd.expire";
    public static final String CMD_PEXPIRE = "cmd.pexpire";
    public static final String CMD_PERSIST = "cmd.persist";
    public static final String CMD_EXISTS = "cmd.exists";
    public static final String CMD_SETNX = "cmd.setnx";
    public static final String CMD_GETSET = "cmd.getset";
    public static final String CMD_INCR = "cmd.incr";
    public static final String CMD_DECR = "cmd.decr";
    public static final String CMD_INCRBY = "cmd.incrby";
    public static final String CMD_DECRBY = "cmd.decrby";
    public static final String CMD_MGET = "cmd.mget";
    public static final String CMD_MSET = "cmd.mset";
    public static final String CMD_DBSIZE = "cmd.dbsize";

    // protocol metrics
    public static final String PROTO_VERSION = "proto.version";
    public static final String PROTO_INFO_SCHEMA_VERSION = "proto.infoschemaversion";

    public static final String PROTO_ERR_UNKNOWN = "proto.err.unknown";
    public static final String PROTO_ERR_VALUE_LEN = "proto.err.valuelen";
    public static final String PROTO_ERR_EOF = "proto.err.eof";
    public static final String PROTO_ERR_NON_DIGIT_LEN = "proto.err.nondigitlen";
    public static final String PROTO_ERR_INVALID_SEPARATOR = "proto.err.invalidseparator";
    public static final String PROTO_ERR_VALUE_TOO_SHORT = "proto.err.valuetooshort";
    public static final String PROTO_ERR_NUMERIC_VALUE_OVERFLOW = "proto.err.numericvalueoverflow";
    public static final String PROTO_ERR_INVALID_HEADER = "proto.err.invalidheader";
    public static final String PROTO_ERR_BUFFER_ERROR = "proto.err.buffererror";
    public static final String PROTO_ERR_SINGLE_KEY = "proto.err.singlekey";

    // server metrics
    public static final String SERVER_START = "server.start";
    public static final String SERVER_UPTIME = "server.uptime";

    // storage metrics
    public static final String STORAGE_TTL_EXPIRED_EVICTION = "storage.ttl.expired.eviction";

    // thread pool metrics
    public static final String TP_WORKERS_MAX = ".workers_max";
    public static final String TP_WORKERS_ACTIVE = ".workers_active";
    public static final String TP_QUEUE_SIZE = ".queue_size";
    public static final String TP_TASK_ENQUEUED = ".task_enqueued";
    public static final String TP_TASK_COMPLETED = ".task_completed";
    public static final String TP_TASK_REJECTED = ".task_rejected";

    // backpressure metrics
    public static final String BP_PAUSE_COUNT = ".bp.pause_count";
    public static final String BP_PAUSED_COUNT = ".bp.paused_count";
}
