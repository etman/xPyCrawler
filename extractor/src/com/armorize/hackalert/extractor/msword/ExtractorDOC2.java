/* Copyright (C) 2003 Internet Archive.
 *
 * This file is part of the Heritrix web crawler (crawler.archive.org).
 *
 * Heritrix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Heritrix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Heritrix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Created on Jul 7, 2003
 *
 */
package com.armorize.hackalert.extractor.msword;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.io.IOUtils;
import org.apache.nutch.parse.msword.MSWordParser;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.archive.crawler.datamodel.CoreAttributeConstants;
import org.archive.crawler.datamodel.CrawlURI;
import org.archive.crawler.extractor.Extractor;
import org.archive.crawler.extractor.Link;

/**
 * This class migrates MS Word extractor from nutch
 * 
 * @author Tim Chang
 * 
 */
public class ExtractorDOC2 extends Extractor implements CoreAttributeConstants {

	private static final long serialVersionUID = 1896822554981116303L;

	private static Logger logger = Logger.getLogger(ExtractorDOC2.class
			.getName());

	/**
	 * Regex pattern to get URLs within a plain text.
	 * 
	 * @see <a
	 *      href="http://www.truerwords.net/articles/ut/urlactivation.html">http://www.truerwords.net/articles/ut/urlactivation.html

	 *      </a>
	 */
	private static final String URL_PATTERN = "([A-Za-z][A-Za-z0-9+.-]{1,120}:[A-Za-z0-9/](([A-Za-z0-9$_.+!*,;/?:@&~=-])|%[A-Fa-f0-9]{2}){1,333}(#([a-zA-Z0-9][a-zA-Z0-9$_.+!*,;/?:@&~=%-]{0,1000}))?)";

	/**
	 * @param name
	 */
	public ExtractorDOC2(String name) {
		super(name, "MS-Word document Extractor. Extracts links from MS-Word"
				+ " '.doc' documents.");
	}

	/**
	 * Processes a word document and extracts any hyperlinks from it. This only
	 * extracts href style links, and does not examine the actual text for valid
	 * URIs.
	 * 
	 * @param curi
	 *            CrawlURI to process.
	 */
	protected void extract(CrawlURI curi) {
		// Assumes docs will be coming in through http.
		// TODO make this more general (currently we're only fetching via http
		// so it doesn't matter)
		if (!isHttpTransactionContentToProcess(curi)
				|| !isExpectedMimeType(curi.getContentType(),
						"application/msword")) {
			return;
		}

		MSWordParser parser = new MSWordParser();
		try {
			byte[] raw = IOUtils.toByteArray(curi.getHttpRecorder()
					.getRecordedInput().getContentReplayInputStream());
			String text = parser.getParse(raw);

			extractOutlinks(curi, text);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MalformedPatternException e) {
			e.printStackTrace();
		}
        curi.linkExtractorFinished(); // Set flag to indicate that link extraction is completed.
	}

	public void extractOutlinks(CrawlURI curi, final String plainText)
			throws MalformedPatternException {
		long start = System.currentTimeMillis();

		final PatternCompiler cp = new Perl5Compiler();
		final Pattern pattern = cp.compile(URL_PATTERN,
				Perl5Compiler.CASE_INSENSITIVE_MASK
						| Perl5Compiler.READ_ONLY_MASK
						| Perl5Compiler.MULTILINE_MASK);
		final PatternMatcher matcher = new Perl5Matcher();

		final PatternMatcherInput input = new PatternMatcherInput(plainText);

		MatchResult result;
		String url;

		// loop the matches
		while (matcher.contains(input, pattern)) {
			// if this is taking too long, stop matching
			// (SHOULD really check cpu time used so that heavily loaded
			// systems
			// do not unnecessarily hit this limit.)
			if (System.currentTimeMillis() - start >= 60000L) {
				if (logger.isLoggable(Level.WARNING)) {
					logger.warning("Time limit exceeded for getOutLinks");
				}
				break;
			}
			result = matcher.getMatch();
			url = result.group(0);
			try {
				curi.createAndAddLink(url, Link.NAVLINK_MISC, Link.NAVLINK_HOP);
			} catch (URIException e) {
				getController().logUriError(e, curi.getUURI(), url);
				if (getController() != null) {
					// Controller can be null: e.g. when running
					// ExtractorTool.
					getController().logUriError(e, curi.getUURI(), url);
				} else {
					logger.info(curi + ", " + url + ": "
							+ e.getMessage());
				}
			}
		}

	}
}
