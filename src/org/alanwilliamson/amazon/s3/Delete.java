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
 *  $Id: Delete.java 2529 2015-03-01 23:39:29Z alan $
 */

package org.alanwilliamson.amazon.s3;

import java.util.ArrayList;
import java.util.List;

import org.alanwilliamson.amazon.AmazonBase;
import org.alanwilliamson.amazon.AmazonKey;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest.KeyVersion;
import com.amazonaws.services.s3.model.DeleteObjectsResult;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class Delete extends AmazonBase {
	private static final long serialVersionUID = 1L;
	
	public Delete(){  min = 3; max = 3; setNamedParams( new String[]{ "datasource", "bucket", "key" } ); }

	public String[] getParamInfo(){
		return new String[]{
			"Amazon datasource",
			"Amazon bucket",
			"a single key, or an array of keys to delete (if deleting multiple keys then only one request is made to Amazon)"
		};
	}
  
	public java.util.Map<String,String> getInfo(){
		return makeInfo(
				"amazon", 
				"Amazon S3: Deletes one or more remote keys, returns the number of keys deleted", 
				ReturnType.NUMERIC );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
		AmazonKey amazonKey	= getAmazonKey(_session, argStruct);
		AmazonS3 s3Client		= getAmazonS3(amazonKey);

		String bucket				= getNamedStringParam(argStruct, "bucket", null );
		if ( bucket == null )
			throwException(_session, "Please specify a bucket" );

 		cfData key				= getNamedParam(argStruct, "key");
 		
		try {
  		
			if ( key.getDataType() == cfData.CFARRAYDATA ){
				DeleteObjectsRequest multiObjectDeleteRequest = new DeleteObjectsRequest(bucket);

				List keysT = new ArrayList();
				
				cfArrayData arrData	= (cfArrayData)key;
				
				for ( int x=0; x < arrData.size(); x++ ){

					String k = arrData.getData(x+1).toString();
					if ( k.charAt( 0 ) == '/' )
						k	= k.substring(1);
					
					keysT.add(new KeyVersion( k ));	
				}
        
				multiObjectDeleteRequest.setKeys(keysT);
				DeleteObjectsResult delObjRes = s3Client.deleteObjects(multiObjectDeleteRequest);
				
				return new cfNumberData( delObjRes.getDeletedObjects().size() );
				
			}else{
				
				String k = key.toString();
				if ( k.charAt( 0 ) == '/' )
					k	= k.substring(1);
				
				s3Client.deleteObject(new DeleteObjectRequest(bucket, k));
				return new cfNumberData( 1 );
			}
			
		} catch (Exception e) {
			throwException(_session, "AmazonS3: " + e.getMessage() );
			return new cfNumberData(0);
		}
  }
}