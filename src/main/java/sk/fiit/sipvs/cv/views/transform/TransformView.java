package sk.fiit.sipvs.cv.views.transform;

import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class TransformView {

	private JFrame window;
	private JScrollPane scrollPane;
	private JPanel panel;
	
	public TransformView() {		
		// Window
		window = new JFrame();
		window.setTitle("Transform");
		window.setBounds(100, 100, 740, 768);
		window.setLayout(null);
		
		// Form scroll panel
		panel = new JPanel(null);
		panel.setPreferredSize(new Dimension(720 - 128, 2000));
		scrollPane = new JScrollPane(panel);
		scrollPane.setBounds(32, 64, 720 - 64, 768 - 128);
		window.add(scrollPane);
		

	}
	
	public void show() {
		window.setVisible(true);
		scrollPane.repaint();
		window.repaint();
	}
	


}
