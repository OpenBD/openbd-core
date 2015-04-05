/* 
 *  Copyright (C) 2000-2015 aw2.0 LTD
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
 *  http://www.openbd.org/
 *  $Id: cfDecodedInput.java 2526 2015-02-26 15:58:34Z alan $
 */

package com.naryx.tagfusion.cfm.engine;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.vfs.FileSystemException;

import com.nary.servlet.MultipartRequestDecode;
import com.nary.servlet.MultiPartUploadedFile;


/**
 * This class is used to co-ordinate the multi-part HTTP streams for uploading files
 */
public class cfDecodedInput extends Object implements java.io.Serializable{
	private static final long serialVersionUID = 1L;

	private List<MultiPartUploadedFile> fileList;
  private Hashtable rawParams;
 
  public static final String DATA_BIN_KEY = "CFDECODEDINPUT_DATA";
 
  private String encoding;
  
	public void setFiles( cfSession _session, List<MultiPartUploadedFile> _fileList ) {
		fileList	= _fileList;
		MultiPartUploadedFile nextFile;
		cfData formData = _session.getQualifiedData( variableStore.FORM_SCOPE );
		
		// loop thru files adding formName-realFile pair to formData 
		Iterator<MultiPartUploadedFile>	it = fileList.iterator();
		while ( it.hasNext() ){
			nextFile = it.next();

			if ( nextFile.filename.length() == 0 ){
        formData.setData( nextFile.formName, cfStringData.EMPTY_STRING );
      }else{
  		  formData.setData( nextFile.formName, new cfStringData( nextFile.tempFile.toString() ) );
      }
    }
	}
	
	public void setParameters( cfSession Session, MultipartRequestDecode	mRequest ) throws Exception {
		Enumeration E = mRequest.getParameterNames();
    rawParams = mRequest.getRawParameters();
		String key, data;
		cfFormData formData = (cfFormData) Session.getQualifiedData( variableStore.FORM_SCOPE );
		while ( E.hasMoreElements() ){
			key		= (String)E.nextElement();
			data	= mRequest.getParameter( key );
			formData.setData( key, new cfStringData( data ) );
		}
    if ( formData.size() > 0 ){
      formData.put( "fieldnames",  new cfStringData( formData.getKeyList( "," ) ) );
    }
	}

	
	
	/**
	 * Returns the file form that was uploaded
	 * 
	 * @param Name
	 * @return
	 * @throws FileSystemException 
	 */
	public MultiPartUploadedFile getFile( String Name ) {
		
		Iterator<MultiPartUploadedFile>	it = fileList.iterator();
		MultiPartUploadedFile uFile;
		while ( it.hasNext() ){
			uFile	= it.next();
			
			if ( uFile.formName.equalsIgnoreCase( Name ) && uFile.tempFile.length() != 0 )
				return uFile;
		}
		
		return null;
	}
	
	
	/**
	 * Returns a list of all the uploaded files
	 * 
	 * @return
	 */
	public	List<MultiPartUploadedFile>	getFiles(){
		List<MultiPartUploadedFile>	fL	= new ArrayList<MultiPartUploadedFile>(2);
		
		Iterator<MultiPartUploadedFile>	it = fileList.iterator();
		MultiPartUploadedFile uFile;
		while ( it.hasNext() ){
			uFile	= it.next();
			if ( uFile.tempFile.length() != 0 )
				fL.add( uFile );
		}
		
		return fL;
	}
	
	
	/**
	 * Deletes all the uploaded files
	 */
	public void deleteFiles(){
		Iterator<MultiPartUploadedFile>	it = fileList.iterator();
		while ( it.hasNext() ){
			it.next().deleteTempFile();
		}
	}
  
  
  public void reencodeParameters( cfSession _session, String _currEncoding, String _encoding ) throws UnsupportedEncodingException{

      // don't need to reencode if it's already this encoding
      if ( _encoding.equalsIgnoreCase( encoding ) ){
        return;
      }

      cfFormData formdata = (cfFormData) _session.getQualifiedData( variableStore.FORM_SCOPE );
      Enumeration keys = rawParams.keys();
      while ( keys.hasMoreElements() ){
        String nextKey = (String) keys.nextElement();
        Object nextElement = rawParams.get( nextKey );
        if ( nextElement instanceof List ){
          List values = (List) nextElement;
          StringBuilder valuesStr = new StringBuilder( (String) values.get(0) );
          for ( int i = 1; i < values.size(); i++ ){
            String nextValue = (String) values.get(i);
            valuesStr.append( ',' );
            valuesStr.append( new String( nextValue.getBytes(), _encoding ) );
          }
        }else{
          String reencoded = new String( ( (String) nextElement).getBytes(), _encoding );
          formdata.put( nextKey, new cfStringData(reencoded) );
        }
      }
      
      formdata.remove( "fieldnames" );
      if ( formdata.size() > 0 ){
        formdata.put( "fieldnames",  new cfStringData( formdata.getKeyList( "," ) ) );
      }
      
      // the filenames also need to be converted to the new encoding
  		Iterator<MultiPartUploadedFile>	it = fileList.iterator();
  		while ( it.hasNext() ){
      	MultiPartUploadedFile nextFile = it.next();
      	nextFile.filename = new String( nextFile.filename.getBytes( _currEncoding ), _encoding );
      }
  }
	

	//-----------------------------------------------------------------
	//--] This is a static method for pulling back the content
	//-----------------------------------------------------------------
	
	public static void checkAndDecodePost( cfSession Session ) throws cfmRunTimeException {

		// Check to see if the request is the result of a file upload
		if ( Session.REQ.getContentType() == null || Session.REQ.getContentType().indexOf("multipart/form-data") == -1 )
			return;

		try{
	  	// Register this file with the Session so a cfFILE=UPLOAD may be able to get it
  		cfDecodedInput DI	= new cfDecodedInput();
 			Session.setDataBin( DATA_BIN_KEY, DI );

			// Decode the input stream
      String encoding = cfEngine.getDefaultEncoding();
      cfFormData formData = (cfFormData)Session.getQualifiedData(variableStore.FORM_SCOPE);
      
      byte[] requestData = formData.getRequestData();
      InputStream requestStream = null;
      
      if ( requestData != null ){ // unlikely
      	requestStream = new ByteArrayInputStream( requestData );
      }else{ // multipart/form-data
      	requestStream = Session.REQ.getInputStream();
      }
      
			MultipartRequestDecode	mRequest	= new MultipartRequestDecode( Session.REQ.getContentType(), 
					requestStream, 
					cfEngine.thisPlatform.getFileIO().getTempDirectory(), 
					encoding ); 
			
      DI.encoding = encoding;
			DI.setFiles( Session, mRequest.getFiles() );
			DI.setParameters( Session, mRequest );			
			
		}catch(cfmRunTimeException RTE){
      throw RTE;
    }catch(Exception E){
			cfCatchData	catchData	= new cfCatchData( Session );
    	catchData.setMessage( "An error has occurred while attempting to process the incoming multipart/form-data stream: " + E );
    	throw new cfmRunTimeException( catchData );
    }
	}
}
