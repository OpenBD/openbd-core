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
 *  README.txt @ http://openbd.org/license/README.txt
 *  
 *  http://openbd.org/
 *  $Id: Cancel.java 2476 2015-01-18 23:00:40Z alan $
 */
package org.alanwilliamson.amazon.transcoder.job;

import org.alanwilliamson.amazon.AmazonBase;
import org.alanwilliamson.amazon.AmazonKey;

import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoder;
import com.amazonaws.services.elastictranscoder.model.CancelJobRequest;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class Cancel extends AmazonBase {
	private static final long serialVersionUID = 813688638618582478L;

	public Cancel(){  min = 2; max = 2; setNamedParams( new String[]{ "datasource", "jobid" } ); }

	public String[] getParamInfo(){
		return new String[]{
			"Amazon datasource",
			"Job ID"
		};
	}
  
	public java.util.Map<String,String> getInfo(){
		return makeInfo(
				"amazon", 
				"Amazon Elastic Transcoder: You can only cancel a job that has a status of Submitted",
				ReturnType.BOOLEAN );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
		AmazonKey amazonKey	= getAmazonKey(_session, argStruct);
		AmazonElasticTranscoder et = getAmazonElasticTranscoder(amazonKey);

		String jobid	= getNamedStringParam(argStruct, "jobid", null );
		if ( jobid == null )
			throwException(_session, "Please specify a jobid" );

		try{
			et.cancelJob( new CancelJobRequest().withId(jobid) );
		}catch(Exception e){
			throwException(_session, "AmazonElasticTranscoder: " + e.getMessage() );
		}

		return cfBooleanData.TRUE; 
	}

}
