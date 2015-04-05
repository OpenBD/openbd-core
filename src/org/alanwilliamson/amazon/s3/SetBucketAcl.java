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
 *  $Id: SetBucketAcl.java 2466 2015-01-11 15:53:09Z alan $
 */

package org.alanwilliamson.amazon.s3;

import org.alanwilliamson.amazon.AmazonBase;
import org.alanwilliamson.amazon.AmazonKey;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class SetBucketAcl extends AmazonBase {
	private static final long serialVersionUID = 1L;
	
	public SetBucketAcl(){  min = 3; max = 3; setNamedParams( new String[]{ "datasource", "bucket", "acl" } ); }

	public String[] getParamInfo(){
		return new String[]{
			"Amazon datasource",
			"Amazon bucket",
			"ACL to set: private | public-read | public-read-write | authenticated-read | bucket-owner-read | bucket-owner-full-control | log-delivery-write"
		};
	}
  
	public java.util.Map<String,String> getInfo(){
		return makeInfo(
				"amazon", 
				"Amazon S3: Sets the ACL on the given bucket; all objects uploaded will be defaulted to this", 
				ReturnType.BOOLEAN );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
 		
		AmazonKey amazonKey	= getAmazonKey(_session, argStruct);
		AmazonS3 s3Client		= getAmazonS3(amazonKey);
 		String bucket				= getNamedStringParam(argStruct, "bucket", null );
 		
 		CannedAccessControlList	acl	= amazonKey.getAmazonCannedAcl( getNamedStringParam(argStruct, "acl", null ) );
  	
		try {
  		s3Client.setBucketAcl(bucket, acl);
		} catch (Exception e) {
			throwException(_session, "AmazonS3: " + e.getMessage() );
		}

		return cfBooleanData.TRUE;
  }
}