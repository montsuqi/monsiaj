package org.montsuqi.util;

public class ByteOrder {

	// inhibit instantiation
	private ByteOrder() {
	}

	public static long reverse(long l) {
		return
			(((l >> 56) & 0xff) <<  0) |
			(((l >> 48) & 0xff) <<  8) |
			(((l >> 40) & 0xff) << 16) |
			(((l >> 32) & 0xff) << 24) |
			(((l >> 24) & 0xff) << 32) |
			(((l >> 16) & 0xff) << 40) |
			(((l >>  8) & 0xff) << 48) |
			(((l >>  0) & 0xff) << 56);
	}

	public static int reverse(int i) {
		return
			(((i >> 24) & 0xff) <<  0) |
			(((i >> 16) & 0xff) <<  8) |
			(((i >>  8) & 0xff) << 16) |
			(((i >>  0) & 0xff) << 24);
	}

	public static short reverse(short s) {
		return (short)
			((((s >> 8) & 0xff) << 0) |
			(((s >> 0) & 0xff) << 8));
	}
	
	public static char reverse(char c) {
		return (char)
			((((c >> 8) & 0xff) << 0) |
			(((c >> 0) & 0xff) << 8));
	}

	public static double reverse(double d) {
		return Double.longBitsToDouble(reverse(Double.doubleToLongBits(d)));
	}

	public static float reverse(float f) {
		return Float.intBitsToFloat(reverse(Float.floatToIntBits(f)));
	}
}
