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
 *  $Id: GetUrl.java 2529 2015-03-01 23:39:29Z alan $
 */

package org.alanwilliamson.amazon.s3;

import java.util.Date;

import org.alanwilliamson.amazon.AmazonBase;
import org.alanwilliamson.amazon.AmazonKey;
import org.aw20.util.DateUtil;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class GetUrl extends AmazonBase {
	private static final long serialVersionUID = 1L;

	public GetUrl(){  min = 3; max = 4; setNamedParams( new String[]{ "datasource", "bucket", "key", "expiration" } ); }

	@Override
	public String[] getParamInfo(){
		return new String[]{
			"Amazon datasource",
			"Amazon bucket",
			"Full key",
			"Optional date to expire the url; can be a CFML date or the number of seconds"
		};
	}

	@Override
	public java.util.Map<String,String> getInfo(){
		return makeInfo(
				"amazon",
				"Amazon S3: Returns back a signed URL that gives people public access to a given file, with an optional expiration date",
				ReturnType.STRING );
	}

	@Override
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{

		AmazonKey amazonKey	= getAmazonKey(_session, argStruct);
		AmazonS3 s3Client		= getAmazonS3(amazonKey);

 		String bucket		= getNamedStringParam(argStruct, "bucket", null );
 		String key			= getNamedStringParam(argStruct, "key", null );

		if ( bucket == null )
			throwException(_session, "Please specify a bucket" );

		if ( key == null )
			throwException(_session, "Please specify a key" );
		
		if ( key.charAt( 0 ) == '/' )
			key	= key.substring(1);

  	cfData expired	= getNamedParam(argStruct,"expiration", null );
  	try {
  		Date expirationDate;
  		if ( expired != null ){
  			if ( expired.getDataType() == cfData.CFDATEDATA )
  				expirationDate = new Date( expired.getLong() );
  			else
  				expirationDate = new Date( System.currentTimeMillis() + (expired.getLong() * DateUtil.SECS_MS) );
  		}else{
  			expirationDate = new Date( System.currentTimeMillis() + DateUtil.DAY_MS*7 );
  		}

  		GeneratePresignedUrlRequest g = new GeneratePresignedUrlRequest(bucket, key);
  		g.setExpiration(expirationDate);

  		return new cfStringData( s3Client.generatePresignedUrl(g).toString() );
		} catch (Exception e) {
			throwException(_session, "AmazonS3: " + e.getMessage() );
			return cfBooleanData.FALSE;
		}
  }

}