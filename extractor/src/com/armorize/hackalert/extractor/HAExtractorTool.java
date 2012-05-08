package com.armorize.hackalert.extractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.archive.crawler.datamodel.CoreAttributeConstants;
import org.archive.crawler.datamodel.CrawlOrder;
import org.archive.crawler.datamodel.CrawlURI;
import org.archive.crawler.extractor.Link;
import org.archive.crawler.framework.Processor;
import org.archive.crawler.framework.ToePool;
import org.archive.crawler.framework.ToeThread;
import org.archive.crawler.settings.CrawlerSettings;
import org.archive.crawler.settings.MapType;
import org.archive.crawler.settings.SettingsHandler;
import org.archive.crawler.settings.XMLSettingsHandler;
import org.archive.net.UURIFactory;
import org.archive.util.HttpRecorder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class HAExtractorTool {

	public static List<Processor> initExtractors(String[] e, String orderFileStr)
			throws AttributeNotFoundException, MBeanException,
			ReflectionException, IllegalArgumentException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, InvalidAttributeValueException,
			SecurityException, NoSuchMethodException, ClassNotFoundException {
		ArrayList<Processor> processors = new ArrayList<Processor>();

		File orderFile = new File(orderFileStr);
		SettingsHandler settingsHandler = new XMLSettingsHandler(orderFile);
		settingsHandler.initialize();
		// settingsHandler.getOrder().setAttribute(
		// new Attribute(CrawlOrder.ATTR_SETTINGS_DIRECTORY, "tmp/"));
		CrawlerSettings globalSettings = settingsHandler
				.getSettingsObject(null);
		MapType extractorsSettings = (MapType) settingsHandler.getOrder()
				.getAttribute(CrawlOrder.ATTR_EXTRACT_PROCESSORS);
		for (int i = 0; i < e.length; i++) {
			Constructor c = Class.forName(e[i]).getConstructor(
					new Class[] { String.class });
			String name = Integer.toString(i);
			Processor p = (Processor) c.newInstance(new Object[] { name });
			extractorsSettings.addElement(globalSettings, p);
			p.setAttribute(new Attribute(Processor.ATTR_ENABLED, Boolean.TRUE));
			processors.add(p);
		}
		return processors;
	}

	public static JSONArray extractLinks(HAExtractorParams params)
			throws AttributeNotFoundException, InvalidAttributeValueException,
			MBeanException, ReflectionException, IllegalArgumentException,
			SecurityException, InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException,
			ClassNotFoundException, IOException, InterruptedException {
		HttpRecorder recorder = null;
		try {
			String className = HAExtractorTool.class.getName();

			recorder = HttpRecorder.wrapInputStreamWithHttpRecord(new File(
					params.getWorkingFolder()), className, params
					.getInputStream(), null);

			CrawlURI curi = new CrawlURI(UURIFactory.getInstance(params
					.getURL()));
			curi.setContentSize(recorder.getRecordedInput().getSize());
			curi.setContentType(params.getContentType());
			curi.setFetchStatus(200);
			curi.setHttpRecorder(recorder);

			curi.putObject(CoreAttributeConstants.A_HTTP_TRANSACTION,
					new Object());

			List<Processor> extractors = initExtractors(new String[] {
					"com.armorize.hackalert.extractor.msword.ExtractorDOC2",
					"org.archive.crawler.extractor.ExtractorSWF",
					"org.archive.crawler.extractor.ExtractorHTML",
					"org.archive.crawler.extractor.ExtractorCSS",
					"org.archive.crawler.extractor.ExtractorJS",
					"org.archive.crawler.extractor.ExtractorPDF",
					"org.archive.crawler.extractor.ExtractorImpliedURI",
					"org.archive.crawler.extractor.ExtractorUniversal" },
					"order.xml");
			for (Processor proc : extractors) {
				proc.process(curi);
			}

			JSONArray result = new JSONArray();
			for (Link link : curi.getOutLinks()) {
				result.add(link.getDestination().toString());
			}
			return result;
		} finally {
			if (recorder != null) {
				recorder.cleanup();
			}
		}

	}

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		final String file = (args.length >= 1) ? args[0] : null;
		final boolean debug = StringUtils.equals("1", System
				.getProperty("debug"));

		final JSONObject summary = new JSONObject();
		summary.put("source", (file != null) ? "file" : "stdin");

		try {
			HAExtractorParams params = parseArgs(file);
			if (debug)
				summary.put("debug", params.toString());

			JSONArray result = extractLinks(params);
			summary.put("extracted-urls", result);
		} catch (Exception e) {
			StringWriter strOut = new StringWriter();
			PrintWriter out = new PrintWriter(strOut, true);
			e.printStackTrace(out);
			out.flush();
			summary.put("error", strOut.toString());
		}

		long elapsed = System.currentTimeMillis() - startTime;
		summary.put("cost", elapsed + "ms");
		System.out.println(summary.toJSONString());
	}

	public static HAExtractorParams parseArgs(String file) throws IOException {
		InputStream in = null;
		try {
			in = (file == null) ? System.in : new FileInputStream(file);
			HAExtractorParams params = HAExtractorParams.load(in);
			System.setProperty("haTempDir", params.getWorkingFolder());
			return params;
		} finally {
			if (System.in != in)
				IOUtils.closeQuietly(in);
		}
	}
}
