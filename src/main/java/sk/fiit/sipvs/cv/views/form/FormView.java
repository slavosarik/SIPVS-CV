package sk.fiit.sipvs.cv.views.form;

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
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		String nowAsISO = df.format(new Date());

		parentNode = new Node(null, "biography", "", false, true);
			parentNode.add(new HiddenNode(parentNode, "date_created", nowAsISO));
			parentNode.add(new HiddenNode(parentNode, "place", "Bratislava"));

			Node personalInfo = new Node(parentNode, "personal_info", "Personal info", false, true);
				personalInfo.add(new TextNode(parentNode, "title_before", "Title before", "Ing.", true, true));	
				personalInfo.add(new TextNode(parentNode, "first_name", "First name", "Branislav", false, true));
				personalInfo.add(new TextNode(parentNode, "surname", "Surname", "Široký", false, true));
				personalInfo.add(new TextNode(parentNode, "title_after", "Title after", "PhD.", true, true));
				personalInfo.add(new TextNode(parentNode, "birth_date", "Birth date", "1969-10-29", false, true));
			parentNode.add(personalInfo);

			Node contactInfo = new Node(parentNode, "contact_info", "Contact info", false, true);
				contactInfo.add(new TextNode(parentNode, "email", "Email", "branislav@siroky.sk", false, true));
				contactInfo.add(new TextNode(parentNode, "phone", "Phone", "+421 903 940 397", false, true));

				Node address = new Node(parentNode, "address", "Address", false, true);
					address.add(new TextNode(parentNode, "country", "Country", "Slovakia", true, true));
					address.add(new TextNode(parentNode, "city", "City", "Bratislava", true, true));
					address.add(new TextNode(parentNode, "street", "Street", "Ilkovičova 2", true, true));
					address.add(new TextNode(parentNode, "postal_code", "Postal code", "84104", true, true));
				contactInfo.add(address);
			parentNode.add(contactInfo);
			
			Node education = new Node(parentNode, "education", "Education", false, true, 1, 999);
				Node school = new Node(parentNode, "school", "School", false, true);
					school.add(new TextNode(parentNode, "name", "Name", "Slovenská Technická Univerzita", false, true));
					school.add(new TextNode(parentNode, "faculty", "Faculty", "Fakulta Elektrotechniky a Informatiky", true, true));
					school.add(new TextNode(parentNode, "profession", "Profession", "Aplikovaná Informatika", false, true));
					school.add(new OptionNode(parentNode, "degree", "Degree", degreeOptions, "isced_2011_7"));
					school.add(new TextNode(parentNode, "start_date", "Start date", "1990-09-21", false, true));
					school.add(new TextNode(parentNode, "end_date", "End date", "1995-06-30", true, true));
				education.add(school);
			parentNode.add(education);
		
			Node courses = new Node(parentNode, "courses", "Courses", true, true, 0, 999);
				Node course1 = new Node(parentNode, "course", "Course", courseType0);
					course1.add(new TextNode(parentNode, "organization", "Organization", "Tunelárska elektrika, spol. s r.o.", false, true));
					course1.add(new TextNode(parentNode, "name", "Name", "Elektrotechnická spôsobilosť §22", false, true));
					course1.add(new TextNode(parentNode, "valid_from", "Valid from", "2010-02-27", false, true));
					course1.add(new TextNode(parentNode, "valid_to", "Valid to", "2015-02-27", true, true));
				courses.add(course1);
				Node course2 = new Node(parentNode, "course", "Course", courseType1);
					course2.add(new TextNode(parentNode, "organization", "Organization", "CISCO Network Academy", false, true));
					course2.add(new TextNode(parentNode, "name", "Name", "CCNP", false, true));
					course2.add(new TextNode(parentNode, "valid_from", "Valid from", "2013-02-27", false, true));
					course2.add(new TextNode(parentNode, "valid_from", "Valid to", "", true, false));
				courses.add(course2);
			parentNode.add(courses);
			
			Node career = new Node(parentNode, "career", "Career", true, true, 0, 999);
				Node experience = new Node(parentNode, "experience", "Experience", false, true);
					experience.add(new TextNode(parentNode, "employer", "Employer", "Fakulta Informatiky a Informačných technológií", false, true));
					experience.add(new TextNode(parentNode, "profession", "Profession", "Systémová administrácia", false, true));
					experience.add(new TextNode(parentNode, "description", "Description", "man bash", true, true));
					experience.add(new TextNode(parentNode, "start_date", "Start date", "1996-07-30", false, true));
					experience.add(new TextNode(parentNode, "end_date", "End date", "", true, false));
					Node projects = new Node(parentNode, "projects", "Projects", false, true, 0, 999);
						Node project = new Node(parentNode, "project", "Project", false, true);
							project.add(new TextNode(parentNode, "name", "Name", "Digitálna FIIT", false, true));
							project.add(new TextNode(parentNode, "description", "Description", "curl XGET http://fiit.stuba.sk", false, true));
							project.add(new TextNode(parentNode, "role", "Role", "Decision maker", false, true));
						projects.add(project);
					experience.add(projects);
				career.add(experience);
			parentNode.add(career);
			
			Node skills = new Node(parentNode, "skills", "Skills", false, true, 0, 999);
				Node skill1 = new Node(parentNode, "skill", "Skill", false, true);
					skill1.add(new TextNode(parentNode, "name", "Name", "Bash", false, true));
					skill1.add(new OptionNode(parentNode, "level", "Level", skillLevelOptions, "expert"));
					skill1.add(new TextNode(parentNode, "years", "Years", "28", false, true));
				skills.add(skill1);
				Node skill2 = new Node(parentNode, "skill", "Skill", false, true);
					skill2.add(new TextNode(parentNode, "name", "Name", "C", false, true));
					skill2.add(new OptionNode(parentNode, "level", "Level", skillLevelOptions, "expert"));
					skill2.add(new TextNode(parentNode, "years", "Years", "22", false, true));
				skills.add(skill2);
				Node skill3 = new Node(parentNode, "skill", "Skill", false, true);
					skill3.add(new TextNode(parentNode, "name", "Name", "C#", false, true));
					skill3.add(new OptionNode(parentNode, "level", "Level", skillLevelOptions, "beginner"));
					skill3.add(new TextNode(parentNode, "years", "Years", "2", false, true));
				skills.add(skill3);
			parentNode.add(skills);

		int createdRows = parentNode.paint(panel, 0, 0);
		panel.setPreferredSize(new Dimension(720 - 128, createdRows * 24 + 192));
	}


}
