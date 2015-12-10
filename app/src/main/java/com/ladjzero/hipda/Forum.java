package com.ladjzero.hipda;

import android.content.Context;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by chenzhuo on 15-9-19.
 */
public class Forum {
	public static List<Forum> buildFromJSON(String json) {
		List<Forum> forums = JSON.parseArray(json, Forum.class);
		addALLType(forums);

		return forums;
	}

	public static List<Forum> buildFromJSON(Context context) {
		return buildFromJSON(Utils.readAssetFile(context, "hipda.json"));
	}

	public static Forum findById(List<Forum> forums, int fid) {
		List<Forum> ret = findByIds(forums, new ArrayList<Integer>(Arrays.asList(fid)));

		return ret.size() == 0 ? null : ret.get(0);
	}

	public static List<Forum> findByIds(List<Forum> forums, Collection<Integer> fids) {
		ArrayList<Forum> ret = new ArrayList<>();

		for (Forum f : forums) {
			List<Forum> children = f.getChildren();

			if (fids.contains(f.getFid())) ret.add(f);
			if (children != null) ret.addAll(findByIds(children, fids));
		}

		return ret;
	}

	public static List<Forum> flatten(List<Forum> forums) {
		List<Forum> ret = new ArrayList<>();

		for (Forum f : forums) {
			ret.add(f);

			List<Forum> children = f.getChildren();

			if (children != null) {
				ret.addAll(flatten(children));
			}
		}

		return ret;
	}

	private static void addALLType(List<Forum> forums) {
		Type all = new Type();
		all.setId(-1);
		all.setName("所有类别");

		for (Forum f : forums) {
			List<Type> types = f.getTypes();
			List<Forum> children = f.getChildren();

			if (types != null) types.add(0, all);
			if (children != null) addALLType(children);
		}
	}

	public int getFid() {
		return fid;
	}

	public void setFid(int fid) {
		this.fid = fid;
	}

	private int fid;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private String name;

	public ArrayList<Forum> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<Forum> children) {
		this.children = children;
	}

	private ArrayList<Forum> children;

	public ArrayList<Type> getTypes() {
		return types;
	}

	public void setTypes(ArrayList<Type> types) {
		this.types = types;
	}

	private ArrayList<Type> types;

	@Override
	public String toString() {
		return name;
	}

	public static class Type {
		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		private int id;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		private String name;

		@Override
		public String toString() {
			return name;
		}
	}
}
