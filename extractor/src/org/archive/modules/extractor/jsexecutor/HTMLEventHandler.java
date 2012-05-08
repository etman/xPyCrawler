package org.archive.modules.extractor.jsexecutor;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.lobobrowser.html.domimpl.HTMLAbstractUIElement;
import org.lobobrowser.html.renderer.HtmlController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Handle HTML events.
 * @author Ping Wang
 *
 */

public class HTMLEventHandler {
    
    private Document htmlDocument;
    
    public HTMLEventHandler(Document doc) {
        htmlDocument = doc;
    }
    
    /**
     * Simulate all onclick events
     */
    public void simMouseClick() {
        String xpathEvalExp = "//*[@" + "onclick" + "]";
        NodeList nodeList = getNodeList(xpathEvalExp);
        
        int listLength = nodeList.getLength();
        if (nodeList != null && listLength > 0) {
            for (int i = 0; i < listLength; i ++) {
                Element el = (Element)nodeList.item(i);
                if (el instanceof HTMLAbstractUIElement) {
                    HTMLAbstractUIElement uiElement = 
                        (HTMLAbstractUIElement) el;
                    HtmlController.getInstance().onMouseClick(uiElement, 
                            null, 0, 0);
                }
            }
        }
        
        xpathEvalExp = "html//a";
        nodeList = getNodeList(xpathEvalExp);
        listLength = nodeList.getLength();
        if (nodeList != null && listLength > 0) {
            for (int i = 0; i < listLength; i ++) {
                Element el = (Element)nodeList.item(i);
                if (el instanceof HTMLAbstractUIElement) {
                    HTMLAbstractUIElement uiElement = 
                        (HTMLAbstractUIElement) el;
                    HtmlController.getInstance().onMouseClick(uiElement, 
                            null, 0, 0);
                }
            }
        }
    }
    
    /**
     * Simulate all mouseover events
     */
    public void simMouseOver() {
        String xpathEvalExp = "//*[@" + "onmouseover" + "]";
        NodeList nodeList = getNodeList(xpathEvalExp);
        int listLength = nodeList.getLength();
        if (nodeList != null && listLength > 0) {
            for (int i = 0; i < listLength; i ++) {
                Element el = (Element)nodeList.item(i);
                if (el instanceof HTMLAbstractUIElement) {
                    HTMLAbstractUIElement uiElement = 
                        (HTMLAbstractUIElement) el;
                    HtmlController.getInstance().onMouseOver(uiElement, 
                            null, 0, 0, null);
                }
            }
        }
    }
    
    /**
     * Simulate all mouseout events
     */
    public void simMouseOut() {
        String xpathEvalExp = "//*[@" + "onmouseout" + "]";
        NodeList nodeList = getNodeList(xpathEvalExp);
        int listLength = nodeList.getLength();
        if (nodeList != null && listLength > 0) {
            for (int i = 0; i < listLength; i ++) {
                Element el = (Element)nodeList.item(i);
                if (el instanceof HTMLAbstractUIElement) {
                    HTMLAbstractUIElement uiElement = 
                        (HTMLAbstractUIElement) el;
                    HtmlController.getInstance().onMouseOut(uiElement, 
                            null, 0, 0, null);
                }
            }
        }
    }
    
    /**
     * Given a XPath expression, find nodes in a DOM 
     * @param evalExp an XPath expression
     * @return a list of nodes if found
     */
    private NodeList getNodeList(String evalExp) {
        NodeList nodeList = null;
        XPath xpath = XPathFactory.newInstance().newXPath();
        try {
            nodeList = (NodeList) xpath.evaluate(evalExp, 
                                                 htmlDocument, 
                                                 XPathConstants.NODESET);
            if (nodeList != null) {
                return nodeList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }        
        return nodeList;
    }
}
