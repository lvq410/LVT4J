package com.lvt4j.test;

import java.util.Date;

public class Person extends Entity{

	private String title;
	
	
	public byte[] getBb() {
		return bb;
	}

	public void setBb(byte[] bb) {
		this.bb = bb;
	}

	public boolean isOk() {
		return isOk;
	}

	public void setOk(boolean isOk) {
		this.isOk = isOk;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	private String name;

	private Date date;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private boolean isOk;
	private byte[] bb;

}
