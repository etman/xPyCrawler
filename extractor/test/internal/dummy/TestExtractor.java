package internal.dummy;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

import org.apache.commons.io.IOUtils;
import org.archive.crawler.datamodel.CoreAttributeConstants;
import org.archive.crawler.datamodel.CrawlOrder;
import org.archive.crawler.datamodel.CrawlURI;
import org.archive.crawler.extractor.Link;
import org.archive.crawler.framework.Processor;
import org.archive.crawler.settings.CrawlerSettings;
import org.archive.crawler.settings.MapType;
import org.archive.crawler.settings.SettingsHandler;
import org.archive.crawler.settings.XMLSettingsHandler;
import org.archive.net.UURIFactory;
import org.archive.util.HttpRecorder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class TestExtractor {

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

	public static JSONArray extractLinks(String url, String contentType,
			String file) throws AttributeNotFoundException,
			InvalidAttributeValueException, MBeanException,
			ReflectionException, IllegalArgumentException, SecurityException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException,
			ClassNotFoundException, IOException, InterruptedException {
		InputStream in = null;
		HttpRecorder recorder = null;
		try {
			in = (file == null) ? System.in :
				new ByteArrayInputStream(IOUtils
					.toByteArray(new URL(url).openStream()));
			String className = TestExtractor.class.getName();

			recorder = HttpRecorder.wrapInputStreamWithHttpRecord(new File(
					"tmp/"), className, in, null);

			CrawlURI curi = new CrawlURI(UURIFactory.getInstance(url));
			curi.setContentSize(recorder.getRecordedInput().getSize());
			curi.setContentType(contentType);
			curi.setFetchStatus(200);
			curi.setHttpRecorder(recorder);

			curi.putObject(CoreAttributeConstants.A_HTTP_TRANSACTION,
					new Object());

			List<Processor> extractors = initExtractors(
					new String[] { 
							"com.armorize.hackalert.extractor.msword.ExtractorDOC2",
							"org.archive.crawler.extractor.ExtractorUniversal"
//							"org.archive.crawler.extractor.ExtractorHTML",
//							"org.archive.crawler.extractor.ExtractorJS"
//							"org.archive.modules.extractor.jsexecutor.ExecuteJS"
							},
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
			if (System.in != in)
				IOUtils.closeQuietly(in);
		}

	}

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		final String haTempDir = (args.length >= 1) ? args[0] : null;
		final String urlBase = (args.length >= 2) ? args[1] : null;
		final String contentType = (args.length >= 3) ? args[2] : null;
		final String file = (args.length >= 4) ? args[3] : null;

		final JSONObject summary = new JSONObject();
		summary.put("source", (file != null) ? "file" : "stdin");
		if (haTempDir == null) {
			summary.put("error", "temp folder not specified.");
		} else if (urlBase == null) {
			summary.put("error", "URL base not specified.");
		} else if (contentType == null) {
			summary.put("error", "Content type not specified.");
		}

		if (!summary.containsKey("error")) {
			System.setProperty("haTempDir", haTempDir);
			try {
				JSONArray result = extractLinks(urlBase, contentType, file);
				summary.put("extracted-urls", result);
			} catch (Exception e) {
				StringWriter strOut = new StringWriter();
				PrintWriter out = new PrintWriter(strOut, true);
				e.printStackTrace(out);
				out.flush();
				summary.put("error", strOut.toString());
			}
		}
		long elapsed = System.currentTimeMillis() - startTime;
		summary.put("cost", elapsed + "ms");
		System.out.println(summary.toJSONString());
		System.exit(0);
	}
}
