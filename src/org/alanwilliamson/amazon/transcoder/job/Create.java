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
package org.alanwilliamson.amazon.transcoder.job;

import java.util.LinkedList;
import java.util.List;

import org.alanwilliamson.amazon.AmazonBase;
import org.alanwilliamson.amazon.AmazonKey;

import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoder;
import com.amazonaws.services.elastictranscoder.model.Artwork;
import com.amazonaws.services.elastictranscoder.model.CaptionFormat;
import com.amazonaws.services.elastictranscoder.model.Captions;
import com.amazonaws.services.elastictranscoder.model.Clip;
import com.amazonaws.services.elastictranscoder.model.CreateJobOutput;
import com.amazonaws.services.elastictranscoder.model.CreateJobRequest;
import com.amazonaws.services.elastictranscoder.model.CreateJobResult;
import com.amazonaws.services.elastictranscoder.model.Encryption;
import com.amazonaws.services.elastictranscoder.model.JobAlbumArt;
import com.amazonaws.services.elastictranscoder.model.JobInput;
import com.amazonaws.services.elastictranscoder.model.JobWatermark;
import com.amazonaws.services.elastictranscoder.model.TimeSpan;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.dataNotSupportedException;

public class Create extends AmazonBase {
	private static final long serialVersionUID = 813688638618582478L;

	public Create(){  min = 4; max = 5; setNamedParams( new String[]{ "datasource", 
		"pipelineid", "outputkeyprefix",
		"input", "outputs"
		} ); }

	public String[] getParamInfo(){
		return new String[]{
			"Amazon datasource",
			
			"ID of the pipeline",
			"The value, if any, that you want Elastic Transcoder to prepend to the names of all files that this job creates, including output files, thumbnails, and playlists.",
			
			"Structure for the input: {'aspectratio':'aspect ratio of the input file', 'container':'container type for the input file','framerate':'frame rate of the input file','interlaced':'Whether the input file is interlaced','key':'name of the file to encode','resolution':'This value must be auto, which causes Elastic Transcoder to automatically detect the resolution of the input file','encryption':{'key':'The data encryption key that you want Elastic Transcoder to use to encrypt your output file, or that was used to encrypt your input file.','keymd5':'MD5 digest of the key that you used to encrypt your input file, or that you want Elastic Transcoder to use to encrypt your output file.','mode':'specific server-side encryption mode that you want Elastic Transcoder to use when decrypting your input files or encrypting your output files.','initializationvector':'series of random bits created by a random bit generator, unique for every encryption operation, that you used to encrypt your input files or that you want Elastic Transcoder to use to encrypt your output files.'}}",
			"Array of structures for the output: [{'presetid':'The Id of the preset to use for this job','key':'The name to assign to the transcoded file','rotate':'The number of degrees clockwise by which you want Elastic Transcoder to rotate the output relative to the input','segmentduration':'(Outputs in Fragmented MP4 or MPEG-TS format only.If you specify a preset in PresetId for which the value of Container is fmp4 (Fragmented MP4) or ts (MPEG-TS), SegmentDuration is the target maximum duration of each segment in seconds.','encryption':{'key':'The data encryption key that you want Elastic Transcoder to use to encrypt your output file, or that was used to encrypt your input file.','keymd5':'MD5 digest of the key that you used to encrypt your input file, or that you want Elastic Transcoder to use to encrypt your output file.','mode':'specific server-side encryption mode that you want Elastic Transcoder to use when decrypting your input files or encrypting your output files.','initializationvector':'series of random bits created by a random bit generator, unique for every encryption operation, that you used to encrypt your input files or that you want Elastic Transcoder to use to encrypt your output files.'},'thumbnailencryption':{'key':'The data encryption key that you want Elastic Transcoder to use to encrypt your output file, or that was used to encrypt your input file.','keymd5':'MD5 digest of the key that you used to encrypt your input file, or that you want Elastic Transcoder to use to encrypt your output file.','mode':'specific server-side encryption mode that you want Elastic Transcoder to use when decrypting your input files or encrypting your output files.','initializationvector':'series of random bits created by a random bit generator, unique for every encryption operation, that you used to encrypt your input files or that you want Elastic Transcoder to use to encrypt your output files.'},'thumbnailpattern':'Whether you want Elastic Transcoder to create thumbnails for your videos and, if so, how you want Elastic Transcoder to name the files','watermarks':'[{encyption:{see above},'inputkey':'The name of the .png or .jpg file that you want to use for the watermark.','presetwatermarkid':'The ID of the watermark settings that Elastic Transcoder uses to add watermarks to the video during transcoding.']','composition':[{'starttime:'starting place of the clip, in HH:mm:ss.SSS or sssss.SSS',duration:'duration of the clip, in HH:mm:ss.SSS or sssss.SSS'}],'captions':{'mergepolicy':'policy that determines how Elastic Transcoder handles the existence of multiple captions',[{'encryption':'see above','format':'The format you specify determines whether Elastic Transcoder generates an embedded or sidecar caption for this output.','pattern':'The prefix for caption filenames, in the form description-{language}, where: description is a description of the video. {language} is a literal value that Elastic Transcoder replaces with the two- or three-letter code for the language of the caption in the output file name'}]},'albumart':{'mergepolicy':'A policy that determines how Elastic Transcoder will handle the existence of multiple album artwork files','artwork':[{'encryption':'see above','albumartformat':'jpg|png','inputkey':'The name of the file to be used as album art.','maxheight:'maximum height of the output album art in pixels','maxwidth':'maximum width of the output album art in pixels','paddingpolicy':'Pad|NoPad','sizingpolicy':'Fit|Fill|Stretch|Keep|ShrinkToFit|ShrinkToFill' }]}}]"
			
		};
	}
  
	public java.util.Map<String,String> getInfo(){
		return makeInfo(
			"amazon", 
			"Amazon Elastic Transcoder: Creates a job.  Returns the Job ID.  See AWS http://docs.aws.amazon.com/elastictranscoder/latest/developerguide/create-job.html", 
			ReturnType.STRING );
	}
	
	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException{
		AmazonKey amazonKey	= getAmazonKey(_session, argStruct);
		AmazonElasticTranscoder et = getAmazonElasticTranscoder(amazonKey);

		CreateJobRequest cjr	= new CreateJobRequest();

		cjr.setPipelineId( getNamedStringParam(argStruct, "pipelineid", null) );
		if ( cjr.getPipelineId() == null || cjr.getPipelineId().isEmpty() )
			throwException(_session, "please provide a valid pipelineid");

		cjr.setOutputKeyPrefix( getNamedStringParam(argStruct, "outputkeyprefix", null) );
		if ( cjr.getOutputKeyPrefix() != null && cjr.getOutputKeyPrefix().isEmpty() )
			throwException(_session, "please provide a valid outputkeyprefix");
		
		
		// Handle the input
		cfStructData	input	= getNamedStructParam( _session, argStruct, "input", null );
		if ( input == null )
			throwException(_session, "please provide a 'input'");
		
		JobInput jobinput	= new JobInput();
		
		if ( input.containsKey("aspectratio") )
			jobinput.setAspectRatio( input.getData("aspectratio").getString() );
		
		if ( input.containsKey("container") )
			jobinput.setContainer( input.getData("container").getString() );
		
		if ( input.containsKey("framerate") )
			jobinput.setFrameRate( input.getData("framerate").getString() );
		
		if ( input.containsKey("interlaced") )
			jobinput.setInterlaced( input.getData("interlaced").getString() );
		
		if ( input.containsKey("key") )
			jobinput.setKey( input.getData("key").getString() );
		
		if ( input.containsKey("resolution") )
			jobinput.setResolution( input.getData("resolution").getString() );
		
		if ( input.containsKey("encryption") )
			jobinput.setEncryption( getEncryption( (cfStructData)input.getData("encryption") ) );
			
		cjr.setInput(jobinput);
		
		
		// Set the output
		cfArrayData	outputArr	= getNamedArrayParam( _session, argStruct, "outputs", null );
		if ( outputArr == null )
			throwException(_session, "please provide 'outputs'");
		
		List<CreateJobOutput>	outputs	= new LinkedList();
		for ( int x=0; x < outputArr.size(); x++ )
			outputs.add( getCreateJobOutput( (cfStructData)outputArr.getData(x+1) ) );
		
		cjr.setOutputs(outputs);
		
		
		// Now after collection all that; create the actual pipeline
		try{
			CreateJobResult cpres = et.createJob(cjr);
			return new cfStringData( cpres.getJob().getId() ); 
		}catch(Exception e){
			throwException(_session, "AmazonElasticTranscoder: " + e.getMessage() );
			return cfBooleanData.TRUE;
		}
	}

	private CreateJobOutput getCreateJobOutput(cfStructData d) throws cfmRunTimeException {
		CreateJobOutput	cjo	= new CreateJobOutput();
		
		if ( d.containsKey("presetid") )
			cjo.setPresetId( d.getData("presetid").getString() );
		
		if ( d.containsKey("key") )
			cjo.setKey( d.getData("key").getString() );

		if ( d.containsKey("rotate") )
			cjo.setRotate( d.getData("rotate").getString() );

		if ( d.containsKey("segmentduration") )
			cjo.setSegmentDuration( d.getData("segmentduration").getString() );
		
		if ( d.containsKey("encryption") )
			cjo.setEncryption( getEncryption( (cfStructData)d.getData("encryption") ) );
		
		if ( d.containsKey("thumbnailencryption") )
			cjo.setThumbnailEncryption( getEncryption( (cfStructData)d.getData("thumbnailencryption") ) );

		if ( d.containsKey("thumbnailpattern") )
			cjo.setThumbnailPattern( d.getData("thumbnailpattern").getString() );
		
		
		// Handle the watermarks
		if ( d.containsKey("watermarks") && d.getData("watermarks") instanceof cfArrayData ){
			List<JobWatermark>	watermarks	= new LinkedList();
			cfArrayData	arr	= (cfArrayData)d.getData("watermarks");
			
			for ( int x=0; x < arr.size(); x++ ){
				cfStructData	s = (cfStructData)arr.getData(x+1);
				JobWatermark	w = new JobWatermark();
				
				if ( s.containsKey("encryption") )
					w.setEncryption( getEncryption( (cfStructData)s.getData("encryption") ) );
				
				if ( s.containsKey("inputkey") )
					w.setInputKey( s.getData("inputkey").getString() );
				
				if ( s.containsKey("presetwatermarkid") )
					w.setPresetWatermarkId( s.getData("presetwatermarkid").getString() );

				watermarks.add(w);
			}

			cjo.setWatermarks(watermarks);
		}
		
		// Set the composition
		if ( d.containsKey("composition") && d.getData("composition") instanceof cfArrayData ){
			List<Clip>	clips = new LinkedList();
			cfArrayData	arr	= (cfArrayData)d.getData("composition");
			
			for ( int x=0; x < arr.size(); x++ ){
				cfStructData	s = (cfStructData)arr.getData(x+1);
				
				Clip clip	= new Clip();
				TimeSpan	ts = new TimeSpan();
				clip.setTimeSpan(ts);
				
				if ( s.containsKey("starttime") )
					ts.setStartTime( s.getData("starttime").getString() );
				if ( s.containsKey("duration") )
					ts.setDuration( s.getData("duration").getString() );
				
				clips.add(clip);
			}

			cjo.setComposition(clips);
		}
		
		// handle the captions
		if ( d.containsKey("captions") && d.getData("captions") instanceof cfStructData ){
			Captions c	= new Captions();
			cfStructData	s = (cfStructData)d.getData("captions");
			
			if ( s.containsKey("mergepolicy") )
				c.setMergePolicy( s.getData("mergepolicy").getString() );
			
			// CaptionFormat
			if ( s.containsKey("captionformat") && s.getData("captionformat") instanceof cfArrayData ){
				List<CaptionFormat>	captionFormatList	= new LinkedList();
				cfArrayData arr	= (cfArrayData)s.getData("captionformat");
				
				for ( int x=0; x < arr.size(); x++ ){
					cfStructData	ss = (cfStructData)arr.getData(x+1);
					CaptionFormat cf	= new CaptionFormat();
					
					if ( ss.containsKey("encryption") )
						cf.setEncryption( getEncryption((cfStructData)ss.get("encryption")));
					
					if ( ss.containsKey("format"))
						cf.setFormat(ss.getData("format").getString());
					
					if ( ss.containsKey("pattern"))
						cf.setPattern(ss.getData("pattern").getString());
					
					captionFormatList.add(cf);
				}
				
				c.setCaptionFormats(captionFormatList);
			}
			
			
			cjo.setCaptions(c);
		}
		
		// handle the album art
		if ( d.containsKey("albumart") && d.getData("albumart") instanceof cfStructData ){
			JobAlbumArt	ja = new JobAlbumArt();
			cfStructData	s = (cfStructData)d.getData("albumart");
	
			if ( s.containsKey("mergepolicy") )
				ja.setMergePolicy( s.getData("mergepolicy").getString() );
			
			// artwork
			if ( s.containsKey("artwork") && s.getData("artwork") instanceof cfArrayData ){
				List<Artwork>	artworkList	= new LinkedList();
				cfArrayData arr	= (cfArrayData)s.getData("artwork");
				
				for ( int x=0; x < arr.size(); x++ ){
					cfStructData	ss = (cfStructData)arr.getData(x+1);
					Artwork	a = new Artwork();
					
					if ( ss.containsKey("encryption") )
						a.setEncryption( getEncryption((cfStructData)ss.get("encryption")));
					
					if ( ss.containsKey("albumartformat"))
						a.setAlbumArtFormat(ss.getData("albumartformat").getString());
					
					if ( ss.containsKey("inputkey"))
						a.setInputKey(ss.getData("inputkey").getString());
					
					if ( ss.containsKey("maxheight"))
						a.setMaxHeight(ss.getData("maxheight").getString());
					
					if ( ss.containsKey("maxwidth"))
						a.setMaxWidth(ss.getData("maxwidth").getString());
					
					if ( ss.containsKey("paddingpolicy"))
						a.setPaddingPolicy(ss.getData("paddingpolicy").getString());
					
					if ( ss.containsKey("sizingpolicy"))
						a.setSizingPolicy(ss.getData("sizingpolicy").getString());
						
					artworkList.add(a);
				}
				
				ja.setArtwork(artworkList);
			}
			
			cjo.setAlbumArt(ja);
		}
		
		return cjo;
	}

	private Encryption getEncryption(cfStructData es) throws dataNotSupportedException {
		Encryption e	= new Encryption();
		
		if ( es.containsKey("key") )
			e.setKey( es.getData("key").getString() );
		
		if ( es.containsKey("keymd5") )
			e.setKeyMd5( es.getData("keymd5").getString() );

		if ( es.containsKey("mode") )
			e.setMode( es.getData("mode").getString() );

		if ( es.containsKey("initializationvector") )
			e.setInitializationVector( es.getData("initializationvector").getString() );

		return e;
	}

}