/* 
 *  Copyright (C) 2000 - 2009 TagServlet Ltd
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
package org.alanwilliamson.openbd.plugin.spreadsheet;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;

import com.naryx.tagfusion.cfm.engine.cfStructData;

public class SpreadSheetFormatOptions extends Object {
	
	private	static	Map<String,Short> lookup_colors;
	private	static	Map<String,Short> lookup_alignment;
	private	static	Map<String,Short> lookup_border;
	private	static	Map<String,Short> lookup_fillpatten;
	private	static	Map<String,Byte> 	lookup_underline;
	private	static	Map<String,Color> lookup_color;
	
	public static void initialize(){
		lookup_colors			= new HashMap<String,Short>();
		lookup_alignment	= new HashMap<String,Short>();
		lookup_border			= new HashMap<String,Short>();
		lookup_fillpatten	= new HashMap<String,Short>();
		lookup_underline	= new HashMap<String,Byte>();
		lookup_color			= new HashMap<String,Color>();
		
		lookup_underline.put( "double", Font.U_DOUBLE );
		lookup_underline.put( "double_accounting", Font.U_DOUBLE_ACCOUNTING );
		lookup_underline.put( "single", Font.U_SINGLE );
		lookup_underline.put( "single_accounting", Font.U_SINGLE_ACCOUNTING );
		lookup_underline.put( "none", Font.U_NONE );
		
		lookup_colors.put( "black", IndexedColors.BLACK.getIndex() );
		lookup_colors.put( "brown", IndexedColors.BROWN.getIndex() );
		lookup_colors.put( "olive_green", IndexedColors.OLIVE_GREEN.getIndex() );
		lookup_colors.put( "dark_green", IndexedColors.DARK_GREEN.getIndex() );
		lookup_colors.put( "dark_teal", IndexedColors.DARK_TEAL.getIndex() );
		lookup_colors.put( "dark_blue", IndexedColors.DARK_BLUE.getIndex() );
		lookup_colors.put( "indigo", IndexedColors.INDIGO.getIndex() );
		lookup_colors.put( "grey_80_percent", IndexedColors.GREY_80_PERCENT.getIndex() );
		lookup_colors.put( "grey_50_percent", IndexedColors.GREY_50_PERCENT.getIndex() );
		lookup_colors.put( "grey_40_percent", IndexedColors.GREY_40_PERCENT.getIndex() );
		lookup_colors.put( "grey_25_percent", IndexedColors.GREY_25_PERCENT.getIndex() );
		lookup_colors.put( "orange", IndexedColors.ORANGE.getIndex() );
		lookup_colors.put( "dark_yellow", IndexedColors.DARK_YELLOW.getIndex() );
		lookup_colors.put( "green", IndexedColors.GREEN.getIndex() );
		lookup_colors.put( "teal", IndexedColors.TEAL.getIndex() );
		lookup_colors.put( "blue", IndexedColors.BLUE.getIndex() );
		lookup_colors.put( "blue_grey", IndexedColors.BLUE_GREY.getIndex() );
		lookup_colors.put( "red", IndexedColors.RED.getIndex() );
		lookup_colors.put( "light_orange", IndexedColors.LIGHT_ORANGE.getIndex() );
		lookup_colors.put( "lime", IndexedColors.LIME.getIndex() );
		lookup_colors.put( "sea_green", IndexedColors.SEA_GREEN.getIndex() );
		lookup_colors.put( "aqua", IndexedColors.AQUA.getIndex() );
		lookup_colors.put( "light_blue", IndexedColors.LIGHT_BLUE.getIndex() );
		lookup_colors.put( "violet", IndexedColors.VIOLET.getIndex() );
		lookup_colors.put( "pink", IndexedColors.PINK.getIndex() );
		lookup_colors.put( "gold", IndexedColors.GOLD.getIndex() );
		lookup_colors.put( "yellow", IndexedColors.YELLOW.getIndex() );
		lookup_colors.put( "bright_green", IndexedColors.BRIGHT_GREEN.getIndex() );
		lookup_colors.put( "turquoise", IndexedColors.TURQUOISE.getIndex() );
		lookup_colors.put( "dark_red", IndexedColors.DARK_RED.getIndex() );
		lookup_colors.put( "sky_blue", IndexedColors.SKY_BLUE.getIndex() );
		lookup_colors.put( "plum", IndexedColors.PLUM.getIndex() );
		lookup_colors.put( "rose", IndexedColors.ROSE.getIndex() );
		lookup_colors.put( "light_yellow", IndexedColors.LIGHT_YELLOW.getIndex() );
		lookup_colors.put( "light_green", IndexedColors.LIGHT_GREEN.getIndex() );
		lookup_colors.put( "light_turquoise", IndexedColors.LIGHT_TURQUOISE.getIndex() );
		lookup_colors.put( "pale_blue", IndexedColors.PALE_BLUE.getIndex() );
		lookup_colors.put( "lavender", IndexedColors.LAVENDER.getIndex() );
		lookup_colors.put( "white", IndexedColors.WHITE.getIndex() );
		lookup_colors.put( "cornflower_blue", IndexedColors.CORNFLOWER_BLUE.getIndex() );
		lookup_colors.put( "lemon_chiffon", IndexedColors.LEMON_CHIFFON.getIndex() );
		lookup_colors.put( "maroon", IndexedColors.MAROON.getIndex() );
		lookup_colors.put( "orchid", IndexedColors.ORCHID.getIndex() );
		lookup_colors.put( "coral", IndexedColors.CORAL.getIndex() );
		lookup_colors.put( "royal_blue", IndexedColors.ROYAL_BLUE.getIndex() );
		lookup_colors.put( "light_cornflower_blue", IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex() );
		
		lookup_alignment.put( "left", CellStyle.ALIGN_LEFT );
		lookup_alignment.put( "right", CellStyle.ALIGN_RIGHT );
		lookup_alignment.put( "center", CellStyle.ALIGN_CENTER );
		lookup_alignment.put( "justify", CellStyle.ALIGN_JUSTIFY );
		lookup_alignment.put( "general", CellStyle.ALIGN_GENERAL );
		lookup_alignment.put( "fill", CellStyle.ALIGN_FILL );
		lookup_alignment.put( "center_selection", CellStyle.ALIGN_CENTER_SELECTION );
		lookup_alignment.put( "vertical_top", CellStyle.VERTICAL_TOP );
		lookup_alignment.put( "vertical_bottom", CellStyle.VERTICAL_BOTTOM );
		lookup_alignment.put( "vertical_center", CellStyle.VERTICAL_CENTER );
		lookup_alignment.put( "vertical_justify", CellStyle.VERTICAL_JUSTIFY );
		
		
		lookup_border.put( "none", CellStyle.BORDER_NONE );
		lookup_border.put( "thin", CellStyle.BORDER_THIN );
		lookup_border.put( "medium", CellStyle.BORDER_MEDIUM );
		lookup_border.put( "dashed", CellStyle.BORDER_DASHED );
		lookup_border.put( "hair", CellStyle.BORDER_HAIR );
		lookup_border.put( "thick", CellStyle.BORDER_THICK );
		lookup_border.put( "double", CellStyle.BORDER_DOUBLE );
		lookup_border.put( "dotted", CellStyle.BORDER_DOTTED );
		lookup_border.put( "medium_dashed", CellStyle.BORDER_MEDIUM_DASHED );
		lookup_border.put( "dash_dot", CellStyle.BORDER_DASH_DOT );
		lookup_border.put( "medium_dash_dot", CellStyle.BORDER_MEDIUM_DASH_DOT );
		lookup_border.put( "dash_dot_dot", CellStyle.BORDER_DASH_DOT_DOT );
		lookup_border.put( "medium_dash_dot_dot", CellStyle.BORDER_MEDIUM_DASH_DOT_DOT );
		lookup_border.put( "slanted_dash_dot", CellStyle.BORDER_SLANTED_DASH_DOT );
		
		
		lookup_fillpatten.put( "big_spots", CellStyle.BIG_SPOTS );
		lookup_fillpatten.put( "squares", CellStyle.SQUARES );
		lookup_fillpatten.put( "nofill", CellStyle.NO_FILL );
		lookup_fillpatten.put( "solid_foreground", CellStyle.SOLID_FOREGROUND );
		lookup_fillpatten.put( "fine_dots", CellStyle.FINE_DOTS );
		lookup_fillpatten.put( "alt_bars", CellStyle.ALT_BARS );
		lookup_fillpatten.put( "sparse_dots", CellStyle.SPARSE_DOTS );
		lookup_fillpatten.put( "thick_horz_bands", CellStyle.THICK_HORZ_BANDS );
		lookup_fillpatten.put( "thick_vert_bands", CellStyle.THICK_VERT_BANDS );
		lookup_fillpatten.put( "thick_backward_diag", CellStyle.THICK_BACKWARD_DIAG );
		lookup_fillpatten.put( "thick_forward_diag", CellStyle.THICK_FORWARD_DIAG );
		lookup_fillpatten.put( "thin_horz_bands", CellStyle.THIN_HORZ_BANDS );
		lookup_fillpatten.put( "thin_vert_bands", CellStyle.THIN_VERT_BANDS );
		lookup_fillpatten.put( "thin_backward_diag", CellStyle.THIN_BACKWARD_DIAG );
		lookup_fillpatten.put( "thin_forward_diag", CellStyle.THIN_FORWARD_DIAG );
		lookup_fillpatten.put( "diamonds", CellStyle.DIAMONDS );
		lookup_fillpatten.put( "less_dots", CellStyle.LESS_DOTS );
		lookup_fillpatten.put( "least_dots", CellStyle.LEAST_DOTS );
		
		
		lookup_color.put("black", Color.BLACK );
		lookup_color.put("blue", Color.BLUE );
		lookup_color.put("cyan", Color.CYAN );
		lookup_color.put("dark_gray", Color.DARK_GRAY );
		lookup_color.put("darkGray", 	Color.DARK_GRAY );
		lookup_color.put("gray", 	Color.GRAY );
		lookup_color.put("green", 	Color.GREEN );
		lookup_color.put("light_gray", 	Color.LIGHT_GRAY );
		lookup_color.put("lightGray", 	Color.LIGHT_GRAY );
		lookup_color.put("magenta", 	Color.MAGENTA );
		lookup_color.put("orange", 	Color.ORANGE );
		lookup_color.put("pink", 	Color.PINK );
		lookup_color.put("red", 	Color.RED );
		lookup_color.put("white", 	Color.WHITE );
		lookup_color.put("yellow", 	Color.YELLOW );
	}
	
	public static Color getJavaColor( String name ){
		return lookup_color.get( name.toLowerCase() );
	}
	
	public static Font createCommentFont( Workbook workbook, cfStructData _struct ) throws Exception {
		Font font = workbook.createFont();
		
		if ( _struct.containsKey("bold") ){
			if ( _struct.getData("bold").getBoolean() )
				font.setBoldweight( Font.BOLDWEIGHT_BOLD );
			else
				font.setBoldweight( Font.BOLDWEIGHT_NORMAL );
		}
		
		if ( _struct.containsKey("color") ){
			String v 	= _struct.getData("color").getString();
			Short s 	= lookup_colors.get( v );
			if ( s == null ){
				throw new Exception( "invalid parameter for 'color' (" + v + ")" );
			}else
				font.setColor( s );
		}
		
		if ( _struct.containsKey("font") ){
			font.setFontName( _struct.getData("font").getString() );
		}
		
		if ( _struct.containsKey("italic") ){
			font.setItalic( _struct.getData("italic").getBoolean() );
		}
		
		if ( _struct.containsKey("strikeout") ){
			font.setStrikeout( _struct.getData("strikeout").getBoolean() );
		}
		
		if ( _struct.containsKey("underline") ){
			font.setUnderline( (byte)_struct.getData("underline").getInt() );
		}

		if ( _struct.containsKey("size") ){
			font.setFontHeightInPoints( (short)_struct.getData("size").getInt() );
		}
		
		return font;
	}
	
	
	public static String createCellStyleHelp(){
		StringBuilder	s = new StringBuilder(1000);
		
		s.append( "alignment=[" );
		Iterator<String>	it	= lookup_alignment.keySet().iterator();
		while ( it.hasNext() )
			s.append( it.next() + " | " );

		s.deleteCharAt( s.length()-1 );
		s.deleteCharAt( s.length()-1 );
		s.append("]; ");
		
		s.append( "bottomborder,topborder,leftborder,rightborder=[" );
		it	= lookup_border.keySet().iterator();
		while ( it.hasNext() )
			s.append( it.next() + " | " );

		s.deleteCharAt( s.length()-1 );
		s.deleteCharAt( s.length()-1 );
		s.append("]; ");
	
		
		s.append( "bottombordercolor,topbordercolor,leftbordercolor,rightbordercolor,fgcolor,bgcolor,color=[" );
		it	= lookup_colors.keySet().iterator();
		while ( it.hasNext() )
			s.append( it.next() + " | " );

		s.deleteCharAt( s.length()-1 );
		s.deleteCharAt( s.length()-1 );
		s.append("]; ");
		
		s.append( "fillpattern=[" );
		it	= lookup_fillpatten.keySet().iterator();
		while ( it.hasNext() )
			s.append( it.next() + " | " );

		s.deleteCharAt( s.length()-1 );
		s.deleteCharAt( s.length()-1 );
		s.append("]; ");
	
		s.append( "textwrap=[true|false]; " );
		s.append( "hidden=[true|false]; " );
		s.append( "locked=[true|false]; " );
		s.append( "indent=[value]; " );
		s.append( "rotation=[value]; " );
		s.append( "dateformat=[format]; " );
		s.append( "strikeout=[true|false]; " );
		s.append( "bold=[true|false]; " );
		s.append( "underline=[value]; " );
		s.append( "fontsize=[value]; " );
		s.append( "font=[face]; " );
		
		return s.toString();
	}
	
	
	public static CellStyle	createCellStyle( Workbook workbook, cfStructData _struct ) throws Exception {
		CellStyle style	= workbook.createCellStyle();
		
		if ( _struct.containsKey("alignment") ){
			String v 	= _struct.getData("alignment").getString();
			Short s 	= lookup_alignment.get( v );
			if ( s == null ){
				throw new Exception( "invalid parameter for 'alignment' (" + v + ")" );
			}else
				style.setAlignment( s );
		}
		
		
		if ( _struct.containsKey("bottomborder") ){
			String v 	= _struct.getData("bottomborder").getString();
			Short s 	= lookup_border.get( v );
			if ( s == null ){
				throw new Exception( "invalid parameter for 'bottomborder' (" + v + ")" );
			}else
				style.setBorderBottom( s );
		}
		
		if ( _struct.containsKey("topborder") ){
			String v 	= _struct.getData("topborder").getString();
			Short s 	= lookup_border.get( v );
			if ( s == null ){
				throw new Exception( "invalid parameter for 'topborder' (" + v + ")" );
			}else
				style.setBorderTop( s );
		}
		
		if ( _struct.containsKey("leftborder") ){
			String v 	= _struct.getData("leftborder").getString();
			Short s 	= lookup_border.get( v );
			if ( s == null ){
				throw new Exception( "invalid parameter for 'leftborder' (" + v + ")" );
			}else
				style.setBorderLeft( s );
		}
		
		if ( _struct.containsKey("rightborder") ){
			String v 	= _struct.getData("rightborder").getString();
			Short s 	= lookup_border.get( v );
			if ( s == null ){
				throw new Exception( "invalid parameter for 'rightborder' (" + v + ")" );
			}else
				style.setBorderRight( s );
		}
		
		if ( _struct.containsKey("bottombordercolor") ){
			String v 	= _struct.getData("bottombordercolor").getString();
			Short s 	= lookup_colors.get( v );
			if ( s == null ){
				throw new Exception( "invalid parameter for 'bottombordercolor' (" + v + ")" );
			}else
				style.setBottomBorderColor( s );
		}
		
		if ( _struct.containsKey("topbordercolor") ){
			String v 	= _struct.getData("topbordercolor").getString();
			Short s 	= lookup_colors.get( v );
			if ( s == null ){
				throw new Exception( "invalid parameter for 'topbordercolor' (" + v + ")" );
			}else
				style.setTopBorderColor( s );
		}
		
		if ( _struct.containsKey("leftbordercolor") ){
			String v 	= _struct.getData("leftbordercolor").getString();
			Short s 	= lookup_colors.get( v );
			if ( s == null ){
				throw new Exception( "invalid parameter for 'leftbordercolor' (" + v + ")" );
			}else
				style.setLeftBorderColor( s );
		}
		
		if ( _struct.containsKey("rightbordercolor") ){
			String v 	= _struct.getData("rightbordercolor").getString();
			Short s 	= lookup_colors.get( v );
			if ( s == null ){
				throw new Exception( "invalid parameter for 'rightbordercolor' (" + v + ")" );
			}else
				style.setRightBorderColor( s );
		}
		
		if ( _struct.containsKey("fillpattern") ){
			String v 	= _struct.getData("fillpattern").getString();
			Short s 	= lookup_fillpatten.get( v );
			if ( s == null ){
				throw new Exception( "invalid parameter for 'fillpattern' (" + v + ")" );
			}else
				style.setFillPattern( s );
		}
		
		if ( _struct.containsKey("fgcolor") ){
			String v 	= _struct.getData("fgcolor").getString();
			Short s 	= lookup_colors.get( v );
			if ( s == null ){
				throw new Exception( "invalid parameter for 'fgcolor' (" + v + ")" );
			}else
				style.setFillForegroundColor( s );
		}
		
		if ( _struct.containsKey("bgcolor") ){
			String v 	= _struct.getData("bgcolor").getString();
			Short s 	= lookup_colors.get( v );
			if ( s == null ){
				throw new Exception( "invalid parameter for 'bgcolor' (" + v + ")" );
			}else
				style.setFillBackgroundColor( s );
		}
		
		if ( _struct.containsKey("textwrap") ){
			Boolean b 	= _struct.getData("textwrap").getBoolean();
			style.setWrapText( b );
		}

		if ( _struct.containsKey("hidden") ){
			Boolean b 	= _struct.getData("hidden").getBoolean();
			style.setHidden( b );
		}

		if ( _struct.containsKey("locked") ){
			Boolean b 	= _struct.getData("locked").getBoolean();
			style.setLocked( b );
		}

		if ( _struct.containsKey("indent") ){
			style.setIndention( (short)_struct.getData("indent").getInt() );
		}
		
		if ( _struct.containsKey("rotation") ){
			style.setRotation( (short)_struct.getData("rotation").getInt() );
		}

		if ( _struct.containsKey("dateformat") ){
			style.setDataFormat( workbook.createDataFormat().getFormat( _struct.getData("dateformat").getString() ) );
		}
		
		// Manage the fonts
		Font f = workbook.createFont();
		
		if ( _struct.containsKey("strikeout") ){
			f.setStrikeout( true );
		}
		
		if ( _struct.containsKey("bold") ){
			Boolean b 	= _struct.getData("bold").getBoolean();
			f.setBoldweight( b ? Font.BOLDWEIGHT_BOLD : Font.BOLDWEIGHT_NORMAL );
		}
		
		if ( _struct.containsKey("underline") ){
			String v 	= _struct.getData("underline").getString();
			Byte b 	= lookup_underline.get( v );
			if ( b == null ){
				throw new Exception( "invalid parameter for 'underline' (" + v + ")" );
			}else
				f.setUnderline( b );
		}
		
		if ( _struct.containsKey("color") ){
			String v 	= _struct.getData("color").getString();
			Short s 	= lookup_colors.get( v );
			if ( s == null ){
				throw new Exception( "invalid parameter for 'color' (" + v + ")" );
			}else
				f.setColor( s );
		}
		
		if ( _struct.containsKey("fontsize") ){
			int	s = _struct.getData("fontsize").getInt();
			f.setFontHeightInPoints( (short)s );
		}

		if ( _struct.containsKey("font")){
			f.setFontName( _struct.getData("font").getString() );
		}
		
		style.setFont( f );
		
		return style;
	}
}