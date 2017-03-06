package com.lvt4j.basic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.sf.json.JSONObject;

import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.lvt4j.basic.TDB.Col;
import com.lvt4j.basic.TDB.NotCol;
import com.lvt4j.basic.TDB.Table;
import com.lvt4j.basic.TThread.SplitListJobWorker;
import com.lvt4j.mybatis.JSONArrayHandler;
import com.lvt4j.mybatis.JSONObjectHandler;

/**
 * @author LV
 */
public class TDBTest {

    final String driverClassName = "org.h2.Driver";
    final String url = "jdbc:h2:mem:test";
    final String user = "LV";
    final String pwd = "tdbtest";
    
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
            db.executeSQL("drop table if exists husband").execute();
            db.executeSQL("create table husband("
                    +"id int(11) not null auto_increment,"
                    +"name varchar(255) not null,"
                    +"salary int(11) default null,"
                    +"wifeName varchar(255),"
                    +"primary key (id,name)"
                    +")engine=innodb default charset=utf8").execute();
            db.executeSQL("drop table if exists wife").execute();
            db.executeSQL("create table wife("
                    +"id int(11) not null auto_increment,"
                    +"name varchar(255) not null,"
                    +"des varchar(255),"
                    +"json varchar(255),"
                    +"primary key (id,name)"
                    +")engine=innodb default charset=utf8").execute();
            
            Assert.assertTrue(0==
                    db.select("select count(*) from husband").execute2BasicOne(int.class));
            Assert.assertTrue(0==
                    db.select("select count(*) from wife").execute2BasicOne(int.class));
            
            int cycleNum = 100;
            List<Integer> jobs = new ArrayList<Integer>(100);
            for(int i=0; i<cycleNum; i++) jobs.add(i);
            TThread.splitListJob(jobs, 10, new SplitListJobWorker<Integer>(){@Override public void doJob(Integer job){
                String name = UUID.randomUUID().toString();
                
                Husband husband0 = new Husband();
                husband0.name = name;
                husband0.wifeName = name;
                Assert.assertTrue(1==db.insert(husband0).execute());
                int husband0OldId = husband0.id;
                Assert.assertTrue(husband0.id!=null && husband0.id>0);
                Assert.assertEquals(name,
                        db.select("select name from husband where id=?", husband0.id).execute2BasicOne(String.class));
                Assert.assertTrue(1==db.executeSQL("delete from husband where id=?", husband0.id).execute());
                Assert.assertFalse(db.select("select count(id)<>0 from husband where id=?", husband0.id).execute2BasicOne(boolean.class));
                Assert.assertTrue(1==db.insert(husband0).execute());
                Assert.assertTrue(db.select("select count(id)<>0 from husband where id=?", husband0.id).execute2BasicOne(boolean.class));
                Assert.assertTrue(husband0OldId==husband0.id);
                Assert.assertNull(db.select("select salary from husband where id=?", husband0.salary).execute2BasicOne(Integer.class));
                Assert.assertTrue(0==db.select("select salary from husband where id=?", husband0.id).execute2BasicOne(int.class));
                husband0.salary = 1000000;
                Assert.assertTrue(1==db.update(husband0).execute());
                Assert.assertTrue(1000000==db.select("select salary from husband where id=?", husband0.id).execute2BasicOne(int.class));
                husband0.salary = 10000000;
                Assert.assertTrue(1==db.update(husband0, husband0.id, husband0.name).execute());
                Assert.assertTrue(10000000==db.select("select salary from husband where id=?", husband0.id).execute2BasicOne(int.class));
                
                Assert.assertEquals(husband0, db.get(husband0).execute());
                
                Wife wife0 = new Wife();
                wife0.name = name;
                wife0.json = JSONObject.fromObject("{'name':'"+name+"'}");
                Assert.assertTrue(1==db.insert(wife0).execute());
                Assert.assertNull(db.select("select des from wife where id=?", wife0.id).execute2BasicOne(String.class));
                Assert.assertEquals(name,
                        db.select("select json from wife where id=?", wife0.id).execute2BasicOne(JSONObject.class).getString("name"));
                wife0.json = null;
                wife0.des = name;
                Assert.assertTrue(1==db.update(wife0).execute());
                Assert.assertNull(db.select("select json from wife where id=?", wife0.id).execute2Basic(JSONObject.class).get(0));
                Assert.assertEquals(name,
                        db.select("select des from wife where id=?", wife0.id).execute2BasicOne(String.class));
                Assert.assertEquals(wife0,
                        db.select("select * from wife where id=?", wife0.id).execute2ModelOne(Wife.class));
                
                Assert.assertNull(husband0.wifeDes);
                Assert.assertNull(husband0.wifeJson);
                husband0 = db.select("select H.id,H.name,H.salary,W.name as wifeName,W.des as wifeDes,W.json as wifeJson "
                        + "from husband H left join wife W on H.wifeName=W.name where H.id=?", husband0.id).execute2Model(Husband.class).get(0);
                Assert.assertEquals(husband0.wifeDes, wife0.des);
                Assert.assertEquals(husband0.wifeJson, wife0.json);
                
                Assert.assertTrue(db.select("select count(id)<>0 from husband where id=?", husband0.id).execute2BasicOne(boolean.class));
                Assert.assertTrue(db.select("select count(id)<>0 from wife where id=?", wife0.id).execute2BasicOne(boolean.class));
                db.beginTransaction();
                Assert.assertTrue(1==db.delete(husband0).execute());
                Assert.assertTrue(1==db.delete(wife0).execute());
                Assert.assertFalse(db.select("select count(id)<>0 from husband where id=?", husband0.id).execute2BasicOne(boolean.class));
                Assert.assertFalse(db.select("select count(id)<>0 from wife where id=?", wife0.id).execute2BasicOne(boolean.class));
                db.rollbackTransaction();
                Assert.assertTrue(db.select("select count(id)<>0 from husband where id=?", husband0.id).execute2BasicOne(boolean.class));
                Assert.assertTrue(db.select("select count(id)<>0 from wife where id=?", wife0.id).execute2BasicOne(boolean.class));
                
                db.beginTransaction();
                Assert.assertTrue(1==db.delete(husband0).execute());
                Assert.assertTrue(1==db.delete(wife0).execute());
                Assert.assertFalse(db.select("select count(id)<>0 from husband where id=?", husband0.id).execute2BasicOne(boolean.class));
                Assert.assertFalse(db.select("select count(id)<>0 from wife where id=?", wife0.id).execute2BasicOne(boolean.class));
                db.endTransaction();
                Assert.assertFalse(db.select("select count(id)<>0 from husband where id=?", husband0.id).execute2BasicOne(boolean.class));
                Assert.assertFalse(db.select("select count(id)<>0 from wife where id=?", wife0.id).execute2BasicOne(boolean.class));
                Assert.assertTrue(1==db.insert(husband0).execute());
                Assert.assertTrue(1==db.insert(wife0).execute());
                Assert.assertTrue(db.select("select count(id)<>0 from husband where id=?", husband0.id).execute2BasicOne(boolean.class));
                Assert.assertTrue(db.select("select count(id)<>0 from wife where id=?", wife0.id).execute2BasicOne(boolean.class));
            
                Husband husband1 = new Husband();
                husband1.name = name+1;
                Husband husband2 = new Husband();
                husband2.name = name+2;
                Assert.assertTrue(2==db.insert(husband1).insert(husband2).execute());
                Assert.assertTrue(husband1.id!=husband2.id);
                Assert.assertTrue(db.exist(husband1).execute());
                Assert.assertTrue(db.exist(Husband.class, husband2.id, husband2.name).execute());
                Assert.assertTrue(1==db.delete(husband1).execute());
                Assert.assertTrue(1==db.delete(husband2).execute());
                Assert.assertNull(db.get(husband1).execute());
                Assert.assertNull(db.get(Husband.class, husband1.id, husband1.name).execute());
                int oldHusband1Id = husband1.id;
                int oldHusband2Id = husband2.id;
                husband1.id = null;
                List<Husband> husbands = Arrays.asList(husband1, husband2);
                Assert.assertTrue(2==db.insert(husbands).execute());
                Assert.assertTrue(oldHusband1Id!=husband1.id);
                Assert.assertTrue(oldHusband2Id==husband2.id);
                Assert.assertEquals(husband1, db.get(husband1).execute());
                Assert.assertTrue(1==db.delete(husband1).execute());
                Assert.assertTrue(1==db.delete(Husband.class, husband2.id, husband2.name).execute());
            }});
            int husbandRowCount = db.delete(new Richer()).execute();
            Assert.assertTrue(husbandRowCount==cycleNum);
            int wifeRowCount = db.executeSQL("delete from wife").execute();
            Assert.assertTrue(husbandRowCount==wifeRowCount);
            
            db.executeSQL("drop table husband").execute();
            db.executeSQL("drop table wife").execute();
        }
    }
    
    @Data
    static class Person {
        @Col(autoId=true,id=true,idSeq=0)
        Integer id;
    }
    @Table("husband")
    @Data
    @EqualsAndHashCode(callSuper=true)
    static class Husband extends Person{
        @Col(value="name",id=true,idSeq=1)
        String name;
        Integer salary;
        String wifeName;
        
        @NotCol
        String wifeDes;
        transient JSONObject wifeJson;
    }
    @Table("wife")
    @Data
    @EqualsAndHashCode(callSuper=true)
    static class Wife extends Person{
        @Col(value="name",id=true,idSeq=1)
        String name;
        String des;
        JSONObject json;
    }
    
    @Table("husband")
    static class Richer{}
    
}
