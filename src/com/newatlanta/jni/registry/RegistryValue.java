/*
 ** Java native interface to the Windows Registry API.
 ** 
 ** Authored by Timothy Gerard Endres
 ** <mailto:time@gjt.org>  <http://www.trustice.com>
 ** 
 ** This work has been placed into the public domain.
 ** You may use this work in any way and for any purpose you wish.
 **
 ** THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND,
 ** NOT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR
 ** OF THIS SOFTWARE, ASSUMES _NO_ RESPONSIBILITY FOR ANY
 ** CONSEQUENCE RESULTING FROM THE USE, MODIFICATION, OR
 ** REDISTRIBUTION OF THIS SOFTWARE. 
 ** 
 */

package com.newatlanta.jni.registry;

import java.io.PrintWriter;

/**
 * The RegistryValue class represents a value in the registry. This class is
 * abstract, so it can not be instantiated. The class is a superclass to all
 * value classes. The common abstract methods for getting and setting data must
 * be defined by the subclass, but subclasses will almost always provide
 * additional methods that get and set the value using the data type of the
 * subclass.
 * 
 * @version 3.1.3
 * 
 * @see com.newatlanta.jni.registry.Registry
 * @see com.newatlanta.jni.registry.RegistryKey
 */

abstract public class RegistryValue {
	public static final int REG_NONE = 0;

	public static final int REG_SZ = 1;

	public static final int REG_EXPAND_SZ = 2;

	public static final int REG_BINARY = 3;

	public static final int REG_DWORD = 4;

	public static final int REG_DWORD_LITTLE_ENDIAN = 4;

	public static final int REG_DWORD_BIG_ENDIAN = 5;

	public static final int REG_LINK = 6;

	public static final int REG_MULTI_SZ = 7;

	public static final int REG_RESOURCE_LIST = 8;

	public static final int REG_FULL_RESOURCE_DESCRIPTOR = 9;

	public static final int REG_RESOURCE_REQUIREMENTS_LIST = 10;

	protected static char[] hexChars;

	int type;

	String name;

	RegistryKey key;

	static {
		RegistryValue.hexChars = new char[20];

		RegistryValue.hexChars[0] = '0';
		RegistryValue.hexChars[1] = '1';
		RegistryValue.hexChars[2] = '2';
		RegistryValue.hexChars[3] = '3';
		RegistryValue.hexChars[4] = '4';
		RegistryValue.hexChars[5] = '5';
		RegistryValue.hexChars[6] = '6';
		RegistryValue.hexChars[7] = '7';
		RegistryValue.hexChars[8] = '8';
		RegistryValue.hexChars[9] = '9';
		RegistryValue.hexChars[10] = 'a';
		RegistryValue.hexChars[11] = 'b';
		RegistryValue.hexChars[12] = 'c';
		RegistryValue.hexChars[13] = 'd';
		RegistryValue.hexChars[14] = 'e';
		RegistryValue.hexChars[15] = 'f';
	}

	public RegistryValue(RegistryKey key, String name, int type) {
		this.key = key;
		this.name = name;
		this.type = type;
	}

	public RegistryKey getKey() {
		return this.key;
	}

	public String getName() {
		return this.name;
	}

	public int getType() {
		return this.type;
	}

	public void export(PrintWriter out) {
		out.print("\"" + this.getName() + "\"=");
		out.println("\"ERROR called RegistryValue.export()!\"");
	}

	public String toString() {
		return "[type=" + this.type + ",name=" + this.name + "]";
	}

	public static void exportHexData(PrintWriter out, byte[] data) {
		int i, cnt;
		char ch1, ch2;
		int len = data.length;

		for (i = 0, cnt = 0; i < len; ++i) {
			byte dByte = data[i];

			ch2 = RegistryValue.hexChars[(dByte & 0x0F)];
			ch1 = RegistryValue.hexChars[((dByte >> 4) & 0x0F)];

			if (cnt == 0)
				out.print("  ");

			out.print(ch1);
			out.print(ch2);

			if (i < (len - 1))
				out.print(",");

			if (++cnt > 15) {
				cnt = 0;
				if (i < (len - 1))
					out.println("\\");
			}
		}

		out.println("");
	}

	abstract public byte[] getByteData();

	abstract public int getByteLength();

	abstract public void setByteData(byte[] data);

}
