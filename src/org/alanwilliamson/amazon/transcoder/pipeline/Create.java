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
 *  $Id: Create.java 2476 2015-01-18 23:00:40Z alan $
 */
package org.alanwilliamson.amazon.transcoder.pipeline;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.alanwilliamson.amazon.AmazonBase;
import org.alanwilliamson.amazon.AmazonKey;

import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoder;
import com.amazonaws.services.elastictranscoder.model.CreatePipelineRequest;
import com.amazonaws.services.elastictranscoder.model.CreatePipelineResult;
import com.amazonaws.services.elastictranscoder.model.Permission;
import com.amazonaws.services.elastictranscoder.model.PipelineOutputConfig;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

public class Create extends AmazonBase {
	private static final long serialVersionUID = 813688638618582478L;

	public Create(){  min = 4; max = 8; setNamedParams( new String[]{ "datasource", 
		"name", "inputbucket", "role", "awskey", "outputbucket",
		"contentconfig", "thumbnailconfig"
		} ); }

	public String[] getParamInfo(){
		return new String[]{
			"Amazon datasource",
			
			"name of the pipeline",
			"Amazon S3 bucket in which you saved the media files that you want to transcode",
			"The IAM Amazon Resource Name (ARN) for the role that you want Elastic Transcoder to use to create the pipeline",
			
			"The AWS Key Management Service (AWS KMS) key that you want to use with this pipeline",
			"The Amazon S3 bucket in which you want Elastic Transcoder to save the transcoded files. if specified, contentconfig is ignored",
			
			"Structure for files: { 'bucket' : 'The Amazon S3 bucket in which you want Elastic Transcoder to save the transcoded files', 'storageclass' : 'S3 storage class, Standard or ReducedRedundancy, that you want Elastic Transcoder to assign to the video files and playlists that it stores in your Amazon S3 bucket', 'permissions' : [{'granteetype':'Canonical|Email|Group','grantee':'AWS user ID or CloudFront origin access identity|registered email address for AWS account|AllUsers|AuthenticatedUsers|LogDelivery','access':[Read|ReadAcp|WriteAcp|FullControl]}] }",
			"Structure for thumbnails: { 'bucket' : 'The Amazon S3 bucket in which you want Elastic Transcoder to save the transcoded files', 'storageclass' : 'S3 storage class, Standard or ReducedRedundancy, that you want Elastic Transcoder to assign to the video files and playlists that it stores in your Amazon S3 bucket', 'permissions' : [{'granteetype':'Canonical|Email|Group','grantee':'AWS user ID or CloudFront origin access identity|registered email address for AWS account|AllUsers|AuthenticatedUsers|LogDelivery','access':[Read|ReadAcp|WriteAcp|FullControl]}] }"
		};
	}
  
	public java.util.Map<String,String> getInfo(){
		return makeInfo(
				"amazon", 
				"Amazon Elastic Transcoder: Creates a pipeline.  Returns the Pipeline ID.  See AWS http://docs.aws.amazon.com/elastictranscoder/latest/developerguide/create-pipeline.html", 
				ReturnType.STRING );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
		AmazonKey amazonKey	= getAmazonKey(_session, argStruct);
		AmazonElasticTranscoder et = getAmazonElasticTranscoder(amazonKey);

		CreatePipelineRequest cpr	= new CreatePipelineRequest();

		cpr.setName( getNamedStringParam(argStruct, "name", null) );
		if ( cpr.getName() == null || cpr.getName().isEmpty() || cpr.getName().length() > 40 )
			throwException(_session, "please provide a valid name (40chars or less)");
		
		cpr.setInputBucket( getNamedStringParam(argStruct, "inputbucket", null) );
		if ( cpr.getInputBucket() == null || cpr.getInputBucket().isEmpty() )
			throwException(_session, "please provide a valid inputbucket");
		
		cpr.setRole( getNamedStringParam(argStruct, "role", null) );
		if ( cpr.getRole() == null || cpr.getRole().isEmpty() )
			throwException(_session, "please provide a valid role");
		
		cpr.setAwsKmsKeyArn( getNamedStringParam(argStruct, "awskey", null) );
		if ( cpr.getAwsKmsKeyArn() == null || cpr.getAwsKmsKeyArn().isEmpty() )
			throwException(_session, "please provide a valid awskey");
		
		
		if ( getNamedStringParam(argStruct, "outputbucket", null) != null ){
			
			cpr.setOutputBucket( getNamedStringParam(argStruct, "outputbucket", null) );
			if ( cpr.getOutputBucket().isEmpty() )
				throwException(_session, "please provide a 'contentconfig' or a 'outputbucket'");

		}else{

			// Handle the ContentConfig
			cfStructData	cc	= getNamedStructParam( _session, argStruct, "contentconfig", null );
			if ( cc == null )
				throwException(_session, "please provide a 'contentconfig' or a 'outputbucket'");

			PipelineOutputConfig contentConfig	= new PipelineOutputConfig();
			
			contentConfig.setBucket( cc.getData("bucket").getString() );
			if ( contentConfig.getBucket() == null || contentConfig.getBucket().isEmpty() )
				throwException(_session, "please provide a 'contentconfig.bucket'");
			
			contentConfig.setStorageClass( cc.getData("storageclass").getString() );
			if ( contentConfig.getStorageClass() == null || contentConfig.getStorageClass().isEmpty() )
				throwException(_session, "please provide a 'contentconfig.storageclass'");
			
			Collection<Permission> permissions = getPermissions(_session, cc );
			if ( !permissions.isEmpty() )
				contentConfig.setPermissions( permissions );
			
			cpr.setContentConfig(contentConfig);

			
			// Handle the thumbnailconfig
			cc	= getNamedStructParam( _session, argStruct, "thumbnailconfig", null );
			if ( cc == null )
				throwException(_session, "please provide a 'thumbnailconfig' or a 'outputbucket'");

			contentConfig	= new PipelineOutputConfig();

			contentConfig.setBucket( cc.getData("bucket").getString() );
			if ( contentConfig.getBucket() == null || contentConfig.getBucket().isEmpty() )
				throwException(_session, "please provide a 'thumbnailconfig.bucket'");
			
			contentConfig.setStorageClass( cc.getData("storageclass").getString() );
			if ( contentConfig.getStorageClass() == null || contentConfig.getStorageClass().isEmpty() )
				throwException(_session, "please provide a 'thumbnailconfig.storageclass'");
			
			permissions = getPermissions(_session, cc );
			if ( !permissions.isEmpty() )
				contentConfig.setPermissions( permissions );

			cpr.setThumbnailConfig(contentConfig);
		}
		
		
		// Now after collection all that; create the actual pipeline
		try{
			CreatePipelineResult cpres = et.createPipeline(cpr);
			return new cfStringData( cpres.getPipeline().getId() ); 
		}catch(Exception e){
			throwException(_session, "AmazonElasticTranscoder: " + e.getMessage() );
			return cfBooleanData.TRUE;
		}
	}

	
	private Collection<Permission> getPermissions(cfSession _session, cfStructData cc) throws cfmRunTimeException {
		List<Permission>	permissions	= new LinkedList<Permission>();
		
		if ( !cc.containsKey("permissions") )
			return permissions;

		cfArrayData	arr	= (cfArrayData)cc.getData("permissions");
		
		for ( int x=0; x < arr.size(); x++ ){
			cfStructData	cs = (cfStructData)arr.getData(x+1);
			
			Permission p	= new Permission();
			
			p.setGranteeType( cs.getData("granteetype").getString() );
			p.setGrantee( cs.getData("grantee").getString() );

			cfArrayData	access	= (cfArrayData)cs.getData("access");
			List<String>	accessList	= new LinkedList<String>();
			
			for ( int a=0; a < access.size(); a++ ){
				accessList.add( access.getData(a+1).getString() );
			}
			p.setAccess(accessList);

			permissions.add(p);
		}
		
		return permissions;
	}
}