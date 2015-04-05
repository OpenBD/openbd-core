package com.intridea.io.vfs.provider.s3;

import java.util.Collection;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.aw20.amazon.S3;

/**
 * An S3 file system.
 * 
 * @author Marat Komarov
 * @author Matthias L. Jugel
 */
public class S3FileSystem extends AbstractFileSystem {
	private S3 service;
	String bucket;

	public S3FileSystem(S3FileName fileName, S3 s3, FileSystemOptions fileSystemOptions) {
		super(fileName, null, fileSystemOptions);
		
		this.bucket = fileName.getRootFile();
		this.service = s3;
	}

	@SuppressWarnings("unchecked")
	protected void addCapabilities(Collection caps) {
		caps.addAll(S3FileProvider.capabilities);
	}

	protected FileObject createFile(FileName fileName) throws Exception {
		return new S3FileObject(fileName, this, service, bucket);
	}
}
