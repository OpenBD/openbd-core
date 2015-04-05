/* 
 *  Copyright (C) 2012 TagServlet Ltd
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
 *  $Id: SessionStorageFactory.java 2131 2012-06-27 19:02:26Z alan $
 */
package com.naryx.tagfusion.cfm.application.sessionstorage;

import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class SessionStorageFactory {

	public enum SessionEngine {
		NONE,
  	INTERNAL,
  	J2EE,
  	MONGO,
  	MEMCACHED
  };
	
	public static SessionStorageInterface	NULL_STORAGESESSION	= new SessionStorageNullImpl();
	

	public static SessionStorageInterface	createStorage( String appName, SessionStorageInterface sessionStorage, boolean bSessionManagement, boolean bJ2EESessionManagement, String storage ) throws cfmRunTimeException {
	
		if ( !bSessionManagement ){
			
			if ( sessionStorage != null && sessionStorage.getType() == SessionEngine.NONE )
				return null;
			else{
				
				if ( sessionStorage != null )
					sessionStorage.shutdown();
				
				return NULL_STORAGESESSION;
			}
			
		} else if ( bJ2EESessionManagement ){
			
			if ( sessionStorage != null && sessionStorage.getType() == SessionEngine.J2EE )
				return null;
			else{
				
				if ( sessionStorage != null )
					sessionStorage.shutdown();
				
				return new SessionStorageJ2EEImpl(appName);
			}
			
		} else if ( storage == null ){
			
			if ( sessionStorage != null && sessionStorage.getType() == SessionEngine.INTERNAL )
				return null;
			else{
				
				if ( sessionStorage != null )
					sessionStorage.shutdown();
				
				return new SessionStorageInternalImpl(appName);
			}
			
		} else if ( storage.startsWith("mongo://") ) {
		
			if ( sessionStorage != null && sessionStorage.getType() == SessionEngine.MONGO )
				return null;
			else{
				
				if ( sessionStorage != null )
					sessionStorage.shutdown();
				
				try{
					return new SessionStorageMongoImpl(appName, storage);
				}catch( Exception e){
					throw new cfmRunTimeException( catchDataFactory.generalException("SessionStorageFactory", e.getMessage()) );
				}
			}

			
		} else if ( storage.startsWith("memcached://") ) {
		
			if ( sessionStorage != null && sessionStorage.getType() == SessionEngine.MEMCACHED )
				return null;
			else{
				
				if ( sessionStorage != null )
					sessionStorage.shutdown();
				
				try{
					return new SessionStorageMemcachedImpl(appName, storage);
				}catch( Exception e){
					throw new cfmRunTimeException( catchDataFactory.generalException("SessionStorageFactory", e.getMessage()) );
				}
			}

		}
		
		
		return null;
	}
	
}
