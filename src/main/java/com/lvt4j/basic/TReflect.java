package com.lvt4j.basic;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 基于反射的方法集合
 * 
 * @author LV
 */
public class TReflect {
    /**
     * 将一个对象中的属性值赋值给另一个对象中属性名相同类型也相同 且修饰也相同的属性
     */
    public static void obj2Obj(Object fromObj, Object toObj) {
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

    public static final List<Field> allField(Class<?> cls) {
        List<Field> fields = new ArrayList<Field>();
        while (cls != null) {
            for (Field field: cls.getDeclaredFields()) {
                fields.add(field);
            }
            cls = cls == Object.class ? null : cls.getSuperclass();
        }
        return fields;
    }

    public static final List<Field> allField(Class<?> cls, int modifiers) {
        List<Field> fields = new ArrayList<Field>();
        while (cls != null) {
            for (Field field: cls.getDeclaredFields()) {
                if (field.getModifiers() == modifiers) {
                    fields.add(field);
                }
            }
            cls = cls == Object.class ? null : cls.getSuperclass();
        }
        return fields;
    }

    public static final Field field(Class<?> cls, String fieldName) {
        while (cls != null) {
            try {
                return cls.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {}
            cls = cls == Object.class ? null : cls.getSuperclass();
        }
        return null;
    }

    public static final Field field(Class<?> cls, String fieldName,
            int modifiers) {
        Field field = null;
        while (cls != null) {
            try {
                field = cls.getDeclaredField(fieldName);
                if (field.getModifiers() == modifiers) {
                    return field;
                }
            } catch (NoSuchFieldException e) {}
            cls = cls == Object.class ? null : cls.getSuperclass();
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
            Method[] methods = cls.getDeclaredMethods();
            for (Method method: methods) {
                if (method.getName().equals(methodName)) {
                    if (TVerify.arrNullOrEmpty(parameterTypeS)) {
                        return method;
                    } else {
                        if (TArr.equal(parameterTypeS,
                                method.getParameterTypes())) {
                            return method;
                        }
                    }
                }
            }
            cls = cls == Object.class ? null : cls.getSuperclass();
        }
        return null;
    }

    public static <E> E map2Obj(Map<String, Object> map, E obj) {
        for (Entry<String, Object> entry: map.entrySet()) {
            Field field = field(obj.getClass(), entry.getKey());
            if (field != null) {
                try {
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    field.set(obj, entry.getValue());
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        return obj;
    }

    public static Object invoke(String cls, String methodName, Object... args) {
        try {
            Method method = TReflect.method(Class.forName(cls), methodName);
            return method.invoke(null, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static final Map<Class<?>, Object> constructMap = new HashMap<Class<?>, Object>();
    static {
        constructMap.put(int.class, 0);
        constructMap.put(Integer.class, 0);
        constructMap.put(String.class, "");
        constructMap.put(char.class, (char) 0);
    }

    @SuppressWarnings("unchecked")
    public static <E> E newInstance(Class<E> cls, Object... initargs) {
        Constructor<?>[] constructors = cls.getConstructors();
        for (Constructor<?> constructor: constructors) {
            try {
                return (E) constructor.newInstance(initargs);
            } catch (Exception e) {}
        }
        for (Constructor<?> constructor: constructors) {
            Class<?>[] types = constructor.getParameterTypes();
            Object[] vals = new Object[types.length];
            for (int i = 0; i < types.length; i++) {
                if (!constructMap.containsKey(types[i]))
                    break;
                vals[i] = constructMap.get(types[i]);
            }
            try {
                return (E) constructor.newInstance(vals);
            } catch (Exception e) {}
        }
        if (constructMap.containsKey(cls)) {
            return (E) constructMap.get(cls);
        }
        throw new RuntimeException("Can't instance " + cls);
    }

}
