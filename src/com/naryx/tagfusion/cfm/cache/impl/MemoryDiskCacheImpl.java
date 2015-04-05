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
 *  $Id: MemoryDiskCacheImpl.java 2426 2014-03-30 18:53:18Z alan $
 *  
 *  size 							int size of items in memory
 *  diskpersistent 		true / false
 *  diskspooldir 			full path
 *  diskcleanonstart 	true/false
 *  diskmaxsizemb     maximum size on disk
 */
package com.naryx.tagfusion.cfm.cache.impl;

import java.io.File;
import java.io.IOException;

import org.aw20.io.FileUtil;

import com.nary.io.FileUtils;
import com.naryx.tagfusion.cfm.cache.CacheFactory;
import com.naryx.tagfusion.cfm.cache.CacheInterface;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;

public class MemoryDiskCacheImpl extends java.util.LinkedHashMap<String,CacheUnit> implements CacheInterface {
	private static final long serialVersionUID = 1L;

	private cfStructData props;
	
	private int	maxItems 		= 0;
	private File	cacheDir	= null;
	
	private int	
			statsSet = 0, 
			statsGet = 0,
			statsDelete = 0, 
			statsMiss = 0,
			statsMissAge = 0, 
			statsHitMem = 0,
			statsHitDisk = 0, 
			statsDiskPage = 0,
			statsDiskPrune = 0;

	@Override
	public cfStructData getStats() {
		cfStructData	stats	= new cfStructData();
		stats.setData("set", 			statsSet);
		stats.setData("get", 			statsGet);
		stats.setData("miss", 		statsMiss);
		stats.setData("missage",	statsMissAge);
		stats.setData("remove", 	statsDelete);
		stats.setData("hitmem", 	statsHitMem);
		stats.setData("hitdisk", 	statsHitDisk);
		stats.setData("diskpage", statsDiskPage);
		stats.setData("diskprune",statsDiskPrune);
		stats.setData("inmem", 		super.size() );
		return stats;
	}


	
	@Override
	public void setProperties(String region, cfStructData _props) throws Exception {
		this.props	= _props;
		
		// Maximum the size
		if ( !props.containsKey("size") )
			props.setData("size", new cfNumberData(100) );
		
		if ( !props.containsKey("diskmaxsizemb") )
			props.setData("diskmaxsizemb", new cfNumberData(25) );
		
		maxItems	= props.getData("size").getInt();
		
		cfEngine.log( getName() + "." + region + ": MaxSize: " + maxItems );


		// Determine the disk side 
		if ( props.containsKey("diskpersistent") ){
			if ( props.getData("diskpersistent").getBoolean() ){

				if ( props.containsKey("diskspooldir") ){
					cacheDir	= new File( props.getData("diskspooldir").getString() );
				}else{
					cacheDir	= new File( cfEngine.thisPlatform.getFileIO().getWorkingDirectory(), "cache-" + region );
				}

				if ( !cacheDir.isDirectory() && !cacheDir.mkdirs() )
					throw new Exception( "Failed to create: " + cacheDir );

				cfEngine.log( getName() + "." + region + ": Directory set: " + cacheDir );

				if ( props.containsKey("diskcleanonstart") && props.getData("diskcleanonstart").getBoolean() ){
					FileUtils.recursiveDelete( cacheDir, false );
					cfEngine.log( getName() + "." + region + ": Directory cleaned");
				}
			}
		}else{
			if ( cacheDir != null )
				FileUtils.recursiveDelete( cacheDir, false );
			
			cacheDir = null;
		}
		
	}
	
	
	@Override
	public void set(String id, cfData data, long ageMS, long idleTime) {
		statsSet++;
		
		String idMd5	= CacheFactory.createCacheKey(id);

		synchronized(this){
			CacheUnit	cu	= new CacheUnit( id, data, ageMS );
			super.put(idMd5, cu);
		}
		
		// if we are spooling to disk, then we need to delete the one that is on disk
		if ( cacheDir != null )
			new File( cacheDir, idMd5 + ".cache" ).delete();
	}

	
	@Override
	public cfData get(String id) {
		statsGet++;
		String idMd5 = CacheFactory.createCacheKey(id);

		// Check to see if in memory
		synchronized(this){

			if ( this.containsKey(idMd5) ){
				CacheUnit	cu	= super.get(idMd5);
	
				if ( cu.stillYoung() ){
					statsHitMem++;
					return cu.val;
				}else{
					super.remove(idMd5);
					if ( cacheDir != null )
						new File( cacheDir, idMd5 + ".cache" ).delete();
	
					statsMissAge++;
					return null;
				}
			}

			// Check to see if on disk
			if ( cacheDir != null ){
				File fu	= new File( cacheDir, idMd5 + ".cache" );
				if ( fu.isFile() ){
	
					CacheUnit	cu = (CacheUnit)org.aw20.io.FileUtil.loadClass(fu);
					fu.delete();
	
					if ( cu != null ){
						if ( cu.stillYoung() ){
							super.put( idMd5, cu );
							statsHitDisk++;
							return cu.val;
						}else{
							statsMissAge++;
							return null;
						}
					}
				}
			}
		
		}

		// not found
		statsMiss++;
		return null;
	}

	
	
	@Override
	public void delete(String id, boolean exact) {
		statsDelete++;
		
		String idMd5	= CacheFactory.createCacheKey(id);

		synchronized(this){
			this.remove(idMd5);
		}
		
		if ( cacheDir != null )
			new File( cacheDir, idMd5 + ".cache" ).delete();		
	}

	
	
	@Override
	public synchronized void deleteAll() {
		super.clear();
		
		if ( cacheDir != null ){
			try {
				FileUtils.recursiveDelete( cacheDir, false );
			} catch (IOException e) {
				cfEngine.log( getName() + " " + e.getMessage() );
			}
		}
	}

	
	
	@Override
	public String getName() {
		return "MemoryDiskCache";
	}

	@Override
	public cfArrayData getAllIds() {
		return cfArrayData.createArray(1);
	}

	@Override
	public cfStructData getProperties() {
		return props;
	}

	@Override
	public void shutdown() {
		clear();
	}

	protected synchronized boolean removeEldestEntry(java.util.Map.Entry eldest) {
		if ( size() > maxItems ){

			if ( cacheDir != null ){
				CacheUnit	cu	= (CacheUnit)eldest.getValue();

				// Only page to the disk if this object is still young
				if ( cu.stillYoung() ){
					try{
						File f = new File( cacheDir, eldest.getKey() + ".cache" );
						f.delete();
	
						FileUtil.saveClass( f, cu );
						statsDiskPage++;
						pruneDiskSpace();
					}catch(Exception e){
						cfEngine.log( getName() + " " + e.getMessage() );
					}
				}
			}
			
			return true;
		}else
			return false;
	}

	
	/**
	 * Runs around the disk space and make sure we have enough space, deleting files if
	 * need be.
	 */
	private void pruneDiskSpace(){
		long maxBytes = 0;
		try {
			maxBytes	= props.getData("diskmaxsizemb").getLong() * 1024000;
		} catch (dataNotSupportedException e) {
			maxBytes	= 25 * 1024000;
		}

		// Go through the files making sure we have enough space
		File[] allFiles	= cacheDir.listFiles();
		if ( allFiles != null && allFiles.length > 0 ){
			long totalBytes = 0, fileLen = 0;

			for (int x=0; x < allFiles.length; x++ ){
				fileLen			= allFiles[x].length();
				totalBytes += fileLen;
				
				if ( totalBytes > maxBytes ){
					allFiles[x].delete();
					totalBytes -= fileLen;
					statsDiskPrune++;
				}
			}
		}
	}
	
}