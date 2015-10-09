package sk.fiit.sipvs.cv.views.form;

import javax.swing.JComponent;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class HiddenNode extends Node {

	private String value;
	
	public HiddenNode(String name, String value) {
		super(name, "");

		this.value = value;
	}
	
	public int createWidgets(JComponent parent, int depth, int index) { return index; }
	public void setDefaultValues() {}

	public void getXML(Document doc, Element parentNode) {
		Element element = doc.createElement(this.name);
		element.appendChild(doc.createTextNode(this.value));
		parentNode.appendChild(element);
	}
}
