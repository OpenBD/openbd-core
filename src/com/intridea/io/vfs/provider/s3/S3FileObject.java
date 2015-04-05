package com.intridea.io.vfs.provider.s3;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileObject;
import org.apache.commons.vfs.util.MonitorOutputStream;
import org.aw20.amazon.S3;
import org.aw20.amazon.S3Exception;
import org.aw20.amazon.S3Result;

import com.naryx.tagfusion.cfm.file.MimeLookup;

/**
 * Implementation of the virtual S3 file system object using the Jets3t library.
 * Based on Matthias Jugel code
 * http://thinkberg.com/svn/moxo/trunk/src/main/java/com/thinkberg/moxo/
 * 
 * @author Marat Komarov
 * @author Matthias L. Jugel
 * 
 *         Modified by Alan Williamson @ September 2009
 * 
 */
public class S3FileObject extends AbstractFileObject {
	
	/**
	 * Amazon S3 service
	 */
	private final S3 service;
	private final String bucket;
	
	private S3Result s3result;

	/**
	 * True when content attached to file
	 */
	private boolean attached = false;

	/**
	 * True when content downloaded. It's an extended flag to
	 * <code>attached</code>.
	 */
	private boolean downloaded = false;

	/**
	 * Local cache of file content
	 */
	private File cacheFile;

	
	/**
	 * Class logger
	 */
	
	private FileName fileName;
	private S3FileSystem fileSystem;
	
	public S3FileObject(FileName fileName, S3FileSystem fileSystem, S3 service, String bucket) throws FileSystemException {
		super(fileName, fileSystem);
		this.service 		= service;
		this.bucket 		= bucket;
		this.fileName		= fileName;
		this.fileSystem	= fileSystem;
	}
	
	public FileObject resolveFile(String f) throws FileSystemException {
		if ( f != null )
			return super.resolveFile(f);
		else{
			// Special constructor to create a new version
			return new S3FileObject( fileName, fileSystem, service, bucket );
		}
	}

	public void close(){
		if ( cacheFile != null ){
			cacheFile.delete();
			cacheFile = null;
		}
	}
	
	protected void doAttach() throws Exception {
		if (!attached) {
			try {
				s3result	= service.getInfo(bucket, getS3Key() );
			} catch (S3Exception e) {
				s3result = null;
				downloaded = true;
			}
			attached = true;
		}
	}

	protected void doDetach() throws Exception {
		if (attached) {
			downloaded = false;
			attached = false;
		}
		
		if ( cacheFile != null ){
			cacheFile.delete();
			cacheFile = null;
		}
	}

	protected void doDelete() throws Exception {
		service.delete(bucket, getS3Key() );
	}

	protected void doRename(FileObject newfile) throws Exception {
		throw new Exception( "doRename not supported" );
	}

	protected void doCreateFolder() throws Exception {
		return;
	}

	protected long doGetLastModifiedTime() throws Exception {
		if ( s3result != null ){
			return s3result.getLastModified();
		}else
			return 0;
	}

	protected void doSetLastModifiedTime(final long modtime) throws Exception {
		System.out.println( "S3:doSetLastModifiedTime" );
	}

	protected InputStream doGetInputStream() throws Exception {
		downloadOnce();
		if ( downloaded ){
			return new java.io.FileInputStream( cacheFile );
		}else
			throw new Exception( "file not available" );
	}

	protected OutputStream doGetOutputStream(boolean bAppend) throws Exception {
		if ( bAppend )
			throw new Exception("Append Operation not supported");
		
		if ( cacheFile != null ){
			cacheFile.delete();
		}

		cacheFile = File.createTempFile( "openbd.", ".out.s3" );
		return new S3OutputStream( new FileOutputStream( cacheFile ), service, getS3Key() );
	}

	protected FileType doGetType() throws Exception {
		if ("".equals(this.getS3Key()) || this.getS3Key().endsWith("/") ) {
			return FileType.FOLDER;
		}else{
			return FileType.FILE;
		}
	}

	protected String[] doListChildren() throws Exception {
		String path = getS3Key();

		// make sure we add a '/' slash at the end to find children
		if (!"".equals(path)) {
			path = path + "/";
		}

		S3Result listResult = service.bucketListAll( bucket, path );
		String[] childrenNames = new String[ listResult.fileList.size() ];
		for (int i = 0; i < childrenNames.length; i++) {
			childrenNames[i] = listResult.fileList.get(i).key.replaceAll("[^/]*//*", "");
		}
		
		return childrenNames;
	}

	protected long doGetContentSize() throws Exception {
		if ( s3result != null ){
			String contentLength	= s3result.getHttpHeader("content-length");
			if ( contentLength != null )
				return Long.valueOf(contentLength);
		}
		
		return 0;
	}

	// Utility methods

	/**
	 * Download S3 object content and save it in temporary file. Do it only if
	 * object was not already downloaded.
	 */
	private void downloadOnce() throws FileSystemException {
		if (!downloaded) {
			if ( cacheFile != null ){
				cacheFile.delete();
			}

			final String failedMessage = "Failed to download S3 Object %s. %s";
			final String objectPath = getName().getPath();
			
			BufferedInputStream reader = null;
			BufferedOutputStream	writer = null;
			FileOutputStream fileStream = null;
			
			try {
				cacheFile 	= File.createTempFile( "openbd.", ".in.s3" );
				
				fileStream	= new FileOutputStream(cacheFile);
				s3result		= service.get(bucket, getS3Key(), null, S3.READ_TIMEOUT, fileStream );
				
			} catch (S3Exception e) {
				throw new FileSystemException(String.format(failedMessage, objectPath, e.getMessage()), e);
			} catch (IOException e) {
				throw new FileSystemException(String.format(failedMessage, objectPath, e.getMessage()), e);
			} finally {

				if ( reader != null ){
					try {reader.close();} catch (IOException e) {}
				}

				if ( writer != null ){
					try {writer.flush(); writer.close();} catch (IOException e) {}
				}
				
				if ( fileStream != null ){
					try {fileStream.close();} catch (IOException e) {}
				}

			}
			downloaded = true;
		}
	}
	

	/**
	 * Create an S3 key from a commons-vfs path. This simply strips the slash from
	 * the beginning if it exists.
	 * 
	 * @return the S3 object key
	 */
	private String getS3Key() {
		String path = getName().getPath();
		if ("".equals(path)) {
			return path;
		} else {
			return path.substring(1);
		}
	}

	
	/**
	 * Special JetS3FileObject output stream. It saves all contents in temporary
	 * file, onClose sends contents to S3.
	 */
	private class S3OutputStream extends MonitorOutputStream {
		private S3 service;
		private String object;

		public S3OutputStream(OutputStream out, S3 service, String object) {
			super(out);
			this.service 	= service;
			this.object 	= object;
		}

		protected void onClose() throws IOException {
			out.flush();
			out.close();

			try {
				service.put( bucket, object, MimeLookup.getMimeType(object), cacheFile );
			} catch (S3Exception e) {
				throw new IOException(e.getMessage());
			}finally{
				cacheFile.delete();
				cacheFile = null;
			}
		}
	}
}