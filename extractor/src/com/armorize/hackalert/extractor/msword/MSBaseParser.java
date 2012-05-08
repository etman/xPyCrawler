/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.armorize.hackalert.extractor.msword;

// JDK imports
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import javax.management.Attribute;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.archive.crawler.datamodel.CoreAttributeConstants;
import org.archive.crawler.datamodel.CrawlOrder;
import org.archive.crawler.datamodel.CrawlURI;
import org.archive.crawler.extractor.ExtractorUniversal;
import org.archive.crawler.framework.Processor;
import org.archive.crawler.settings.CrawlerSettings;
import org.archive.crawler.settings.MapType;
import org.archive.crawler.settings.SettingsHandler;
import org.archive.crawler.settings.XMLSettingsHandler;
import org.archive.net.UURIFactory;
import org.archive.util.HttpRecorder;

/**
 * A generic Microsoft document parser.
 * 
 * @author J&eacute;r&ocirc;me Charron
 */
public abstract class MSBaseParser {

	protected static final Log LOG = LogFactory.getLog(MSBaseParser.class);

	public abstract String getParse(byte[] content);

	/**
	 * Parses a Content with a specific {@link MSExtractor Microsoft document
	 * extractor}.
	 */
	protected String getParse(MSExtractor extractor, byte[] raw) {
		try {
			extractor.extract(new ByteArrayInputStream(raw));
			return extractor.getText();
			// properties = extractor.getProperties();
			// System.out.println(properties);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Main for testing. Pass a ms document as argument
	 */
	public static void main(String mime, MSBaseParser parser, String args[]) {
		try {
			ExtractorUniversal extractor = new ExtractorUniversal("TEST");
			File orderFile = new File("order.xml");
			SettingsHandler settingsHandler = new XMLSettingsHandler(orderFile);
			settingsHandler.initialize();
			// settingsHandler.getOrder().setAttribute(
			// new Attribute(CrawlOrder.ATTR_SETTINGS_DIRECTORY, "tmp/"));
			CrawlerSettings globalSettings = settingsHandler
					.getSettingsObject(null);
			MapType extractorsSettings = (MapType) settingsHandler.getOrder()
					.getAttribute(CrawlOrder.ATTR_EXTRACT_PROCESSORS);

			extractorsSettings.addElement(globalSettings,extractor);
			extractor.setAttribute(new Attribute(Processor.ATTR_ENABLED, Boolean.TRUE));
			//======================
			byte[] raw = IOUtils.toByteArray(new FileInputStream(
					"C:\\temp\\userman.doc"));
			String text = parser.getParse(raw);
			ByteArrayInputStream in = new ByteArrayInputStream(text.getBytes("utf-8"));

			HttpRecorder recorder = HttpRecorder.wrapInputStreamWithHttpRecord(new File(
					"tmp"), MSBaseParser.class.getName(), in, "utf-8");
			
			CrawlURI curi = new CrawlURI(UURIFactory.getInstance("http://www.armorize.com/"));
			curi.setContentSize(recorder.getRecordedInput().getSize());
			curi.setContentType("application/msword");
			curi.setFetchStatus(200);
			curi.setHttpRecorder(recorder);

			curi.putObject(CoreAttributeConstants.A_HTTP_TRANSACTION,
					new Object());
			
			extractor.process(curi);
			System.out.println(curi.getOutLinks());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
