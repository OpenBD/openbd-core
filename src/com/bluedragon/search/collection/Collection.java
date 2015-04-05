/* 
 *  Copyright (C) 2000 - 2013 TagServlet Ltd
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
 *  
 *  $Id: Collection.java 2523 2015-02-22 16:23:11Z alan $
 */

package com.bluedragon.search.collection;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.FileSystems;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Constants;

import com.bluedragon.search.AnalyzerFactory;
import com.bluedragon.search.DocumentWrap;
import com.bluedragon.search.index.DocumentWriter;
import com.nary.io.FileUtils;


public class Collection extends Object {

	private	String 	name;
	private	long		created;
	private boolean	bStoreBody;
	private String	language, collectionpath;
	
	// Lucene specific variables
	private Directory	directory = null;
	private IndexSearcher	indexsearcher = null;
	private IndexWriter	indexwriter = null;
	private int	totalDocs = -1;
	private DocumentWriter documentwriter = null;
	private long lastUsed	= System.currentTimeMillis();
	
	public String toString(){
		return "[Collection name=" + name + "; path=" + collectionpath + "]";
	}
	
	public long getTimeSinceLastUsed(){
		return System.currentTimeMillis() - lastUsed;
	}
	
	public void setName(String string) throws Exception {
		this.name	= string;
		
		if ( this.name == null || this.name.length() == 0 )
			throw new Exception("invalid collection name");
	}


	public void setLanguage(String string) {
		this.language	= string;
	}

	public String getLanguage(){
		return this.language;
	}
	
	public void setStoreBody(boolean boolean1) {
		bStoreBody	= boolean1;
	}

	public boolean bStoreBody(){
		return bStoreBody;
	}

	public String getName() {
		return name;
	}
	
	public long getLastModified(){
		return lastUsed;
	}

	public long getCreated(){
		return created;
	}
	
	public String getPath(){
		return collectionpath;
	}
	
	public void setDirectory(String path) throws Exception {
		File	filePath;
		if ( !path.endsWith(name) )
			filePath = new File( path, name );
		else
			filePath = new File( path );

		if ( !filePath.exists() || !filePath.isDirectory() )
			throw new Exception( "invalid collection path: " + path );
		
		collectionpath	= filePath.getCanonicalPath();
	}

	
	public void close(){
		closeReader();
		
		if ( indexwriter != null ){
			try {
				indexwriter.close();
			} catch (IOException e){}
			indexwriter = null;
		}
	}
	
	
	public void closeReader(){
		if ( indexsearcher != null ){
			try {
				indexsearcher.getIndexReader().close();
			} catch ( IOException ignoreThisException ) {}
			indexsearcher = null;
		}
	}
	
	/**
	 * Retrieves the IndexSearcher object.  This object is cached against this collection and is intended
	 * to be used amongst several threads.   This is the recommended approach.
	 * 
	 * If new content is added to this collection then it won't be available for searching until a new
	 * indexsearcher is created
	 * 
	 * @return
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public synchronized IndexSearcher getIndexSearcher() throws CorruptIndexException, IOException{
		lastUsed	= System.currentTimeMillis();
		
		if ( indexsearcher != null )
			return indexsearcher;
		
		setDirectory();
		indexsearcher	= new IndexSearcher( DirectoryReader.open(directory) );
		
		totalDocs			= indexsearcher.getIndexReader().numDocs();
		
		return indexsearcher;
	}
	
	
	public int	getTotalDocs(){
		return totalDocs;
	}
	
	
	public int size(){
		int size = 0;
		try {
			setDirectory();

			String[] files = directory.listAll();
  		for ( int i = 0; i < files.length; i++ ){
				try {
					size += directory.fileLength( files[i] );
				} catch (IOException e) {}
			}
		} catch (IOException e1) {}
		
		return size;
	}
	
	private void setDirectory() throws IOException {
		if ( directory != null )
			return;
		
    if (Constants.WINDOWS) {
    	directory = new SimpleFSDirectory( FileSystems.getDefault().getPath(collectionpath) );
    } else {
    	directory = new NIOFSDirectory( FileSystems.getDefault().getPath(collectionpath) );
    }
    
    File touchFile	= new File( collectionpath, "openbd.created" );
    if ( touchFile.exists() )
    	created = touchFile.lastModified();
    else
    	created = System.currentTimeMillis();
	}

	
	/**
	 * Creates an empty collection to get it up and running
	 */
	public synchronized void create() throws IOException {
		setDirectory();
		
		if ( directory.listAll().length > 2 )
			throw new IOException( "directory not empty; possible collection already present" );

		IndexWriterConfig iwc = new IndexWriterConfig( AnalyzerFactory.get(language) );
		iwc.setOpenMode( OpenMode.CREATE );
		
		indexwriter = new IndexWriter(directory, iwc);
		indexwriter.commit();
		indexwriter.close();
		indexwriter = null;
		
		// throw an openbd.create file in there so we know when it was created
		created	= System.currentTimeMillis();
		File touchFile	= new File( collectionpath, "openbd.created" );
		Writer	fw	= new FileWriter( touchFile );
		fw.close();
	}
	
	
	/**
	 * Deletes this collection, removing all files
	 * @throws IOException 
	 */
	public synchronized void delete() throws IOException {
		close();

		// delete the file
		File f = new File(collectionpath);
    if (f.exists() && f.isDirectory())
      FileUtils.recursiveDelete(f, true);
	}

	public synchronized DocumentWriter getDocumentWriter() throws CorruptIndexException, LockObtainFailedException, IOException {
		if ( documentwriter != null )
			return documentwriter;
		
		documentwriter	= new DocumentWriter( this );
		return documentwriter;
	}

	private void setIndexWriter() throws IOException{
		if ( indexwriter != null )
			return;
		
		setDirectory();
		IndexWriterConfig iwc = new IndexWriterConfig( AnalyzerFactory.get(language) );
		iwc.setOpenMode( OpenMode.CREATE_OR_APPEND );
		indexwriter = new IndexWriter(directory, iwc);
	}
	
	public synchronized void addDocuments(java.util.Collection<Document> pageCollection) throws CorruptIndexException, IOException {
		setIndexWriter();
		indexwriter.addDocuments(pageCollection);
		closeWriter();
		close();
	}

	public synchronized void deleteDocument(DocumentWrap docwrap, boolean closeOptimize ) throws IOException {
		setIndexWriter();
    indexwriter.deleteDocuments( new Term(DocumentWrap.ID, docwrap.getId()) );
    
    if ( closeOptimize ){
    	closeWriter();
    }
	}
	
	
	public synchronized void closeWriter() throws CorruptIndexException, IOException{
		indexwriter.commit();
		indexwriter.close();
		indexwriter	= null;
	}
	
	
	public synchronized void deleteAll() throws IOException {
		setIndexWriter();
		indexwriter.deleteAll();
		closeWriter();
		close();		
	}
}