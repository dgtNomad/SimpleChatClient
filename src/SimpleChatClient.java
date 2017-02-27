import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

import javax.swing.*;

public class SimpleChatClient {
	
	JTextArea incomingArea;
	JTextField outgoingField;
	BufferedReader reader;
	PrintWriter writer;
	Socket socket;
	
	public static void main(String args[]) {
		SimpleChatClient client = new SimpleChatClient();
		client.run();
	}
	
	public void run() {
		JFrame frame = new JFrame("Simple Chat Client");
		JPanel mainPanel = new JPanel();
		incomingArea = new JTextArea(10, 20);
		incomingArea.setLineWrap(true);
		incomingArea.setWrapStyleWord(true);
		incomingArea.setEditable(false); 
		JScrollPane qScroller = new JScrollPane(incomingArea);
		qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		outgoingField = new JTextField(20);
		JButton sendButton = new JButton("Send");
		sendButton.addActionListener(new SendButtonListener());
		
		mainPanel.add(qScroller);
		mainPanel.add(outgoingField);
		mainPanel.add(sendButton);
		
		setUpNetworking();
		
		Thread readerThread = new Thread(new IncomingMessageReader()); // Start a new thread for the reader, so that writer can work concurrently (via sendButtonListener)
		readerThread.start();
		
		frame.getContentPane().add(BorderLayout.CENTER, mainPanel);
		frame.setSize(400, 500);
		frame.setVisible(true);
	}
	
	private void setUpNetworking() {
		
		try {
			socket = new Socket("127.0.0.1", 5000); 
			// Setup socket for BOTH input and output streams
			InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
			reader = new BufferedReader(streamReader);
			writer = new PrintWriter(socket.getOutputStream());
			System.out.println("Networking has been established");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private class SendButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// Print the text entered into outgoing field to the server and flush immediately
			writer.println(outgoingField.getText()); 
			writer.flush();
			
			outgoingField.setText(""); // Clear the outgoingField's existing text for the next potential message
			outgoingField.requestFocus();
		}
	}
	
	public class IncomingMessageReader implements Runnable {
		
		public void run() {
			String message;
			try {
				while ((message = reader.readLine()) != null) { // reader gets stuff from server, indefinitely
					System.out.println("Read: " + message); 
					incomingArea.append(message + "\n"); // Append the message from server to client's conversation box
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
