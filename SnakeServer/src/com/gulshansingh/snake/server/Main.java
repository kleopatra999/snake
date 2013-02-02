package com.gulshansingh.snake.server;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Main {

	private ArrayList<PrintWriter> writers = new ArrayList<PrintWriter>();
	private ArrayList<ClientHandler> handlers = new ArrayList<ClientHandler>();

	private int connections = 0;

	public enum Direction {
		UP, DOWN, RIGHT, LEFT
	}

	public static void main(String[] args) {
		new Main();
	}

	private Main() {
		// Wait for client connections
		listenForConnections();
	}

	private void listenForConnections() {
		try {
			ServerSocket socket = new ServerSocket(35267);
			while (true) {
				Socket clientSocket = socket.accept();
				PrintWriter writer = new PrintWriter(
						clientSocket.getOutputStream());
				writers.add(writer);
				ClientHandler handler = new ClientHandler(clientSocket);
				new Thread(handler).start();
				handlers.add(handler);
				connections++;

				if (connections == 2) {
					break;
				}
			}

			Dimension dimension1 = handlers.get(0).getDimension();
			Dimension dimension2 = handlers.get(1).getDimension();

			int width = dimension1.width + dimension2.width;
			int height = Math.max(dimension1.height, dimension2.height);
			tellAll(width);
			tellAll(height);

			PrintWriter writer = writers.get(0);
			dimension1 = handlers.get(0).getDimension();
			writer.write(0);
			writer.write(dimension1.width);
			writer.write(dimension1.height);
			writer.flush();

			writer = writers.get(1);
			writer.write(dimension2.width);
			writer.write(dimension2.width);
			writer.write(dimension2.height);
			writer.flush();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	class ClientHandler implements Runnable {
		private BufferedReader reader;
		private Dimension dimension;

		public ClientHandler(Socket socket) {
			try {
				reader = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public Dimension getDimension() {
			return dimension;
		}

		@Override
		public void run() {
			String message;
			boolean sizeReceived = false;
			try {
				while ((message = reader.readLine()) != null) {
					System.out.println(message);
					if (!sizeReceived) {
						int width = Integer.valueOf(message);
						int height = Integer.valueOf(reader.readLine());
						dimension = new Dimension(width, height);
						System.out.println(dimension);
						sizeReceived = true;
					} else {
						// Key presses
						int key = Integer.valueOf(message);
						switch (key) {
						case KeyEvent.VK_LEFT:
							tellAll(Direction.LEFT.ordinal());
							break;
						case KeyEvent.VK_RIGHT:
							tellAll(Direction.RIGHT.ordinal());
							break;
						case KeyEvent.VK_UP:
							tellAll(Direction.UP.ordinal());
							break;
						case KeyEvent.VK_DOWN:
							tellAll(Direction.DOWN.ordinal());
							break;
						case 666:
							tellAll(666);
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void tellAll(int result) {
		for (PrintWriter writer : writers) {
			writer.write(result);
			writer.flush();
		}
	}
}
