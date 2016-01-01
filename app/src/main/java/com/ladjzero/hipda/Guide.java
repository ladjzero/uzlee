package com.ladjzero.hipda;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import java.util.ArrayList;

/**
 * Created by chenzhuo on 15-8-13.
 */
public class Guide {
	public static final ArrayList<Topic> AllTopics;
	public static Stringify stringify;
	public static void setStringify(Stringify any) {
		stringify = any;
	}
	public static ArrayList<Topic> getAccessibleTopics() {
		User user = Core.getUser();

		return user != null && user.getId() > 0 ? Guide.AllTopics :
				(ArrayList<Guide.Topic>) CollectionUtils.select(Guide.AllTopics, new Predicate() {
					@Override
					public boolean evaluate(Object o) {
						return !((Guide.Topic) o).isHidden;
					}
				});
	}

	static {
		AllTopics = new ArrayList<>();
		Topic temp;

		AllTopics.add(Topic.newInstance("站务与公告", 5, null, true));
		AllTopics.add(temp = Topic.newInstance("Buy & Sell", 6, null, true));
		AllTopics.add(Topic.newInstance("已完成交易", 63, temp, true));
		AllTopics.add(temp = Topic.newInstance("Geek Talks", 7, null, false));
		AllTopics.add(Topic.newInstance("E-INK", 59, temp, false));
		AllTopics.add(Topic.newInstance("Joggler", 62, temp, false));
		AllTopics.add(Topic.newInstance("Smartphone", 9, null, false));
		AllTopics.add(Topic.newInstance("iPhone,iPod touch,iPad", 56, null, false));
		AllTopics.add(Topic.newInstance("Android,Chrome,Google", 60, null, false));
		AllTopics.add(temp = Topic.newInstance("PalmOS,Treo", 12, null, false));
		AllTopics.add(Topic.newInstance("Palm芝麻宝典", 40, temp, false));
		AllTopics.add(Topic.newInstance("WM,PPC,HPC", 14, null, false));
		AllTopics.add(Topic.newInstance("麦客爱苹果", 22, null, false));
		AllTopics.add(Topic.newInstance("DC,NB,MP3,Gadgets", 50, null, false));
		AllTopics.add(temp = Topic.newInstance("Discovery", 2, null, true));
		AllTopics.add(Topic.newInstance("改版建议", 65, temp, true));
		AllTopics.add(Topic.newInstance("只讨论2.0", 64, temp, true));
		AllTopics.add(Topic.newInstance("意欲蔓延", 24, null, true));
		AllTopics.add(Topic.newInstance("随笔与个人文集", 23, null, true));
		AllTopics.add(Topic.newInstance("吃喝玩乐", 25, null, true));
		AllTopics.add(Topic.newInstance("La Femme", 51, null, true));
		AllTopics.add(Topic.newInstance("疑似机器人", 57, null, true));
	}

	public static class Topic {
		public String title;
		public int fid;
		public Topic parent;
		public boolean isHidden;

		public static Topic newInstance(String title, int fid, Topic parent, boolean isHidden) {
			Topic topic = new Topic();

			topic.title = title;
			topic.fid = fid;
			topic.parent = parent;
			topic.isHidden = isHidden;

			return topic;
		}

		@Override
		public String toString() {
			return stringify == null ? super.toString() : stringify.stringify(this);
		}
	}

	public interface Stringify {
		String stringify(Topic topic);
	}
}
