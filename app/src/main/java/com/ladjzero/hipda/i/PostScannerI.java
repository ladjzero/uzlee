package com.ladjzero.hipda.i;

import java.util.ArrayList;

import com.ladjzero.hipda.Post;

public interface PostScannerI{
	int getPageCount();
	ArrayList<Post> getPageAt(int p);
}
