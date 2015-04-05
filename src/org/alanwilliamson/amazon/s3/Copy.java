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
 *  $Id: Copy.java 2529 2015-03-01 23:39:29Z alan $
 */

package org.alanwilliamson.amazon.s3;

import org.alanwilliamson.amazon.AmazonBase;
import org.alanwilliamson.amazon.AmazonKey;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.SSECustomerKey;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class Copy extends AmazonBase {
	private static final long serialVersionUID = 1L;
	
	public Copy(){  min = 5; max = 9; setNamedParams( new String[]{ "datasource", "srcbucket", "srckey", 
			"destbucket", "destkey", "srcaes256key", "destaes256key", "deststorageclass", "destacl" 
			} ); }

	public String[] getParamInfo(){
		return new String[]{
			"Amazon datasource",
			"source bucket",
			"source key",
			"destination bucket",
			"destination key",
			"source aes256key - see AmazonS3Write()",
			"optional - destination aes256key - see AmazonS3Write()",
			"optional - destination storage class - Standard or ReducedRedundancy",
			"optional - destination ACL to set: private | public-read | public-read-write | authenticated-read | bucket-owner-read | bucket-owner-full-control | log-delivery-write"
		};
	}
  
	public java.util.Map<String,String> getInfo(){
		return makeInfo(
				"amazon", 
				"Amazon S3: Copies the file from the given bucket/key to a new bucket/key", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
		
		AmazonKey amazonKey	= getAmazonKey(_session, argStruct);
		AmazonS3 s3Client		= getAmazonS3(amazonKey);

 		String srcbucket		= getNamedStringParam(argStruct, "srcbucket", null );
 		String srckey				= getNamedStringParam(argStruct, "srckey", null );
 		String srcaes256key	= getNamedStringParam(argStruct, "srcaes256key", null );
 		
 		String destbucket					= getNamedStringParam(argStruct, "destbucket", null );
 		String deskey							= getNamedStringParam(argStruct, "destkey", null );
 		String destaes256key			= getNamedStringParam(argStruct, "destaes256key", null );
 		String deststorageclass		= getNamedStringParam(argStruct, "deststorageclass", null );
 		String destacl						= getNamedStringParam(argStruct, "destacl", null );
  	
		if ( srckey != null && srckey.charAt( 0 ) == '/' )
			srckey	= srckey.substring(1);

		if ( deskey != null && deskey.charAt( 0 ) == '/' )
			deskey	= deskey.substring(1);
 		
 		CopyObjectRequest cor = new CopyObjectRequest(srcbucket, srckey, destbucket, deskey);
 		
 		if ( srcaes256key != null && !srcaes256key.isEmpty() )
 			cor.setSourceSSECustomerKey( new SSECustomerKey(srcaes256key) );

 		if ( destaes256key != null && !destaes256key.isEmpty() )
 			cor.setDestinationSSECustomerKey( new SSECustomerKey(destaes256key) );
 		
 		if ( deststorageclass != null && !deststorageclass.isEmpty() )
 			cor.setStorageClass( amazonKey.getAmazonStorageClass(deststorageclass) );
 		
 		if ( destacl != null && !destacl.isEmpty() )
 			cor.setCannedAccessControlList( amazonKey.getAmazonCannedAcl(destacl) );
 		
		try {
			s3Client.copyObject(cor);
			return cfBooleanData.TRUE;
		} catch (Exception e) {
			throwException(_session, "AmazonS3: " + e.getMessage() );
			return cfBooleanData.FALSE;
		}
  }
}