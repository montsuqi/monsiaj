package org.montsuqi.client;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import org.montsuqi.util.ByteOrder;

public class LittleEndianDataOutputStream extends OutputStream implements DataOutput {

	protected DataOutputStream dos;

	public LittleEndianDataOutputStream(OutputStream os) {
		super();
		this.dos = new DataOutputStream(os);
	}

	public void close() throws IOException {
		dos.close();
	}

	public void flush() throws IOException {
		dos.flush();
	}

	public void write(byte[] b) throws IOException {
		dos.write(b);
	}

	public void write(byte[] b, int off, int len) throws IOException {
		dos.write(b, off, len);
	}

	public void write(int b) throws IOException {
		dos.write(b);
	}
		
	public void writeBoolean(boolean v) throws IOException {
		dos.writeBoolean(v);
	}

	public void writeByte(int b) throws IOException {
		dos.writeByte(b);
	}

	public void writeBytes(String s) throws IOException {
		dos.writeBytes(s);
	}
	
	public void writeChar(int c) throws IOException {
		dos.writeChar(ByteOrder.reverse(c));
	}

	public void writeChars(String s) throws IOException {
		char[] chars = s.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			dos.writeChar(chars[i]);
		}
	}
		
	public void writeDouble(double d) throws IOException {
		long bits = Double.doubleToLongBits(d);
		dos.writeDouble(Double.longBitsToDouble(ByteOrder.reverse(bits)));
	}

	public void writeFloat(float f) throws IOException {
		int bits = Float.floatToIntBits(f);
		dos.writeFloat(Float.intBitsToFloat(ByteOrder.reverse(bits)));
	}

	public void writeInt(int i) throws IOException {
		dos.writeInt(ByteOrder.reverse(i));
	}

	public void writeLong(long l) throws IOException {
		dos.writeLong(ByteOrder.reverse(l));
	}

	public void writeShort(int s) throws IOException {
		dos.writeShort(ByteOrder.reverse((short)s));
	}

	public void writeUTF(String s) throws IOException {
		dos.writeUTF(s);
	}
}
