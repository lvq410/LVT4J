package com.lvt4j.test;

import java.io.Serializable;

public class Entity implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8201595473327214204L;
	public static String SIGN_WORD = "${word}";
	
	private int a;
	private Integer b;
	private String id;
	public static String replaceWord(String paraphrase,String word) {
		String lowP = paraphrase.toLowerCase();
		String lowW = word.toLowerCase();
		int i = -1;
		while ((i=lowP.indexOf(lowW))>-1) {
			paraphrase = paraphrase.substring(0, i)+SIGN_WORD+paraphrase.substring(i+word.length(), paraphrase.length());
			lowP = paraphrase.toLowerCase();
		}
		return paraphrase;
	}
	
	public static void main(String[] args) {
		String p = "N-COUNT A mass noun is a nounmass noun such as \"wine\" which is usually uncount but is used with \"a\" or \"an\" or used in the plural when it refers to types of that substance, as in \"a range of Australian wines.\" 集合名词";
		String w = "MASS Noun";
		System.out.println(replaceWord(p, w));
	}
public void setId(String id) {
	this.id = id;
}
@Override
public int hashCode() {
	// TODO Auto-generated method stub
	return id==null?0:id.hashCode();
}
public String getId() {
	return id;
}
@Override
public boolean equals(Object obj) {
	if (this==obj) {
		return true;
	}
	if (obj instanceof Entity) {
		return hashCode()==obj.hashCode();
	}
	return false;
}
}
