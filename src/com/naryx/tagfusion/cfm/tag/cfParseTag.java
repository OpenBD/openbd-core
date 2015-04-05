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
 *  $Id: cfParseTag.java 2513 2015-02-13 00:24:49Z alan $
 */

package com.naryx.tagfusion.cfm.tag;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import com.nary.util.FastMap;
import com.naryx.tagfusion.cfm.engine.catchDataFactory;
import com.naryx.tagfusion.cfm.engine.cfEngine;
import com.naryx.tagfusion.cfm.engine.cfOutputFilter;
import com.naryx.tagfusion.cfm.engine.cfmBadFileException;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;
import com.naryx.tagfusion.cfm.file.cfmlFileCache;
import com.naryx.tagfusion.cfm.file.sourceReader;
import com.naryx.tagfusion.cfm.parser.ANTLRNoCaseReaderStream;
import com.naryx.tagfusion.cfm.parser.CFMLLexer;
import com.naryx.tagfusion.cfm.parser.CFMLParser;
import com.naryx.tagfusion.cfm.parser.CFMLTree;
import com.naryx.tagfusion.cfm.parser.ParseException;
import com.naryx.tagfusion.cfm.parser.poundSignFilterStream;
import com.naryx.tagfusion.cfm.parser.poundSignFilterStreamException;
import com.naryx.tagfusion.cfm.parser.script.CFScriptStatement;

public class cfParseTag extends Object {

	public static int TAG_END_MARKER = 62; // '>'
	public static int TAG_START_MARKER = 60; // '<'
	public static int CHAR_SPACE = 32; // ' '
	public static int CHAR_HYPEN = 45; // '-'
	public static int CHAR_TAB = 9; // '\t'
	public static int CHAR_LINEFEED = 10; // '\n'
	public static int CHAR_CARRIAGERETURN = 13; // '\r'
	public static int CHAR_FORWARDSLASH = 47; // '/'
	public static int CHAR_SINGLEQUOTE = 39; // '\''
	public static int CHAR_DOUBLEQUOTE = 34; // '"'
	public static int CHAR_HASH = 35; // '#'
	public static int CHAR_OPEN_PAREN = 40; // '('
	public static int CHAR_CLOSE_PAREN = 41; // ')'
	public static int CHAR_OPEN_BRACKET = 91; // '['
	public static int CHAR_CLOSE_BRACKET = 93; // '['
	
	public static int CHAR_EQUAL 		= 61; // '='
	public static int CHAR_PERCENT 	= 37; // '%'
	
	private static char[] matchStart = new char[] { '<', '!', '-', '-', '-' };
	private static char[] matchEnd = new char[] { '-', '-', '-', '>' };

	
	private cfTag tagClass;
	private Map<String, String> importedMappings;

	public cfParseTag(cfTag tag) {
		tagClass = tag;
		importedMappings = new FastMap<String, String>(10);
	}

	private cfParseTag(cfTag tag, Map<String, String> _imported) {
		tagClass = tag;
		importedMappings = _imported;
	}

	public void readTag(tagReader inFile) throws IOException, cfmRunTimeException {
		readTag(inFile, null);
	}

	// read in all the tags from the file represented by tagReader
	private void readTag(tagReader inFile, String _endTagMarker) throws IOException, cfmBadFileException {
		CharArrayWriter tagBuffer = new CharArrayWriter();

		// Read the tag
		boolean bReadingTag = false;
		StringBuilder tag = null;
		StringBuilder tagname = null;
		String tagElement = "";
		int character = -1, lastcharactor = -1;
		boolean embeddedSigns = doesTagHaveEmbeddedPoundSigns();

		while ((character = inFile.read()) != -1) {
			
			if (!bReadingTag && character == TAG_START_MARKER) {
				
				tag = new StringBuilder(32);
				tagname = new StringBuilder(8);
				bReadingTag = true;
				tag.append((char) character);
				inFile.mark();

			} else if (bReadingTag) {
				tag.append((char) character);

				// Need to determine the tag name, and discover if we support this tag or not
				if (tag.length() > 0 && character == TAG_START_MARKER) {
					for (int x = 0; x < tag.length() - 1; x++)
						tagBuffer.write(tag.charAt(x));

					tag = new StringBuilder(32);
					tagname = new StringBuilder(8);
					bReadingTag = true;
					tag.append((char) character);
					inFile.mark();
					continue;
				}


				// <%=  in a cfoutput block now #len(name)#  %> 
				if ( tag.length() == 3 && tag.charAt(1) == CHAR_PERCENT && tag.charAt(2) == CHAR_EQUAL && cfEngine.isCFOutputShorthand() ){
					cfTag tg = createCFOutputRails(inFile, tagBuffer );
					
					tg.posColumn 	= inFile.curColumn;
					tg.posLine		= inFile.curLine; 
					tg.setParentTag(this.tagClass);
					
					inFile.pushStartTag(tg);
					new cfParseTag(tg, importedMappings).readTag(inFile, tg.getEndMarker());
					inFile.popEndTag(tg);

					tg.posEndColumn = inFile.curColumn;
					tg.posEndLine		= inFile.curLine; 
					
					bReadingTag = false;
					continue;
				}


				if (!isTagNameTerminatingChar(character) && (character != TAG_END_MARKER)) {
					tagname.append((char) character);

					// Is this tag a comment tag? If so then lets read
					if ((tagname.length() > 3) && (tagname.toString().indexOf("!---") != -1) && !(tagClass instanceof ContentTypeStaticInterface)) {
						/* We need to record the line */
						int commentLine = readCommentTag(inFile);
						if (commentLine > 0) {
							tagClass.commentList.add(new Integer(commentLine));
							tagBuffer.write(cfTag.COMMENT_MARKER);
						}
						bReadingTag = false;
					}

					continue;
				} else if ((tagname.length() > 0) && (isTagNameTerminatingChar(character) || (character == TAG_END_MARKER))) {
					// check for <cfreturn/> or <cfbreak/> syntax (XML-compliant closing with no attributes)
					
					if ((character == TAG_END_MARKER) && (tagname.charAt(tagname.length() - 1) == CHAR_FORWARDSLASH)) {
						tagname = tagname.deleteCharAt(tagname.length() - 1);
						if (tagname.length() == 0) { // tag is just </> so no need to look at the tagname any further
							continue;
						}
					}
					if (!isTagSupported(tagname.toString().toUpperCase()) || (tagClass instanceof ContentTypeStaticInterface)) {

						// read whitespace so we support </cfoutput >
						if (tagname.charAt(0) == '/' && Character.isWhitespace((char) character)) {
							do {
								character = inFile.read();
							} while (character != -1 && Character.isWhitespace((char) character));
						}

						// So the tag isn't supported, so lets check to see if its an ending tag
						if (character == TAG_END_MARKER && _endTagMarker != null && foundTag(_endTagMarker, tag.toString())) {
							inFile.popEndTag(tagClass);
							break;
						} else if ((character == TAG_END_MARKER) && checkEndTag(tag.toString()) && !(tagClass instanceof ContentTypeStaticInterface)) {
							inFile.addException(catchDataFactory.noStartTagException(tag.toString(), inFile.curLine, inFile.curColumn - tag.length()));
							continue;
						}	else if ( (character == TAG_END_MARKER) && tag.toString().equalsIgnoreCase("<NOCFML>") ){
								// Skip to the end of this tag; and capture all the tags, including CFML tags
								int start			= inFile.currentIndx;
								int endNoCfml = inFile.nextIndexOf("</NOCFML>", start );
								if (endNoCfml > 0) {
									tagBuffer.write( new String( inFile.data, start, endNoCfml - start) );
									inFile.setPeekIndx(endNoCfml+9);
									inFile.reset();
									bReadingTag = false;
									continue;
								}
						} else {
							// at this point, we don't recognize the tag as a supported tag
							if (tag.toString().toUpperCase().startsWith("<CF") && !(tagClass instanceof ContentTypeStaticInterface)) {
								inFile.addException(catchDataFactory.tagNotRecognizedException(tag.toString(), inFile.curLine, inFile.curColumn - tag.length()));
							}

							// It wasn't. So reset the reader to the marked position
							// just after the '<' and let the parsing continue from there
							// Note: this previously just dumped the contents to the buffer
							// and continued onwards but there was a bug within a cfquery tag '<
							// #createodbcdatetime( '
							tagBuffer.write('<');
							inFile.reset();
							bReadingTag = false;
							continue;
						}
					} else {
						//  Now read in the rest of the tag note these in case there's a parsing error
						int tagLine = inFile.curLine;
						int tagCol = inFile.curColumn;

						tagElement = readTagElement(inFile, tag);

						if (tagElement == null) {
							inFile.addException(catchDataFactory.tagParseException(tag.toString(), tagLine, tagCol));
							break;
						} else {
							determineCFtag(inFile, tagElement, tagBuffer);
						}
						bReadingTag = false;
					}
				}
			} else {
				
				if ((character == CHAR_HASH) && (_endTagMarker != null) && embeddedSigns) {
					readHashExpression(inFile, tagBuffer);
				} else if ( character == TAG_END_MARKER && lastcharactor == CHAR_PERCENT && (_endTagMarker != null) && _endTagMarker.equals("%>") ){
					break;
				} else {
					lastcharactor	= character;
					tagBuffer.write(character);
				}
			}
		}

		// Update the contents
		tagClass.tagContents = tagBuffer.toCharArray();
		
		
		// We have a little too many characters
		if ( tagClass instanceof cfOUTPUTRails ){
			if ( character != TAG_END_MARKER && lastcharactor != CHAR_PERCENT ){
				inFile.addException( catchDataFactory.noEndTagException(tagClass) );
				return;
			}
			
			tagClass.tagContents	= Arrays.copyOf( tagClass.tagContents, tagClass.tagContents.length - 1 );
		}
		
	}

	// -----------------------------------------------------

	private cfTag createTag(tagReader _inFile, String _tag) throws IOException, cfmBadFileException {

		// Is this tag one of the tags inwhich we support?
		String tag = getTagName(_tag);
		boolean bTagAvailable = cfEngine.thisInstance.TagChecker.isTagAvailable(tag);
		boolean bCustomTag = false;
		boolean bImportedCustomTag = false;

		if (!bTagAvailable) {
			bCustomTag = cfmlFileCache.isCustomTag(tag);
			if (!bCustomTag)
				bImportedCustomTag = isImportedTag(tag);// importedMappings.containsKey(tag.toUpperCase() );
		}

		if (!bTagAvailable && !bCustomTag && !bImportedCustomTag)
			return null;

		boolean bTagSupported = cfEngine.thisInstance.TagChecker.isTagSupported(tag);
		if (!bCustomTag && !bTagSupported && !bImportedCustomTag) {
			_inFile.addException(catchDataFactory.tagNotSupportedException(_tag, _inFile.curLine, _inFile.curColumn - _tag.length()));
			return null;
		}

		cfTag tg;
		try {
			Class<?> C;
			if (tag != null && !bCustomTag && bTagSupported) // look for it from the XML definition file
				C = Class.forName(cfEngine.thisInstance.TagChecker.getClass(tag));
			else if (bCustomTag) { // could be a custom tag
				if (tag.indexOf("CFX") == 0)
					C = Class.forName("com.naryx.tagfusion.cfx.cfCFX");
				else
					C = Class.forName("com.naryx.tagfusion.cfm.tag.cfCustomTag");
			} else if (bImportedCustomTag) {
				C = Class.forName("com.naryx.tagfusion.cfm.tag.cfCustomTag");
			} else
				return null;

			tg = (cfTag) C.newInstance();

			tg.posLine 		= _inFile.curLine;
			tg.posColumn 	= _inFile.curColumn - _tag.length();
			if (tg.posColumn < 1)
				tg.posColumn = _inFile.curColumn;

		} catch (Exception E) {
			throw new cfmBadFileException(catchDataFactory.classNotFoundException(_tag, _inFile.curLine, _inFile.curColumn - _tag.length()));
		}

		// Setup the tag with the various parameters
		tg.setParentTag(this.tagClass);

		if (bImportedCustomTag) {
			((cfCustomTag) tg).defaultParameters(_tag, getImportedMapping(tag));

		} else if (_tag.endsWith("/>")) {
			tg.defaultParameters(_tag.substring(0, _tag.length() - 2) + ">");
		} else
			tg.defaultParameters(_tag);

		// Make a check for the optional tag body processing
		if (tg instanceof cfOptionalBodyTag) {
			if (!_tag.endsWith("/>"))
				((cfOptionalBodyTag) tg).lookAheadForEndTag(_inFile);
			else
				((cfOptionalBodyTag) tg).setEndTag();
		} else if (tg instanceof cfIMPORT) {
			if ( !tg.containsAttribute( "PATH" ) ){
				String prefix = ((cfIMPORT) tg).getPrefix().toUpperCase();
				String dir = ((cfIMPORT) tg).getDirectory();
				// if prefix has already been defined, add the directory to the list
				if (importedMappings.containsKey(prefix)) {
					dir = importedMappings.get(prefix) + "," + dir;
				}
				importedMappings.put(prefix, dir);
			}
		}

		if (tg instanceof cfSCRIPT) {
			
			if ( !tg.containsAttribute("LANGUAGE") || tg.getConstant("LANGUAGE").equalsIgnoreCase("cfscript") ){
				((cfSCRIPT) tg).setStatement(readCFSCRIPT((cfSCRIPT) tg, _inFile));
			}else{
				cfSCRIPT newLangImp = cfSCRIPT.getLanguage(tg.getConstant("LANGUAGE"));
				if ( newLangImp != null ){
					newLangImp.setParentFile( tg.getFile() );
					newLangImp.setParentTag( tg.parentTag );
					newLangImp.setProperties( tg.getProperties() );
					tg	= newLangImp;
					new cfParseTag(tg, importedMappings ).readTag(_inFile, tg.getEndMarker());
				}else{
					throw new cfmBadFileException( catchDataFactory.generalException("CFSCRIPT", "Invalid LANGUAGE='" + tg.getConstant("LANG") + "'" ) );
				}
			}
			
			tg.posEndColumn = _inFile.curColumn -tg.getTagName().length() - 2;
			tg.posEndLine = _inFile.curLine;
			
		} else if (!_tag.endsWith("/>") && tg.getEndMarker() != null) {
			_inFile.pushStartTag(tg);
			new cfParseTag(tg, importedMappings ).readTag(_inFile, tg.getEndMarker());
			tg.posEndColumn = _inFile.curColumn - tg.getTagName().length() - 2;
			tg.posEndLine = _inFile.curLine;
		}

		return tg;
	}

	private static CFScriptStatement readCFSCRIPT(cfSCRIPT _script, tagReader _reader) throws cfmBadFileException {
		// note reader positions before starting to read
		int start = _reader.currentIndx;
		int col = _reader.curColumn;
		int line = _reader.curLine;

		try {
			/*
			 * The ANTLR parser will consume the entire input stream provided to it
			 * which is not very efficient and also leaves our poundSignFilterStream
			 * with an inaccurate 'added' count.
			 * 
			 * So we do a crude search for the end tag with a string search to find 
			 * the script block. The only problem being the scenario where '</cfscript>'
			 * appears in the block as part of a string. To handle this, we keep searching 
			 * for the end tag until we find a successfully parsing block, or we reach the
			 * end of the file (upon which the original Exception is thrown
			 */

			int endScript = _reader.nextIndexOf("</CFSCRIPT>", start);
			if (endScript > 0) {
				endScript += 11;
			}

			poundSignFilterStream psf = null;
			ANTLRNoCaseReaderStream input = null;
			CFMLLexer lexer = null;
			ParserRuleReturnScope r = null;
			CommonTokenStream tokens = null;
			Exception firstException = null;

			while (endScript > 0) {
				try {
					String scriptBlock = new String(_reader.data, start, endScript - start);
					psf = new poundSignFilterStream(new StringReader(scriptBlock));
					input = new ANTLRNoCaseReaderStream(psf);

					lexer = new CFMLLexer(input);
					tokens = new CommonTokenStream(lexer);
					CFMLParser parser = new CFMLParser(tokens);
					r = parser.scriptBlock();

				} catch (Exception e) {
					if (firstException == null) {
						firstException = e;
					}
					endScript = _reader.nextIndexOf("</CFSCRIPT>", endScript);
					if (endScript > 0) {
						endScript += 11;
						continue;
					}
				}
				break;
			}

			if (r == null) { // we did not successfully find a parsable script block
				if (firstException != null) {
					throw firstException;
				}

				throw new cfmBadFileException(catchDataFactory.noEndTagException(_script));
			}

			CommonTree tree = (CommonTree) r.getTree();
			// need to check the child count. It will be 0 if the script tag only
			// contains comments
			// otherwise, the last child should be the </CFSCRIPT> tag
			if (tree.getChildCount() > 0) {
				CommonTree t2 = (CommonTree) tree.getChild(tree.getChildCount() - 1);
				if (t2.token != null && t2.token.getType() != CFMLParser.SCRIPTCLOSE) {
					throw new cfmBadFileException(catchDataFactory.noEndTagException(_script));
				}
			}

			CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
			nodes.setTokenStream(tokens);
			CFMLTree p2 = new CFMLTree(nodes);
			CFScriptStatement statement = p2.scriptBlock();
			List<String> importPaths = p2.getImportPaths();
			if ( importPaths.size() > 0 ){
				_script.getFile().addImportPaths( importPaths );
			}

			// having parsed the tagReader input to the end tag </CFSCRIPT>, the
			// tagReader will be left in a position just beyond that tag.
			// So first get the positions that the parser's SimpleCharStream is at
			int newCol = input.getCharPositionInLine();
			int newLine = input.getLine();
			int newStart = start + lexer.getCharIndex();

			// now work out the position in the tagReader that the cfscript
			// block ends. As well as including the SimpleCharStream position from
			// above we also need to adjust for the chars added/removed by the
			// poundSignFilterStream.
			_reader.currentIndx = newStart - psf.getAdded();
			_reader.curColumn = newCol + 1;
			_reader.curLine = line + newLine - 1;

			// DEBUG: String scriptblock = new String( _reader.data, start,
			// _reader.currentIndx-start );
			// find special cases of "#varName#"="value";
			// note that only the char [] that represents the <cfscript> block
			// contents is passed in
			sourceReader sr = new sourceReader(new BufferedReader(new CharArrayReader(_reader.data, start, _reader.currentIndx - 11 - start)));
			statement.checkIndirectAssignments(sr.getLines());

			int scriptBodyLen = _reader.currentIndx - start;
			char[] tagBody = new char[scriptBodyLen];
			for (int i = 0; i < scriptBodyLen; i++) {
				tagBody[i] = _reader.data[start + i];
			}
			_script.tagContents = tagBody;

			return statement;

		} catch (poundSignFilterStreamException e) {
			// reset the reader to start of the cfscript block
			_reader.curLine = line;
			_reader.curColumn = col;
			throw new cfmBadFileException(catchDataFactory.extendedScriptException("errorCode.badFormat", "expression.Parse", new String[] { e.getMessage() }, 0, 0));

		} catch (RecognitionException e) {
			com.nary.Debug.printStackTrace( e );
			_reader.curLine = line + e.line - 1;
			_reader.curColumn = e.charPositionInLine + 1;
			String msg = e.getMessage();
			if (e instanceof NoViableAltException && e.token != null) {
				msg = "Encountered invalid token after \"" + e.token.getText() + "\"";
			}
			throw new cfmBadFileException(catchDataFactory.extendedScriptException("errorCode.badFormat", "expression.Parse", new String[] { msg }, 0, 0));
		} catch (ParseException e) {
			_reader.curLine = line + e.getLine() - 1;
			_reader.curColumn = e.getCol() + 1;
			throw new cfmBadFileException(catchDataFactory.extendedScriptException("errorCode.badFormat", "expression.Parse", new String[] { e.getMessage() }, 0, 0));
		} catch (cfmBadFileException e) {
			throw e;
		} catch (Exception e) {
			com.nary.Debug.printStackTrace( e );
			throw new cfmBadFileException(catchDataFactory.extendedScriptException("errorCode.badFormat", "expression.Parse", new String[] { "Error parsing <CFSCRIPT> block: " + e.getMessage() }, line, col));
		}
	}

	
	
	private cfTag createCFOutputRails(tagReader _inFile, CharArrayWriter _tagBuffer) {
		cfTag	tagInst	= new cfOUTPUTRails();
		tagClass.tagList.add(tagInst);
		_tagBuffer.write(cfTag.TAG_MARKER);
		return tagInst;
	}
	
	
	
	private void determineCFtag(tagReader _inFile, String _tag, CharArrayWriter _tagBuffer) throws IOException, cfmBadFileException {
		cfTag tagInst = createTag(_inFile, _tag);
		if (tagInst != null) { // tag is a CFML tag
			// ignore CFASSERT tags if assertions disabled
			if (!(tagInst instanceof cfASSERT) || cfEngine.isAssertionsEnabled()) {
				tagClass.tagList.add(tagInst);
				_tagBuffer.write(cfTag.TAG_MARKER);
			}
		} else { // tag is not a CFML tag, copy it to the buffer
			_tagBuffer.write(_tag);
		}
	}

	// -----------------------------------------------------

	private boolean isImportedTag(String _tag) {
		String prefix = getPrefix(_tag);
		return (prefix == null ? false : importedMappings.containsKey(prefix.toUpperCase()));
	}

	private static String getPrefix(String _tag) {
		int colonIndex = _tag.indexOf(':');
		int underscoreIndex = _tag.indexOf('_');
		if (colonIndex != -1) {
			return _tag.substring(0, colonIndex);
		} else if (underscoreIndex != -1) {
			return _tag.substring(0, underscoreIndex);
		} else {
			return null;
		}
	}

	private String getImportedMapping(String _tag) {
		String prefix = getPrefix(_tag);
		return importedMappings.get(prefix.toUpperCase());
	}

	// -----------------------------------------------------

	private static String getTagName(String tag) {
		try {
			int tagStart = 1; // start at position 1 to bypass opening <
			for (int i = tagStart; i < tag.length(); i++) {
				if (!Character.isWhitespace(tag.charAt(i))) {
					tagStart = i;
					break;
				}
			}
			int tagEnd = -1;
			for (int x = tagStart; x < tag.length(); x++) {
				if (isTagNameTerminatingChar(tag.charAt(x))) {
					tagEnd = x;
					break;
				}
			}

			if (tagEnd == -1) {
				if (tag.endsWith("/>")) {
					return tag.substring(tagStart, tag.length() - 2).toUpperCase();
				} else {
					return tag.substring(tagStart, tag.length() - 1).toUpperCase();
				}
			} else {
				return tag.substring(tagStart, tagEnd).toUpperCase();
			}
		} catch (Exception E) {
			return null;
		}
	}


	protected static boolean isWhiteSpace(int character) {
		return ((character == CHAR_SPACE) || (character == CHAR_CARRIAGERETURN) || (character == CHAR_LINEFEED) || (character == CHAR_TAB));
	}

	
	protected static boolean isTagNameTerminatingChar(int character) {
		return ((character == CHAR_SPACE) || (character == CHAR_CARRIAGERETURN) || (character == CHAR_LINEFEED) || (character == CHAR_TAB) || (character == CHAR_HASH) || (character == CHAR_OPEN_PAREN));
	}

	private boolean isTagSupported(String tag) {
		if (cfEngine.thisInstance.TagChecker.isTagAvailable(tag) || cfmlFileCache.isCustomTag(tag) || this.isImportedTag(tag))
			return true;
		else
			return false;
	}

	private static int readCommentTag(tagReader inFile) {
		StringBuilder tag = new StringBuilder(32);
		int counter = 1;
		int character;
		int matchSI = 0;
		int matchEI = 0;

		int startLine = inFile.curLine;
		while ((character = inFile.read()) != -1) {
			tag.append((char) character);
			if (character == matchStart[matchSI]) {
				if (matchSI == 4) {
					counter++;
					tag = new StringBuilder(32);
					matchSI = 0;
				} else {
					matchSI++;
				}
			} else if (character == matchEnd[matchEI]) {
				if (matchEI == 3) {
					--counter;
					if (counter == 0)
						break;
					matchEI = 0;
					tag = new StringBuilder(32);
				} else {
					matchEI++;
				}
			} else if (matchSI != 0) {
				matchSI = 0;
			} else if (!(matchEI == 3 && character == '-')) {
				matchEI = 0;
			}

		}

		if (startLine != inFile.curLine) {
			return inFile.curLine;
		} else {
			return 0;
		}
	}

	
	/**
	 * This method reads the remainder part of the tagelement and returns
	 * the completed tag. ie "<CFSET asdalk=asdasd>" would be returned
	 * 
	 * Method enabled for the special escaping of the <cfif> / <cfelseif> tags
	 * to support the [] escape syntax.  This enables us to use <, > operators
	 * within the tag without corrupting the tag parser
	 * 
	 * @param inFile
	 * @param tag
	 * @return
	 */
	private static String readTagElement(tagReader inFile, StringBuilder tag) {
		if (tag.charAt(tag.length() - 1) == TAG_END_MARKER)
			return tag.toString();

		// For special case, <cfif ()> / <cfelseif ()> to cope with the > >= operators
		boolean braceEscapable = false, braceEscapeStarted = false;
		int braceEscapeCount = 0;
		String tmpTag = tag.toString().toLowerCase();
		if ( tmpTag.indexOf("<cfif") == 0 || tmpTag.indexOf("<cfelseif") == 0 ){
			braceEscapable = true;
		}
		
		// Run through and look for the ">" character
		int character;
		StringBuilder blockChars = new StringBuilder(8);

		while ((character = inFile.read()) != -1) {
			tag.append( (char)character );

			//Check for the escapable area for <cfif> <cfelseif> tags that lets us do <cfif [2>1]>
			if ( braceEscapable ){
				if ( !braceEscapeStarted ){
					if ( !isWhiteSpace(character) ){
						if ( character == CHAR_OPEN_PAREN ){
							braceEscapeStarted = true;
							braceEscapeCount = 1;
						}else{
							braceEscapable = false; // this is a normal expression, not being escaped, so bypass all this logic
						}
					}
				}else if ( braceEscapeStarted && character == CHAR_OPEN_PAREN && blockChars.length() == 0 ){
					braceEscapeCount++;
				}else if ( braceEscapeStarted && character == CHAR_CLOSE_PAREN && blockChars.length() == 0 ){
					braceEscapeCount--;
				}
			}
			
			//Check for the end marker if none has been specified
			if (character == CHAR_DOUBLEQUOTE || character == CHAR_SINGLEQUOTE || character == CHAR_HASH) {
				if (blockChars.length() > 0) {
					char lastChar = blockChars.charAt(blockChars.length() - 1);
					if (blockChars.length() > 0 && character == lastChar) {
						blockChars.deleteCharAt(blockChars.length() - 1);
					} else if (!((character == CHAR_DOUBLEQUOTE && lastChar == CHAR_SINGLEQUOTE) || (character == CHAR_SINGLEQUOTE && lastChar == CHAR_DOUBLEQUOTE))) {
						blockChars.append((char) character);
					}
				} else {
					blockChars.append((char) character);
				}
			} else if (character == TAG_END_MARKER && blockChars.length() == 0 && braceEscapeCount == 0)
				break;
		}

		return tag.toString();
	}

	// --------------------------------------------------------

	private static boolean foundTag(String needTag, String tag) {
		return getTagName(needTag).equalsIgnoreCase(getTagName(tag).trim());
	}

	// --------------------------------------------------------

	private boolean checkEndTag(String tag) {
		tag = getTagName(tag).trim();

		// This method takes the tag and see's if its a tag that should have an opening tag
		if (tag.length() == 0 || tag.charAt(0) != CHAR_FORWARDSLASH)
			return false;

		// The tag is in the format </XXXX>
		tag = tag.substring(1);
		return (cfEngine.thisInstance.TagChecker.isTagAvailable(tag) && cfEngine.thisInstance.TagChecker.isTagSupported(tag)) || cfmlFileCache.isCustomTag(tag) || isImportedTag(tag);
	}

	// --------------------------------------------------------

	private boolean doesTagHaveEmbeddedPoundSigns() {
		return (tagClass == null ? false : tagClass.doesTagHaveEmbeddedPoundSigns());
	}

	// --------------------------------------------------------

	private static void readHashExpression(tagReader inFile, CharArrayWriter out) throws cfmBadFileException {
		/*
		 *  This method reads #...# expressions. It has to be noted that by the time of the call
		 *  the first # has been read from the input stream. We'll utilise the technology of the
		 *  cfOutputFilter to do all the escaping of the #'s. We will pass in null
		 *  to the output side of the cfOutputFilter, since we only want it to collect the
		 *  expression for us.
		 */
		cfOutputFilter outFilter = new cfOutputFilter();

		try {
			outFilter.write(CHAR_HASH, out);
			int charIn;
			while ((charIn = inFile.read()) != -1) {
				if (outFilter.write(charIn, null))
					break;
			}
		} catch (cfmBadFileException bfe) {
			// catch and rethrow because cfmBadFileException is a subclass of
			// cfmRunTimeException
			throw bfe;
		} catch (cfmRunTimeException ignore) {
			// will never happen because the code in outFilter.write() that throws
			// cfmRunTimeException is never executed because ( session == null )
		}

		
		// Need to add the expression to the output
		out.write(CHAR_HASH);
		char chs[] = outFilter.getExpression().toCharArray();
		out.write(chs, 0, chs.length);
		out.write(CHAR_HASH);
	}
}
