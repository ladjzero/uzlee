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
}
