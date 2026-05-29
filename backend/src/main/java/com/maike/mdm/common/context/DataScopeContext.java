package com.maike.mdm.common.context;

/**
 * 数据范围上下文，通过ThreadLocal传递数据范围SQL条件
 */
public class DataScopeContext {

    private static final ThreadLocal<String> SCOPE_SQL = new ThreadLocal<>();

    public static void setScopeSql(String sql) {
        SCOPE_SQL.set(sql);
    }

    public static String getScopeSql() {
        return SCOPE_SQL.get();
    }

    public static void clear() {
        SCOPE_SQL.remove();
    }
}
