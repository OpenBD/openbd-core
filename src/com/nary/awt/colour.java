/* 
 *  Copyright (C) 2000 - 2010 TagServlet Ltd
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

package com.nary.awt;

/**
 * This class allows you to lookup html colours
 * either by name of hex code
 */
 
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class colour{ 

	static Map<String, String> colours;

  static{
    colours = new HashMap<String, String>();
    colours.put("aliceblue","f0f8ff");
    colours.put("antiquewhite","faebd7");
    colours.put("aqua","00ffff");	
    colours.put("aquamarine","7fffd4");
    colours.put("azure","f0ffff");
    colours.put("beige","f5f5dc");
    colours.put("bisque","ffe4c4");
    colours.put("black","000000");
    colours.put("blanchedalmond","ffebcd");
    colours.put("blue","0000ff");
    colours.put("blueviolet","8a2be2");
    colours.put("brown","a52a2a");
    colours.put("burlywood","deb887");
    colours.put("cadetblue","5f9ea0");
    colours.put("chartreuse","7fff00");
    colours.put("chocolate","d2691e");
    colours.put("coral","ff7f50");
    colours.put("cornflowerblue","6495ed");
    colours.put("cornsilk","fff8dc");
    colours.put("crimson","dc143c");
    colours.put("cyan","00ffff");
    colours.put("darkblue","00008b");
    colours.put("darkcyan","008b8b");
    colours.put("darkgoldenrod","b8860b");
    colours.put("darkgray","a9a9a9");
    colours.put("darkgreen","006400");
    colours.put("darkkhaki","bdb76b");
    colours.put("darkmagenta","8b008b");
    colours.put("darkolivegreen","556b2f");
    colours.put("darkorange","ff8c00");
    colours.put("darkorchid","9932cc");
    colours.put("darkred","8b0000");
    colours.put("darksalmon","e9967a");
    colours.put("darkseagreen","8fbc8f");
    colours.put("darkslateblue","483d8b");
    colours.put("darkslategray","2f4f4f");
    colours.put("darkturquoise","00ced1");
    colours.put("darkviolet","9400d3");
    colours.put("deeppink","ff1493");
    colours.put("deepskyblue","00bfff");
    colours.put("dimgray","696969");
    colours.put("dodgerblue","1e90ff");
    colours.put("firebrick","b22222");
    colours.put("floralwhite","fffaf0");
    colours.put("forestgreen","228b22");
    colours.put("fuchsia","ff00ff");
    colours.put("gainsboro","dcdcdc");
    colours.put("ghostwhite","f8f8ff");
    colours.put("gold","ffd700");
    colours.put("green","008000");
    colours.put("greenyellow","adff2f");
    colours.put("honeydew","f0fff0");
    colours.put("hotpink","ff69b4");
    colours.put("indianred","cd5c5c");
    colours.put("indigo","4b0082");
    colours.put("ivory","fffff0");
    colours.put("khaki","f0e68c");
    colours.put("lavender","e6e6fa");
    colours.put("lavenerblush","fff0f5");
    colours.put("lawngreen","7cfc00");
    colours.put("lemonchiffon","fffacd");
    colours.put("lightblue","add8e6");
    colours.put("lightcoral","f08080");
    colours.put("lightcyan","e0ffff");
    colours.put("lightgoldenrodyellow","fafad2");
    colours.put("lightgreen","90ee90");
    colours.put("lightgrey","d3d3d3");
    colours.put("lightpink","ffb6c1");
    colours.put("lightseagreen","20b2aa");
    colours.put("lightskyblue","87cefa");
    colours.put("lightslategray","778899");
    colours.put("lightsteelblue","b0c4de");
    colours.put("lightyellow","ffffe0");
    colours.put("lime","00ff00");
    colours.put("limegreen","32cd32");
    colours.put("linen","faf0e6");
    colours.put("magenta","ff00ff");
    colours.put("maroon","800000");
    colours.put("mediumaquamarine","66cdaa");
    colours.put("mediumblue","0000cd");
    colours.put("mediumorchid","ba55d3");
    colours.put("mediumpurple","9370db");
    colours.put("mediumseagreen","3cb371");
    colours.put("mediumslateblue","7b68ee");
    colours.put("mediumspringgreen","00fa9a");
    colours.put("mediumturquoise","48d1cc");
    colours.put("mediumvioletred","c71585");
    colours.put("midnightblue","191970");
    colours.put("mintcream","f5fffa");
    colours.put("mistyrose","ffe4e1");
    colours.put("moccasin","ffe4b5");
    colours.put("navajowhite","ffdead");
    colours.put("navy","000080");
    colours.put("oldlace","fdf5e6");
    colours.put("olive","808000");
    colours.put("olivedrab","6b8e23");
    colours.put("orange","ffa500");
    colours.put("orangered","ff4500");
    colours.put("orchid","da70d6");
    colours.put("palegoldenrod","eee8aa");
    colours.put("palegreen","98fb98");
    colours.put("paleturquoise","afeeee");
    colours.put("palevioletred","db7093");
    colours.put("papayawhip","ffefd5");
    colours.put("peachpuff","ffdab9");
    colours.put("peru","cd853f");
    colours.put("pink","ffc0cb");
    colours.put("plum","dda0dd");
    colours.put("powderblue","b0e0e6");
    colours.put("purple","800080");
    colours.put("red","ff0000");
    colours.put("rosybrown","bc8f8f");
    colours.put("royalblue","4169e1");
    colours.put("saddlebrown","8b4513");
    colours.put("salmon","fa8072");
    colours.put("sandybrown","faa460");
    colours.put("seagreen","2e8b57");
    colours.put("seashell","fff5ee");
    colours.put("sienna","a0522d");
    colours.put("silver","c0c0c0");
    colours.put("skyblue","87ceeb");
    colours.put("slateblue","6a5acd");
    colours.put("slategray","708090");
    colours.put("snow","fffafa");
    colours.put("springgreen","00ff7f");
    colours.put("steelblue","4682b4");
    colours.put("tan","d2b48c");
    colours.put("teal","008080");
    colours.put("thistle","d8bfd8");
    colours.put("tomato","ff6347");
    colours.put("turquoise","40e0d0");
    colours.put("violet","ee82ee");
    colours.put("wheat","f5deb3");
    colours.put("white","ffffff");
    colours.put("whitesmoke","f5f5f5");
    colours.put("yellow","ffff00");
    colours.put("yellowgreen","9acd32");
  }

  public static Color getColor( String cc ) {
    if ( cc.length() < 1 )
      return Color.black;

    String colourToDecode;
    if (colours.containsKey(cc.toLowerCase())){
      colourToDecode = colours.get(cc.toLowerCase());
    }else{
      colourToDecode = cc;
    }
 
    try{
      return Color.decode( "0x" + colourToDecode );
    }catch(Exception E){
      return Color.black;
    }

  }
	
}
