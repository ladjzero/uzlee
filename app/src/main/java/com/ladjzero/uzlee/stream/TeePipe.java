package com.ladjzero.uzlee.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by R9NKCC3 on 2016/2/22.
 */
public class TeePipe {
	public static int stream(InputStream in, OutputStream... outs) {
		int length = 0;

		byte[] buffer = new byte[1024];

		try {
			int len = in.read(buffer);

			while (len != -1) {
				for (OutputStream out : outs) {
					out.write(buffer, 0, len);
				}

				length += len;
				len = in.read(buffer);
			}

			return length;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			for (OutputStream out : outs) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
