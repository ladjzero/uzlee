package com.ladjzero.hipda.entities;

import java.util.ArrayList;
import java.util.Date;

public class Thread {
	private int id;
	private User author;

	private boolean isNew;
	private int commentCount;

	private Date createDate;
	private String title;
	private String body;
	private String type;
	private ArrayList<Post> comments;
	private int replyTo;
	private String dateStr;
	private int fid;
	private boolean isBold;
	private String color;
	private boolean isStick;
	private int toFind;

	public User getAuthor() {
		return author;
	}

	public Thread setAuthor(User author) {
		this.author = author;
		return this;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public Thread setCreateDate(Date createDate) {
		this.createDate = createDate;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public Thread setTitle(String title) {
		this.title = title;
		return this;
	}

	public String getBody() {
		return body;
	}

	public Thread setBody(String body) {
		this.body = body;
		return this;
	}

	public String getType() {
		return type;
	}

	public Thread setType(String type) {
		this.type = type;
		return this;
	}

	public ArrayList<Post> getComments() {
		return comments;
	}

	public Thread setComments(ArrayList<Post> comments) {
		this.comments = comments;
		return this;
	}

	public boolean isNew() {
		return isNew;
	}

	public Thread setNew(boolean isNew) {
		this.isNew = isNew;
		return this;
	}

	public int getId() {
		return id;
	}

	public Thread setId(int id) {
		this.id = id;
		return this;
	}

	public int getCommentCount() {
		return commentCount;
	}

	public Thread setCommentCount(int commentCount) {
		this.commentCount = commentCount;
		return this;
	}

	public int getReplyTo() {
		return replyTo;
	}

	public Thread setReplyTo(int replyTo) {
		this.replyTo = replyTo;
		return this;
	}

	public String getDateStr() {
		return dateStr;
	}

	public Thread setDateStr(String dateStr) {
		this.dateStr = dateStr;
		return this;
	}

	@Override
	public String toString() {
		return this.getTitle();
	}

	public int getFid() {
		return fid;
	}

	public Thread setFid(int fid) {
		this.fid = fid;
		return this;
	}

	public boolean getBold() {
		return isBold;
	}

	public Thread setBold(boolean isBold) {
		this.isBold = isBold;
		return this;
	}

	public String getColor() {
		return color;
	}

	public Thread setColor(String color) {
		this.color = color;
		return this;
	}

	public boolean isStick() {
		return isStick;
	}

	public Thread setStick(boolean isStick) {
		this.isStick = isStick;
		return this;
	}

	public int getToFind() {
		return toFind;
	}

	public Thread setToFind(int toFind) {
		this.toFind = toFind;
		return this;
	}
}
