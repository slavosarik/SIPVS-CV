package sk.fiit.sipvs.cv.views.form;

import javax.swing.JComponent;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class HiddenNode extends Node {

	private String value;
	
	public HiddenNode(Node parent, String name, String value) {
		super(parent, name, "", false, true);

		this.value = value;
	}
	
	public HiddenNode(HiddenNode otherNode) {
		super(otherNode);
		
		this.value = otherNode.value;
	}
	
	public int paint(JComponent parent, int depth, int index) { return index; }
	public void setDefaultValues() {}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public void getXML(Document doc, Element parentNode) {
		Element element = doc.createElement(this.name);
		element.appendChild(doc.createTextNode(this.value));
		parentNode.appendChild(element);
	}
}
