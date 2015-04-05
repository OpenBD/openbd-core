/*
 *  Copyright (C) 2000 - 2013 TagServlet Ltd
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
 *  $Id: AmazonRegisterDatasource.java 2487 2015-01-22 22:40:49Z alan $
 *
 *  For full regions
 *  http://docs.amazonwebservices.com/general/latest/gr/index.html?rande.html#s3_region
 */


package org.alanwilliamson.amazon;

import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.expression.function.functionBase;

public class AmazonRegisterDatasource extends functionBase {
	private static final long serialVersionUID = 1L;

	public AmazonRegisterDatasource(){
		min = 3; max = 4;
		setNamedParams( new String[]{ "datasource", "awsaccess", "awskey", "region" } );
	}

	@Override
	public String[] getParamInfo(){
		return new String[]{
			"Name of the datasource",
			"Your Amazon Access ID",
			"Your Amazon Secret Key",
			"The region you will be accessing.  Can be one of the following: us-standard (default), us-east, us-west-1, us-west-2, eu-ireland, eu-frankfurt, ap-singapore, ap_sydney, ap_tokyo, sa-saopaulo, cn-beijing, ap-southeast-1, ap-southeast-2, ap-northeast-1, sa-east-1, eu.  Alternatively you put in the full domain if using an Amazon clone"
		};
	}

	@Override
	public java.util.Map getInfo(){
		return makeInfo(
				"amazon",
				"Amazon Datasource: Registers a datasource for use with the Amazon functions and tags.  If you are working with a specific region, then you have to specify which one you are going to be using",
				ReturnType.STRING );
	}

	@Override
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{

  	String dsName					= getNamedStringParam(argStruct, "datasource", "").trim();
  	if ( dsName.length() == 0 )
  		throwException(_session, "Invalid name for Amazon Datasource");

  	String awsAccessId		= getNamedStringParam(argStruct, "awsaccess", null );
  	String awsSecretKey		= getNamedStringParam(argStruct, "awskey", null );

  	if ( awsAccessId == null )
  		throwException(_session, "Please provide a value for the 'awsaccess' parameter");

  	if ( awsSecretKey == null )
  		throwException(_session, "Please provide a value for the 'awskey' parameter");

  	String region			= getNamedStringParam(argStruct, "region", "us-standard" ).toLowerCase();
  	AmazonKeyFactory.registerDS( dsName, new AmazonKey( awsAccessId, awsSecretKey, region ) );
  	return new cfStringData( dsName );
  }

}
