package sk.fiit.sipvs.cv.views.form;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Node {

	protected Node parent;
	protected String name;
	protected String title;
	protected boolean visible;
	private List<Node> children;
	private Attribute attr;
	protected boolean optional;
	protected boolean defaultEnabled;
	private int minChildren;
	private int maxChildren;
	
	private JCheckBox checkBox;	
	private JButton btnAdd;
	private JButton btnRemove;
	private JLabel label;
	
	public Node(Node parent, String name, String title, boolean optional, boolean defaultEnabled) {
		this.visible = true;
		this.parent = parent;
		this.name = name;
		this.title = title;
		this.children = new ArrayList<Node>();
		this.attr = null;
		this.optional = optional;
		this.defaultEnabled = defaultEnabled;
		this.minChildren = 1;
		this.maxChildren = 1;
	}
	
	public Node(Node parent, String name, String title, Attribute attr) {
		this.visible = true;
		this.parent = parent;
		this.name = name;
		this.title = title;
		this.children = new ArrayList<Node>();
		this.attr = attr;
		this.optional = false;
		this.defaultEnabled = true;
		this.minChildren = 1;
		this.maxChildren = 1;
	}
	
	public Node(Node parent, String name, String title, boolean optional, boolean defaultEnabled, int minChildren, int maxChildren) {
		this.visible = true;
		this.parent = parent;
		this.name = name;
		this.title = title;
		this.children = new ArrayList<Node>();
		this.attr = null;
		this.optional = optional;
		this.defaultEnabled = defaultEnabled;
		this.minChildren = minChildren;
		this.maxChildren = maxChildren;
	}
	
	public Node(Node otherNode) {
		this.visible = otherNode.visible;
		this.parent = otherNode.parent;
		this.name = otherNode.name;
		this.title = otherNode.title;
		
		this.children = new ArrayList<Node>();
		for (Node node : otherNode.children) {
			Node newNode = null;
			if (node.getClass() == Node.class) newNode = new Node(node);
			else if (node.getClass() == TextNode.class) newNode = new TextNode((TextNode) node);
			else if (node.getClass() == OptionNode.class) newNode = new OptionNode((OptionNode) node);
			else if (node.getClass() == HiddenNode.class) newNode = new HiddenNode((HiddenNode) node);
			this.children.add(newNode);
		}
		
		if (otherNode.attr != null) {
			this.attr = new Attribute(otherNode.attr);
		} else {
			this.attr = null;
		}

		this.optional = otherNode.optional;
		this.defaultEnabled = otherNode.defaultEnabled;
		this.minChildren = otherNode.minChildren;
		this.maxChildren = otherNode.maxChildren;
	}
	
	public void add(Node node) {
		this.children.add(node);
	}
	
	public int paint(final JComponent parent, int depth, int index) {
		if (!this.visible) return index;
		
		int nextRow = index;

		if (this.title != "") {
			if (this.label == null) {
				this.label = new JLabel(this.title);
				Font font = this.label.getFont();
				Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize() + 2 - depth);
				this.label.setFont(boldFont);
			}
			parent.add(this.label);
			this.setBounds(this.label, depth, index, 100, 0);
			
			if (this.attr != null) {
				this.attr.paint(parent, depth, nextRow);
			}
			
			if (this.optional) {
				if (this.checkBox == null) {
					this.checkBox = new JCheckBox();
					this.checkBox.setSelected(this.defaultEnabled);
				}
				parent.add(this.checkBox);
				this.setBounds(this.checkBox, depth, index, 24, 170);
			}
			
			if (this.minChildren != this.maxChildren) {
				if (this.btnAdd == null) {
					final Node that = this;

					this.btnAdd = new JButton("+");
					this.btnAdd.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if (that.children.size() == 1 && that.children.get(0).visible == false) {
								that.children.get(0).setVisible(true);
							} else {
								Node lastChild = that.children.get(that.children.size() - 1);
								Node clonedNode = new Node(lastChild);
								that.children.add(clonedNode);
							}
							
							parent.removeAll();
							int createdRows = that.parent.paint(parent, 0, 0);
							parent.setPreferredSize(new Dimension(720 - 128, createdRows * 24 + 192));
							parent.repaint();
						}
					});

					this.btnRemove = new JButton("-");
					this.btnRemove.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if (that.children.size() != that.minChildren) {
								if (that.children.size() == 1) {
									that.children.get(0).setVisible(false);
								} else {
									that.children.remove(that.children.size() - 1);
								}
								parent.removeAll();
								int createdRows = that.parent.paint(parent, 0, 0);
								parent.setPreferredSize(new Dimension(720 - 128, createdRows * 24 + 192));
								parent.repaint();
							}
						}
					});
				}
				parent.add(this.btnAdd);
				parent.add(this.btnRemove);
				this.setBounds(this.btnAdd, depth, index, 42, 72);
				this.setBounds(this.btnRemove, depth, index, 42, 118);
			}
			
			nextRow += 1;
		}

		for (int i = 0; i < this.children.size(); i++) {
			nextRow = this.children.get(i).paint(parent, depth + (this.title != "" ? 1 : 0), nextRow);
		}

		return nextRow;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
		for (int i = 0; i < this.children.size(); i++) {
			this.children.get(i).setVisible(visible);
		}
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
		if (this.visible && (!this.optional || this.checkBox.isSelected())) {
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
	}

	protected void setBounds(JComponent component, int depth, int index, int width, int xoffset) {
		component.setBounds(32 + depth * 32 + xoffset, 20 + index * (24 + 2), width, 24);
	}
	
}
