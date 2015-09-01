/*
 *  Copyright (C) 2000 - 2015 TagServlet Ltd
 *
 *  This file is part of Open BlueDragon (OpenBD) CFML Server Engine.
 *
 *  OpenBD is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  Free Software Foundation,version 3.
 *
 *  OpenBD is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenBD.  If not, see http://www.gnu.org/licenses/
 *
 *  Additional permission under GNU GPL version 3 section 7
 *
 *  If you modify this Program, or any covered work, by linking or combining
 *  it with any of the JARS listed in the README.txt (or a modified version of
 *  (that library), containing parts covered by the terms of that JAR, the
 *  licensors of this Program grant you additional permission to convey the
 *  resulting work.
 *  README.txt @ http://www.openbluedragon.org/license/README.txt
 *
 *  http://openbd.org/
 */

package com.naryx.tagfusion.expression.function.string;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javolution.util.FastMap;

import org.aw20.util.SystemClockEvent;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfLOCK;
import com.naryx.tagfusion.cfm.tag.cfLockingObject;
import com.naryx.tagfusion.expression.function.functionBase;


/*
 * Reads a file, converting it to JSON and caches this object
 */
public class JsonFileRead extends functionBase {

	private static final long serialVersionUID = 1L;
	private static long CACHE_IDLE_MS = 60000;
	private static Map<File, JsonElement> cacheMap;

	static {
		cacheMap = new FastMap<File, JsonElement>();
		org.aw20.util.SystemClock.setListenerMinute( new SystemClockEvent() {

			@Override
			public void clockEvent( int arg0 ) {

				if ( cacheMap.isEmpty() )
					return;

				synchronized ( cacheMap ) {

					Iterator<JsonElement> it = cacheMap.values().iterator();
					while ( it.hasNext() ) {
						JsonElement je = it.next();
						if ( ( System.currentTimeMillis() - je.lastRead ) > CACHE_IDLE_MS )
							it.remove();
					}

				}

			}

		} );
	}

	private class JsonElement {

		long lastModifiedAtRead;
		long lastRead;
		cfData data;


		private boolean isValid( long lastModified ) {
			return lastModified == lastModifiedAtRead;
		}
	}


	public JsonFileRead() {
		min = 1;
		max = 3;
		setNamedParams( new String[] { "filesrc", "strictmapping", "charset" } );
	}


	@Override
	public String[] getParamInfo() {
		return new String[] {
				"path to the file source, full path name is required.",
				"Flag to determine if CFML Query objects should be recognized and converted to a Query object; defaults to true",
				"the character set to use for reading the file"
		};
	}


	@Override
	public java.util.Map getInfo() {
		return makeInfo(
				"conversion",
				"Reads and decodes the given file to JSON.  This function caches the result so it won't keep reading/decoding the file on every request.  The cache will remove items from memory that haven't been touched in over 60 seconds.",
				ReturnType.OBJECT );
	}


	@Override
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {

		// Pull in the file name
		String srcname = getNamedStringParam( argStruct, "filesrc", null );
		if ( srcname == null )
			throwException( _session, "missing 'filesrc' attribute" );

		File srcFile = new File( srcname );
		if ( !srcFile.exists() )
			throwException( _session, "file does not exist: " + srcname );


		// Check to see if we have it in the cache
		JsonElement je = getFromMap( srcFile );
		if ( je != null ) {
			return je.data;
		}


		// We must read the file and put it into the cache
		String lockname	= org.aw20.security.MD5.getDigest( srcFile.toString() );

		cfLockingObject lock = cfLOCK.getLock( _session, lockname );
		try {
			if ( lock.lock( cfLOCK.TYPE_EXCLUSIVE, 1000 ) ) {
				try {

					// Do a quick check to see if hasn't been done in a previous lock
					je = getFromMap( srcFile );
					if ( je != null ) {
						return je.data;
					}

					// Now to read it
					String charset = getNamedStringParam( argStruct, "charset", null );
					String json = null;

					try {
						if ( charset == null )
							json = org.aw20.io.FileUtil.readToString( srcFile );
						else
							json = org.aw20.io.FileUtil.readToString( srcFile, charset );
					} catch ( IOException ioe ) {
						throwException( _session, "file: " + srcname + "; " + ioe.getMessage() );
					}

					// Deserialize the object now
					argStruct.setNamedBasedMode();
					argStruct.setData( "jsonstring", json );

					je = new JsonElement();
					je.data = new DeserializeJSONJackson().execute( _session, argStruct );
					je.lastRead = System.currentTimeMillis();
					je.lastModifiedAtRead = srcFile.lastModified();

					synchronized ( cacheMap ) {
						cacheMap.put( srcFile, je );
					}

					return je.data;

				} finally {
					lock.unlock( cfLOCK.TYPE_EXCLUSIVE );
				}
			} else {
				throwException( _session, "file: " + srcname + "; timeout waiting to lock" );
			}
		} finally {
			cfLOCK.freeLock( _session, lockname, lock );
		}

		return null; // will never reach here but keeps the compiler happy
	}


	private JsonElement getFromMap( File srcFile ) {
		JsonElement je;
		synchronized ( cacheMap ) {
			je = cacheMap.get( srcFile );

			if ( je != null ) {
				if ( je.isValid( srcFile.lastModified() ) ) {
					je.lastRead = System.currentTimeMillis();
					return je;
				} else {
					cacheMap.remove( srcFile );
				}
			}
		}
		return null;
	}

}