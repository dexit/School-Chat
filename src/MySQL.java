/**
* Verbindet mit MySQL
* @author Lukas Schreiner
* @version 0.5
*/ 
package school.chat;
import java.sql.*;

public class MySQL{
    // Anfang Attribute
    private String treiber;
    private String dbURL;
    private String userName;
    private String password;
    private String port;
    private String database;
    private String host;
    private Connection cn;
    private String[][] resultData;
    private boolean sqlite = false;
    // Ende Attribute

    /**
     * Setzt den Proxy fest.
     * @param host String Proxy URL
     * @param port String Proxy Port
     */
    public void useProxy(String host, String port){
        System.setProperty("http.proxyHost", host);
        System.setProperty("http.proxyPort", port);
    }

    /**
     * Setzt Datenbanktreiber (MySQL, SQLite)
     * MySQL => org.gjt.mm.mysql.Driver
     * SQLite => org.sqlite.JDBC
     * @param treiber String Treiber
     */
    public void setTreiber(String treiber){
	this.treiber = treiber;
    }

    /**
     * Setzt die Datenbank fest.
     * Bei SQLite den entsprechenden Pfad!
     * @param database String Datenbankname ggbf. Pfad
     */
    public void setDatabase(String database){
	this.database = database;
    }

    /**
     * Setzt den Benutzername bei MySQL
     * @param userName String Benutzername
     */
    public void setUserName(String userName){
	this.userName = userName;
    }

    /**
     * Legt das Passwort fest bei MySQL
     * @param password String Passwort
     */
    public void setPassword(String password){
	this.password = password;
    }

    /**
     * Legt den Port fest bei MySQL
     * @param port String Port (3306)
     */
    public void setPort(String port){
	this.port = port;
    }

    /**
     * Legt die URL zur Datenbank fest bei MySQL
     * @param host String URL
     */
    public void setHost(String host){
	this.host = host;
    }

    /**
     * Setzt SQLite fest oder MySQL
     * @param sqlite boolean True bei Verwendung von SQLite, bei MySQL auf False
     */
    public void setSQLite(boolean sqlite){
	this.sqlite = sqlite;
    }

    /**
     * Konstruktor => Setzt Werte fuer SQLite Verbindung
     * @param driver String Treiber
     * @param database String Datebankname ggbf. Pfad
     * @param sqlite boolean True bei Verwendung von SQLite, bei MySQL false
     */
    public MySQL(String driver, String database, boolean sqlite){
	this(driver, database, "", "", "", "", true);
    }

    /**
     * Konstruktor => Setzt Werte fuer MySQL Verbindung
     * @param driver String MySQL Treiber
     * @param database String Datenbank
     * @param host String URL des Servers
     * @param user String Benutzername
     * @param pass String Passwort
     * @param port String Port (Standard 3306)
     * @param sqlite boolean True bei Verwendung von SQLite, bei MySQL false
     */
    public MySQL(String driver, String database, String host, String user, String pass, String port, boolean sqlite){
	this.setSQLite(sqlite);
	this.setTreiber(driver);
	this.setDatabase(database);
	this.setHost(host);
	this.setUserName(user);
	this.setPassword(pass);
	this.setPort(port);
	this.mergeAll();
    }

    /**
     * Setzt alle Werte fuer die Verbindung zusammen
     */
    public void mergeAll(){
	if(this.sqlite){
	    this.sqlite = true;
	    this.dbURL = "jdbc:sqlite:"+this.database;
	}else{
	    this.dbURL = "jdbc:mysql://"+this.host+":"+this.port+"/"+this.database;
	}
    }

    /**
     * Stellt eine Verbindung zur Datenbank her.
     */
    public void connect(){
	try{
	    Class.forName( this.treiber ).newInstance();
	    if(this.sqlite){
		this.cn = DriverManager.getConnection( this.dbURL ); 
	    }else{
		this.cn = DriverManager.getConnection( this.dbURL, this.userName, this.password ); 
	    }
	    System.out.println( "Connected successful" ); 
	}catch(Exception ex){
	    System.out.println(ex);
	}
    }

    /**
     * Schliesst die Verbindung
     */
    public void closeConnection(){
	try{
	    this.cn.close();
	}catch(SQLException ex){
	    System.out.println(ex);
	}
    }

    /**
     * Fuehrt ein Statement durch, wenn bei exec() ein NullPointerException kommt!
     * @param statement String Statement
     * @param connect boolean Unbedingt auf True setzen.
     * @throws Exception Bei SQL Fehlern
     */
    public void exec(String statement, boolean connect) throws Exception{
        if(connect){
            try{
                this.connect();
                this.exec(statement);
                this.closeConnection();
            }catch(Exception e){
                throw new Exception(e);
            }
            this.closeConnection();
        }
    }

    /**
     * Fuehrt ein Statement aus.
     * Speichert Ergebnis in resultData
     * @param statement String Statement
     * @throws Exception Bei SQL Fehler.
     */
    public void exec(String statement) throws Exception{
	String[][] dataRow = new String[0][0];
	String numRows = "";
	try{
	    Statement  st = cn.createStatement();
	    if(!statement.substring(0, 6).equals("INSERT") && !statement.substring(0, 6).equals("DELETE") && !statement.substring(0,6).equals("UPDATE")){
		ResultSet  rs = st.executeQuery(statement);
		ResultSetMetaData rsmd = rs.getMetaData();
		int columns = rsmd.getColumnCount()+1;
		while( rs.next() ){
		    for(int i = 1; i < columns; i++){
			numRows += rs.getString(i)+":;;:";
		    }
		    numRows += ";::;";
		}
		String numTest[] = numRows.split(";::;");
		if(!numTest[0].equals("")){
		    dataRow = new String[numTest.length][columns];
		    for(int i = 0; i < numTest.length; i++){
			String innerSplit[] = numTest[i].split(":;;:");
			for(int j = 0; j < innerSplit.length; j++){
			    dataRow[i][j] = innerSplit[j];
			}
		    }
		}else{
		    dataRow = new String[0][0];
		}
		rs.close();
		this.resultData = dataRow;
	    }else{
		//System.out.println("STATEMENT::: "+statement);
		st.executeUpdate(statement);
	    }
	    st.close();
	}catch(SQLException ex){
	    System.out.println("SQL Error unknown. :: DEBUG ::\nStatement: "+statement+"\nError Message: "+ex+" :::: "+numRows);
	    throw new Exception(ex);
	}
    }

    /**
     * Gibt das SQL Ergebnis zuruck
     * @return String[][]
     */
    public String[][] getResult(){
	return this.resultData;
    }
}
