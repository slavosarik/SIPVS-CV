package sk.fiit.sipvs.cv.views.form;

import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class OptionNode extends Node {

	private String defaultValue;
	private Map<String, String> values;
	private JComboBox<String> comboBox;
	private JLabel label;
	
	public OptionNode(Node parent, String name, String title, Map<String, String> values, String defaultValue) {
		super(parent, name, title, false, true);

		this.defaultValue = defaultValue;
		this.values = values;
	}
	
	public OptionNode(OptionNode otherNode) {
		super(otherNode);
		
		this.defaultValue = otherNode.defaultValue;
		this.values = otherNode.values;
	}
	
	public int paint(JComponent parent, int depth, int index) {
		if (!this.visible) return index;

		if (this.label == null) {
			this.label = new JLabel(this.title);
		}
		parent.add(this.label);
		this.setBounds(this.label, depth, index, 100, 0);

		if (this.comboBox == null) {
			this.comboBox = new JComboBox<String>();
			for (String str : this.values.values()) {
				this.comboBox.addItem(str);
			}
		}
		parent.add(this.comboBox);
		this.setBounds(this.comboBox, depth, index, 300, 100);

		return index + 1;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public void setDefaultValues() {
		comboBox.setSelectedItem(this.values.get(this.defaultValue));
	}
	
	public void getXML(Document doc, Element parentNode) {
		Element element = doc.createElement(this.name);
		
		String selectedItem = (String) this.comboBox.getSelectedItem();
		for (String str : this.values.keySet()) {
			if (this.values.get(str) == selectedItem) {
				element.appendChild(doc.createTextNode(str));
				parentNode.appendChild(element);
			}
		}
	}
	
}
