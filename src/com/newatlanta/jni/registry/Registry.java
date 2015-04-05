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

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * The Registry class provides is used to load the native library DLL, as well
 * as a placeholder for the top level keys, error codes, and utility methods.
 * 
 * @version 3.1.3
 * 
 */

public class Registry {
	/**
	 * The following statics are the top level keys. Without these, there is no
	 * way to get "into" the registry, since the RegOpenSubkey() call requires an
	 * existing key which contains the subkey.
	 */
	public static RegistryKey HKEY_CLASSES_ROOT;

	public static RegistryKey HKEY_CURRENT_USER;

	public static RegistryKey HKEY_LOCAL_MACHINE;

	public static RegistryKey HKEY_USERS;

	public static RegistryKey HKEY_PERFORMANCE_DATA;

	public static RegistryKey HKEY_CURRENT_CONFIG;

	public static RegistryKey HKEY_DYN_DATA;

	/**
	 * These are predefined keys ($0-$9) used to make the testing program easier
	 * to use (less typing).
	 */
	private static String[] preDefines;

	/**
	 * These are the Registry API error codes, which can be returned via the
	 * RegistryException.
	 */
	public static final int ERROR_SUCCESS = 0;

	public static final int ERROR_FILE_NOT_FOUND = 2;

	public static final int ERROR_ACCESS_DENIED = 5;

	public static final int ERROR_INVALID_HANDLE = 6;

	public static final int ERROR_INVALID_PARAMETER = 87;

	public static final int ERROR_CALL_NOT_IMPLEMENTED = 120;

	public static final int ERROR_INSUFFICIENT_BUFFER = 122;

	public static final int ERROR_LOCK_FAILED = 167;

	public static final int ERROR_TRANSFER_TOO_LONG = 222;

	public static final int ERROR_MORE_DATA = 234;

	public static final int ERROR_NO_MORE_ITEMS = 259;

	public static final int ERROR_BADDB = 1009;

	public static final int ERROR_BADKEY = 1010;

	public static final int ERROR_CANTOPEN = 1011;

	public static final int ERROR_CANTREAD = 1012;

	public static final int ERROR_CANTWRITE = 1013;

	public static final int ERROR_REGISTRY_RECOVERED = 1014;

	public static final int ERROR_REGISTRY_CORRUPT = 1015;

	public static final int ERROR_REGISTRY_IO_FAILED = 1016;

	public static final int ERROR_NOT_REGISTRY_FILE = 1017;

	public static final int ERROR_KEY_DELETED = 1018;

	/**
	 * These are used by dumpHex().
	 */
	private static final int ROW_BYTES = 16;

	private static final int ROW_QTR1 = 3;

	private static final int ROW_HALF = 7;

	private static final int ROW_QTR2 = 11;

	/**
	 * This is the last key used by the test program ($$).
	 */
	private static String saveKey = null;

	/**
	 * This is a Hashtable which maps nams to the top level keys.
	 */
	private static Hashtable topLevelKeys = null;

	/**
	 * If true, debug the fv parameters and computation.
	 */
	public boolean debugLevel;

	/**
	 * Loads the DLL needed for the native methods, creates the toplevel keys,
	 * fills the hashtable that maps various names to the toplevel keys.
	 */

	static {
		/*
		 * ICE_JNIRegistry.dll has been renamed to cfregistry.dll. It is loaded by
		 * cfREGISTRY.init() so we don't need to load it here.
		 * 
		 * try { System.loadLibrary( "ICE_JNIRegistry" ); } catch (
		 * UnsatisfiedLinkError e ) { System.err.println ( "ERROR You have not
		 * installed the DLL named '" + "ICE_JNIRegistry.DLL'.\n\t" + e.getMessage() ); }
		 * catch ( SecurityException e ) { System.err.println ( "ERROR You do not
		 * have permission to load the DLL named '" + "ICE_JNIRegistry.DLL'.\n\t" +
		 * e.getMessage() ); }
		 */

		Registry.HKEY_CLASSES_ROOT = new RegistryKey(0x80000000, "HKEY_CLASSES_ROOT");

		Registry.HKEY_CURRENT_USER = new RegistryKey(0x80000001, "HKEY_CURRENT_USER");

		Registry.HKEY_LOCAL_MACHINE = new RegistryKey(0x80000002, "HKEY_LOCAL_MACHINE");

		Registry.HKEY_USERS = new RegistryKey(0x80000003, "HKEY_USERS");

		Registry.HKEY_PERFORMANCE_DATA = new RegistryKey(0x80000004, "HKEY_PERFORMANCE_DATA");

		Registry.HKEY_CURRENT_CONFIG = new RegistryKey(0x80000005, "HKEY_CURRENT_CONFIG");

		Registry.HKEY_DYN_DATA = new RegistryKey(0x80000006, "HKEY_DYN_DATA");

		Registry.topLevelKeys = new Hashtable(16);

		topLevelKeys.put("HKCR", Registry.HKEY_CLASSES_ROOT);
		topLevelKeys.put("HKEY_CLASSES_ROOT", Registry.HKEY_CLASSES_ROOT);

		topLevelKeys.put("HKCU", Registry.HKEY_CURRENT_USER);
		topLevelKeys.put("HKEY_CURRENT_USER", Registry.HKEY_CURRENT_USER);

		topLevelKeys.put("HKLM", Registry.HKEY_LOCAL_MACHINE);
		topLevelKeys.put("HKEY_LOCAL_MACHINE", Registry.HKEY_LOCAL_MACHINE);

		topLevelKeys.put("HKU", Registry.HKEY_USERS);
		topLevelKeys.put("HKUS", Registry.HKEY_USERS);
		topLevelKeys.put("HKEY_USERS", Registry.HKEY_USERS);

		topLevelKeys.put("HKPD", Registry.HKEY_PERFORMANCE_DATA);
		topLevelKeys.put("HKEY_PERFORMANCE_DATA", Registry.HKEY_PERFORMANCE_DATA);

		topLevelKeys.put("HKCC", Registry.HKEY_PERFORMANCE_DATA);
		topLevelKeys.put("HKEY_CURRENT_CONFIG", Registry.HKEY_PERFORMANCE_DATA);

		topLevelKeys.put("HKDD", Registry.HKEY_PERFORMANCE_DATA);
		topLevelKeys.put("HKEY_DYN_DATA", Registry.HKEY_PERFORMANCE_DATA);
	}

	/**
	 * Get a top level key by name using the top level key Hashtable.
	 * 
	 * @param keyName
	 *          The name of the top level key.
	 * @return The top level RegistryKey, or null if unknown keyName.
	 * 
	 * @see topLevelKeys
	 */

	public static RegistryKey getTopLevelKey(String keyName) {
		return (RegistryKey) Registry.topLevelKeys.get(keyName);
	}

	/**
	 * Open a subkey of a given top level key.
	 * 
	 * @param topKey
	 *          The top level key containing the subkey.
	 * @param keyName
	 *          The subkey's name.
	 * @param access
	 *          The access flag for the newly opened key.
	 * @return The newly opened RegistryKey.
	 * 
	 * @see RegistryKey
	 */

	public static RegistryKey openSubkey(RegistryKey topKey, String keyName, int access) {
		RegistryKey subKey = null;

		try {
			subKey = topKey.openSubKey(keyName, access);
		} catch (NoSuchKeyException ex) {
			subKey = null;
		} catch (RegistryException ex) {
			subKey = null;
		}

		return subKey;
	}

	/**
	 * Get the description of a Registry error code.
	 * 
	 * @param errCode
	 *          The error code from a RegistryException
	 * @return The description of the error code.
	 */

	public static String getErrorMessage(int errCode) {
		switch (errCode) {
		case ERROR_SUCCESS:
			return "success";
		case ERROR_FILE_NOT_FOUND:
			return "key or value not found";
		case ERROR_ACCESS_DENIED:
			return "access denied";
		case ERROR_INVALID_HANDLE:
			return "invalid handle";
		case ERROR_INVALID_PARAMETER:
			return "invalid parameter";
		case ERROR_CALL_NOT_IMPLEMENTED:
			return "call not implemented";
		case ERROR_INSUFFICIENT_BUFFER:
			return "insufficient buffer";
		case ERROR_LOCK_FAILED:
			return "lock failed";
		case ERROR_TRANSFER_TOO_LONG:
			return "transfer was too long";
		case ERROR_MORE_DATA:
			return "more data buffer needed";
		case ERROR_NO_MORE_ITEMS:
			return "no more items";
		case ERROR_BADDB:
			return "bad database";
		case ERROR_BADKEY:
			return "bad key";
		case ERROR_CANTOPEN:
			return "can not open";
		case ERROR_CANTREAD:
			return "can not read";
		case ERROR_CANTWRITE:
			return "can not write";
		case ERROR_REGISTRY_RECOVERED:
			return "registry recovered";
		case ERROR_REGISTRY_CORRUPT:
			return "registry corrupt";
		case ERROR_REGISTRY_IO_FAILED:
			return "registry IO failed";
		case ERROR_NOT_REGISTRY_FILE:
			return "not a registry file";
		case ERROR_KEY_DELETED:
			return "key has been deleted";
		}

		return "errCode=" + errCode;
	}

	/**
	 * Export the textual definition for a registry key to a file. The resulting
	 * file can be re-loaded via RegEdit.
	 * 
	 * @param pathName
	 *          The pathname of the file into which to export.
	 * @param key
	 *          The registry key definition to export.
	 * @param descend
	 *          If true, descend and export all subkeys.
	 * 
	 * @exception NoSuchKeyException
	 *              Thrown by openSubKey().
	 * @exception RegistryException
	 *              Any other registry API error.
	 */

	public static void exportRegistryKey(String pathName, RegistryKey key, boolean descend) throws java.io.IOException, NoSuchKeyException, RegistryException {
		PrintWriter out = new PrintWriter(new FileWriter(pathName));

		out.println("REGEDIT4");
		out.println("");

		key.export(out, descend);

		out.flush();
		out.close();
	}

	/**
	 * The main() method is used to test the Registry package.
	 */

	public static void main(String argv[]) {
		Registry.preDefines = new String[10];

		Registry.preDefines[0] = "HKLM\\System\\CurrentControlSet\\control";
		Registry.preDefines[1] = "HKLM\\Software";
		Registry.preDefines[2] = "HKLM\\Software\\Miscrosoft";
		Registry.preDefines[3] = "HKLM\\Software\\Microsoft\\Windows" + "\\CurrentVersion";
		Registry.preDefines[4] = "HKLM\\Software\\Microsoft\\Windows" + "\\CurrentVersion\\ProfileList";
		Registry.preDefines[5] = "HKCU\\Software";
		Registry.preDefines[6] = "HKCU\\Software\\Microsoft";
		Registry.preDefines[7] = "HKCU\\AppEvents";
		Registry.preDefines[8] = "HKCU\\AppEvents\\Schemes";
		Registry.preDefines[9] = "HKCU\\AppEvents\\Schemes";

		if (argv.length > 0) {
			Registry.subMain(argv);
		} else {
			String inLine;
			String saveLine = null;
			BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

			for (;;) {
				System.out.print("command: ");
				System.out.flush();

				try {
					inLine = input.readLine();
				} catch (IOException ex) {
					inLine = null;
				}

				if (inLine == null || inLine.length() == 0)
					break;

				if (inLine.equalsIgnoreCase("help")) {
					Registry.usage(null);
					continue;
				}

				String[] subArgs;
				if (inLine.equals("!!") && saveLine != null) {
					subArgs = Registry.parseArgumentString(saveLine);
				} else {
					subArgs = Registry.parseArgumentString(inLine);
					saveLine = inLine;
				}

				Registry.subMain(subArgs);
			}
		}
	}

	/**
	 * Print the usage/help information.
	 */

	public static void usage(String message) {
		if (message != null)
			System.err.println(message);

		System.err.println("keys regKey -- print the key names");
		System.err.println("values regKey -- print the value names");
		System.err.println("data regKey subKey -- print the key's data");
		System.err.println("string regKey subKey -- print REG_SZ key's string");
		System.err.println("setbin regKey subKey  binaryString -- set REG_BINARY");
		System.err.println("setdw regKey subKey int -- set REG_DWORD");
		System.err.println("setstr regKey subKey string -- set REG_SZ");
		System.err.println("setmulti regKey subKey semiColonString -- set REG_MULTI_SZ");
		System.err.println("delkey regKey subKey -- delete key 'subKey' of regKey");
		System.err.println("delval regKey subKey -- delete value 'subKey' of regKey");
		System.err.println("export regKey fileName -- export registry key to fileName");
		System.err.println("expand regKey valueName -- expand string value");

		System.err.println("");

		System.err.println("!! -- repeats last command");
		System.err.println("$$ -- re-uses previous keyname");
		System.err.println("Predefined Key Prefixes: (e.g. $0-9)");
		for (int idx = 0; idx < Registry.preDefines.length; ++idx)
			System.err.println("   $" + idx + "=" + Registry.preDefines[idx]);
	}

	/**
	 * The actual main method, which is called for each command.
	 */

	public static void subMain(String argv[]) {
		int index;
		RegistryKey topKey = null;
		boolean isRemote = false;
		String topKeyName = null;
		String hostName = null;

		if (argv.length < 1 || argv[0].equals("help")) {
			Registry.usage(null);
			return;
		}

		if (argv.length < 2) {
			Registry.usage(null);
			return;
		}

		String keyName = argv[1];

		if (Registry.saveKey != null && keyName.equals("$$")) {
			keyName = Registry.saveKey;
		} else if (keyName.equals("@@")) {
			keyName = "HKCU\\Software\\ICE Engineering\\test";
		} else {
			char ch1 = keyName.charAt(0);
			char ch2 = keyName.charAt(1);

			if (ch1 == '$' && ch2 >= '0' && ch2 <= '9') {
				int pIdx = (ch2 - '0');
				if (Registry.preDefines[pIdx] != null) {
					if (keyName.length() < 3)
						keyName = Registry.preDefines[pIdx];
					else
						keyName = Registry.preDefines[pIdx] + keyName.substring(2);
				} else {
					System.err.println("Predefine '" + keyName + "' not defined.");
					return;
				}
			} else {
				Registry.saveKey = argv[1];
			}
		}

		if (keyName.startsWith("\\\\")) {
			isRemote = true;
			index = keyName.indexOf('\\', 2);
			hostName = keyName.substring(2, index);
			keyName = keyName.substring(index + 1);
		}

		index = keyName.indexOf('\\');

		if (index < 0) {
			//
			// "topLevelKeyname"
			//
			topKeyName = keyName;
			keyName = null;
		} else if (index < 4) {
			//
			// INVALID KEYNAME, topLevelName too short
			//
			System.err.println("Invalid key '" + keyName + "', top level key name too short.");
			return;
		} else {
			//
			// "topLevelKeyname\subKey\subKey\..."
			//
			topKeyName = keyName.substring(0, index);

			if ((index + 1) >= keyName.length())
				keyName = null;
			else
				keyName = keyName.substring(index + 1);
		}

		topKey = Registry.getTopLevelKey(topKeyName);
		if (topKey == null) {
			System.err.println("ERROR, toplevel key '" + topKeyName + "' not resolved!");
			return;
		}

		if (isRemote) {
			System.err.println("REMOTE Key host='" + hostName + "'");

			RegistryKey remoteKey = null;

			try {
				remoteKey = topKey.connectRegistry(hostName);
			} catch (NoSuchKeyException ex) {
				System.err.println("ERROR No such key connecting to '" + hostName + "', " + ex.getMessage());
				return;
			} catch (RegistryException ex) {
				System.err.println("ERROR errCode=" + ex.getErrorCode() + "' connecting to '" + hostName + "', " + ex.getMessage());
				return;
			}

			if (remoteKey != null) {
				topKey = remoteKey;
			}
		}

		//
		// P R O C E S S C O M M A N D S
		//

		if (argv[0].equalsIgnoreCase("create")) {
			Registry.createCommand(topKey, keyName);
		} else if (argv[0].equalsIgnoreCase("setbin")) {
			Registry.setBinaryCommand(topKey, keyName, argv[2], argv[3]);
		} else if (argv[0].equalsIgnoreCase("setdw")) {
			Registry.setBinaryCommand(topKey, keyName, argv[2], argv[3]);
		} else if (argv[0].equalsIgnoreCase("setstr")) {
			Registry.setStringCommand(topKey, keyName, argv[2], argv[3]);
		} else if (argv[0].equalsIgnoreCase("setmulti")) {
			Registry.setMultiStringCommand(topKey, keyName, argv[2], argv[3]);
		} else if (argv[0].equalsIgnoreCase("keys")) {
			Registry.listKeysCommand(topKey, keyName);
		} else if (argv[0].equalsIgnoreCase("values")) {
			Registry.listValuesCommand(topKey, keyName);
		} else if (argv[0].equalsIgnoreCase("delkey")) {
			Registry.deleteKeyCommand(topKey, keyName, argv[2]);
		} else if (argv[0].equalsIgnoreCase("delval")) {
			Registry.deleteValueCommand(topKey, keyName, argv[2]);
		} else if (argv[0].equalsIgnoreCase("data")) {
			Registry.getDataCommand(topKey, keyName, argv[2]);
		} else if (argv[0].equalsIgnoreCase("string")) {
			Registry.getStringCommand(topKey, keyName, argv[2]);
		} else if (argv[0].equalsIgnoreCase("export")) {
			Registry.exportKeyCommand(topKey, keyName, argv[2]);
		} else if (argv[0].equalsIgnoreCase("expand")) {
			Registry.expandStringCommand(topKey, keyName, argv[2]);
		}
	}

	private static void exportKeyCommand(RegistryKey topKey, String keyName, String pathName) {
		RegistryKey subKey = Registry.openSubKeyVerbose(topKey, keyName, RegistryKey.ACCESS_READ);

		if (subKey == null)
			return;

		try {
			Registry.exportRegistryKey(pathName, subKey, true);
		} catch (IOException ex) {
			System.err.println("IO Exception: '" + ex.getMessage() + "'");
		} catch (NoSuchKeyException ex) {
			System.err.println("Error, encountered non-existent key during export.");
		} catch (RegistryException ex) {
			System.err.println("ERROR registry error=" + ex.getErrorCode() + ", " + ex.getMessage());
		}
	}

	private static void getDataCommand(RegistryKey topKey, String keyName, String valueName) {
		RegistryKey subKey = Registry.openSubKeyVerbose(topKey, keyName, RegistryKey.ACCESS_READ);

		if (subKey == null)
			return;

		RegistryValue data = null;

		try {
			data = subKey.getValue(valueName);
		} catch (NoSuchValueException ex) {
			System.err.println("Value '" + valueName + "' does not exist.");
			return;
		} catch (RegistryException ex) {
			System.err.println("ERROR registry error=" + ex.getErrorCode() + ", " + ex.getMessage());
			return;
		}

		System.err.println("Value '" + valueName + "' is " + data.toString());

		if (data instanceof RegStringValue) {
			RegStringValue val = (RegStringValue) data;
			System.err.println("REG_SZ '" + val.getData() + "'");
		} else if (data instanceof RegMultiStringValue) {
			RegMultiStringValue val = (RegMultiStringValue) data;
			String[] args = val.getData();
			for (int idx = 0; idx < args.length; ++idx)
				System.err.println("REG_MULTI_SZ[" + idx + "] '" + args[idx] + "'");
		} else if (data instanceof RegDWordValue) {
			RegDWordValue val = (RegDWordValue) data;

			HexNumberFormat xFmt = new HexNumberFormat("XXXXXXXX");

			System.err.println("REG_DWORD" + ((RegistryValue.REG_DWORD_BIG_ENDIAN == val.getType()) ? "_BIG_ENDIAN" : "") + " '" + val.getData() + "' [x" + xFmt.format(val.getData()) + "]");
		} else {
			RegBinaryValue val = (RegBinaryValue) data;
			/*
			 * System.err.println( "BINARY: len=" + val.getLength() );
			 * System.err.println( "BINARY: [0]=" + val.getData()[0] );
			 * System.err.println( "BINARY: [1]=" + val.getData()[1] );
			 * System.err.println( "BINARY: [2]=" + val.getData()[2] );
			 * System.err.println( "BINARY: [3]=" + val.getData()[3] );
			 */
			Registry.dumpHexData(System.err, "REG_BINARY '" + val.getName() + "', len=" + val.getLength(), val.getData(), val.getLength());
		}
	}

	private static void getStringCommand(RegistryKey topKey, String keyName, String valueName) {
		RegistryKey subKey = Registry.openSubKeyVerbose(topKey, keyName, RegistryKey.ACCESS_READ);

		if (subKey == null)
			return;

		try {
			String value = subKey.getStringValue(valueName);
			System.err.println("String Value " + valueName + "='" + value + "'");
		} catch (RegistryException ex) {
			System.err.println("ERROR getting value '" + valueName + "', " + ex.getMessage());
			return;
		}
	}

	private static void expandStringCommand(RegistryKey topKey, String keyName, String valueName) {
		RegistryKey subKey = Registry.openSubKeyVerbose(topKey, keyName, RegistryKey.ACCESS_READ);

		if (subKey == null)
			return;

		try {
			String value = subKey.getStringValue(valueName);
			System.err.println("String Value " + valueName + "='" + value + "'");
			value = RegistryKey.expandEnvStrings(value);
			System.err.println("Expanded Value " + valueName + "='" + value + "'");
		} catch (RegistryException ex) {
			System.err.println("ERROR getting value '" + valueName + "', " + ex.getMessage());
			return;
		}
	}

	private static void deleteKeyCommand(RegistryKey topKey, String keyName, String deleteKeyName) {
		RegistryKey subKey = Registry.openSubKeyVerbose(topKey, keyName, RegistryKey.ACCESS_WRITE);

		if (subKey == null)
			return;

		try {
			subKey.deleteSubKey(deleteKeyName);
		} catch (NoSuchKeyException ex) {
			System.err.println("Key '" + keyName + "\\" + deleteKeyName + "' does not exist.");
			return;
		} catch (RegistryException ex) {
			System.err.println("ERROR deleting key '" + keyName + "', " + ex.getMessage());
			return;
		}
	}

	private static void deleteValueCommand(RegistryKey topKey, String keyName, String valueName) {
		RegistryKey subKey = Registry.openSubKeyVerbose(topKey, keyName, RegistryKey.ACCESS_WRITE);

		if (subKey == null)
			return;

		try {
			subKey.deleteValue(valueName);
		} catch (NoSuchValueException ex) {
			System.err.println("Value '" + valueName + "' does not exist.");
			return;
		} catch (RegistryException ex) {
			System.err.println("ERROR deleting value '" + valueName + "', " + ex.getMessage());
			return;
		}
	}

	private static void listKeysCommand(RegistryKey topKey, String keyName) {
		RegistryKey subKey = Registry.openSubKeyVerbose(topKey, keyName, RegistryKey.ACCESS_READ);

		if (subKey == null)
			return;

		try {
			Enumeration enumer = subKey.keyElements();
			for (int kIdx = 0; enumer.hasMoreElements(); ++kIdx) {
				String keyStr = (String) enumer.nextElement();
				System.err.println("Subkey[" + kIdx + "] = '" + keyStr + "'");
			}
		} catch (RegistryException ex) {
			System.err.println("ERROR getting key enumerator, " + ex.getMessage());
			return;
		}
	}

	private static void listValuesCommand(RegistryKey topKey, String keyName) {
		RegistryKey subKey = Registry.openSubKeyVerbose(topKey, keyName, RegistryKey.ACCESS_READ);

		if (subKey == null)
			return;

		try {
			Enumeration enumer = subKey.valueElements();
			for (int kIdx = 0; enumer.hasMoreElements(); ++kIdx) {
				String name = (String) enumer.nextElement();
				System.err.println("Value Name[" + kIdx + "] = '" + name + "'");
			}
		} catch (RegistryException ex) {
			System.err.println("ERROR getting value enumerator, " + ex.getMessage());
			return;
		}
	}


	private static void setMultiStringCommand(RegistryKey topKey, String keyName, String valueName, String data) {
		RegistryKey subKey = Registry.openSubKeyVerbose(topKey, keyName, RegistryKey.ACCESS_WRITE);

		if (subKey == null)
			return;

		String[] strArray = Registry.splitString(data, ";");

		RegMultiStringValue val = new RegMultiStringValue(subKey, valueName, strArray);

		Registry.setValue(subKey, val);
	}

	private static void setStringCommand(RegistryKey topKey, String keyName, String valueName, String data) {
		RegistryKey subKey = Registry.openSubKeyVerbose(topKey, keyName, RegistryKey.ACCESS_WRITE);

		if (subKey == null)
			return;

		RegStringValue val = new RegStringValue(subKey, valueName, data);

		Registry.setValue(subKey, val);
	}

	private static void setBinaryCommand(RegistryKey topKey, String keyName, String valueName, String data) {
		RegistryKey subKey = Registry.openSubKeyVerbose(topKey, keyName, RegistryKey.ACCESS_WRITE);

		if (subKey == null)
			return;

		byte[] binData = data.getBytes();

		RegBinaryValue val = new RegBinaryValue(subKey, valueName, binData);

		Registry.setValue(subKey, val);
	}

	private static void createCommand(RegistryKey topKey, String keyName) {
		RegistryKey subKey;

		try {
			subKey = topKey.createSubKey(keyName, "", RegistryKey.ACCESS_WRITE);
		} catch (RegistryException ex) {
			subKey = null;
			System.err.println("ERROR creating subKey: " + ex.getMessage());
		}

		if (subKey != null) {
			try {
				subKey.flushKey();
				subKey.closeKey();
			} catch (RegistryException ex) {
				subKey = null;
				System.err.println("ERROR flushing and closing key: " + ex.getMessage());
			}
		}

		if (subKey != null) {
			System.err.println("SUCCEEDED " + (subKey.wasCreated() ? "Creating" : "Opening via create") + " Key '" + keyName + "'");
		} else {
			System.err.println("FAILED Creating Key '" + keyName + "'");
		}
	}

	private static RegistryKey openSubKeyVerbose(RegistryKey topKey, String keyName, int access) {
		RegistryKey subKey = null;

		try {
			subKey = topKey.openSubKey(keyName, access);
		} catch (NoSuchKeyException ex) {
			subKey = null;
			System.err.println("Key '" + keyName + "' does not exist.");
		} catch (RegistryException ex) {
			subKey = null;
			System.err.println("ERROR registry error=" + ex.getErrorCode() + ", " + ex.getMessage());
		}

		return subKey;
	}

	private static void setValue(RegistryKey subKey, RegistryValue value) {
		try {
			subKey.setValue(value);
			subKey.flushKey();
		} catch (RegistryException ex) {
			System.err.println("ERROR setting MULTI_SZ value '" + value.getName() + "', " + ex.getMessage());
		}
	}

	public static void dumpHexData(PrintStream out, String title, byte[] buf, int numBytes) {
		PrintWriter wrtr = new PrintWriter(new OutputStreamWriter(out));

		Registry.dumpHexData(wrtr, title, buf, 0, numBytes);
	}

	public static void dumpHexData(PrintWriter out, String title, byte[] buf, int offset, int numBytes) {
		int rows, residue, i, j;
		byte[] save_buf = new byte[ROW_BYTES + 2];
		char[] hex_buf = new char[4];
		char[] idx_buf = new char[8];
		char[] hex_chars = new char[20];

		hex_chars[0] = '0';
		hex_chars[1] = '1';
		hex_chars[2] = '2';
		hex_chars[3] = '3';
		hex_chars[4] = '4';
		hex_chars[5] = '5';
		hex_chars[6] = '6';
		hex_chars[7] = '7';
		hex_chars[8] = '8';
		hex_chars[9] = '9';
		hex_chars[10] = 'A';
		hex_chars[11] = 'B';
		hex_chars[12] = 'C';
		hex_chars[13] = 'D';
		hex_chars[14] = 'E';
		hex_chars[15] = 'F';

		out.println(title + " - " + numBytes + " bytes.");

		rows = (numBytes + (ROW_BYTES - 1)) / ROW_BYTES;
		residue = (numBytes % ROW_BYTES);

		for (i = 0; i < rows; i++) {
			int hexVal = (i * ROW_BYTES);
			idx_buf[0] = hex_chars[((hexVal >> 12) & 15)];
			idx_buf[1] = hex_chars[((hexVal >> 8) & 15)];
			idx_buf[2] = hex_chars[((hexVal >> 4) & 15)];
			idx_buf[3] = hex_chars[(hexVal & 15)];

			String idxStr = new String(idx_buf, 0, 4);
			out.print(idxStr + ": ");

			for (j = 0; j < ROW_BYTES; j++) {
				if (i == (rows - 1) && j >= residue) {
					save_buf[j] = ' ';
					out.print("   ");
					if (j == ROW_QTR1 || j == ROW_HALF || j == ROW_QTR2)
						out.print(' ');
				} else {
					save_buf[j] = buf[offset + (i * ROW_BYTES) + j];

					hex_buf[0] = hex_chars[(save_buf[j] >> 4) & 0x0F];
					hex_buf[1] = hex_chars[save_buf[j] & 0x0F];

					out.print(hex_buf[0]);
					out.print(hex_buf[1]);
					out.print(' ');

					if (j == ROW_QTR1 || j == ROW_HALF || j == ROW_QTR2)
						out.print(' ');

					if (save_buf[j] < 0x20 || save_buf[j] > 0x7E)
						save_buf[j] = (byte) '.';
				}
			}

			String saveStr = new String(save_buf, 0, j);
			out.println(" | " + saveStr + " |");
		}

		out.flush();
	}

	/**
	 * Split a string into a string array containing the substrings between the
	 * delimiters.
	 * 
	 * NOTE This method WILL <strong>NOT</strong> return an empty token at the
	 * end of the array that is returned, if the string ends with the delimiter.
	 * If you wish to have a property string array that ends with the delimiter
	 * return an empty string at the end of the array, use
	 * <code>vectorString()</code>.
	 */

	static public String[] splitString(String splitStr, String delim) {
		int i, count;
		String[] result;
		StringTokenizer toker;

		toker = new StringTokenizer(splitStr, delim);

		count = toker.countTokens();

		result = new String[count];

		for (i = 0; i < count; ++i) {
			try {
				result[i] = toker.nextToken();
			} catch (NoSuchElementException ex) {
				result = null;
				break;
			}
		}

		return result;
	}

	public static String[] parseArgumentString(String argStr) {
		String[] result = null;

		Vector vector = Registry.parseArgumentVector(argStr);

		if (vector != null && vector.size() > 0) {
			result = new String[vector.size()];
			vector.copyInto(result);
		}

		return result;
	}

	public static Vector parseArgumentVector(String argStr) {
		Vector result = new Vector();
		StringBuilder argBuf = new StringBuilder();

		boolean backSlash = false;
		boolean matchSglQuote = false;
		boolean matchDblQuote = false;

		for (int cIdx = 0; cIdx < argStr.length(); ++cIdx) {
			char ch = argStr.charAt(cIdx);

			switch (ch) {
			//
			// W H I T E S P A C E
			//
			case ' ':
			case '\t':
			case '\n':
			case '\r':
				if (backSlash) {
					argBuf.append(ch);
					backSlash = false;
				} else if (matchSglQuote || matchDblQuote) {
					argBuf.append(ch);
				} else if (argBuf.length() > 0) {
					result.addElement(argBuf.toString());
					argBuf.setLength(0);
				}
				break;

			case '\\':
				if (backSlash) {
					argBuf.append("\\");
				}
				backSlash = !backSlash;
				break;

			case '\'':
				if (backSlash) {
					argBuf.append("'");
					backSlash = false;
				} else if (matchSglQuote) {
					result.addElement(argBuf.toString());
					argBuf.setLength(0);
					matchSglQuote = false;
				} else if (!matchDblQuote) {
					matchSglQuote = true;
				}
				break;

			case '"':
				if (backSlash) {
					argBuf.append("\"");
					backSlash = false;
				} else if (matchDblQuote) {
					result.addElement(argBuf.toString());
					argBuf.setLength(0);
					matchDblQuote = false;
				} else if (!matchSglQuote) {
					matchDblQuote = true;
				}
				break;

			default:
				if (backSlash) {
					switch (ch) {
					case 'b':
						argBuf.append('\b');
						break;
					case 'f':
						argBuf.append('\f');
						break;
					case 'n':
						argBuf.append('\n');
						break;
					case 'r':
						argBuf.append('\r');
						break;
					case 't':
						argBuf.append('\t');
						break;

					default:
						char ch2 = argStr.charAt(cIdx + 1);
						char ch3 = argStr.charAt(cIdx + 2);
						if ((ch >= '0' && ch <= '7') && (ch2 >= '0' && ch2 <= '7') && (ch3 >= '0' && ch3 <= '7')) {
							int octal = (((ch - '0') * 64) + ((ch2 - '0') * 8) + (ch3 - '0'));
							argBuf.append((char) octal);
							cIdx += 2;
						} else if (ch == '0') {
							argBuf.append('\0');
						} else {
							argBuf.append(ch);
						}
						break;
					}
				} else {
					argBuf.append(ch);
				}

				backSlash = false;
				break;
			}
		}

		if (argBuf.length() > 0) {
			result.addElement(argBuf.toString());
		}

		return result;
	}

}
