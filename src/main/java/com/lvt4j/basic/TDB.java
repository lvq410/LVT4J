package com.lvt4j.basic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.NonNull;

import org.apache.commons.lang3.StringUtils;

import com.sun.xml.internal.ws.Closeable;

/**
 * 数据库轻量框架
 * @author lichenxi
 */
public class TDB implements Closeable {
    
    public static enum Driver{
        H2("org.h2.Driver","h2")
        ,SQLite("org.sqlite.JDBC","sqlite")
        ,MySql("com.mysql.jdbc.Driver","mysql");
        
        private String driverClassName;
        private String urlPrefix;
        private Driver(String driverClassName,String urlPrefix) {
            this.driverClassName = driverClassName;
            this.urlPrefix = urlPrefix;
        }
        
    }
    
    public static enum Qualifer{
        PrimaryKey("PRIMARY KEY"),
        NotNull("NOT NULL"),
        Unique("UNIQUE");
        
        private String qualifer;
        
        private Qualifer(String qualifer) {
            this.qualifer = qualifer;
        }
        
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public static @interface Table {
        
        String name() default "";
        
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface Col {
        
        String name() default "";
        
    }
    
    private static String tblName(Class<?> cls) {
        Table table = cls.getAnnotation(Table.class);
        return (table==null || TVerify.strNullOrEmpty(table.name()))?
                cls.getSimpleName():
                    table.name();
    }
    
    private static boolean isCol(Field field) {
        int modifiers = field.getModifiers();
        return !Modifier.isStatic(modifiers) &&
                !Modifier.isFinal(modifiers) &&
                !Modifier.isTransient(modifiers);
    }

    private static String colName(Field field) {
        Col col = field.getAnnotation(Col.class);
        return (col==null || TVerify.strNullOrEmpty(col.name()))?
                field.getName():
                    col.name();
    }
    
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
            throw new RuntimeException("Not support type<" + cls + ">");
        }
    }

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
            throw new RuntimeException("Not support type<" + cls + ">");
        }
    }

    private static Object colData2JData(Class<?> jType, ResultSet query, String col) {
        try {
            if (jType == byte.class || jType == Byte.class) {
                return query.getByte(col);
            } else if (jType == int.class || jType == Integer.class) {
                return query.getInt(col);
            } else if (jType == boolean.class || jType == Boolean.class) {
                return (boolean) query.getBoolean(col);
            } else if (jType == String.class) {
                return query.getString(col);
            } else if (jType == Date.class) {
                return new Date(query.getDate(col).getTime());
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
                calendar.setTimeInMillis(query.getDate(col).getTime());
                return calendar;
            } else {
                throw new RuntimeException("Not support type<" + jType + ">");
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    
    private static Object colData2JData(Class<?> jType, ResultSet query, int col) {
        try {
            if (jType == byte.class || jType == Byte.class) {
                return query.getByte(col);
            } else if (jType == int.class || jType == Integer.class) {
                return query.getInt(col);
            } else if (jType == boolean.class || jType == Boolean.class) {
                return (boolean) query.getBoolean(col);
            } else if (jType == String.class) {
                return query.getString(col);
            } else if (jType == Date.class) {
                return new Date(query.getDate(col).getTime());
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
                calendar.setTimeInMillis(query.getDate(col).getTime());
                return calendar;
            } else {
                throw new Exception("Not support type<" + jType + ">");
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean printSQL = false;

    public static void openPrintSQL() {
        printSQL = true;
    }
    
    /**
     * 根据value的值类型不同，调用PreparedStatement的不同方法向其内赋值
     * 
     * @param values
     * @param key
     * @param value
     * @throws Exception
     */
    private static void setValues(PreparedStatement prep, Object... values) {
        if (values == null || values.length==0) return;
        int i = 1;
        for (Object value : values)
            setValue(prep, i++, value);
    }

    /**
     * 根据value的值类型不同，调用PreparedStatement的不同方法向其内赋值
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
                prep.setDate(i, new java.sql.Date(((Date) value).getTime()));
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
                prep.setDate(i,
                        new java.sql.Date(((Calendar) value).getTimeInMillis()));
            else
                throw new RuntimeException("Not support type<" + value.getClass() + ">");
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static void pringSQL(StringBuilder sql) {
        if (printSQL) {
            if (TLog.isInitialized()) {
                TLog.i("TDB:" + sql.toString());
            } else {
                System.out.println(sql.toString());
            }
        }
    }

    private static void pringSQL(String sql) {
        if (printSQL) {
            if (TLog.isInitialized()) {
                TLog.i("TDB:" + sql);
            } else {
                System.out.println(sql.toString());
            }
        }
    }

    private static void clearEndComma(StringBuilder sql) {
        if (sql.charAt(sql.length() - 1) == ',') {
            sql.deleteCharAt(sql.length() - 1);
        }
    }

    private Connection conn;

    public TDB(Driver driver, String path) {
        try {
            Class.forName(driver.driverClassName);
            String url = "jdbc:"+driver.urlPrefix+":"+path;
            conn = DriverManager.getConnection(url);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    public TDB(Driver driver, String path, String userId, String pwd) {
        try {
            Class.forName(driver.driverClassName);
            String url = "jdbc:"+driver.urlPrefix+":"+path;
            conn = DriverManager.getConnection(url, userId, pwd);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    public TDB(Connection conn) {
        this.conn = conn;
    }
    
    public void close() {
        try {
            conn.close();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    public CreateTable table(Class<?> model) {
        return new CreateTable(model).in(conn);
    }

    public Index index(Class<?> model, String indexName, String colS)
            {
        return new Index(model, indexName, colS).in(conn);
    }

    public Insert insert() {
        return new Insert().in(conn);
    }
    
    public Insert insert(Object obj) {
        return new Insert(obj).in(conn);
    }

    public Select select(String sql, Object... args) {
        return new Select(sql, args).in(conn);
    }
    
    public Select select(Class<?> modelCls, String whereClause, Object... argS) {
        return new Select(modelCls, whereClause, argS).in(conn);
    }

    public Update update(Class<?> model) {
        return new Update(model).in(conn);
    }
    
    public Update update(String tblName) {
        return new Update(tblName).in(conn);
    }

    public Delete delete(Class<?> model) {
        return new Delete(model).in(conn);
    }

    public Num num(String sql, Object... args) {
        return new Num(sql, args).in(conn);
    }

    public Exist exist(Class<?> model, String whereClause, Object... argS) {
        return new Exist(model, whereClause, argS).in(conn);
    }

    public void executeSQL(String sql, Object... args) {
        try {
            pringSQL(sql);
            PreparedStatement prep = conn.prepareStatement(sql);
            setValues(prep, args);
            prep.execute();
            prep.close();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static class CreateTable {
        private Class<?> model;
        private Map<String, String> qualifiers = new HashMap<String, String>();
        private List<String> constraints = new ArrayList<String>();
        private Connection conn;
        private StringBuilder sql = new StringBuilder();
        
        private CreateTable(Class<?> model) {
            this.model = model;
            createSql();
        }

        private CreateTable in(Connection conn) {
            this.conn = conn;
            return this;
        }
        
        public CreateTable setQualifier(String col, Qualifer qualifier) {
            qualifiers.put(col, qualifier.qualifer);
            createSql();
            return this;
        }

        public CreateTable addConstraint(String constraintName, Qualifer qualifier,
                String fields) {
            constraints.add("constraint " + constraintName + " " + qualifier.qualifer
                    + " (" + fields + ")");
            createSql();
            return this;
        }

        private void createSql() {
            sql.setLength(0);
            String table = tblName(model);
            sql.append("create table if not exists " + table + " (");
            for (Field field : model.getDeclaredFields()) {
                if (!isCol(field))continue;
                String colName = colName(field);
                sql.append(colName).append(' ');
                sql.append(colType2Str(field.getType()));
                if (qualifiers.containsKey(colName)) {
                    sql.append(" "+qualifiers.get(colName)+" ");
                }
                sql.append(',');
            }
            for (String constraint : constraints) {
                sql.append(constraint).append(',');
            }
            clearEndComma(sql);
            sql.append(");");
        }
        
        public void execute() {
            pringSQL(sql);
            try {
                Statement stat = conn.createStatement();
                stat.execute(sql.toString());
                stat.close();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class Index {
        private Class<?> model;
        private String indexName, colS;
        private Connection conn;
        
        private Index(Class<?> model, String indexName, String colS)
                {
            this.model = model;
            this.indexName = indexName;
            this.colS = colS;
        }
        
        private Index in(Connection conn) {
            this.conn = conn;
            return this;
        }
        
        public void execute() {
            StringBuilder sql = new StringBuilder();
            String table = tblName(model);
            sql.append("create unique index if not exists " + indexName
                    + " on " + table + " (" + colS + ");");
            pringSQL(sql);
            try {
                Statement stat = conn.createStatement();
                stat.execute(sql.toString());
                stat.close();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class Insert {
        private Class<?> modelCls;
        private List<Object> modelS = new ArrayList<Object>();
        private Connection conn;
        
        private Insert() {}
        
        private Insert(Object model) {
            modelS.add(model);
            modelCls = model.getClass();
        }
        
        private Insert in(Connection conn) {
            this.conn = conn;
            return this;
        }

        public Insert insert(Object model) {
            if (modelCls!=null && modelCls!=model.getClass()) {
                throw new RuntimeException("Model class not match!For stroed class<"+modelCls+"> not equal <"+model.getClass()+">");
            }
            modelS.add(model);
            if (modelCls==null) {
                modelCls = model.getClass();
            }
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
            try {
                if (modelCls==null)return;
                StringBuilder sql = new StringBuilder();
                List<Field> fields = new ArrayList<Field>();
                List<Boolean> accessibles = new ArrayList<Boolean>();
                List<Object> valueS = new ArrayList<Object>();
                String table = tblName(modelCls);
                sql.append("insert into " + table + "(");
                for (Field field : modelCls.getDeclaredFields()) {
                    if (!isCol(field))continue;
                    accessibles.add(field.isAccessible());
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
                        valueS.add(field.get(model));
                    }
                    clearEndComma(sql);
                    sql.append("),");
                }
                for (Field field: fields) {
                    field.setAccessible(accessibles.remove(0));
                }
                clearEndComma(sql);
                sql.append(";");
                pringSQL(sql);
                PreparedStatement prep = conn.prepareStatement(sql.toString());
                setValues(prep, valueS.toArray());
                prep.executeUpdate();
                prep.close();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class Select {
        private StringBuilder sql;
        private Object[] argS;
        private Connection conn;
        
        private Select(String sql, Object... argS) {
            this.sql = new StringBuilder(sql);
            this.argS = argS;
        }
        
        private Select(Class<?> modelCls, String whereClause, Object... argS) {
            sql = new StringBuilder("select * from ")
                .append(tblName(modelCls));
            if (!TVerify.strNullOrEmpty(whereClause)) {
                sql.append(" where ").append(whereClause.replaceAll("where", ""));
            }
            this.argS = argS;
        }

        private Select in(Connection conn) {
            this.conn = conn;
            return this;
        }
        
        /**
         * 查询结果model对象列表
         * @param modelCls
         * @return
         */
        public <E> List<E> execute2Model(Class<E> modelCls) {
            try {
                List<Field> fieldS = new ArrayList<Field>();
                List<String> colS = new ArrayList<String>();
                for (Field field : modelCls.getDeclaredFields()) {
                    if (!isCol(field))continue;
                    field.setAccessible(true);
                    fieldS.add(field);
                    colS.add(colName(field));
                }
                List<E> rst = new ArrayList<E>();
                pringSQL(sql);
                PreparedStatement prep = conn.prepareStatement(sql.toString());
                setValues(prep, argS);
                ResultSet query = prep.executeQuery();
                while (query.next()) {
                    E obj = modelCls.newInstance();
                    for (int i = 0; i < fieldS.size(); i++) {
                        Field field = fieldS.get(i);
                        String col = colS.get(i);
                        field.set(obj, colData2JData(field.getType(), query, col));
                    }
                    rst.add(obj);
                }
                query.close();
                prep.close();
                return rst;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        
        /**
         * 查询为一个指定model对象
         * @param modelCls
         * @return 查询结果为空时返回null
         */
        public <E> E execute2ModelOne(Class<E> modelCls) {
            try {
                List<Field> fieldS = new ArrayList<Field>();
                List<String> colS = new ArrayList<String>();
                for (Field field : modelCls.getDeclaredFields()) {
                    if (!isCol(field))continue;
                    field.setAccessible(true);
                    fieldS.add(field);
                    colS.add(colName(field));
                }
                PreparedStatement prep = conn.prepareStatement(sql.toString());
                setValues(prep, argS);
                ResultSet query = prep.executeQuery();
                while (query.next()) {
                    E obj = modelCls.newInstance();
                    for (int i = 0; i < fieldS.size(); i++) {
                        Field field = fieldS.get(i);
                        String col = colS.get(i);
                        field.set(obj, colData2JData(field.getType(), query, col));
                    }
                    query.close();
                    prep.close();
                    return obj;
                }
                return null;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * 只查询一个列时，需要查询结果为单值的列表
         * @param basic
         * @return
         */
        @SuppressWarnings("unchecked")
        public <E> List<E> execute2Basic(Class<E> basic) {
            try {
                List<E> rst = new ArrayList<E>();
                pringSQL(sql);
                PreparedStatement prep = conn.prepareStatement(sql.toString());
                setValues(prep, argS);
                ResultSet query = prep.executeQuery();
                while (query.next()) {
                    rst.add((E) colData2JData(basic, query, 1));
                }
                query.close();
                prep.close();
                return rst;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        
        /**
         * 只查询一个列时，需要查询结果为单值
         * @param basic
         * @return
         */
        public <E> E execute2BasicOne(Class<E> basic) {
            List<E> rst = execute2Basic(basic);
            if(rst.isEmpty()) return null;
            return rst.get(0);
        }
        
        /**
         * 查询结果为map
         * @param clsS
         * @return
         */
        public List<Map<String, Object>> execute2Map(@NonNull Class<?>... clsS) {
            try {
                List<Map<String, Object>> rst = new ArrayList<Map<String, Object>>();
                pringSQL(sql);
                PreparedStatement prep = conn.prepareStatement(sql.toString());
                setValues(prep, argS);
                ResultSet query = prep.executeQuery();
                ResultSetMetaData metaData = query.getMetaData();
                int colCount = metaData.getColumnCount();
                if(clsS.length!=colCount)
                    throw new RuntimeException("Arg clsS'length["+clsS.length+"]"
                            + " is not equal sql rst col count["+colCount+"]!");
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
                query.close();
                prep.close();
                return rst;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        
        /**
         * 查询结果为map,且只查询一个结果
         * @param clsS
         * @return
         */
        public Map<String, Object> execute2MapOne(Class<?>... clsS) {
            List<Map<String, Object>> rst = execute2Map(clsS);
            if(rst.isEmpty()) return null;
            return rst.get(0);
        }
    }

    public static class Update {
        
        private Connection conn;
        
        private String tblName;
        private Map<String, Object> sets = new LinkedHashMap<String, Object>();
        private String whereClause;
        private Object[] whereArgS;
        
        private Update(Class<?> modelCls) {
            tblName = tblName(modelCls);
        }
        
        private Update(String tblName) {
            this.tblName = tblName;
        }
        
        private Update in(Connection conn) {
            this.conn = conn;
            return this;
        }
        
        public Update set(String col, Object val) {
            sets.put(col, val);
            return this;
        }

        public Update where(String whereClause, Object... whereArgS) {
            if(StringUtils.isEmpty(whereClause)) return this;
            this.whereClause = whereClause;
            this.whereArgS = whereArgS;
            return this;
        }

        public void execute() {
            if(sets.isEmpty()) return;
            StringBuilder sql = new StringBuilder("update " + tblName + " set ");
            List<Object> setValues = new ArrayList<Object>();
            for (Entry<String, Object> set : sets.entrySet()) {
                sql.append(set.getKey()).append("=?,");
                setValues.add(set.getValue());
            }
            clearEndComma(sql);
            if(StringUtils.isNotEmpty(whereClause))
                sql.append(" where ").append(whereClause).append(';');
            if(whereArgS!=null)
                setValues.addAll(Arrays.asList(whereArgS));
            pringSQL(sql);
            try {
                PreparedStatement prep = conn.prepareStatement(sql.toString());
                setValues(prep, setValues.toArray());
                prep.executeUpdate();
                prep.close();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class Delete {
        private StringBuilder sql;
        private List<Object> values = new ArrayList<Object>();;
        private Connection conn;
        
        private Delete(Class<?> modelCls) {
            sql = new StringBuilder("delete from " + tblName(modelCls));
        }
        
        private Delete in(Connection conn) {
            this.conn = conn;
            return this;
        }
        
        public Delete where(String whereClause, Object... whereArgS) {
            if (TVerify.strNullOrEmpty(whereClause)) {
                sql.append(" where ").append(whereClause.replaceAll("where", "where")).append(';');
                values.addAll(Arrays.asList(whereArgS));
            }
            return this;
        }

        public void execute() {
            pringSQL(sql);
            try {
                PreparedStatement prep = conn.prepareStatement(sql.toString());
                setValues(prep, values.toArray());
                prep.executeUpdate();
                prep.close();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class Num {
        private StringBuilder sql;
        private Object[] argS;
        private Connection conn;

        private Num(String sql, Object... argS) {
            this.sql = new StringBuilder(sql);
            this.argS = argS;
        }
        
        private Num in(Connection conn) {
            this.conn = conn;
            return this;
        }
        
        public int execute() {
            try {
                pringSQL(sql);
                PreparedStatement prep = conn.prepareStatement(sql.toString());
                setValues(prep, argS);
                ResultSet query = prep.executeQuery();
                if (query.next()) {
                    int num = query.getInt(1);
                    query.close();
                    prep.close();
                    return num;
                }
                query.close();
                prep.close();
                return 0;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class Exist {
        private StringBuilder sql;
        private Object[] args;
        private Connection conn;
        
        private Exist(Class<?> model, String whereClause, Object... args) {
            sql = new StringBuilder("select count(*) from ")
                .append(tblName(model));
            if (TVerify.strNullOrEmpty(whereClause)) {
                sql.append(" where ").append(whereClause.replaceAll("where", ""));
            }
            this.args = args;
        }
        
        private Exist in(Connection conn) {
            this.conn = conn;
            return this;
        }
        
        public boolean execute() {
            pringSQL(sql);
            try {
                PreparedStatement prep = conn.prepareStatement(sql.toString());
                setValues(prep, args);
                ResultSet query = prep.executeQuery();
                if (query.next()) {
                    int num = query.getInt(1);
                    query.close();
                    prep.close();
                    return num == 1;
                }
                query.close();
                prep.close();
                return false;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }
}
