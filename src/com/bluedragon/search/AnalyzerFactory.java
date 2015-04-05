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
 *  $Id: AnalyzerFactory.java 2523 2015-02-22 16:23:11Z alan $
 */

package com.bluedragon.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.br.BrazilianAnalyzer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.cz.CzechAnalyzer;
import org.apache.lucene.analysis.da.DanishAnalyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.el.GreekAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.fi.FinnishAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.nl.DutchAnalyzer;
import org.apache.lucene.analysis.no.NorwegianAnalyzer;
import org.apache.lucene.analysis.pt.PortugueseAnalyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.sv.SwedishAnalyzer;

public class AnalyzerFactory {

  public static HashMap analyzers   = null;
  
  static{
    analyzers = new HashMap();
    analyzers.put( "english", 		new EnglishAnalyzer() );
    analyzers.put( "german", 			new GermanAnalyzer() );
    analyzers.put( "russian", 		new RussianAnalyzer() );
    analyzers.put( "brazilian", 	new BrazilianAnalyzer() );
    analyzers.put( "korean", 			new CJKAnalyzer() ); 
    analyzers.put( "chinese", 		new StandardAnalyzer() );
    analyzers.put( "japanese", 		new CJKAnalyzer() );
    analyzers.put( "czech", 			new CzechAnalyzer() );
    analyzers.put( "greek", 			new GreekAnalyzer() );
    analyzers.put( "french", 			new FrenchAnalyzer() );
    analyzers.put( "dutch", 			new DutchAnalyzer() );
    analyzers.put( "danish", 			new DanishAnalyzer() );
    analyzers.put( "finnish", 		new FinnishAnalyzer() );
    analyzers.put( "italian", 		new ItalianAnalyzer() );
    analyzers.put( "norwegian", 	new NorwegianAnalyzer() );
    analyzers.put( "portuguese", 	new PortugueseAnalyzer() );
    analyzers.put( "spanish", 		new SpanishAnalyzer() );
    analyzers.put( "swedish", 		new SwedishAnalyzer() );
    analyzers.put( "simple", 			new SimpleAnalyzer() );
    
    String s = System.getProperty(org.apache.lucene.analysis.Analyzer.class.getName());
    if (s != null) {
      try {
        Class klass = Class.forName(s);
        analyzers.put( "english", (Analyzer) klass.newInstance() );
      } catch (ClassNotFoundException ex) {
      } catch (InstantiationException ex) {
      } catch (IllegalAccessException ex) {
      }
    }
  }
  
  public static List getSupportedLanguages(){
    ArrayList langs = new ArrayList();
    Iterator keys = analyzers.keySet().iterator();
    while ( keys.hasNext() ){
      langs.add( keys.next() );
    }
    Collections.sort( langs );
    return langs;
  }


  public static Analyzer get( String _lang ){
    return (Analyzer) analyzers.get( _lang.toLowerCase() );
  }

  public static boolean isValid( String _lang ){
  	return analyzers.containsKey( _lang.toLowerCase() );
  }
}
