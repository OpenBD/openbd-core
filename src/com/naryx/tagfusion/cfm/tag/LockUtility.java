/* 
 * Copyright (C) 2000 - 2008 TagServlet Ltd
 *
 * This file is part of the BlueDragon Java Open Source Project.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package com.naryx.tagfusion.cfm.tag;

import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.parser.cfLData;
import com.naryx.tagfusion.cfm.parser.runTime;

public class LockUtility {

	public static synchronized cfLockingObject getLock( cfSession _Session, String _name ) throws cfmRunTimeException {
		cfLockingObject lockOb = getLockingObject( _Session, _name );
		if ( lockOb == null ) {
			lockOb = new cfLockingObject();
			_Session.setData( _name, lockOb );
		}
		lockOb.incrRefCount();
		return lockOb;
	}
	
	public static synchronized void freeLock( cfSession _Session, String _name, cfLockingObject lockOb ) throws cfmRunTimeException {
		lockOb.decrRefCount();
		if ( lockOb.isFree() ) {
			_Session.deleteData( _name );
		}
	}

	public static cfLockingObject getLockingObject( cfSession _Session, String _name ) {
		cfData lockOb = null;
		
		try {
			lockOb = runTime.runExpression( _Session, _name, false );
			if ( lockOb instanceof cfLData ) {
				if ( ((cfLData)lockOb).exists() ) {
					lockOb = ((cfLData)lockOb).Get( _Session.getCFContext() );
				} else {
					lockOb = null;
				}
			}
		} catch( cfmRunTimeException ignored ) {
		} // lockOb doesn't exist
		
		if ( lockOb instanceof cfLockingObject ) {
			return (cfLockingObject)lockOb;
		}
		
		return null;
	}

}
