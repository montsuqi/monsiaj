package org.montsuqi.client;

public interface Type {
	public final int NULL = 0x00;
	public final int INT = 0x10;
	public final int BOOL = 0x11;
	public final int FLOAT = 0x20;
	public final int CHAR = 0x30;
	public final int TEXT = 0x31;
	public final int VARCHAR = 0x32;
	public final int BYTE = 0x40;
	public final int NUMBER = 0x50;
	public final int DBCODE = 0x60;
	public final int OBJECT = 0x61;
	public final int STRUCTURE = 0x80;
	public final int ARRAY = 0x90;
	public final int RECORD = 0xA0;
}
