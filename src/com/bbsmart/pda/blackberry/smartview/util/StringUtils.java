package com.bbsmart.pda.blackberry.smartview.util;

public final class StringUtils {
	public static int indexOfIgnoreCase(String str1, String str2, int fromIndex) {
		if (str1 == null || str2 == null) {
			return -1;
		}

		if (fromIndex < 0) {
			fromIndex = 0;
		}

		int str1Len = str1.length(), str2Len = str2.length();
		int limit = str1Len - str2Len;
		for (; fromIndex <= limit; fromIndex++) {
			if (str1.regionMatches(true, fromIndex, str2, 0, str2Len)) {
				return fromIndex;
			}
		}

		return -1;
	}

	public static String replace(String _text, String _searchStr,
			String _replacementStr) {
		// String buffer to store str
		StringBuffer sb = new StringBuffer();

		// Search for search
		int searchStringPos = _text.indexOf(_searchStr);
		int startPos = 0;
		int searchStringLength = _searchStr.length();

		// Iterate to add string
		while (searchStringPos != -1) {
			sb.append(_text.substring(startPos, searchStringPos)).append(
					_replacementStr);
			startPos = searchStringPos + searchStringLength;
			searchStringPos = _text.indexOf(_searchStr, startPos);
		}

		// Create string
		sb.append(_text.substring(startPos, _text.length()));

		return sb.toString();
	}

	/**
	 * Removes all instances of toRemove from text irrespective of case
	 * 
	 * @param text
	 * @param toRemove
	 * @return
	 */
	public static String removeIgnoreCase(String text, String toRemove) {
		// String buffer to store str
		StringBuffer sb = new StringBuffer();

		// Search for search
		int searchStringPos = indexOfIgnoreCase(text, toRemove, 0);
		int toRemoveLength = toRemove.length();
		int startPos = 0;

		// Iterate to add string
		while (searchStringPos != -1) {
			sb.append(text.substring(startPos, searchStringPos));
			startPos = searchStringPos + toRemoveLength;
			searchStringPos = indexOfIgnoreCase(text, toRemove, startPos);
		}

		// Create string
		sb.append(text.substring(startPos, text.length()));

		return sb.toString();
	}

	public static String replace(String _text, char _searchChr,
			String _replacementStr) {
		// String buffer to store str
		StringBuffer sb = new StringBuffer();

		// Search for search
		int searchStringPos = _text.indexOf(_searchChr);
		int startPos = 0;

		// Iterate to add string
		while (searchStringPos != -1) {
			sb.append(_text.substring(startPos, searchStringPos)).append(
					_replacementStr);
			startPos = searchStringPos + 1;
			searchStringPos = _text.indexOf(_searchChr, startPos);
		}

		// Create string
		sb.append(_text.substring(startPos, _text.length()));

		return sb.toString();
	}
}
