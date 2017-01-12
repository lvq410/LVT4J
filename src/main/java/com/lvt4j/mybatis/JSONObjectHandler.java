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

import com.lvt4j.basic.TDB.TDBTypeHandler;

@MappedJdbcTypes(value=JdbcType.VARCHAR)
@MappedTypes(value=JSONObject.class)
public class JSONObjectHandler implements TypeHandler<JSONObject>,TDBTypeHandler<JSONObject> {

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
        if(val==null) return null;
        return JSONObject.fromObject(val);
    }
    
    @Override
    public JSONObject getResult(ResultSet rs, int columnIndex) throws SQLException {
        String val = rs.getString(columnIndex);
        if(val==null) return null;
        return JSONObject.fromObject(val);
    }

    @Override
    public JSONObject getResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        String val = cs.getString(columnIndex);
        if(val==null) return null;
        return JSONObject.fromObject(val);
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
