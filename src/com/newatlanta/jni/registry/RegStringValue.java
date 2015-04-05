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
 * The RegStringValue class represents a string value in the registry (REG_SZ,
 * and REG_EXPAND_SZ).
 * 
 * @version 3.1.3
 * 
 * @see com.newatlanta.jni.registry.Registry
 * @see com.newatlanta.jni.registry.RegistryKey
 */

public class RegStringValue extends RegistryValue {
	String data;

	int dataLen;

	public RegStringValue(RegistryKey key, String name) {
		super(key, name, RegistryValue.REG_SZ);
		this.data = null;
		this.dataLen = 0;
	}

	public RegStringValue(RegistryKey key, String name, int type) {
		super(key, name, type);
		this.data = null;
		this.dataLen = 0;
	}

	public RegStringValue(RegistryKey key, String name, String data) {
		super(key, name, RegistryValue.REG_SZ);
		this.setData(data);
	}

	public String getData() {
		return this.data;
	}

	public int getLength() {
		return this.dataLen;
	}

	public void setData(String data) {
		this.data = data;
		this.dataLen = data.length();
	}

	public byte[] getByteData() {
		return this.data.getBytes();
	}

	public int getByteLength() {
		return this.dataLen;
	}

	public void setByteData(byte[] data) {
		this.setData(new String(data));
	}

	public void export(PrintWriter out) {
		if (this.getName().length() == 0)
			out.print("@=");
		else
			out.print("\"" + this.getName() + "\"=");

		out.println("\"" + this.getData() + "\"");
	}

}
