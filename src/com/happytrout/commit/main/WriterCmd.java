package com.happytrout.commit.main;

import java.security.cert.PKIXRevocationChecker.Option;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options; 
import org.apache.commons.cli.ParseException;


public class WriterCmd {

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
		// add msg option
		org.apache.commons.cli.Option msg   = OptionBuilder.withArgName( "msg" )
                .hasArg()
                .withDescription(  "commit message for logfile" )
                .create( "msg" );
		opt.addOption(msg);

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
		
		String msg_p = cmd.getOptionValue("msg");
		if(msg_p == null) {
			throw new RuntimeException("Msg required.");
		}
		
		// Make sure our log file exists
		try {
			Writer writer = new Writer(fname_p, cid_p);
			System.out.println("Committed");
			writer.commit(msg_p);
		} catch(RuntimeException e) {
			throw new RuntimeException("Initialization Error [["+e+"]]");
		}
	}

}
