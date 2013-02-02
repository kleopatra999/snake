package com.gulshansingh.snake;

import javax.swing.JFrame;
import javax.swing.JWindow;

/**
 * Application entry point
 * 
 * @author Gulshan Singh
 * 
 */
public class Main {

	public static void main(String[] args) {
		new Main();
	}

	private Main() {
		JFrame frame = new JFrame("Snake");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		JWindow window = new JWindow(frame);

		Snake snake = new Snake(frame);
	}
}
