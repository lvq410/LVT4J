package com.lvt4j.basic;

import java.io.InputStream;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
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
 * static class BaseModel{
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
public class TDB{
    
    private static final TDBTypeHandler<Boolean> booleanHandler = new TDBTypeHandler<Boolean>(){
        @Override public Class<Boolean> supportType(){return Boolean.class;}
        @Override public int jdbcType(){return Types.BIT;}
        @Override public String jdbcTypeName(){return "BIT";}
        @Override public void setParameter(PreparedStatement prep, int parameterIndex, Boolean value)throws SQLException{prep.setBoolean(parameterIndex, value);};
        @Override public Boolean getResult(ResultSet rs, int columnIndex)throws SQLException{return rs.getBoolean(columnIndex);}
    };
    private static final TDBTypeHandler<Boolean> BooleanHandler = new TDBTypeHandler<Boolean>(){
        @Override public Class<Boolean> supportType(){return Boolean.class;}
        @Override public int jdbcType(){return Types.BIT;}
        @Override public String jdbcTypeName(){return "BIT";}
        @Override public void setParameter(PreparedStatement prep, int parameterIndex, Boolean value)throws SQLException{prep.setBoolean(parameterIndex, value);};
        @Override public Boolean getResult(ResultSet rs, int columnIndex)throws SQLException{
            boolean value = rs.getBoolean(columnIndex);
            if(rs.wasNull()) return null;
            return value;
        }
    };
    private static final TDBTypeHandler<Byte> byteHandler = new TDBTypeHandler<Byte>(){
        @Override public Class<Byte> supportType(){return Byte.class;}
        @Override public int jdbcType(){return Types.TINYINT;}
        @Override public String jdbcTypeName(){return "TINYINT";}
        @Override public void setParameter(PreparedStatement prep, int parameterIndex, Byte value)throws SQLException{prep.setByte(parameterIndex, value);};
        @Override public Byte getResult(ResultSet rs, int columnIndex)throws SQLException{return rs.getByte(columnIndex);}
    };
    private static final TDBTypeHandler<Byte> ByteHandler = new TDBTypeHandler<Byte>(){
        @Override public Class<Byte> supportType(){return Byte.class;}
        @Override public int jdbcType(){return Types.TINYINT;}
        @Override public String jdbcTypeName(){return "TINYINT";}
        @Override public void setParameter(PreparedStatement prep, int parameterIndex, Byte value)throws SQLException{prep.setByte(parameterIndex, value);};
        @Override public Byte getResult(ResultSet rs, int columnIndex)throws SQLException{
            byte value = rs.getByte(columnIndex);
            if(rs.wasNull()) return null;
            return value;
        }
    };
    private static final TDBTypeHandler<Short> shortHandler = new TDBTypeHandler<Short>(){
        @Override public Class<Short> supportType(){return Short.class;}
        @Override public int jdbcType(){return Types.SMALLINT;}
        @Override public String jdbcTypeName(){return "SMALLINT";}
        @Override public void setParameter(PreparedStatement prep, int parameterIndex, Short value)throws SQLException{prep.setShort(parameterIndex, value);};
        @Override public Short getResult(ResultSet rs, int columnIndex)throws SQLException{return rs.getShort(columnIndex);}
    };
    private static final TDBTypeHandler<Short> ShortHandler = new TDBTypeHandler<Short>(){
        @Override public Class<Short> supportType(){return Short.class;}
        @Override public int jdbcType(){return Types.SMALLINT;}
        @Override public String jdbcTypeName(){return "SMALLINT";}
        @Override public void setParameter(PreparedStatement prep, int parameterIndex, Short value)throws SQLException{prep.setShort(parameterIndex, value);};
        @Override public Short getResult(ResultSet rs, int columnIndex)throws SQLException{
            short value = rs.getShort(columnIndex);
            if(rs.wasNull()) return null;
            return value;
        }
    };
    private static final TDBTypeHandler<Integer> intHandler = new TDBTypeHandler<Integer>(){
        @Override public Class<Integer> supportType(){return Integer.class;}
        @Override public int jdbcType(){return Types.INTEGER;}
        @Override public String jdbcTypeName(){return "INTEGER";}
        @Override public void setParameter(PreparedStatement prep, int parameterIndex, Integer value)throws SQLException{prep.setInt(parameterIndex, value);};
        @Override public Integer getResult(ResultSet rs, int columnIndex)throws SQLException{return rs.getInt(columnIndex);}
    };
    private static final TDBTypeHandler<Integer> IntegerHandler = new TDBTypeHandler<Integer>(){
        @Override public Class<Integer> supportType(){return Integer.class;}
        @Override public int jdbcType(){return Types.INTEGER;}
        @Override public String jdbcTypeName(){return "INTEGER";}
        @Override public void setParameter(PreparedStatement prep, int parameterIndex, Integer value)throws SQLException{prep.setInt(parameterIndex, value);};
        @Override public Integer getResult(ResultSet rs, int columnIndex)throws SQLException{
            int value = rs.getInt(columnIndex);
            if(rs.wasNull()) return null;
            return value;
        }
    };
    private static final TDBTypeHandler<Long> longHandler = new TDBTypeHandler<Long>(){
        @Override public Class<Long> supportType(){return Long.class;}
        @Override public int jdbcType(){return Types.BIGINT;}
        @Override public String jdbcTypeName(){return "BIGINT";}
        @Override public void setParameter(PreparedStatement prep, int parameterIndex, Long value)throws SQLException{prep.setLong(parameterIndex, value);};
        @Override public Long getResult(ResultSet rs, int columnIndex)throws SQLException{return rs.getLong(columnIndex);}
    };
    private static final TDBTypeHandler<Long> LongHandler = new TDBTypeHandler<Long>(){
        @Override public Class<Long> supportType(){return Long.class;}
        @Override public int jdbcType(){return Types.BIGINT;}
        @Override public String jdbcTypeName(){return "BIGINT";}
        @Override public void setParameter(PreparedStatement prep, int parameterIndex, Long value)throws SQLException{prep.setLong(parameterIndex, value);};
        @Override public Long getResult(ResultSet rs, int columnIndex)throws SQLException{
            long value = rs.getLong(columnIndex);
            if(rs.wasNull()) return null;
            return value;
        }
    };
    private static final TDBTypeHandler<Float> floatHandler = new TDBTypeHandler<Float>(){
        @Override public Class<Float> supportType(){return Float.class;}
        @Override public int jdbcType(){return Types.REAL;}
        @Override public String jdbcTypeName(){return "REAL";}
        @Override public void setParameter(PreparedStatement prep, int parameterIndex, Float value)throws SQLException{prep.setFloat(parameterIndex, value);};
        @Override public Float getResult(ResultSet rs, int columnIndex)throws SQLException{return rs.getFloat(columnIndex);}
    };
    private static final TDBTypeHandler<Float> FloatHandler = new TDBTypeHandler<Float>(){
        @Override public Class<Float> supportType(){return Float.class;}
        @Override public int jdbcType(){return Types.REAL;}
        @Override public String jdbcTypeName(){return "REAL";}
        @Override public void setParameter(PreparedStatement prep, int parameterIndex, Float value)throws SQLException{prep.setFloat(parameterIndex, value);};
        @Override public Float getResult(ResultSet rs, int columnIndex)throws SQLException{
            float value = rs.getFloat(columnIndex);
            if(rs.wasNull()) return null;
            return value;
        }
    };
    private static final TDBTypeHandler<Double> doubleHandler = new TDBTypeHandler<Double>(){
        @Override public Class<Double> supportType(){return Double.class;}
        @Override public int jdbcType(){return Types.DOUBLE;}
        @Override public String jdbcTypeName(){return "DOUBLE";}
        @Override public void setParameter(PreparedStatement prep, int parameterIndex, Double value)throws SQLException{prep.setDouble(parameterIndex, value);};
        @Override public Double getResult(ResultSet rs, int columnIndex)throws SQLException{return rs.getDouble(columnIndex);}
    };
    private static final TDBTypeHandler<Double> DoubleHandler = new TDBTypeHandler<Double>(){
        @Override public Class<Double> supportType(){return Double.class;}
        @Override public int jdbcType(){return Types.DOUBLE;}
        @Override public String jdbcTypeName(){return "DOUBLE";}
        @Override public void setParameter(PreparedStatement prep, int parameterIndex, Double value)throws SQLException{prep.setDouble(parameterIndex, value);};
        @Override public Double getResult(ResultSet rs, int columnIndex)throws SQLException{
            double value = rs.getDouble(columnIndex);
            if(rs.wasNull()) return null;
            return value;
        }
    };
    private static final TDBTypeHandler<Character> charHandler = new TDBTypeHandler<Character>(){
        @Override public Class<Character> supportType(){return Character.class;}
        @Override public int jdbcType(){return Types.CHAR;}
        @Override public String jdbcTypeName(){return "CHAR";}
        @Override public void setParameter(PreparedStatement prep, int parameterIndex, Character value)throws SQLException{prep.setInt(parameterIndex, value);};
        @Override public Character getResult(ResultSet rs, int columnIndex)throws SQLException{return (char)rs.getInt(columnIndex);}
    };
    private static final TDBTypeHandler<Character> CharacterHandler = new TDBTypeHandler<Character>(){
        @Override public Class<Character> supportType(){return Character.class;}
        @Override public int jdbcType(){return Types.CHAR;}
        @Override public String jdbcTypeName(){return "CHAR";}
        @Override public void setParameter(PreparedStatement prep, int parameterIndex, Character value)throws SQLException{prep.setInt(parameterIndex, value);};
        @Override public Character getResult(ResultSet rs, int columnIndex)throws SQLException{
            int value = rs.getInt(columnIndex);
            if(rs.wasNull()) return null;
            return (char)value;
        }
    };
    private static final TDBTypeHandler<String> StringHandler = new TDBTypeHandler<String>(){
        @Override public Class<String> supportType(){return String.class;}
        @Override public int jdbcType(){return Types.VARCHAR;}
        @Override public String jdbcTypeName(){return "VARCHAR";}
        @Override public void setParameter(PreparedStatement prep, int parameterIndex, String value)throws SQLException{prep.setString(parameterIndex, value);};
        @Override public String getResult(ResultSet rs, int columnIndex)throws SQLException{return rs.getString(columnIndex);}
    };
    private static final TDBTypeHandler<StringBuilder> StringbuilderHandler = new TDBTypeHandler<StringBuilder>(){
        @Override public Class<StringBuilder> supportType(){return StringBuilder.class;}
        @Override public int jdbcType(){return Types.VARCHAR;}
        @Override public String jdbcTypeName(){return "VARCHAR";}
        @Override public void setParameter(PreparedStatement prep, int parameterIndex, StringBuilder value)throws SQLException{prep.setString(parameterIndex, value.toString());};
        @Override public StringBuilder getResult(ResultSet rs, int columnIndex)throws SQLException{
            String value = rs.getString(columnIndex);
            if(value==null) return null;
            return new StringBuilder(value);
        }
    };
    private static final TDBTypeHandler<StringBuffer> StringbufferHandler = new TDBTypeHandler<StringBuffer>(){
        @Override public Class<StringBuffer> supportType(){return StringBuffer.class;}
        @Override public int jdbcType(){return Types.VARCHAR;}
        @Override public String jdbcTypeName(){return "VARCHAR";}
        @Override public void setParameter(PreparedStatement prep, int parameterIndex, StringBuffer value)throws SQLException{prep.setString(parameterIndex, value.toString());};
        @Override public StringBuffer getResult(ResultSet rs, int columnIndex)throws SQLException{
            String value = rs.getString(columnIndex);
            if(value==null) return null;
            return new StringBuffer(value);
        }
    };
    private static final TDBTypeHandler<Timestamp> TimestampHandler = new TDBTypeHandler<Timestamp>(){
        @Override public Class<Timestamp> supportType(){return Timestamp.class;}
        @Override public int jdbcType(){return Types.TIMESTAMP;}
        @Override public String jdbcTypeName(){return "TIMESTAMP";}
        @Override public void setParameter(PreparedStatement prep, int parameterIndex, Timestamp value)throws SQLException{prep.setTimestamp(parameterIndex, value);};
        @Override public Timestamp getResult(ResultSet rs, int columnIndex)throws SQLException{return rs.getTimestamp(columnIndex);}
    };
    private static final TDBTypeHandler<Date> DateHandler = new TDBTypeHandler<Date>(){
        @Override public Class<Date> supportType(){return Date.class;}
        @Override public int jdbcType(){return Types.TIMESTAMP;}
        @Override public String jdbcTypeName(){return "TIMESTAMP";}
        @Override public void setParameter(PreparedStatement prep, int parameterIndex, Date value)throws SQLException{prep.setTimestamp(parameterIndex, new Timestamp(value.getTime()));};
        @Override public Date getResult(ResultSet rs, int columnIndex)throws SQLException{
            Timestamp timestamp = rs.getTimestamp(columnIndex);
            if(timestamp==null) return null;
            return new Date(timestamp.getTime());
        }
    };
    private static final TDBTypeHandler<Calendar> CalendarHandler = new TDBTypeHandler<Calendar>(){
        @Override public Class<Calendar> supportType(){return Calendar.class;}
        @Override public int jdbcType(){return Types.TIMESTAMP;}
        @Override public String jdbcTypeName(){return "TIMESTAMP";}
        @Override public void setParameter(PreparedStatement prep, int parameterIndex, Calendar value)throws SQLException{prep.setTimestamp(parameterIndex, new Timestamp(value.getTimeInMillis()));};
        @Override public Calendar getResult(ResultSet rs, int columnIndex)throws SQLException{
            Timestamp timestamp = rs.getTimestamp(columnIndex);
            if(timestamp==null) return null;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp.getTime());
            return calendar;
        }
    };
    private static final TDBTypeHandler<BigDecimal> BigDecimalHandler = new TDBTypeHandler<BigDecimal>(){
        @Override public Class<BigDecimal> supportType(){return BigDecimal.class;}
        @Override public int jdbcType(){return Types.DECIMAL;}
        @Override public String jdbcTypeName(){return "DECIMAL";}
        @Override public void setParameter(PreparedStatement prep, int parameterIndex, BigDecimal value)throws SQLException{prep.setBigDecimal(parameterIndex, value);};
        @Override public BigDecimal getResult(ResultSet rs, int columnIndex)throws SQLException{return rs.getBigDecimal(columnIndex);}
    };
    private static final TDBTypeHandler<byte[]> byteArrHandler = new TDBTypeHandler<byte[]>(){
        @Override public Class<byte[]> supportType(){return byte[].class;}
        @Override public int jdbcType(){return Types.BLOB;}
        @Override public String jdbcTypeName(){return "BLOB";}
        @Override public void setParameter(PreparedStatement prep, int parameterIndex, byte[] value)throws SQLException{prep.setBytes(parameterIndex, value);};
        @Override public byte[] getResult(ResultSet rs, int columnIndex)throws SQLException{return rs.getBytes(columnIndex);}
    };
    private static final TDBTypeHandler<InputStream> InputstreamHandler = new TDBTypeHandler<InputStream>(){
        @Override public Class<InputStream> supportType(){return InputStream.class;}
        @Override public int jdbcType(){return Types.BLOB;}
        @Override public String jdbcTypeName(){return "BLOB";}
        @Override public void setParameter(PreparedStatement prep, int parameterIndex, InputStream value)throws SQLException{prep.setBlob(parameterIndex, value);};
        @Override public InputStream getResult(ResultSet rs, int columnIndex)throws SQLException{return rs.getBinaryStream(columnIndex);}
    };
    
    private static final Map<Class<?>, TDBTypeHandler<?>> AllTypeHandlers = new HashMap<Class<?>, TDBTypeHandler<?>>();
    
    /**
     * 注册类型转换器<br>
     * @param typeHandler
     * @return
     * @see com.lvt4j.basic.TDB.TDBTypeHandler
     */
    public static final <E> TDBTypeHandler<?> registerTypeHandler(TDBTypeHandler<E> typeHandler){
        return AllTypeHandlers.put(typeHandler.supportType(), typeHandler);
    }
    
    /** 类对应的数据表名 */
    private static String tblName(Class<?> cls){
        Table table = cls.getAnnotation(Table.class);
        if(table==null || TVerify.strNullOrEmpty(table.value())) return cls.getSimpleName();
        return table.value();
    }
    
    /** 类属性对应的数据列名 */
    private static String colName(Field field){
        Col col = field.getAnnotation(Col.class);
        if(col==null || TVerify.strNullOrEmpty(col.value())) return field.getName();
        return col.value();
    }
    
    /** 判断javaType对应的jdbcType */
    public static int colType(Class<?> cls){
        return getTypeHandler(cls).jdbcType();
    }
    /** 判断javaType对应的jdbcType的名 */
    public static String colType2Str(Class<?> cls){
        return getTypeHandler(cls).jdbcTypeName();
    }

    private static <E> TDBTypeHandler<E> getTypeHandler(Class<E> cls){
        @SuppressWarnings("unchecked")
        TDBTypeHandler<E> typeHandler = (TDBTypeHandler<E>) AllTypeHandlers.get(cls);
        if(typeHandler==null) throw new RuntimeException("不支持的java类型<" + cls + ">");
        return typeHandler;
    }
    
    /**
     * 根据value的值类型不同,调用PreparedStatement的不同方法向其内赋值
     * 
     * @param values
     * @param key
     * @param value
     * @throws Exception
     */
    private static void setValues(PreparedStatement prep, Object... values){
        if (values == null || values.length==0) return;
        int i = 1;
        for(Object value : values) setValue(prep, i++, value);
    }

    /**
     * 根据value的值类型不同,调用PreparedStatement的不同方法向其内赋值
     * 
     * @param prep
     * @param i
     * @param value
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private static void setValue(PreparedStatement prep, int i, Object value){
        if (value==null){
            try{
                prep.setNull(i, Types.NULL);
            }catch(SQLException e){
                throw new RuntimeException("设置sql参数值为null失败", e);
            }
            return;
        }
        @SuppressWarnings("rawtypes")
        TDBTypeHandler typeHandler = getTypeHandler(value.getClass());
        if(typeHandler==null) throw new RuntimeException("未注册的java类型["+value.getClass()+"]");
        try{
            typeHandler.setParameter(prep, i, value);
        }catch(SQLException e){
            throw new RuntimeException("设置sql参数值为"+value+"失败", e);
        }
    }

    static{
        AllTypeHandlers.put(boolean.class, booleanHandler);
        AllTypeHandlers.put(Boolean.class, BooleanHandler);
        AllTypeHandlers.put(byte.class, byteHandler);
        AllTypeHandlers.put(Byte.class, ByteHandler);
        AllTypeHandlers.put(short.class, shortHandler);
        AllTypeHandlers.put(Short.class, ShortHandler);
        AllTypeHandlers.put(int.class, intHandler);
        AllTypeHandlers.put(Integer.class, IntegerHandler);
        AllTypeHandlers.put(long.class, longHandler);
        AllTypeHandlers.put(Long.class, LongHandler);
        AllTypeHandlers.put(float.class, floatHandler);
        AllTypeHandlers.put(Float.class, FloatHandler);
        AllTypeHandlers.put(double.class, doubleHandler);
        AllTypeHandlers.put(Double.class, DoubleHandler);
        AllTypeHandlers.put(char.class, charHandler);
        AllTypeHandlers.put(Character.class, CharacterHandler);
        AllTypeHandlers.put(String.class, StringHandler);
        AllTypeHandlers.put(StringBuilder.class, StringbuilderHandler);
        AllTypeHandlers.put(StringBuffer.class, StringbufferHandler);
        AllTypeHandlers.put(Timestamp.class, TimestampHandler);
        AllTypeHandlers.put(Date.class, DateHandler);
        AllTypeHandlers.put(Calendar.class, CalendarHandler);
        AllTypeHandlers.put(BigDecimal.class, BigDecimalHandler);
        AllTypeHandlers.put(byte[].class, byteArrHandler);
        AllTypeHandlers.put(InputStream.class, InputstreamHandler);
    }
    
    private boolean printSQL;
    private DataSource dataSource;
    private ThreadLocal<Connection> curConn = new ThreadLocal<Connection>();
    private ThreadLocal<Transaction> curTransaction = new ThreadLocal<Transaction>();

    public TDB(DataSource dataSource){
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
    public TDB(String driverClassName, String url, String user, String pwd){
        this.dataSource = new TDBDataSource(driverClassName, url, user, pwd);
    }
    
    /** 开启sql打印 */
    public void openPrintSQL(){
        printSQL = true;
    }
    private void pringSQL(String sql){
        if (!printSQL) return;
        System.out.println(sql);
    }
    
    private Connection getConnection(){
        Connection conn = curConn.get();
        if(conn!=null) return conn;
        try{
            conn = dataSource.getConnection();
            conn.setAutoCommit(true);
            curConn.set(conn);
            return conn;
        }catch(Throwable getThrowable){
            throw new RuntimeException("获取连接异常", getThrowable);
        }
    }
    
    /**
     * 在当前线程内开启一个事务<br>
     * 注意处理完后必须调用{@link #endTransaction}结束当前线程内的事务
     * @throws Exception
     */
    public void beginTransaction(){
        Transaction transaction = curTransaction.get();
        if(transaction!=null) return;
        try{
            transaction = new Transaction(getConnection());
            curTransaction.set(transaction);
        }catch(Throwable beginThrowable){
            throw new RuntimeException("开启事务异常", beginThrowable);
        }
    }
    
    /**
     * 回滚当前线程内事务
     * @throws Exception
     */
    public void rollbackTransaction(){
        Transaction transaction = curTransaction.get();
        if(transaction==null) return;
        try{
            transaction.conn.rollback();
        }catch(Throwable rollbackThrowable){
            throw new RuntimeException("回滚事务异常", rollbackThrowable);
        }
    }
    
    /**
     * 如果当前线程内有事务,则提交并结束<br>
     * 如果提交失败会尝试回滚并抛出异常<br>
     * 同时释放连接
     * @throws Exception
     */
    public void endTransaction(){
        Transaction transaction = curTransaction.get();
        if(transaction==null) return;
        RuntimeException ex = null;
        try{
            transaction.conn.commit();
        }catch(Throwable commitThrowable){
            ex = appendException(ex, "提交事务失败!", commitThrowable);
            try{
                transaction.conn.rollback();
            }catch(Throwable rollbackThrowable){
                ex = appendException(ex, "回滚事务失败!", rollbackThrowable);
            }
        }
        curTransaction.remove();
        Connection conn = curConn.get();
        if(conn==null)
            throw new RuntimeException("连接异常：当前线程内连接丢失!", ex);
        if(conn!=transaction.conn)
            throw new RuntimeException("连接异常：当前线程内连接与事务连接不一致！", ex);
        try{
            conn.close();
        }catch(Throwable closeThrowable){
            ex = appendException(ex, "关闭连接异常", closeThrowable);
        }
        curConn.remove();
        if(ex!=null) throw ex;
    }

    /**
     * 若当前线程未打开事务,则释放连接
     * @throws Exception
     */
    private void releaseNoneTransactionConnection(){
        Connection conn = curConn.get();
        if(conn==null) return;
        Transaction transaction = curTransaction.get();
        if(transaction!=null) return;
        try{
            curConn.remove();
            conn.close();
        }catch(Throwable closeThrowable){
            throw new RuntimeException("关闭连接异常", closeThrowable);
        }
    }

    /**
     * 关闭并释放execute期间使用的PreparedStatement、ResultSet及事务
     * @param ex
     * @param prep
     * @param rs
     */
    private void executeFinally(RuntimeException ex, PreparedStatement prep, ResultSet rs){
        try{
            if(rs!=null) rs.close();
        }catch(Throwable closeThrowable){
            ex = appendException(ex, "关闭ResultSet异常", closeThrowable);
        }
        try{
            if(prep!=null) prep.close();
        }catch(Throwable closeThrowable){
            ex = appendException(ex, "关闭PreparedStatement异常", closeThrowable);
        }
        try{
            releaseNoneTransactionConnection();
        }catch(Throwable closeThrowable){
            ex = appendException(ex, "关闭连接异常", closeThrowable);
        }
        if(ex!=null) throw ex;
    }

    private RuntimeException appendException(RuntimeException ex, String msg, Throwable cause){
        RuntimeException exNew = new RuntimeException(msg, cause);
        if(ex==null) ex = exNew;
        else ex.addSuppressed(exNew);
        return ex;
    }
    
    
    public <E> Insert<E> insert(E model){return new Insert<E>(model);}
    public <E> Insert<E> insert(Collection<E> models){return new Insert<E>(models);}
    public Delete delete(Object model){return new Delete(model);}
    public Delete delete(Class<?> modelCls, Object... idVals){return new Delete(modelCls, idVals);}
    public Update update(Object model, Object... idVals){return new Update(model, idVals);}
    public Select select(String sql, Object... args){return new Select(sql, args);}
    public <E> Get<E> get(E model){return new Get<E>(model);}
    public <E> Get<E> get(Class<E> modelCls, Object... idVals){return new Get<E>(modelCls, idVals);}
    public Exist exist(Object model){return new Exist(model);}
    public Exist exist(Class<?> modelCls, Object... idVals){return new Exist(modelCls, idVals);}
    public ExecSQL executeSQL(String sql, Object... args){return new ExecSQL(sql, args);}

    public class Insert<E>{
        private Class<E> modelCls;
        private List<E> models = new LinkedList<E>();
        
        private Insert(E model){insert(model);}
        private Insert(Collection<E> models){insert(models);}
        
        @SuppressWarnings("unchecked")
        public Insert<E> insert(E model){
            if(modelCls==null) modelCls = (Class<E>)model.getClass();
            models.add(model);
            return this;
        }
        @SuppressWarnings("unchecked")
        public Insert<E> insert(Collection<E> models){
            if(models.isEmpty()) return this;
            for(E model : models){
                if(modelCls==null) modelCls = (Class<E>)model.getClass();
                if(modelCls!=model.getClass())
                    throw new RuntimeException("待插入的Model["+modelCls
                            +"]与新添加的Model["+model.getClass()+"]类型不符!");
                this.models.add(model);
            }
            return this;
        }
        
        /** 返回影响的行数 */
        public int execute(){
            ModelRegister modelRegister = ModelRegister.get(modelCls);
            Collection<Field> fields = modelRegister.colFieldsMap.values();
            Field autoIdField = modelRegister.autoIdField;
            StringBuilder sql = new StringBuilder("insert into `"+tblName(modelCls)+"`(");
            StringBuilder qClause = new StringBuilder();
            boolean first = true;
            for(Field field : fields){
                if(!first) sql.append(',');
                if(!first) qClause.append(',');
                sql.append('`').append(colName(field)).append('`');
                qClause.append('?');
                first = false;
            }
            sql.append(")values(").append(qClause).append(")");
            String sqlStr = sql.toString();
            pringSQL(sqlStr);
            RuntimeException ex = null;
            PreparedStatement prep = null;
            ResultSet rs = null;
            try{
                prep = getConnection().prepareStatement(sqlStr, Statement.RETURN_GENERATED_KEYS);
                List<Object> valueS = new LinkedList<Object>();
                for(E model : models){
                    for(Field field : fields)
                        try{valueS.add(field.get(model));}catch(Exception e){valueS.add(null);}
                    setValues(prep, valueS.toArray());
                    prep.addBatch();
                    valueS.clear();
                }
                int[] rowCounts = prep.executeBatch();
                int rowCount = 0;
                for(int count : rowCounts)rowCount += count;
                if(autoIdField==null) return rowCount;
                try{
                    rs = prep.getGeneratedKeys();
                    for(E model : models){
                        if(!rs.next()) continue;
                        autoIdField.set(model,
                                getTypeHandler(autoIdField.getType()).getResult(rs, 1));
                    }
                }catch(Throwable setAutoIdThrowable){
                    throw new RuntimeException("获取与设置自增ID异常", setAutoIdThrowable);
                }
                return rowCount;
            }catch(Throwable executeThrowable){
                ex = appendException(ex, "执行insert异常", executeThrowable);
                return 0;
            }finally{executeFinally(ex, prep, rs);}
        }
        public void clear(){
            modelCls = null;
            models.clear();
        }
        @Override public String toString(){return "insert:"+models;}
    }
    public class Delete{
        private String sql;
        private Object[] argS;
        private Delete(Object model){
            Class<?> modelCls = model.getClass();
            ModelRegister modelRegister = ModelRegister.get(modelCls);
            List<Object> args = new LinkedList<Object>();
            StringBuilder sql = new StringBuilder("delete from `").append(tblName(modelCls)).append('`');
            boolean first = true;
            if(!modelRegister.idFields.isEmpty()){
                sql.append(" where");
                first = true;
                for(Field idField : modelRegister.idFields){
                    sql.append(first?" `":" and `").append(colName(idField)).append("`=?");
                    Object val = null;
                    try{val=idField.get(model);}catch(Exception ignore){}
                    if(val==null) throw new RuntimeException("Model["+modelCls+"]的主键["+colName(idField)+"]值不能为null!");
                    args.add(val);
                    first = false;
                }
            }
            sql.append(';');
            this.sql = sql.toString();
            argS = args.toArray();
        }
        private Delete(Class<?> modelCls, Object[] idVals){
            ModelRegister modelRegister = ModelRegister.get(modelCls);
            List<Object> args = new LinkedList<Object>();
            StringBuilder sql = new StringBuilder("delete from `").append(tblName(modelCls)).append('`');
            if(!modelRegister.idFields.isEmpty()){
                if(modelRegister.idFields.size()!=idVals.length)
                    throw new RuntimeException("Model["+modelCls+"]的主键数量["
                            +modelRegister.idFields.size()+"]与指定的主键值数量["+idVals.length+"]不符!");
                sql.append(" where");
                int i = 0;
                for(Field idField : modelRegister.idFields){
                    sql.append(i==0?" `":" and `").append(colName(idField)).append("`=?");
                    Object val = idVals[i];
                    if(val==null) throw new RuntimeException("Model["+modelCls+"]的主键["+colName(idField)+"]值不能为null!");
                    args.add(val);
                    i++;
                }
            }
            sql.append(';');
            this.sql = sql.toString();
            argS = args.toArray();
        }
        
        /** 返回影响的行数 */
        public int execute(){
            pringSQL(sql);
            RuntimeException ex = null;
            PreparedStatement prep = null;
            ResultSet rs = null;
            try{
                prep = getConnection().prepareStatement(sql);
                setValues(prep, argS);
                return prep.executeUpdate();
            }catch(Throwable executeThrowable){
                ex = appendException(ex, "执行delete异常", executeThrowable);
                return 0;
            }finally{executeFinally(ex, prep, rs);}
        }
        @Override public String toString(){return "delete:"+sql+". args:"+Arrays.asList(argS);}
    }
    public class Update{
        private String sql;
        private Object[] argS;
        private Update(Object model, Object[] idVals){
            Class<?> modelCls = model.getClass();
            ModelRegister modelRegister = ModelRegister.get(modelCls);
            List<Object> args = new LinkedList<Object>();
            StringBuilder sql = new StringBuilder("update `").append(tblName(modelCls)).append("` set ");
            boolean first = true;
            for(Field colField : modelRegister.colFieldsMap.values()){
                if(!first) sql.append(',');
                sql.append('`').append(colName(colField)).append("`=?");
                try{args.add(colField.get(model));}catch(Exception ignore){}
                first = false;
            }
            if(first) throw new RuntimeException("Model["+modelCls+"]没有可更新的列!");
            if(!modelRegister.idFields.isEmpty()){
                boolean specifyIdVals = idVals!=null && idVals.length>0;
                if(specifyIdVals && modelRegister.idFields.size()!=idVals.length)
                    throw new RuntimeException("Model["+modelCls+"]的主键数量["
                            +modelRegister.idFields.size()+"]与指定的主键值数量["+idVals.length+"]不符!");
                sql.append(" where");
                int i = 0;
                for(Field idField : modelRegister.idFields){
                    sql.append(i==0?" `":" and `").append(colName(idField)).append("`=?");
                    Object val = null;
                    try{val=specifyIdVals?idVals[i]:idField.get(model);}catch(Exception ignore){}
                    if(val==null) throw new RuntimeException("Model["+modelCls+"]的主键["+colName(idField)+"]值不能为null!");
                    args.add(val);
                    i++;
                }
            }
            sql.append(';');
            this.sql = sql.toString();
            argS = args.toArray();
        }
        
        /** 返回影响的行数 */
        public int execute(){
            pringSQL(sql);
            RuntimeException ex = null;
            PreparedStatement prep = null;
            ResultSet rs = null;
            try{
                prep = getConnection().prepareStatement(sql);
                setValues(prep, argS);
                return prep.executeUpdate();
            }catch(Throwable executeThrowable){
                ex = appendException(ex, "执行update异常", executeThrowable);
                return 0;
            }finally{executeFinally(ex, prep, rs);}
        }
        @Override public String toString(){return "update:"+sql+". args:"+Arrays.asList(argS);}
    }
    public class Select{
        private String sql;
        private Object[] argS;
        private Select(String sql, Object[] argS){
            this.sql = sql;
            this.argS = argS;
        }
        
        /**
         * 查询结果model对象列表<br>
         * 注意model必须是全局可见的
         * @param modelCls
         * @return
         * @throws SQLException 
         */
        public <E> List<E> execute2Model(Class<E> modelCls){
            List<E> rst = new LinkedList<E>();
            pringSQL(sql);
            RuntimeException ex = null;
            PreparedStatement prep = null;
            ResultSet rs = null;
            try{
                prep = getConnection().prepareStatement(sql);
                setValues(prep, argS);
                rs = prep.executeQuery();
                ResultSetMetaData metaData = rs.getMetaData();
                int colCount = metaData.getColumnCount();
                String[] colS = new String[colCount];
                for(int i=0; i<colCount; i++) colS[i] = metaData.getColumnLabel(i+1);
                ModelRegister modelRegister = ModelRegister.get(modelCls);
                while (rs.next()){
                    E model = TReflect.newInstance(modelCls);
                    for(int i=0; i<colS.length; i++){
                        String col = colS[i];
                        Field field = modelRegister.getRstField(col);
                        if(field==null) continue;
                        field.set(model, getTypeHandler(field.getType()).getResult(rs, i+1));
                    }
                    rst.add(model);
                }
                return rst;
            }catch(Throwable executeThrowable){
                throw ex=appendException(ex, "执行select.execute2Model()异常", executeThrowable);
            }finally{executeFinally(ex, prep, rs);}
        }
        
        /**
         * 查询为一个指定model对象<br>
         * 注意model必须是全局可见的
         * @param modelCls
         * @return 查询结果为空时返回null
         */
        public <E> E execute2ModelOne(Class<E> modelCls){
            pringSQL(sql);
            RuntimeException ex = null;
            PreparedStatement prep = null;
            ResultSet rs = null;
            try{
                prep = getConnection().prepareStatement(sql);
                setValues(prep, argS);
                rs = prep.executeQuery();
                if(!rs.next()) return null;
                ResultSetMetaData metaData = rs.getMetaData();
                int colCount = metaData.getColumnCount();
                String[] colS = new String[colCount];
                for(int i=0; i<colCount; i++) colS[i] = metaData.getColumnLabel(i+1);
                ModelRegister modelRegister = ModelRegister.get(modelCls);
                E model = TReflect.newInstance(modelCls);
                for(int i=0; i<colS.length; i++){
                    String col = colS[i];
                    Field field = modelRegister.getRstField(col);
                    if(field==null) continue;
                    field.set(model, getTypeHandler(field.getType()).getResult(rs, i+1));
                }
                return model;
            }catch(Throwable executeThrowable){
                throw ex=appendException(ex, "执行select.execute2ModelOne()异常", executeThrowable);
            }finally{executeFinally(ex, prep, rs);}
        }

        /**
         * 只查询一个列时,需要查询结果为多个单值
         * @param basic
         * @return
         */
        public <E> List<E> execute2Basic(Class<E> basic){
            List<E> rst = new LinkedList<E>();
            pringSQL(sql);
            RuntimeException ex = null;
            PreparedStatement prep = null;
            ResultSet rs = null;
            try{
                prep = getConnection().prepareStatement(sql);
                setValues(prep, argS);
                rs = prep.executeQuery();
                while (rs.next()) rst.add(getTypeHandler(basic).getResult(rs, 1));
                return rst;
            }catch(Throwable executeThrowable){
                throw ex=appendException(ex, "执行select.execute2Basic()异常", executeThrowable);
            }finally{executeFinally(ex, prep, rs);}
        }
        
        /**
         * 只查询一个列时,需要查询结果为单值
         * @param basic
         * @return
         */
        public <E> E execute2BasicOne(Class<E> basic){
            pringSQL(sql);
            RuntimeException ex = null;
            PreparedStatement prep = null;
            ResultSet rs = null;
            try{
                prep = getConnection().prepareStatement(sql);
                setValues(prep, argS);
                rs = prep.executeQuery();
                if(!rs.next()) return null;
                return getTypeHandler(basic).getResult(rs, 1);
            }catch(Throwable executeThrowable){
                throw ex=appendException(ex, "执行select.execute2BasicOne()异常", executeThrowable);
            }finally{executeFinally(ex, prep, rs);}
        }
        
        /**
         * 查询结果为map
         * @param clsS
         * @return
         */
        public List<Map<String, Object>> execute2Map(Class<?>... clsS){
            List<Map<String, Object>> rst = new LinkedList<Map<String, Object>>();
            pringSQL(sql);
            RuntimeException ex = null;
            PreparedStatement prep = null;
            ResultSet rs = null;
            try{
                prep = getConnection().prepareStatement(sql);
                setValues(prep, argS);
                rs = prep.executeQuery();
                ResultSetMetaData metaData = rs.getMetaData();
                int colCount = metaData.getColumnCount();
                if(clsS.length!=colCount)
                    throw new RuntimeException("参数类列表的数量["+clsS.length+"]"
                            + "与sql查询结果的列的数量["+colCount+"]不等!");
                String[] keys = new String[metaData.getColumnCount()];
                for(int i=0; i<colCount; i++) keys[i] = metaData.getColumnLabel(i+1);
                while (rs.next()){
                    Map<String, Object> map = new HashMap<String, Object>(colCount);
                    for(int i=0; i<colCount; i++)
                        map.put(keys[i], getTypeHandler(clsS[i]).getResult(rs, i+1));
                    rst.add(map);
                }
                return rst;
            }catch(Throwable executeThrowable){
                throw ex=appendException(ex, "执行select.execute2Map()异常", executeThrowable);
            }finally{executeFinally(ex, prep, rs);}
        }
        
        /**
         * 查询结果为map,且只查询一个结果
         * @param clsS
         * @return
         */
        public Map<String, Object> execute2MapOne(Class<?>... clsS){
            pringSQL(sql);
            RuntimeException ex = null;
            PreparedStatement prep = null;
            ResultSet rs = null;
            try{
                prep = getConnection().prepareStatement(sql);
                setValues(prep, argS);
                rs = prep.executeQuery();
                ResultSetMetaData metaData = rs.getMetaData();
                int colCount = metaData.getColumnCount();
                if(clsS.length!=colCount)
                    throw new RuntimeException("参数类列表的数量["+clsS.length+"]"
                            + "与sql查询结果的列的数量["+colCount+"]不等!");
                String[] keys = new String[metaData.getColumnCount()];
                for(int i=0; i<colCount; i++) keys[i] = metaData.getColumnLabel(i+1);
                if(!rs.next()) return null;
                Map<String, Object> map = new HashMap<String, Object>(colCount);
                for(int i=0; i<colCount; i++)
                    map.put(keys[i], getTypeHandler(clsS[i]).getResult(rs, i+1));
                return map;
            }catch(Throwable executeThrowable){
                throw ex=appendException(ex, "执行select.execute2MapOne()异常", executeThrowable);
            }finally{executeFinally(ex, prep, rs);}
        }
        @Override public String toString(){return "select:"+sql+". args:"+Arrays.asList(argS);}
    }
    public class Get<E>{
        private Class<E> modelCls;
        private String sql;
        private Object[] argS;
        @SuppressWarnings("unchecked")
        private Get(E model){
            modelCls = (Class<E>)model.getClass();
            ModelRegister modelRegister = ModelRegister.get(modelCls);
            StringBuilder sql = new StringBuilder("select * from `").append(tblName(modelCls)).append('`');
            List<Object> args = new LinkedList<Object>();
            if(!modelRegister.idFields.isEmpty()){
                sql.append(" where");
                boolean first = true;
                for(Field idField : modelRegister.idFields){
                    sql.append(first?" `":" and `").append(colName(idField)).append("`=?");
                    Object val = null;
                    try{val=idField.get(model);}catch(Exception ignore){}
                    if(val==null) throw new RuntimeException("Model["+modelCls+"]的主键["+colName(idField)+"]值不能为null!");
                    args.add(val);
                    first = false;
                }
            }
            this.sql = sql.toString();
            this.argS = args.toArray();
        }
        private Get(Class<E> modelCls, Object[] idVals){
            this.modelCls = modelCls;
            ModelRegister modelRegister = ModelRegister.get(modelCls);
            StringBuilder sql = new StringBuilder("select * from `").append(tblName(modelCls)).append('`');
            List<Object> args = new LinkedList<Object>();
            if(!modelRegister.idFields.isEmpty()){
                if(modelRegister.idFields.size()!=idVals.length)
                    throw new RuntimeException("Model["+modelCls+"]的主键数量["
                            +modelRegister.idFields.size()+"]与指定的主键值数量["+idVals.length+"]不符!");
                sql.append(" where");
                int i = 0;
                for(Field idField : modelRegister.idFields){
                    sql.append(i==0?" `":" and `").append(colName(idField)).append("`=?");
                    Object val = idVals[i];
                    if(val==null) throw new RuntimeException("Model["+modelCls+"]的主键["+colName(idField)+"]值不能为null!");
                    args.add(val);
                    i++;
                }
            }
            this.sql = sql.toString();
            this.argS = args.toArray();
        }
        public E execute(){
            return select(sql, argS).execute2ModelOne(modelCls);
        }
        @Override public String toString(){return "get:"+sql+". args:"+Arrays.asList(argS);}
    }
    public class Exist{
        private Class<?> modelCls;
        private String sql;
        private Object[] argS;
        private Exist(Object model){
            modelCls = (Class<?>)model.getClass();
            ModelRegister modelRegister = ModelRegister.get(modelCls);
            StringBuilder sql = new StringBuilder("select count(*)<>0 from `").append(tblName(modelCls)).append('`');
            List<Object> args = new LinkedList<Object>();
            if(!modelRegister.idFields.isEmpty()){
                sql.append(" where");
                boolean first = true;
                for(Field idField : modelRegister.idFields){
                    sql.append(first?" `":" and `").append(colName(idField)).append("`=?");
                    Object val = null;
                    try{val=idField.get(model);}catch(Exception ignore){}
                    if(val==null) throw new RuntimeException("Model["+modelCls+"]的主键["+colName(idField)+"]值不能为null!");
                    args.add(val);
                    first = false;
                }
            }
            this.sql = sql.toString();
            this.argS = args.toArray();
        }
        private Exist(Class<?> modelCls, Object[] idVals){
            this.modelCls = modelCls;
            ModelRegister modelRegister = ModelRegister.get(modelCls);
            StringBuilder sql = new StringBuilder("select count(*)<>0 from `").append(tblName(modelCls)).append('`');
            List<Object> args = new LinkedList<Object>();
            if(!modelRegister.idFields.isEmpty()){
                if(modelRegister.idFields.size()!=idVals.length)
                    throw new RuntimeException("Model["+modelCls+"]的主键数量["
                            +modelRegister.idFields.size()+"]与指定的主键值数量["+idVals.length+"]不符!");
                sql.append(" where");
                int i = 0;
                for(Field idField : modelRegister.idFields){
                    sql.append(i==0?" `":" and `").append(colName(idField)).append("`=?");
                    Object val = idVals[i];
                    if(val==null) throw new RuntimeException("Model["+modelCls+"]的主键["+colName(idField)+"]值不能为null!");
                    args.add(val);
                    i++;
                }
            }
            this.sql = sql.toString();
            this.argS = args.toArray();
        }
        public boolean execute(){
            return new Select(sql, argS).execute2BasicOne(boolean.class);
        }
        @Override public String toString(){return "exist:"+sql+". args:"+Arrays.asList(argS);}
    }
    public class ExecSQL{
        private String sql;
        private Object[] argS;
        private ExecSQL(String sql, Object[] argS){
            this.sql = sql;
            this.argS = argS;
        }

        public int execute(){
            pringSQL(sql);
            RuntimeException ex = null;
            PreparedStatement prep = null;
            try{
                prep = getConnection().prepareStatement(sql);
                setValues(prep, argS);
                int effectRowCount = prep.executeUpdate();
                prep.close();
                return effectRowCount;
            }catch(Throwable executeThrowable){
                throw ex=appendException(ex, "执行sql异常", executeThrowable);
            }finally{executeFinally(ex, prep, null);}
        }
        @Override public String toString(){return "execSql:"+sql+". args:"+Arrays.asList(argS);}
    }
    

    /** 简单数据源,最大只支持5个链接 */
    private class TDBDataSource implements DataSource{
        /** 最大连接数量 */
        private static final int MaxConnNum = 5;
        
        private String url;
        private String user;
        private String pwd;
        /** 已创建的连接的连接池 */
        ConcurrentLinkedQueue<Connection> allConns = new ConcurrentLinkedQueue<Connection>();
        /** 空闲连接池 */
        ConcurrentLinkedQueue<Connection> freeConns = new ConcurrentLinkedQueue<Connection>();
        
        private TDBDataSource(String driverClassName, String url, String user, String pwd){
            try{
                Class.forName(driverClassName);
            }catch(ClassNotFoundException e){
                throw new RuntimeException(e);
            }
            this.url = url;
            this.user = user;
            this.pwd = pwd;
        }
        
        @Override
        public Connection getConnection()throws SQLException{
            Connection conn = freeConns.poll();
            if(conn!=null) return conn;
            conn = makeConnection();
            if(conn!=null) return conn;
            while (conn==null){
                synchronized (this){
                    try{
                        wait();
                    }catch(InterruptedException ignore){}
                    conn = freeConns.poll();
                }
            }
            return conn;
        }
        
        private Connection makeConnection()throws SQLException{
            if(allConns.size()>=MaxConnNum) return null;
            synchronized (allConns){
                if(allConns.size()>=MaxConnNum) return null;
                Connection innerConn = TVerify.strNullOrEmpty(user)?
                        DriverManager.getConnection(url)
                        :DriverManager.getConnection(url, user, pwd);
                TDBDataSourceConnection conn = new TDBDataSourceConnection(this, innerConn);
                allConns.add(conn);
                return conn;
            }
        }
        
        private void releaseConnection(Connection conn){
            freeConns.add(conn);
            synchronized (this){
                notify();
            }
        }
        
        @Override public PrintWriter getLogWriter()throws SQLException{return null;}
        @Override public void setLogWriter(PrintWriter out)throws SQLException{}
        @Override public void setLoginTimeout(int seconds)throws SQLException{}
        @Override public int getLoginTimeout()throws SQLException{return 0;}
        @Override public Logger getParentLogger()throws SQLFeatureNotSupportedException{return null;}
        @Override public <T> T unwrap(Class<T> iface)throws SQLException{return null;}
        @Override public boolean isWrapperFor(Class<?> iface)throws SQLException{return false;}
        @Override public Connection getConnection(String username, String password)throws SQLException{return null;}
        
        private class TDBDataSourceConnection implements Connection{
            TDBDataSource dataSource;
            Connection conn;

            private TDBDataSourceConnection(TDBDataSource dataSource, Connection conn){
                this.dataSource = dataSource;
                this.conn = conn;
            }
            
            @Override
            public void close()throws SQLException{
                dataSource.releaseConnection(this);
            }
            
            @Override public <T> T unwrap(Class<T> iface)throws SQLException{return conn.unwrap(iface);}
            @Override public boolean isWrapperFor(Class<?> iface)throws SQLException{return conn.isWrapperFor(iface);}
            @Override public Statement createStatement()throws SQLException{return conn.createStatement();}
            @Override public PreparedStatement prepareStatement(String sql)throws SQLException{return conn.prepareStatement(sql);}
            @Override public CallableStatement prepareCall(String sql)throws SQLException{return conn.prepareCall(sql);}
            @Override public String nativeSQL(String sql)throws SQLException{return conn.nativeSQL(sql);}
            @Override public void setAutoCommit(boolean autoCommit)throws SQLException{conn.setAutoCommit(autoCommit);}
            @Override public boolean getAutoCommit()throws SQLException{return conn.getAutoCommit();}
            @Override public void commit()throws SQLException{conn.commit();}
            @Override public void rollback()throws SQLException{conn.rollback();}
            @Override public boolean isClosed()throws SQLException{return conn.isClosed();}
            @Override public DatabaseMetaData getMetaData()throws SQLException{return conn.getMetaData();}
            @Override public void setReadOnly(boolean readOnly)throws SQLException{conn.setReadOnly(readOnly);}
            @Override public boolean isReadOnly()throws SQLException{return conn.isReadOnly();}
            @Override public void setCatalog(String catalog)throws SQLException{conn.setCatalog(catalog);}
            @Override public String getCatalog()throws SQLException{return conn.getCatalog();}
            @Override public void setTransactionIsolation(int level)throws SQLException{conn.setTransactionIsolation(level);}
            @Override public int getTransactionIsolation()throws SQLException{return conn.getTransactionIsolation();}
            @Override public SQLWarning getWarnings()throws SQLException{return conn.getWarnings();}
            @Override public void clearWarnings()throws SQLException{conn.clearWarnings();}
            @Override public Statement createStatement(int resultSetType, int resultSetConcurrency)throws SQLException{return conn.createStatement(resultSetType, resultSetConcurrency);}
            @Override public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)throws SQLException{return conn.prepareStatement(sql, resultSetType, resultSetConcurrency);}
            @Override public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)throws SQLException{return conn.prepareCall(sql, resultSetType, resultSetConcurrency);}
            @Override public Map<String, Class<?>> getTypeMap()throws SQLException{return conn.getTypeMap();}
            @Override public void setTypeMap(Map<String, Class<?>> map)throws SQLException{conn.setTypeMap(map);}
            @Override public void setHoldability(int holdability)throws SQLException{conn.setHoldability(holdability);}
            @Override public int getHoldability()throws SQLException{return conn.getHoldability();}
            @Override public Savepoint setSavepoint()throws SQLException{return conn.setSavepoint();}
            @Override public Savepoint setSavepoint(String name)throws SQLException{return conn.setSavepoint(name);}
            @Override public void rollback(Savepoint savepoint)throws SQLException{conn.rollback(savepoint);}
            @Override public void releaseSavepoint(Savepoint savepoint)throws SQLException{conn.releaseSavepoint(savepoint);}
            @Override public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)throws SQLException{return conn.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);}
            @Override public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)throws SQLException{return conn.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);}
            @Override public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)throws SQLException{return conn.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);}
            @Override public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)throws SQLException{return conn.prepareStatement(sql, autoGeneratedKeys);}
            @Override public PreparedStatement prepareStatement(String sql, int[] columnIndexes)throws SQLException{return conn.prepareStatement(sql, columnIndexes);}
            @Override public PreparedStatement prepareStatement(String sql, String[] columnNames)throws SQLException{return conn.prepareStatement(sql, columnNames);}
            @Override public Clob createClob()throws SQLException{return conn.createClob();}
            @Override public Blob createBlob()throws SQLException{return conn.createBlob();}
            @Override public NClob createNClob()throws SQLException{return conn.createNClob();}
            @Override public SQLXML createSQLXML()throws SQLException{return conn.createSQLXML();}
            @Override public boolean isValid(int timeout)throws SQLException{return conn.isValid(timeout);}
            @Override public void setClientInfo(String name, String value)throws SQLClientInfoException{conn.setClientInfo(name, value);}
            @Override public void setClientInfo(Properties properties)throws SQLClientInfoException{conn.setClientInfo(properties);}
            @Override public String getClientInfo(String name)throws SQLException{return conn.getClientInfo(name);}
            @Override public Properties getClientInfo()throws SQLException{return conn.getClientInfo();}
            @Override public Array createArrayOf(String typeName, Object[] elements)throws SQLException{return conn.createArrayOf(typeName, elements);}
            @Override public Struct createStruct(String typeName, Object[] attributes)throws SQLException{return conn.createStruct(typeName, attributes);}
            @Override public void setSchema(String schema)throws SQLException{conn.setSchema(schema);}
            @Override public String getSchema()throws SQLException{return conn.getSchema();}
            @Override public void abort(Executor executor)throws SQLException{conn.abort(executor);}
            @Override public void setNetworkTimeout(Executor executor, int milliseconds)throws SQLException{conn.setNetworkTimeout(executor, milliseconds);}
            @Override public int getNetworkTimeout()throws SQLException{return conn.getNetworkTimeout();}
        }
    }
    
    private class Transaction{
        private Connection conn;
        private Transaction(Connection conn){
            this.conn = conn;
            try{
                conn.setAutoCommit(false);
            }catch(Throwable e){
                throw new RuntimeException(e);
            }
        }
    }
    
    
    /** 已注册的模型类 */
    private static class ModelRegister{
        private static final Map<Class<?>, ModelRegister> RegisteredModels = new ConcurrentHashMap<Class<?>, ModelRegister>();
        
        private List<Field> idFields;
        private Field autoIdField;
        private Map<String, Field> colFieldsMap;
        private Map<String, Field> rstFieldsMap;
        private Map<String, Field> rstFieldsIgnoreCaseMap;
        
        private static ModelRegister get(Class<?> cls){
            ModelRegister modelRegister = RegisteredModels.get(cls);
            if(modelRegister!=null) return modelRegister;
            synchronized (RegisteredModels){
                modelRegister = RegisteredModels.get(cls);
                if(modelRegister!=null) return modelRegister;
                registerModelClass(cls);
                return RegisteredModels.get(cls);
            }
        }
        
        private static void registerModelClass(Class<?> cls){
            List<IdFieldComparator> idFieldComparators = new LinkedList<IdFieldComparator>();
            Field autoIdField = null;
            Map<String, Field> colFieldsMap = new HashMap<String, Field>();
            Map<String, Field> rstFieldsMap = new HashMap<String, Field>();
            Map<String, Field> rstFieldsIgnoreCaseMap = new HashMap<String, Field>();
            for(Field field : TReflect.allField(cls)){
                String colName = colName(field);
                String colNameLowerCase  =colName.toLowerCase();
                field.setAccessible(true);
                if(isRst(field) && !rstFieldsMap.containsKey(colName)){
                    rstFieldsMap.put(colName, field);
                    rstFieldsIgnoreCaseMap.put(colNameLowerCase, field);
                }
                if(isCol(field) && !colFieldsMap.containsKey(colName)) colFieldsMap.put(colName, field);
                if(autoIdField==null && isAutoId(field)) autoIdField = field;
                if(isId(field)) idFieldComparators.add(new IdFieldComparator(field));
            }
            Collections.sort(idFieldComparators);
            List<Field> idFields = new LinkedList<Field>();
            for(IdFieldComparator idFieldComparator : idFieldComparators) idFields.add(idFieldComparator.field);
            
            ModelRegister modelRegister = new ModelRegister();
            modelRegister.idFields = idFields;
            modelRegister.autoIdField = autoIdField;
            modelRegister.colFieldsMap = colFieldsMap;
            modelRegister.rstFieldsMap = rstFieldsMap;
            modelRegister.rstFieldsIgnoreCaseMap = rstFieldsIgnoreCaseMap;
            RegisteredModels.put(cls, modelRegister);
        }
        private static class IdFieldComparator implements Comparable<IdFieldComparator>{
            private int seq;
            private Field field;
            private IdFieldComparator(Field field){
                this.field = field;
                this.seq = field.getAnnotation(Col.class).idSeq();
            }
            @Override
            public int compareTo(IdFieldComparator o){
                return Integer.compare(seq, o.seq);
            }
        }
        /**
         * 判断一个属性是不是一个可以映射到一个数据库表列上<br>
         * 默认非static,非final,非transient的都会认为是一个列<br>
         * 除非它被{@link NotCol}所标记<br>
         * 用于数据插入
         * @param field
         * @return
         */
        private static boolean isCol(Field field){
            if(field.isAnnotationPresent(NotCol.class)) return false;
            int modifiers = field.getModifiers();
            return !Modifier.isStatic(modifiers) &&
                    !Modifier.isFinal(modifiers) &&
                    !Modifier.isTransient(modifiers);
        }
        /**
         * 判断一个属性是不是一个可以映射到数据库查询结果列上<br>
         * 非static,非final的都会认为是<br>
         * 用于select返回结果集
         * @param field
         * @return
         */
        private static boolean isRst(Field field){
            int modifiers = field.getModifiers();
            return !Modifier.isStatic(modifiers) &&
                    !Modifier.isFinal(modifiers);
        }
        /**
         * 判断一个属性是不是主键
         * @param field
         * @return
         * @see com.lvt4j.basic.TDB.Col
         */
        private static boolean isId(Field field){
            Col col = field.getAnnotation(Col.class);
            if(col==null) return false;
            return col.id();
        }
        /**
         * 判断一个属性是不是自增主键,用于insert后获取自增主键的值
         * @param field
         * @return
         * @see com.lvt4j.basic.TDB.Col
         */
        private static boolean isAutoId(Field field){
            Col col = field.getAnnotation(Col.class);
            if(col==null) return false;
            return col.autoId();
        }
    
        private Field getRstField(String colName){
            Field colField = rstFieldsMap.get(colName);
            if(colField!=null) return colField;
            return rstFieldsIgnoreCaseMap.get(colName.toLowerCase());
        }
    }
    
    /**
     * 声明于一个映射到数据库表的类上<br>
     * 用于指明该类对应的数据库表的表名<br>
     * 若无此注解,则表名为类的类名
     * @author LV
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public static @interface Table{
        String value() default "";
    }
    /**
     * 声明于一个映射到数据库表列的属性上,用于<br>
     * 1.指明该属性对应数据库表列的列名,若无此注解,则属性名即为列名<br>
     * 2.id=true表示该列为主键,用于update/delete一个model时生成where语句,若有多个主键,用idSeq表明顺序(递增)<br>
     * 3.若该列为自增主键,insert时若想获取自增id,需设置autoId为true<br>
     * @author LV
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface Col{
        String value() default "";
        boolean autoId() default false;
        boolean id() default false;
        int idSeq() default 0;
    }
    /**
     * 声明于一个不想映射到数据库表列的属性上<br>
     * @author LV
     * @see{@link TDB#isCol(Field)}
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface NotCol{}
    
    /**
     * 类型转换器<br>
     * 用于java类和数据库类型的转换与处理
     */
    public interface TDBTypeHandler<E>{
        /** 所有支持的java类 */
        public Class<E> supportType();
        /** 对应的jdbc类型,在{@link java.sql.Types Types}范围内 */
        public int jdbcType();
        /** 对应jdbc类型的名 */
        public String jdbcTypeName();
        /** 设置sql参数值 */
        public void setParameter(PreparedStatement prep, int parameterIndex, E value)throws SQLException;
        /** 结果集转为java类 */
        public E getResult(ResultSet rs, int columnIndex)throws SQLException;
    }
}
