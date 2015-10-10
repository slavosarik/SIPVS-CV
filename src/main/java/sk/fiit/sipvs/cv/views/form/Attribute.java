package sk.fiit.sipvs.cv.views.form;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Attribute {

	private String name;
	private String defaultValue;
	private Map<String, String> values;
	private JComboBox<String> comboBox;
	
	public Attribute(String name, String defaultValue) {
		this.name = name;
		this.defaultValue = defaultValue;
		this.values = new HashMap<String, String>();
	}
	
	public Attribute(Attribute otherAttr) {
		this.name = otherAttr.name;
		this.defaultValue = otherAttr.defaultValue;
		this.values = new HashMap<String, String>(otherAttr.values);
	}
	
	public void add(String value, String title) {
		this.values.put(value, title);
	}
	
	public int paint(JComponent parent, int depth, int index) {
		if (this.comboBox == null) {
			this.comboBox = new JComboBox<String>();
			for (String str : this.values.values()) {
				this.comboBox.addItem(str);
			}
		}
		parent.add(this.comboBox);
		this.setBounds(this.comboBox, depth, index, 200, 100);
		
		return index;
	}
	
	public void setDefaultValues() {
		comboBox.setSelectedItem(this.values.get(this.defaultValue));
	}
	
	public void getXML(Document doc, Element parentNode) {
		Attr attr = doc.createAttribute(this.name);
		
		String selectedItem = (String) this.comboBox.getSelectedItem();
		for (String str : this.values.keySet()) {
			if (this.values.get(str) == selectedItem) {
				attr.setValue(str);
				parentNode.setAttributeNode(attr);	
			}
		}
	}

	private void setBounds(JComponent component, int depth, int index, int width, int xoffset) {
		component.setBounds(32 + depth * 32 + xoffset, 20 + index * (24 + 2), width, 24);
	}
	
}
