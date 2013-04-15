import java.io.*;
import java.net.*;
import java.util.*;

public class CGCalc extends Thread {

	Socket connectedClient = null;    
	BufferedReader inFromClient = null;
	DataOutputStream outToClient = null;
	String cgthomepage;

	private static String readFile(String path) throws IOException {
		String text = new Scanner( new File(path) ).useDelimiter("\\A").next();
		return text;
	}


	private static String eval(String expr) {
		return "\\sin\\left( "+expr+" \\right)";
	}

	public CGCalc(Socket client) {
		connectedClient = client;
		try {
			cgthomepage = CGCalc.readFile( "cgcalc.html" );
		} catch (IOException e) {
			cgthomepage = "FAILED";
		}
	}            

	public void run() {

		String
			currentLine = null, 
						postBoundary = null, 
						contentength = null, 
						filename = null, 
						contentLength = null;

		try {

			inFromClient = new BufferedReader(
					new InputStreamReader(
						connectedClient.getInputStream()));                  
			outToClient = new DataOutputStream(
					connectedClient.getOutputStream());

			currentLine = inFromClient.readLine();
			String headerLine = currentLine;                
			StringTokenizer tokenizer = new StringTokenizer(headerLine);
			String httpMethod = tokenizer.nextToken();
			String httpQueryString = tokenizer.nextToken();

			int responseCode = 0;
			String responseString = "";

			if (httpMethod.equals("GET")) {    

				if (httpQueryString.equals("/")) {
					responseCode = 200;
					responseString  = cgthomepage;

				} else if (httpQueryString.startsWith("/cgcalc")) {
					responseCode = 200;
					String[] args = httpQueryString.split("\\?");
					String expr = args[1].split("=")[1];
					responseString = "CGCalc:OK"+CGCalc.eval( expr );

				} else {
					responseCode = 404;
					responseString = "<b>The Requested resource not found ...." +
						"Usage: http://127.0.0.1:31415</b>";                  
				}

				sendResponse( responseCode, responseString );
			}
		} catch (Exception e) {
			e.printStackTrace();
		}    
	}

	public void sendResponse (int statusCode, String responseString) throws Exception {
		String statusLine = null;
		String serverdetails = "Server: CombGames server ;)";
		String contentLengthLine = null;
		String fileName = null;        
		String contentTypeLine = "Content-Type: text/html" + "\r\n";
		FileInputStream fin = null;
		if (statusCode == 200)
			statusLine = "HTTP/1.1 200 OK" + "\r\n";
		else
			statusLine = "HTTP/1.1 404 Not Found" + "\r\n";    

		contentLengthLine = "Content-Length: " + 
			responseString.length() + "\r\n";    

		outToClient.writeBytes(statusLine);
		outToClient.writeBytes(serverdetails);
		outToClient.writeBytes(contentTypeLine);
		outToClient.writeBytes(contentLengthLine);
		outToClient.writeBytes("Connection: close\r\n");
		outToClient.writeBytes("\r\n");        
		outToClient.writeBytes(responseString);
		outToClient.close();
	}

	public static void main (String args[]) throws Exception {
		ServerSocket Server = new ServerSocket (31415);         

		System.out.println ("Combinatorial Games Calculator Server Waiting for client on port 31415");

		while (true) {                                         
			Socket connected = Server.accept();
			(new CGCalc(connected)).start();
		}      
	}
}
