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
 *  $Id: Write.java 2529 2015-03-01 23:39:29Z alan $
 */

package org.alanwilliamson.amazon.s3;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.alanwilliamson.amazon.AmazonBase;
import org.alanwilliamson.amazon.AmazonKey;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.SSECustomerKey;
import com.amazonaws.services.s3.model.StorageClass;
import com.naryx.tagfusion.cfm.application.cfApplicationData;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBinaryData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.string.serializejson;
import com.naryx.tagfusion.expression.function.string.serializejson.CaseType;
import com.naryx.tagfusion.expression.function.string.serializejson.DateType;


/**
 * http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/index.html
 *
 */
public class Write extends AmazonBase {
	private static final long serialVersionUID = 1L;
	
	public Write(){  min = 4; max = 17; setNamedParams( new String[]{ 
			"datasource", "bucket", "key", "file", 
			"storageclass", "metadata", "data", "mimetype", 
			"retry", "retrywaitseconds", "deletefile",
			"background", "callback", "callbackdata", 
			"acl", "aes256key", "customheaders"
			} ); }

	public String[] getParamInfo(){
		return new String[]{
			"Amazon datasource",
			"Amazon S3 bucket",
			"full S3 key",
			"path to local file to send.  If not supplied the 'data' attribute will be used as the data",
			"storage class to store this object; Standard or ReducedRedundancy",
			"structure of data that will be stored with the object.  Available via AmazonS3GetInfo() or any HTTP header call to the object",
			"variable with the object data.  Cannot be used with 'file'.  If not a string or a binary, it will be encoded into JSON and stored application/json",
			"mimetype of the data.  if not supplied will attempt to guessestimate the mimetype",
			"number of times to retry before giving up; defaults to 1",
			"number of seconds to wait before retrying; defaults to 1",
			"if file, then will delete the file after successfully uploading",
			"flag to determine if this upload goes to a background process, returning immediately; defaults to false. Only for use with file attribute",
			"if background=true the method, onAmazonS3Write(file,success,callbackdata,error), will be called on the CFC passed in",
			"a string that will be passed on through to the callback function; can be any string",
			"ACL to set: private | public-read | public-read-write | authenticated-read | bucket-owner-read | bucket-owner-full-control | log-delivery-write",
			"optional AES256 key for Amazon to encrypt the file at rest, using the specified key, in Base64.  If you write the file using encryption, then you have to supply the same key in the AmazonS3Read() function to read the file.  To create a AES key use the OpenBD function:  GenerateSecurityKey('aes',256)",
			"a map of custom headers to pass along side the request. This is not metadata.  For example Cache-Control"
		};
	}
  
	public java.util.Map<String,String> getInfo(){
		return makeInfo(
				"amazon", 
				"Amazon S3: Copies the local file up to Amazon S3", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
		AmazonKey amazonKey	= getAmazonKey(_session, argStruct);
		

		String localpath	= getNamedStringParam(argStruct, "file", null );
 		cfData data				= getNamedParam(argStruct, "data", null );
 	
 		if ( data != null && localpath != null )
 			throwException(_session, "You cannot use 'file' and 'data' at the same time");
  	
 		String bucket			= getNamedStringParam(argStruct, "bucket", null );
 		String key				= getNamedStringParam(argStruct, "key", null );
 		String mimetype		= getNamedStringParam(argStruct, "mimetype", null );
 		String acl				= getNamedStringParam(argStruct, "acl", null );
 		String aes256key	= getNamedStringParam(argStruct, "aes256key", null );
 		
		if ( key != null && key.charAt( 0 ) == '/' )
			key	= key.substring(1);


 		int	retry						= getNamedIntParam(argStruct, "retry", 1 );
 		int	retryseconds		= getNamedIntParam(argStruct, "retrywaitseconds", 1 );
 		boolean deletefile	= getNamedBooleanParam(argStruct, "deletefile", false);
 		boolean background	= getNamedBooleanParam(argStruct, "background", false);
 		
 		String callback = null, callbackdata = null, appname = null;
 		if ( background ){
 			cfApplicationData appData = _session.getApplicationData();
			if ( appData != null )
				appname	= appData.getAppName();

 			callback			= getNamedStringParam(argStruct, "callback", null );
 			callbackdata	= getNamedStringParam(argStruct, "callbackdata", null );
 		}
 		
 		
 		// Storage -------------------------------
 		StorageClass	storageClass	= amazonKey.getAmazonStorageClass( getNamedStringParam(argStruct, "storageclass", null ) );

 		
 		// Metadata --------------------------------
 		cfData	cfMetaData	= getNamedParam(argStruct, "metadata", null);
 		Map<String,String>	metadata = null;
 		if ( cfMetaData != null ){
 			if ( cfMetaData.getDataType() != cfData.CFSTRUCTDATA ){
 				throwException(_session, "the 'metadata' parameter must be of type struct" );
 			}else{
 				
 				metadata = new HashMap<String,String>();
 				Iterator<String>	it	= ((cfStructData)cfMetaData).keySet().iterator();
 				while ( it.hasNext() ){
 					String k = it.next();
 					metadata.put( k, ((cfStructData)cfMetaData).getData(k).getString() );
 				}
 			}
 		}
 		
 		
 		// CustomHeaders --------------------------------
 		cfMetaData	= getNamedParam(argStruct, "customheaders", null);
 		Map<String,String>	customheaders = null;
 		if ( cfMetaData != null ){
 			if ( cfMetaData.getDataType() != cfData.CFSTRUCTDATA ){
 				throwException(_session, "the 'customheaders' parameter must be of type struct" );
 			}else{
 				
 				customheaders = new HashMap<String,String>();
 				Iterator<String>	it	= ((cfStructData)cfMetaData).keySet().iterator();
 				while ( it.hasNext() ){
 					String k = it.next();
 					customheaders.put( k, ((cfStructData)cfMetaData).getData(k).getString() );
 				}
 			}
 		}
 		
 		
  	try {
  		if  ( localpath != null )
  			writeFile( amazonKey, bucket, key, metadata, storageClass, localpath, retry, retryseconds, deletefile, background, callback, callbackdata, appname, acl, aes256key, customheaders );
  		else
  			writeData( amazonKey, bucket, key, metadata, storageClass, mimetype, data, retry, retryseconds, acl, aes256key, customheaders );
  		
  		return cfBooleanData.TRUE;
		} catch (Exception e) {
			throwException(_session, "AmazonS3: " + e.getMessage() );
			return cfBooleanData.FALSE;
		}
  }

	
	private void writeData( AmazonKey amazonKey, String bucket, String key, Map<String,String>	metadata, StorageClass storage, String mimetype, cfData data, int retry, int retryseconds, String acl, String aes256key, Map<String,String>	customheaders ) throws Exception {
		if ( mimetype == null ){
			if ( data.getDataType() == cfData.CFBINARYDATA )
				mimetype	= "application/unknown";
			else if ( cfData.isSimpleValue(data) ) 
				mimetype	= "text/plain";
			else
				mimetype	= "application/json";
			
			// Check to see if the mime type is in the metadata
			if ( metadata != null && metadata.containsKey( "Content-Type" ) )
				mimetype	= metadata.get( "Content-Type" );
		}
		
		
		InputStream ios = null;
		long size = 0;
		if ( data.getDataType() == cfData.CFSTRINGDATA ){
			ios	= new java.io.ByteArrayInputStream( data.getString().getBytes() );
			size	= data.getString().length();
		}else if ( data.getDataType() == cfData.CFBINARYDATA ){
			ios	= new java.io.ByteArrayInputStream( ((cfBinaryData)data).getByteArray() );
			size	= ((cfBinaryData)data).getLength();
		}else{
			serializejson	json	= new serializejson();
			StringBuilder	out		= new StringBuilder();
			json.encodeJSON(out, data, false, CaseType.MAINTAIN, DateType.LONG );
			size			= out.length();
			mimetype	= "application/json";
			ios				= new java.io.ByteArrayInputStream( out.toString().getBytes() );
		}
		
		
		// Setup the object data
		ObjectMetadata	omd = new ObjectMetadata();
		if ( metadata != null )
			omd.setUserMetadata(metadata);
		
		omd.setContentType(mimetype);
		omd.setContentLength(size);
		
		AmazonS3 s3Client		= getAmazonS3(amazonKey);
		
		// Let us run around the number of attempts
		int attempts = 0;
		while ( attempts < retry ){
			try{
				
				PutObjectRequest	por	= new PutObjectRequest(bucket,key,ios,omd);
				por.setStorageClass(storage);
				
				if ( aes256key != null && !aes256key.isEmpty() )
					por.setSSECustomerKey( new SSECustomerKey(aes256key) );
				
				if ( acl != null && !acl.isEmpty() )
					por.setCannedAcl( amazonKey.getAmazonCannedAcl(acl) );
				
				if ( customheaders != null && !customheaders.isEmpty() ){
					Iterator<String> it	 = customheaders.keySet().iterator();
					while ( it.hasNext() ){
						String k = it.next();
						por.putCustomRequestHeader( k, customheaders.get(k) );
					}
				}
				
				s3Client.putObject(por);
				break;
				
			}catch(Exception e){
				cfEngine.log("Failed: AmazonS3Write(bucket=" + bucket + "; key=" + key + "; attempt=" + (attempts+1) + "; exception=" + e.getMessage() + ")");
				attempts++;

				if ( attempts == retry )
					throw e;
				else
					Thread.sleep( retryseconds*1000 );
			}
		}
	}
	
	
	private void writeFile( AmazonKey amazonKey, String bucket, String key, Map<String,String>	metadata, StorageClass storage, String localpath, int retry, int retryseconds, boolean deletefile, boolean background, String callback, String callbackdata, String appname, String acl, String aes256key,Map<String,String>	customheaders ) throws Exception {
		File localFile	= new File( localpath );
		if ( !localFile.isFile() )
			throw new Exception("The file specified does not exist: " + localpath );

		// Push this to the background loader to handle and return immediately
		if ( background ){
			BackgroundUploader.acceptFile(amazonKey, bucket, key, metadata, storage, localpath, retry, retryseconds, deletefile, callback, callbackdata, appname, acl, aes256key, customheaders );
			return;
		}
		
		
		// Setup the object data
		ObjectMetadata	omd = new ObjectMetadata();
		if ( metadata != null )
			omd.setUserMetadata(metadata);
		
		AmazonS3 s3Client		= getAmazonS3(amazonKey);
		
		// Let us run around the number of attempts
		int attempts = 0;
		while ( attempts < retry ){
			try{
				
				PutObjectRequest	por	= new PutObjectRequest(bucket,key,localFile);
				por.setMetadata(omd);
				por.setStorageClass(storage);
				
				if ( acl != null && !acl.isEmpty() )
					por.setCannedAcl( amazonKey.getAmazonCannedAcl(acl) );

				if ( aes256key != null && !aes256key.isEmpty() )
					por.setSSECustomerKey( new SSECustomerKey(aes256key) );

				if ( customheaders != null && !customheaders.isEmpty() ){
					Iterator<String> it	 = customheaders.keySet().iterator();
					while ( it.hasNext() ){
						String k = it.next();
						por.putCustomRequestHeader( k, customheaders.get(k) );
					}
				}
				
				s3Client.putObject(por);
				break;
				
			}catch(Exception e){
				cfEngine.log("Failed: AmazonS3Write(bucket=" + bucket +  "key=" + key + "; file=" + localFile + "; attempt=" + (attempts+1) + "; exception=" + e.getMessage() + ")");
				attempts++;

				if ( attempts == retry )
					throw e;
				else
					Thread.sleep( retryseconds*1000 );
			}
		}
		
		
		// delete the file now that it is a success
		if ( deletefile )
			localFile.delete();
	}
	
}