package com.lvt4j.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.sf.json.JSONArray;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

@MappedJdbcTypes(value=JdbcType.VARCHAR)
@MappedTypes(value=JSONArray.class)
public class JSONArrayHandler implements TypeHandler<JSONArray> {

    @Override
    public void setParameter(PreparedStatement ps, int i, JSONArray arr,
            JdbcType jdbcType) throws SQLException {
        if (arr!=null && arr.size()>0) {
            ps.setString(i, arr.toString());
        } else {
            ps.setNull(i, JdbcType.VARCHAR.ordinal());
        }
    }

    @Override
    public JSONArray getResult(ResultSet rs, String columnName)
            throws SQLException {
        String val = rs.getString(columnName);
        return val==null?new JSONArray():JSONArray.fromObject(val);
    }

    @Override
    public JSONArray getResult(ResultSet rs, int columnIndex) throws SQLException {
        String val = rs.getString(columnIndex);
        return val==null?new JSONArray():JSONArray.fromObject(val);
    }

    @Override
    public JSONArray getResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        String val = cs.getString(columnIndex);
        return val==null?new JSONArray():JSONArray.fromObject(val);
    }

}
