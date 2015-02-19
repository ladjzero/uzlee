package com.ladjzero.hipda;

import java.util.ArrayList;
import java.util.Date;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "post")
public class Post{
	@DatabaseField(id = true)
	private int id;
	@DatabaseField(canBeNull = false, foreign = true, columnName = "uid")
	private User author;
	
	@DatabaseField
	private boolean isNew;
	@DatabaseField
	private int commentCount;
	
	private Date createDate;
	@DatabaseField
	private String title;
	@DatabaseField
	private String body;
	private int tid;
	private String[] niceBody;
	private String type;
	private ArrayList<Post> comments;
	private int replyTo;
	private int fid;
	private String timeStr;
	private String editTimeStr;
	private int postIndex;
	private Post quote;
	
	public User getAuthor() {
		return author;
	}
	public Post setAuthor(User author) {
		this.author = author;
		return this;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public Post setCreateDate(Date createDate) {
		this.createDate = createDate;
		return this;
	}
	public String getTitle() {
		return title;
	}
	public Post setTitle(String title) {
		this.title = title;
		return this;
	}
	public String getBody() {
		return body;
	}
	public Post setBody(String body) {
		this.body = body;
		return this;
	}
	public Post setNiceBody(String[] body) {
		this.niceBody = body;
		return this;
	}
	public String[] getNiceBody() {
		return niceBody;
	}
	public String getType() {
		return type;
	}
	public Post setType(String type) {
		this.type = type;
		return this;
	}
	public ArrayList<Post> getComments() {
		return comments;
	}
	public Post setComments(ArrayList<Post> comments) {
		this.comments = comments;
		return this;
	}
	public boolean isNew() {
		return isNew;
	}
	public Post setNew(boolean isNew) {
		this.isNew = isNew;
		return this;
	}
	public int getId() {
		return id;
	}
	public Post setId(int id) {
		this.id = id;
		return this;
	}
	public int getTid() {return tid;}
	public Post setTid(int tid) {
		this.tid = tid;
		return this;
	}
	public int getCommentCount() {
		return commentCount;
	}
	public Post setCommentCount(int commentCount) {
		this.commentCount = commentCount;
		return this;
	}
	public int getReplyTo() {
		return replyTo;
	}
	public Post setReplyTo(int replyTo) {
		this.replyTo = replyTo;
		return this;
	}

	public int getFid() {
		return fid;
	}

	public Post setFid(int fid) {
		this.fid = fid;
		return this;
	}

	public String getTimeStr() {
		return timeStr;
	}

	public Post setTimeStr(String timeStr) {
		this.timeStr = timeStr;
		return this;
	}

	public String getEditTimeStr() {
		return editTimeStr;
	}

	public Post setEditTimeStr(String editTimeStr) {
		this.editTimeStr = editTimeStr;
		return this;
	}

	public int getPostIndex() {
		return postIndex;
	}

	public Post setPostIndex(int postIndex) {
		this.postIndex = postIndex;
		return this;
	}

	public Post getQuote() {
		return quote;
	}

	public Post setQuote(Post quote) {
		this.quote = quote;
		return this;
	}
}
