package com.ladjzero.hipda.entities;

import java.util.Date;

public class User {
	private int id;
	private String name;
	private Date creatDate;
	private String image;
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
		if (image == null) {
			int avatar0 = id / 10000;
			int avatar1 = (id % 10000) / 100;
			int avatar2 = id % 100;

			image = String.format("http://www.hi-pda.com/forum/uc_server/data/avatar/000/%02d/%02d/%02d_avatar_middle.jpg", avatar0, avatar1, avatar2);
		}

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

	@Override
	public boolean equals(Object o) {
		if (o instanceof User) {
			return id == ((User) o).getId();
		}

		return false;
	}
}
