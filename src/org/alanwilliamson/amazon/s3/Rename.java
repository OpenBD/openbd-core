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
 *  $Id: Rename.java 2529 2015-03-01 23:39:29Z alan $
 */

package org.alanwilliamson.amazon.s3;

import org.alanwilliamson.amazon.AmazonBase;
import org.alanwilliamson.amazon.AmazonKey;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.SSECustomerKey;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class Rename extends AmazonBase {
	private static final long serialVersionUID = 1L;
	
	public Rename(){  min = 4; max = 5; setNamedParams( new String[]{ "datasource", "bucket", "srckey", "destkey", "aes256key" } ); }

	public String[] getParamInfo(){
		return new String[]{
			"Amazon datasource",
			"Amazon bucket",
			"src key",
			"dest key",
			"optional - aes256key - see AmazonS3Write().  Required if the file you are attempting to rename was encryped",
		};
	}
  
	public java.util.Map<String,String> getInfo(){
		return makeInfo(
				"amazon", 
				"Amazon S3: Rename the remote file", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
		AmazonKey amazonKey	= getAmazonKey(_session, argStruct);
		AmazonS3 s3Client		= getAmazonS3(amazonKey);

 		String bucket			= getNamedStringParam(argStruct, "bucket", null );
 		String srckey			= getNamedStringParam(argStruct, "srckey", null );
 		String deskey			= getNamedStringParam(argStruct, "destkey", null );
 		String aes256key	= getNamedStringParam(argStruct, "aes256key", null );

		if ( srckey != null && srckey.charAt( 0 ) == '/' )
			srckey	= srckey.substring(1);

		if ( deskey != null && deskey.charAt( 0 ) == '/' )
			deskey	= deskey.substring(1);

 		
 		CopyObjectRequest cor = new CopyObjectRequest(bucket, srckey, bucket, deskey);
 		
 		if ( aes256key != null && !aes256key.isEmpty() ){
 			cor.setSourceSSECustomerKey( new SSECustomerKey(aes256key) );
 			cor.setDestinationSSECustomerKey( new SSECustomerKey(aes256key) );
 		}
 		

		try {
			s3Client.copyObject(cor);
			s3Client.deleteObject(new DeleteObjectRequest(bucket, srckey));
			return cfBooleanData.TRUE;
		} catch (Exception e) {
			throwException(_session, "AmazonS3: " + e.getMessage() );
			return cfBooleanData.FALSE;
		}
  }
}