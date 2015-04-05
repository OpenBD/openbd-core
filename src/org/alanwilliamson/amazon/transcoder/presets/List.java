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
package org.alanwilliamson.amazon.transcoder.presets;

import org.alanwilliamson.amazon.AmazonBase;
import org.alanwilliamson.amazon.AmazonKey;

import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoder;
import com.amazonaws.services.elastictranscoder.model.AudioParameters;
import com.amazonaws.services.elastictranscoder.model.ListPresetsRequest;
import com.amazonaws.services.elastictranscoder.model.ListPresetsResult;
import com.amazonaws.services.elastictranscoder.model.Preset;
import com.amazonaws.services.elastictranscoder.model.VideoParameters;
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
				"Amazon Elastic Transcoder: Lists all the presets", 
				ReturnType.ARRAY );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
		AmazonKey amazonKey	= getAmazonKey(_session, argStruct);
		AmazonElasticTranscoder et = getAmazonElasticTranscoder(amazonKey);

		try{
			cfArrayData	pipelines	= cfArrayData.createArray(1);

			ListPresetsRequest listObjectsRequest = new ListPresetsRequest();

			ListPresetsResult lpr;
			
			do {
				lpr = et.listPresets(listObjectsRequest);
			
				for (Preset preset: lpr.getPresets() )
					pipelines.addElement( getPreset(preset) );
			
				listObjectsRequest.setPageToken( lpr.getNextPageToken() );
			} while ( listObjectsRequest.getPageToken() != null );

			return pipelines;
			
		}catch(Exception e){
			throwException(_session, "AmazonElasticTranscoder: " + e.getMessage() );
		}
		
		return cfBooleanData.TRUE; 
	}


	protected cfStructData getPreset(Preset preset) throws cfmRunTimeException {
		cfStructData	s = new cfStructData();

		s.setData( "id", new cfStringData( preset.getId() ) );
		s.setData( "name", new cfStringData( preset.getName() ) );
		s.setData( "container", new cfStringData( preset.getContainer() ) );
		s.setData( "description", new cfStringData( preset.getDescription() ) );
		s.setData( "type", new cfStringData( preset.getType() ) );
		s.setData( "arn", new cfStringData( preset.getArn() ) );
		
		
		AudioParameters	ap	= preset.getAudio();
		if ( ap != null ){
			cfStructData	a = new cfStructData();
			a.setData( "bitrate", new cfStringData( ap.getBitRate() ) );
			a.setData( "channels", new cfStringData( ap.getChannels() ) );
			a.setData( "codec", new cfStringData( ap.getCodec() ) );
			a.setData( "samplerate", new cfStringData( ap.getSampleRate() ) );
			s.setData( "audio", a );
		}
		
		VideoParameters	vp	= preset.getVideo();
		if ( vp != null ){
			cfStructData	a = new cfStructData();
			a.setData( "aspectratio", new cfStringData( vp.getAspectRatio() ) );
			a.setData( "bitrate", new cfStringData( vp.getBitRate() ) );
			a.setData( "codec", new cfStringData( vp.getCodec() ) );
			a.setData( "displayaspectratio", new cfStringData( vp.getDisplayAspectRatio() ) );
			a.setData( "fixedgop", new cfStringData( vp.getFixedGOP() ) );
			a.setData( "framerate", new cfStringData( vp.getFrameRate() ) );
			a.setData( "keyframesmaxdist", new cfStringData( vp.getKeyframesMaxDist() ) );
			a.setData( "maxframesrate", new cfStringData( vp.getMaxFrameRate() ) );
			a.setData( "maxheight", new cfStringData( vp.getMaxHeight() ) );
			a.setData( "maxwidth", new cfStringData( vp.getMaxWidth() ) );
			a.setData( "paddingpolicy", new cfStringData( vp.getPaddingPolicy() ) );
			a.setData( "resolution", new cfStringData( vp.getResolution() ) );
			a.setData( "sizingpolicy", new cfStringData( vp.getSizingPolicy() ) );
			s.setData( "video", a );
		}
		
		return s;
	}
	
}
