/* 
 *  Copyright (C) 2000 - 2012 TagServlet Ltd
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
 *  $Id: cfCACHECONTENT.java 2140 2012-06-28 15:59:49Z alan $
 *  
 *  
 * Implements a mechanism to allow you to cache blocks of content.
 * 
 * BlueDragon Only Tag
 * 
 * Usage:
 * <CFCACHECONTENT NAME="" CACHEDWITHIN="" REGION="">
 *   normal processing
 * </CFCACHECONTENT>
 * 
 * BlueDragon configuration parameters:
 * 
 * server.cfcachecontent.total
 * 		This is the total number of items that will be held in memory at any one time
 * 
 */
 
package com.naryx.tagfusion.cfm.tag.ext;

import java.io.Serializable;

import org.aw20.util.StringUtil;

import com.naryx.tagfusion.cfm.cache.CacheFactory;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;
import com.naryx.tagfusion.xmlConfig.xmlCFML;

public class cfCACHECONTENT extends cfTag implements Serializable  {
	static final long serialVersionUID 	= 1;

	public String getEndMarker(){	return "</CFCACHECONTENT>"; }
	
	public static void init( xmlCFML configFile ) {
		int cacheCount	= StringUtil.toInteger( configFile.getString("server.cfcachecontent.total"), 50 );
		CacheFactory.setMemoryDiskCache( "CFCACHECONTENT", cacheCount, true, 25 );
	}

	
	public java.util.Map getInfo(){
		return createInfo("control", "Used to store a specific amount of content to cache, improving page rendering performance and server memory costs. Use this function when its not necessary to serve dynamic content with every page request. Cache duration can be specified to be any lenth of time using the CACHEDWITHIN attribute. Additionally, cache can be manually flushed by setting the ACTION attribute to 'flush' and the CACHENAME attribute to the name of the cached resource that you wish to flush.");
	}
  
	
	public java.util.Map[] getAttInfo(){
		return new java.util.Map[] {
			createAttInfo("NAME", 				"The name of the cache", "YES", false ),
			createAttInfo("CACHEDWITHIN", "The timespan (CreateTimeSpan) for the life of the content in the cache", "", false ),
			createAttInfo("REGION", 			"The type of cache to use; this can be any of the registered cache handlers", "default cache", false ),
		};
	}

		
	protected void defaultParameters( String _tag ) throws cfmBadFileException {
		defaultAttribute( "REGION", "CFCACHECONTENT" );
		
	  parseTagHeader( _tag );

	  if ( !containsAttribute("NAME") && !containsAttribute("CACHENAME") )
			throw newBadFileException("Missing", "Missing 'NAME' attribute");
	}
	
	
	public cfTagReturnType render( cfSession _Session ) throws cfmRunTimeException {
		// Get the Cache Engine
		String region = getDynamic( _Session, "REGION" ).getString();
		if ( !CacheFactory.isCacheEnabled( region ) )
			throw newRunTimeException( "REGION='" + region + "' has not been enabled" );
		
		
		// Get the timeout
		long expireTime = -1;
		if ( containsAttribute("CACHEDWITHIN") ){
			double timeOut = getDynamic(_Session, "CACHEDWITHIN" ).getDouble();
			if ( timeOut > 0 )
				expireTime = (long)(timeOut * (double)86400000);
		}

		// Get the key
		String cacheName = null;
		if ( containsAttribute("CACHENAME") )
			cacheName	= getDynamic( _Session, "CACHENAME" ).getString();
		else
			cacheName	= getDynamic( _Session, "NAME" ).getString();

		cfData	content		= CacheFactory.getCacheEngine(region).get( cacheName );
		if ( content == null ){
			synchronized( CacheFactory.getLock( cacheName ) ) {
        try{

					// we need to check the cache again since another thread might have set this entry
					content = CacheFactory.getCacheEngine(region).get( cacheName );
					if (content == null){
						content = new cfStringData( renderToString(_Session).getOutput() );
						CacheFactory.getCacheEngine(region).set( cacheName, content, expireTime, expireTime );
					}

        }finally{
          // Be sure to remove the lock for this entry
        	CacheFactory.removeLock( cacheName );
        }
			}
		}
		
		/* The content was retrieve, now send it out to the client */
		_Session.write( content.getString() );
		
		return cfTagReturnType.NORMAL;
	}

}