package com.ladjzero.hipda;

import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "user")
public class User{
	@DatabaseField(id = true)
	private int id;
	@DatabaseField
	private String name;
	private Date creatDate;
	@DatabaseField
	private String image;
	@DatabaseField
	private String grade;

	private String registerDateStr;
	private String qq;
	private String sex;
	private String totalThreads;
	private String level;
	private String points;
	
	public String getName() {
		return name;
	}
	public User setName(String name) {
		this.name = name;
		return this;
	}
	public Date getCreatDate() {
		return creatDate;
	}
	public User setCreatDate(Date creatDate) {
		this.creatDate = creatDate;
		return this;
	}
	public String getImage() {
		return image;
	}
	public User setImage(String image) {
		this.image = image;
		return this;
	}
	public String getGrade() {
		return grade;
	}
	public User setGrade(String grade) {
		this.grade = grade;
		return this;
	}
	public int getId() {
		return id;
	}
	public User setId(int id) {
		this.id = id;
		return this;
	}

	public String getRegisterDateStr() {
		return registerDateStr;
	}

	public User setRegisterDateStr(String registerDateStr) {
		this.registerDateStr = registerDateStr;
		return this;
	}

	public String getQq() {
		return qq;
	}

	public User setQq(String qq) {
		this.qq = qq;
		return this;
	}

	public String getSex() {
		return sex;
	}

	public User setSex(String sex) {
		this.sex = sex;
		return this;
	}

	public String getTotalThreads() {
		return totalThreads;
	}

	public User setTotalThreads(String totalThreads) {
		this.totalThreads = totalThreads;
		return this;
	}

	public String getLevel() {
		return level;
	}

	public User setLevel(String level) {
		this.level = level;
		return this;
	}

	public String getPoints() {
		return points;
	}

	public User setPoints(String points) {
		this.points = points;
		return this;
	}
}
