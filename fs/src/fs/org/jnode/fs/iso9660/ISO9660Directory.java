/*
 * $Id$
 */
package org.jnode.fs.iso9660;

import java.io.IOException;

import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSEntryIterator;
import org.jnode.fs.FileSystem;

/**
 * @author Chira
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class ISO9660Directory implements FSDirectory {

    private final ISO9660Entry entry;

    /**
     * @param entry
     */
    public ISO9660Directory(ISO9660Entry entry) {
        this.entry = entry;
    }

    /**
     * @see org.jnode.fs.FSDirectory#iterator()
     */
    public FSEntryIterator iterator() throws IOException {
        return new FSEntryIterator() {

            int offset = 0;

            final EntryRecord parent = ISO9660Directory.this.entry
                    .getCDFSentry();

            final byte[] buffer = parent.getExtentData();

            public boolean hasNext() {
                return ((offset < buffer.length) && (buffer[ offset] > 0));
            }

            public FSEntry next() {
                final ISO9660Volume volume = parent.getVolume();
                final EntryRecord fEntry = new EntryRecord(volume, buffer, offset+1,
                        parent.getEncoding());
                offset += fEntry.getLengthOfDirectoryEntry();
                return new ISO9660Entry((ISO9660FileSystem) entry
                        .getFileSystem(), fEntry);
            }
        };
    }

    /**
     * @see org.jnode.fs.FSDirectory#getEntry(java.lang.String)
     */
    public FSEntry getEntry(String name) throws IOException {
        for (FSEntryIterator it = this.iterator(); it.hasNext();) {
            ISO9660Entry entry = (ISO9660Entry) it.next();
            if (entry.getName().equalsIgnoreCase(name)) return entry;
        }
        return null;
    }

    /**
     * @see org.jnode.fs.FSDirectory#addFile(java.lang.String)
     */
    public FSEntry addFile(String name) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * @see org.jnode.fs.FSDirectory#addDirectory(java.lang.String)
     */
    public FSEntry addDirectory(String name) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * @see org.jnode.fs.FSDirectory#remove(java.lang.String)
     */
    public void remove(String name) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * @see org.jnode.fs.FSObject#isValid()
     */
    public final boolean isValid() {
        return true;
    }

    /**
     * @see org.jnode.fs.FSObject#getFileSystem()
     */
    public final FileSystem getFileSystem() {
        return entry.getFileSystem();
    }

	/**
	 * Save all dirty (unsaved) data to the device 
	 * @throws IOException
	 */
	public void flush() throws IOException
	{
		//TODO
	}
}