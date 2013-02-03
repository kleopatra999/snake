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
import java.util.Random;


public class Main {

	private ArrayList<PrintWriter> writers = new ArrayList<PrintWriter>();
	private ArrayList<ClientHandler> handlers = new ArrayList<ClientHandler>();
	private static Random r = new Random();
	private int width_p1, width_p2, height;
	private int connections = 0;
	private boolean switchFoodto1;
	private static final int DIM = 32;
	public enum Direction {
		UP, DOWN, RIGHT, LEFT, EAT
	}

	public static void main(String[] args) {
		new Main();
	}

	private Main() {
		// Wait for client connections
		switchFoodto1 = false;
		listenForConnections();
	}

	private int getFoodCoord(int size) {
			return (r.nextInt(size) + DIM / 2) / DIM * DIM;
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

			// TODO
			Thread.sleep(1000);
			Dimension dimension1 = handlers.get(0).getDimension();
			Dimension dimension2 = handlers.get(1).getDimension();

			int width = dimension1.width + dimension2.width;
			int height_m = Math.max(dimension1.height, dimension2.height);
			tellAll(width);
			tellAll(height_m);

			width_p1 = dimension1.width;
			width_p2 = dimension2.width;
			height = dimension1.height;
			int x_food = getFoodCoord(width_p1);
			int y_food = getFoodCoord(height);

			PrintWriter writer = writers.get(0);
			dimension1 = handlers.get(0).getDimension();
			writer.write(0);
			writer.write(dimension1.width);
			writer.write(dimension1.height);
			writer.write(x_food);
			writer.write(y_food);
			writer.flush();

			writer = writers.get(1);
			writer.write(dimension1.width);
			writer.write(dimension2.width);
			writer.write(dimension2.height);
			writer.write(x_food);
			writer.write(y_food);
			writer.flush();


		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
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
			int message;
			boolean sizeReceived = false;
			try {
				while ((message = reader.read()) != -1) {
					if (!sizeReceived) {
						int width = message;
						int height = reader.read();
						width = (width + 32 / 2) / 32 * 32;
						height = (height + 32 / 2) / 32 * 32;
						dimension = new Dimension(width, height);
						sizeReceived = true;
					} else {
						// Key presses
						int key = message;
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
							tellAll(Direction.EAT.ordinal());
							if (switchFoodto1) {
								tellAll(getFoodCoord(width_p1));
								tellAll(getFoodCoord(height));
								switchFoodto1 = false;
							} else {
								tellAll(width_p1 + getFoodCoord(width_p2));
								tellAll(getFoodCoord(height));
								switchFoodto1 = true;
							}
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
