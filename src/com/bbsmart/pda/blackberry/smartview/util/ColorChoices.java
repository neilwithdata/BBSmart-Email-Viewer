package com.bbsmart.pda.blackberry.smartview.util;

import net.rim.device.api.ui.Color;

public class ColorChoices {
	private static Object[] colorChoices = null;

	public static Object[] getColorChoices() {
		if (colorChoices != null) {
			return colorChoices;
		}
		colorChoices = new Object[] {
				new ColorChoice(Color.WHITE, "White"),
				new ColorChoice(Color.ALICEBLUE, "Alice Blue"),
				new ColorChoice(Color.ANTIQUEWHITE, "Antique White"),
				new ColorChoice(Color.AQUA, "Aqua"),
				new ColorChoice(Color.AQUAMARINE, "Aquamarine"),
				new ColorChoice(Color.AZURE, "Azure"),
				new ColorChoice(Color.BEIGE, "Beige"),
				new ColorChoice(Color.BISQUE, "Bisque"),
				new ColorChoice(Color.BLACK, "Black"),
				new ColorChoice(Color.BLANCHEDALMOND, "Blanched Almond"),
				new ColorChoice(Color.BLUE, "Blue"),
				new ColorChoice(Color.BLUEVIOLET, "Blue Violet"),
				new ColorChoice(Color.BROWN, "Brown"),
				new ColorChoice(Color.BURLYWOOD, "Burlywood"),
				new ColorChoice(Color.CADETBLUE, "Cadet Blue"),
				new ColorChoice(Color.CHARTREUSE, "Chartreuse"),
				new ColorChoice(Color.CHOCOLATE, "Chocolate"),
				new ColorChoice(Color.CORAL, "Coral"),
				new ColorChoice(Color.CORNFLOWERBLUE, "Cornflower Blue"),
				new ColorChoice(Color.CORNSILK, "Cornsilk"),
				new ColorChoice(Color.CRIMSON, "Crimson"),
				new ColorChoice(Color.CYAN, "Cyan"),
				new ColorChoice(Color.DARKBLUE, "Dark Blue"),
				new ColorChoice(Color.DARKCYAN, "Dark Cyan"),
				new ColorChoice(Color.DARKGOLDENROD, "Dark Goldenrod"),
				new ColorChoice(Color.DARKGRAY, "Dark Gray"),
				new ColorChoice(Color.DARKGREEN, "Dark Green"),
				new ColorChoice(Color.DARKKHAKI, "Dark Khaki"),
				new ColorChoice(Color.DARKMAGENTA, "Dark Magenta"),
				new ColorChoice(Color.DARKOLIVEGREEN, "Dark Olive Green"),
				new ColorChoice(Color.DARKORANGE, "Dark Orange"),
				new ColorChoice(Color.DARKORCHID, "Dark Orchid"),
				new ColorChoice(Color.DARKRED, "Dark Red"),
				new ColorChoice(Color.DARKSALMON, "Dark Salmon"),
				new ColorChoice(Color.DARKSEAGREEN, "Dark Sea Green"),
				new ColorChoice(Color.DARKSLATEBLUE, "Dark Slate Blue"),
				new ColorChoice(Color.DARKSLATEGRAY, "Dark Slate Gray"),
				new ColorChoice(Color.DARKTURQUOISE, "Dark Turquoise"),
				new ColorChoice(Color.DARKVIOLET, "Dark Violet"),
				new ColorChoice(Color.DEEPPINK, "Deep Pink"),
				new ColorChoice(Color.DEEPSKYBLUE, "Deep Sky Blue"),
				new ColorChoice(Color.DIMGRAY, "Dim Gray"),
				new ColorChoice(Color.DODGERBLUE, "Dodger Blue"),
				new ColorChoice(Color.FIREBRICK, "Firebrick"),
				new ColorChoice(Color.FLORALWHITE, "Floral White"),
				new ColorChoice(Color.FORESTGREEN, "Forest Green"),
				new ColorChoice(Color.FUCHSIA, "Fuchsia"),
				new ColorChoice(Color.GAINSBORO, "Gainsboro"),
				new ColorChoice(Color.GHOSTWHITE, "Ghost White"),
				new ColorChoice(Color.GOLD, "Gold"),
				new ColorChoice(Color.GOLDENROD, "Goldenrod"),
				new ColorChoice(Color.GRAY, "Gray"),
				new ColorChoice(Color.GREEN, "Green"),
				new ColorChoice(Color.GREENYELLOW, "Green Yellow"),
				new ColorChoice(Color.HONEYDEW, "Honeydew"),
				new ColorChoice(Color.HOTPINK, "Hot Pink"),
				new ColorChoice(Color.INDIANRED, "Indian Red"),
				new ColorChoice(Color.INDIGO, "Indigo"),
				new ColorChoice(Color.IVORY, "Ivory"),
				new ColorChoice(Color.KHAKI, "Khaki"),
				new ColorChoice(Color.LAVENDER, "Lavender"),
				new ColorChoice(Color.LAVENDERBLUSH, "Lavender Blush"),
				new ColorChoice(Color.LAWNGREEN, "Lawn Green"),
				new ColorChoice(Color.LEMONCHIFFON, "Lemon Chiffon"),
				new ColorChoice(Color.LIGHTBLUE, "Light Blue"),
				new ColorChoice(Color.LIGHTCORAL, "Light Coral"),
				new ColorChoice(Color.LIGHTCYAN, "Light Cyan"),
				new ColorChoice(Color.LIGHTGOLDENRODYELLOW,
						"Light Goldenrod Yellow"),
				new ColorChoice(Color.LIGHTGREEN, "Light Green"),
				new ColorChoice(Color.LIGHTGREY, "Light Grey"),
				new ColorChoice(Color.LIGHTPINK, "Light Pink"),
				new ColorChoice(Color.LIGHTSALMON, "Light Salmon"),
				new ColorChoice(Color.LIGHTSEAGREEN, "Light Sea Green"),
				new ColorChoice(Color.LIGHTSKYBLUE, "Light Sky Blue"),
				new ColorChoice(Color.LIGHTSLATEGRAY, "Light Slate Gray"),
				new ColorChoice(Color.LIGHTSTEELBLUE, "Light Steel Blue"),
				new ColorChoice(Color.LIGHTYELLOW, "Light Yellow"),
				new ColorChoice(Color.LIME, "Lime"),
				new ColorChoice(Color.LIMEGREEN, "Lime Green"),
				new ColorChoice(Color.LINEN, "Linen"),
				new ColorChoice(Color.MAGENTA, "Magenta"),
				new ColorChoice(Color.MAROON, "Maroon"),
				new ColorChoice(Color.MEDIUMAQUAMARINE, "Medium Aquamarine"),
				new ColorChoice(Color.MEDIUMBLUE, "Medium Blue"),
				new ColorChoice(Color.MEDIUMORCHID, "Medium Orchid"),
				new ColorChoice(Color.MEDIUMPURPLE, "Medium Purple"),
				new ColorChoice(Color.MEDIUMSEAGREEN, "Medium Sea Green"),
				new ColorChoice(Color.MEDIUMSLATEBLUE, "Medium Slate Blue"),
				new ColorChoice(Color.MEDIUMSPRINGGREEN, "Medium Spring Green"),
				new ColorChoice(Color.MEDIUMTURQUOISE, "Medium Turquoise"),
				new ColorChoice(Color.MEDIUMVIOLETRED, "Medium Violet Red"),
				new ColorChoice(Color.MIDNIGHTBLUE, "Midnight Blue"),
				new ColorChoice(Color.MINTCREAM, "Mint Cream"),
				new ColorChoice(Color.MISTYROSE, "Misty Rose"),
				new ColorChoice(Color.MOCCASIN, "Moccasin"),
				new ColorChoice(Color.NAVAJOWHITE, "Navajo White"),
				new ColorChoice(Color.NAVY, "Navy"),
				new ColorChoice(Color.OLDLACE, "Old Lace"),
				new ColorChoice(Color.OLIVE, "Olive"),
				new ColorChoice(Color.OLIVEDRAB, "Olive Drab"),
				new ColorChoice(Color.ORANGE, "Orange"),
				new ColorChoice(Color.ORANGERED, "Orange Red"),
				new ColorChoice(Color.ORCHID, "Orchid"),
				new ColorChoice(Color.PALEGOLDENROD, "Pale Goldenrod"),
				new ColorChoice(Color.PALEGREEN, "Pale Green"),
				new ColorChoice(Color.PALETURQUOISE, "Pale Turquoise"),
				new ColorChoice(Color.PALEVIOLETRED, "Pale Violet Red"),
				new ColorChoice(Color.PAPAYAWHIP, "Papaya Whip"),
				new ColorChoice(Color.PEACHPUFF, "Peach Puff"),
				new ColorChoice(Color.PERU, "Peru"),
				new ColorChoice(Color.PINK, "Pink"),
				new ColorChoice(Color.PLUM, "Plum"),
				new ColorChoice(Color.POWDERBLUE, "Powder Blue"),
				new ColorChoice(Color.PURPLE, "Purple"),
				new ColorChoice(Color.RED, "Red"),
				new ColorChoice(Color.ROSYBROWN, "Rosy Brown"),
				new ColorChoice(Color.ROYALBLUE, "Royal Blue"),
				new ColorChoice(Color.SADDLEBROWN, "Saddle Brown"),
				new ColorChoice(Color.SALMON, "Salmon"),
				new ColorChoice(Color.SANDYBROWN, "Sandy Brown"),
				new ColorChoice(Color.SEAGREEN, "Sea Green"),
				new ColorChoice(Color.SEASHELL, "Seashell"),
				new ColorChoice(Color.SIENNA, "Sienna"),
				new ColorChoice(Color.SILVER, "Silver"),
				new ColorChoice(Color.SKYBLUE, "Sky Blue"),
				new ColorChoice(Color.SLATEBLUE, "Slate Blue"),
				new ColorChoice(Color.SLATEGRAY, "Slate Gray"),
				new ColorChoice(Color.SNOW, "Snow"),
				new ColorChoice(Color.SPRINGGREEN, "Spring Green"),
				new ColorChoice(Color.STEELBLUE, "Steel Blue"),
				new ColorChoice(Color.TAN, "Tan"),
				new ColorChoice(Color.TEAL, "Teal"),
				new ColorChoice(Color.THISTLE, "Thistle"),
				new ColorChoice(Color.TOMATO, "Tomato"),
				new ColorChoice(Color.TURQUOISE, "Turquoise"),
				new ColorChoice(Color.VIOLET, "Violet"),
				new ColorChoice(Color.WHEAT, "Wheat"),
				new ColorChoice(Color.WHITESMOKE, "White Smoke"),
				new ColorChoice(Color.YELLOW, "Yellow"),
				new ColorChoice(Color.YELLOWGREEN, "Yellow Green") };

		return colorChoices;
	}

	public static class ColorChoice {
		private int colorValue = -1;

		private String name = null;

		public ColorChoice(int aValue, String aName) {
			name = aName;
			colorValue = aValue;
		}

		public int getColorValue() {
			return colorValue;
		}

		public String getName() {
			return name;
		}

		public String toString() {
			return name;
		}
	}

	public static String getColorText(int colorValue) {
		Object[] colorChoices = getColorChoices();
		int numChoices = colorChoices.length;
		for (int i = 0; i < numChoices; i++) {
			ColorChoice choice = (ColorChoice) colorChoices[i];
			if (choice.getColorValue() == colorValue) {
				return choice.getName();
			}
		}
		return "NO COLOR";
	}
}