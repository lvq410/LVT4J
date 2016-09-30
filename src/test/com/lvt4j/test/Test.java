package com.lvt4j.test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.Charset;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.lvt4j.basic.TDB;
import com.lvt4j.basic.TDB.Driver;
import com.lvt4j.office.TExcel;


public class Test {
    public static void main(String[] args) throws Exception {
        
        
        System.out.println(new BigDecimal(10072229.5));
        
        String[][] courseIdses = {{"1932","1933","1934"},{"2032","2033","2034","2036"}};
        String[] intervalses = {"2016-06-01T00:00:00+08:00/2016-09-08T00:00:00+08:00"
                ,"2016-08-01T00:00:00+08:00/2016-09-08T00:00:00+08:00"};
        
        final String qLTpl = FileUtils.readFileToString(new File("d:/ql.json"), Charset.defaultCharset());
        
        final String qTpl = FileUtils.readFileToString(new File("d:/q.json"), Charset.defaultCharset());
        
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        Credentials developerCredentials = new UsernamePasswordCredentials(
                "lichenxi", "L911c^k119v");
        credsProvider.setCredentials(
                AuthScope.ANY, developerCredentials);
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
        
        TDB db = new TDB(Driver.MySql,
                "//nb051x.corp.youdao.com:3306/dictCourse?characterEncoding=utf8&useLegacyDatetimeCode=false&serverTimezone=Asia/Shanghai"
                ,"discovery"
                ,"dictcourse123outfox");
        
        File rootFolder = new File("d:/LuoShuJun");
        rootFolder.mkdirs();
        
        for (int i = 0; i < courseIdses.length; i++) {
            for (int j = 0; j < courseIdses[i].length; j++) {
                String intervals = intervalses[i];
                String courseId = courseIdses[i][j];
                File courseOutFile = new File(rootFolder, courseId+".xls");
                TExcel excel = new TExcel();
                excel.open(courseOutFile.getAbsolutePath());
                
                String ql = qLTpl.replace("@@intervals@@", intervals).replace("@@courseId@@", courseId);
                JSONObject data = JSONObject.fromObject(ql);
                HttpPost post = new HttpPost("http://analyzer2.corp.youdao.com/druid/v2/cached?pretty");
                post.setEntity(new StringEntity(data.toString()));
                CloseableHttpResponse response = httpClient.execute(post);
                JSONArray lessonIdRst = JSONArray.fromObject(EntityUtils.toString(response.getEntity()))
                        .getJSONObject(0).getJSONArray("result");
                response.close();
                for (int k = 0; k < lessonIdRst.size(); k++) {
                    String lessonId = lessonIdRst.getJSONObject(k).getString("lessonId");
                    excel.createSheet(lessonId);
                    excel.createRow();
                    excel.addCell("userId");
                    excel.addCell("cnt");
                    excel.addCell("mobile");
                    
                    String q = qTpl.replace("@@intervals@@", intervals).replace("@@courseId@@", courseId)
                            .replace("@@lessonId@@", lessonId);
                    data = JSONObject.fromObject(q);
                    post = new HttpPost("http://analyzer2.corp.youdao.com/druid/v2/cached?pretty");
                    post.setEntity(new StringEntity(data.toString()));
                    response = httpClient.execute(post);
                    JSONArray userIdRst = JSONArray.fromObject(EntityUtils.toString(response.getEntity()))
                            .getJSONObject(0).getJSONArray("result");
                    response.close();
                    
                    for (int l = 0; l < userIdRst.size(); l++) {
                        String userId = userIdRst.getJSONObject(l).getString("userId");
                        long cnt = userIdRst.getJSONObject(l).getLong("cnt");
                        
                        String mobile = "";
                        String basicInfo = db.select("select basicInfo from userInfo where userId=?", userId)
                                .execute2BasicOne(String.class);
                        if(!StringUtils.isEmpty(basicInfo)) {
                            try {
                                mobile = JSONObject.fromObject(basicInfo).getString("mobile");
                            } catch (Exception e) {}
                        }
                        
                        excel.createRow();
                        excel.addCell(userId);
                        excel.addCell(cnt);
                        excel.addCell(mobile);
                    }
                }
                excel.close();
            }
        }
        
    }
}
