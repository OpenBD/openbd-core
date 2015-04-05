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
 *  $Id: Read.java 2529 2015-03-01 23:39:29Z alan $
 */

package org.alanwilliamson.amazon.s3;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.alanwilliamson.amazon.AmazonBase;
import org.alanwilliamson.amazon.AmazonKey;
import org.aw20.io.StreamUtil;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.SSECustomerKey;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBinaryData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class Read extends AmazonBase {
	private static final long serialVersionUID = 1L;
	
	public Read(){  min = 3; max = 8; setNamedParams( new String[]{ "datasource", "bucket", "key", "file", "overwrite", "aes256key", "retry", "retrywaitseconds" } ); }

	public String[] getParamInfo(){
		return new String[]{
			"Amazon datasource",
			"Amazon bucket",
			"Full key",
			"Path to local file where this remote object will be saved.  If not specified then the contents of the remote file is returned from this function",
			"flag to control if the local file should be overwritten before downloading; defaults to true",
			"optional AES256 key for Amazon to decrypt the file from rest, using the specified key, in Base64.  If you write the file using encryption, then you have to supply the same key in the AmazonS3Write() function to write the file.  To create a AES key use the OpenBD function:  GenerateSecurityKey('aes',256)",
			"number of times to retry before giving up; defaults to 1",
			"number of seconds to wait before retrying; defaults to 1"
		};
	}
  
	public java.util.Map<String,String> getInfo(){
		return makeInfo(
				"amazon", 
				"Amazon S3: Reads the remote file sitting on S3 copying to the local file system or reading the file into memory and returning its content", 
				ReturnType.STRING );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
		
		AmazonKey amazonKey	= getAmazonKey(_session, argStruct);
		AmazonS3 s3Client		= getAmazonS3(amazonKey);
		
 		String bucket			= getNamedStringParam(argStruct, "bucket", 	null );
 		String key				= getNamedStringParam(argStruct, "key", 		null );
 		String localpath	= getNamedStringParam(argStruct, "file", 		null );
 		String aes256key	= getNamedStringParam(argStruct, "aes256key", null );
 		int	retry					= getNamedIntParam(argStruct, "retry", 1 );
 		int	retryseconds	= getNamedIntParam(argStruct, "retrywaitseconds", 1 );
	
 		
		if ( bucket == null )
			throwException(_session, "Please specify a bucket" );

		if ( key == null )
			throwException(_session, "Please specify a key" );

		if ( key != null && key.charAt( 0 ) == '/' )
			key	= key.substring(1);
		
  	try {
  		
  		if ( localpath != null ){
  			return readToFile( s3Client, bucket, key, localpath, getNamedBooleanParam(argStruct, "overwrite", true ), aes256key, retry, retryseconds  );
  		}else{
  			return readToMemory( s3Client, bucket, key, aes256key, retry, retryseconds );
  		}
  		
		} catch (Exception e) {
			throwException(_session, "AmazonS3: " + e.getMessage() );
			return cfBooleanData.FALSE;
		}
  }
	
	
	private cfData	readToFile(AmazonS3 s3Client, String bucket, String key, String localpath, boolean overwrite, String aes256key, int retry, int retryseconds ) throws Exception {
		File localFile	= new File( localpath );
		if ( localFile.isFile() ){
			if ( !overwrite )
				throw new Exception("The file specified exists: " + localpath );
			else
				localFile.delete();
		}

		// Let us run around the number of attempts
		int attempts = 0;
		while ( attempts < retry ){
			try{
				
				GetObjectRequest gor = new GetObjectRequest(bucket, key);
				if ( aes256key != null && !aes256key.isEmpty() )
					gor.setSSECustomerKey( new SSECustomerKey(aes256key) );
				
				S3Object s3object = s3Client.getObject(gor);
				
				FileOutputStream	outStream = null;
				try{
					outStream	= new FileOutputStream( localFile );
					StreamUtil.copyTo(s3object.getObjectContent(), outStream, false );
				}finally{
					StreamUtil.closeStream(outStream);
				}
				
				return new cfStringData( localFile.toString() );
				
			}catch(Exception e){
				cfEngine.log("Failed: AmazonS3Read(bucket=" + bucket + "; key=" + key + "; attempt=" + (attempts+1) + "; exception=" + e.getMessage() + ")");
				attempts++;
		
				if ( attempts == retry )
					throw e;
				else
					Thread.sleep( retryseconds*1000 );
			}
		}
		
		return null; // should never 				
	}
	
	
	private cfData	readToMemory(AmazonS3 s3Client, String bucket, String key, String aes256key, int retry, int retryseconds) throws Exception {
		
		// Let us run around the number of attempts
		int attempts = 0;
		while ( attempts < retry ){
			try{
				
				GetObjectRequest gor = new GetObjectRequest(bucket, key);
				if ( aes256key != null && !aes256key.isEmpty() )
					gor.setSSECustomerKey( new SSECustomerKey(aes256key) );
		
				S3Object s3object = s3Client.getObject(gor);
				String contentType = s3object.getObjectMetadata().getContentType();
				
				ByteArrayOutputStream	baos	= new ByteArrayOutputStream( 32000 );
				StreamUtil.copyTo(s3object.getObjectContent(), baos, false );
				
				if ( contentType.indexOf("text") != -1 || contentType.indexOf("javascript") != -1 ){
					return new cfStringData( baos.toString() );
				}else{
					return new cfBinaryData( baos.toByteArray() );
				}
				
			}catch(Exception e){
				cfEngine.log("Failed: AmazonS3Read(bucket=" + bucket + "; key=" + key + "; attempt=" + (attempts+1) + "; exception=" + e.getMessage() + ")");
				attempts++;
		
				if ( attempts == retry )
					throw e;
				else
					Thread.sleep( retryseconds*1000 );
			}
		}
		
		return null; // should never 
	}
}