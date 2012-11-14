package school.chat;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Date;
import java.lang.Character;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import com.nitido.utils.toaster.*;

public class ChatClient extends JApplet implements Runnable,
	ActionListener{


	// Anfang Attribute
	private final static int width = 500;
	private final static int hight = 400;

	private static boolean standalone = false;
	private Toaster toasterManager = new Toaster();

	private ArrayList<String> channelName = new ArrayList<String>(0);
	private ArrayList<StringBuffer> channelHistory = new ArrayList<StringBuffer>(0);
	private ArrayList<JTextPane> channelPane = new ArrayList<JTextPane>(0);

	private JLabel label;
	private JTextPane area;
	private JPanel input;
	private JPanel textpw;
	private JTextField text;
	private JPasswordField pw;
	private JButton button;
	private JTabbedPane tabs;
	private JPanel main;
	private JPanel main_user;
	private JScrollPane main_user_scroll;
	private StringBuffer earlyText = new StringBuffer("");

	private String host;
	private int port;
	private Socket socket;
	private BufferedReader in;
	private BufferedWriter out;

	private volatile Thread t;
	private boolean login = false;
	private String name;
	private static String server;

	private static int newMessages;
	private TrayIcon trayIcon = null;
	private SystemTray tray = null;

	private int status = -1; //-1: disconnected;0: offline; 1: online; 2: away
	private static newMessageBlink blinker;

	private ArrayList<UserInfo> userList = new ArrayList<UserInfo>(0);
	private String style = "";

	private PopupMenu traymenu = new PopupMenu();
	private MenuItem traymenu_on = new MenuItem("online");//,new ImageIcon(getClass().getResource("/icons/status_online.png")));
	private MenuItem traymenu_afk = new MenuItem("away");//,new ImageIcon(getClass().getResource("/icons/status_away.png")));
	private MenuItem traymenu_off = new MenuItem("offline");//,new ImageIcon(getClass().getResource("/icons/status_away.png")));
	private MenuItem traymenu_quit = new MenuItem("quit");//,new ImageIcon(getClass().getResource("/icons/door_in.png")));
	//JCheckBoxMenuItem

	private boolean mute = false;

	private boolean useEmos = false;
	private String[][] emos =	{{	":-)",					":-P",					":-D",					"^^",					":-o",						":-(",					"-.-",					";-D",					"xD"					/*"=D",					"x-(",				"B-)",				":'(",				"}:-)",				"=)",						":(|)",				";^)",						"\\m/",					"&gt;.&lt;"*/				},
								{	"emoticon_smile.png",	"emoticon_tongue.png",	"emoticon_grin.png",	"emoticon_happy.png",	"emoticon_surprised.png",	"emoticon_unhappy.png", "emoticon_waii.png",	"emoticon_wink.png",	"emoticon_evilgrin.png"	/*"gtalk/equal_grin.gif",	"gtalk/angry.gif",	"gtalk/cool.gif",	"gtalk/cry.gif",	"gtalk/devil.gif",	"gtalk/equal_smile.gif",	"gtalk/monkey.gif",	"gtalk/nose_big_wink.gif",	"gtalk/rockout.gif",	"gtalk/wince.gif"*/	} };
	private boolean useSigns = false;
	private String[][] signs =	{{	"@=*",		"&euro;",			"$",				"*music*",		"/!\\",			"(!)",				"&lt;3",			"(+)",		"(tick)",	 "(?)",			"(i)",				"*joystick*",		"*play*",			"*java*"	},
								{	"bomb.png", "money_euro.png",	"money_dollar.png", "music.png",	"error.png",	"exclamation.png",	"gtalk/heart.png",	"add.png",	"accept.png", "help.png",	"information.png",	"joystick.png",		"controller.png",	"cup.png"	} };

	// Wird verwendet, um den Client als Applikation zu starten
	private static JFrame frame;
	// Kommandozeilenparameter fuer die Socket-Initialisiierung
	private static String[] cmdLineArgs;

        // Ignorliste
        private ArrayList<String> ignore = new ArrayList<String>();
	// Ende Attribute

	// Anfang Methoden
	// Der Client kann auch als Applikation gestartet werden.
	// Dies wird ermoeglicht, indem ein Frame fuer das Applet zur Verfuegung gestellt wird.
	static public void main(String[] args){
		if(args.length == 0){
			new school.chat.ChatClientGUI("ChatClient");
		}else{
			if(args.length != 2){
				System.err.println("java ChatClient <host> <port>");
				System.exit(1);
			}else{
				standalone = true;
				impMain(args[0],Integer.parseInt(args[1]));
			}
		}
	}
	static public void impMain(String ip, int port){
		// Wird fuer Socket-Initialisierung benoetigt

		// Der Client
		final ChatClient client = new ChatClient(ip,port);
		server = port + "@" + ip;
		frame = new JFrame(server + " - School Java Chat");
		frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

		client.init();
		frame.getContentPane().add(client);
		frame.setSize(width, hight);
		frame.setVisible(true);

		try{
		     UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}catch(Exception ex){
		    System.out.println("Uhh.. there is no Native Look&Feel...");
		}

	}
	public ChatClient(String host, int port){
                // Toaster einrichten:
                toasterManager.setToasterWidth(150);
                toasterManager.setMaxToaster(2);

		if(SystemTray.isSupported()){
			traymenu.add( traymenu_on );
			traymenu_on.addActionListener(new ActionListener() {
                                @Override
				public void actionPerformed(ActionEvent evt) {
					traymenu_on_clicked(evt); } });
			traymenu.add( traymenu_afk );
			traymenu_afk.addActionListener(new ActionListener() {
                                @Override
				public void actionPerformed(ActionEvent evt) {
					traymenu_afk_clicked(evt); } });
			traymenu.add( traymenu_off );
			traymenu_off.addActionListener(new ActionListener() {
                                @Override
				public void actionPerformed(ActionEvent evt) {
					traymenu_off_clicked(evt); } });
			traymenu.addSeparator();
			traymenu.add( traymenu_quit );
			traymenu_quit.addActionListener(new ActionListener() {
                                @Override
				public void actionPerformed(ActionEvent evt) {
					traymenu_quit_clicked(evt); } });
			tray = SystemTray.getSystemTray();
			Image image = new ImageIcon(getClass().getResource("/icons/disconnect.png")).getImage();
			MouseListener mouseListener = new MouseListener() {
                                @Override
				public void mouseClicked(MouseEvent e) {
					if(e.getButton() == 3){
						//traymenu.show( e.getComponent(), e.getX(), e.getY() );
					}else if(e.getButton() == 1 && e.getClickCount() == 2){
						if(frame.isVisible()){
							frame.setVisible(false);
						}else{
							frame.setVisible(true);
						}
					}
				}
                                @Override
				public void mouseEntered(MouseEvent e) {}
                                @Override
				public void mouseExited(MouseEvent e) {}
                                @Override
				public void mousePressed(MouseEvent e) {}
                                @Override
				public void mouseReleased(MouseEvent e) {}
			};
			trayIcon = new TrayIcon(image, "School Java Chat - disconnected", traymenu);
			trayIcon.setImageAutoSize(true);
			trayIcon.addMouseListener(mouseListener);
			try {
				tray.add(trayIcon);
			} catch (AWTException error) {
				System.err.println("TrayIcon could not be added.");
			}
		}
		this.host = host;
		this.port = port;
		label = new JLabel(" ");
		JPanel top = new JPanel();
		top.add(label);

		tabs = new JTabbedPane();

		main = new JPanel();
		main.setLayout(new BorderLayout());

		main_user = new JPanel(new GridBagLayout());
		main_user_scroll = new JScrollPane(main_user);

		area = new JTextPane();
		area.setContentType("text/html");
		area.setFont(new Font("Monospaced", Font.PLAIN, 14));
		area.setEditable(false);

		text = new JTextField(48);
		text.setFont(new Font("Monospaced", Font.PLAIN, 14));
		text.setEnabled(true);

		channelName.add("main");
		channelHistory.add(earlyText);
		channelPane.add(area);

		text.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
			    commandHandler(text,area, earlyText, "main");
			}
		    }
		);

		pw = new JPasswordField(48);
		pw.setFont(new Font("Monospaced", Font.PLAIN, 14));
		pw.setEchoChar('#');
		pw.setEnabled(true);
		pw.addActionListener(this);
		pw.setVisible(false);


		button = new JButton("Join");
		button.setEnabled(true);
		button.addActionListener(this);

		input = new JPanel(new BorderLayout());
		textpw = new JPanel(new BorderLayout());
		textpw.add(text,BorderLayout.NORTH);
		textpw.add(pw,BorderLayout.SOUTH);
		input.add(textpw,BorderLayout.CENTER);
		input.add(button,BorderLayout.EAST);

		Container c = getContentPane();
		c.add(top, BorderLayout.NORTH);
		main.add(new JScrollPane(area), BorderLayout.CENTER);
		main.add(main_user_scroll, BorderLayout.EAST);
		main.add(input, BorderLayout.SOUTH);
		tabs.addTab("main",main);
		c.add(tabs, BorderLayout.CENTER);
		//c.add(input, BorderLayout.SOUTH);

		tabs.validate();
		tabs.updateUI();
		this.validate();
	}

	public void addChannel(final String name){
		final JPanel tmp;
		JPanel input = new JPanel(new BorderLayout());
		JTextPane area;
		final JTextField text;
		JButton close;
		Container cont = getContentPane();

		tmp = new JPanel();
		tmp.setLayout(new BorderLayout());

		area = new JTextPane();
		area.setContentType("text/html");
		area.setFont(new Font("Monospaced", Font.PLAIN, 14));
		area.setEditable(false);

		text = new JTextField(48);
		text.setFont(new Font("Monospaced", Font.PLAIN, 14));
		text.setEnabled(true);

		channelName.add(name);
		channelHistory.add(new StringBuffer(""));
		channelPane.add(area);

		text.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
			    commandHandler(text,channelPane.get(getIndexOfChannel(name)),channelHistory.get(getIndexOfChannel(name)),name);
			}
		    }
		);
		text.setVisible(true);

		close = new JButton("Close");
		close.setEnabled(true);
		close.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent evt){
			    Object e = evt.getSource();
			    String cmd = evt.getActionCommand();

			    tabs.remove(tmp);
			    tabs.updateUI();
			}
		    }
		);

		input.add(text, BorderLayout.CENTER);
		input.add(close, BorderLayout.EAST);

		tmp.add(new JScrollPane(area), BorderLayout.CENTER);
		tmp.add(main_user_scroll, BorderLayout.EAST);
		tmp.add(input, BorderLayout.SOUTH);
		tabs.addTab(name,tmp);

		tabs.validate();
		tabs.updateUI();
		this.validate();
	}

	public int getIndexOfChannel(String name){
		int tmp = 0;
		for(String n :channelName){
			if(n.equals(name)){
			    tmp = channelName.indexOf(n);
			}
		}

		return (tmp == -1?0:tmp);
	}

        @Override
	public void init(){
		if(frame == null){
			host = getCodeBase().getHost();
			text.setEnabled(true);
			text.requestFocus();
			button.setEnabled(true);
		}else{
			frame.setIconImage(new ImageIcon(getClass().getResource("/icons/comment.png")).getImage());
		}

		// Anfang Komponenten
		// Ende Komponenten
	}
        @Override
	public void setName(String name){
		this.name = name;
		frame.setTitle(server + " - " + name + " - School Java Chat");
	}
	private void addText(String text){
		addText(area, earlyText, text);
	}
	private void addText(JTextPane area,StringBuffer history,String text){
		history.append(text);
		area.setText("<html><style>" + style + "</style><link rel=\"stylesheet\" type=\"text/css\" href=\"" + getClass().getResource("style.css") + "\"><body>" + history.toString() + "</body></html>");
	}
	private void setTray(){
		if(newMessages == 0){
			Image image = null;
			switch(status){
			  case -1:
				image = new ImageIcon(getClass().getResource("/icons/disconnect.png")).getImage();
				trayIcon.setToolTip(server + " - " + name + " - School Java Chat - disconnected");
				break;
			  case 0:
				image = new ImageIcon(getClass().getResource("/icons/status_offline.png")).getImage();
				trayIcon.setToolTip(server + " - " + name + " - School Java Chat - offline");
				break;
			  case 1:
				image = new ImageIcon(getClass().getResource("/icons/status_online.png")).getImage();
				trayIcon.setToolTip(server + " - " + name + " - School Java Chat - online");
				break;
			  case 2:
				image = new ImageIcon(getClass().getResource("/icons/status_away.png")).getImage();
				trayIcon.setToolTip(server + " - " + name + " - School Java Chat - away from keyboard");
				break;
			}
			trayIcon.setImage(image);
		}
	}
	private class UserInfo{
		private String name;
		private String color;
		private int status = 1;
		private String statusMessage;
		private JTextPane pane = new JTextPane();
		public UserInfo(){
			this("","000000",0,"");
		}
		public UserInfo(String name, String color, int status, String statusMessage){//name,color,status,statusMessages
			this.name = name;
			this.color = color;
			this.status = status;
			this.statusMessage = statusMessage;
			pane.setContentType("text/html");
			pane.setFont(new Font("Monospaced", Font.PLAIN, 14));
			pane.setEditable(false);
			pane.setSize(Short.MAX_VALUE,20);
			reloadPane();
		}
		public void reloadPane(){
			String statusImg = "disconnect.png";
			switch(status){
			  case 0:
				statusImg = "status_offline.png";
				break;
			  case 1:
				statusImg = "status_online.png";
				break;
			  case 2:
				statusImg = "status_away.png";
				break;
			}
			pane.setToolTipText(statusMessage);
			pane.setText("<html><body><img src=\"" + getClass().getResource("/icons/" + statusImg) + "\" border=\"0\"/><span style=\"color:#" + color + ";\">" + name + "</span></body></html>");
		}
	}
	private class newMessageBlink extends Thread {
		private String[] images = {"/icons/email.png","icons/empty.png"};
                @Override
		public void run() {
			try {
				while(true){//!isInterrupted){
					for (int i = 0; i < images.length; i++) {
						if(newMessages != 0 && !frame.isFocused()){
							Image image = new ImageIcon(getClass().getResource(images[i])).getImage();
							trayIcon.setImage(image);
						}else if(newMessages != 0 && frame.isFocused()){
							newMessages = 0;
							setTray();
						}
						Thread.sleep(1000);
					}
				}
			}catch(InterruptedException ie){}
			finally{
				setTray();
			}
		}
	}
	public void traymenu_on_clicked(ActionEvent e){
		if(login){
			if(status == 2){
				autoSend("/me is back");
				status = 1;
				setTray();
			}
		}else{
			addText(getTimestamp() + " <b>log in first!</b><br>");
		}
	}
	public void traymenu_afk_clicked(ActionEvent e){
		if(login && status != 2){
			autoSend("/afk");
			status = 2;
			setTray();
		}
	}
	public void traymenu_off_clicked(ActionEvent e){
		if(login){
			autoSend("/quit");
		}
	}
	public void traymenu_quit_clicked(ActionEvent e){
		destroy();
		frame.setVisible(false);
		tray.remove(trayIcon);
		if(standalone){
		  System.exit(1);
		}
	}
	public void commandHandler(JTextField text, JTextPane area, StringBuffer history, String name){
		try{
			if(text.getText().equals("/cls")){
				history.replace(0,history.length()-1,"");
				area.setText("<html><body></body></html>");
			}else if(text.getText().equals("/credits")){
				addText(area,history,"<b>Credits</b><br>Lukas Schreiner a.k.a. Mono<br/>Sebastian Riedel a.k.a. D<font color=\"#FF0000;\">a</font>BASCHT<br/>Roman Herberg a.k.a. terrorbaby<br/>more infos at http://code.google.com/p/school-java-chat/people/list<br/>");
			}else if(text.getText().equals("/emoticons")){
				if(useEmos){
						useEmos = false;
						addText(area,history,getTimestamp() + " <i>don't use emoticons</i><br/>");
				}else{
						useEmos = true;
						addText(area,history,getTimestamp() + " <i>use emoticons</i><br/>");
				}
			}else if(text.getText().equals("/signs")){
				if(useSigns){
						useSigns = false;
						addText(area,history,getTimestamp() + " <i>don't use signs</i><br/>");
				}else{
						useSigns = true;
						addText(area,history,getTimestamp() + " <i>use signs</i><br/>");
				}
			}else  if(text.getText().equals("/es")){
				if(useEmos && useSigns){
						useEmos = false;
						useSigns = false;
						addText(area,history,getTimestamp() + " <i>don't use emoticons &amp; signs</i><br/>");
				}else{
						useEmos = true;
						useSigns = true;
						addText(area,history,getTimestamp() + " <i>use emoticons &amp; signs</i><br/>");
				}
			}else if(text.getText().equals("/mute")){
				if(mute){
						mute = false;
						addText(area,history,getTimestamp() + " <i>mute off</i><br/>");
				}else{
						mute = true;
						addText(area,history,getTimestamp() + " <i>mute on</i><br/>");
				}
			}else if(text.getText().equals("/emoticonlist")){
				history.append("<b>" + getTimestamp() + " list of all emoticons:</b><ol>");
				for(int i = 0; i < emos[0].length; i++){
					history.append("<li><img src=\"" + getClass().getResource("icons/" + emos[1][i]) + "\" border=\"0\"/> <img src=\"" + getClass().getResource("icons/bullet_left.png") + "\" border=\"0\"/> " + emos[0][i] + "</li>");
				}
				addText(area,history,"</ol>");
			}else if(text.getText().equals("/signlist")){
				history.append("<b>" + getTimestamp() + " list of all signs:</b><ol>");
				for(int i = 0; i < signs[0].length; i++){
					history.append("<li><img src=\"" + getClass().getResource("icons/" + signs[1][i]) + "\" border=\"0\"/> <img src=\"" + getClass().getResource("icons/bullet_left.png") + "\" border=\"0\"/> " + signs[0][i] + "</li>");
				}
				addText(area,history,"</ol>");
			}else if(text.getText().equals("/eslist")){
				history.append("<b>" + getTimestamp() + " list of all emoticons:</b><ol>");
				for(int i = 0; i < emos[0].length; i++){
					history.append("<li><img src=\"" + getClass().getResource("icons/" + emos[1][i]) + "\" border=\"0\"/> <img src=\"" + getClass().getResource("icons/bullet_left.png") + "\" border=\"0\"/> " + emos[0][i] + "</li>");
				}
				history.append("</ol>");
				history.append("<b>list of all signs:</b><ol>");
				for(int i = 0; i < signs[0].length; i++){
					history.append("<li><img src=\"" + getClass().getResource("icons/" + signs[1][i]) + "\" border=\"0\"/> <img src=\"" + getClass().getResource("icons/bullet_left.png") + "\" border=\"0\"/> " + signs[0][i] + "</li>");
				}
				addText(area,history,"</ol>");
			}else if(text.getText().equals("/ignorelist")){
				history.append("<b>"+getTimestamp()+" list of all to ignore people:</b><ol>");
				for(int i = 0; i < ignore.size(); i++){
					history.append("<li>"+ignore.get(i)+"</li>");
				}
				addText(area,history,"</ol>");
			}else if(text.getText().length() > 7 && text.getText().substring(0,7).equals("/ignore")){
				String username = text.getText().substring(8);
				// Ueberpruefen ob bereits vorhanden
				if(!isIgnored(username)){
					ignore.add(username);
					addText(area,history,"<div class=\"system\"><font class=\"command_joined\">"+getTimestamp()+"Benutzer \""+username+"\" wird ab jetzt ignoriert!</div>");
				}else{
					addText(area,history,"<div class=\"system\"><font class=\"command_joined\">"+getTimestamp()+"Du ignorierst den Benutzer \""+username+"\" doch schon l&auml;ngst!</div>");
				}
			}else if(text.getText().length() > 9 && text.getText().substring(0,9).equals("/unignore")){
				String username = text.getText().substring(10);
				// Ueberpruefen ob bereits vorhanden
				if(isIgnored(username)){
					ignore.remove(username);
					addText(area,history,"<div class=\"system\"><font class=\"command_joined\">"+getTimestamp()+"Benutzer \""+username+"\" wird ab jetzt nicht mehr ignoriert!</div>");
				}else{
					addText(area,history,"<div class=\"system\"><font class=\"command_joined\">"+getTimestamp()+"Du ignorierst den Benutzer \""+username+"\" &uuml;berhaupt nicht ;-)</div>");
				}
			}else if(text.getText().length() >= 2 && text.getText().substring(0,2).equals("/?")){
				text.setText("/help");
			}else if(text.getText().length() >= 5 && text.getText().substring(0,5).equals("/help")){
				addText(area,history,"<b>clientside commands</b><ol><li>/ignore _user_ - Ignores a person</li><li>/unignore _user_ - removes the person from your ignorelist</li><li>/ignorelist - shows you your ignorelist</li><li>/emoticons - Toggles emoticons (default is off)</li><li>/emoticonlist - Shows a list of all supported emoticons with their code</li><li>/signs - Toggles signs (default is off)</li><li>/signlist - Shows a list of all supported signs with their code</li><li>/es - Toggles emoticons &amp; signs (default is off)</li><li>/eslist - Shows a list of all supported emoticons &amp; signs with their code</li><li>/mute - Toggles blinking and popup for new messages (default is on)</li><li>/credits - Shows credits</li></ol>");
			}else if(text.getText().length() > 5 && text.getText().substring(0,5).equals("/join")){
				String channelName = text.getText().substring(6);
				addChannel(channelName);
			}else if(login){
				if(text.getText().length() >= 4 && text.getText().substring(0,4).equals("/afk")){
					status = 2;
					setTray();
				}else{
					status = 1;
					setTray();
				}
				school.chat.Blowfish bl = new Blowfish("Zba4ZknLfsqKNtkAubGMvMsVWOCAJ8dOox7i8DxSyS9GfsbZHUtWJhRBaMysDFtNepJygJ6IjFYPKXol");
				if(text.getText().charAt(0) == '/'){
					out.write(name+"~.~server~.~" + replaceSpechialChars(text.getText()));
				}else{
					out.write(name+"~.~public~.~" + bl.encryptString(replaceSpechialChars(text.getText())));//room;type;message
				}
				out.newLine();
				out.flush();
			}
		}catch(IOException ex){
			addText(area,history,getTimestamp() + ex.getMessage() + "<br/>");
			destroy();
		}finally{
			text.setText("");
			text.requestFocus();
		}
	}
        @Override
	public void actionPerformed(ActionEvent e){
		Object obj = e.getSource();
		String cmd = e.getActionCommand();

		try{
			if(obj == button){
				if(cmd.equals("Join")){
					name = replaceSpechialChars(text.getText());
					boolean isValid = true;
					int i = 0;
					while(i < name.length() && isValid){
						if(Character.isWhitespace(name.charAt(i)) ){
							isValid = false;
						}
						i++;
					}
					if(name.length() != 0 && name.charAt(0) != '!'  && isValid){
						login();
					}
				}else{
					destroy();
				}
			}else if(obj == pw){
				String password = "";
				for(char pwchar: pw.getPassword()){
					password += pwchar;
				}
				out.write("main~.~server~.~/login " + name + " " + replaceSpechialChars(password));
				out.newLine();
				out.flush();
				pw.setVisible(false);
				pw.setText("");
				text.setVisible(true);
			}else{
				System.out.println("Oha.. da lauft was schief...");

			}
		}catch(IOException ex){
			addText(getTimestamp() + ex.getMessage() + "<br/>");
			destroy();
		}finally{
			text.setText("");
			text.requestFocus();
		}
	}
	private String replaceSpechialChars(String text){
	    text = text.replace("&","&amp;");
		text = text.replace("±","&#177;");
		text = text.replace("<","&lt;");
		text = text.replace(">","&gt;");
		text = text.replace("\"","&quot;");
		text = text.replace("'","&#39;");
		text = text.replace("ä","&auml;");
		text = text.replace("Ä","&Auml;");
		text = text.replace("ö","&ouml;");
		text = text.replace("Ö","&Ouml;");
		text = text.replace("ü","&uuml;");
		text = text.replace("Ü","&Uuml;");
		text = text.replace("ß","&szlig;");
		text = text.replace("€","&euro;");
		text = text.replace("~","&#126;");
		return text;
	}
	private String getTimestamp(){
		return "[" + (new SimpleDateFormat("HH:mm:ss")).format(new Date()) + "] ";
	}
	private void autoSend(String text){
		try{
			out.write("main~.~server~.~" + text);
			out.newLine();
			out.flush();
		}catch(IOException ioe){}
	}
        private boolean isIgnored(String uName){
                boolean ignored = false;
                int i = 0;

                while(!ignored && i < ignore.size()){
                    if(ignore.get(i).equals(uName)){
                        ignored = true;
                    }
                    i++;
                }

                return ignored;
        }
	private void login() throws IOException{
		socket = new Socket(host, port);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

		out.write(name);
		out.newLine();
		out.flush();

		login = true;
		label.setText("new session");
		setName(name);
		button.setText("Logout");

		t = new Thread(this);
		t.start();

		status = 1;
		setTray();
	}
        @Override
	public void destroy(){
		if(login){
			try{
				frame.setTitle(server + " - School Java Chat");
				login = false;
				label.setText(" ");
				pw.setText("");
				button.setText("Join");
				pw.setVisible(false);
				text.setVisible(true);
				status = 0;
				setTray();
				t = null;
				if (socket != null)
					socket.close();
				if (in != null)
					in.close();
				if (out != null)
					out.close();
			}catch(IOException e){}
		}
	}
        @Override
	public void run(){
		try{
			blinker = new newMessageBlink();
			blinker.start();
			while(Thread.currentThread() == t && in != null){
				final String[] inComing = in.readLine().split("~.~");

				if(inComing == null)
					break;
				final String msg = inComing[3];
				doUpdate(new Runnable(){
                                        @Override
					public void run(){
						String roomString = inComing[0];
						JTextPane room = channelPane.get(getIndexOfChannel(roomString));
						StringBuffer history = channelHistory.get(getIndexOfChannel(roomString));
						if(inComing[1].equals("server") && msg.length() > 0 && msg.charAt(0) == '!'){//servercommands and co...
							String comm = msg.substring(1);
							String[] command = comm.split(" ");
							String message = "";
							if(command[0].equals("me")){
									String tmp1 = comm.substring(3,comm.length());

									String name = tmp1.substring(0,tmp1.indexOf('&'));
									message = tmp1.substring(tmp1.indexOf('&')+1,tmp1.length());
									if(!frame.isFocused()){
										newMessages++;
									}
									if(useEmos){
										for(int i = 0; i < emos[0].length; i++){
											message = message.replace(emos[0][i],"<img src=\"" + getClass().getResource("icons/" + emos[1][i]) + "\" border=\"0\"/>");
										}
									}
									if(useSigns){
										for(int i = 0; i < signs[0].length; i++){
											message = message.replace(signs[0][i],"<img src=\"" + getClass().getResource("icons/" + signs[1][i]) + "\" border=\"0\"/>");
										}
									}
									addText(room,history,"<div class=\"system\"><font class=\"command_me\">" + getTimestamp() + "<span class=\"user_" + name +"\">" + name +"</span> " + message + "</font></div>");
							}else if(command[0].equals("afk")){
									String tmp1 = comm.substring(4,comm.length());
									String oldName = name;
									String name = tmp1.substring(0,tmp1.indexOf('&'));
									message = tmp1.substring(tmp1.indexOf('&')+1,tmp1.length());
									autoSend("/userlist plain");
									addText(room,history,"<div class=\"system\"><font class=\"command_afk\">" + getTimestamp() + "<span class=\"user_" + name +"\">" + name +"</span> is <b>a</b>way <b>f</b>rom <b>k</b>eyboard " + (message.equals("")?"":" (" + message + ")") + "</span></div>");
									if(!mute && !frame.isFocused() && !name.equals(oldName)){
										toasterManager.showToaster("<b>" + name + "</b> is now afk" + (message.equals("")?"":"<br/>" + message));
									}
							}else if(command[0].equals("joined")){//style
									String tmp1 = comm.substring(7,comm.length());


									String color = tmp1.substring(0,tmp1.indexOf('&'));
									String oldName = name;
									String name = tmp1.substring(tmp1.indexOf('&')+1,tmp1.length());

									//style += ".user_" + name + " {color:#" + color + ";}\n";
									autoSend("/userlist plain");
									addText(room,history,"<div class=\"system\"><font class=\"command_joined\">" + getTimestamp() + "<span class=\"user_" + name +"\">" + name +"</span> joined</font></div>");
									if(!mute && !frame.isFocused() && !name.equals(oldName)){
										toasterManager.showToaster("<b>" + name + "</b> joined");
									}
							}else if(command[0].equals("left")){
									String tmp1 = comm.substring(5,comm.length());
									String oldName = name;
									String name = tmp1.substring(0,tmp1.indexOf('&'));
									message = tmp1.substring(tmp1.indexOf('&')+1,tmp1.length());
									autoSend("/userlist plain");
									addText(room,history,"<div class=\"system\"><font class=\"command_left\">" + getTimestamp() + "<span class=\"user_" + name +"\">" + name +"</span> left " + (message.equals("")?"":" (" + message + ")") + "</font></div>");
									if(!mute && !frame.isFocused() && !name.equals(oldName)){
										toasterManager.showToaster("<b>" + name + "</b> left");
									}
							}else if(command[0].equals("quit")){
									destroy();
									addText(room,history,"<div class=\"system\"><font class=\"command_quit\">" + getTimestamp() + "You left the server</font></div><hr noshade=\"noshade\"/>");
							}else if(command[0].equals("kick")){
								String temp = comm.substring(comm.indexOf('&')+1);
								if(command[1].equals("fail")){
									addText(room,history,"<div class=\"login\"><font class=\"command_login\"><a class=\"success\">" + getTimestamp() + temp +"</a></font></div>");
								}else{
									if(command[1].equals(name)){
										destroy();
										addText(room,history,"<div class=\"system\"><font class=\"command_quit\">" + getTimestamp() + "You were kicked by the Operator "+command[2]+"</font></div><hr noshade=\"noshade\"/>");
									}else if(command[2].equals(name)){
										addText(room,history,"<div class=\"login\"><font class=\"command_login\"><a class=\"success\">" + getTimestamp() + "User "+command[1]+" were kicked successfully.</a></font></div>");
									}
								}
							}else if(command[0].equals("bann")){
								String temp = comm.substring(comm.indexOf('&')+1);
								if(command[1].equals("fail")){
									addText(room,history,"<div class=\"login\"><font class=\"command_login\"><a class=\"success\">" + getTimestamp() + temp +"</a></font></div>");
								}else{
									if(command[1].equals(name)){
										destroy();
										addText(room,history,"<div class=\"system\"><font class=\"command_quit\">" + getTimestamp() + "You were banned for 24h by the Operator "+command[2]+"</font></div><hr noshade=\"noshade\"/>");
									}else if(command[2].equals(name)){
										addText(room,history,"<div class=\"login\"><font class=\"command_login\"><a class=\"success\">" + getTimestamp() + "User "+command[1]+" were banned successfully.</a></font></div>");
									}
								}
							}else if(command[0].equals("login")){
									if(command[1].equals("success")){
										setName(command[2]);
										addText(room,history,"<div class=\"login\"><font class=\"command_login\"><a class=\"success\">" + getTimestamp() + "Login successful as: " + command[2] + " with rights #" + command[3] + "</a></font></div>");
									}else if(command[1].equals("all")){
										autoSend("/userlist plain");
										addText(room,history,"<div class=\"login\"><font class=\"command_login\"><a class=\"all\">" + getTimestamp() + "User &quot;"+command[2]+"&quot; logged in as &quot;"+command[3]+"&quot;</a></font></div>");
									}else if(command[1].equals("auth")){
										addText(room,history,"<div class=\"system\"><font class=\"command_login\"><a class=\"auth\">Please enter password!</a></font></div>");//Please login with /login!
										text.setVisible(false);
										pw.setVisible(true);
										input.updateUI();
										pw.requestFocus();
									}else{
										if(command[2].equals("true")){
											destroy();
										}
										String temp = comm.substring(comm.indexOf('&')+1);
										addText(room,history,"<div class=\"login\"><font class=\"command_login\"><a class=\"fail\">" + getTimestamp() + "Login failed: "+temp+"</a></font></div>");
									}
							}else if(command[0].equals("register")){
									if(command[1].equals("success")){
										addText(room,history,"<div class=\"login\"><font class=\"command_register\"><a class=\"success\">" + getTimestamp() + "Register was successful: " + command[2] + ". </a></font></div>");
									}else{
										String temp = comm.substring(comm.indexOf('&')+1);
										addText(room,history,"<div class=\"login\"><font class=\"command_register\"><a class=\"fail\">" + getTimestamp() + "Register failed: "+temp+"</a></font></div>");
									}
							}else if(command[0].equals("sr")){
									if(command[1].equals("success")){
										addText(room,history,"<div class=\"login\"><font class=\"command_sr\"><a class=\"success\">" + getTimestamp() + "Setted right #"+command[3]+" for user " + command[2] + " successfully. </a></font></div>");
									}else{
										addText(room,history,"<div class=\"login\"><font class=\"command_sr\"><a class=\"fail\">" + getTimestamp() + "Setting right # failed.</a></font></div>");
									}
							}else if(command[0].equals("connectionClosed")){
									destroy();
									addText(room,history,"<div class=\"system\"><font class=\"command_connectionClosed\">" + getTimestamp() + "Server stopped!</font></div><hr noshade=\"noshade\"/>");
									status = -1;
									setTray();
							}else if(command[0].equals("help")){
									message = comm.substring(5,comm.length());
									history.append("<b>serverside commands</b>" + message);
									addText(room,history,"<b>serverside & clientside commands</b><ol><li>/help or /? - Shows this list</li></ol>");
							}else if(command[0].equals("userlist")){
									message = comm.substring(9,comm.length());
									String[] users = message.split("~");
									main_user.removeAll();
									style = "";
									int i = 0;
									for(String user: users){
										String[] properties = user.split("&");
										UserInfo tmpUser = new UserInfo(""+properties[0],""+properties[1],Integer.parseInt(properties[2]),(properties.length == 4?""+properties[3]:""));
										GridBagConstraints tmpUser_gbc = new GridBagConstraints();
										tmpUser_gbc.gridx = 0;
										tmpUser_gbc.gridy = i;
										tmpUser_gbc.weightx = 1.0;
										tmpUser_gbc.fill = GridBagConstraints.HORIZONTAL;
										main_user.add(tmpUser.pane,tmpUser_gbc);
										style += ".user_" + tmpUser.name + " {color:#" + tmpUser.color + ";}\n";
										i++;
									}
									JPanel end = new JPanel();
									GridBagConstraints end_gbc = new GridBagConstraints();
									end_gbc.gridx = 0;
									end_gbc.gridy = i;
									end_gbc.weightx = 1.0;
									end_gbc.weighty = 1.0;
									end_gbc.fill = GridBagConstraints.BOTH;
									main_user.add(end,end_gbc);
									main.updateUI();
									addText(room,history,"");
							}else if(command[0].equals("topic")){
									message = comm.substring(6,comm.length());
									addText(room,history,"<div class=\"topic\">topic changed to &quot;" + message + "&quot;</div>");
									label.setText(message);
							}else{
									addText(room,history,"<div class=\"server\">" + getTimestamp() + comm + "</div>");
							}
						}else{
							school.chat.Blowfish bl = new Blowfish("Zba4ZknLfsqKNtkAubGMvMsVWOCAJ8dOox7i8DxSyS9GfsbZHUtWJhRBaMysDFtNepJygJ6IjFYPKXol");
							String endMSG = bl.decryptString(msg);
							String name = inComing[2];
							if(useEmos){
								for(int i = 0; i < emos[0].length; i++){
									endMSG = endMSG.replace(emos[0][i],"<img src=\"" + getClass().getResource("icons/" + emos[1][i]) + "\" border=\"0\"/>");
								}
							}
							if(useSigns){
								for(int i = 0; i < signs[0].length; i++){
									endMSG = endMSG.replace(signs[0][i],"<img src=\"" + getClass().getResource("icons/" + signs[1][i]) + "\" border=\"0\"/>");
								}
							}
							if(!isIgnored(name)){
								if(!frame.isFocused()){
									newMessages++;
									toasterManager.showToaster("School Java Chat \n Neue Nachrichten: "+newMessages);
								}
								addText(room,history,"<div class=\"user\">" + getTimestamp() + "<span class=\"user_" + name + "\">" + name +  "</span>: </div>" + endMSG + "<br/>");
							}
						}
					}
				});
			}
			if(in == null){
				addText(getTimestamp() + " connection closed, your are now logged out!<br/>");
			}
		}catch(IOException e){}
	}
	private void doUpdate(Runnable r){
		try{
			EventQueue.invokeLater(r);
		}catch(Exception e){}
	}
	// Ende Methoden
}