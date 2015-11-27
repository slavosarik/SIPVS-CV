package sk.fiit.sipvs.cv.controllers;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.xml.xpath.XPathExpressionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import exceptions.SignVerificationException;
import sk.fiit.sipvs.cv.views.form.FormView;
import sk.fiit.sipvs.cv.views.sign.SignView;
import sk.fiit.sipvs.cv.views.transform.TransformView;
import sk.fiit.sipvs.cv.views.validation.ValidationView;

public class MainClass {

	// GUI constants
	public static final Integer VERTICAL_SPACING = 5;
	public static final Integer HORIZONTAL_SPACING = 10;

	// GUI strings
	public static final String MISSING_FILE = "Missing file";
	public static final String NO_FILE_CHOSEN = "No file chosen";
	public static final String SAVE_ERROR = "File not saved";

	private static FormView formView;
	private static TransformView transformView;
	private static ValidationView validationView;
	private static SignView signView;

	public static void main(String[] args) throws IOException {

		final Logger logger = LogManager.getLogger(MainClass.class.getName());

		logger.info("Starting app...");

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}

		final JFrame window = new JFrame();
		window.setTitle("SIPVS");
		window.setBounds(100, 100, 280, 400);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLayout(null);
		window.setVisible(true);

		JButton formButton = getButton("Create XML", 0);
		window.add(formButton);
		formButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				formView = new FormView();
				formView.show();
			}
		});

		JButton validateButton = getButton("Validate XML", 1);
		window.add(validateButton);
		validateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				validationView = new ValidationView();
				validationView.show();
			}
		});

		JButton transformButton = getButton("Transform XML", 2);
		window.add(transformButton);
		transformButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				transformView = new TransformView();
				transformView.show();
			}
		});

		JButton signButton = getButton("Sign XML", 3);
		window.add(signButton);
		signButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				signView = new SignView();
				signView.show();
			}
		});
		
		JButton verifyButton = getButton("Verify XML", 4);
		window.add(verifyButton);
		verifyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					VerifyController.verifyXmlSignedFile();
					JOptionPane.showMessageDialog(window, "Verification passed successfully.",
							"Verification success", JOptionPane.INFORMATION_MESSAGE);
				} catch (SignVerificationException e1) {
					JOptionPane.showMessageDialog(window, e1.getMessage(),
							"Verification error", JOptionPane.ERROR_MESSAGE);
				} catch (XPathExpressionException e1) {
					logger.error(e1.getStackTrace(), e1);
				}
			}
		});
		
		logger.info("App started");
	}

	public static JLabel createBoldLabel(String text) {
		JLabel label = new JLabel(text);
		Font font = label.getFont();
		Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize() + 2);
		label.setFont(boldFont);

		return label;
	}

	private static JButton getButton(String name, int position) {
		JButton button = new JButton(name);
		button.setBounds(32, 32 + position * 48, 200, 40);

		return button;
	}
}
