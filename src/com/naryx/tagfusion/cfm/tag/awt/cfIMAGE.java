/* 
 *  Copyright (C) 2000 - 2011 TagServlet Ltd
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
 *  
 *  $Id: cfIMAGE.java 2374 2013-06-10 22:14:24Z alan $
 */

package com.naryx.tagfusion.cfm.tag.awt;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Serializable;

import com.nary.awt.image.imageOps;
import com.nary.io.FileUtils;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfNumberData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfStringData;
import com.naryx.tagfusion.cfm.engine.cfStructData;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.tag.cfTag;
import com.naryx.tagfusion.cfm.tag.cfTagReturnType;

public class cfIMAGE extends cfTag implements Serializable {
	static final long	serialVersionUID	= 1;



	@SuppressWarnings("unchecked")
	public java.util.Map getInfo() {
		return createInfo("remote", "CFIMAGE is a tag that allows you to modify an existing GIF, PNG or JPEG image file to produce a new image file that is resized and/or has a text label added to he image. Variables returned by this tag provide information about the new image file");
	}



	@SuppressWarnings("unchecked")
	public java.util.Map[] getAttInfo() {
		return new java.util.Map[] { 
				createAttInfo("ATTRIBUTECOLLECTION", "A structure containing the tag attributes", "", false), 
				createAttInfo("SRCFILE", "The file name of the source image file that is to be modified. Can be either a full physical path or a relative path (see the URIDirectory attribute). ", "", true), 
				createAttInfo("DESTFILE", "Required if ACTION=EDIT,CROP,ROTATE,BORDER,GRAYSCALE, Optional if ACTION=INFO. The file name of the new image file to be created by the CFIMAGE tag. Can be either a full physical path or a relative path (see the URIDirectory attribute).", "", false), 
				createAttInfo("ACTION", "The action to be taken by the  CFIMAGE tag. Valid values are 'EDIT', 'INFO', 'CROP', 'ROTATE', 'BORDER' and 'GRAYSCALE'.  The value INFO populates the CFIMAGE variables with information about the image file specified by the srcFile attribute without modifying the image. The value of EDIT creates a new image file by resizing and/or adding a text label to the source image file. The value of CROP will crop the image., The value of ROTATE will rotate an image., The action of BORDER will put a border around an image., The action of GRAYSCALE will aplly a grayscale filter to the image.", "EDIT", false), 
				createAttInfo("TYPE", "The image file type,  either  GIF, PNG or  JPEG. If this attribute is not specified, the CFIMAGE tag attempts to determine the image type based on the file name extension.", "", false), 
				createAttInfo("WIDTH", "The width of the new image, can be specified either in pixels or as a percentage of the source image width. Required if ACTION=CROP", "100%", false), 
				createAttInfo("HEIGHT", "The height of the new image, can be specified either in pixels or as a percentage of the source image height. Required if ACTION=CROP", "100%", false), 
				createAttInfo("X", "For ACTION=CROP, the starting X position for the crop", "", true), 
				createAttInfo("Y", "For ACTION=CROP, the starting Y position for the crop", "", true), 
				createAttInfo("ANGLE", "For ACTION=ROTATE the angle to which to rotate the image.  Expressed in degrees", "", false), 
				createAttInfo("FONTSIZE", "An integer value that  specified the font size of the text label to be added to the image", "12", false), 
				createAttInfo("FONTCOLOR", "Specifies the font color of the text label to be added to the image. Accepts any value that is valid for use in the FONT tag.", "black", false), 
				createAttInfo("COLOR", "The color of the border or text", "black", false), 
				createAttInfo("THICKNESS", "The thickness of the border", "1", false), 
				createAttInfo("CONTRAST", "The value to which to adjust the contrast of the image by", "", false), 
				createAttInfo("BRIGHTNESS", "The value to which to adjust the brightness of the image by", "", false), 
				createAttInfo("TEXT", "The text label to add to the image", "", false),
				createAttInfo("POSITION", "The position of  the text label to add to the image; valid valued are 'north' and 'south'. Defaults to 'south'.", "", false), 
				createAttInfo("NAMECONFLICT", "Indicates the behavior of the CFIMAGE tag when the file specified by destFile already exists. Valid values are ERROR, which generates a runtime error; SKIP, which causes the CFIMAGE tag to do nothing without generating an error; OVERWRITE, to overwrite the existing image; and,  MAKEUNIQUE, which causes CFIMAGE to create a new unique file name for the new image file", "error", false), 
				createAttInfo("URIDIRECTORY", "If YES, relative paths specified in srcFile and destFile are calculated from the web server document root directory. If NO, relative paths are calculated as relative to the current file.", "no", false)
		};
	}



	protected void defaultParameters(String _tag) throws cfmBadFileException {
		defaultAttribute("FONTSIZE", "12");
		defaultAttribute("WIDTH", "100%");
		defaultAttribute("HEIGHT", "100%");
		defaultAttribute("POSITION", "NORTH");
		defaultAttribute("URIDIRECTORY", "NO");
		defaultAttribute("FONTCOLOR", "BLACK");
		defaultAttribute("NAMECONFLICT", "ERROR");
		defaultAttribute("ACTION", "EDIT");
		defaultAttribute("COLOR", "BLACK");
		defaultAttribute("THICKNESS", "1");
		parseTagHeader(_tag);

		if (containsAttribute("ATTRIBUTECOLLECTION"))
			return;

		if (!containsAttribute("SRCFILE")) {
			throw newBadFileException("Missing Attribute", "Must contain a SRCFILE attribute");
		}

	}// defaultParameters()



	protected cfStructData setAttributeCollection(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributes = super.setAttributeCollection(_Session);

		if (!containsAttribute(attributes, "SRCFILE"))
			throw newBadFileException("Missing Attribute", "Must contain a SRCFILE attribute");

		return attributes;
	}



	public cfTagReturnType render(cfSession _Session) throws cfmRunTimeException {
		cfStructData attributes = setAttributeCollection(_Session);

		BufferedImage theImage = null;

		boolean bURI = getDynamic(attributes, _Session, "URIDIRECTORY").getBoolean();
		String error = null;
		File mainFile = null;
		boolean saveImage = false;

		try {
			// get attribute values that are req'd regardless
			String sourcefile = getDynamic(attributes, _Session, "SRCFILE").getString();
			if (bURI) {
				sourcefile = FileUtils.getRealPath(_Session.REQ, sourcefile);
			}

			String action = getDynamic(attributes, _Session, "ACTION").getString().toLowerCase();
			byte imgType = getImageType(attributes, _Session, sourcefile, "TYPE");


			if (action.equals("edit")) {
				if (!containsAttribute(attributes, "DESTFILE")) {
					throw newRunTimeException("Must contain a DESTFILE attribute");
				}

				// load the image
				theImage = loadImage(_Session, sourcefile, imgType);

				if (theImage != null) {
					// label the image if required
					labelImage(attributes, _Session, theImage);

					// resize the image if required
					theImage = resize(attributes, _Session, theImage);

					// modify the contrast and brightness if required
					if (containsAttribute(attributes, "CONTRAST") || containsAttribute(attributes, "BRIGHTNESS")) {
						try {
							theImage = contrastAndBrightness(attributes, _Session, theImage);
						} catch (Exception exc) {
							theImage = null;
							error = exc.getMessage();
						}
					}

					if (theImage != null) {
						// save the image
						saveImage = true;
					} else {
						if (error == null)
							error = "Invalid WIDTH/HEIGHT attribute. Please specify either the number of pixels or the percentage (e.g \"50%\").";
					}
				} else {
					error = "Error loading image. File could not be found,  or is an invalid format : " + getDynamic(_Session, "SRCFILE").getString();
				}

			} else if (action.equals("info")) {
				
				try {
					mainFile = new File(sourcefile);
				} catch (Exception ignored) { /* if file doesn't exist, will be caught in loadImage */
				}
				// load the image to get the width/height
				theImage = loadImage(_Session, sourcefile, imgType);
				if (theImage == null)
					error = "Error loading image. File could not be found,  or is an invalid format : " + getDynamic(_Session, "SRCFILE").getString();
				
			} else if (action.equals("crop")) {
				
				if (!containsAttribute(attributes, "DESTFILE")) {
					throw newRunTimeException("Must contain a DESTFILE attribute");
				}
				if (!containsAttribute(attributes, "X")) {
					throw newRunTimeException("Must contain a X attribute");
				}
				if (!containsAttribute(attributes, "Y")) {
					throw newRunTimeException("Must contain a X attribute");
				}
				if (!containsAttribute(attributes, "WIDTH")) {
					throw newRunTimeException("Must contain a WIDTH attribute");
				}
				if (!containsAttribute(attributes, "HEIGHT")) {
					throw newRunTimeException("Must contain a HEIGHT attribute");
				}

				// load the image
				theImage = loadImage(_Session, sourcefile, imgType);

				if (theImage != null) {
					// crop the image
					theImage = crop(attributes, _Session, theImage);

					if (theImage != null) {
						// save the image
						saveImage = true;
					} else {
						error = "Invalid X/Y/WIDTH/HEIGHT attribute.";
					}
				} else {
					error = "Error loading image. File could not be found,  or is an invalid format : " + getDynamic(attributes, _Session, "SRCFILE").getString();
				}
				
			} else if (action.equals("rotate")) {
				
				if (!containsAttribute(attributes, "DESTFILE")) {
					throw newRunTimeException("Must contain a DESTFILE attribute");
				}
				if (!containsAttribute(attributes, "ANGLE")) {
					throw newRunTimeException("Must contain an ANGLE attribute");
				}

				// load the image
				theImage = loadImage(_Session, sourcefile, imgType);

				if (theImage != null) {
					// rotate the image
					theImage = rotate(attributes, _Session, theImage);

					if (theImage != null) {
						// save the image
						saveImage = true;
					} else {
						error = "Invalid ANGLE attribute.";
					}
				} else {
					error = "Error loading image. File could not be found,  or is an invalid format : " + getDynamic(attributes, _Session, "SRCFILE").getString();
				}
				
			} else if (action.equals("border")) {
				
				if (!containsAttribute(attributes, "DESTFILE")) {
					throw newRunTimeException("Must contain a DESTFILE attribute");
				}

				// load the image
				theImage = loadImage(_Session, sourcefile, imgType);

				if (theImage != null) {
					// add a border
					theImage = addBorder(attributes, _Session, theImage);

					if (theImage != null) {
						// save the image
						saveImage = true;
					} else {
						error = "Invalid THICKNESS attribute.";
					}
				} else {
					error = "Error loading image. File could not be found,  or is an invalid format : " + getDynamic(_Session, "SRCFILE").getString();
				}
				
			} else if (action.equals("grayscale")) {
				
				if (!containsAttribute(attributes, "DESTFILE")) {
					throw newRunTimeException("Must contain a DESTFILE attribute");
				}

				// load the image
				theImage = loadImage(_Session, sourcefile, imgType);

				if (theImage != null) {
					// apply filter
					theImage = applyFilter(attributes, _Session, theImage);

					if (theImage != null) {
						// save the image
						saveImage = true;
					} else {
						error = "Failed to apply GRAYSCALE filter.";
					}
				} else {
					error = "Error loading image. File could not be found,  or is an invalid format : " + getDynamic(attributes, _Session, "SRCFILE").getString();
				}
				
			} else {
				throw newRunTimeException("Invalid ACTION attribute value. Valid values are 'EDIT', 'INFO', 'CROP', 'ROTATE', 'BORDER' and 'GRAYSCALE'.");
			}

			
			
			if (saveImage) {
				String destfile = getDynamic(attributes,_Session, "DESTFILE").getString();

				mainFile = getDestFile(_Session, destfile, bURI);
				if (mainFile == null) {
					error = "Invalid DESTFILE path: " + destfile;
				} else if (mainFile.exists()) {
					mainFile = handleExistingDestFile(attributes, _Session, mainFile);
					if (mainFile == null) {
						error = "File skipped as destination file already exists";
					} else {
						// Test when file has been renamed
						error = saveImage(_Session, theImage, mainFile, imgType);
					}
				} else {
					// Test when file has been renamed
					error = saveImage(_Session, theImage, mainFile, imgType);
				}
			}

			if (error != null) {
				createImageData_Error(_Session, error);
				throw newRunTimeException(error);
			} else {
				createImageData(_Session, theImage, mainFile);
			}

		} catch (SecurityException secExc) {
			throw newRunTimeException("CFIMAGE is not supported when it does not have FileIOPermission to the specified files.");
		}

		return cfTagReturnType.NORMAL;
	}// render()



	private static BufferedImage loadImage(cfSession _Session, String _sourcefile, byte _imgType) {
		return imageOps.loadImage(_sourcefile, _imgType);
	}// loadImage



	private static File getDestFile(cfSession _Session, String destfile, boolean _bURI) {
		if (_bURI) {
			destfile = FileUtils.getRealPath(_Session.REQ, destfile);
		}

		File outFile = new File(destfile);

		if (!outFile.isAbsolute()) {
			return null;
		}
		return outFile;
	}



	private File handleExistingDestFile(cfStructData attributes, cfSession _Session, File _outfile) throws cfmRunTimeException {
		// note the toLowerCase() so I can just do an equals on it ( not equalsIgnoreCase() )
		String nameconflict = getDynamic(attributes, _Session, "NAMECONFLICT").getString().toLowerCase();

		if (nameconflict.equals("overwrite")) {
			return _outfile;
		} else if (nameconflict.equals("skip")) {
			return null;
		} else if (nameconflict.equals("makeunique")) {
			int num = 0;
			File outFile = _outfile;
			String fileNameWithExt = outFile.getName();

			int c1 = fileNameWithExt.lastIndexOf(".");
			if (c1 > 0) {
				// if there's a file extension, add the numeric to make it unique before the extension
				String fileext = fileNameWithExt.substring(c1 + 1);
				String filename = fileNameWithExt.substring(0, c1);

				while (outFile.exists()) {
					outFile = new File(outFile.getParent(), filename + num + "." + fileext);
					num++;
				}
			} else { // no file extension
				while (outFile.exists()) {
					outFile = new File(outFile.getParent(), fileNameWithExt + num);
					num++;
				}
			}
			return outFile;
		} else { // assume error
			throw newRunTimeException("Destination file already exists.");
		}

	}



	private static String saveImage(cfSession _Session, BufferedImage _img, File _outfile, byte _imgType) {

		try {
			imageOps.saveImage(_img, _outfile, _imgType);
			return null;
		} catch (Exception e) {
			return "Error saving image. (" + e.toString() + ")";
		}
	}// loadImage



	private void labelImage(cfStructData attributes, cfSession _Session, BufferedImage _img) throws cfmRunTimeException {
		if (containsAttribute(attributes, "TEXT")) {
			String label = getDynamic(attributes, _Session, "TEXT").getString();

			if (label.length() > 0) {
				int fontsize, posX, posY;

				fontsize = getDynamic(attributes, _Session, "FONTSIZE").getInt();
				Font font = new Font("Arial", Font.PLAIN, fontsize);

				String pos = getDynamic(attributes, _Session, "POSITION").getString();
				if (pos.equalsIgnoreCase("NORTH")) {
					posX = 5;
					posY = 5 + fontsize;
				} else {
					posX = 5;
					posY = _img.getHeight() - 5;
				}

				Color labelColour = com.nary.awt.colour.getColor(getDynamic(attributes, _Session, "FONTCOLOR").getString());
				imageOps.drawString(_img, font, labelColour, label, posX, posY);
			}
		}

	}// labelImage()



	private BufferedImage resize(cfStructData attributes, cfSession _Session, BufferedImage _img) throws cfmRunTimeException {
		String width = getDynamic(attributes, _Session, "WIDTH").getString();
		String height = getDynamic(attributes, _Session, "HEIGHT").getString();

		if (width.endsWith("%") && height.endsWith("%")) {
			width = width.substring(0, width.length() - 1);
			height = height.substring(0, height.length() - 1);

			try {
				// convert to doubles
				double widthPercent = Double.parseDouble(width) / 100.0;
				double heightPercent = Double.parseDouble(height) / 100.0;
				return imageOps.resize(_img, widthPercent, heightPercent);
			} catch (Exception e) {
				return null;
			}
		} else {
			try {
				// convert to ints
				int widthInt;
				int heightInt;
				if (width.endsWith("%")) {
					widthInt = (int) (_img.getWidth() * (double) (Integer.parseInt(width.substring(0, width.length() - 1)) / 100.0));
				} else {
					widthInt = Integer.parseInt(width);
				}

				if (height.endsWith("%")) {
					heightInt = (int) (_img.getHeight() * (double) (Integer.parseInt(height.substring(0, height.length() - 1)) / 100.0));
				} else {
					heightInt = Integer.parseInt(height);
				}
				return imageOps.resize(_img, widthInt, heightInt);
			} catch (Exception e) {
				return null;
			}
		}
	}// resize()



	private BufferedImage crop(cfStructData attributes, cfSession _Session, BufferedImage _img) throws cfmRunTimeException {
		try {
			int x = getDynamic(attributes, _Session, "X").getInt();
			int y = getDynamic(attributes, _Session, "Y").getInt();
			int width = getDynamic(attributes, _Session, "WIDTH").getInt();
			int height = getDynamic(attributes, _Session, "HEIGHT").getInt();

			return imageOps.crop(_img, x, y, width, height);
		} catch (Exception e) {
			return null;
		}
	}// crop()



	private BufferedImage rotate(cfStructData attributes, cfSession _Session, BufferedImage _img) throws cfmRunTimeException {
		try {
			int angle = getDynamic(attributes, _Session, "ANGLE").getInt();
			Color bgColour = com.nary.awt.colour.getColor(getDynamic(attributes, _Session, "COLOR").getString());

			return imageOps.rotate(_img, angle, bgColour);
		} catch (Exception e) {
			return null;
		}
	}// rotate()



	private BufferedImage addBorder(cfStructData attributes, cfSession _Session, BufferedImage _img) throws cfmRunTimeException {
		try {
			int thickness = getDynamic(attributes, _Session, "THICKNESS").getInt();
			Color color = com.nary.awt.colour.getColor(getDynamic(_Session, "COLOR").getString());

			return imageOps.addBorder(_img, thickness, color);
		} catch (Exception e) {
			return null;
		}
	}// addBorder()



	private BufferedImage contrastAndBrightness(cfStructData attributes, cfSession _Session, BufferedImage _img) throws cfmRunTimeException {
		float contrast;
		float brightness;

		if (containsAttribute(attributes, "CONTRAST"))
			contrast = (float) getDynamic(attributes, _Session, "CONTRAST").getDouble();
		else
			contrast = 1;

		if (containsAttribute(attributes, "BRIGHTNESS"))
			brightness = (float) getDynamic(attributes, _Session, "BRIGHTNESS").getDouble();
		else
			brightness = 0;

		return imageOps.contrastAndBrightness(_img, contrast, brightness);
	}// contrastAndBrightness()



	private BufferedImage applyFilter(cfStructData attributes, cfSession _Session, BufferedImage _img) throws cfmRunTimeException {
		try {
			String filter = getDynamic(attributes, _Session, "ACTION").getString().toLowerCase();

			if (filter.equals("grayscale"))
				return imageOps.applyGrayScaleFilter(_img);
			else
				return null;
		} catch (Exception e) {
			return null;
		}
	}// applyFilter()



	private byte getImageType(cfStructData attributes, cfSession _Session, String _filename, String _typeAttr) throws cfmRunTimeException {
		if (containsAttribute(attributes, _typeAttr)) {
			String type = getDynamic(attributes, _Session, _typeAttr).getString().toLowerCase();
			if (type.equals("gif")) {
				return imageOps.GIF;
			} else if (type.equals("jpeg")) {
				return imageOps.JPG;
			} else if (type.equals("png")) {
				return imageOps.PNG;
			} else {
				throw newRunTimeException("Invalid " + _typeAttr + " attribute. Valid values are \"GIF\", \"JPEG\" or \"PNG\".");
			}
		} else {
			String lowerCaseFile = _filename.toLowerCase();
			if (lowerCaseFile.endsWith(".gif")) {
				return imageOps.GIF;
			} else if (lowerCaseFile.endsWith(".jpg") || lowerCaseFile.endsWith(".jpeg")) {
				return imageOps.JPG;
			} else if (lowerCaseFile.endsWith(".png")) {
				return imageOps.PNG;
			} else {
				throw newRunTimeException("Couldn't determine image type. Please specify a value for the " + _typeAttr + " attribute. Valid values are \"GIF\", \"JPEG\" or \"PNG\".");
			}
		}
	}// getImageType



	private static void createImageData_Error(cfSession _Session, String _error) {
		cfStructData imgData = new cfStructData();
		imgData.setData("success", cfBooleanData.getcfBooleanData(false));
		imgData.setData("width", new cfNumberData(0));
		imgData.setData("height", new cfNumberData(0));
		imgData.setData("errortext", new cfStringData(_error));
		imgData.setData("filename", new cfStringData(""));
		imgData.setData("filepath", new cfStringData(""));
		imgData.setData("filesize", new cfStringData(""));

		try {
			_Session.setData("cfimage", imgData);
		} catch (cfmRunTimeException ignored) {
		}
	}



	private static void createImageData(cfSession _Session, BufferedImage _img, File _file) {
		cfStructData imgData = new cfStructData();
		imgData.setData("success", cfBooleanData.getcfBooleanData(true));
		imgData.setData("width", new cfNumberData((_img != null) ? _img.getWidth() : 0));
		imgData.setData("height", new cfNumberData((_img != null) ? _img.getHeight() : 0));
		imgData.setData("errortext", new cfStringData(""));

		imgData.setData("filename", new cfStringData(_file.getName()));
		imgData.setData("filepath", new cfStringData(_file.getParent()));
		imgData.setData("filesize", new cfStringData(String.valueOf(_file.length())));

		try {
			_Session.setData("cfimage", imgData);
		} catch (cfmRunTimeException ignored) {
		}

	}// createImageData

}// cfIMAGE
