package com.lvt4j.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import android.R.string;

import com.lvt4j.basic.TLog;
import com.lvt4j.basic.TProps;
import com.lvt4j.basic.TReflect;
import com.lvt4j.basic.TStr;
import com.lvt4j.mybatis.TMybatis;

public class Num {
	
	private Node first;
	private Node cur;
	
	
	public void append(byte data) {
		Node node = new Node();
		node.data = data;
		if (cur!=null) {
			cur.next = node;
		}
		cur = node;
		if (first == null) {
			first = node;
		}
	}
	
	
	public Num add(Num n) {
		Num rst = new Num();
		cur = first;
		n.cur = n.first;
		int offset = 0;
		while (cur!=null || n.cur!=null ) {
			byte a = cur!=null?cur.data:0;
			byte b = n.cur!=null?n.cur.data:0;
			if (cur!=null) {
				cur = cur.next;
			}
			if (n.cur!=null) {
				n.cur = n.cur.next;
			}
			byte data = (byte) (a+b+offset);
			if (data>9) {
				offset = data/10;
				data = (byte) (data%10);
			}
			rst.append(data);
		}
		
		return rst;
	}
	
	@Override
	public String toString() {
		cur = first;
		String str = "";
		while (cur!=null) {
			str = "->"+cur.data+str;
			cur = cur.next;
		}
		return TStr.lTrim(str, "->");
	}
	
	private static class Node{
		byte data;
		Node next;
	}
	public static void main(String[] args) throws Exception {
		String arg1 = "1->9->2->7";
		String arg2 = "5->9->0";
		String[] data1 = arg1.split("->");
		Num n1 = new Num();
		for (int i = data1.length-1; i >-1; i--) {
			n1.append((byte) Integer.parseInt(data1[i]));
		}
		String[] data2 = arg2.split("->");
		Num n2 = new Num();
		for (int i = data2.length-1; i > -1; i--) {
			n2.append((byte) Integer.parseInt(data2[i]));
		}
		Num rst = n1.add(n2);
		System.out.println(rst.toString());
	}

}
