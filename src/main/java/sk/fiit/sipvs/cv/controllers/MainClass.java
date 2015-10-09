package sk.fiit.sipvs.cv.controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainClass {

	public static void main(String[] args) throws IOException {
		System.out.println("Hello world!");

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

	}

}
