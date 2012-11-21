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
	private JTextField clientPort = new JTextField("12345");
	private JLabel clientPortLabel = new JLabel("Port");
	private JTextField clientIP = new JTextField("schoolchat.is-a-geek.org");
	private JLabel clientIPLabel = new JLabel("ServerIP:");
	private JButton joinServer = new JButton("join Server");
        private Font font=new Font("MS Sans Serif", Font.PLAIN, 13);
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
                //Layouts genutzt :)
		cp.setLayout(new BorderLayout());
                JPanel center=new JPanel(new GridLayout(4, 1));
		setIconImage(new ImageIcon(getClass().getResource("/icons/comments.png")).getImage());
		// Anfang Komponenten

		clientPortLabel.setFont(font);
		center.add(clientPortLabel);
		center.add(clientPort);
		clientIPLabel.setFont(font);
		center.add(clientIPLabel);
		center.add(clientIP);
                cp.add(center,BorderLayout.CENTER);
		cp.add(joinServer,BorderLayout.SOUTH);
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
		ChatClient.impMain(this.clientIP.getText(),Integer.parseInt(this.clientPort.getText()));
                setVisible(false);
	}

	// Ende Ereignisprozeduren
	// Ende Methoden
}

