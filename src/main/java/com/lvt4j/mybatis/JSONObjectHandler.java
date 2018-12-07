package com.lvt4j.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import net.sf.json.JSONObject;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import com.lvt4j.basic.TVerify;
import com.lvt4j.basic.TDB.TDBTypeHandler;

@MappedJdbcTypes(value=JdbcType.VARCHAR)
@MappedTypes(value=JSONObject.class)
public class JSONObjectHandler implements TypeHandler<JSONObject>,TDBTypeHandler<JSONObject> {

    /** 字符串反序列化为json对象时,若原字符串非空且不是json形式:false抛出异常,true(默认)返回空json对象 */
    private boolean ignoreNoJson = true;
    
    //---------------------------------------------------------------for mybatis
    @Override
    public void setParameter(PreparedStatement ps, int i, JSONObject json,
            JdbcType jdbcType) throws SQLException {
        if(json==null) {
            ps.setNull(i, JdbcType.VARCHAR.ordinal());
        } else {
            ps.setString(i, json.toString());
        }
    }

    @Override
    public JSONObject getResult(ResultSet rs, String columnName)
            throws SQLException {
        String val = rs.getString(columnName);
        if(TVerify.strNullOrEmpty(val)) return null;
        try{
            return JSONObject.fromObject(val);
        }catch(Exception e){
            if(ignoreNoJson) return new JSONObject();
            else throw e;
        }
    }
    
    @Override
    public JSONObject getResult(ResultSet rs, int columnIndex) throws SQLException {
        String val = rs.getString(columnIndex);
        if(TVerify.strNullOrEmpty(val)) return null;
        try{
            return JSONObject.fromObject(val);
        }catch(Exception e){
            if(ignoreNoJson) return new JSONObject();
            else throw e;
        }
    }

    @Override
    public JSONObject getResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        String val = cs.getString(columnIndex);
        if(TVerify.strNullOrEmpty(val)) return null;
        try{
            return JSONObject.fromObject(val);
        }catch(Exception e){
            if(ignoreNoJson) return new JSONObject();
            else throw e;
        }
    }

    //-------------------------------------------------------------------for TDB
    @Override
    public Class<JSONObject> supportType() {
        return JSONObject.class;
    }

    @Override
    public int jdbcType() {
        return Types.VARCHAR;
    }

    @Override
    public String jdbcTypeName() {
        return "VARCHAR";
    }

    @Override
    public void setParameter(PreparedStatement ps, int i,
            JSONObject json) throws SQLException {
        if(json==null) {
            ps.setNull(i, Types.NULL);
        } else {
            ps.setString(i, json.toString());
        }
    }

}
