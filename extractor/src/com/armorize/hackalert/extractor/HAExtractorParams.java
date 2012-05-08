package com.armorize.hackalert.extractor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.archive.util.IoUtils;

public class HAExtractorParams {
	private Properties header = null;
	private InputStream in = null;

	private HAExtractorParams(Properties header, InputStream in) {
		this.header = header;
		this.in = in;
	}

	public String getURL() {
		return header.getProperty("url");
	}

	public String getContentType() {
		return header.getProperty("content.type");
	}

	public String getWorkingFolder() {
		return header.getProperty("working.folder");
	}

	public InputStream getInputStream() {
		return in;
	}

	public String toString() {
		String info = "URL[" + getURL() + "], ContentType[" + getContentType()
				+ "], WorkDir[" + getWorkingFolder() + "]";
		InputStream in = null;
		try {
			in = getInputStream();
			in.mark(20);
			info += "\n" + IOUtils.toString(getInputStream());
			in.reset();
			return info;
		} catch (IOException e) {
			StringWriter strOut = new StringWriter();
			PrintWriter out = new PrintWriter(strOut, true);
			e.printStackTrace(out);
			out.flush();
			info += "\n[Could not read InputStream]" + strOut.toString();
		}
		return info;
	}

	public static HAExtractorParams load(InputStream in) throws IOException {
		byte[] headerLength = new byte[10];
		IoUtils.readFully(in, headerLength);

		int length = Integer.parseInt(new String(headerLength));
		byte[] headerBuf = new byte[length];
		IoUtils.readFully(in, headerBuf);

		Properties header = new Properties();
		header.load(new ByteArrayInputStream(headerBuf));

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IOUtils.copy(in, out);
		return new HAExtractorParams(header, new ByteArrayInputStream(
				out.toByteArray()));
	}

}
