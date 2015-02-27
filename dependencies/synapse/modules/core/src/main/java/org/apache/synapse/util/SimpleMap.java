package org.apache.synapse.util;

import java.util.Map;
/*
 * This interface is a helper for dealing with a restriction on Map messages 
 * The key MUST be a String
 * and the value MUST be one of
 * boolean, char, String, int, double, float, long, short, byte, byte[] (Consider adding DataHandler in future)
 * or the equivalent Object
 * The simple type putters are equivalent to using an Object
 * so 
 * put("paul", new Integer(38));
 * getInt("paul"); returns 38
 */

public interface SimpleMap extends Map {
	
	public Object get(String name);
	public void put(String name, Object value);
	public boolean getBoolean(String name);
	public void putBoolean(String name, boolean b);
	public String getString(String value);
	public void putString(String name, String value);
	public char getChar(String name);
	public void putChar(String name, char c);
	public int getInt(String name);
	public void putInt(String name, int i);
	public short getShort(String name);
	public void putShort(String name, short s);
	public float getFloat(String name);
	public void putFloat(String name, float fl);
	public double getDouble(String name);
	public void putDouble(String name, double d);
	public long getLong(String name);
	public void putLong(String name, long l);
	public byte getByte(String name);
	public void putByte(String name, byte b);
	public byte[] getBytes(String name);
	public void putBytes(String name, byte[] bytes);
	

}
