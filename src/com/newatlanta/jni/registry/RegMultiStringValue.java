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
 * The RegMultiStringValue class represents a multiple string, or string array,
 * value in the registry (REG_MULTI_SZ).
 * 
 * @version 3.1.3
 * 
 * @see com.newatlanta.jni.registry.Registry
 * @see com.newatlanta.jni.registry.RegistryKey
 */

public class RegMultiStringValue extends RegistryValue {
	String[] data;

	int dataLen;

	public RegMultiStringValue(RegistryKey key, String name) {
		super(key, name, RegistryValue.REG_MULTI_SZ);
		this.data = null;
		this.dataLen = 0;
	}

	public RegMultiStringValue(RegistryKey key, String name, int type) {
		super(key, name, type);
		this.data = null;
		this.dataLen = 0;
	}

	public RegMultiStringValue(RegistryKey key, String name, String[] data) {
		super(key, name, RegistryValue.REG_MULTI_SZ);
		this.setData(data);
	}

	public String[] getData() {
		return this.data;
	}

	public int getLength() {
		return this.dataLen;
	}

	public void setData(String[] data) {
		this.data = data;
		this.dataLen = data.length;
	}

	public byte[] getByteData() {
		int len = this.getByteLength();

		int ri = 0;
		byte[] result = new byte[len];
		for (int i = 0; i < this.dataLen; ++i) {
			byte[] strBytes = this.data[i].getBytes();

			for (int j = 0; j < strBytes.length; ++j)
				result[ri++] = strBytes[j];

			result[ri++] = 0;
		}

		return result;
	}

	public int getByteLength() {
		int len = 0;
		for (int i = 0; i < this.dataLen; ++i)
			len += this.data[i].length() + 1;

		return len;
	}

	public void setByteData(byte[] data) {
		int start;
		int count = 0;

		for (int i = 0; i < data.length; ++i) {
			if (data[i] == 0)
				count++;
		}

		int si = 0;
		String[] newData = new String[count];
		for (int i = start = 0; i < data.length; ++i) {
			if (data[i] == 0) {
				newData[si] = new String(data, start, (i - start));
				start = si;
			}
		}

		this.setData(newData);
	}

	public void export(PrintWriter out) {
		byte[] hexData;
		int dataLen = 0;

		out.println("\"" + this.getName() + "\"=hex(7):\\");

		for (int i = 0; i < this.data.length; ++i) {
			dataLen += this.data[i].length() + 1;
		}

		++dataLen;

		int idx = 0;
		hexData = new byte[dataLen];

		for (int i = 0; i < this.data.length; ++i) {
			int strLen = this.data[i].length();
			byte[] strBytes = this.data[i].getBytes();

			System.arraycopy(strBytes, 0, hexData, idx, strLen);

			idx += strLen;

			hexData[idx++] = 0;
		}

		hexData[idx++] = 0;

		RegistryValue.exportHexData(out, hexData);
	}

}
