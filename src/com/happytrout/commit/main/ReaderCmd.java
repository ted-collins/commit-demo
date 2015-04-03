package com.happytrout.commit.main;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options; 
import org.apache.commons.cli.ParseException;


public class ReaderCmd {

	public static void main(String[] args) {
		// create Options object
		Options opt = new Options();
		
		// add fname option
		org.apache.commons.cli.Option fname   = OptionBuilder.withArgName( "fname" )
                .hasArg()
                .withDescription(  "filename for log file" )
                .create( "fname" );
		opt.addOption(fname);
		// add cid option
		org.apache.commons.cli.Option cid   = OptionBuilder.withArgName( "cid" )
                .hasArg()
                .withDescription(  "commit id for message" )
                .create( "cid" );
		opt.addOption(cid);

		CommandLineParser parser = new GnuParser();
		CommandLine cmd;
		try {
			cmd = parser.parse( opt, args);
		} catch (ParseException e) {
			throw new RuntimeException("Parser Blew Fuse."+e);
		}
		
		String fname_p = cmd.getOptionValue("fname");
		if(fname_p == null) {
			throw new RuntimeException("Filename required.");
		}
		
		String cid_p = cmd.getOptionValue("cid");
		if(cid_p == null) {
			throw new RuntimeException("CID required.");
		}
		
		// Make sure our log file exists
		try {
			Reader reader = new Reader(fname_p, cid_p);
			reader.fetch();
		} catch(RuntimeException e) {
			throw new RuntimeException("Initialization Error [["+e+"]]");
		}
	}

}
