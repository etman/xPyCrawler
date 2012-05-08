package internal.dummy;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.archive.modules.extractor.jsexecutor.HTMLParser;
import org.archive.util.IoUtils;
import org.lobobrowser.html.HtmlRendererContext;
import org.lobobrowser.html.UserAgentContext;
import org.lobobrowser.html.domimpl.HTMLDocumentImpl;
import org.lobobrowser.html.gui.HtmlPanel;
import org.lobobrowser.html.io.WritableLineReader;
import org.lobobrowser.html.test.SimpleHtmlRendererContext;
import org.lobobrowser.html.test.SimpleUserAgentContext;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class CobraExtractor {

	public static void main(String[] args) throws Exception {
		String sUrl = "http://www.guess.com/worldofguess/";
		
		URL url = new URL(sUrl);
		String content = IoUtils.readFullyAsString(url.openStream());

		SimpleUserAgentContext userAgentCtx = new SimpleUserAgentContext();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		WritableLineReader wis = new WritableLineReader(new StringReader(
				content));
		Document document = new HTMLDocumentImpl(userAgentCtx, null, wis,
				sUrl);

		HTMLParser parser = new HTMLParser(userAgentCtx, document);

		parser.parse(wis);

		// if (document != null) {
		// discoverNewLinks(uri, document, handledUris);
		//            
		// if (shouldSimHTMLEvents()) {
		// handleHTMLEvents1(uri, document, handledUris, contents);
		// }
		//            
		// for (Link wref: uri.getOutLinks()) {
		// System.out.println(wref.getDestination());
		// }
		// //uri.setFetchStatus(251);
		// uri.getData().put("ExecuteJS", "done");
		// logger.info("ExecuteJS: " + uri.getUURI().toString());
		// }

		if (true)
			return;
		try {
			long t0 = System.nanoTime();
			HtmlPanel panel = new HtmlPanel();
			HAHtmlRendererContext ctx = new HAHtmlRendererContext(panel,
					new SimpleUserAgentContext());
			ctx.navigate("http://guess.com/");
			ctx.getHtmlDocument();

			long t1 = System.nanoTime();
			System.out.println(TimeUnit.NANOSECONDS.toMillis(t1 - t0));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static class HAHtmlRendererContext extends
			SimpleHtmlRendererContext {
		private HTMLDocumentImpl doc = null;

		public HAHtmlRendererContext(HtmlPanel contextComponent,
				HtmlRendererContext parentRcontext) {
			super(contextComponent, parentRcontext);
		}

		public HAHtmlRendererContext(HtmlPanel contextComponent,
				UserAgentContext ucontext) {
			super(contextComponent, ucontext);
		}

		@Override
		protected HTMLDocumentImpl createDocument(InputSource inputSource)
				throws IOException, SAXException {
			this.doc = super.createDocument(inputSource);
			return this.doc;
		}

		public HTMLDocumentImpl getHtmlDocument() {
			return this.doc;
		}
	}
}
