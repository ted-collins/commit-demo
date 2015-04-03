package com.happytrout.commit.main;

import java.io.*;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.concurrent.Semaphore;

/**
 * Receives log messages bound for a commit log, formats them
 * and writes them to that commit log.
 * 
 * Will create the commit log file if it does not already exist.
 * If it does exist, it will open and append.
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
 * @see         Reader
 * @since       1.0.0 (Apr 2015)
 */

public class Writer {

	// Global file handle and writer
	File fh = null;
	FileWriter fileWriter = null;
	
	// Global CID
	String cid;
	
	// Semaphore for concurrency control
	private final Semaphore available = new Semaphore(1, true);
	
/**
 * Constructor, which takes a file name.  This file name is
 * added to the current path to create or open the log file
 * 
 * @param fname  File name for the log file
 */
	//FIXME File permissions should reflect creator of the
	// logfile, even if created by proxy (e.g. www-data)
	public Writer(final String fname) throws RuntimeException {
		
		if(fname.length() < 1) {
			throw new RuntimeException("File name cannot be null.");
		}
		
		try {
		    fh = new File(fname);
		    
		    synchronized(this) {
		    		if(!fh.exists()) {
		    			// Create our logfile if needed
		    			fh.createNewFile();
		    		}
		    }
		      
		} catch(FileNotFoundException e) {
			throw new RuntimeException("Could not open file ["+fname+"] for writing "+e);
		} catch(IOException e) {
			throw new RuntimeException("IO error opening file ["+fname+"] for writing "+e);
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
	public Writer(final String fname, final String cid) throws RuntimeException {
		
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
* Writes a message to the commit log.
* 
* If it is successful, it returns silently.
* 
* If it fails, it throws a RuntimeException, with the error
* set to the explanation of the failure.
* 
* @param  msg  a string of arbitrary length
* @return      void
*/
	public final void commit(final String msg) {
		if(msg.length() < 1) {
			throw new RuntimeException("Cannot commit null string.");
		}
		if(cid.length() < 1) {
			throw new RuntimeException("Must set CID before logging entries.");
		}
		try {
			//TODO Create test to simulate multiple writes
			synchronized(this) {
				// Formatting does not require semaphore
				String s = formatLine(msg) + "\r\n";
				// Be sure our writes are atomic
				available.acquire();
				// Open our file writer
				fileWriter = new FileWriter(fh, true);
				fileWriter.append(s);
				fileWriter.flush();
				available.release();
			}
		} catch(IOException | InterruptedException e) {
			throw new RuntimeException("IO Error writing to log file ["+fh.getPath()+"] "+e);
		}
	}

/**
* Formats a single line for writing into a log file.
* 
* Note, this is NOT thread safe, so always call it from
* within a synchronized block if multi-threading.
* 
* @param  msg  a string of arbitrary length
* @return      string for writing to log file
*/
	public String formatLine(final String msg) {
		final String delimiter = ":";
		
		if(cid.length() < 1) {
			throw new RuntimeException("CID cannot be null.");
		}
		String stamp = Objects.toString(System.currentTimeMillis(), null);
		SecureRandom random = new SecureRandom();
		String rand = new BigInteger(130, random).toString(32).substring(0, 4);
		String ret = this.cid + delimiter + stamp + "-" + rand + delimiter + msg;
		
		return ret;
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
        		if(fileWriter != null) {
        			fileWriter.close();
        		}
        		if(available != null) {
        			available.release();
        		}
        }catch(Throwable t){
            throw t;
        }finally{
            super.finalize();
        }
    }
}
