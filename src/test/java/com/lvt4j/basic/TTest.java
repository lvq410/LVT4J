package com.lvt4j.basic;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import org.junit.Test;

/**
 *
 * @author LV
 *
 */
public class TTest {

    @Test
    public void test() {
    }
    
    public static void main(String[] args) throws Exception {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("D:/Program Files/apache-jmeter-2.12/rbac-test/userId.txt")));
        for (int i = 0; i < 10000; i++) {
            writer.write("user"+i);
            writer.newLine();
        }
        writer.close();
    }
    
}
