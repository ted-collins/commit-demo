package com.happytrout.commit.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.hamcrest.CoreMatchers.*;
import org.junit.Test;

import com.happytrout.commit.main.Writer;

public class WriterTest {
	
	@Test(expected = RuntimeException.class)
	public final void whenNullStringIsPassedInThenExceptionIsThrown() {
		Writer w = new Writer("deleteme");
		w.commit("");
	}

	@Test(expected = RuntimeException.class)
	public final void whenNullStringIsPassedForCIDThenExceptionIsThrown() {
		Writer w = new Writer("deleteme");
		w.setCid("");
	}
	
	@Test(expected = RuntimeException.class)
	public final void whenCIDIsNotSetWritingCausesException() {
		Writer w = new Writer("deleteme");
		w.commit("test message");
	}
	
	@Test(expected = RuntimeException.class)
	public final void whenCIDIsNullThenFormattingCausesException() {
		Writer w = new Writer("deleteme");
		w.formatLine("booger");
	}
	
	@Test
	public final void whenCIDIsSetThenMessageIsFormattedProperly() {
		Writer w = new Writer("deleteme");
		String cid = "a";
		String msg = "big row of chickens";
		w.setCid(cid);
		String fmt_msg = w.formatLine(msg);
		assertThat(fmt_msg, CoreMatchers.containsString(cid+":"));
		assertThat(fmt_msg, CoreMatchers.containsString(":"+msg));
		assertEquals("string [["+fmt_msg+"]] is the wrong length", cid.length() + msg.length() + 2 + 18, fmt_msg.length());
	}
	
	@Test
	public final void whenMessageIsCommittedTheFileGetsBigger() throws IOException {
		Writer w = new Writer("deleteme");
		File fh = new File("deleteme");
		FileWriter fileWriter = new FileWriter(fh);
		assertTrue("Writer failed to create log file", fh.exists());
		assertEquals("Log file not empty at beginning", 0, fh.length());
		String cid = "b";
		String msg = "quick brown fox on a bench";
		w.setCid(cid);
		w.commit(msg);
		File fh1 = new File("deleteme");
		assertEquals("Message was not written to log file", msg.length() + 23, fh1.length());
	}
	
	@Test
	public final void whenTwoMessagesAreCommittedFileGetsBigger() throws IOException {
		Writer w = new Writer("deleteme");
		File fh = new File("deleteme");
		FileWriter fileWriter = new FileWriter(fh);
		assertEquals("Log file not empty at beginning", 0, fh.length());
		String cid = "c";
		String msg_0 = "it was a dark and stormy night";
		String msg_1 = "and the rain was lightly falling";
		w.setCid(cid);
		w.commit(msg_0);
		w.commit(msg_1);
		File fh1 = new File("deleteme");
		assertEquals("Messages were not written to log file", msg_0.length() + msg_1.length() + 46, fh1.length());
	}
	
	@Test
	public final void whenSpecialCharactersArePassedInTheyAreHandled() {
		Writer w = new Writer("deleteme");
		String msg_0 = "two paths \r\ndiverged\r\nin the woods";
		w.setCid("a");
		String ret = w.formatLine(msg_0);
		assertThat("Did not replace carriage returns in message",
				ret, CoreMatchers.not(CoreMatchers.containsString("\r")));
		assertThat("Did not replace newlines returns in message",
				ret, CoreMatchers.not(CoreMatchers.containsString("\n")));
		assertThat("Did not replace newlines and carriage returns in message with correct substitutions",
				ret, CoreMatchers.containsString("<cr>"));

	}
}
