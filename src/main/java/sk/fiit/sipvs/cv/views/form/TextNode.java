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
	private JLabel label;
	
	public TextNode(Node parent, String name, String title, String defaultValue, boolean optional, boolean defaultEnabled) {
		super(parent, name, title, optional, defaultEnabled);

		this.defaultValue = defaultValue;
	}
	
	public TextNode(TextNode otherNode) {
		super(otherNode);
		
		this.defaultValue = otherNode.defaultValue;
	}

	public int paint(JComponent parent, int depth, int index) {
		if (!this.visible) return index;
		
		if (this.label == null) {
			this.label = new JLabel(this.title);
		}
		parent.add(this.label);
		this.setBounds(this.label, depth, index, 100, 0);
		
		if (this.textField == null) {
			this.textField = new JTextField();
		}
		parent.add(this.textField);
		this.setBounds(this.textField, depth, index, 300, 100);
		
		if (this.optional) {
			if (this.checkBox == null) {
				this.checkBox = new JCheckBox();
				this.checkBox.setSelected(this.defaultEnabled);
			}
			parent.add(this.checkBox);
			this.setBounds(this.checkBox, depth, index, 24, 400);
		}

		return index + 1;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
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
