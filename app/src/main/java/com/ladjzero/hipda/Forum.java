package com.ladjzero.hipda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by chenzhuo on 15-9-19.
 */
public class Forum {
	private int fid;
	private String name;

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	private String alias;
	private ArrayList<Forum> children;
	private ArrayList<Type> types;

	public Type getCurrentType() {
		return currentType;
	}

	public void setCurrentType(Type currentType) {
		this.currentType = currentType;
	}

	private Type currentType;

	public static Forum findById(List<Forum> forums, int fid) {
		List<Forum> ret = findByIds(forums, new ArrayList<>(Arrays.asList(fid)));

		return ret.size() == 0 ? null : ret.get(0);
	}

	public static List<Forum> findByIds(List<Forum> forums, Collection<Integer> fids) {
		ArrayList<Forum> ret = new ArrayList<>();

		List<Forum> forums2 = flatten(forums);

		for (int fid : fids) {
			Forum f = null;

			for (Forum _f : forums2) {
				if (_f.getFid() == fid) {
					f = _f;
					break;
				}
			}

			if (f != null) {
				ret.add(f);
			}
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


	public int getFid() {
		return fid;
	}

	public Forum setFid(int fid) {
		this.fid = fid;
		return this;
	}

	public String getName() {
		return name;
	}

	public Forum setName(String name) {
		this.name = name;
		return this;
	}

	public ArrayList<Forum> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<Forum> children) {
		this.children = children;
	}

	public ArrayList<Type> getTypes() {
		return types;
	}

	public void setTypes(ArrayList<Type> types) {
		this.types = types;
	}

	@Override
	public String toString() {
		if (alias == null) {
			return name;
		} else {
			return alias;
		}
	}

	public static class Type {
		private int id;
		private String name;

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
