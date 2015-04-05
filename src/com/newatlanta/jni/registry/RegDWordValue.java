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
 * The RegDWordValue class represents a double word, or integer, value in the
 * registry (REG_DWORD).
 * 
 * @see com.newatlanta.jni.registry.Registry
 * @see com.newatlanta.jni.registry.RegistryKey
 * 
 * @version 3.1.3
 * 
 */

public class RegDWordValue extends RegistryValue {
	int data;

	int dataLen;

	public RegDWordValue(RegistryKey key, String name) {
		super(key, name, RegistryValue.REG_DWORD);
		this.data = 0;
		this.dataLen = 0;
	}

	public RegDWordValue(RegistryKey key, String name, int type) {
		super(key, name, type);
		this.data = 0;
		this.dataLen = 0;
	}

	public RegDWordValue(RegistryKey key, String name, int type, int data) {
		super(key, name, RegistryValue.REG_DWORD);
		this.setData(data);
	}

	public int getData() {
		return this.data;
	}

	public int getLength() {
		return this.dataLen;
	}

	public void setData(int data) {
		this.data = data;
		this.dataLen = 1;
	}

	public byte[] getByteData() {
		byte[] result = new byte[4];

		result[0] = (byte) ((this.data >> 24) & 255);
		result[1] = (byte) ((this.data >> 16) & 255);
		result[2] = (byte) ((this.data >> 8) & 255);
		result[3] = (byte) (this.data & 255);

		return result;
	}

	public int getByteLength() {
		return 4;
	}

	public void setByteData(byte[] data) {
		int newValue = ((((int) data[0]) << 24) & 0xFF000000) | ((((int) data[1]) << 16) & 0x00FF0000) | ((((int) data[2]) << 8) & 0x0000FF00) | (((int) data[3]) & 0x000000FF);

		this.setData(newValue);
	}

	public void export(PrintWriter out) {
		out.print("\"" + this.getName() + "\"=");

		HexNumberFormat nFmt = new HexNumberFormat("xxxxxxxx");

		out.println("dword:" + nFmt.format(this.getData()));
	}

}
