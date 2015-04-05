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
 *  $Id: List.java 2476 2015-01-18 23:00:40Z alan $
 */
package org.alanwilliamson.amazon.transcoder.pipeline;

import java.util.Iterator;

import org.alanwilliamson.amazon.AmazonBase;
import org.alanwilliamson.amazon.AmazonKey;

import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoder;
import com.amazonaws.services.elastictranscoder.model.ListPipelinesRequest;
import com.amazonaws.services.elastictranscoder.model.ListPipelinesResult;
import com.amazonaws.services.elastictranscoder.model.Permission;
import com.amazonaws.services.elastictranscoder.model.Pipeline;
import com.amazonaws.services.elastictranscoder.model.PipelineOutputConfig;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class List extends AmazonBase {
	private static final long serialVersionUID = 813688638618582478L;

	public List(){  min = 1; max = 1; setNamedParams( new String[]{ "datasource" } ); }

	public String[] getParamInfo(){
		return new String[]{
			"Amazon datasource"
		};
	}
  
	public java.util.Map<String,String> getInfo(){
		return makeInfo(
				"amazon", 
				"Amazon Elastic Transcoder: Lists all the pipelines", 
				ReturnType.ARRAY );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
		AmazonKey amazonKey	= getAmazonKey(_session, argStruct);
		AmazonElasticTranscoder et = getAmazonElasticTranscoder(amazonKey);

		try{
			cfArrayData	pipelines	= cfArrayData.createArray(1);

			ListPipelinesRequest listObjectsRequest = new ListPipelinesRequest();

			ListPipelinesResult lpr;
			
			do {
				lpr = et.listPipelines(listObjectsRequest);
			
				for (Pipeline pipeline : lpr.getPipelines())
					pipelines.addElement( getPipeline(pipeline) );
			
				listObjectsRequest.setPageToken( lpr.getNextPageToken() );
			} while ( listObjectsRequest.getPageToken() != null );

			return pipelines;
			
		}catch(Exception e){
			throwException(_session, "AmazonElasticTranscoder: " + e.getMessage() );
		}
		
		return cfBooleanData.TRUE; 
	}


	protected cfStructData getPipeline(Pipeline pipeline) throws cfmRunTimeException {
		cfStructData	s = new cfStructData();

		s.setData( "id", new cfStringData( pipeline.getId() ) );
		s.setData( "name", new cfStringData( pipeline.getName() ) );
		s.setData( "inputbucket", new cfStringData( pipeline.getInputBucket() ) );
		s.setData( "status", new cfStringData( pipeline.getStatus() ) );
		s.setData( "role", new cfStringData( pipeline.getRole() ) );
		s.setData( "arn", new cfStringData( pipeline.getArn() ) );
		s.setData( "awskey", new cfStringData( pipeline.getAwsKmsKeyArn()) );
		s.setData( "outputbucket", new cfStringData( pipeline.getOutputBucket()) );
		
		
		s.setData("contentconfig", getConfig(pipeline.getContentConfig()) );
		s.setData("thumbnailconfig", getConfig(pipeline.getThumbnailConfig()) );

		return s;
	}
	
	
	private cfStructData getConfig(PipelineOutputConfig config) throws cfmRunTimeException {
		cfStructData	s = new cfStructData();
		
		s.setData("bucket", new cfStringData(config.getBucket()) );
		s.setData("storageclass", new cfStringData(config.getStorageClass()) );

		cfArrayData	permissionArr	= cfArrayData.createArray(1);
		Iterator<Permission> it = config.getPermissions().iterator();
		while ( it.hasNext() ){
			Permission p	= it.next();
			
			cfStructData	ps = new cfStructData();
			
			ps.setData("grantee", new cfStringData( p.getGrantee() ) );
			ps.setData("granteetype", new cfStringData( p.getGranteeType() ) );
			
			cfArrayData	accessArr	= cfArrayData.createArray(1);
			Iterator<String> ait = p.getAccess().iterator();
			while ( ait.hasNext() )
				accessArr.addElement( new cfStringData(ait.next()) );

			ps.setData("access", accessArr );
		}
		
		s.setData("permissions", permissionArr );
		
		return s;
	}
	
}
