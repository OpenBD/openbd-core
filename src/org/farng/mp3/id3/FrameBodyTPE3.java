package org.farng.mp3.id3;

import java.io.RandomAccessFile;

import org.farng.mp3.InvalidTagException;

/**
 * &nbsp;&nbsp; The 'Conductor' frame is used for the name of the conductor.</p>
 *
 * @author Eric Farng
 * @version $Revision: 2374 $
 */
public class FrameBodyTPE3 extends AbstractFrameBodyTextInformation {

    /**
     * Creates a new FrameBodyTPE3 object.
     */
    public FrameBodyTPE3() {
        super();
    }

    /**
     * Creates a new FrameBodyTPE3 object.
     */
    public FrameBodyTPE3(final FrameBodyTPE3 body) {
        super(body);
    }

    /**
     * Creates a new FrameBodyTPE3 object.
     */
    public FrameBodyTPE3(final byte textEncoding, final String text) {
        super(textEncoding, text);
    }

    /**
     * Creates a new FrameBodyTPE3 object.
     */
    public FrameBodyTPE3(final RandomAccessFile file) throws java.io.IOException, InvalidTagException {
        super(file);
    }

    public String getIdentifier() {
        return "TPE3";
    }
}