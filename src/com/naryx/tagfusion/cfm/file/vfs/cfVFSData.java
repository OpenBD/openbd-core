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
 *  $Id: cfVFSData.java 2501 2015-02-04 01:10:04Z alan $
 */

package com.naryx.tagfusion.cfm.file.vfs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.RandomAccessContent;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.auth.StaticUserAuthenticator;
import org.apache.commons.vfs.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs.util.RandomAccessMode;

import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfDateData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;

public class cfVFSData extends cfStructData implements java.io.Serializable{
	static final long serialVersionUID = 1;

	private String 			fileUrl;
	private FileObject	fileObject = null;
	private File				file	= null;

	transient private BufferedReader			bufferedReader = null;
	transient private BufferedInputStream	bufferedInputStream = null;

	transient private BufferedOutputStream	bufferedOutputStream = null;
	transient private OutputStream					outputStream = null;
	transient private RandomAccessContent 	randomAccessContent = null;
	transient private RandomAccessFile 			randomAccessFile = null;
	transient private boolean								bEOF = false;

	private String charset = null;

	public cfVFSData( String fileUrl ) throws Exception {
		super();
		setFileObject( fileUrl );
	}


	public cfVFSData( String fileUrl, String mode, String charset ) throws Exception {
		super();

		/* Manage the mode */
		this.charset = charset;
		mode = mode.toLowerCase().trim();
		if ( !mode.equals("read") && !mode.equals("readbinary") && !mode.equals("write") && !mode.equals("append") )
			throw new Exception("invalid mode use read/readbinary/write/append");
		else
			setData( "mode",	new cfStringData(mode) );

		/* Get a handle to this object */
		setFileObject( fileUrl );
		populateInfo();


		/* Check the modes */
		if ( mode.equals("read") || mode.equals("readbinary") ){

			if ( mode.equals("read") ){
				openReader(charset);
			}else{
				openInputStream();
			}

		}else if ( mode.equals("write") || mode.equals("append") ){

			if ( mode.equals("write") || (mode.equals("append") && filesize() == 0 ) ){
				openOutputStream();
			}else if ( mode.equals("append")){
				openRandomAccessContent();
			}
		}

		setData( "status",	new cfStringData("open") );
	}


	public void openRandomAccessContent() throws IOException{
		if ( fileObject != null ){
			randomAccessContent = fileObject.getContent().getRandomAccessContent( RandomAccessMode.READWRITE );
			randomAccessContent.seek( randomAccessContent.length() );
		}else{
			randomAccessFile	= new RandomAccessFile( file, "rw" );
			randomAccessFile.seek( randomAccessFile.length() );
		}
	}


	public void openOutputStream() throws Exception {
		if ( fileObject != null ){
			if (!fileObject.isWriteable())
				throw new Exception("resource is not writable");

			outputStream	= fileObject.getContent().getOutputStream();
		}else{
			outputStream	= new FileOutputStream(file);
		}

		bufferedOutputStream	= new BufferedOutputStream( outputStream );
	}


	public void openInputStream() throws Exception{
		if ( fileObject != null ){
			if (!fileObject.isReadable())
				throw new Exception("resource is not readable");

			bufferedInputStream = new BufferedInputStream( fileObject.getContent().getInputStream() );
		}else{
			if (!file.canRead())
				throw new Exception("resource is not readable");

			bufferedInputStream = new BufferedInputStream( new FileInputStream( file ) );
		}
	}


	private void openReader( String charset ) throws Exception{
		if ( fileObject != null ){
			if (!fileObject.isReadable())
				throw new Exception("resource is not readable");

			if ( charset != null )
				bufferedReader	= new BufferedReader( new InputStreamReader( fileObject.getContent().getInputStream(), charset ) );
			else
				bufferedReader	= new BufferedReader( new InputStreamReader( fileObject.getContent().getInputStream() ) );
		}else{
			if (!file.canRead())
				throw new Exception("resource is not readable");

			if ( charset != null )
				bufferedReader	= new BufferedReader( new InputStreamReader( new FileInputStream( file ), charset ) );
			else
				bufferedReader	= new BufferedReader( new FileReader( file ) );
		}
	}


	private void setFileObject( String fileUrl ) throws Exception {

		// Check to see if this is a native file
		boolean bFoundScheme = false;
		String[] schemes = VFS.getManager().getSchemes();
		for (int x=0; x < schemes.length; x++ ){
			if ( fileUrl.startsWith( schemes[x] + "://") ){
				bFoundScheme = true;
				break;
			}
		}

		if ( !bFoundScheme ){
			file	= new File( fileUrl );
			return;
		}


		if ( fileUrl.toLowerCase().startsWith("s3://") ){
			/* This is an S3 object, so we pull out the details here */
			int c1 = fileUrl.indexOf("@");
			if ( c1 == -1 )
				throw new Exception("provider S3 authentication 's3://[accesskey]@[secretkey]/[bucket]' or 's3://@[datasource]/[bucket]'");

			int c2 = fileUrl.indexOf("/", c1 + 1 );

			String accessKey 	= fileUrl.substring( 5, c1 );
			String secretKey	= fileUrl.substring( c1+1, c2 );
			String domain			= null;

			if ( accessKey.length() == 0 ){
				//This is an Amazon datasource format
				org.alanwilliamson.amazon.AmazonKey	amazonKey	= org.alanwilliamson.amazon.AmazonKeyFactory.getDS( secretKey );
				if ( amazonKey == null )
					throw new Exception( "The Amazon Datasource [" + secretKey + "] has not been registered; use AmazonRegisterDataSource()" );

				accessKey	= amazonKey.getKey();
				secretKey	= amazonKey.getSecret();
				domain		= amazonKey.getS3Host();
			}else
				domain = "http://s3.amazonaws.com/";

			fileUrl = "s3://" + fileUrl.substring( c2 );

			FileSystemOptions opts = new FileSystemOptions();
			DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, new StaticUserAuthenticator(domain, accessKey, secretKey ) );

			this.fileUrl = fileUrl;

			fileObject = VFS.getManager().resolveFile( this.fileUrl, opts ).resolveFile(null);
		}else{
			this.fileUrl = fileUrl;
			fileObject = VFS.getManager().resolveFile( this.fileUrl );
		}
	}


	public boolean isNative(){
		return (file != null);
	}

	public String getFileName(){
		if ( fileObject != null ){
			return fileObject.getName().getBaseName();
		}else{
			return file.getName();
		}
	}

	public String getFullName(){
		if ( fileObject != null ){
			return fileObject.getName().getFriendlyURI();
		}else{
			return file.getPath();
		}
	}

	public String getParent(){
		if ( fileObject != null ){
			return fileObject.getName().getParent().getFriendlyURI();
		}else{
			return file.getParent() + File.separator;
		}
	}


	public void populateInfo() {
		setData( "name",			new cfStringData( getFileName() ) );
		setData( "path",			new cfStringData( getFullName() ) );
		setData( "filepath",	new cfStringData( getFullName() ) );
		setData( "parent",		new cfStringData( getParent() ) );

		if ( fileObject != null ){
			try{
				setData( "size",	new cfNumberData( fileObject.getContent().getSize() ) );
			}catch(Exception e){ setData( "size",	new cfNumberData( 0 ) ); }

			try{
				setData( "lastmodified", 	new cfDateData( fileObject.getContent().getLastModifiedTime() ) );
			}catch(Exception e){ setData( "lastmodified",	cfStringData.EMPTY_STRING ); }

			try{
				setData( "canread", 	cfBooleanData.getcfBooleanData(fileObject.isReadable()) );
			}catch(Exception e){ setData( "canread",	cfBooleanData.FALSE ); }

			try{
				setData( "canwrite", 	cfBooleanData.getcfBooleanData(fileObject.isWriteable()) );
			}catch(Exception e){ setData( "canwrite",	cfBooleanData.FALSE ); }

			try{
				setData( "ishidden", 	cfBooleanData.getcfBooleanData(fileObject.isHidden()) );
			}catch(Exception e){ setData( "ishidden",	cfBooleanData.FALSE ); }

			try{
				setData( "type", fileObject.getType() == FileType.FILE ? new cfStringData("file") : new cfStringData("directory") );
			}catch(Exception e){ setData( "type", new cfStringData("unknown") ); }

		}else{
			setData( "size",					new cfNumberData( file.length() ) );
			setData( "lastmodified", 	new cfDateData( file.lastModified() ) );
			setData( "canread", 			cfBooleanData.getcfBooleanData(file.canRead()) );
			setData( "canwrite", 			cfBooleanData.getcfBooleanData(file.canWrite()) );
			setData( "canexecute", 		cfBooleanData.getcfBooleanData(file.canExecute()) );
			setData( "ishidden", 			cfBooleanData.getcfBooleanData(file.isHidden()) );
			setData( "type", 					file.isDirectory() ? new cfStringData("directory") : new cfStringData("file") );
		}

	}

	private long filesize() throws FileSystemException{
		if ( fileObject != null )
			return fileObject.getContent().getSize();
		else
			return file.length();
	}

	public void write( String s ) throws Exception {
		if ( bufferedOutputStream != null ){
			if ( charset == null )
				bufferedOutputStream.write( s.getBytes() );
			else
				bufferedOutputStream.write( s.getBytes(charset) );
		}else if ( randomAccessContent != null ){
			randomAccessContent.writeBytes( s );
		}else if ( randomAccessFile != null ){
			randomAccessFile.writeBytes( s );
		}
	}

	public void writeln( String s ) throws Exception {
		if ( bufferedOutputStream != null ){
			write( s );
			write( "\r\n" );
		}else if ( randomAccessContent != null ){
			randomAccessContent.writeBytes( s );
			randomAccessContent.writeBytes( "\r\n" );
		}else if ( randomAccessFile != null ){
			randomAccessFile.writeBytes( s );
			randomAccessFile.writeBytes( "\r\n" );
		}
	}

	public void write( byte[] b ) throws Exception {
		if ( bufferedOutputStream != null ){
			bufferedOutputStream.write(b);
		}else if ( randomAccessContent != null ){
			randomAccessContent.write(b);
		}else if ( randomAccessFile != null ){
			randomAccessFile.write(b);
		}
	}

	public void flushWrite() throws Exception {
		if ( bufferedOutputStream != null ){
			bufferedOutputStream.flush();
		}
	}

	public boolean isReadable(){
		return (bufferedReader != null) || (bufferedInputStream != null);
	}

	public boolean isBinaryMode(){
		return (bufferedInputStream != null);
	}

	public boolean isWriteable(){
		return (bufferedOutputStream != null) || (randomAccessContent != null) || (randomAccessFile != null);
	}

	public BufferedInputStream getStreamReader(){
		return bufferedInputStream;
	}

	public BufferedOutputStream getStreamWriter(){
		return bufferedOutputStream;
	}

	public BufferedReader getReader(){
		return bufferedReader;
	}

	public void close() throws Exception {
		if ( bufferedInputStream != null ){
			bufferedInputStream.close();

			if ( fileObject != null )
				fileObject.getContent().close();

			bufferedInputStream = null;
		}

		if ( bufferedReader != null ){
			bufferedReader.close();

			if ( fileObject != null )
				fileObject.getContent().close();

			bufferedReader = null;
		}

		if ( bufferedOutputStream != null ){
			bufferedOutputStream.flush();
			outputStream.flush();

			bufferedOutputStream.close();
			outputStream.close();

			if ( fileObject != null )
				fileObject.getContent().close();

			outputStream					= null;
			bufferedOutputStream 	= null;
		}

		if ( randomAccessContent != null ){
			randomAccessContent.close();
			randomAccessContent = null;
		}

		if ( randomAccessFile != null ){
			randomAccessFile.close();
			randomAccessFile = null;
		}

		if ( fileObject != null )
			fileObject.close();

		setData( "status",	new cfStringData("closed") );
	}

	public boolean exists() throws Exception {
		if ( fileObject != null )
			return fileObject.exists();
		else
			return file.exists();
	}

	public FileObject getFileObject(){
		return fileObject;
	}

	public File getFile(){
		return file;
	}

	public void setEOF(){
		bEOF = true;
	}

	public boolean isEOF() {
		return bEOF;
	}


	public boolean isDirectory() throws FileSystemException {
		if ( fileObject != null )
			return (fileObject.getType() == FileType.FOLDER);
		else
			return file.isDirectory();
	}


	public void delete() throws FileSystemException {
		if ( fileObject != null )
			fileObject.delete();
		else
			file.delete();
	}
}
