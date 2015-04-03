package com.happytrout.commit.main;

import java.io.*;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Objects;

/**
 * Reads messages from commit log and displays them on stdout:w
 * 
 * Messages have the format:
 *   CID: <Unique-ID>: Data <CRLF>
 *   
 *   where:
 *     CID	      is a log writer identifier, unique for each writer
 *     Unique-ID  is a alpha-numeric immutable key for each log message
 *     Data       is a string of arbitrary length, presumed to be UTF8 encoded
 *
 * @author      T. Collins
 * @see         Writer
 * @since       1.0.0 (Apr 2015)
 */

public class Reader {

	// Global file handle and writer
	File fh = null;
	FileReader fileReader = null;
	BufferedReader br = null;
	
	// Global CID
	String cid;
	
	// Global Line Counter for Debug
	int line_cntr = 0;
	
/**
 * Constructor, which takes a file name.  This file name is
 * added to the current path to create or open the log file
 * 
 * @param fname  File name for the log file
 */
	//FIXME File permissions should reflect creator of the
	// logfile, even if created by proxy (e.g. www-data)
	public Reader(final String fname) throws RuntimeException {
		
		if(fname.length() < 1) {
			throw new RuntimeException("File name cannot be null.");
		}
		
	    fh = new File(fname);
		    
	    if(!fh.exists()) {
	    		throw new RuntimeException("Could not open file ["+fname+"] for reading, does not exist");
	    }
	}
	
/**
 * Alternative constructor, which takes a file name and a cid.
 * This is the same as the base constructor, but saves setting the
 * cid later.
 * 
 * @param fname  File name for the log file
 * @param cid    String uniquely identifying this writer
 */
	public Reader(final String fname, final String cid) throws RuntimeException {
		
		this(fname);
		
		this.setCid(cid);
		
	}
	
/**
* Setters and Getters for CID
* 
* @param  msg  a string of arbitrary length
* @return      void
*/
	public void setCid(final String cid) {
		if(cid.length() < 1) {
			throw new RuntimeException("CID cannot be null.");
		}		
		this.cid = cid;
	}
	
	public String getCid() {
		return(this.cid);
	}
	
/**
* Sends the contents of the commit log to stdout. Note that
* it ignores all entries with CIDs different than the current
* CID for this reader.
* 
* If it is successful, it returns silently.
* 
* If it fails, it throws a RuntimeException, with the error
* set to the explanation of the failure.
* 
* @return      void
*/
	public final void fetch() {
		if(cid.length() < 1) {
			throw new RuntimeException("Must set CID before logging entries.");
		}
		try {
			synchronized(this) {
				// Open our file reader
				fileReader = new FileReader(fh);
				br = new BufferedReader(fileReader); 

				String s;
				line_cntr = 0;
				while((s = br.readLine()) != null) {
					String out = parseLine(s);
					if(out != null) {
						System.out.println(out);
					}
					line_cntr++;
				} 
			}
		} catch(IOException e) {
			throw new RuntimeException("IO Error writing to log file ["+fh.getPath()+"] "+e);
		}
	}

/**
* Parses a single line from the log file, and displays
* it if the CID matches the current CID of this reader
* 
* Note, this is NOT thread-safe.  Always call this from
* within a synchronized block.
* 
* @param  raw  a string of arbitrary length
* @return      void
*/
	public String parseLine(final String raw) {
		final String delimiter = ":";
		
		if(cid == null || cid.length() < 1) {
			throw new RuntimeException("CID cannot be null.");
		}
		if(raw.length() < 5) {
			throw new RuntimeException("Malformed line in log, too short line ["+line_cntr+"]");
		}
		int first_delim = raw.indexOf(delimiter);
		if(first_delim < 0) {
			throw new RuntimeException("Malformed line in log, has no delimiters line ["+line_cntr+"]");
		}
		String residual = raw.substring(first_delim + 1);
		if(residual.indexOf(delimiter)< 0) {
			throw new RuntimeException("Malformed line in log, has only one delimiter line ["+line_cntr+"]");
		}

		String[] fields = raw.split(delimiter);
		if(cid.equals(fields[0])) {
			return(raw);
		} else {
			return(null);
		}
	}
	
/**
 * Tidies up, for purists, and on the side of
 * always closing file handles, etc. This is not strictly
 * necessary, as Java will handle this for us.
 * 
 * 	Read more: http://javarevisited.blogspot.com/2012/03/finalize-method-in-java-tutorial.html#ixzz3W9uaCDRa
 */
@Override
    protected void finalize() throws Throwable {
        try{
        		if(fileReader != null) {
        			fileReader.close();
        		}
        }catch(Throwable t){
            throw t;
        }finally{
            super.finalize();
        }
    }
}
