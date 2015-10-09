package sk.fiit.sipvs.cv.views.transform;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class TransformView {

	private JFrame window;

	public TransformView() {
		// Window
		window = new JFrame();
		window.setTitle("Transform");
		window.setBounds(100, 100, 665, 390);
		window.getContentPane().setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 11, 630, 262);
		window.getContentPane().add(scrollPane);

		final JTextArea textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);

		JButton btnSourceFile = new JButton("Source file");
		btnSourceFile.setBounds(353, 287, 89, 23);
		window.getContentPane().add(btnSourceFile);

		JButton btnXSLFile = new JButton("XSL file");
		btnXSLFile.setBounds(353, 321, 89, 23);
		window.getContentPane().add(btnXSLFile);

		final JLabel lblSourceFile = new JLabel("No file was chosen");
		lblSourceFile.setBounds(20, 291, 323, 14);
		window.getContentPane().add(lblSourceFile);

		final JLabel lblXSLFile = new JLabel("No file was chosen");
		lblXSLFile.setBounds(20, 325, 323, 14);
		window.getContentPane().add(lblXSLFile);

		JButton btnTransform = new JButton("Transform");
		btnTransform.setBounds(452, 321, 89, 23);
		window.getContentPane().add(btnTransform);

		JButton btnSave = new JButton("Save");
		btnSave.setBounds(551, 321, 89, 23);
		window.getContentPane().add(btnSave);

		final JFileChooser sourceFileDialog = new JFileChooser();
		btnSourceFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int returnVal = sourceFileDialog.showOpenDialog(window);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = sourceFileDialog.getSelectedFile();
					textArea.setText("Source file Selected :" + file.getName());
					lblSourceFile.setText(file.getName());
				} else {
					textArea.setText("Open source file command cancelled by user.");
					lblSourceFile.setText("No file was chosen");
				}
			}
		});

		final JFileChooser xSLFileDialog = new JFileChooser();
		btnXSLFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int returnVal = xSLFileDialog.showOpenDialog(window);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = xSLFileDialog.getSelectedFile();
					textArea.setText("XSL file Selected :" + file.getName());
					lblXSLFile.setText(file.getName());
				} else {
					textArea.setText("Open XSL file command cancelled by user.");
					lblXSLFile.setText("No file was chosen");
				}
			}
		});

		btnTransform.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				textArea.setText("Transformation started...");

			}
		});

		final JFileChooser saveTransformed = new JFileChooser();
		btnSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int returnVal = saveTransformed.showSaveDialog(window);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = saveTransformed.getSelectedFile();
					textArea.setText("File location selected :" + file.getName());
				}
			}
		});

	}

	public void show() {
		window.setVisible(true);
		window.repaint();
	}
}
