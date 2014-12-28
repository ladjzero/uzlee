package com.ladjzero.hipda;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DBHelper extends OrmLiteSqliteOpenHelper {

	static String DB_NAME = "uzlee";
	static int DB_V = 1;
	
	private Dao<Thread, Integer> threadDao = null;
	private Dao<Post, Integer> postDao = null;
	private Dao<User, Integer> userDao = null;

	public DBHelper(Context context) {
		super(context, DB_NAME, null, DB_V);
	}

	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			TableUtils.createTable(connectionSource, Thread.class);
			TableUtils.createTable(connectionSource, Post.class);
			TableUtils.createTable(connectionSource, User.class);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource,
			int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	public Dao<Thread, Integer> getThreadDao() throws SQLException {
		if (threadDao == null) {
			threadDao = getDao(Thread.class);
		}

		return threadDao;
	}
	
	public Dao<Post, Integer> getPostDao() throws SQLException {
		if (postDao == null) {
			postDao = getDao(Post.class);
		}

		return postDao;
	}

	public Dao<User, Integer> getUserDao() throws SQLException {
		if (userDao == null) {
			userDao = getDao(User.class);
		}

		return userDao;
	}

	@Override
	public void close() {
		super.close();
		postDao = null;
		userDao = null;
	}

}
