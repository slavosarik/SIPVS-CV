package sk.fiit.sipvs.cv.form;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

public class FormView {

	private JFrame window;
	private JScrollPane scrollPane;
	private JPanel panel;
	private Node parentNode;

	public FormView() {		
		// Window
		window = new JFrame();
		window.setTitle("Create XML");
		window.setBounds(100, 100, 740, 768);
		window.setLayout(null);
		
		// Form scroll panel
		panel = new JPanel(null);
		panel.setPreferredSize(new Dimension(720 - 128, 2000));
		scrollPane = new JScrollPane(panel);
		scrollPane.setBounds(32, 64, 720 - 64, 768 - 128);
		window.add(scrollPane);
		
		// Fill fields button
		JButton fillFields = new JButton("Fill fields");
		fillFields.setBounds(32, 20, 100, 24);
		window.add(fillFields);
		fillFields.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parentNode.setDefaultValues();
			}
		});
		
		// Save XML button
		JButton saveXML = new JButton("Save XML");
		saveXML.setBounds(720 - 100 - 32, 20, 100, 24);
		window.add(saveXML);
		saveXML.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
					
					Document doc = docBuilder.newDocument();
					parentNode.getXML(doc, null);
					
					TransformerFactory transformerFactory = TransformerFactory.newInstance();
					Transformer transformer = transformerFactory.newTransformer();
					transformer.setOutputProperty(OutputKeys.INDENT, "yes");
					transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
					DOMSource source = new DOMSource(doc);
					
					final JFileChooser fc = new JFileChooser();
					int returnVal = fc.showSaveDialog(window);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						StreamResult result = new StreamResult(file);
						transformer.transform(source, result);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		this.createFields();
	}
	
	public void show() {
		window.setVisible(true);
		scrollPane.repaint();
		window.repaint();
	}

	private void createFields() {
		Map<String, String> degreeOptions = new HashMap<String, String>();
		degreeOptions.put("isced_2011_8", "Doctoral or equivalent");
		degreeOptions.put("isced_2011_7", "Master or equivalent");
		degreeOptions.put("isced_2011_6", "Bachelor or equivalent");
		degreeOptions.put("isced_2011_5", "Short-cycle tertiary education");
		degreeOptions.put("isced_2011_4", "Post-secondary non-tertiary education");
		degreeOptions.put("isced_2011_3", "Upper secondary education");
		degreeOptions.put("isced_2011_2", "Lower secondary education");
		degreeOptions.put("isced_2011_1", "Primary education");

		Map<String, String> skillLevelOptions = new HashMap<String, String>();
		skillLevelOptions.put("expert", "Expert");
		skillLevelOptions.put("intermediate", "Intermediate");
		skillLevelOptions.put("beginner", "Beginner");
		
		Attribute courseType0 = new Attribute("type", "0");
		courseType0.add("1", "Certificate");
		courseType0.add("0", "Course");
		
		Attribute courseType1 = new Attribute("type", "1");
		courseType1.add("1", "Certificate");
		courseType1.add("0", "Course");
		
	    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
	    String nowAsISO = df.format(new Date());

		parentNode = new Node("biography", "");
			parentNode.add(new HiddenNode("date_created", nowAsISO));
			parentNode.add(new HiddenNode("place", "Bratislava"));

			Node personalInfo = new Node("personal_info", "Personal info");
				personalInfo.add(new TextNode("first_name", "First name", "Branislav"));
				personalInfo.add(new TextNode("surname", "Surname", "Široký"));
				personalInfo.add(new TextNode("birth_date", "Birth date", "1969-10-29"));
			parentNode.add(personalInfo);

			Node contactInfo = new Node("contact_info", "Contact info");
				contactInfo.add(new TextNode("email", "Email", "branislav@siroky.sk"));
				contactInfo.add(new TextNode("phone", "Phone", "+421 903 940 397"));

				Node address = new Node("address", "Address");
					address.add(new TextNode("country", "Country", "Slovakia"));
					address.add(new TextNode("city", "City", "Bratislava"));
					address.add(new TextNode("street", "Street", "Ilkovičova 2"));
					address.add(new TextNode("postal_code", "Postal code", "84104"));
				contactInfo.add(address);
			parentNode.add(contactInfo);
			
			Node education = new Node("education", "Education");
				Node school = new Node("school", "School");
					school.add(new TextNode("name", "Name", "Slovenská Technická Univerzita"));
					school.add(new TextNode("faculty", "Faculty", "Fakulta Elektrotechniky a Informatiky"));
					school.add(new TextNode("profession", "Profession", "Aplikovaná Informatika"));
					school.add(new OptionNode("degree", "Degree", degreeOptions, "isced_2011_7"));
					school.add(new TextNode("start_date", "Start date", "1990-09-21"));
					school.add(new TextNode("end_date", "End date", "1995-06-30"));
				education.add(school);
			parentNode.add(education);
		
			Node courses = new Node("courses", "Courses");
				Node course1 = new Node("course", "Course", courseType0);
					course1.add(new TextNode("organization", "Organization", "Tunelárska elektrika, spol. s r.o."));
					course1.add(new TextNode("name", "Name", "Elektrotechnická spôsobilosť §22"));
					course1.add(new TextNode("valid_from", "Valid from", "2010-02-27"));
					course1.add(new TextNode("valid_to", "Valid to", "2015-02-27"));
				courses.add(course1);
				Node course2 = new Node("course", "Course", courseType1);
					course2.add(new TextNode("organization", "Organization", "CISCO Network Academy"));
					course2.add(new TextNode("name", "Name", "CCNP"));
					course2.add(new TextNode("valid_from", "Valid from", "2013-02-27"));
				courses.add(course2);
			parentNode.add(courses);
			
			Node career = new Node("career", "Career");
				Node experience = new Node("experience", "Experience");
					experience.add(new TextNode("employer", "Employer", "Fakulta Informatiky a Informačných technológií"));
					experience.add(new TextNode("profession", "Profession", "Systémová administrácia"));
					experience.add(new TextNode("description", "Description", "man bash"));
					experience.add(new TextNode("start_date", "Start date", "1996-07-30"));
					Node projects = new Node("projects", "Projects");
						Node project = new Node("project", "Project");
							project.add(new TextNode("name", "Name", "Digitálna FIIT"));
							project.add(new TextNode("description", "Description", "curl XGET http://fiit.stuba.sk"));
							project.add(new TextNode("role", "Role", "Decision maker"));
						projects.add(project);
					experience.add(projects);
				career.add(experience);
			parentNode.add(career);
			
			Node skills = new Node("skills", "Skills");
				Node skill1 = new Node("skill", "Skill");
					skill1.add(new TextNode("name", "Name", "Bash"));
					skill1.add(new OptionNode("level", "Level", skillLevelOptions, "expert"));
					skill1.add(new TextNode("years", "Years", "28"));
				skills.add(skill1);
				Node skill2 = new Node("skill", "Skill");
					skill2.add(new TextNode("name", "Name", "C"));
					skill2.add(new OptionNode("level", "Level", skillLevelOptions, "expert"));
					skill2.add(new TextNode("years", "Years", "22"));
				skills.add(skill2);
				Node skill3 = new Node("skill", "Skill");
					skill3.add(new TextNode("name", "Name", "C#"));
					skill3.add(new OptionNode("level", "Level", skillLevelOptions, "beginner"));
					skill3.add(new TextNode("years", "Years", "2"));
				skills.add(skill3);
			parentNode.add(skills);
	
		int createdRows = parentNode.createWidgets(panel, 0, 0);
		panel.setPreferredSize(new Dimension(720 - 128, createdRows * 24 + 192));
	}


}
