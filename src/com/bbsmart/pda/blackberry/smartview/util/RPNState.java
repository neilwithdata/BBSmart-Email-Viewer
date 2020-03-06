package com.bbsmart.pda.blackberry.smartview.util;

public final class RPNState {
	protected final int[] stack;
	protected int pos;

	public RPNState() {
		// Hard limit! Bleh
		stack = new int[30];
		pos = 0;
	}

	public void push(int value) {
		stack[pos++] = value;
	}

	public int pop() {
		return stack[--pos];
	}

	public int size() {
		return pos;
	}

	public boolean apply(String term, int c, int i, int key) {
		if (pushVariable(term, c, i, key) || pushConstant(term) || applyOperator(term))
			return true;

		return false;
	}

	protected boolean pushVariable(String term, int c, int i, int key) {
		if (term.equals("c")) {
			push(c);
			return true;
		}

		if (term.equals("i")) {
			push(i);
			return true;
		}

		if (term.equals("key")) {
			push(key);
			return true;
		}

		return false;
	}

	protected boolean pushConstant(String term) {
		try {
			push(Integer.parseInt(term));
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}

	protected boolean applyOperator(String term) {
		if (size() >= 2 && applyBinaryOperator(term))
			return true;

		if (size() >= 1 && applyUnaryOperator(term))
			return true;

		return false;
	}

	protected boolean applyBinaryOperator(String op) {
		// Pop in reverse order
		int b = pop();
		int a = pop();
		int c = 0;

		// Arithmetic
		if (op.equals("+"))
			c = a + b;
		else if (op.equals("-"))
			c = a - b;
		else if (op.equals("*"))
			c = a * b;
		else if (op.equals("/"))
			c = a / b;
		else if (op.equals("%"))
			c = a % b;

		// Equality
		else if (op.equals("=="))
			c = asint(a == b);
		else if (op.equals(">="))
			c = asint(a >= b);
		else if (op.equals("<="))
			c = asint(a <= b);
		else if (op.equals("<"))
			c = asint(a < b);
		else if (op.equals(">"))
			c = asint(a > b);
		else if (op.equals("!="))
			c = asint(a != b);

		// Logic
		else if (op.equals("&&"))
			c = asint(asbool(a) && asbool(b));
		else if (op.equals("||"))
			c = asint(asbool(a) || asbool(b));

		// Bitwise
		else if (op.equals(">>"))
			c = a >> b;
		else if (op.equals("<<"))
			c = a << b;
		else if (op.equals("|"))
			c = a | b;
		else if (op.equals("&"))
			c = a & b;
		else if (op.equals("^"))
			c = a ^ b;

		// Or all failed
		else
			return false;

		// Push result
		push(c);
		return true;
	}

	public boolean applyUnaryOperator(String op) {
		int a = pop();
		int c = 0;

		if (op.equals("!"))
			c = asint(!asbool(a));
		else if (op.equals("~"))
			c = ~a;
		else
			return false;

		// Push result
		push(c);
		return true;
	}

	protected static boolean asbool(int val) {
		return val != 0;
	}

	protected static int asint(boolean res) {
		return res ? 1 : 0;
	}
}
