package com.lvt4j.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.sf.json.JSONObject;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

@MappedJdbcTypes(value=JdbcType.VARCHAR)
@MappedTypes(value=JSONObject.class)
public class JSONObjectHandler implements TypeHandler<JSONObject> {

    @Override
    public void setParameter(PreparedStatement ps, int i, JSONObject json,
            JdbcType jdbcType) throws SQLException {
        if (json!=null && json.size()>0) {
            ps.setString(i, json.toString());
        } else {
            ps.setNull(i, JdbcType.VARCHAR.ordinal());
        }
    }

    @Override
    public JSONObject getResult(ResultSet rs, String columnName)
            throws SQLException {
        String val = rs.getString(columnName);
        return val==null?new JSONObject():JSONObject.fromObject(val);
    }
    
    @Override
    public JSONObject getResult(ResultSet rs, int columnIndex) throws SQLException {
        String val = rs.getString(columnIndex);
        return val==null?new JSONObject():JSONObject.fromObject(val);
    }

    @Override
    public JSONObject getResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        String val = cs.getString(columnIndex);
        return val==null?new JSONObject():JSONObject.fromObject(val);
    }

}
