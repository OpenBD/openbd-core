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
 *  $Id: GetInfo.java 2529 2015-03-01 23:39:29Z alan $
 */

package org.alanwilliamson.amazon.s3;

import java.util.Iterator;
import java.util.Map;

import org.alanwilliamson.amazon.AmazonBase;
import org.alanwilliamson.amazon.AmazonKey;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.tagUtils;

public class GetInfo extends AmazonBase {
	private static final long serialVersionUID = 1L;

	public GetInfo(){  min = 3; max = 3; setNamedParams( new String[]{ "datasource", "bucket", "key" } ); }

	@Override
	public String[] getParamInfo(){
		return new String[]{
			"Amazon datasource",
			"Amazon bucket",
			"Full key"
		};
	}

	@Override
	public java.util.Map<String,String> getInfo(){
		return makeInfo(
				"amazon",
				"Amazon S3: Returns back a structure detailing all the headers from a given remote object",
				ReturnType.STRUCTURE );
	}

	@Override
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{

		AmazonKey amazonKey	= getAmazonKey(_session, argStruct);
		AmazonS3 s3Client		= getAmazonS3(amazonKey);

 		String bucket	= getNamedStringParam(argStruct, "bucket", null );
		if ( bucket == null )
			throwException(_session, "Please specify a bucket" );

 		String key		= getNamedStringParam(argStruct, "key", null );
		if ( key == null )
			throwException(_session, "Please specify a key" );

		if ( key.charAt( 0 ) == '/' )
			key	= key.substring(1);


  	try {
  		GetObjectMetadataRequest g	= new GetObjectMetadataRequest(bucket, key);
  		
  		ObjectMetadata metadata = s3Client.getObjectMetadata( g );

  		cfStructData	s = new cfStructData();

  		s.setData("bucket", new cfStringData(bucket) );
  		s.setData("key", 		new cfStringData(key) );
  		s.setData("host", 	new cfStringData( amazonKey.getAmazonRegion().toAWSRegion().getDomain() ) );

  		Map m = metadata.getRawMetadata();
  		Iterator<String> it = m.keySet().iterator();
  		while ( it.hasNext() ){
  			String k = it.next();
  			s.setData(k, tagUtils.convertToCfData(m.get(k)) );
  		}

  		return s;
		} catch (Exception e) {
			throwException(_session, "AmazonS3: " + e.getMessage() );
			return cfBooleanData.FALSE;
		}
  }

}