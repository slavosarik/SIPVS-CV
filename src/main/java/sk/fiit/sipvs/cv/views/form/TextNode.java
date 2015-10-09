package sk.fiit.sipvs.cv.views.form;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TextNode extends Node {

	private String defaultValue;
	private JTextField textField;
	
	public TextNode(String name, String title, String defaultValue) {
		super(name, title);

		this.defaultValue = defaultValue;
	}

	public int createWidgets(JComponent parent, int depth, int index) {
		JLabel label = new JLabel(this.title);
		this.setBounds(label, depth, index, 100, 0);
		parent.add(label);
		
		textField = new JTextField();
		this.setBounds(textField, depth, index, 300, 100);
		parent.add(textField);

		return index + 1;
	}
	
	public void setDefaultValues() {
		textField.setText(this.defaultValue);
	}
	
	public void getXML(Document doc, Element parentNode) {
		Element element = doc.createElement(this.name);
		element.appendChild(doc.createTextNode(this.textField.getText()));
		parentNode.appendChild(element);
	}

}
