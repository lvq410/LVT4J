package com.lvt4j.basic;

import java.io.PrintWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * 数据库轻量框架<br>
 * 目前只在mysql上做过测试<br>
 * <strong>examples:</strong><br>
 * <pre>
 * <label>//注意Model声明于类中必须是static的</label>
 * static class BaseModel {
 *     Integer id;
 * }
 * @Table("model")
 * static class Model extends BaseModel{
 *     @Col("col")
 *     String alia;
 * }
 * <label>//清表</label>
 * db.executeSQL("delete from model").execute();
 * <label>//插入</label>
 * Model model1 = new Model();
 * model1.alia = "model1";
 * db.insert(model1).execute();
 * <label>//批量插入</label>
 * Model model2 = new Model();
 * model1.alia = "model2";
 * Model model3 = new Model();
 * model1.alia = "model3";
 * db.insert(model2).insert(model3).execute();
 * <label>//查询数量</label>
 * int num = db.select("select count(id) from model").execute2BasicOne(int.class);
 * <label>//查询是否存在</label>
 * boolean exist = db.select("select count(id)<>0 from model where id=?", 1).execute2BasicOne(boolean.class);
 * <label>//查询单列单数据</label>
 * String alia = db.select("select alia from model where id=?", 1).execute2BasicOne(String.class);
 * <label>//查询单列多数据</label>
 * List&lt;String&gt; alias = db.select("select alia from model").execute2Basic(String.class);
 * <label>//查询多列以model形式返回(model可以用'*')</label>
 * List&lt;Model&gt; models1 = db.select("select * from model").execute2Model(Model.class);
 * List&lt;Model&gt; models2 = db.select("select id,col from model").execute2Model(Model.class);
 * <label>//查询一个model(model可以用'*')</label>
 * Model modelRst1 = db.select("select * from model").execute2ModelOne(Model.class);
 * Model modelRst2 = db.select("select id,col from model").execute2ModelOne(Model.class);
 * <label>//查询多列以map形式返回(map不能用'*',且声明的结果类型数必须与sql脚本里列数量及类型对应)</label>
 * List&lt;Map&lt;String, Object&gt;&gt; modelAsMaps = db.select("select id,col from model").execute2Map(Integer.class, String.class);
 * <label>//查询一个map(map不能用'*',且声明的结果类型数必须与sql脚本里列数量及类型对应)</label>
 * Map&lt;String, Object&gt; modelAsMap = db.select("select id,col from model").execute2MapOne(Integer.class, String.class);
 * </pre>
 * <strong>支持映射的java类型包括:</strong><br>
 * java的基本类及其装箱 java.util.Date String byte[] Byte[] BigDecimal Calendar<br>
 * <strong>tips:</strong><br>
 * <strong>一些数据库类型及其driver:</strong><br>
 * ⊙h2:org.h2.Driver<br>
 * ⊙sqlite:org.sqlite.JDBC<br>
 * ⊙mysql:com.mysql.jdbc.Driver/com.mysql.cj.jdbc.Driver<br>
 * <strong>一般jdbc的url格式</strong><br>
 * jdbc:[type]://[host]:[port]/[database]?[query]
 * <strong>一些jdbc的url的query的参数</strong><br>
 * ⊙user:数据库用户名<br>
 * ⊙password:用户密码<br>
 * ⊙useUnicode[false]:是否使用Unicode字符集,如果参数characterEncoding设置为gb2312或gbk,本参数值必须设置为true<br>
 * ⊙characterEncoding:当useUnicode设置为true时,指定字符编码.比如可设置为gb2312或gbk<br>
 * ⊙autoReconnect[false]:当数据库连接异常中断时,是否自动重新连接<br>
 * ⊙autoReconnectForPools[false]:是否使用针对数据库连接池的重连策略<br>
 * ⊙failOverReadOnly[true]:自动重连成功后,连接是否设置为只读<br>
 * ⊙maxReconnects[3]:autoReconnect设置为true时,重试连接的次数<br>
 * ⊙initialTimeout[2]:autoReconnect设置为true时,两次重连之间的时间间隔,单位:S<br>
 * ⊙connectTimeout[0]:和数据库服务器建立socket连接时的超时,单位：毫秒.0表示永不超时,适用于JDK14及更高版本<br>
 * ⊙socketTimeout[0]:socket操作（读写）超时,单位：毫秒.0表示永不超时<br>
 * ⊙useLegacyDatetimeCode:false<br>
 * ⊙serverTimezone:Asia/Shanghai<br>
 * @author LV
 */
public class TDB {
    
    /**
     * 声明于一个映射到数据库表的类上<br>
     * 用于指明该类对应的数据库表的表名<br>
     * 若无此注解,则表名为类的类名
     * @author LV
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public static @interface Table {
        String value() default "";
    }
    
    /**
     * 声明于一个映射到数据库表列的属性上<br>
     * 用于指明该属性对应数据库表列的列名<br>
     * 若无此注解,则属性名即为列名
     * @author LV
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface Col {
        String value() default "";
    }
    
    private static String tblName(Class<?> cls) {
        Table table = cls.getAnnotation(Table.class);
        if(table==null || TVerify.strNullOrEmpty(table.value()))
            return cls.getSimpleName();
        return table.value();
    }
    
    private static boolean isCol(Field field) {
        int modifiers = field.getModifiers();
        return !Modifier.isStatic(modifiers) &&
                !Modifier.isFinal(modifiers) &&
                !Modifier.isTransient(modifiers);
    }

    private static String colName(Field field) {
        Col col = field.getAnnotation(Col.class);
        if(col==null || TVerify.strNullOrEmpty(col.value()))
            return field.getName();
        return col.value();
    }
    
    /** 判断javaType对应的jdbcType */
    public static int colType(Class<?> cls) {
        if (cls == byte.class || cls == Byte.class) {
            return Types.TINYINT;
        } else if (cls == int.class || cls == Integer.class) {
            return Types.INTEGER;
        } else if (cls == String.class) {
            return Types.VARCHAR;
        } else if (cls == boolean.class || cls == Boolean.class) {
            return Types.BIT;
        } else if (cls == Date.class || cls == Calendar.class) {
            return Types.TIMESTAMP;
        } else if (cls == byte[].class || cls == Byte[].class) {
            return Types.BLOB;
        } else if (cls == short.class || cls == Short.class) {
            return Types.SMALLINT;
        } else if (cls == long.class || cls == Long.class) {
            return Types.BIGINT;
        } else if (cls == float.class || cls == Float.class) {
            return Types.REAL;
        } else if (cls == double.class || cls == Double.class) {
            return Types.DOUBLE;
        } else if (cls == BigDecimal.class) {
            return Types.DECIMAL;
        } else if (cls == char.class || cls == Character.class) {
            return Types.CHAR;
        } else {
            throw new RuntimeException("不支持的java类型<" + cls + ">");
        }
    }
    /** 判断javaType对应的jdbcType的名 */
    public static String colType2Str(Class<?> cls) {
        if (cls == byte.class || cls == Byte.class) {
            return "TINYINT";
        } else if (cls == int.class || cls == Integer.class) {
            return "INTEGER";
        } else if (cls == String.class) {
            return "VARCHAR";
        } else if (cls == boolean.class || cls == Boolean.class) {
            return "BIT";
        } else if (cls == Date.class || cls == Calendar.class) {
            return "TIMESTAMP";
        } else if (cls == byte[].class || cls == Byte[].class) {
            return "BLOB";
        } else if (cls == short.class || cls == Short.class) {
            return "SMALLINT";
        } else if (cls == long.class || cls == Long.class) {
            return "BIGINT";
        } else if (cls == float.class || cls == Float.class) {
            return "REAL";
        } else if (cls == double.class || cls == Double.class) {
            return "DOUBLE";
        } else if (cls == BigDecimal.class) {
            return "DECIMAL";
        } else if (cls == char.class || cls == Character.class) {
            return "CHAR";
        } else {
            throw new RuntimeException("不支持的java类型<" + cls + ">");
        }
    }

    private static Object colData2JData(Class<?> jType, ResultSet query, int col) throws SQLException {
        if (jType == byte.class || jType == Byte.class) {
            return query.getByte(col);
        } else if (jType == int.class || jType == Integer.class) {
            return query.getInt(col);
        } else if (jType == boolean.class || jType == Boolean.class) {
            return (boolean) query.getBoolean(col);
        } else if (jType == String.class) {
            return query.getString(col);
        } else if (jType == Date.class) {
            return new Date(query.getTimestamp(col).getTime());
        } else if (jType == byte[].class || jType == Byte[].class) {
            return query.getBytes(col);
        } else if (jType == short.class || jType == Short.class) {
            return query.getShort(col);
        } else if (jType == long.class || jType == Long.class) {
            return query.getLong(col);
        } else if (jType == float.class || jType == Float.class) {
            return query.getFloat(col);
        } else if (jType == double.class || jType == Double.class) {
            return query.getDouble(col);
        } else if (jType == BigDecimal.class) {
            return query.getBigDecimal(col);
        } else if (jType == char.class || jType == Character.class) {
            return query.getString(col).charAt(0);
        } else if (jType == Calendar.class) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(query.getTimestamp(col).getTime());
            return calendar;
        } else {
            throw new RuntimeException("不支持的java类型<" + jType + ">");
        }
    }

    /**
     * 根据value的值类型不同,调用PreparedStatement的不同方法向其内赋值
     * 
     * @param values
     * @param key
     * @param value
     * @throws Exception
     */
    private static void setValues(PreparedStatement prep, Object... values) {
        if (values == null || values.length==0) return;
        int i = 1;
        for (Object value : values) setValue(prep, i++, value);
    }

    /**
     * 根据value的值类型不同,调用PreparedStatement的不同方法向其内赋值
     * 
     * @param prep
     * @param i
     * @param value
     * @throws Exception
     */
    private static void setValue(PreparedStatement prep, int i, Object value) {
        try {
            if (value == null)
                prep.setNull(i, Types.NULL);
            else if (value.getClass() == int.class
                    || value.getClass() == Integer.class)
                prep.setInt(i, (Integer) value);
            else if (value.getClass() == String.class)
                prep.setString(i, (String) value);
            else if (value.getClass() == boolean.class
                    || value.getClass() == Boolean.class)
                prep.setBoolean(i, (Boolean) value);
            else if (value.getClass() == Date.class)
                prep.setTimestamp(i, new Timestamp(((Date) value).getTime()));
            else if (value.getClass() == byte.class
                    || value.getClass() == Byte.class)
                prep.setByte(i, (Byte) value);
            else if (value.getClass() == byte[].class
                    || value.getClass() == Byte[].class)
                prep.setBytes(i, (byte[]) value);
            else if (value.getClass() == double.class
                    || value.getClass() == Double.class)
                prep.setDouble(i, (Double) value);
            else if (value.getClass() == float.class
                    || value.getClass() == Float.class)
                prep.setFloat(i, (Float) value);
            else if (value.getClass() == long.class
                    || value.getClass() == Long.class)
                prep.setLong(i, (Long) value);
            else if (value.getClass() == short.class
                    || value.getClass() == Short.class)
                prep.setShort(i, (Short) value);
            else if (value.getClass() == char.class
                    || value.getClass() == Character.class)
                prep.setString(i, String.valueOf(value));
            else if (value.getClass() == BigDecimal.class)
                prep.setBigDecimal(i, (BigDecimal) value);
            else if (value.getClass() == Calendar.class)
                prep.setTimestamp(i,
                    new Timestamp(((Calendar) value).getTimeInMillis()));
            else
                throw new RuntimeException("Not support type<" + value.getClass() + ">");
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static void clearEndComma(StringBuilder sql) {
        if (sql.charAt(sql.length() - 1) == ',')
            sql.deleteCharAt(sql.length() - 1);
    }

    private boolean printSQL;
    private DataSource dataSource;
    private ThreadLocal<Connection> curConn = new ThreadLocal<Connection>();
    private ThreadLocal<Transaction> curTransaction = new ThreadLocal<Transaction>();

    public TDB(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    /**
     * 会使用一个内部的最大只有5个连接的连接池<br>
     * 这个连接池不是很规范,建议使用{@link #TDB(DataSource)}
     * @param driverClassName
     * @param url
     * @param user
     * @param pwd
     */
    public TDB(String driverClassName, String url, String user, String pwd) {
        this.dataSource = new TDBDataSource(driverClassName, url, user, pwd);
    }
    
    /** 开启sql打印 */
    public void openPrintSQL() {
        printSQL = true;
    }
    private void pringSQL(String sql) {
        if (!printSQL) return;
        if (TLog.isInitialized()) {
            TLog.i("TDB:" + sql);
            return;
        }
        System.out.println(sql.toString());
    }
    
    /**
     * 若当前线程未打开事务,则释放连接
     * @throws Exception
     */
    private void releaseNoneTransactionConnection() {
        Connection conn = curConn.get();
        if(conn==null) return;
        Transaction transaction = curTransaction.get();
        if(transaction!=null) return;
        Throwable ex = null;
        try {
            conn.close();
        } catch (Throwable e) {
            ex = e;
        }
        curConn.remove();
        if(ex!=null) throw new RuntimeException(ex);
    }
    
    private Connection getConnection() {
        Connection conn = curConn.get();
        if(conn!=null) return conn;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(true);
            curConn.set(conn);
            return conn;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 在当前线程内开启一个事务<br>
     * 注意处理完后必须调用{@link #endTransaction}结束当前线程内的事务
     * @throws Exception
     */
    public void beginTransaction() {
        Transaction transaction = curTransaction.get();
        if(transaction!=null) return;
        try {
            transaction = new Transaction(getConnection());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        curTransaction.set(transaction);
    }
    
    /**
     * 回滚当前线程内事务
     * @throws Exception
     */
    public void rollbackTransaction() {
        Transaction transaction = curTransaction.get();
        if(transaction==null) return;
        try {
            transaction.conn.rollback();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 如果当前线程内有事务,则提交并结束<br>
     * 如果提交失败会尝试回滚并抛出异常<br>
     * 同时释放连接
     * @throws Exception
     */
    public void endTransaction() {
        Transaction transaction = curTransaction.get();
        if(transaction==null) return;
        TDBException ex = null;
        try {
            transaction.conn.commit();
        } catch (Throwable commitException) {
            if(ex==null) ex = new TDBException();
            ex.appendExcetion(commitException);
            try {
                transaction.conn.rollback();
            } catch (Throwable roolbackException) {
                ex.appendExcetion(roolbackException);
            }
        }
        curTransaction.remove();
        Connection conn = curConn.get();
        if(conn==null)
            throw new RuntimeException("连接异常：当前线程内连接丢失!");
        if(conn!=transaction.conn)
            throw new RuntimeException("连接异常：当前线程内连接与事务连接不一致！");
        try {
            conn.close();
        } catch (Throwable e) {
            if(ex==null) ex = new TDBException();
            ex.appendExcetion(e);
        }
        curConn.remove();
        if(ex!=null) throw ex;
    }

    public Insert insert() {
        return new Insert();
    }
    
    public Insert insert(Object obj) {
        return new Insert(obj);
    }

    public Select select(String sql, Object... args) {
        return new Select(sql, args);
    }
    
    public ExecSQL executeSQL(String sql, Object... args) {
        return new ExecSQL(sql, args);
    }

    public class Insert {
        private Class<?> modelCls;
        private List<Object> modelS = new ArrayList<Object>();
        
        private Insert() {}
        
        private Insert(Object model) {
            modelS.add(model);
            modelCls = model.getClass();
        }
        
        /** 批量插入必须保证多次调用该方法的参数的类一致 */
        public Insert insert(Object model) {
            if (modelCls!=null && modelCls!=model.getClass())
                throw new RuntimeException("Model class not match!"
                        + "For stroed class<"+modelCls+"> not equal <"+model.getClass()+">");
            modelS.add(model);
            if (modelCls==null) modelCls = model.getClass();
            return this;
        }
        
        public int size() {
            return modelS.size();
        }
        
        public void clear() {
            modelCls = null;
            modelS.clear();
        }
        
        public void execute() {
            if (modelCls==null)return;
            StringBuilder sql = new StringBuilder();
            List<Field> fields = new ArrayList<Field>();
            List<Object> valueS = new ArrayList<Object>();
            String table = tblName(modelCls);
            sql.append("insert into " + table + "(");
            for (Field field : modelCls.getDeclaredFields()) {
                if (!isCol(field))continue;
                field.setAccessible(true);
                fields.add(field);
                sql.append(colName(field)).append(',');
            }
            clearEndComma(sql);
            sql.append(") values ");
            
            for (Object model : modelS) {
                sql.append("(");
                for (Field field: fields) {
                    sql.append("?,");
                    try {
                        valueS.add(field.get(model));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                clearEndComma(sql);
                sql.append("),");
            }
            clearEndComma(sql);
            sql.append(";");
            String sqlStr = sql.toString();
            pringSQL(sqlStr);
            PreparedStatement prep = null;
            TDBException ex = null;
            try {
                prep = getConnection().prepareStatement(sqlStr);
                setValues(prep, valueS.toArray());
                prep.executeUpdate();
            } catch (Throwable e) {
                ex = new TDBException();
                ex.appendExcetion(e);
                throw ex;
            } finally {
                try {
                    if(prep!=null) prep.close();
                } catch (Throwable e) {
                    if(ex==null) ex = new TDBException();
                    ex.appendExcetion(e);
                }
                try {
                    releaseNoneTransactionConnection();
                } catch (Throwable e) {
                    if(ex==null) ex = new TDBException();
                    ex.appendExcetion(e);
                }
                if(ex!=null) throw ex;
            }
        }
    }

    public class Select {
        private StringBuilder sql;
        private Object[] argS;
        
        private Select(String sql, Object[] argS) {
            this.sql = new StringBuilder(sql);
            this.argS = argS;
        }
        
        
        /**
         * 查询结果model对象列表<br>
         * 注意model必须是全局可见的
         * @param modelCls
         * @return
         * @throws SQLException 
         */
        public <E> List<E> execute2Model(Class<E> modelCls) {
            Map<String, Field> fieldS = new HashMap<String, Field>();
            for (Field field : TReflect.allField(modelCls)) {
                if (!isCol(field))continue;
                field.setAccessible(true);
                fieldS.put(colName(field), field);
            }
            List<E> rst = new ArrayList<E>();
            String sqlStr = sql.toString();
            pringSQL(sqlStr);
            PreparedStatement prep = null;
            ResultSet query = null;
            TDBException ex = null;
            try {
                prep = getConnection().prepareStatement(sqlStr);
                setValues(prep, argS);
                query = prep.executeQuery();
                ResultSetMetaData metaData = query.getMetaData();
                int colCount = metaData.getColumnCount();
                String[] colS = new String[colCount];
                for (int i = 0; i < colCount; i++) {
                    colS[i] = metaData.getColumnLabel(i+1);
                }
                while (query.next()) {
                    E obj = TReflect.newInstance(modelCls);
                    for (int i = 0; i < colS.length; i++) {
                        String col = colS[i];
                        Field field = fieldS.get(col);
                        if(field==null) continue;
                        field.set(obj, colData2JData(field.getType(), query, i+1));
                    }
                    rst.add(obj);
                }
                return rst;
            } catch (Throwable e) {
                ex = new TDBException();
                ex.appendExcetion(e);
                throw ex;
            } finally {
                try {
                    if(query!=null) query.close();
                } catch (Throwable e) {
                    if(ex==null) ex = new TDBException();
                    ex.appendExcetion(e);
                }
                try {
                    if(prep!=null) prep.close();
                } catch (Throwable e) {
                    if(ex==null) ex = new TDBException();
                    ex.appendExcetion(e);
                }
                try {
                    releaseNoneTransactionConnection();
                } catch (Throwable e) {
                    if(ex==null) ex = new TDBException();
                    ex.appendExcetion(e);
                }
                if(ex!=null) throw ex;
            }
        }
        
        /**
         * 查询为一个指定model对象<br>
         * 注意model必须是全局可见的
         * @param modelCls
         * @return 查询结果为空时返回null
         */
        public <E> E execute2ModelOne(Class<E> modelCls) {
            PreparedStatement prep = null;
            ResultSet query = null;
            TDBException ex = null;
            try {
                prep = getConnection().prepareStatement(sql.toString());
                setValues(prep, argS);
                query = prep.executeQuery();
                if(!query.next()) return null;
                Map<String, Field> fieldS = new HashMap<String, Field>();
                for (Field field : TReflect.allField(modelCls)) {
                    if (!isCol(field))continue;
                    field.setAccessible(true);
                    fieldS.put(colName(field), field);
                }
                ResultSetMetaData metaData = query.getMetaData();
                int colCount = metaData.getColumnCount();
                String[] colS = new String[colCount];
                for (int i = 0; i < colCount; i++) {
                    colS[i] = metaData.getColumnLabel(i+1);
                }
                E obj = TReflect.newInstance(modelCls);
                for (int i = 0; i < colS.length; i++) {
                    String col = colS[i];
                    Field field = fieldS.get(col);
                    if(field==null) continue;
                    field.set(obj, colData2JData(field.getType(), query, i+1));
                }
                return obj;
            } catch (Throwable e) {
                ex = new TDBException();
                ex.appendExcetion(e);
                throw ex;
            } finally {
                try {
                    if(query!=null) query.close();
                } catch (Throwable e) {
                    if(ex==null) ex = new TDBException();
                    ex.appendExcetion(e);
                }
                try {
                    if(prep!=null) prep.close();
                } catch (Throwable e) {
                    if(ex==null) ex = new TDBException();
                    ex.appendExcetion(e);
                }
                try {
                    releaseNoneTransactionConnection();
                } catch (Throwable e) {
                    if(ex==null) ex = new TDBException();
                    ex.appendExcetion(e);
                }
                if(ex!=null) throw ex;
            }
        }

        /**
         * 只查询一个列时,需要查询结果为多个单值
         * @param basic
         * @return
         */
        @SuppressWarnings("unchecked")
        public <E> List<E> execute2Basic(Class<E> basic) {
            List<E> rst = new ArrayList<E>();
            String sqlStr = sql.toString();
            pringSQL(sqlStr);
            PreparedStatement prep = null;
            ResultSet query = null;
            TDBException ex = null;
            try {
                prep = getConnection().prepareStatement(sqlStr);
                setValues(prep, argS);
                query = prep.executeQuery();
                while (query.next()) {
                    rst.add((E) colData2JData(basic, query, 1));
                }
                return rst;
            } catch (Throwable e) {
                ex = new TDBException();
                ex.appendExcetion(e);
                throw ex;
            } finally {
                try {
                    if(query!=null) query.close();
                } catch (Throwable e) {
                    if(ex==null) ex = new TDBException();
                    ex.appendExcetion(e);
                }
                try {
                    if(prep!=null) prep.close();
                } catch (Throwable e) {
                    if(ex==null) ex = new TDBException();
                    ex.appendExcetion(e);
                }
                try {
                    releaseNoneTransactionConnection();
                } catch (Throwable e) {
                    if(ex==null) ex = new TDBException();
                    ex.appendExcetion(e);
                }
                if(ex!=null) throw ex;
            }
        }
        
        /**
         * 只查询一个列时,需要查询结果为单值
         * @param basic
         * @return
         */
        @SuppressWarnings("unchecked")
        public <E> E execute2BasicOne(Class<E> basic) {
            String sqlStr = sql.toString();
            pringSQL(sqlStr);
            PreparedStatement prep = null;
            ResultSet query = null;
            TDBException ex = null;
            try {
                prep = getConnection().prepareStatement(sqlStr);
                setValues(prep, argS);
                query = prep.executeQuery();
                if(!query.next()) return null;
                return (E) colData2JData(basic, query, 1);
            } catch (Throwable e) {
                ex = new TDBException();
                ex.appendExcetion(e);
                throw ex;
            } finally {
                try {
                    if(query!=null) query.close();
                } catch (Throwable e) {
                    if(ex==null) ex = new TDBException();
                    ex.appendExcetion(e);
                }
                try {
                    if(prep!=null) prep.close();
                } catch (Throwable e) {
                    if(ex==null) ex = new TDBException();
                    ex.appendExcetion(e);
                }
                try {
                    releaseNoneTransactionConnection();
                } catch (Throwable e) {
                    if(ex==null) ex = new TDBException();
                    ex.appendExcetion(e);
                }
                if(ex!=null) throw ex;
            }
        }
        
        /**
         * 查询结果为map
         * @param clsS
         * @return
         */
        public List<Map<String, Object>> execute2Map(Class<?>... clsS) {
            List<Map<String, Object>> rst = new ArrayList<Map<String, Object>>();
            String sqlStr = sql.toString();
            pringSQL(sqlStr);
            PreparedStatement prep = null;
            ResultSet query = null;
            TDBException ex = null;
            try {
                prep = getConnection().prepareStatement(sqlStr);
                setValues(prep, argS);
                query = prep.executeQuery();
                ResultSetMetaData metaData = query.getMetaData();
                int colCount = metaData.getColumnCount();
                if(clsS.length!=colCount)
                    throw new RuntimeException("参数类列表的数量["+clsS.length+"]"
                            + "与sql查询结果的列的数量["+colCount+"]不等!");
                String[] keys = new String[metaData.getColumnCount()];
                for (int i = 0; i < colCount; i++) {
                    keys[i] = metaData.getColumnLabel(i+1);
                }
                while (query.next()) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    for (int i = 0; i < colCount; i++) {
                        map.put(keys[i], colData2JData(clsS[i], query, i+1));
                    }
                    rst.add(map);
                }
                return rst;
            } catch (Throwable e) {
                ex = new TDBException();
                ex.appendExcetion(e);
                throw ex;
            } finally {
                try {
                    if(query!=null) query.close();
                } catch (Throwable e) {
                    if(ex==null) ex = new TDBException();
                    ex.appendExcetion(e);
                }
                try {
                    if(prep!=null) prep.close();
                } catch (Throwable e) {
                    if(ex==null) ex = new TDBException();
                    ex.appendExcetion(e);
                }
                try {
                    releaseNoneTransactionConnection();
                } catch (Throwable e) {
                    if(ex==null) ex = new TDBException();
                    ex.appendExcetion(e);
                }
                if(ex!=null) throw ex;
            }
        }
        
        /**
         * 查询结果为map,且只查询一个结果
         * @param clsS
         * @return
         */
        public Map<String, Object> execute2MapOne(Class<?>... clsS) {
            String sqlStr = sql.toString();
            pringSQL(sqlStr);
            PreparedStatement prep = null;
            ResultSet query = null;
            TDBException ex = null;
            try {
                prep = getConnection().prepareStatement(sqlStr);
                setValues(prep, argS);
                query = prep.executeQuery();
                ResultSetMetaData metaData = query.getMetaData();
                int colCount = metaData.getColumnCount();
                if(clsS.length!=colCount)
                    throw new RuntimeException("参数类列表的数量["+clsS.length+"]"
                            + "与sql查询结果的列的数量["+colCount+"]不等!");
                String[] keys = new String[metaData.getColumnCount()];
                for (int i = 0; i < colCount; i++) {
                    keys[i] = metaData.getColumnLabel(i+1);
                }
                if(!query.next()) return null;
                Map<String, Object> map = new HashMap<String, Object>();
                for (int i = 0; i < colCount; i++) {
                    map.put(keys[i], colData2JData(clsS[i], query, i+1));
                }
                return map;
            } catch (Throwable e) {
                ex = new TDBException();
                ex.appendExcetion(e);
                throw ex;
            } finally {
                try {
                    if(query!=null) query.close();
                } catch (Throwable e) {
                    if(ex==null) ex = new TDBException();
                    ex.appendExcetion(e);
                }
                try {
                    if(prep!=null) prep.close();
                } catch (Throwable e) {
                    if(ex==null) ex = new TDBException();
                    ex.appendExcetion(e);
                }
                try {
                    releaseNoneTransactionConnection();
                } catch (Throwable e) {
                    if(ex==null) ex = new TDBException();
                    ex.appendExcetion(e);
                }
                if(ex!=null) throw ex;
            }
        }
    }

    public class ExecSQL{
        
        private String sql;
        private Object[] argS;
        
        private ExecSQL(String sql, Object[] argS) {
            this.sql = sql;
            this.argS = argS;
        }

        public void execute() {
            pringSQL(sql);
            PreparedStatement prep = null;
            TDBException ex = null;
            try {
                prep = getConnection().prepareStatement(sql);
                setValues(prep, argS);
                prep.execute();
                prep.close();
            } catch (Throwable e) {
                ex = new TDBException();
                ex.appendExcetion(e);
                throw ex;
            } finally {
                try {
                    if(prep!=null) prep.close();
                } catch (Throwable e) {
                    if(ex==null) ex = new TDBException();
                    ex.appendExcetion(e);
                }
                try {
                    releaseNoneTransactionConnection();
                } catch (Throwable e) {
                    if(ex==null) ex = new TDBException();
                    ex.appendExcetion(e);
                }
                if(ex!=null) throw ex;
            }
        }
        
    }

    /** 简单数据源,最大只支持5个链接 */
    private class TDBDataSource implements DataSource {

        /** 最大连接数量 */
        private static final int MaxConnNum = 5;
        
        private String url;
        private String user;
        private String pwd;
        /** 已创建的连接的连接池 */
        ConcurrentLinkedQueue<Connection> allConns = new ConcurrentLinkedQueue<Connection>();
        /** 空闲连接池 */
        ConcurrentLinkedQueue<Connection> freeConns = new ConcurrentLinkedQueue<Connection>();
        
        private TDBDataSource(String driverClassName, String url, String user, String pwd) {
            try {
                Class.forName(driverClassName);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            this.url = url;
            this.user = user;
            this.pwd = pwd;
        }
        
        @Override
        public Connection getConnection() throws SQLException {
            Connection conn = freeConns.poll();
            if(conn!=null) return conn;
            conn = makeConnection();
            if(conn!=null) return conn;
            while (conn==null) {
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException ignore) {}
                    conn = freeConns.poll();
                }
            }
            return conn;
        }
        
        private Connection makeConnection() throws SQLException {
            if(allConns.size()>=MaxConnNum) return null;
            synchronized (allConns) {
                if(allConns.size()>=MaxConnNum) return null;
                Connection innerConn = TVerify.strNullOrEmpty(user)?
                        DriverManager.getConnection(url)
                        :DriverManager.getConnection(url, user, pwd);
                TDBDataSourceConnection conn = new TDBDataSourceConnection(this, innerConn);
                allConns.add(conn);
                return conn;
            }
        }
        
        private void releaseConnection(Connection conn) {
            freeConns.add(conn);
            synchronized (this) {
                notify();
            }
        }
        
        @Override public PrintWriter getLogWriter() throws SQLException { return null; }
        @Override public void setLogWriter(PrintWriter out) throws SQLException { }
        @Override public void setLoginTimeout(int seconds) throws SQLException { }
        @Override public int getLoginTimeout() throws SQLException { return 0; }
        @Override public Logger getParentLogger() throws SQLFeatureNotSupportedException { return null; }
        @Override public <T> T unwrap(Class<T> iface) throws SQLException { return null; }
        @Override public boolean isWrapperFor(Class<?> iface) throws SQLException { return false; }
        @Override public Connection getConnection(String username, String password) throws SQLException { return null; }
        
        private class TDBDataSourceConnection implements Connection {
            
            TDBDataSource dataSource;
            Connection conn;

            private TDBDataSourceConnection(TDBDataSource dataSource, Connection conn) {
                this.dataSource = dataSource;
                this.conn = conn;
            }
            
            @Override
            public void close() throws SQLException {
                dataSource.releaseConnection(this);
            }
            
            @Override public <T> T unwrap(Class<T> iface) throws SQLException { return conn.unwrap(iface); }
            @Override public boolean isWrapperFor(Class<?> iface) throws SQLException { return conn.isWrapperFor(iface); }
            @Override public Statement createStatement() throws SQLException { return conn.createStatement(); }
            @Override public PreparedStatement prepareStatement(String sql) throws SQLException { return conn.prepareStatement(sql); }
            @Override public CallableStatement prepareCall(String sql) throws SQLException { return conn.prepareCall(sql); }
            @Override public String nativeSQL(String sql) throws SQLException { return conn.nativeSQL(sql); }
            @Override public void setAutoCommit(boolean autoCommit) throws SQLException { conn.setAutoCommit(autoCommit); }
            @Override public boolean getAutoCommit() throws SQLException { return conn.getAutoCommit(); }
            @Override public void commit() throws SQLException { conn.commit(); }
            @Override public void rollback() throws SQLException { conn.rollback(); }
            @Override public boolean isClosed() throws SQLException { return conn.isClosed(); }
            @Override public DatabaseMetaData getMetaData() throws SQLException { return conn.getMetaData(); }
            @Override public void setReadOnly(boolean readOnly) throws SQLException { conn.setReadOnly(readOnly); }
            @Override public boolean isReadOnly() throws SQLException { return conn.isReadOnly(); }
            @Override public void setCatalog(String catalog) throws SQLException { conn.setCatalog(catalog); }
            @Override public String getCatalog() throws SQLException { return conn.getCatalog(); }
            @Override public void setTransactionIsolation(int level) throws SQLException { conn.setTransactionIsolation(level); }
            @Override public int getTransactionIsolation() throws SQLException { return conn.getTransactionIsolation(); }
            @Override public SQLWarning getWarnings() throws SQLException { return conn.getWarnings(); }
            @Override public void clearWarnings() throws SQLException { conn.clearWarnings(); }
            @Override public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException { return conn.createStatement(resultSetType, resultSetConcurrency); }
            @Override public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException { return conn.prepareStatement(sql, resultSetType, resultSetConcurrency); }
            @Override public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException { return conn.prepareCall(sql, resultSetType, resultSetConcurrency); }
            @Override public Map<String, Class<?>> getTypeMap() throws SQLException { return conn.getTypeMap(); }
            @Override public void setTypeMap(Map<String, Class<?>> map) throws SQLException { conn.setTypeMap(map); }
            @Override public void setHoldability(int holdability) throws SQLException { conn.setHoldability(holdability); }
            @Override public int getHoldability() throws SQLException { return conn.getHoldability(); }
            @Override public Savepoint setSavepoint() throws SQLException { return conn.setSavepoint(); }
            @Override public Savepoint setSavepoint(String name) throws SQLException { return conn.setSavepoint(name); }
            @Override public void rollback(Savepoint savepoint) throws SQLException { conn.rollback(savepoint); }
            @Override public void releaseSavepoint(Savepoint savepoint) throws SQLException { conn.releaseSavepoint(savepoint); }
            @Override public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException { return conn.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability); }
            @Override public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException { return conn.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability); }
            @Override public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException { return conn.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability); }
            @Override public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException { return conn.prepareStatement(sql, autoGeneratedKeys); }
            @Override public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException { return conn.prepareStatement(sql, columnIndexes); }
            @Override public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException { return conn.prepareStatement(sql, columnNames); }
            @Override public Clob createClob() throws SQLException { return conn.createClob(); }
            @Override public Blob createBlob() throws SQLException { return conn.createBlob(); }
            @Override public NClob createNClob() throws SQLException { return conn.createNClob(); }
            @Override public SQLXML createSQLXML() throws SQLException { return conn.createSQLXML(); }
            @Override public boolean isValid(int timeout) throws SQLException { return conn.isValid(timeout); }
            @Override public void setClientInfo(String name, String value) throws SQLClientInfoException { conn.setClientInfo(name, value); }
            @Override public void setClientInfo(Properties properties) throws SQLClientInfoException { conn.setClientInfo(properties); }
            @Override public String getClientInfo(String name) throws SQLException { return conn.getClientInfo(name); }
            @Override public Properties getClientInfo() throws SQLException { return conn.getClientInfo(); }
            @Override public Array createArrayOf(String typeName, Object[] elements) throws SQLException { return conn.createArrayOf(typeName, elements); }
            @Override public Struct createStruct(String typeName, Object[] attributes) throws SQLException { return conn.createStruct(typeName, attributes); }
            @Override public void setSchema(String schema) throws SQLException { conn.setSchema(schema); }
            @Override public String getSchema() throws SQLException { return conn.getSchema(); }
            @Override public void abort(Executor executor) throws SQLException { conn.abort(executor); }
            @Override public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException { conn.setNetworkTimeout(executor, milliseconds); }
            @Override public int getNetworkTimeout() throws SQLException { return conn.getNetworkTimeout(); }
        }
    }
    
    private class Transaction{
        
        private Connection conn;
        
        private Transaction(Connection conn) {
            this.conn = conn;
            try {
                conn.setAutoCommit(false);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        
    }
    
    private class TDBException extends RuntimeException {
        
        private static final long serialVersionUID = 1L;
        
        StringBuilder msg = new StringBuilder();
        
        private TDBException() {}
        
        @Override
        public String getMessage() {
            return msg.toString();
        }
        
        private void appendExcetion(Throwable e) {
            this.msg.append(TSys.printThrowStackTrace(e));
        }
    }
    
}
