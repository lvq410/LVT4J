package com.lvt4j.extend.typeadapter;

import com.lvt4j.basic.TTypeAdapter;
import com.lvt4j.basic.TTypeAdapter.TypeAdapter;

/**
 * 此包下类型转换器的动态注册工具<br>
 * 此包下的所有类型转换器不会在创建时全量加载类<br>
 * 而是当需要时才加载<br>
 * 此包下的所有类型转换器命名规则为TypeAdapter4[A类全名称]__[B类全名称],注:全名称里'.','$','[',';'被'_'替换<br>
 * 以实现依赖lvt4j的项目不需要依赖不需要的类型适配器所依赖的项目jar
 * @author LV
 * @see com.lvt4j.basic.TTypeAdapter
 * @see com.lvt4j.google.GsonSupport#setSupportClses
 * @see com.lvt4j.google.GsonSupport#loadJsonAdapters
 * @see com.lvt4j.spring.ControllerConfig#loadPropertySupports
 * @see com.lvt4j.spring.ControllerConfig#setPropertyEditorSupportClses
 */
public class TypeAdapterRegister {

    private static final String PackageName = TypeAdapterRegister.class.getPackage().getName();
    
    @SuppressWarnings("unchecked")
    public static TypeAdapter<?, ?> getAdapter(Class<?> typeA, Class<?> typeB) {
        TypeAdapter<?, ?> typeAdapter = TTypeAdapter.getAdapter(typeA, typeB);
        if(typeAdapter!=null) return typeAdapter;
        String typeAName = typeA.getName().replaceAll("[.$\\[;]", "_");
        String typeBName = typeB.getName().replaceAll("[.$\\[;]", "_");
        String[] typeAdapterClsNames = {
            PackageName+".TypeAdapter4"+typeAName+"__"+typeBName,
            PackageName+".TypeAdapter4"+typeBName+"__"+typeAName,
        };
        Class<TypeAdapter<?, ?>> typeAdapterCls = null;
        for (String typeAdapterClsName : typeAdapterClsNames) {
            try {
                typeAdapterCls = (Class<TypeAdapter<?, ?>>) Class.forName(typeAdapterClsName);
                typeAdapter = typeAdapterCls.newInstance();
                TTypeAdapter.registerAdapter(typeAdapter);
                return typeAdapter;
            } catch (Exception e) {}
        }
        throw new RuntimeException("扩展包里不存在类["+typeA+"]和["+typeB+"]的转换器");
    }
}
