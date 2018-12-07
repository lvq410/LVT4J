package com.lvt4j.google;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.lvt4j.basic.TTypeAdapter.TypeAdapter;
import com.lvt4j.extend.typeadapter.TypeAdapterRegister;

/**
 * 创建基于{@link com.lvt4j.basic.TTypeAdapter TTypeAdapter}的gson
 * @author LV
 */
public class GsonSupport {
    
    /** 类型转换支持的类型(基于{@link com.lvt4j.basic.TTypeAdapter TTypeAdapter}) */
    private Class<?>[] supportClses;
    /** 设置类型转换支持的类型(基于{@link com.lvt4j.basic.TTypeAdapter TTypeAdapter}) */
    public void setSupportClses(Class<?>... supportClses) {
        this.supportClses = supportClses;
    }
    private JsonAdapter<?>[] jsonAdapters;
    
    /** 额外的类型转换适配器 */
    private Map<Class<?>, Object> extraTypeAdapter = new HashMap<Class<?>, Object>();
    /** 添加额外的类型转换适配器 */
    public Object addExtraTypeAdapter(Class<?> type, Object adapter) {
        return extraTypeAdapter.put(type, adapter);
    }
    
    public Gson buildGson() {
        loadJsonAdapters();
        GsonBuilder builder = new GsonBuilder();
        for (int i = 0; i < supportClses.length; i++) {
            Class<?> type = supportClses[i];
            JsonAdapter<?> jsonAdapter = jsonAdapters[i];
            builder.registerTypeAdapter(type, jsonAdapter);
        }
        return builder.create();
    }
    @SuppressWarnings("rawtypes")
    private void loadJsonAdapters() {
        if(jsonAdapters!=null) return;
        synchronized (this) {
            jsonAdapters = new JsonAdapter[supportClses.length];
            for (int i = 0; i < supportClses.length; i++) {
                Class<?> type = supportClses[i];
                jsonAdapters[i] = new JsonAdapter() {
                    TypeAdapter<?, ?> typeAdapter = TypeAdapterRegister.getAdapter(type, String.class);
                    @Override
                    public JsonElement serialize(Object src, Type typeOfSrc,
                            JsonSerializationContext context) {
                        return new JsonPrimitive((String) typeAdapter.change(src));
                    }
                    @Override
                    public Object deserialize(JsonElement json, Type typeOfT,
                            JsonDeserializationContext context)
                            throws JsonParseException {
                        if(json instanceof JsonPrimitive) return typeAdapter.change(json.getAsString());
                        else if(json instanceof JsonNull) return typeAdapter.change(null);
                        else return typeAdapter.change(json.toString());
                    }
                };
            }
        }
    }
    
    public interface JsonAdapter<T> extends JsonSerializer<T>, JsonDeserializer<T>{}
}
