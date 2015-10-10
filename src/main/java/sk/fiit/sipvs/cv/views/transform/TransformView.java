package sk.fiit.sipvs.cv.views.transform;

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
import javax.xml.transform.TransformerException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sk.fiit.sipvs.cv.controllers.TransformController;

public class TransformView {

	private JFrame window;
	File fileOutput;
	File fileSource;
	File fileXSL;
	File workingDirectory = new File(System.getProperty("user.dir"));
	final Logger logger = LogManager.getLogger(TransformView.class.getName());

	public TransformView() {
		// Window
		window = new JFrame();
		window.setTitle("Transform");
		window.setBounds(100, 100, 665, 429);
		window.getContentPane().setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 11, 630, 262);
		window.getContentPane().add(scrollPane);

		final JTextArea textArea = new JTextArea();
		textArea.setEditable(false);
		Font font = new Font("Arial", Font.PLAIN, 14);
		textArea.setFont(font);
		scrollPane.setViewportView(textArea);

		JButton btnSourceFile = new JButton("Source file");
		btnSourceFile.setBounds(353, 287, 89, 23);
		window.getContentPane().add(btnSourceFile);

		JButton btnXSLFile = new JButton("XSL file");
		btnXSLFile.setBounds(353, 321, 89, 23);
		window.getContentPane().add(btnXSLFile);

		final JLabel lblSourceFile = new JLabel("No file was chosen");
		lblSourceFile.setBounds(10, 291, 323, 14);
		window.getContentPane().add(lblSourceFile);

		final JLabel lblXSLFile = new JLabel("No file was chosen");
		lblXSLFile.setBounds(10, 325, 188, 14);
		window.getContentPane().add(lblXSLFile);

		JButton btnTransform = new JButton("Transform");
		btnTransform.setBounds(551, 355, 89, 23);
		window.getContentPane().add(btnTransform);

		JButton btnOutputFile = new JButton("Output file");
		btnOutputFile.setBounds(353, 355, 89, 23);
		window.getContentPane().add(btnOutputFile);

		final JLabel lblOutput = new JLabel("No file was chosen");
		lblOutput.setBounds(10, 359, 323, 14);
		window.getContentPane().add(lblOutput);

		final JFileChooser sourceFileDialog = new JFileChooser();
		sourceFileDialog.setCurrentDirectory(workingDirectory);
		btnSourceFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int returnVal = sourceFileDialog.showOpenDialog(window);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					fileSource = sourceFileDialog.getSelectedFile();
					lblSourceFile.setText(fileSource.getName());
				} else {
					lblSourceFile.setText("No file was chosen");
					fileSource = null;
				}
			}
		});

		final JFileChooser xSLFileDialog = new JFileChooser();
		xSLFileDialog.setCurrentDirectory(workingDirectory);
		btnXSLFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int returnVal = xSLFileDialog.showOpenDialog(window);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					fileXSL = xSLFileDialog.getSelectedFile();
					lblXSLFile.setText(fileXSL.getName());
				} else {
					lblXSLFile.setText("No file was chosen");
					fileXSL = null;
				}
			}
		});

		btnTransform.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				if (fileXSL != null && fileSource != null) {

					textArea.setText("Transformation started...");
					TransformController tc = new TransformController();
					try {
						textArea.setText(tc.transform(fileSource, fileXSL, fileOutput));
					} catch (TransformerException e1) {
						logger.error("An error occured during transformation", e1);
					}
				} else {
					logger.error("No input file chosen");
					JOptionPane.showMessageDialog(window, "No input file chosen.", "File input error", JOptionPane.WARNING_MESSAGE);
				}

			}
		});

		final JFileChooser saveTransformed = new JFileChooser();
		saveTransformed.setCurrentDirectory(workingDirectory);
		btnOutputFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int returnVal = saveTransformed.showSaveDialog(window);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					fileOutput = saveTransformed.getSelectedFile();
					lblOutput.setText(fileOutput.getName());
				} else {
					lblOutput.setText("No file was chosen");
					fileOutput = null;
				}
			}
		});

	}

	public void show() {
		window.setVisible(true);
		window.repaint();
	}
}
