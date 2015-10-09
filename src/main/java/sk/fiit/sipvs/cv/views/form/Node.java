package sk.fiit.sipvs.cv.views.form;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Node {

	protected String name;
	protected String title;
	private List<Node> children;
	private Attribute attr;
	
	public Node(String name, String title) {
		this.name = name;
		this.title = title;
		this.children = new ArrayList<Node>();
		this.attr = null;
	}
	
	public Node(String name, String title, Attribute attr) {
		this.name = name;
		this.title = title;
		this.children = new ArrayList<Node>();
		this.attr = attr;
	}
	
	public void add(Node node) {
		this.children.add(node);
	}
	
	public int createWidgets(JComponent parent, int depth, int index) {
		int nextRow = index;
		
		if (this.title != "") {
			JLabel label = new JLabel(this.title);
			Font font = label.getFont();
			Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize() + 2 - depth);
			this.setBounds(label, depth, index, 100, 0);
			label.setFont(boldFont);
			parent.add(label);
			
			if (this.attr != null) {
				this.attr.createWidgets(parent, depth, nextRow);
			}
			
			nextRow += 1;
		}

		for (int i = 0; i < this.children.size(); i++) {
			nextRow = this.children.get(i).createWidgets(parent, depth + (this.title != "" ? 1 : 0), nextRow);
		}
		
		return nextRow;
	}
	
	public void setDefaultValues() {
		for (Node node : this.children) {
			node.setDefaultValues();
		}
		
		if (this.attr != null) {
			this.attr.setDefaultValues();
		}
	}
	
	public void getXML(Document doc, Element parentNode) {
		Element element = doc.createElement(this.name);
		
		for (Node node : this.children) {
			node.getXML(doc, element);
		}
		
		if (this.attr != null) {
			this.attr.getXML(doc, element);
		}

		if (parentNode == null) doc.appendChild(element);
		else parentNode.appendChild(element);
	}

	protected void setBounds(JComponent component, int depth, int index, int width, int xoffset) {
		component.setBounds(32 + depth * 32 + xoffset, 20 + index * (24 + 2), width, 24);
	}
	
}
