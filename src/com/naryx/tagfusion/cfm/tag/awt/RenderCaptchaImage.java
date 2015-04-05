/*
 *  Copyright (C) 2000 - 2008 TagServlet Ltd
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
 *  http://www.openbluedragon.org/
 */

/*
 * Created on 02-Jan-2005
 *
 */
package com.naryx.tagfusion.cfm.tag.awt;

import java.awt.Image;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Random;

import com.bluedragon.captcha.RenderEngine;
import com.bluedragon.captcha.RenderImage;
import com.nary.util.HashMapTimed;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.engine.engineListener;
import com.naryx.tagfusion.cfm.engine.variableStore;
import com.naryx.tagfusion.cfm.file.cfmlFileCache;
import com.naryx.tagfusion.cfm.file.cfmlURI;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;
import com.naryx.tagfusion.xmlConfig.xmlCFML;

public class RenderCaptchaImage extends graphingEngine implements RenderEngine, engineListener {

	private static final long serialVersionUID = 1L;

	private HashMapTimed	clientData;
	private	long					startId;
  private Random 				random;
  private	RenderImage		defaultRender;

  public RenderCaptchaImage(cfSession _Session){
    super();

    clientData	= new HashMapTimed( 60 );	//- 60seconds to get the request from the browser for this image
    startId			= System.currentTimeMillis();

    cfmlFileCache.insertCfmlFile( this, new cfmlURI( _Session.REQ, "/CFCaptchaEngine.cfm" ), _Session.REQ );

    engineAdminUpdate( cfEngine.getConfig() );
    cfEngine.registerEngineListener( this );
  }

  public void engineAdminUpdate( xmlCFML config ){

  	String renderClass = config.getString( "server.system.captcha", "com.naryx.tagfusion.cfm.tag.awt.defaultCaptcha" );

		try{
			Class C  			= Class.forName( renderClass );
			defaultRender = (RenderImage)C.newInstance();
			cfEngine.log( "-] Captcha Render Engine.Captcha: " + renderClass );
		}catch(Exception E){
			cfEngine.log( "-] Captcha Render Engine.Captcha.Error: " + renderClass + "; " + E.getMessage() );
			defaultRender	= null;
		}
  }

  public String getURL(cfSession Session){
		return Session.REQ.getContextPath() + "/CFCaptchaEngine.cfm";
	}

  public synchronized String setClientData( HashMap data ){
  	startId++;
  	String iD = startId + "";
  	clientData.put( iD, data );
  	return iD;
  }

  public cfTagReturnType render( cfSession _Session) throws cfmRunTimeException {

  	//-- Make sure there is a render method associated
  	if ( defaultRender == null ){
  		_Session.setStatus( 404 );
  		_Session.abortPageProcessing();
  	}

    _Session.setContentType( "image/jpeg" );

    cfData formData = _Session.getQualifiedData( variableStore.URL_SCOPE ); // the URL scope

    Image	image;

    HashMap	data	= (HashMap)clientData.get( formData.getData( "id" ).getString() );
    if ( data == null ){
    	image	= defaultRender.renderCaptcha(this, "invalid request",150,50);
    }else{
    	clientData.remove( formData.getData( "id" ).getString() );
    	image	= defaultRender.renderCaptcha(this, (String)data.get("DISPLAYSTRING"), ((Integer)data.get("WIDTH")).intValue(),  ((Integer)data.get("HEIGHT")).intValue() );
    }

    //-- Render this Image to a JPEG
    ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
    JpegEncoder jpg = new JpegEncoder(image, 80, outBuffer );
    jpg.Compress();

	_Session.writeAndClose( outBuffer.toByteArray() );

	return cfTagReturnType.NORMAL;
  }

  public int rand( int lo, int hi ){
    return getRandom().nextInt( hi - lo + 1 ) + lo;
  }

  public Random getRandom() {
  	if ( random == null ) {
        random = new Random();
    }
    return random;
  }

	public void engineShutdown() {}
}
