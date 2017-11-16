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
 *  $Id$
 */
package org.alanwilliamson.amazon.lambda;

import java.nio.ByteBuffer;
import java.util.Map;

import org.alanwilliamson.amazon.AmazonBase;
import org.alanwilliamson.amazon.AmazonKey;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.model.LogType;
import com.amazonaws.util.json.Jackson;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.tagUtils;

/**
 * Execute of a Lambda Function
 * 
 * @author Amanda
 *
 */
public class LambdaExecute extends AmazonBase {

	private static final long serialVersionUID = 1L;


	public LambdaExecute() {
		min = 3;
		max = 4;
		setNamedParams( new String[] { "datasource", "function", "payload", "qualifier" } );
	}


	@Override
	public String[] getParamInfo() {
		return new String[] {
				"Lambda Session",
				"Lambda Function Name",
				"JSON Payload for the Lambda Function",
				"Lambda Function Alias"
		};
	}


	@Override
	public java.util.Map<String, String> getInfo() {
		return makeInfo(
				"amazon",
				"Amazon Lambda: executes a lambda function",
				ReturnType.STRUCTURE );
	}


	/**
	 * Executes a lambda function and returns the result of the execution.
	 */
	@Override
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {

		AmazonKey amazonKey = getAmazonKey( _session, argStruct );

		// Arguments to extract
		String payload = getNamedStringParam( argStruct, "payload", null );
		String functionName = getNamedStringParam( argStruct, "function", null );
		String qualifier = getNamedStringParam( argStruct, "qualifier", null );

		try {

			// Construct the Lambda Client
			InvokeRequest invokeRequest = new InvokeRequest();
			invokeRequest.setInvocationType( InvocationType.RequestResponse );
			invokeRequest.setLogType( LogType.Tail );
			invokeRequest.setFunctionName( functionName );
			invokeRequest.setPayload( payload );
			if ( qualifier != null ) {
				invokeRequest.setQualifier( qualifier );
			}

			// Lambda client must be created with credentials
			BasicAWSCredentials awsCreds = new BasicAWSCredentials( amazonKey.getKey(), amazonKey.getSecret() );
			AWSLambda awsLambda = AWSLambdaClientBuilder.standard()
					.withRegion( amazonKey.getAmazonRegion().toAWSRegion().getName() )
					.withCredentials( new AWSStaticCredentialsProvider( awsCreds ) ).build();

			// Execute and process the results
			InvokeResult result = awsLambda.invoke( invokeRequest );

			// Convert the returned result
			ByteBuffer resultPayload = result.getPayload();
			String resultJson = new String( resultPayload.array(), "UTF-8" );
			Map<String, Object> resultMap = Jackson.fromJsonString( resultJson, Map.class );

			return tagUtils.convertToCfData( resultMap );

		} catch ( Exception e ) {
			throwException( _session, "AmazonLambdaExecute: " + e.getMessage() );
			return cfBooleanData.FALSE;
		}

	}

}
