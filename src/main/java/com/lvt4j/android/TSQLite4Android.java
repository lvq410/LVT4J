package com.lvt4j.android;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.lvt4j.basic.TDataConvert;
import com.lvt4j.basic.TVerify;

/**
 * SQLite 数据库工具类 仅用于Android
 * 
 * @author LV
 * 
 */
public class TSQLite4Android {

    private static SQLiteDatabase defaultDB;

    public static void setDefaultDB(SQLiteDatabase database) {
        defaultDB = database;
    }

    public static CreateTable create(Class<?> cls) {
        return new CreateTable(cls);
    }

    public static CreateTable create(String createSQL) {
        return new CreateTable(createSQL);
    }

    public static Insert insert(Object obj) {
        return new Insert(obj);
    }

    public static Select select(String sql) {
        return new Select(sql);
    }

    public static Select select(String sql, Object... argS) {
        return new Select(sql, argS);
    }

    public static Update update() {
        return new Update();
    }

    public static Update update(SQLiteDatabase db) {
        return new Update(db);
    }

    public static Update update(Class<?> cls) {
        return new Update(cls);
    }

    public static Update update(String tbName) {
        return new Update(tbName);
    }

    public static Delete delete(Class<?> cls) {
        return new Delete(cls);
    }

    public static Delete delete(String tbName) {
        return new Delete(tbName);
    }

    public static Delete delete(SQLiteDatabase db) {
        return new Delete(db);
    }

    public static Num num(String sql) {
        return new Num(sql);
    }

    public static Num num(String sql, Object... argS) {
        return new Num(sql, argS);
    }

    public static Exist exist(Class<?> cls) {
        return new Exist(cls);
    }

    public static Exist exist(String tbName) {
        return new Exist(tbName);
    }

    public static Exist exist(SQLiteDatabase db) {
        return new Exist(db);
    }

    private static String tblName(Class<?> cls) {
        Table table = cls.getAnnotation(Table.class);
        return (table==null || TVerify.strNullOrEmpty(table.name()))?
                cls.getSimpleName():
                    table.name();
    }
    
    private static boolean isValidColField(Field field) {
        int modifiers = field.getModifiers();
        return !Modifier.isStatic(modifiers) &&
                !Modifier.isFinal(modifiers) &&
                !Modifier.isTransient(modifiers) &&
                field.getAnnotation(Col.class)!=null;
    }
    
    private static String colName(Field field) {
        Col col = field.getAnnotation(Col.class);
        return (col==null || TVerify.strNullOrEmpty(col.name()))?
                field.getName():
                    col.name();
    }

    private static boolean isID(Field field) {
        return "_id".equals(field.getName());
    }

    private static boolean isSupportType(Field field) {
        Class<?> cls = field.getType();
        if (cls == Integer.class || cls == int.class || cls == Byte.class
                || cls == byte.class || cls == Double.class
                || cls == double.class || cls == Float.class
                || cls == float.class || cls == String.class
                || cls == Boolean.class || cls == boolean.class
                || cls == Long.class || cls == long.class || cls == Date.class
                || cls == byte[].class || cls == Byte[].class)
            return true;
        else
            return false;
    }

    /**
     * 根据value的值类型不同，调用ContentValues的不同方法向其内赋值
     * 
     * @param values
     * @param key
     * @param value
     * @throws Exception
     */
    private static void setValues(ContentValues values, String key, Object value)
            {
        if (value == null)
            values.putNull(key);
        else if (value.getClass() == byte[].class)
            values.put(key, (byte[]) value);
        else if (value.getClass() == double.class
                || value.getClass() == Double.class)
            values.put(key, (Double) value);
        else if (value.getClass() == float.class
                || value.getClass() == Float.class)
            values.put(key, (Float) value);
        else if (value.getClass() == int.class
                || value.getClass() == Integer.class)
            values.put(key, (Integer) value);
        else if (value.getClass() == long.class
                || value.getClass() == Long.class)
            values.put(key, (Long) value);
        else if (value.getClass() == short.class
                || value.getClass() == Short.class)
            values.put(key, (Short) value);
        else if (value.getClass() == boolean.class
                || value.getClass() == Boolean.class)
            values.put(key, TDataConvert.bit2BitStr((Boolean) value));
        else if (value.getClass() == Date.class)
            values.put(key, ((Date) value).getTime());
        else if (value.getClass() == String.class)
            values.put(key, (String) value);
        else
            throw new RuntimeException("TSQLite is not support type<"
                    + value.getClass() + "> on <" + key + ">");
    }

    private static String[] changeArgS(Object... args) {
        if (args == null)
            return null;
        String[] sqlArgS = new String[args.length];
        for (int i = 0; i < sqlArgS.length; i++)
            if (args[i] == null)
                sqlArgS[i] = null;
            else if (args[i].getClass() == double.class
                    || args[i].getClass() == Double.class
                    || args[i].getClass() == float.class
                    || args[i].getClass() == Float.class
                    || args[i].getClass() == int.class
                    || args[i].getClass() == Integer.class
                    || args[i].getClass() == long.class
                    || args[i].getClass() == Long.class
                    || args[i].getClass() == short.class
                    || args[i].getClass() == Short.class
                    || args[i].getClass() == String.class)
                sqlArgS[i] = String.valueOf(args[i]);
            else if (args[i].getClass() == boolean.class
                    || args[i].getClass() == Boolean.class)
                sqlArgS[i] = TDataConvert.bit2BitStr((Boolean) args[i]);
            else if (args[i].getClass() == Date.class)
                sqlArgS[i] = String.valueOf(((Date) args[i]).getTime());
            else
                throw new RuntimeException("TSQLite is not support type<"
                        + args[i].getClass() + ">");
        return sqlArgS;
    }

    public static class CreateTable {
        private StringBuilder createSQL = new StringBuilder();
        private SQLiteDatabase db = defaultDB;

        public CreateTable(Class<?> cls) {
            createSQL.append("create table if not exists " + tblName(cls)
                    + " (_id integer primary key autoincrement");
            Field[] fields = cls.getDeclaredFields();
            for (Field field : fields){
                if (isID(field) || !isSupportType(field) || !isValidColField(field))continue;
                createSQL.append("," + colName(field));
            }
            createSQL.append(")");
        }

        public CreateTable(String createSQL) {
            this.createSQL.append(createSQL);
        }

        public CreateTable in(SQLiteDatabase db) {
            this.db = db;
            return this;
        }

        public void execute() {
            db.execSQL(createSQL.toString());
        }
    }

    public static class Insert {
        private List<Object> objs = new ArrayList<Object>();
        private SQLiteDatabase db = defaultDB;

        public Insert() {
        }
        
        private Insert(Object obj) {
            objs.add(obj);
        }

        public int size() {
            return objs.size();
        }
        
        public synchronized Insert insert(Object obj) {
            objs.add(obj);
            return this;
        }
        
        public Insert in(SQLiteDatabase db) {
            this.db = db;
            return this;
        }

        public void execute() {
            try {
                for (Object obj: objs) {
                    Class<?> cls = obj.getClass();
                    ContentValues values = new ContentValues();
                    Field[] fields = cls.getDeclaredFields();
                    for (Field field : fields) {
                        if (isID(field) || !isSupportType(field) || !isValidColField(field)) continue;
                        boolean accessible = field.isAccessible();
                        field.setAccessible(true);
                        setValues(values, colName(field), field.get(obj));
                        field.setAccessible(accessible);
                    }
                    db.insertOrThrow(tblName(cls), null, values);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class Select {
        private String sql;
        private String[] argS;
        private SQLiteDatabase db = defaultDB;
        private List<Field> fieldS;

        private Select(String sql, Object... argS) {
            this.sql = sql;
            if (argS==null || argS.length==0)return;
            this.argS = changeArgS(argS);
        }

        public Select in(SQLiteDatabase db) {
            this.db = db;
            return this;
        }

        public <E> List<E> execute(Class<E> cls, String fieldNameS){
            try {
                fieldNameS = fieldNameS.replaceAll(" ", "");
                fieldS = new ArrayList<Field>();
                List<Boolean> accessibles = new ArrayList<Boolean>();
                for (String fieldName : fieldNameS.split(",")) {
                    Field field = cls.getDeclaredField(fieldName);
                    accessibles.add(field.isAccessible());
                    field.setAccessible(true);
                    fieldS.add(field);
                }
                List<E> rst = new ArrayList<E>();
                Cursor cursor = db.rawQuery(sql, argS);
                while (cursor.moveToNext()) {
                    E obj = cls.newInstance();
                    for (int i = 0; i < fieldS.size(); i++)
                        if (fieldS.get(i).getType() == byte[].class
                        || fieldS.get(i).getType() == Byte[].class)
                            fieldS.get(i).set(obj, cursor.getBlob(i));
                        else if (fieldS.get(i).getType() == double.class
                                || fieldS.get(i).getType() == Double.class)
                            fieldS.get(i).set(obj, cursor.getDouble(i));
                        else if (fieldS.get(i).getType() == float.class
                                || fieldS.get(i).getType() == Float.class)
                            fieldS.get(i).set(obj, cursor.getFloat(i));
                        else if (fieldS.get(i).getType() == int.class
                                || fieldS.get(i).getType() == Integer.class)
                            fieldS.get(i).set(obj, cursor.getInt(i));
                        else if (fieldS.get(i).getType() == long.class
                                || fieldS.get(i).getType() == Long.class)
                            fieldS.get(i).set(obj, cursor.getLong(i));
                        else if (fieldS.get(i).getType() == short.class
                                || fieldS.get(i).getType() == Short.class)
                            fieldS.get(i).set(obj, cursor.getShort(i));
                        else if (fieldS.get(i).getType() == boolean.class
                                || fieldS.get(i).getType() == Boolean.class)
                            fieldS.get(i).set(obj,
                                    TDataConvert.bitStr2Bit(cursor.getString(i)));
                        else if (fieldS.get(i).getType() == Date.class)
                            fieldS.get(i).set(obj, new Date(cursor.getLong(i)));
                        else if (fieldS.get(i).getType() == String.class)
                            fieldS.get(i).set(obj, cursor.getString(i));
                    rst.add(obj);
                }
                for (int i = 0; i < fieldS.size(); i++) {
                    fieldS.get(i).setAccessible(accessibles.get(i));
                }
                cursor.close();
                return rst;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class Update {
        private String tbName;
        private SQLiteDatabase db = defaultDB;
        private ContentValues setValueS;
        private String whereClause;
        private String[] whereArgS;

        private Update() {
        }

        private Update(SQLiteDatabase db) {
            in(db);
        }

        private Update(Class<?> cls) {
            at(cls);
        }

        private Update(String tbName) {
            at(tbName);
        }

        public Update in(SQLiteDatabase db) {
            this.db = db;
            return this;
        }

        public Update at(Class<?> cls) {
            this.tbName = tblName(cls);
            return this;
        }

        public Update at(String tbName) {
            this.tbName = tbName;
            return this;
        }

        public Update set(String colNameS, Object... valueS) {
            colNameS = colNameS.replaceAll(" ", "");
            String colName[] = colNameS.split(",");
            if (colName.length != valueS.length)
                throw new RuntimeException("Fields num<" + colName.length
                        + "> diffrient valueS num<" + valueS.length + ">");
            setValueS = new ContentValues();
            for (int i = 0; i < colName.length; i++)
                setValues(setValueS, colName[i], valueS[i]);
            return this;
        }

        public Update set(Object obj) {
            try {
                Class<?> cls = obj.getClass();
                at(cls);
                setValueS = new ContentValues();
                for (Field field : cls.getDeclaredFields()) {
                    if (isID(field) || !isSupportType(field) || !isValidColField(field))continue;
                    boolean accessible = field.isAccessible();
                    field.setAccessible(true);
                    setValues(setValueS, colName(field), field.get(obj));
                    field.setAccessible(accessible);
                }
                return this;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public Update where(String whereClause, Object... argS)
                {
            this.whereClause = whereClause;
            this.whereArgS = changeArgS(argS);
            return this;
        }

        public void execute() {
            db.update(tbName, setValueS, whereClause, whereArgS);
        }
    }

    public static class Delete {
        private String tbName;
        private SQLiteDatabase db = defaultDB;
        private String whereClause = "";
        private String[] whereArgS = null;

        private Delete(Class<?> cls) {
            at(cls);
        }

        private Delete(String tbName) {
            at(tbName);
        }

        private Delete(SQLiteDatabase db) {
            in(db);
        }

        public Delete in(SQLiteDatabase db) {
            this.db = db;
            return this;
        }

        public Delete at(Class<?> cls) {
            this.tbName = tblName(cls);
            return this;
        }

        public Delete at(String tbName) {
            this.tbName = tbName;
            return this;
        }

        public Delete where(String whereClause, Object... argS){
            this.whereClause = whereClause;
            this.whereArgS = changeArgS(argS);
            return this;
        }

        public void execute() {
            db.delete(tbName, whereClause, whereArgS);
        }
    }

    public static class Num {
        private String sql;
        private String[] argS;
        private SQLiteDatabase db = defaultDB;

        public Num(String sql) {
            this.sql = sql;
        }

        public Num(String sql, Object... argS) {
            this.sql = sql;
            this.argS = changeArgS(argS);
        }

        public Num in(SQLiteDatabase db) {
            this.db = db;
            return this;
        }

        public int execute() {
            Cursor c = db.rawQuery(sql, argS);
            if (c.moveToNext()) {
                int num = c.getInt(0);
                c.close();
                return num;
            }
            c.close();
            return -1;
        }
    }

    public static class Exist {
        private String tbName;
        private String whereClause;
        private String[] whereArgS;
        private SQLiteDatabase db = defaultDB;

        public Exist(SQLiteDatabase db) {
            this.db = db;
        }

        public Exist(Class<?> cls) {
            at(cls);
        }

        public Exist(String tbName) {
            this.tbName = tbName;
        }

        public Exist in(SQLiteDatabase db) {
            this.db = db;
            return this;
        }

        public Exist at(Class<?> cls) {
            this.tbName = tblName(cls);
            return this;
        }

        public Exist at(String tbName) {
            this.tbName = tbName;
            return this;
        }

        public Exist where(String whereClause, Object... argS) {
            this.whereClause = whereClause;
            this.whereArgS = changeArgS(argS);
            return this;
        }

        public boolean execute() {
            Cursor c = db.rawQuery("select count(*) from " + tbName + " where "
                    + whereClause, whereArgS);
            if (c.moveToNext()) {
                boolean exist = c.getInt(0) != 0;
                c.close();
                return exist;
            }
            c.close();
            return false;
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
}
