package sk.fiit.sipvs.cv.controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.UIManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.Transform;

import sk.fiit.sipvs.cv.views.form.FormView;
import sk.fiit.sipvs.cv.views.transform.TransformView;

public class MainClass {

	private static FormView formView;
	private static TransformView transformView;
	
	public static void main(String[] args) throws IOException {
		
		final Logger logger = LogManager.getLogger(MainClass.class.getName());
		
		logger.info("Starting app...");
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {}

		
		JFrame window = new JFrame();
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
		
		JButton formButton2 = getButton("Transform", 1);
		window.add(formButton2);
		formButton2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				transformView = new TransformView();
				transformView.show();
			}
		});
		

		logger.info("App started");
		
		/*
		InputStream is = ClassLoader.getSystemResourceAsStream("valid_example.xml");
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		String line;

		br = new BufferedReader(new InputStreamReader(is));
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}

		if (br != null) {
			br.close();
		}

		System.out.println(sb.toString());
		*/
	}

	private static JButton getButton(String name, int position) {
		JButton button = new JButton(name);
		button.setBounds(32, 32 + position * 48, 200, 40);

		return button;
	}
	
}
