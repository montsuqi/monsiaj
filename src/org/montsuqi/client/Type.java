package org.montsuqi.client;

public interface Type {
	final int NULL = 0x00;
	final int INT = 0x10;
	final int BOOL = 0x11;
	final int FLOAT = 0x20;
	final int CHAR = 0x30;
	final int TEXT = 0x31;
	final int VARCHAR = 0x32;
	final int BYTE = 0x40;
	final int NUMBER = 0x50;
	final int DBCODE = 0x60;
	final int OBJECT = 0x61;
	final int STRUCTURE = 0x80;
	final int ARRAY = 0x90;
	final int RECORD = 0xA0;
}
