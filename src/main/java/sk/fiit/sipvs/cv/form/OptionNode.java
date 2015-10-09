package sk.fiit.sipvs.cv.form;

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
	
	public OptionNode(String name, String title, Map<String, String> values, String defaultValue) {
		super(name, title);

		this.defaultValue = defaultValue;
		this.values = values;
	}
	
	public int createWidgets(JComponent parent, int depth, int index) {
		JLabel label = new JLabel(this.title);
		this.setBounds(label, depth, index, 100, 0);
		parent.add(label);
		
		comboBox = new JComboBox<String>();
		for (String str : this.values.values()) {
			comboBox.addItem(str);
		}
		this.setBounds(comboBox, depth, index, 300, 100);
		parent.add(comboBox);

		return index + 1;
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
