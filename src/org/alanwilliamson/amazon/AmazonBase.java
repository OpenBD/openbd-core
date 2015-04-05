/* 
 *  Copyright (C) 2000 - 2015 aw2.0Ltd
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
 *  $Id: AmazonBase.java 2476 2015-01-18 23:00:40Z alan $
 */
package org.alanwilliamson.amazon;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoder;
import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoderClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;


/**
 * Base function for the Amazon functions
 * 
 * http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/index.html
 *
 */
public class AmazonBase extends functionBase  {
	private static final long serialVersionUID = 8638435137605963690L;

	
	/**
	 * Returns back the necessary AmazonS3 class for communicating to the S3
	 * 
	 * @param _session
	 * @param argStruct
	 * @return
	 * @throws cfmRunTimeException
	 */
	public AmazonS3 getAmazonS3( AmazonKey amazonKey ) throws cfmRunTimeException{
		BasicAWSCredentials awsCreds = new BasicAWSCredentials(amazonKey.getKey(), amazonKey.getSecret());
		AmazonS3 s3Client = new AmazonS3Client(awsCreds);
		s3Client.setRegion( amazonKey.getAmazonRegion().toAWSRegion() );
		return s3Client;
	}
	
	
	
	/**
	 * Returns back the necessary AmazonElasticTranscoder class for interacting with Elastic Transcoder
	 * @param amazonKey
	 * @return
	 * @throws cfmRunTimeException
	 */
	public AmazonElasticTranscoder getAmazonElasticTranscoder( AmazonKey amazonKey ) throws cfmRunTimeException{
		BasicAWSCredentials awsCreds = new BasicAWSCredentials(amazonKey.getKey(), amazonKey.getSecret());
		AmazonElasticTranscoder et = new AmazonElasticTranscoderClient(awsCreds);
		et.setRegion( amazonKey.getAmazonRegion().toAWSRegion() );
		return et;
	}
	
	
	
	public AmazonKey	getAmazonKey(cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
 		String dsName	= getDS(argStruct);
  	
		AmazonKey	amazonKey	= AmazonKeyFactory.getDS( dsName );
		if ( amazonKey == null )
			throwException(_session, "Amazon Datasource [" + dsName + "] has not been registered; use AmazonRegisterDataSource()" );

		amazonKey.setDataSource(dsName);
		
		return amazonKey;
	}
	
	
	/**
	 * Returns the Amazon Datasource for this parameters
	 * 
	 * @param argStruct
	 * @return
	 * @throws cfmRunTimeException
	 */
	public String getDS(cfArgStructData argStruct) throws cfmRunTimeException{
		return getNamedStringParam(argStruct, "datasource", "");
	}
	
}
