package com.bbsmart.pda.blackberry.smartview.util;

public final class Tokenizer {
	protected final String line;
	protected final int len;

	protected int pos;
	protected String token;

	public Tokenizer(String nline) {
		line = nline;
		len = line.length();
		pos = 0;
		token = "";
	}

	public boolean hasNext() {
		while (pos < len && here() == ' ')
			pos++;

		int sublen = 0;
		while (pos < len && here() != ' ') {
			pos++;
			sublen++;
		}

		token = line.substring(pos - sublen, pos);
		return sublen > 0;
	}

	public String next() {
		return token;
	}

	protected char here() {
		return line.charAt(pos);
	}
}
