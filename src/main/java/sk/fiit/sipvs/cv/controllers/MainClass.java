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

import sk.fiit.sipvs.cv.form.FormView;

public class MainClass {

	private static FormView formView;
	
	public static void main(String[] args) throws IOException {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {}

		formView = new FormView();
		
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
				formView.show();
			}
		});	

		/*System.out.println("Hello world!");

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

		System.out.println(sb.toString());*/
	}

	private static JButton getButton(String name, int position) {
		JButton button = new JButton(name);
		button.setBounds(32, 32 + position * 48, 200, 40);

		return button;
	}
	
}
