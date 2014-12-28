package com.ladjzero.hipda;

import java.util.ArrayList;
import java.util.regex.Pattern;

import com.ladjzero.hipda.Core.GetHtmlCB;
import com.ladjzero.hipda.cb.UserStatsCB;
import com.ladjzero.hipda.cb.ScannerIsReadyCB;
import com.ladjzero.hipda.i.PostScannerI;

public class PostScanner implements PostScannerI{

	Thread t;
	UserStatsCB cb;
	String body;
	Core core;
	
	Pattern hasPage = Pattern.compile("<div class=\"pages\"><strong>", Pattern.DOTALL);
	
	public PostScanner(int tid, UserStatsCB cb, final ScannerIsReadyCB scb) {
		this.cb = cb;
		
		core = Core.getInstance(null);
		
		core.getHtml("http://www.hi-pda.com/forum/viewthread.php?tid=" + tid, new GetHtmlCB() {

			@Override
			public void onSuccess(String html) {
				body = html;
				scb.onReady();
			}
			
		});
	}
	
	@Override
	public int getPageCount() {
		return 1;
	}

	@Override
	public ArrayList<Post> getPageAt(int pno) {
		return core.parsePosts(body);
	}

}
