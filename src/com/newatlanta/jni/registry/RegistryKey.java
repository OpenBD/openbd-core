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
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * The RegistryKey class represents a key in the registry. The class also provides all of the native interface calls.
 * 
 * You should refer to the Windows Registry API documentation for the details of any of the native methods. The native implementation performs almost no processing before or after a given call, so their behavior should match the API's documented behavior precisely.
 * 
 * Note that you can not open a subkey without an existing open RegistryKey. Thus, you need to start with one of the top level keys defined in the Registry class and open relative to that.
 * 
 * @version 3.1.3
 * 
 * @see com.newatlanta.jni.registry.Registry
 * @see com.newatlanta.jni.registry.RegistryValue
 */

public class RegistryKey {
	/**
	 * Constants used to determine the access level for newly opened keys.
	 */
	public static final int ACCESS_DEFAULT = 0;

	public static final int ACCESS_READ = 1;

	public static final int ACCESS_WRITE = 2;

	public static final int ACCESS_EXECUTE = 3;

	public static final int ACCESS_ALL = 4;

	/**
	 * This is the actual DWORD key that is returned from the Registry API. This value is <strong>totally opaque</strong> and should never be referenced.
	 */
	protected int hKey;

	/**
	 * The full pathname of this key.
	 */
	protected String name;

	/**
	 * Used to indicate whether or not the key was created when method createSubKey() is called, otherwise false.
	 */
	protected boolean created;

	public RegistryKey(int hKey, String name) {
		this.hKey = hKey;
		this.name = name;
		this.created = false;
	}

	public RegistryKey(int hKey, String name, boolean created) {
		this.hKey = hKey;
		this.name = name;
		this.created = created;
	}

	/**
	 * The finalize() override checks to be sure the key is closed.
	 */
	/*
	 * NOTE: This method is commented out because in a multi-threaded environment it can close a handle that is being used by another thread causing it to get an invalid handle exception. This will happen when the calling code properly closes the handle freeing it up to be used by another thread. GPB 7/17/06
	 * 
	 * public void finalize() { // Never close a top level key... if ( this.name.indexOf( "\\" ) > 0 ) { // REVIEW should we have an "open/closed" flag // to avoid double closes? Or is it better to // lazily not call closeKey() and let finalize() // do it all the time? // try { this.closeKey(); } catch ( RegistryException ex ) { } } }
	 */

	/**
	 * Get the name of this key. This is <em>not</em> fully qualified, which means that the name will not contain any backslashes.
	 * 
	 * @return The relative name of this key.
	 */

	public String getName() {
		int index = this.name.lastIndexOf("\\");

		if (index < 0)
			return this.name;
		else
			return this.name.substring(index + 1);
	}

	/**
	 * Get the full name of the key, from the top level down.
	 * 
	 * @return The full name of the key.
	 */

	public String getFullName() {
		return this.name;
	}

	/**
	 * Determine if this key was opened or created and opened. The result can only be true if createSubKey() was called and the key did not exist, and the creation of the new subkey succeeded.
	 * 
	 * @return True if the key was created new, else false.
	 */

	public boolean wasCreated() {
		return this.created;
	}

	/**
	 * Used to set the <em>created</em> state of this key.
	 * 
	 * @param created
	 *          The new <em>created</em> state.
	 */

	public void setCreated(boolean created) {
		this.created = created;
	}

	/**
	 * Open a Registry subkey of this key with READ access.
	 * 
	 * @param subkey
	 *          The name of the subkey to open.
	 * @return The newly opened RegistryKey.
	 * 
	 * @exception NoSuchKeyException
	 *              If the subkey does not exist.
	 * @exception RegistryException
	 *              Any other registry API error.
	 */

	public RegistryKey openSubKey(String subkey) throws NoSuchKeyException, RegistryException {
		return this.openSubKey(subkey, ACCESS_READ);
	}

	/**
	 * Create, and open, a Registry subkey of this key with WRITE access. If the key already exists, it is opened, otherwise it is first created and then opened.
	 * 
	 * @param subkey
	 *          The name of the subkey to create.
	 * @param className
	 *          The className of the created subkey.
	 * @return The newly created and opened RegistryKey.
	 * 
	 * @exception RegistryException
	 *              Any valid registry API error.
	 */

	public RegistryKey createSubKey(String subkey, String className) throws RegistryException {
		return this.createSubKey(subkey, "", ACCESS_WRITE);
	}

	/**
	 * Set the value of this RegistryKey.
	 * 
	 * @param value
	 *          The value to set, including the value name.
	 * 
	 * @exception RegistryException
	 *              Any valid registry API error.
	 */

	public void setValue(RegistryValue value) throws RegistryException {
		this.setValue(value.getName(), value);
	}

	//
	// N A T I V E M E T H O D S
	//

	/**
	 * Open a Registry subkey of this key with the specified access.
	 * 
	 * @param subkey
	 *          The name of the subkey to open.
	 * @param access
	 *          The access level for the open.
	 * @return The newly opened RegistryKey.
	 * 
	 * @exception NoSuchKeyException
	 *              If the subkey does not exist.
	 * @exception RegistryException
	 *              Any other registry API error.
	 */

	public native RegistryKey openSubKey(String subKey, int access) throws NoSuchKeyException, RegistryException;

	/**
	 * Connect to the remote registry on <em>hostName</em>. This method will only work when invoked on a toplevel key. The returned value will be the same toplevel key opened from the remote host's registry.
	 * 
	 * @param hostName
	 *          The remote computer's hostname.
	 * @return The remote top level key identical to this top level key.
	 * 
	 * @exception NoSuchKeyException
	 *              If the subkey does not exist.
	 * @exception RegistryException
	 *              Any other registry API error.
	 */
	public native RegistryKey connectRegistry(String hostName) throws NoSuchKeyException, RegistryException;

	/**
	 * Create a new subkey, or open the existing one. You can determine if the subkey was created, or whether an existing subkey was opened, via the wasCreated() method.
	 * 
	 * @param subKey
	 *          The name of the subkey to create/open.
	 * @param className
	 *          The key's class name, or null.
	 * @param access
	 *          The access level of the opened subkey.
	 * @return The newly created or opened subkey.
	 * 
	 * @exception RegistryException
	 *              Any valid registry API error.
	 */

	public native RegistryKey createSubKey(String subKey, String className, int access) throws RegistryException;

	/**
	 * Closes this subkey. You may chose to let the finalize() method do the close.
	 * 
	 * @exception RegistryException
	 *              Any valid registry API error.
	 */

	public native void closeKey() throws RegistryException;

	/**
	 * Delete a named subkey.
	 * 
	 * @param subKey
	 *          The name of the subkey to delete.
	 * 
	 * @exception NoSuchKeyException
	 *              If the subkey does not exist.
	 * @exception RegistryException
	 *              Any other registry API error.
	 */

	public native void deleteSubKey(String subKey) throws NoSuchKeyException, RegistryException;

	/**
	 * Delete a named value.
	 * 
	 * @param valueName
	 *          The name of the value to delete.
	 * 
	 * @exception NoSuchValueException
	 *              If the value does not exist.
	 * @exception RegistryException
	 *              Any other registry API error.
	 */

	public native void deleteValue(String valueName) throws NoSuchValueException, RegistryException;

	/**
	 * Guarentees that this key is written to disk. This method should be called only when needed, as it has a huge performance cost.
	 * 
	 * @exception RegistryException
	 *              Any valid registry API error.
	 */

	public native void flushKey() throws RegistryException;

	/**
	 * Set the name value to the given data.
	 * 
	 * @param valueName
	 *          The name of the value to set.
	 * @param value
	 *          The data to set the named value.
	 * 
	 * @exception RegistryException
	 *              Any valid registry API error.
	 */

	public native void setValue(String valueName, RegistryValue value) throws RegistryException;

	/**
	 * Get the data of a named value.
	 * 
	 * @param valueName
	 *          The name of the value to get.
	 * @return The data of the named value.
	 * 
	 * @exception NoSuchValueException
	 *              If the value does not exist.
	 * @exception RegistryException
	 *              Any other registry API error.
	 */

	public native RegistryValue getValue(String valueName) throws NoSuchValueException, RegistryException;

	/**
	 * Get the value of a REG_SZ or REG_EXPAND_SZ value.
	 * 
	 * @param valueName
	 *          The name of the value to get.
	 * @return The string data of the named value.
	 * 
	 * @exception NoSuchValueException
	 *              If the value does not exist.
	 * @exception RegistryException
	 *              Any other registry API error.
	 */

	public native String getStringValue(String valueName) throws NoSuchValueException, RegistryException;

	/**
	 * Get the data from the default value.
	 * 
	 * @return The string data of the default value.
	 * 
	 * @exception NoSuchValueException
	 *              If the value does not exist.
	 * @exception RegistryException
	 *              Any other registry API error.
	 */

	public native String getDefaultValue() throws NoSuchValueException, RegistryException;

	/**
	 * Determines if this key has a default value.
	 * 
	 * @return True if there is a default value, else false.
	 * 
	 * @exception RegistryException
	 *              Any valid registry API error.
	 */

	public native boolean hasDefaultValue() throws RegistryException;

	/**
	 * Determines if this key has <em>only</em> a default value.
	 * 
	 * @return True if there is only a default value, else false.
	 * 
	 * @exception RegistryException
	 *              Any valid registry API error.
	 */

	public native boolean hasOnlyDefaultValue() throws RegistryException;

	/**
	 * Obtains the number of subkeys that this key contains.
	 * 
	 * @return The number of subkeys that this key contains.
	 * 
	 * @exception RegistryException
	 *              Any valid registry API error.
	 */

	public native int getNumberSubkeys() throws RegistryException;

	/**
	 * Obtains the maximum length of all of the subkey names.
	 * 
	 * @return The maximum length of all of the subkey names.
	 * 
	 * @exception RegistryException
	 *              Any valid registry API error.
	 */

	public native int getMaxSubkeyLength() throws RegistryException;

	/**
	 * Obtains an enumerator for the subkeys of this key.
	 * 
	 * @return The key enumerator.
	 * 
	 * @exception RegistryException
	 *              Any valid registry API error.
	 */

	public native String regEnumKey(int index) throws RegistryException;

	/**
	 * Obtains the number of values that this key contains.
	 * 
	 * @return The number of values that this key contains.
	 * 
	 * @exception RegistryException
	 *              Any valid registry API error.
	 */

	public native int getNumberValues() throws RegistryException;

	/**
	 * Obtains the maximum length of all of the value data.
	 * 
	 * @return The maximum length of all of the value data.
	 * 
	 * @exception RegistryException
	 *              Any valid registry API error.
	 */

	public native int getMaxValueDataLength() throws RegistryException;

	/**
	 * Obtains the maximum length of all of the value names.
	 * 
	 * @return The maximum length of all of the value names.
	 * 
	 * @exception RegistryException
	 *              Any valid registry API error.
	 */

	public native int getMaxValueNameLength() throws RegistryException;

	/**
	 * Obtains an enumerator for the values of this key.
	 * 
	 * @return The value enumerator.
	 * 
	 * @exception RegistryException
	 *              Any valid registry API error.
	 */

	public native String regEnumValue(int index) throws RegistryException;

	//
	// Convenience routines
	//

	/**
	 * This method will increment the value of a REG_DWORD value.
	 * 
	 * @param valueName
	 *          The name of the value to increment.
	 * 
	 * @exception NoSuchValueException
	 *              If the value does not exist.
	 * @exception RegistryException
	 *              Any other registry API error.
	 */

	public native int incrDoubleWord(String valueName) throws NoSuchValueException, RegistryException;

	/**
	 * This method will decrement the value of a REG_DWORD value.
	 * 
	 * @param valueName
	 *          The name of the value to increment.
	 * 
	 * @exception NoSuchValueException
	 *              If the value does not exist.
	 * @exception RegistryException
	 *              Any other registry API error.
	 */

	public native int decrDoubleWord(String valueName) throws NoSuchValueException, RegistryException;

	/**
	 * This method will expand a string to include the definitions of System environment variables that are referenced via the %variable% construct. This method invokes EnvExpandStrings().
	 * 
	 * @param valueName
	 *          The name of the value to increment.
	 */

	public static native String expandEnvStrings(String exString);

	/**
	 * Returns a new Enumeration that will enumerate the names of the subkeys of this key,
	 * 
	 * @return A new Enumeration to enumerate subkey names.
	 * 
	 * @exception RegistryException
	 *              Any valid registry API error.
	 */

	public Enumeration keyElements() throws RegistryException {
		return this.new RegistryKeyEnumerator(this);
	}

	/**
	 * Returns a new Enumeration that will enumerate the names of the values of this key,
	 * 
	 * @return A new Enumeration to enumerate value names.
	 * 
	 * @exception RegistryException
	 *              Any valid registry API error.
	 */

	public Enumeration valueElements() throws RegistryException {
		return this.new RegistryValueEnumerator(this);
	}

	/**
	 * A RegistryKey enumerator class. This enumerator is used to enumerate the names of this key's subkeys.
	 * 
	 * This class should remain opaque to the client, which will use the Enumeration interface.
	 */
	class RegistryKeyEnumerator implements Enumeration {
		RegistryKey key;

		int currIndex;

		int numSubKeys;

		public RegistryKeyEnumerator(RegistryKey key) throws RegistryException {
			this.key = key;
			this.currIndex = 0;
			this.numSubKeys = key.getNumberSubkeys();
		}

		public boolean hasMoreElements() {
			return (this.currIndex < this.numSubKeys);
		}

		public Object nextElement() {
			Object result = null;

			try {
				result = this.key.regEnumKey(this.currIndex++);
			} catch (RegistryException ex) {
				throw new NoSuchElementException(ex.getMessage());
			}

			return result;
		}
	}

	/**
	 * A RegistryValue enumerator class. This enumerator is used to enumerate the names of this key's values. This will return the default value name as an empty string.
	 * 
	 * This class should remain opaque to the client. It will use the Enumeration interface.
	 */

	class RegistryValueEnumerator implements Enumeration {
		RegistryKey key;

		int currIndex;

		int numValues;

		public RegistryValueEnumerator(RegistryKey key) throws RegistryException {
			this.key = key;
			this.currIndex = 0;
			this.numValues = key.getNumberValues();
		}

		public boolean hasMoreElements() {
			return (this.currIndex < this.numValues);
		}

		public Object nextElement() {
			Object result = null;

			try {
				result = this.key.regEnumValue(this.currIndex++);
			} catch (RegistryException ex) {
				throw new NoSuchElementException(ex.getMessage());
			}

			return result;
		}
	}

	/**
	 * Export this key's definition to the provided PrintWriter. The resulting file can be imported via RegEdit.
	 * 
	 * @exception NoSuchKeyException
	 *              Thrown by openSubKey().
	 * @exception NoSuchValueException
	 *              Thrown by getValue().
	 * @exception RegistryException
	 *              Any other registry API error.
	 */

	public void export(PrintWriter out, boolean descend) throws NoSuchKeyException, RegistryException {
		Enumeration enumer;

		out.println("[" + this.getFullName() + "]");

		enumer = this.valueElements();

		while (enumer.hasMoreElements()) {
			String valueName = (String) enumer.nextElement();

			RegistryValue value = this.getValue(valueName);

			value.export(out);
		}

		out.println("");

		if (descend) {
			enumer = this.keyElements();

			while (enumer.hasMoreElements()) {
				String keyName = (String) enumer.nextElement();

				RegistryKey subKey = this.openSubKey(keyName);

				subKey.export(out, descend);

				subKey.closeKey();
			}
		}
	}

}
