package com.lvt4j.basic;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.lvt4j.basic.TCounter;
import com.lvt4j.basic.TDB;
import com.lvt4j.basic.TDB.Col;
import com.lvt4j.basic.TDB.Table;
import com.lvt4j.mybatis.JSONArrayHandler;
import com.lvt4j.mybatis.JSONObjectHandler;

/**
 *
 * @author LV
 *
 */
public class TDBTest {

    final String driverClassName = "com.mysql.cj.jdbc.Driver";
    final String url = "jdbc:mysql://nc008x.corp.youdao.com:3306/lichenxidb?characterEncoding=utf8&useLegacyDatetimeCode=false&serverTimezone=Asia/Shanghai";
    final String user = "eadonline4nb";
    final String pwd = "new1ife4Th1sAugust";
    
    TDB myDB;
    TDB externalDB;
    
    @Before
    public void before() {
        myDB = new TDB(driverClassName, url, user, pwd);
        
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(pwd);
        dataSource.setInitialSize(8);
        dataSource.setMaxTotal(8);
        dataSource.setMaxIdle(4);
        dataSource.setMinIdle(4);
        dataSource.setMaxWaitMillis(12000);
        dataSource.setRemoveAbandonedOnBorrow(true);
        dataSource.setRemoveAbandonedTimeout(180);
        dataSource.setValidationQuery("select 1");
        dataSource.setTestOnBorrow(true);
        externalDB = new TDB(dataSource);
    }
    
    @Test
    public void test() {
        TDB.registerTypeHandler(new JSONArrayHandler());
        TDB.registerTypeHandler(new JSONObjectHandler());
        
        for (TDB db : new TDB[]{myDB, externalDB}) {
            db.executeSQL("drop table if exists `model`").execute();
            db.executeSQL("create table `model`("
                    + "`id` int(11) not null auto_increment, "
                    + "`col` varchar(255) default null,"
                    + "`json` text ,"
                    + "`arr` text ,"
                    + "`num` integer default null ,"
                    + "primary key (`id`)) "
                    + "engine=innodb default charset=utf8")
                    .execute();
            
            Assert.assertTrue(0==
                    db.select("select count(*) from model").execute2BasicOne(int.class));
            
            TCounter counter = new TCounter();
            for (int i = 0; i < 100; i++) {
                TestJobThread job = new TestJobThread();
                job.db = db;
                job.counter = counter;
                counter.inc();
                job.start();
            }
            
            counter.waitUntil(0, -1);
            
            int rowCount = db.executeSQL("delete from model").execute();
            Assert.assertTrue(rowCount>0);
            db.executeSQL("drop table `model`").execute();
        }
    }
    
    class TestJobThread extends Thread {
        
        TDB db;
        String suffix = UUID.randomUUID().toString();
        TCounter counter;
        
        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            String data1 = "1-"+suffix;
            String data2 = "2-"+suffix;
            String data3 = "3-"+suffix;
            String data4 = "4-"+suffix;
            String data5 = "5-"+suffix;
            String data6 = "6-"+suffix;
            String data7 = "7-"+suffix;
            String data8 = "8-"+suffix;
            String data9 = "9-"+suffix;
            
            Model model1 = new Model();
            model1.alias = data1;
            db.insert(model1).execute();
            Assert.assertTrue(model1.id!=null && model1.id>0);
            Assert.assertEquals(data1,
                    db.select("select col from model where col=?", data1).execute2BasicOne(String.class));
            
            db.executeSQL("delete from model where col=?", data1).execute();
            Assert.assertFalse(
                    db.select("select count(*)<>0 from model where col=?", data1).execute2BasicOne(boolean.class));
            
            db.beginTransaction();
            Model model2 = new Model();
            model2.alias = data2;
            db.insert(model2).execute();
            Assert.assertEquals(data2,
                    db.select("select col from model where col=?", data2).execute2BasicOne(String.class));
            model2.id = (Integer) db.select("select id from model where col=?", data2).execute2MapOne(int.class).get("id");
            
            db.rollbackTransaction();
            Assert.assertFalse(
                    db.select("select count(*)<>0 from model where id=?", model2.id).execute2BasicOne(boolean.class));
            
            Model model3 = new Model();
            model3.alias = data3;
            db.insert(model3).execute();
            model3.id = db.select("select * from model where col=?", data3).execute2ModelOne(Model.class).id;
            Assert.assertTrue(model3.id>model2.id);
            db.endTransaction();
            Assert.assertTrue(
                    db.select("select count(*)<>0 from model where id=?", model3.id).execute2BasicOne(boolean.class));
            
            db.executeSQL("update model set col=? where id=?", data4, model3.id).execute();
            
            Model model5 = new Model();
            model5.alias = data5;
            db.insert(model5).execute();
            Model model6 = new Model();
            model6.alias = data6;
            db.insert(model6).execute();
            
            Collection<String> standardCols = Arrays.asList(data4, data5, data6);
            Assert.assertTrue(
                    TCollection.isEqual(standardCols,
                            db.select("select col from model where col like ?", "%"+suffix).execute2Basic(String.class))
            );
            
            List<String> mapRstCols = new LinkedList<String>();
            for (Map<String, Object> row : db.select("select id,col from model where col like ?", "%"+suffix).execute2Map(int.class, String.class)) {
                mapRstCols.add((String) row.get("col"));
            }
            Assert.assertTrue(TCollection.isEqual(standardCols, mapRstCols));
            
            Model model7 = new Model();
            model7.alias = data7;
            model7.json = new JSONObject();
            model7.json.put("data", data7);
            db.insert(model7).execute();
            JSONObject model7Json = db.select("select json from model where id=?", model7.id).execute2BasicOne(JSONObject.class);
            Assert.assertNotNull(model7Json);
            Assert.assertEquals(data7, model7Json.getString("data"));
            
            Model model8 = new Model();
            model8.alias = data8;
            model8.arr = new JSONArray();
            model8.arr.add(data7);
            model8.arr.add(data8);
            db.insert(model8).execute();
            JSONArray model8Arr = db.select("select arr from model where id=?", model8.id).execute2BasicOne(JSONArray.class);
            Assert.assertNotNull(model8Arr);
            standardCols = Arrays.asList(data7, data8);
            Assert.assertTrue(TCollection.isEqual(standardCols, model8Arr));
            
            Model model9 = new Model();
            model9.alias = data9;
            db.insert(model9).execute();
            model9 = db.select("select * from model where id=?", model9.id).execute2ModelOne(Model.class);
            Assert.assertNull(model9.num);
            Assert.assertTrue(0==db.select("select num from model where id=?", model9.id).execute2BasicOne(int.class));
            
            counter.dec();
        }
    }
    
    static class BaseBean {
        @Col(autoId=true)
        Integer id;
    }
    @Table("model")
    static class Model extends BaseBean{
        @Col("col")
        String alias;
        JSONObject json;
        JSONArray arr;
        Integer num;
    }
    
}
