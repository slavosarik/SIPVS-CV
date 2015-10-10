package sk.fiit.sipvs.cv.views.form;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TextNode extends Node {

	private String defaultValue;
	private JTextField textField;
	private JCheckBox checkBox;
	
	public TextNode(String name, String title, String defaultValue, boolean optional, boolean defaultEnabled) {
		super(name, title, optional, defaultEnabled);

		this.defaultValue = defaultValue;
	}

	public int createWidgets(JComponent parent, int depth, int index) {
		JLabel label = new JLabel(this.title);
		this.setBounds(label, depth, index, 100, 0);
		parent.add(label);
		
		textField = new JTextField();
		this.setBounds(textField, depth, index, 300, 100);
		parent.add(textField);
		
		if (this.optional) {
			checkBox = new JCheckBox();
			this.setBounds(checkBox, depth, index, 24, 400);
			checkBox.setSelected(this.defaultEnabled);
			parent.add(checkBox);
		}

		return index + 1;
	}
	
	public void setDefaultValues() {
		textField.setText(this.defaultValue);
	}
	
	public void getXML(Document doc, Element parentNode) {
		if (!this.optional || this.checkBox.isSelected()) {
			Element element = doc.createElement(this.name);
			element.appendChild(doc.createTextNode(this.textField.getText()));
			parentNode.appendChild(element);			
		}
	}

}
