package org.montsuqi.client;

public interface PacketClass {
	public static final int Null = 0x00;
	public static final int Connect = 0x01;
	public static final int QueryScreen = 0x02;
	public static final int GetScreen = 0x03;
	public static final int GetData = 0x04;
	public static final int Event = 0x05;
	public static final int ScreenData = 0x06;
	public static final int ScreenDefine = 0x07;
	public static final int WindowName = 0x08;
	public static final int FocusName = 0x09;
	public static final int Auth = 0x0A;
	public static final int Name = 0x0B;

	public static final int OK = 0x80;
	public static final int END = 0x81;
	public static final int NOT = 0x83;

	public static final int E_VERSION = 0xF1;
	public static final int E_AUTH = 0xF2;
	public static final int E_APPL = 0xF3;
}
