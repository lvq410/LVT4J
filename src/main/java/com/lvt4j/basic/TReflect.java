package com.lvt4j.basic;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 基于反射的方法集合
 * @author LV
 */
public class TReflect {
    
    /**
     * 将一个对象中的属性值赋值给另一个对象中属性名相同类型也相同 且修饰也相同的属性
     */
    public static final void obj2Obj(Object fromObj, Object toObj) {
        Class<?> fromClass = fromObj.getClass();
        Class<?> toClass = toObj.getClass();
        Field[] toFields = toClass.getDeclaredFields();
        for (int i = 0; i < toFields.length; i++) {
            Field toField = toFields[i];
            String fieldName = toField.getName();
            Field fromField;
            try {
                fromField = fromClass.getDeclaredField(fieldName);
            } catch (Exception e) {
                continue;
            }
            int fieldModifiers = toField.getModifiers();
            if (fromField.getModifiers() != fieldModifiers)
                continue;
            // final || static continue
            if (Modifier.isFinal(fieldModifiers)
                    || Modifier.isStatic(fieldModifiers))
                continue;
            fromField.setAccessible(true);
            toField.setAccessible(true);
            try {
                toField.set(toObj, fromField.get(fromObj));
            } catch (Exception e) {
                TLog.e("On field<" + fromField + "> to field <" + toField + ">",
                        e);
            }
        }
    }

    /**
     * 获得一个类包括其父类在内所有属性
     * @param cls
     * @return
     */
    public static final List<Field> allField(Class<?> cls) {
        return allField(cls, null);
    }

    /**
     * 获得一个类包括其父类在内所有属性<br>
     * 指定修饰符时，只返回与修饰符相等的属性<br>
     * @param cls
     * @param modifiers
     * @return
     */
    public static final List<Field> allField(Class<?> cls, Integer modifiers) {
        List<Field> fields = new LinkedList<Field>();
        while (cls != null) {
            for (Field field: cls.getDeclaredFields()) {
                if(modifiers!=null && field.getModifiers()!=modifiers) continue;
                fields.add(field);
            }
            cls = cls==Object.class ? null : cls.getSuperclass();
        }
        return fields;
    }

    /**
     * 查找指定名称的属性
     * @param cls
     * @param fieldName
     * @return
     */
    public static final Field field(Class<?> cls, String fieldName) {
        return field(cls, fieldName, null);
    }

    /**
     * 查找指定名称的属性，指定修饰符不为null时，会同时确保修饰符相同
     * @param cls
     * @param fieldName
     * @param modifiers
     * @return
     */
    public static final Field field(Class<?> cls, String fieldName,
            Integer modifiers) {
        Field field = null;
        while (cls != null) {
            try {
                field = cls.getDeclaredField(fieldName);
                if(modifiers==null) return field;
                if(field.getModifiers()==modifiers) return field;
            } catch (NoSuchFieldException e) {}
            cls = cls==Object.class ? null : cls.getSuperclass();
        }
        return null;
    }

    /**
     * 深度递归查找方法<br>
     * 未指定参数类型则返回第一个匹配到的，指定参数类型后则返回与参数类型完全匹配的，否则返回null
     */
    public static final Method method(Class<?> cls, String methodName,
            Class<?>... parameterTypeS) {
        while (cls != null) {
            for (Method method: cls.getDeclaredMethods()) {
                if(!method.getName().equals(methodName)) continue;
                if(TVerify.arrNullOrEmpty(parameterTypeS)) return method;
                if (!TArr.equal(parameterTypeS, method.getParameterTypes())) continue;
                return method;
            }
            cls = cls==Object.class ? null : cls.getSuperclass();
        }
        return null;
    }

    public static <E> E map2Obj(Map<String, Object> map, E obj) {
        for (Entry<String, Object> entry: map.entrySet()) {
            Field field = field(obj.getClass(), entry.getKey());
            if(field==null) continue;
            try {
                field.setAccessible(true);
                field.set(obj, entry.getValue());
            } catch (Throwable ignore) {}
        }
        return obj;
    }

    public static Object invoke(String cls, String methodName, Object... args) {
        try {
            return TReflect.method(Class.forName(cls), methodName).invoke(null, args);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static final Map<Class<?>, Object> constructMap = new HashMap<Class<?>, Object>();
    static {
        constructMap.put(byte.class, (byte)0);
        constructMap.put(Byte.class, (byte)0);
        constructMap.put(int.class, 0);
        constructMap.put(Integer.class, 0);
        constructMap.put(boolean.class, false);
        constructMap.put(Boolean.class, false);
        constructMap.put(short.class, (short)0);
        constructMap.put(Short.class, (short)0);
        constructMap.put(long.class, 0l);
        constructMap.put(Long.class, 0L);
        constructMap.put(float.class, 0f);
        constructMap.put(Float.class, 0F);
        constructMap.put(double.class, 0d);
        constructMap.put(Double.class, 0D);
        constructMap.put(char.class, (char)0);
        constructMap.put(Character.class, (char)0);
        constructMap.put(String.class, "");
    }

    @SuppressWarnings("unchecked")
    public static <E> E newInstance(Class<E> cls, Object... initargs) {
        Constructor<?>[] constructors = cls.getDeclaredConstructors();
        for (Constructor<?> constructor: constructors) {
            try {
                constructor.setAccessible(true);
                return (E) constructor.newInstance(initargs);
            } catch (Throwable ignore) {}
        }
        for (Constructor<?> constructor: constructors) {
            Class<?>[] types = constructor.getParameterTypes();
            if(types.length==0) continue;
            Object[] vals = new Object[types.length];
            for (int i = 0; i < types.length; i++) {
                if (!constructMap.containsKey(types[i])) break;
                vals[i] = constructMap.get(types[i]);
            }
            try {
                return (E) constructor.newInstance(vals);
            } catch (Throwable e) {}
        }
        if (constructMap.containsKey(cls)) {
            return (E) constructMap.get(cls);
        }
        throw new RuntimeException("无法实例化类:" + cls);
    }

}
