package com.happytrout.commit.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.happytrout.commit.main.Reader;
import com.happytrout.commit.main.Writer;

public class ReaderTest {
	
	@Test(expected = RuntimeException.class)
	public final void whenNullStringIsPassedInThenExceptionIsThrown() {
		Reader r = new Reader("");
	}

	@Test(expected = RuntimeException.class)
	public final void whenNullStringIsPassedForCIDThenExceptionIsThrown() {
		Reader r = new Reader("deleteme");
		r.setCid("");
	}
	
	@Test(expected = RuntimeException.class)
	public final void whenCIDIsNotSetReadingCausesException() {
		Reader r = new Reader("deleteme");
		r.fetch();
	}
	
	@Test
	public final void whenCIDAndMatchesLineIsReturnedFromFormatter() {
		String fname = "deleteme";
		// Prep a file
		String source = "Now is the time for all good men\\n" 
				+ " to come to the aid of their country\\n" 
				+ " and pay their due taxes."; 
		char buffer[] = new char[source.length()]; 
		source.getChars(0, source.length(), buffer, 0); 
		FileWriter f1;
		try {
			f1 = new FileWriter(fname);
			f1.write(buffer); 
			f1.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		// File should be prepped here
		
		String sample = "A:344:Hello World\r\n";
		Reader r = new Reader(fname, "A");
		assertEquals("Formatter did not recognize correctly formatted line", sample, r.parseLine(sample));
	}
	
	@Test
	public final void whenCIDDoesNotMatchNullIsReturnedFromParsing() throws IOException {
		String fname = "deleteme";
		String sample = "A:344:Hello World\r\n";
		Reader r = new Reader(fname, "B");
		assertNull("Formatter did not recognize correctly formatted line", r.parseLine(sample));
	}
	
	@Test(expected = RuntimeException.class)
	public final void whenLineIsMalformedWithoutDelimitersParserThrowsError() throws IOException {
		String fname = "deleteme";
		String sample = "A344Hello World\r\n";
		Reader r = new Reader(fname, "A");
		assertNull("Formatter did not recognize malformed line", r.parseLine(sample));
	}
	
	@Test(expected = RuntimeException.class)
	public final void whenLineIsMalformedWithOnlyOneDelimiterParserThrowsError() throws IOException {
		String fname = "deleteme";
		String sample = "A344:Hello World\r\n";
		Reader r = new Reader(fname, "A");
		assertNull("Formatter did not recognize malformed line", r.parseLine(sample));
	}
	
	@Test
	public final void whenFileWithProperFormatIsParsedReaderGivesProperOutput() {
		String fname = "deleteme";
		// Prep a file
		String source = "A:12345:First Entry\r\n" 
				+ "A:12346:Second Entry\r\n"
				+ "A:12347:Third Entry\r\n"; 
		char buffer[] = new char[source.length()]; 
		source.getChars(0, source.length(), buffer, 0); 
		FileWriter f1;
		try {
			f1 = new FileWriter(fname);
			f1.write(buffer); 
			f1.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		// File should be prepped here
		
		Reader r = new Reader(fname, "A");
		r.fetch();
		assertThat("Did not retrieve correct delimiter data from file",
				outContent.toString(), CoreMatchers.containsString(":12346:"));
		assertThat("Did not retrieve correct message data from file",
				outContent.toString(), CoreMatchers.containsString(":Third Entry"));
	}
	
	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

	@Before
	public void setUpStreams() {
	    System.setOut(new PrintStream(outContent));
	    System.setErr(new PrintStream(errContent));
	}

	@After
	public void cleanUpStreams() {
	    System.setOut(null);
	    System.setErr(null);
	}
}
