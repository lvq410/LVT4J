package com.lvt4j.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import net.sf.json.JSONArray;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import com.lvt4j.basic.TDB.TDBTypeHandler;
import com.lvt4j.basic.TVerify;

@MappedJdbcTypes(value=JdbcType.VARCHAR)
@MappedTypes(value=JSONArray.class)
public class JSONArrayHandler implements TypeHandler<JSONArray>,TDBTypeHandler<JSONArray> {

    //---------------------------------------------------------------for mybatis
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
        return TVerify.strNullOrEmpty(val)?new JSONArray():JSONArray.fromObject(val);
    }

    @Override
    public JSONArray getResult(ResultSet rs, int columnIndex) throws SQLException {
        String val = rs.getString(columnIndex);
        return TVerify.strNullOrEmpty(val)?new JSONArray():JSONArray.fromObject(val);
    }

    @Override
    public JSONArray getResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        String val = cs.getString(columnIndex);
        return TVerify.strNullOrEmpty(val)?new JSONArray():JSONArray.fromObject(val);
    }

    //-------------------------------------------------------------------for TDB
    @Override
    public Class<JSONArray> supportType() {
        return JSONArray.class;
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
            JSONArray arr) throws SQLException {
        if (arr.size()>0) {
            ps.setString(i, arr.toString());
        } else {
            ps.setNull(i, Types.NULL);
        }
        
    }

}
