package sk.fiit.sipvs.cv.views.validation;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sk.fiit.sipvs.cv.controllers.MainClass;
import sk.fiit.sipvs.cv.controllers.ValidationController;
import sk.fiit.sipvs.cv.models.ValidationResult;

/**
 * Displays window for XML file validation against an XML Schema Definition
 * (XSD) file.
 * 
 * @author Dusan Cymorek
 */
public class ValidationView {

	private static final Integer VERTICAL_SPACING = 5;
	private static final Integer HORIZONTAL_SPACING = 10;
	private static final String MISSING_FILE = "Missing file";
	private static final String MISSING_XML_FILE_USER = "XML file has not been chosen. Please choose the missing file and validate XML file again.";
	private static final String MISSING_XSD_FILE_USER = "XSD file has not been chosen. Please choose the missing file and validate XML file again.";
	private static final String MISSING_XML_FILE_LOGGER = "Validation error. Missing XML file.";
	private static final String MISSING_XSD_FILE_LOGGER = "Validation error. Missing XSD file.";

	private JFrame window;

	private JLabel xmlFilePathLabel;
	private JLabel xsdFilePathLabel;

	private final JFileChooser xmlFileChooser;
	private final JFileChooser xsdFileChooser;
	private final JTextArea validationTextArea;

	private File xmlFile;
	private File xsdFile;
	private File lastDirectory;

	private Logger logger = LogManager.getLogger(ValidationView.class.getName());

	public ValidationView() {
		// Window
		window = new JFrame();
		window.setTitle("Validate XML");
		window.setBounds(100, 100, 500, 400);
		window.getContentPane().setLayout(null);

		// File Chooser with Filters
		FileNameExtensionFilter xmlFilter = new FileNameExtensionFilter("eXtensible Markup Language files (.xml)",
				"xml");
		FileNameExtensionFilter xsdFilter = new FileNameExtensionFilter(
				"eXtensible Markup Language Schema Definition files (.xsd)", "xsd");

		xmlFileChooser = new JFileChooser();
		xmlFileChooser.setAcceptAllFileFilterUsed(false);
		xmlFileChooser.setFileFilter(xmlFilter);

		xsdFileChooser = new JFileChooser();
		xsdFileChooser.setAcceptAllFileFilterUsed(false);
		xsdFileChooser.setFileFilter(xsdFilter);

		// XML File
		final JLabel xmlFileLabel = createBoldLabel("XML File");
		xmlFileLabel.setBounds(32, 20, 150, 24);
		window.getContentPane().add(xmlFileLabel);

		JButton xmlFileBtn = new JButton("Choose file");
		xmlFileBtn.setBounds(32, 20 + 24 + VERTICAL_SPACING, 100, 24);
		window.getContentPane().add(xmlFileBtn);
		xmlFileBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				// Set current directory to last opened directory
				xmlFileChooser.setCurrentDirectory(lastDirectory);

				int returnVal = xmlFileChooser.showOpenDialog(window);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					xmlFile = xmlFileChooser.getSelectedFile();

					xmlFilePathLabel.setText(xmlFile.getName());

					lastDirectory = xmlFile;
				} else {
					xmlFilePathLabel.setText(MainClass.NO_FILE_CHOSEN);
					validationTextArea.setText(null);
					xmlFile = null;
				}
			}
		});

		xmlFilePathLabel = new JLabel(MainClass.NO_FILE_CHOSEN);
		xmlFilePathLabel.setBounds(32 + 100 + HORIZONTAL_SPACING, 20 + 24 + VERTICAL_SPACING, 200, 24);
		window.getContentPane().add(xmlFilePathLabel);

		// XSD File
		final JLabel xsdFileLabel = createBoldLabel("XML Schema File");
		xsdFileLabel.setBounds(32, 90, 150, 24);
		window.getContentPane().add(xsdFileLabel);

		JButton xsdFileBtn = new JButton("Choose file");
		xsdFileBtn.setBounds(32, 90 + 24 + VERTICAL_SPACING, 100, 24);
		window.getContentPane().add(xsdFileBtn);
		xsdFileBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				// Set current directory to last opened directory
				xsdFileChooser.setCurrentDirectory(lastDirectory);

				int returnVal = xsdFileChooser.showOpenDialog(window);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					xsdFile = xsdFileChooser.getSelectedFile();

					xsdFilePathLabel.setText(xsdFile.getName());

					lastDirectory = xsdFile;
				} else {
					xsdFilePathLabel.setText(MainClass.NO_FILE_CHOSEN);
					validationTextArea.setText(null);
					xsdFile = null;
				}
			}
		});

		xsdFilePathLabel = new JLabel(MainClass.NO_FILE_CHOSEN);
		xsdFilePathLabel.setBounds(32 + 100 + HORIZONTAL_SPACING, 90 + 24 + VERTICAL_SPACING, 200, 24);
		window.getContentPane().add(xsdFilePathLabel);

		// Validation output
		final JLabel validationLabel = createBoldLabel("XML Validation");
		validationLabel.setBounds(32, 90 + 2 * 24 + 4 * VERTICAL_SPACING, 150, 24);
		window.getContentPane().add(validationLabel);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(32, 90 + 4 * 24 + 7 * VERTICAL_SPACING, 480 - 64, 5 * 24);
		window.getContentPane().add(scrollPane);

		validationTextArea = new JTextArea();
		validationTextArea.setWrapStyleWord(true);
		validationTextArea.setLineWrap(true);
		validationTextArea.setEditable(false);
		validationTextArea.setFocusable(false);
		validationTextArea.setBackground(UIManager.getColor("Label.background"));
		validationTextArea.setFont(new Font("Arial", Font.PLAIN, 14));
		scrollPane.setViewportView(validationTextArea);

		JButton validationBtn = new JButton("Validate XML");
		validationBtn.setBounds(32, 90 + 3 * 24 + 5 * VERTICAL_SPACING, 100, 24);
		window.getContentPane().add(validationBtn);
		validationBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (xmlFile != null && xsdFile != null) {
					ValidationController vc = new ValidationController();
					ValidationResult result = vc.validateXML(xmlFile, xsdFile);

					if (result.getValidity()) {
						validationTextArea.setForeground(new Color(27, 128, 29));
						validationTextArea.setText(
								"XML File " + xmlFile.getName() + " is valid (XSD: " + xsdFile.getName() + ").");
					} else {
						validationTextArea.setForeground(Color.RED);
						validationTextArea.setText(
								"XML File " + xmlFile.getName() + " is not valid (XSD: " + xsdFile.getName() + ").\n");
						validationTextArea.append("Reason: " + result.getReason());
					}
				} else if (xmlFile == null) {
					logger.error(MISSING_XML_FILE_LOGGER);
					JOptionPane.showMessageDialog(window, MISSING_XML_FILE_USER, MISSING_FILE,
							JOptionPane.ERROR_MESSAGE);
				} else {
					logger.error(MISSING_XSD_FILE_LOGGER);
					JOptionPane.showMessageDialog(window, MISSING_XSD_FILE_USER, MISSING_FILE,
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}

	public void show() {
		window.setVisible(true);
		window.repaint();
	}

	private JLabel createBoldLabel(String text) {
		JLabel label = new JLabel(text);
		Font font = label.getFont();
		Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize() + 2);
		label.setFont(boldFont);

		return label;
	}
}
