package com.bbsmart.pda.blackberry.smartview.util;

public final class RPNString {
	protected final String string;

	public RPNString(String nstring) {
		string = nstring;
	}

	public int apply(int c, int i, int key) {
		RPNState state = new RPNState();

		Tokenizer tokens = new Tokenizer(string);
		while (tokens.hasNext())
			state.apply(tokens.next(), c, i, key);

		if (state.size() == 0)
			return 0;

		return state.pop();
	}

	public String apply(String name) {
		name = toTen(name);

		int key = 0;

		for (int i = 0; i < name.length(); i++) {
			int c = name.charAt(i) & 0xFF;
			key = apply(c, i, key);
		}

		return padInt(key & 0xFFFF);
	}

	public static String toTen(String name) {
		int len = name.length();

		if (len <= 10)
			return name;

		String left = name.substring(0, 5);
		String right = name.substring(len - 5, len);
		return left + right;
	}

	public static String padInt(int val) {
		if (val == 0)
			return "00000";

		char[] out = new char[5];
		for (int i = 4; i >= 0; i--) {
			out[i] = (char) ('0' + (val % 10));
			val /= 10;
		}

		return new String(out);
	}

	public static String apply(String rpnstring, String name) {
		return new RPNString(rpnstring).apply(name);
	}
}
