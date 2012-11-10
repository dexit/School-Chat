package school.chat;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
/**
*
* GraphicUserInfertface for ChatClient
*
* @developed since 06.10.2009
* @version 0.9 vom 08.10.2009
* @author Sebastian Riedel
*/

public class ChatClientGUI extends JFrame {
	// Anfang Variablen

	// Anfang Attribute
	private JTextField clientPort = new JTextField();
	private JLabel clientPortLabel = new JLabel();
	private JTextField clientIP = new JTextField();
	private JLabel clientIPLabel = new JLabel();
	private JButton joinServer = new JButton();
	// Ende Attribute

	// Ende Variablen

	public ChatClientGUI(String title) {
		// Frame-Initialisierung
		super(title);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		int frameWidth = 300;
		int frameHeight = 115;
		setSize(frameWidth, frameHeight);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (d.width - getSize().width) / 2;
		int y = (d.height - getSize().height) / 2 ;
		setLocation(x, y);
		Container cp = getContentPane();
		cp.setLayout(null);
		setIconImage(new ImageIcon(getClass().getResource("icons/comments.png")).getImage());
		// Anfang Komponenten

		clientPortLabel.setBounds(0, 0, frameWidth, 16);
		clientPortLabel.setText("Port:");
		clientPortLabel.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		cp.add(clientPortLabel);
		clientPort.setBounds(0, 16, frameWidth, 16);
		clientPort.setText("10243");//"12345");
		cp.add(clientPort);
		clientIPLabel.setBounds(0, 32, frameWidth, 16);
		clientIPLabel.setText("ServerIP:");
		clientIPLabel.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		cp.add(clientIPLabel);
		clientIP.setBounds(0, 48, frameWidth, 16);
		clientIP.setText("schoolchat.is-a-geek.org");//"localhost");
		cp.add(clientIP);
		joinServer.setBounds(0, 64, frameWidth, 25);
		joinServer.setText("join Server");
		cp.add(joinServer);
		joinServer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				joinServer(evt);
			}
		});

		// Ende Komponenten

		setResizable(false);
		setVisible(true);
	}

	// Anfang Methoden

	// Anfang Ereignisprozeduren
	public void joinServer(ActionEvent evt) {
		school.chat.ChatClient.impMain(this.clientIP.getText(),Integer.parseInt(this.clientPort.getText()));
	}

	// Ende Ereignisprozeduren

	public static void main(String[] args) {
		new school.chat.ChatClientGUI("ChatClient");
	}
	// Ende Methoden
}

