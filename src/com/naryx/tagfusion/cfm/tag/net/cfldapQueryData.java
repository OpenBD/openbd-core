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

package com.naryx.tagfusion.cfm.tag.net;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.engine.cfBinaryData;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;

/**
 * cfldapQueryData is the query object returned by cfLDAP when an 
 * LDAP query is made.
 */


public class cfldapQueryData { 

  /**
   * TODO: this is a bit of a mess because "_results" is passed in as type List<Map<String, String>>, but
   * gets converted to type List<Map<String, cfData>> before being passed to queryResults.populateQuery().
   * Therefore, it's going to take a bit of work to get rid of the raw List types and replace them with
   * parameterized types.
   */
  public cfldapQueryData( cfSession _Session, String _name, List /* List<Map<String, String> */ _results, String [] _attributes, 
                   String _sort, String _sortControl, int _startRow, int _maxRows, List<String> _returnAsBinary ) throws cfmRunTimeException{

    List /* List<Map<String, cfData> */ data = _results;
    String [] columnList;
    
    // note ordering differs depending on if attributes=*
    if ( _attributes.length == 1 && _attributes[0].equals("*") ){
      // get name-value pairs
      data = sortResults( data, _sort, _sortControl ); 
      data = adjustToStartRow( data, _startRow );
      data = convertToNameValuePairs( data );
      data = removeExcessRows( data, _maxRows );
      columnList = new String [] {"name","value"};
    }else{
      data = removeExcessRows( data, _maxRows );
      data = sortResults( data, _sort, _sortControl );   
      data = adjustToStartRow( data, _startRow );
      data = convertResultsForQuery( data, _attributes, _returnAsBinary );
      columnList = _attributes;
    }

    cfQueryResultData queryResult = new cfQueryResultData( columnList, "CFLDAP" );
    //printResults( data ); // testing
	queryResult.populateQuery( data );

    _Session.setData( _name, queryResult );

 }// cfldapQueryData()


  private static List adjustToStartRow( List _data, int _startRow ){
    for ( int i = 1; i < _startRow ; i++ ){
      _data.remove( 0 );
    }
    return _data;
  }// adjustToStartRow()


  private List sortResults( List _data, String _sort, String _sortControl ){
    if ( !_sort.equals("") ){
      
      String [] control = com.nary.util.string.convertToList( _sortControl.toLowerCase(), ',' );
      boolean caseSensitive = true;
      boolean ascending = true;
      for (int i = 0; i < control.length; i++ ){
        if ( control[i].equals( "asc" )){
          ascending = true;
        }else if ( control[i].equals( "desc" )){
          ascending = false;
        }else if ( control[i].equals( "nocase" )){
          caseSensitive = false;
        }
      }

      String [] sortOnAttributes = com.nary.util.string.convertToList( _sort.toLowerCase(), ',' );
      Collections.sort( _data, new entryComparator( sortOnAttributes, caseSensitive, ascending ) );
    }
    return _data;
  }// sortResults()


  private static List convertToNameValuePairs( List _data){
	  List newData = new ArrayList();

    int dataLength = _data.size();
    // for each entry
    for ( int i = 0; i < dataLength; i++ ){
      Map entry = (Map)_data.get(i);
      // get all the attributes (key-value pairs) putting them into a vector of hashtables of name-value pairs
      Iterator keys = entry.keySet().iterator();
      Object nextEntry;
      while ( keys.hasNext() ){
        String name = keys.next().toString();
        FastMap singleEntry = new FastMap(2);
        singleEntry.put( "name", new cfStringData(name) );

        nextEntry = entry.get(name);
        if ( nextEntry.getClass().isArray() && nextEntry.getClass().getComponentType().equals( byte.class ) ){
          singleEntry.put( "value", new cfStringData( new String( (byte[]) nextEntry ) ) );
        }else{
          singleEntry.put( "value", new cfStringData( (String) nextEntry ) );
        }

        newData.add( singleEntry );
      }
    }
    return newData;

  }// convertToNameValuePairs()

  private static List removeExcessRows( List _data, int _maxEntries ){
    if ( _maxEntries!= 0 && _maxEntries < _data.size() ){ // remove excess entries
      int no_excess = _data.size() - _maxEntries;
      for (int i = 0; i < no_excess; i++ ){
        _data.remove( _data.get( _data.size()-1 ) );
      }
    }
    return _data;
  }// removeExcessEntries


  // converts all results values from Strings to cfStringDatas and
  // adds in blank entries where an entry does not have the req'd attributes
  private static List convertResultsForQuery( List _data, String [] _columnList, List<String> _returnAsBinary ){
    
  	boolean [] returnAsBinary = new boolean[ _columnList.length ];
  	if ( _returnAsBinary != null ){
	  	for ( int i = 0; i < _columnList.length; i++ ){
	  		if ( _returnAsBinary.contains( _columnList[i] ) ){
	  			returnAsBinary[i] = true;
	  		} // else default is false
	  	}
  	}
  	
  	Iterator iter = _data.iterator();
    while (iter.hasNext()){
    
      Map entry = (Map) iter.next();
      for ( int i = 0; i < _columnList.length; i++ ){
        String key = _columnList[i];//(String)enum2.nextElement();
        try{
          if ( entry.containsKey( key ) ){
            Object nextEntry = entry.get( key );
            if ( nextEntry.getClass().isArray() && nextEntry.getClass().getComponentType().equals( byte.class ) ){
            	if ( returnAsBinary[i] ){
            		entry.put( key, new cfBinaryData( (byte[]) nextEntry ) );
            	}else{
	          	 	entry.put( key, new cfStringData( new String( (byte[]) nextEntry ) ) );
            	}
            }else{
            	if ( returnAsBinary[i] ){
            		entry.put( key, new cfBinaryData( ( (String) nextEntry ).getBytes() ) );
            	}else{
            		entry.put( key, new cfStringData( (String) nextEntry ) );
            	}
            }
          }else{
            entry.put( key, new cfStringData( "" ) );
          }
        }catch(Exception e){
          cfEngine.log("Exception thrown by " + key + " : " + e.getMessage() );
        }
      }
    }

    return _data;
  }// convertResultsToCFString()



  /**--- class entryComparator -----------------------------------------**/

  public class entryComparator implements Comparator<Map<String, String>>{
  
    String [] attributes;
    boolean caseSensitive;
    boolean ascending;

    public entryComparator( String [] _attributes, boolean _caseSensitive, boolean _asc ){
      attributes = _attributes;
      caseSensitive = _caseSensitive;
      ascending = _asc;
    } 

    public int compare( Map<String, String> ht1, Map<String, String> ht2 ) {
      int compResult = 0;

      for (int i = 0; i < attributes.length; i++ ){
        compResult = compareOnAttribute( attributes[i], ht1, ht2 );
        if ( compResult != 0 ){
          return compResult;
        }
      }
            
      return compResult;
    }// compare()


    private int compareOnAttribute( String attribute, Map<String, String> o1, Map<String, String> o2 ) {
      String str1 = o1.get( attribute );
      String str2 = o2.get( attribute );
      
      if ( str1 == null ){ 
        str1 = "";
      }
      if ( str2 == null ){
        str2 = "";
      }

      if ( caseSensitive ){
        if ( ascending ){
          return str1.compareTo( str2 );        
        }else{
          return str2.compareTo( str1 );
        }
      }

      if ( ascending ){
        return str1.toLowerCase().compareTo( str2.toLowerCase() );      
      }else{
        return str2.toLowerCase().compareTo( str1.toLowerCase() );      
      }
    }// compareOnAttribute()

  }// entryComparator

}//cfldapQueryData


