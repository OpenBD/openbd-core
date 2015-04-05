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
 * The RegBinaryValue class represents a binary value in the registry
 * (REG_BINARY).
 * 
 * @version 3.1.3
 * 
 * @see com.newatlanta.jni.registry.Registry
 * @see com.newatlanta.jni.registry.RegistryKey
 */

public class RegBinaryValue extends RegistryValue {
	byte[] data;

	int dataLen;

	public RegBinaryValue(RegistryKey key, String name) {
		super(key, name, RegistryValue.REG_BINARY);
		this.data = null;
		this.dataLen = 0;
	}

	public RegBinaryValue(RegistryKey key, String name, int type) {
		super(key, name, type);
		this.data = null;
		this.dataLen = 0;
	}

	public RegBinaryValue(RegistryKey key, String name, byte[] data) {
		super(key, name, RegistryValue.REG_BINARY);
		this.setData(data);
	}

	public byte[] getData() {
		return this.data;
	}

	public int getLength() {
		return this.dataLen;
	}

	public void setData(byte[] data) {
		this.data = data;
		this.dataLen = data.length;
	}

	public byte[] getByteData() {
		return this.data;
	}

	public int getByteLength() {
		return this.dataLen;
	}

	public void setByteData(byte[] data) {
		this.data = data;
		this.dataLen = data.length;
	}

	public void export(PrintWriter out) {
		out.println("\"" + this.getName() + "\"=hex:\\");
		RegistryValue.exportHexData(out, this.data);
	}

}
