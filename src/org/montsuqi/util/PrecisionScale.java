package org.montsuqi.util;

public class PrecisionScale {
	/* The hardcoded limits and defaults of the numeric data type */
	public static final int MAX_PRECISION = 1000;
	public static final int DEFAULT_PRECISION = 30;
	public static final int DEFAULT_SCALE = 6;

	public final int precision;
	public final int scale;

	public PrecisionScale() {
		precision = DEFAULT_PRECISION;
		scale =  DEFAULT_SCALE;
	}

	public PrecisionScale(String format) {
		if (format == null) {
			precision = DEFAULT_PRECISION;
			scale = DEFAULT_SCALE;
		} else {
			int pr = 0;
			int sc = 0;
			boolean fSmall = false;
			boolean fSign = false;
			char[] f = format.toCharArray();
			for (int i = 0; i < f.length; i++) {
				switch (f[i]) {
				case '-': case '+':
					if ( ! fSmall) {
						fSign = true;
					}
				case 'Z': case '\\': case '9':
					if (fSmall) {
						sc++;
					}
					pr++;
					break;
				case '.':
					fSmall = true;
					break;
				default:
					break;
				}
			}
			if (fSign) {
				pr--;
			}
			precision = pr;
			scale = sc;
		}
	}
}


