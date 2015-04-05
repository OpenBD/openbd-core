/* 
 *  Copyright (C) 2000 - 2014 TagServlet Ltd
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
 *  $Id: BackgroundUploader.java 2474 2015-01-13 15:24:17Z alan $
 */

package org.alanwilliamson.amazon.s3;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alanwilliamson.amazon.AmazonBase;
import org.alanwilliamson.amazon.AmazonKey;
import org.aw20.io.FileUtil;
import org.aw20.util.DateUtil;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.SSECustomerKey;
import com.amazonaws.services.s3.model.StorageClass;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.bluedragon.plugin.ObjectCFC;
import com.bluedragon.plugin.PluginManager;
import com.nary.io.FileUtils;
import com.naryx.tagfusion.cfm.application.cfAPPLICATION;
import com.naryx.tagfusion.cfm.application.cfApplicationData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.variableStore;

public class BackgroundUploader extends Thread {

	private static BackgroundUploader thisInst = null;
	
	public static void onStart(){
		try {
			thisInst = new BackgroundUploader( FileUtils.checkAndCreateDirectory(cfEngine.thisPlatform.getFileIO().getWorkingDirectory(), "amazons3uploader", false) );
		} catch (Exception E) {
			cfEngine.log("AmazonS3Write.BackgroundUploader failed to create all the CFMAIL spooling directorys: " + cfEngine.thisPlatform.getFileIO().getWorkingDirectory() + "/amazons3uploader" );
		}
	}
	
	public static void onShutdown(){
		thisInst.bRunning = false;
		thisInst.interrupt();
		try {
			thisInst.join();
		} catch (InterruptedException e) {}
	}
	
	public static void acceptFile(AmazonKey amazonKey, String bucket, String key, Map<String,String>	metadata, StorageClass storage, String localpath, int retry, int retryseconds, boolean deletefile, String callback, String callbackdata, String appname, String acl, String aes256key, Map<String,String>	customheaders){
		if ( thisInst == null ){
			cfEngine.log("AmazonS3Write.BackgroundUploader not active due to missing directory. File failed to be uploaded");
			return;
		}
		
		Map<String,Object>	jobFile	= new HashMap<String,Object>();
		jobFile.put("id", com.nary.util.UUID.generateKey() );
		jobFile.put("amazonkey", amazonKey);
		jobFile.put("bucket", bucket);
		jobFile.put("key", key);
		jobFile.put("storage", storage);
		jobFile.put("localpath", localpath);
		jobFile.put("retry", retry);
		jobFile.put("retryms", retryseconds * 1000 );
		jobFile.put("deletefile", deletefile);

		if ( metadata != null && !metadata.isEmpty() )
			jobFile.put("metadata", metadata);

		if ( acl != null && !acl.isEmpty() )
			jobFile.put("acl", acl);

		if ( aes256key != null && !aes256key.isEmpty() )
			jobFile.put("aes256key", aes256key);
		
		if ( callback != null && !callback.isEmpty() )
			jobFile.put("callback", callback);
		
		if ( callbackdata != null && !callbackdata.isEmpty() )
			jobFile.put("callbackdata", callbackdata);
		
		if ( customheaders != null && !customheaders.isEmpty() )
			jobFile.put("customheaders", customheaders);
		
		if ( appname != null )
			jobFile.put("appname", appname);

		jobFile.put("attempt", 0);
		jobFile.put("attemptdate", System.currentTimeMillis() - 1000 );
		
		thisInst.acceptFile( jobFile );
	}

	private File workingDirectory;
	private List<Map<String,Object>> fileList;
	private boolean bRunning = true;

	private BackgroundUploader(File workingDirectory){
		super("AmazonS3BackgroundUploader");

		this.workingDirectory = workingDirectory;
		fileList	= new LinkedList<Map<String,Object>>();
		
		// Load the previous files
		File[]	jobsDisk	= workingDirectory.listFiles();
		if ( jobsDisk != null && jobsDisk.length > 0 ){
			
			for ( int x=0; x < jobsDisk.length; x++ ){
				if ( !jobsDisk[x].getName().endsWith(".job" ) )
					continue;


				@SuppressWarnings("unchecked")
				Map<String, Object> jobFile = (Map<String, Object>)FileUtil.loadClass( jobsDisk[x] );
				if ( jobFile == null ){
					jobsDisk[x].delete();
				}else{
					fileList.add( jobFile );
				}
			}
			
			if (!fileList.isEmpty()){
				cfEngine.log("AmazonS3Write.BackgroundUploader files to upload=" + fileList.size() );
			}
		}
		
		setPriority(MIN_PRIORITY);
		setDaemon(true);
		start();
	}
	
	private void acceptFile(Map<String, Object> jobFile) {
		saveFile( jobFile );
		
		synchronized( fileList ){
			fileList.add( jobFile );
			fileList.notify();
		}
	}
	
	public void run(){
		
		while ( bRunning ){
			
			// Wait until we have a file in the list
			while ( fileList.isEmpty() ){
				try {
					synchronized(fileList){
						fileList.wait( 60 * 1000 );
					}
				} catch (InterruptedException e) {
					break;
				}
			}
			
			Map<String, Object> jobFile	= takeNextJob();
			if ( jobFile == null ){
				/**
				 * If we get here; then we have no jobs that are ready to be sent to S3 yet; their retrytimeout hasn't
				 * been expired.
				 */
				try {
					sleep( 1000 );
				} catch (InterruptedException e) {
					break;
				}
			}else{
				uploadFile( jobFile );
			}		
		}
		
		cfEngine.log("AmazonS3Write.BackgroundUploader: Shutdown");
	}

	
	private void uploadFile(Map<String, Object> jobFile) {
		
		File localFile	= new File( (String)jobFile.get("localpath") );
		if ( !localFile.isFile() ){
			removeJobFile( jobFile );
			callbackCfc( jobFile, false, "local file no longer exists" );
			cfEngine.log("AmazonS3Write.BackgroundUploader: file no longer exists=" + localFile.getName() );
			return;
		}


		
		// Setup the object data
		ObjectMetadata	omd = new ObjectMetadata();
		if ( jobFile.containsKey("metadata") )
			omd.setUserMetadata((Map<String,String>)jobFile.get("metadata"));
		
		
		TransferManager tm = null;
		AmazonS3 s3Client = null;
		try {
			AmazonKey amazonKey = (AmazonKey)jobFile.get("amazonkey");
			s3Client		= new AmazonBase().getAmazonS3(amazonKey);
			
			PutObjectRequest	por	= new PutObjectRequest((String)jobFile.get("bucket"),(String)jobFile.get("key"),localFile);
			por.setMetadata(omd);
			por.setStorageClass((StorageClass)jobFile.get("storage"));
			
			if ( jobFile.containsKey("acl") )
				por.setCannedAcl( amazonKey.getAmazonCannedAcl( (String)jobFile.get("acl") ) );
			
			if ( jobFile.containsKey("aes256key") )
				por.setSSECustomerKey( new SSECustomerKey((String)jobFile.get("aes256key")) );
			
			if ( jobFile.containsKey("customheaders") ){
				Map<String,String> customheaders = (Map)jobFile.get("customheaders");
				
				Iterator<String> it	 = customheaders.keySet().iterator();
				while ( it.hasNext() ){
					String k = it.next();
					por.putCustomRequestHeader( k, customheaders.get(k) );
				}
			}

			
			long startTime	= System.currentTimeMillis();
			tm = new TransferManager( s3Client );
			Upload upload = tm.upload(por);
			upload.waitForCompletion();

			log( jobFile, "Uploaded; timems=" + (System.currentTimeMillis() - startTime) );

			removeJobFile(jobFile);
			callbackCfc( jobFile, true, null );
			
			if ( (Boolean)jobFile.get("deletefile") )
				localFile.delete();
			
		} catch (Exception e) {
			log( jobFile, "Failed=" + e.getMessage() );
			
			callbackCfc( jobFile, false, e.getMessage() );
			
			int retry		= (Integer)jobFile.get("retry");
			int attempt	= (Integer)jobFile.get("attempt") + 1;
			
			if ( retry == attempt ){
				removeJobFile(jobFile);
			}else{
				jobFile.put("attempt", attempt );
				jobFile.put("attemptdate", System.currentTimeMillis() + (Long)jobFile.get("retryms") );
				acceptFile(jobFile);
			}
			
			if ( s3Client != null )
				cleanupMultiPartUploads( s3Client, (String)jobFile.get("bucket") );
			
		} finally {
			if ( tm != null )
				tm.shutdownNow(true);
		}

	}

	
	private void cleanupMultiPartUploads( AmazonS3 s3Client, String bucket ){
		TransferManager tm = new TransferManager(s3Client);        
    try {
    	tm.abortMultipartUploads(bucket, new Date(System.currentTimeMillis() - DateUtil.DAY_MS ));
    } catch (AmazonClientException amazonClientException) {
    	cfEngine.log("AmazonS3Write.BackgroundUploader.cleanupMultiPartUploads():" + amazonClientException.getMessage() );
    }
    tm.shutdownNow(true);
	}
	
	
	private void callbackCfc(Map<String, Object> jobFile, boolean success, String errMessage ){
		String callback = (String)jobFile.get("callback");
		if ( callback == null )
			return;
		
		String callbackdata = (String)jobFile.get("callbackdata");
		if ( callbackdata == null )
			callbackdata = "";
		
		String appname = (String)jobFile.get("appname");
		final cfSession tmpSession = PluginManager.getPlugInManager().createBlankSession();
		if (appname != null) {
			cfApplicationData appData = cfAPPLICATION.getAppManager().getAppData(tmpSession, appname);
			tmpSession.setQualifiedData(variableStore.APPLICATION_SCOPE, appData);
		}

		// Create the CFC we want to call
		try {
			final ObjectCFC cfc = PluginManager.getPlugInManager().createCFC(tmpSession, callback);
			cfc.addArgument("file", 				(String)jobFile.get("localpath"));
			cfc.addArgument("success", 			success );
			cfc.addArgument("callbackdata", callbackdata);
			cfc.addArgument("error", 				errMessage == null ? "" : errMessage );
			
			new Thread() {
				public void run() {
					setName("AmazonS3Write.BackgroundUploader.onAmazonS3Write()");
					try {
						cfc.runMethod(tmpSession, "onAmazonS3Write");
					} catch (cfmRunTimeException rte) {
						rte.handleException(tmpSession);
					} catch (Exception e) {
						cfEngine.log("AmazonS3Write.BackgroundUploader.onAmazonS3Write.thread():" + e.getMessage());
					} finally {
						tmpSession.pageEnd();
						tmpSession.close();
					}
				}
			}.start();

		} catch (Exception e) {
			cfEngine.log("AmazonS3Write.BackgroundUploader.onAmazonS3Write():" + e.getMessage());
		}
	}
	
	private void log(Map<String, Object> jobFile, String action){
		StringBuilder sb = new StringBuilder(64);
		
		File localFile	= new File( (String)jobFile.get("localpath") );
		
		sb.append("AmazonS3Write.BackgroundUploader: ")
			.append("bucket=").append( (String)jobFile.get("bucket") ).append("; ")
			.append("key=").append( (String)jobFile.get("key") ).append("; ")
			.append("localfile=").append( localFile.getName() ).append("; ")
			.append("size=").append( localFile.length() ).append("; ")
			.append(action)
			;
		
		cfEngine.log( sb.toString() );
	}
	
	private void removeJobFile(Map<String, Object> jobFile){
		new File(workingDirectory, jobFile.get("id") + ".job").delete();
	}
	
	private void saveFile(Map<String, Object> jobFile){
		FileUtil.saveClass( new File(workingDirectory, jobFile.get("id") + ".job"), jobFile);
	}
	
	private Map<String, Object> takeNextJob() {
		synchronized( fileList ){
			
			Iterator<Map<String, Object>> it	= fileList.iterator();
			while (it.hasNext()){
				Map<String, Object> jobFile = it.next();
				
				if ( (Long)jobFile.get("attemptdate") <= System.currentTimeMillis() ){
					it.remove();
					return jobFile;
				}
			}
			
		}
		return null;
	}
}