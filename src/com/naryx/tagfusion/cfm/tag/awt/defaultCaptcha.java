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
 * Created on 14-Jan-2005
 *
 * Default implementation of the BlueDragon rendering for Captcha
 */
package com.naryx.tagfusion.cfm.tag.awt;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Image;

import com.bluedragon.captcha.RenderEngine;
import com.bluedragon.captcha.RenderImage;

public class defaultCaptcha implements RenderImage {

	public Image renderCaptcha( RenderEngine renderEngine, String displayText, int width, int height) {
    com.nary.awt.Graphics2D  G = new com.nary.awt.Graphics2D( width, height );
    G.setColor( Color.white );
    G.fillRect( 0, 0, width, height );
    
    Font regularFont = new Font("Courier", Font.PLAIN, 16);
    G.setFont( regularFont );
    
    G.setColor( G.createColor( 255, renderEngine.rand( 130, 230), 0, renderEngine.rand( 70, 190)) );
    G.fillArc( renderEngine.rand( 40, 80), height/2, 40, 40, 0, 180 );
    
    G.setColor( G.createColor( 255, renderEngine.rand( 130, 230), 255, renderEngine.rand( 70, 190)) );
    G.fillArc( renderEngine.rand( 0, 40), -(height), 40, 40, 0, -180 );
    
    int posX = renderEngine.rand( 2, 10);
    int posY = renderEngine.rand( 1, 4);
    for ( int x=0; x < displayText.length(); x++ ){

    	G.setColor( G.createColor( 	renderEngine.rand( 130, 230),renderEngine.rand( 60, 200),renderEngine.rand( 120, 200)	) );

    	G.rotate( Math.PI/24 );

      if ( x%2 == 0 ){
      	G.setFont( G.deriveFont( (float)renderEngine.rand( 15, 19) ) );
      }else if ( x%3 == 0 ){
        G.setFont( G.deriveFont( Font.BOLD, (float)renderEngine.rand( 16, 21)) );
      }else{
      	G.setFont( regularFont );
      }

      FontMetrics fM	= G.getFontMetrics();

      G.drawString( displayText.charAt(x)+"", posX, posY+ fM.getHeight()/2 );
      posX = posX + fM.charWidth(displayText.charAt(x)) + 2;

      G.rotate( -Math.PI/22 );
    }

    return G.getImage();
	}
}
