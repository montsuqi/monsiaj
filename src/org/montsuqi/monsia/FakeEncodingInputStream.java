package org.montsuqi.monsia;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.montsuqi.util.Logger;

final class FakeEncodingInputStream extends InputStream {
	private InputStream in;
	private Logger logger;
	byte[] headerBytes;
	private int index;

	private static final String REAL_HEADER = "<?xml version=\"1.0\"?>";
	private static final String FAKE_HEADER = "<?xml version=\"1.0\" encoding=\"euc-jp\"?>";
	
	public FakeEncodingInputStream(InputStream in) throws IOException {
		this.in = in;
		in.skip(REAL_HEADER.length());
		try {
			headerBytes = FAKE_HEADER.getBytes("euc-jp");
		} catch (UnsupportedEncodingException e) {
			logger.fatal(e);
			e.printStackTrace();
		}
		index = 0;
	}

	public int read() throws IOException {
		int b;
		if (index < headerBytes.length) {
			b = headerBytes[index++];
			if (b < 0) {
				b = 0x100 + b;
			}
		} else {
			b = in.read();
		}
		return b;
	}
}
