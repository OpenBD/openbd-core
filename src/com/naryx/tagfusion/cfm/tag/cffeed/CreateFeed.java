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
 * RSS2 specification @ http://cyber.law.harvard.edu/rss/rss.html
 * ATOM specification @ http://www.atompub.org/2005/07/11/draft-ietf-atompub-format-10.html
 * 
 */

package com.naryx.tagfusion.cfm.tag.cffeed;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.naryx.tagfusion.cfm.engine.cfArrayData;
import com.naryx.tagfusion.cfm.engine.cfArrayListData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfQueryResultData;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.rss.Channel;
import com.sun.syndication.feed.rss.Cloud;
import com.sun.syndication.feed.rss.Guid;
import com.sun.syndication.feed.rss.TextInput;
import com.sun.syndication.io.WireFeedOutput;

public class CreateFeed implements java.io.Serializable {
	static final long serialVersionUID = 1;

	private Channel	activeRSS = null;
	private Feed	activeATOM = null;
	
	public CreateFeed(){}

	public String publishFeedToString() throws Exception {
		if ( activeRSS != null )
			return new WireFeedOutput().outputString(activeRSS);
		else if ( activeATOM != null )
			return new WireFeedOutput().outputString(activeATOM);
		else
			return null;
	}
 
	
	/*
	 * Will attempt to create a feed from the data contained within
	 */
	public void createViaStruct(cfTag tag, cfStructData cffeeddata ) throws cfmRunTimeException {
		String type = cffeeddata.getData("version").getString();
		if ( type.equals("rss_2.0") ){
			activeRSS	= new Channel( type );
		}else if ( type.equals("atom_1.0") ){
			activeATOM = new Feed( type );
		}else{
			throw tag.newRunTimeException( "Invalid feed format; only supporting rss_2.0 and atom_1.0" );
		}
		
		
		if ( activeRSS != null ){
			renderMetaDataToRss( tag, cffeeddata );
			renderEntriesToRss( tag, cffeeddata );
		}else if ( activeATOM != null ){
			renderMetaDataToAtom( tag, cffeeddata );
			renderEntriesToAtom( tag, cffeeddata );
		}
	}
	
	
	
	public void createViaQuery(cfTag tag, cfQueryResultData query, cfStructData properties, cfStructData columnMap ) throws cfmRunTimeException {
		String type = properties.getData("version").getString();
		if ( type.equals("rss_2.0") ){
			activeRSS	= new Channel( type );
		}else if ( type.equals("atom_1.0") ){
			activeATOM = new Feed( type );
		}else{
			throw tag.newRunTimeException( "Invalid feed format; only supporting rss_2.0 and atom_1.0" );
		}

		
		if ( activeRSS != null ){
			renderMetaDataToRss( tag, properties );
			renderEntriesToRss( tag, convertQueryToStructRss(tag, query, columnMap) );
		}else if ( activeATOM != null ){
			renderMetaDataToAtom( tag, properties );
			renderEntriesToAtom( tag, convertQueryToStructAtom(tag, query, columnMap) );
		}
	}
	
	
	
	
	/*
	 *	Runs through the query and converts it into a structure
	 */
	private cfStructData convertQueryToStructAtom(cfTag tag, cfQueryResultData query, cfStructData columnMap ) throws cfmRunTimeException {
		cfStructData	structData	= new cfStructData();
		cfArrayData	itemArray			= cfArrayData.createArray(1);
		structData.setData( "item", itemArray );
		int idx = 0;

		for ( int rows=0; rows < query.getSize(); rows++ ){
			List<cfData> rowData	= query.getRow( rows );
			
			cfStructData	itemData	= new cfStructData();
			
			idx = getDataIndex( query, columnMap, "id" );
			if ( idx != -1 ){
				itemData.setData( "id", rowData.get(idx) );
			}

			idx = getDataIndex( query, columnMap, "createddate" );
			if ( idx != -1 ){
				itemData.setData( "createddate", rowData.get(idx) );
			}

			idx = getDataIndex( query, columnMap, "updateddate" );
			if ( idx != -1 ){
				itemData.setData( "updateddate", rowData.get(idx) );
			}

			idx = getDataIndex( query, columnMap, "publisheddate" );
			if ( idx != -1 ){
				itemData.setData( "publisheddate", rowData.get(idx) );
			}

			idx = getDataIndex( query, columnMap, "issueddate" );
			if ( idx != -1 ){
				itemData.setData( "issueddate", rowData.get(idx) );
			}

			idx = getDataIndex( query, columnMap, "modifieddate" );
			if ( idx != -1 ){
				itemData.setData( "modifieddate", rowData.get(idx) );
			}

			idx = getDataIndex( query, columnMap, "rights" );
			if ( idx != -1 ){
				itemData.setData( "rights", rowData.get(idx) );
			}

			idx = getDataIndex( query, columnMap, "xmlbase" );
			if ( idx != -1 ){
				itemData.setData( "xmlbase", rowData.get(idx) );
			}

			idx = getDataIndex( query, columnMap, "title" );
			if ( idx != -1 ){
				itemData.setData( "title", rowData.get(idx) );
			}

			/* Contents */
			idx = getDataIndex( query, columnMap, "content" );
			if ( idx != -1 ){
				cfStructData	sTmp = new cfStructData();
				sTmp.setData("value", rowData.get(idx) );

				idx = getDataIndex( query, columnMap, "contentmode" );
				if ( idx != -1 )
					sTmp.setData("mode", rowData.get(idx) );	

				idx = getDataIndex( query, columnMap, "contentsrc" );
				if ( idx != -1 )
					sTmp.setData("src", rowData.get(idx) );	

				idx = getDataIndex( query, columnMap, "contenttype" );
				if ( idx != -1 )
					sTmp.setData("type", rowData.get(idx) );	

				cfArrayData sArr = cfArrayData.createArray(1);
				sArr.addElement( sTmp );
				itemData.setData( "contents", sArr );
			}
			
			/* Summary */
			idx = getDataIndex( query, columnMap, "summary" );
			if ( idx != -1 ){
				cfStructData	sTmp = new cfStructData();
				sTmp.setData("value", rowData.get(idx) );

				idx = getDataIndex( query, columnMap, "summarymode" );
				if ( idx != -1 )
					sTmp.setData("mode", rowData.get(idx) );	

				idx = getDataIndex( query, columnMap, "summarysrc" );
				if ( idx != -1 )
					sTmp.setData("src", rowData.get(idx) );	

				idx = getDataIndex( query, columnMap, "summarytype" );
				if ( idx != -1 )
					sTmp.setData("type", rowData.get(idx) );	

				itemData.setData( "summary", sTmp );
			}
			
			/* Links */
			idx = getDataIndex( query, columnMap, "linkhref" );
			if ( idx != -1 ){
				cfStructData	sTmp = new cfStructData();
				sTmp.setData("href", rowData.get(idx) );

				idx = getDataIndex( query, columnMap, "linkhreflang" );
				if ( idx != -1 )
					sTmp.setData("hreflang", rowData.get(idx) );	

				idx = getDataIndex( query, columnMap, "linklength" );
				if ( idx != -1 )
					sTmp.setData("length", rowData.get(idx) );	

				idx = getDataIndex( query, columnMap, "linkrel" );
				if ( idx != -1 )
					sTmp.setData("rel", rowData.get(idx) );	

				idx = getDataIndex( query, columnMap, "linktitle" );
				if ( idx != -1 )
					sTmp.setData("title", rowData.get(idx) );	

				idx = getDataIndex( query, columnMap, "linktype" );
				if ( idx != -1 )
					sTmp.setData("type", rowData.get(idx) );	

				cfArrayData sArr = cfArrayData.createArray(1);
				sArr.addElement( sTmp );
				itemData.setData( "alternativelinks", sArr );
			}

			/* Contributors */
			idx = getDataIndex( query, columnMap, "contributorname" );
			if ( idx != -1 ){
				cfStructData	sTmp = new cfStructData();
				sTmp.setData("name", rowData.get(idx) );

				idx = getDataIndex( query, columnMap, "contributoremail" );
				if ( idx != -1 )
					sTmp.setData("email", rowData.get(idx) );	

				idx = getDataIndex( query, columnMap, "contributoruri" );
				if ( idx != -1 )
					sTmp.setData("uri", rowData.get(idx) );	

				cfArrayData sArr = cfArrayData.createArray(1);
				sArr.addElement( sTmp );
				itemData.setData( "contributors", sArr );
			}
			
			/* Authors */
			idx = getDataIndex( query, columnMap, "authorname" );
			if ( idx != -1 ){
				cfStructData	sTmp = new cfStructData();
				sTmp.setData("name", rowData.get(idx) );

				idx = getDataIndex( query, columnMap, "authoremail" );
				if ( idx != -1 )
					sTmp.setData("email", rowData.get(idx) );	

				idx = getDataIndex( query, columnMap, "authoruri" );
				if ( idx != -1 )
					sTmp.setData("uri", rowData.get(idx) );	

				cfArrayData sArr = cfArrayData.createArray(1);
				sArr.addElement( sTmp );
				itemData.setData( "authors", sArr );
			}
			
			/* Categories */
			idx = getDataIndex( query, columnMap, "categorylabel" );
			if ( idx != -1 ){
				cfStructData	sTmp = new cfStructData();
				sTmp.setData("label", rowData.get(idx) );

				idx = getDataIndex( query, columnMap, "categoryscheme" );
				if ( idx != -1 )
					sTmp.setData("scheme", rowData.get(idx) );	

				idx = getDataIndex( query, columnMap, "categoryterm" );
				if ( idx != -1 )
					sTmp.setData("term", rowData.get(idx) );	

				cfArrayData sArr = cfArrayData.createArray(1);
				sArr.addElement( sTmp );
				itemData.setData( "categories", sArr );
			}

			itemArray.addElement( itemData );
		}
		
		return structData;
	}
	
	
	/*
	 *	Runs through the query and converts it into a structure
	 */
	private cfStructData convertQueryToStructRss(cfTag tag, cfQueryResultData query, cfStructData columnMap ) throws cfmRunTimeException {
		cfStructData	structData	= new cfStructData();
		cfArrayData	itemArray			= cfArrayData.createArray(1);
		structData.setData( "item", itemArray );
		int idx = 0;

		for ( int rows=0; rows < query.getSize(); rows++ ){
			List<cfData> rowData	= query.getRow( rows );
			
			cfStructData	itemData	= new cfStructData();
	
			idx = getDataIndex( query, columnMap, "title" );
			if ( idx != -1 ){
				itemData.setData( "title", rowData.get(idx) );
			}
			
			idx = getDataIndex( query, columnMap, "link" );
			if ( idx != -1 ){
				itemData.setData( "link", rowData.get(idx) );
			}
			
			idx = getDataIndex( query, columnMap, "comments" );
			if ( idx != -1 ){
				itemData.setData( "comments", rowData.get(idx) );
			}
			
			idx = getDataIndex( query, columnMap, "author" );
			if ( idx != -1 ){
				itemData.setData( "author", rowData.get(idx) );
			}
			
			idx = getDataIndex( query, columnMap, "publisheddate" );
			if ( idx != -1 ){
				itemData.setData( "publisheddate", rowData.get(idx) );
			}
			
			idx = getDataIndex( query, columnMap, "expirationdate" );
			if ( idx != -1 ){
				itemData.setData( "expirationdate", rowData.get(idx) );
			}
			
			/* Setup the content */
			idx = getDataIndex( query, columnMap, "content" );
			if ( idx != -1 ){
				cfStructData sTmp = new cfStructData();
				sTmp.setData( "value", rowData.get(idx) );
				
				idx = getDataIndex( query, columnMap, "contenttype" );
				if ( idx != -1 ){
					sTmp.setData( "type", rowData.get(idx) );
				}

				itemData.setData( "description", sTmp );
			}
			
			/* Setup the category */
			idx = getDataIndex( query, columnMap, "categorylabel" );
			if ( idx != -1 ){
				cfArrayData	catArray = cfArrayData.createArray(1);
				
				String[] catLabels = rowData.get(idx).getString().split(",");
				String[] catScheme = new String[0];
				
				idx = getDataIndex( query, columnMap, "categoryscheme" );
				if ( idx != -1 )
					catScheme = rowData.get(idx).getString().split(",");
				
				for ( int x=0; x < catLabels.length; x++ ){
					cfStructData sTmp = new cfStructData();
					
					catLabels[x] = catLabels[x].trim();
					if ( catLabels[x].length() == 0 )
						continue;
					
					sTmp.setData( "value", new cfStringData(catLabels[x]) );
					if ( x < catScheme.length )
						sTmp.setData( "domain", new cfStringData(catScheme[x].trim()) );

					catArray.addElement( sTmp );
				}

				itemData.setData( "category", catArray );
			}
			
			
			/* GUID */
			idx = getDataIndex( query, columnMap, "id" );
			if ( idx != -1 ){
				cfStructData sTmp = new cfStructData();
				sTmp.setData("value", rowData.get(idx) );

				idx = getDataIndex( query, columnMap, "idpermalink" );
				if ( idx != -1 )
					sTmp.setData("ispermalink", rowData.get(idx) );
				
				itemData.setData( "guid", sTmp );
			}

			
			/* Source */
			idx = getDataIndex( query, columnMap, "source" );
			if ( idx != -1 ){
				cfStructData sTmp = new cfStructData();
				sTmp.setData("value", rowData.get(idx) );

				idx = getDataIndex( query, columnMap, "sourceurl" );
				if ( idx != -1 )
					sTmp.setData("url", rowData.get(idx) );
				
				itemData.setData( "source", sTmp );
			}
			
			
			/* Enclosure */
			idx = getDataIndex( query, columnMap, "linkhref" );
			if ( idx != -1 ){
				cfArrayData	encArray = cfArrayData.createArray(1);
				
				String[]	hrefs = rowData.get(idx).getString().split(",");
				String[]	lengths = new String[0], types = new String[0];
				
				idx = getDataIndex( query, columnMap, "linklength" );
				if ( idx != -1 ) lengths = rowData.get(idx).getString().split(",");

				idx = getDataIndex( query, columnMap, "linktype" );
				if ( idx != -1 ) types = rowData.get(idx).getString().split(",");
				
				for ( int x=0; x < hrefs.length; x++ ){
					cfStructData sTmp = new cfStructData();
					
					hrefs[x] = hrefs[x].trim();
					if ( hrefs[x].length() == 0 )	continue;

					sTmp.setData("url", new cfStringData(hrefs[x]) );
					if ( x < lengths.length ) sTmp.setData("length", new cfStringData(lengths[x]) );
					if ( x < types.length ) sTmp.setData("type", new cfStringData(types[x]) );
					
					encArray.addElement( sTmp );
				}
				
				itemData.setData( "enclosure", encArray );
			}
			
			
			itemArray.addElement( itemData );
		}
		
		
		return structData;
	}
	
	private int getDataIndex( cfQueryResultData query, cfStructData columnMap, String key ) throws cfmRunTimeException {
		/*
		 * Retrieves the necessary column index for the query, based on if they have the columnMap defined
		 */
		if ( columnMap == null ){
			return query.getColumnIndexCF( key ) - 1;
		}else if ( columnMap.containsKey(key) ){
			return query.getColumnIndexCF( columnMap.getData(key).getString() ) - 1;
		}else
			return -1;
	}
	
	/* 
	 * Setup the RSS entries details ------------------------------------------------------ 
	 */
	private void renderEntriesToAtom(cfTag tag, cfStructData cffeeddata ) throws cfmRunTimeException {
		if ( !cffeeddata.containsKey("item") || cffeeddata.getData("item").getDataType() != cfData.CFARRAYDATA ) return;
	
		List itemsList = new ArrayList();
		cfArrayListData items =	(cfArrayListData)cffeeddata.getData("item");
		Iterator itemIterator = items.iterator();
		
		while ( itemIterator.hasNext() ){
			cfData cfDataItem = (cfData)itemIterator.next();
			if ( cfDataItem.getDataType() != cfData.CFSTRUCTDATA )
				continue;
			
			cfStructData	sData = (cfStructData)cfDataItem;

			com.sun.syndication.feed.atom.Entry entry = new com.sun.syndication.feed.atom.Entry();
			
			/* Set Alternative Links */
			if ( sData.containsKey("alternativelinks") && sData.getData("alternativelinks").getDataType() == cfData.CFARRAYDATA )
				entry.setAlternateLinks( getAtomLinks( (cfArrayListData)cffeeddata.getData("alternativelinks") ) );

			/* Set Authors Links */
			if ( sData.containsKey("authors") && sData.getData("authors").getDataType() == cfData.CFARRAYDATA )
				entry.setAuthors( getAtomPersons( (cfArrayListData)cffeeddata.getData("authors") ) );

			/* Set Categories */
			if ( sData.containsKey("categories") && sData.getData("categories").getDataType() == cfData.CFARRAYDATA )
				entry.setCategories( getAtomCategory( (cfArrayListData)cffeeddata.getData("categories") ) );

			/* Set Contributors Links */
			if ( sData.containsKey("contributors") && sData.getData("contributors").getDataType() == cfData.CFARRAYDATA )
				entry.setContributors( getAtomPersons( (cfArrayListData)cffeeddata.getData("contributors") ) );

			if ( sData.containsKey("createddate") )
				entry.setCreated( new java.util.Date( sData.getData("createddate").getDateLong() ) );

			if ( sData.containsKey("id") )
				entry.setId( sData.getData("id").getString() );
			
			if ( sData.containsKey("issueddate") )
				entry.setIssued( new java.util.Date( sData.getData("issueddate").getDateLong() ) );
			
			if ( sData.containsKey("modifieddate") )
				entry.setModified( new java.util.Date( sData.getData("modifieddate").getDateLong() ) );
			
			/* Set Other Links */
			if ( sData.containsKey("otherlinks") && sData.getData("otherlinks").getDataType() == cfData.CFARRAYDATA )
				entry.setOtherLinks( getAtomLinks( (cfArrayListData)cffeeddata.getData("otherlinks") ) );
			
			if ( sData.containsKey("rights") )
				entry.setRights( sData.getData("rights").getString() );
			
			if ( sData.containsKey("publisheddate") )
				entry.setPublished( new java.util.Date( sData.getData("publisheddate").getDateLong() ) );
			
			if ( sData.containsKey("title") )
				entry.setTitle( sData.getData("title").getString() );
			
			if ( sData.containsKey("titleex")  && sData.getData("titleex").getDataType() == cfData.CFSTRUCTDATA )
				entry.setTitleEx( getAtomContent( (cfStructData)sData.getData("titleex") ) );
			
			if ( sData.containsKey("summary")  && sData.getData("summary").getDataType() == cfData.CFSTRUCTDATA )
				entry.setSummary( getAtomContent( (cfStructData)sData.getData("summary") ) );
			
			if ( sData.containsKey("updateddate") )
				entry.setUpdated( new java.util.Date( sData.getData("updateddate").getDateLong() ) );
			
			if ( sData.containsKey("xmlbase") )
				entry.setXmlBase( sData.getData("xmlbase").getString() );

			/* Set the contents */
			if ( sData.containsKey("contents") && sData.getData("contents").getDataType() == cfData.CFARRAYDATA ){
				cfArrayListData aTmp = (cfArrayListData)sData.getData("contents");
				Iterator encIterator = aTmp.iterator();
				List contentList	= new ArrayList( aTmp.size() );
				
				while ( encIterator.hasNext() ){
					cfData encData = (cfData)encIterator.next();
					if ( encData.getDataType() != cfData.CFSTRUCTDATA )
						continue;
					
					contentList.add( getAtomContent( (cfStructData)encData ) );
				}
				
				entry.setContents( contentList );
			}
			
			
			itemsList.add( entry );
		}
		
		activeATOM.setEntries( itemsList );
	}
	
	
	
	/* 
	 * Setup the RSS entries details ------------------------------------------------------ 
	 */
	private void renderEntriesToRss(cfTag tag, cfStructData cffeeddata ) throws cfmRunTimeException {
		if ( !cffeeddata.containsKey("item") || cffeeddata.getData("item").getDataType() != cfData.CFARRAYDATA ) return;
	
		List itemsList = new ArrayList();
		cfArrayListData items =	(cfArrayListData)cffeeddata.getData("item");
		Iterator itemIterator = items.iterator();
		
		while ( itemIterator.hasNext() ){
			cfData cfDataItem = (cfData)itemIterator.next();
			if ( cfDataItem.getDataType() != cfData.CFSTRUCTDATA )
				continue;
			
			cfStructData	sData = (cfStructData)cfDataItem;

			com.sun.syndication.feed.rss.Item item	= new com.sun.syndication.feed.rss.Item();
			
			if ( sData.containsKey("title") )
				item.setTitle( sData.getData("title").getString() );

			if ( sData.containsKey("link") )
				item.setLink( sData.getData("link").getString() );
			
			if ( sData.containsKey("comments") )
				item.setComments( sData.getData("comments").getString() );
			
			if ( sData.containsKey("author") )
				item.setAuthor( sData.getData("author").getString() );

			if ( sData.containsKey("publisheddate") )
				item.setPubDate( new java.util.Date( sData.getData("publisheddate").getDateLong() ) );
			else if ( sData.containsKey("pubdate") )
				item.setPubDate( new java.util.Date( sData.getData("pubdate").getDateLong() ) );
			
			if ( sData.containsKey("expirationdate") )
				item.setExpirationDate( new java.util.Date( sData.getData("expirationdate").getDateLong() ) );
			
			if ( sData.containsKey("category") && sData.getData("category").getDataType() == cfData.CFARRAYDATA )
				item.setCategories( getRssCategories( (cfArrayListData)sData.getData("category") ) );
		
			
			/* Description */
			if ( sData.containsKey("description") && sData.getData("description").getDataType() == cfData.CFSTRUCTDATA ){
				cfStructData sTmp = (cfStructData)sData.getData("description");

				com.sun.syndication.feed.rss.Description desc = new com.sun.syndication.feed.rss.Description();
				
				if ( sTmp.containsKey("type") )
					desc.setType( sTmp.getData("type").getString() );

				if ( sTmp.containsKey("value") )
					desc.setValue( sTmp.getData("value").getString() );

				item.setDescription( desc );
			}
			
			
			/* Content */
			if ( sData.containsKey("content") && sData.getData("content").getDataType() == cfData.CFSTRUCTDATA ){
				cfStructData sTmp = (cfStructData)sData.getData("content");

				com.sun.syndication.feed.rss.Content content = new com.sun.syndication.feed.rss.Content();
				
				if ( sTmp.containsKey("type") )
					content.setType( sTmp.getData("type").getString() );

				if ( sTmp.containsKey("value") )
					content.setValue( sTmp.getData("value").getString() );

				item.setContent( content );
			}
			
			
			/* GUID */
			if ( sData.containsKey("guid") && sData.getData("guid").getDataType() == cfData.CFSTRUCTDATA ){
				cfStructData sTmp = (cfStructData)sData.getData("guid");
				Guid guid = new Guid();
				
				if ( sTmp.containsKey("value") )
					guid.setValue( sTmp.getData("value").getString() );
				
				if ( sTmp.containsKey("ispermalink") )
					guid.setPermaLink( sTmp.getData("ispermalink").getBoolean() );

				item.setGuid( guid );
			}

			
			/* Source */
			if ( sData.containsKey("source") && sData.getData("source").getDataType() == cfData.CFSTRUCTDATA ){
				cfStructData sTmp = (cfStructData)sData.getData("source");
				com.sun.syndication.feed.rss.Source src = new com.sun.syndication.feed.rss.Source();
				
				if ( sTmp.containsKey("url") )
					src.setUrl( sTmp.getData("url").getString() );
				
				if ( sTmp.containsKey("value") )
					src.setValue( sTmp.getData("value").getString() );

				item.setSource( src );
			}


			/* Enclosure */
			if ( sData.containsKey("enclosure") && sData.getData("enclosure").getDataType() == cfData.CFARRAYDATA ){
				cfArrayListData aTmp = (cfArrayListData)sData.getData("enclosure");
				Iterator encIterator = aTmp.iterator();
				
				List enclosures	= new ArrayList( aTmp.size() );
				
				while ( encIterator.hasNext() ){
					cfData encData = (cfData)encIterator.next();
					if ( encData.getDataType() != cfData.CFSTRUCTDATA )
						continue;
					
					cfStructData encStruct = (cfStructData)encData;

					com.sun.syndication.feed.rss.Enclosure enclosure = new com.sun.syndication.feed.rss.Enclosure();
					
					if ( encStruct.containsKey("url") )
						enclosure.setUrl( encStruct.getData("url").getString() );
					
					if ( encStruct.containsKey("type") )
						enclosure.setType( encStruct.getData("type").getString() );

					if ( encStruct.containsKey("length") )
						enclosure.setLength( encStruct.getData("length").getLong() );
					
					enclosures.add( enclosure );
				}
				
				item.setEnclosures( enclosures );
			}

			itemsList.add( item );
		}
		
		activeRSS.setItems( itemsList );
	}
	
	
	
	/* 
	 * Setup the RSS header details ------------------------------------------------------ 
	 */
	private void renderMetaDataToRss(cfTag tag, cfStructData cffeeddata ) throws cfmRunTimeException {
		activeRSS.setTitle( cffeeddata.getData("title").getString() );
		activeRSS.setDescription( cffeeddata.getData("description").getString() );
		activeRSS.setLink( cffeeddata.getData("link").getString() );
		
		if ( cffeeddata.containsKey("pubdate") )
			activeRSS.setPubDate( new java.util.Date( cffeeddata.getData("pubdate").getDateLong() ) );
		
		if ( cffeeddata.containsKey("copyright") )
			activeRSS.setCopyright( cffeeddata.getData("copyright").getString() );

		if ( cffeeddata.containsKey("language") )
			activeRSS.setLanguage( cffeeddata.getData("language").getString() );

		if ( cffeeddata.containsKey("webmaster") )
			activeRSS.setWebMaster( cffeeddata.getData("webmaster").getString() );

		if ( cffeeddata.containsKey("managingeditor" ) )
			activeRSS.setManagingEditor( cffeeddata.getData("managingeditor").getString() );

		if ( cffeeddata.containsKey("rating") )
			activeRSS.setRating( cffeeddata.getData("rating").getString() );

		if ( cffeeddata.containsKey("docs") )
			activeRSS.setDocs( cffeeddata.getData("docs").getString() );

		if ( cffeeddata.containsKey("lastbuilddate") )
			activeRSS.setLastBuildDate( new java.util.Date( cffeeddata.getData("lastbuilddate").getDateLong() ) );

		if ( cffeeddata.containsKey("ttl") )
			activeRSS.setTtl( cffeeddata.getData("ttl").getInt() );

		if ( cffeeddata.containsKey("category") && cffeeddata.getData("category").getDataType() == cfData.CFARRAYDATA )
			activeRSS.setCategories( getRssCategories( (cfArrayListData)cffeeddata.getData("category") ) );
		
		if ( cffeeddata.containsKey("image") && cffeeddata.getData("image").getDataType() == cfData.CFSTRUCTDATA )
			activeRSS.setImage( getRssImage( (cfStructData)cffeeddata.getData("image") ) );
		
		if ( cffeeddata.containsKey("cloud") && cffeeddata.getData("cloud").getDataType() == cfData.CFSTRUCTDATA ){
			cfStructData tmpS = (cfStructData)cffeeddata.getData("cloud");
			
			Cloud cloud	= new Cloud();
			
			if ( tmpS.containsKey("domain") )
				cloud.setDomain( tmpS.getData("domain").getString() );
			
			if ( tmpS.containsKey("path") )
				cloud.setPath( tmpS.getData("path").getString() );
	
			if ( tmpS.containsKey("port") )
				cloud.setPort( tmpS.getData("port").getInt() );
			
			if ( tmpS.containsKey("protocol") )
				cloud.setProtocol( tmpS.getData("protocol").getString() );
			
			if ( tmpS.containsKey("registerprocedure") )
				cloud.setRegisterProcedure( tmpS.getData("registerprocedure").getString() );
	
			activeRSS.setCloud( cloud );
		}
		
		if ( cffeeddata.containsKey("textinput") && cffeeddata.getData("textinput").getDataType() == cfData.CFSTRUCTDATA ){
			cfStructData tmpS = (cfStructData)cffeeddata.getData("textinput");
			
			TextInput textinput	= new TextInput();
			
			if ( tmpS.containsKey("description") )
				textinput.setDescription( tmpS.getData("description").getString() );
			
			if ( tmpS.containsKey("link") )
				textinput.setLink( tmpS.getData("link").getString() );
	
			if ( tmpS.containsKey("name") )
				textinput.setName( tmpS.getData("name").getString() );
			
			if ( tmpS.containsKey("title") )
				textinput.setTitle( tmpS.getData("title").getString() );
			
			activeRSS.setTextInput( textinput );
		}

		if ( cffeeddata.containsKey("skiphours") ) {
			String hourList[] = cffeeddata.getData("skiphours").getString().split(",");
			
			List skipHourList = new ArrayList();
			for ( int x=0; x < hourList.length; x++ ){
				try{
					skipHourList.add( Integer.valueOf( hourList[x].trim() ) );
				}catch(Exception ignore){}
			}

			activeRSS.setSkipHours( skipHourList );
		}
		
		if ( cffeeddata.containsKey("skipdays") ) {
			String dayList[] = cffeeddata.getData("skipdays").getString().split(",");
			
			List skipDayList = new ArrayList();
			for ( int x=0; x < dayList.length; x++ ){
				try{
					skipDayList.add( dayList[x].trim() );
				}catch(Exception ignore){}
			}

			activeRSS.setSkipDays( skipDayList );
		}
	}
	
	private List getRssCategories(cfArrayListData cats) throws cfmRunTimeException {
		Iterator catIterator = cats.iterator();
		
		List categories	= new ArrayList( cats.size() );
		
		while ( catIterator.hasNext() ){
			cfData catData = (cfData)catIterator.next();
			if ( catData.getDataType() != cfData.CFSTRUCTDATA )
				continue;
			
			cfStructData catStruct = (cfStructData)catData;
			
			com.sun.syndication.feed.rss.Category category = new com.sun.syndication.feed.rss.Category();
			
			if ( catStruct.containsKey("domain") )
				category.setDomain( catStruct.getData("domain").getString() );

			if ( catStruct.containsKey("value") )
				category.setValue( catStruct.getData("value").getString() );

			categories.add( category );
		}
		return categories;
	}
	
	
	private com.sun.syndication.feed.rss.Image getRssImage( cfStructData tmpS ) throws cfmRunTimeException {
		com.sun.syndication.feed.rss.Image image = new com.sun.syndication.feed.rss.Image();
		
		if ( tmpS.containsKey("description") )
			image.setDescription( tmpS.getData("description").getString() );

		if ( tmpS.containsKey("height") )
			image.setHeight( tmpS.getData("height").getInt() );

		if ( tmpS.containsKey("width") )
			image.setWidth( tmpS.getData("width").getInt() );

		if ( tmpS.containsKey("link") )
			image.setLink( tmpS.getData("link").getString() );

		if ( tmpS.containsKey("url") )
			image.setUrl( tmpS.getData("url").getString() );

		if ( tmpS.containsKey("title") )
			image.setTitle( tmpS.getData("title").getString() );

		return image;
	}


	/* 
	 * Setup the ATOM header details ------------------------------------------------------ 
	 */
	private void renderMetaDataToAtom(cfTag tag, cfStructData cffeeddata ) throws cfmRunTimeException {

		/* Do the simple one-2-one mappings */
		if ( cffeeddata.containsKey("copyright") )
			activeATOM.setCopyright( cffeeddata.getData("copyright").getString() );

		if ( cffeeddata.containsKey("encoding") )
			activeATOM.setEncoding( cffeeddata.getData("encoding").getString() );

		if ( cffeeddata.containsKey("icon") )
			activeATOM.setIcon( cffeeddata.getData("icon").getString() );

		if ( cffeeddata.containsKey("id") )
			activeATOM.setId( cffeeddata.getData("id").getString() );

		if ( cffeeddata.containsKey("language") )
			activeATOM.setLanguage( cffeeddata.getData("language").getString() );

		if ( cffeeddata.containsKey("logo") )
			activeATOM.setLogo( cffeeddata.getData("logo").getString() );

		if ( cffeeddata.containsKey("modified") )
			activeATOM.setModified( new java.util.Date( cffeeddata.getData("modified").getDateLong() ) );

		if ( cffeeddata.containsKey("rights") )
			activeATOM.setRights( cffeeddata.getData("rights").getString() );

		if ( cffeeddata.containsKey("title") )
			activeATOM.setTitle( cffeeddata.getData("title").getString() );

		if ( cffeeddata.containsKey("updated") )
			activeATOM.setUpdated( new java.util.Date( cffeeddata.getData("updated").getDateLong() ) );

		if ( cffeeddata.containsKey("xmlbase") )
			activeATOM.setXmlBase( cffeeddata.getData("xmlbase").getString() );

		/* Set Alternative Links */
		if ( cffeeddata.containsKey("alternativelinks") && cffeeddata.getData("alternativelinks").getDataType() == cfData.CFARRAYDATA )
			activeATOM.setAlternateLinks( getAtomLinks( (cfArrayListData)cffeeddata.getData("alternativelinks") ) );

		/* Set Other Links */
		if ( cffeeddata.containsKey("otherlinks") && cffeeddata.getData("otherlinks").getDataType() == cfData.CFARRAYDATA )
			activeATOM.setOtherLinks( getAtomLinks( (cfArrayListData)cffeeddata.getData("otherlinks") ) );

		/* Set Author Links */
		if ( cffeeddata.containsKey("authors") && cffeeddata.getData("authors").getDataType() == cfData.CFARRAYDATA )
			activeATOM.setAuthors( getAtomPersons( (cfArrayListData)cffeeddata.getData("authors") ) );

		/* Set Contributors Links */
		if ( cffeeddata.containsKey("contributors") && cffeeddata.getData("contributors").getDataType() == cfData.CFARRAYDATA )
			activeATOM.setContributors( getAtomPersons( (cfArrayListData)cffeeddata.getData("contributors") ) );

		/* Set Categories Links */
		if ( cffeeddata.containsKey("categories") && cffeeddata.getData("categories").getDataType() == cfData.CFARRAYDATA )
			activeATOM.setCategories( getAtomCategory( (cfArrayListData)cffeeddata.getData("categories") ) );

		/* Set info */
		if ( cffeeddata.containsKey("info") && cffeeddata.getData("info").getDataType() == cfData.CFSTRUCTDATA )
			activeATOM.setInfo( getAtomContent( (cfStructData)cffeeddata.getData("info") ) );
		
		/* Set subtitle */
		if ( cffeeddata.containsKey("subtitle") && cffeeddata.getData("subtitle").getDataType() == cfData.CFSTRUCTDATA )
			activeATOM.setSubtitle( getAtomContent( (cfStructData)cffeeddata.getData("subtitle") ) );
		
		/* Set tagline */
		if ( cffeeddata.containsKey("tagline") && cffeeddata.getData("tagline").getDataType() == cfData.CFSTRUCTDATA )
			activeATOM.setTagline( getAtomContent( (cfStructData)cffeeddata.getData("tagline") ) );
		
		/* Set titleex */
		if ( cffeeddata.containsKey("titleex") && cffeeddata.getData("titleex").getDataType() == cfData.CFSTRUCTDATA )
			activeATOM.setTitleEx( getAtomContent( (cfStructData)cffeeddata.getData("titleex") ) );
		
		/* Set Generator */
		if ( cffeeddata.containsKey("generator") && cffeeddata.getData("generator").getDataType() == cfData.CFSTRUCTDATA ){
			cfStructData tmpS = (cfStructData)cffeeddata.getData("generator");
			com.sun.syndication.feed.atom.Generator gen	= new com.sun.syndication.feed.atom.Generator();
			
			if ( tmpS.containsKey("version") )
				gen.setVersion( tmpS.getData("version").getString() );
			
			if ( tmpS.containsKey("value") )
				gen.setValue( tmpS.getData("value").getString() );
	
			if ( tmpS.containsKey("url") )
				gen.setUrl( tmpS.getData("url").getString() );
	
			activeATOM.setGenerator( gen );
		}
	}
	
	private com.sun.syndication.feed.atom.Content getAtomContent(cfStructData tmpStruct) throws cfmRunTimeException {
		com.sun.syndication.feed.atom.Content content = new com.sun.syndication.feed.atom.Content();
		
		if ( tmpStruct.containsKey("mode") )
			content.setMode( tmpStruct.getData("mode").getString() );

		if ( tmpStruct.containsKey("src") )
			content.setSrc( tmpStruct.getData("src").getString() );

		if ( tmpStruct.containsKey("type") )
			content.setType( tmpStruct.getData("type").getString() );

		if ( tmpStruct.containsKey("value") )
			content.setValue( tmpStruct.getData("value").getString() );

		return content;
	}
	
	private List getAtomCategory(cfArrayListData links) throws cfmRunTimeException {
		Iterator iIterator = links.iterator();
		
		List linkslist	= new ArrayList( links.size() );
		
		while ( iIterator.hasNext() ){
			cfData cData = (cfData)iIterator.next();
			if ( cData.getDataType() != cfData.CFSTRUCTDATA )
				continue;
			
			cfStructData tmpStruct = (cfStructData)cData;
			
			com.sun.syndication.feed.atom.Category cat = new com.sun.syndication.feed.atom.Category();
			
			if ( tmpStruct.containsKey("label") )
				cat.setLabel( tmpStruct.getData("label").getString() );

			if ( tmpStruct.containsKey("scheme") )
				cat.setScheme( tmpStruct.getData("scheme").getString() );

			if ( tmpStruct.containsKey("schemeresolved") )
				cat.setSchemeResolved( tmpStruct.getData("schemeresolved").getString() );

			if ( tmpStruct.containsKey("term") )
				cat.setTerm( tmpStruct.getData("term").getString() );
			
			linkslist.add( cat );
		}
		
		return linkslist;
	}
	
	private List getAtomPersons(cfArrayListData links) throws cfmRunTimeException {
		Iterator iIterator = links.iterator();
		
		List linkslist	= new ArrayList( links.size() );
		
		while ( iIterator.hasNext() ){
			cfData cData = (cfData)iIterator.next();
			if ( cData.getDataType() != cfData.CFSTRUCTDATA )
				continue;
			
			cfStructData tmpStruct = (cfStructData)cData;
			
			com.sun.syndication.feed.atom.Person person = new com.sun.syndication.feed.atom.Person();
			
			if ( tmpStruct.containsKey("email") )
				person.setEmail( tmpStruct.getData("email").getString() );

			if ( tmpStruct.containsKey("name") )
				person.setName( tmpStruct.getData("name").getString() );

			if ( tmpStruct.containsKey("url") )
				person.setUrl( tmpStruct.getData("url").getString() );
			
			if ( tmpStruct.containsKey("uri") )
				person.setUri( tmpStruct.getData("uri").getString() );
			
			if ( tmpStruct.containsKey("uriresolved") )
				person.setUriResolved( tmpStruct.getData("uriresolved").getString() );
			
			linkslist.add( person );
		}
		
		return linkslist;
	}

	private List getAtomLinks(cfArrayListData links) throws cfmRunTimeException {
		Iterator iIterator = links.iterator();
		
		List linkslist	= new ArrayList( links.size() );
		
		while ( iIterator.hasNext() ){
			cfData cData = (cfData)iIterator.next();
			if ( cData.getDataType() != cfData.CFSTRUCTDATA )
				continue;
			
			cfStructData tmpStruct = (cfStructData)cData;
			
			com.sun.syndication.feed.atom.Link link = new com.sun.syndication.feed.atom.Link();
			
			if ( tmpStruct.containsKey("href") )
				link.setHref( tmpStruct.getData("href").getString() );

			if ( tmpStruct.containsKey("hreflang") )
				link.setHreflang( tmpStruct.getData("hreflang").getString() );

			if ( tmpStruct.containsKey("length") )
				link.setLength( tmpStruct.getData("length").getLong() );
			
			if ( tmpStruct.containsKey("rel") )
				link.setRel( tmpStruct.getData("rel").getString() );

			if ( tmpStruct.containsKey("title") )
				link.setTitle( tmpStruct.getData("title").getString() );

			if ( tmpStruct.containsKey("type") )
				link.setType( tmpStruct.getData("type").getString() );

			linkslist.add( link );
		}
		
		return linkslist;
	}
}