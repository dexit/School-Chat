package school.chat;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;
/**
*
* Graphical User Interface vor ChatServer
*
* @version 1.0 vom 30.09.2009
* @author Sebastian Riedel
*/

public class ChatServerGUI extends JFrame {
	// Anfang Variablen

	// Anfang Attribute
	private JTextField serverPort = new JTextField();
	private JLabel serverPortLabel = new JLabel();
	private JTextField serverIP = new JTextField();
	private JLabel serverIPLabel = new JLabel();
	private static JButton startServer = new JButton();
	
	private boolean server;
	private static ChatServer cs;
	// Ende Attribute

	// Ende Variablen

	public ChatServerGUI(String title) {
		// Frame-Initialisierung
		super(title);
		//setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		addWindowListener(new WindowAdapter(){
			public void windowClosing (WindowEvent evt) {
				if(ChatServerGUI.cs == null){
					System.exit(0);
				}else{
				ChatServerGUI.closeServer();
				}
			}
		});
		int frameWidth = 300;
		int frameHeight = 115;
		setSize(frameWidth, frameHeight);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (d.width - getSize().width) / 2;
		int y = (d.height - getSize().height) / 2 ;
		setLocation(x, y);
		Container cp = getContentPane();
		cp.setLayout(null);
		setIconImage(new ImageIcon(getClass().getResource("icons/server.png")).getImage());
		// Anfang Komponenten

		serverPortLabel.setBounds(0, 0, frameWidth, 16);
		serverPortLabel.setText("Port:");
		serverPortLabel.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		cp.add(serverPortLabel);
		serverPort.setBounds(0, 16, frameWidth, 16);
		serverPort.setText("12345");
		cp.add(serverPort);
		serverIPLabel.setBounds(0, 32, frameWidth, 16);
		serverIPLabel.setText("Your IP: (automaticaly detected)");
		serverIPLabel.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		cp.add(serverIPLabel);
		serverIP.setBounds(0, 48, frameWidth, 16);
		serverIP.setEditable(false);
		try {
			serverIP.setText(InetAddress.getLocalHost().getHostAddress());
		}catch(UnknownHostException e){
			serverIP.setText("can't be detected");
		}
		cp.add(serverIP);
		startServer.setBounds(0, 64, frameWidth, 25);
		startServer.setText("start Server");
		cp.add(startServer);
		startServer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				startServer(evt);
			}
		});

		// Ende Komponenten

		setResizable(false);
		setVisible(true);
	}

	// Anfang Methoden

	// Anfang Ereignisprozeduren
	
	public static void closeServer(){
		startServer.setText("shutdown...");
		startServer.setEnabled(false);
		cs.stopServer();
	}
	
	public void startServer(ActionEvent evt) {
		//String[] args = {this.serverPort.getText()};
		if(this.server){
			closeServer();
		}else{
			cs = new school.chat.ChatServer(Integer.parseInt(this.serverPort.getText()));
			//serverIP.setText(cs.getMyIP());
			//System.out.println(cs.getMyIP());
			cs.start();
			startServer.setText("stop Server");
			this.server = true;
		}
		//Thread.stop();
	}

	// Ende Ereignisprozeduren

	public static void main(String[] args) {
		new school.chat.ChatServerGUI("ChatServer");
	}
	// Ende Methoden
}

