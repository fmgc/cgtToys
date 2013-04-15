import java.io.*;
import java.net.*;
import java.util.*;

public class CGTServer extends Thread {

	Socket connectedClient = null;    
	BufferedReader inFromClient = null;
	DataOutputStream outToClient = null;
	String cgthomepage;

	private static String readFile(String path) throws IOException {
		String text = new Scanner( new File(path) ).useDelimiter("\\A").next();
		return text;
	}

	public CGTServer(Socket client) {
		connectedClient = client;
		try {
			cgthomepage = "" + CGTServer.readFile( "cgtcalc.html" );
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

		PrintWriter fout = null;

		try {

			System.out.println( "The Client " +
					connectedClient.getInetAddress() + ":" + 
					connectedClient.getPort() + " is connected");

			inFromClient = new BufferedReader(new InputStreamReader(connectedClient.getInputStream()));                  
			outToClient = new DataOutputStream(connectedClient.getOutputStream());

			currentLine = inFromClient.readLine();
			String headerLine = currentLine;                
			StringTokenizer tokenizer = new StringTokenizer(headerLine);
			String httpMethod = tokenizer.nextToken();
			String httpQueryString = tokenizer.nextToken();

			System.out.println(currentLine);

			int responseCode = 0;
			String responseString = "";
			boolean responseFlag = false;	
			if (httpMethod.equals("GET")) {    
				System.out.println("GET request: "+ httpQueryString);        
				if (httpQueryString.equals("/")) {
					responseCode = 200;
					responseString  = cgthomepage;
				} else if (httpQueryString.startsWith("/cgcalc")) {
					responseCode = 200;
					String[] args = httpQueryString.split("\\?");
					String expr = args[1].split("=")[1];
					responseString = "CGCalc:OK" +  "\\sum_{i="+expr+"}\\alpha_i";
				} else {
					responseCode = 404;
					responseString = "<b>The Requested resource not found ...." +
							"Usage: http://127.0.0.1:31415</b>";                  
				}
				sendResponse( responseCode, responseString, responseFlag );
			}
		} catch (Exception e) {
			e.printStackTrace();
		}    
	}
	//
	public void sendResponse (int statusCode, String responseString, boolean isFile) throws Exception {
		String statusLine = null;
		String serverdetails = "Server: Java HTTPServer";
		String contentLengthLine = null;
		String fileName = null;        
		String contentTypeLine = "Content-Type: text/html" + "\r\n";
		FileInputStream fin = null;
		if (statusCode == 200)
			statusLine = "HTTP/1.1 200 OK" + "\r\n";
		else
			statusLine = "HTTP/1.1 404 Not Found" + "\r\n";    
		//
		if (isFile) {
			fileName = responseString;            
			fin = new FileInputStream(fileName);
			contentLengthLine = "Content-Length: " + Integer.toString(fin.available()) + "\r\n";
			if (!fileName.endsWith(".htm") && !fileName.endsWith(".html"))
				contentTypeLine = "Content-Type: \r\n";    
		}                        
		else {
			contentLengthLine = "Content-Length: " + responseString.length() + "\r\n";    
		}            
		outToClient.writeBytes(statusLine);
		outToClient.writeBytes(serverdetails);
		outToClient.writeBytes(contentTypeLine);
		outToClient.writeBytes(contentLengthLine);
		outToClient.writeBytes("Connection: close\r\n");
		outToClient.writeBytes("\r\n");        
		if (isFile) sendFile(fin, outToClient);
		else outToClient.writeBytes(responseString);
		outToClient.close();
	}
	
	public void sendFile (FileInputStream fin, DataOutputStream out) throws Exception {
		byte[] buffer = new byte[1024] ;
		int bytesRead;
		while ((bytesRead = fin.read(buffer)) != -1 ) {
			out.write(buffer, 0, bytesRead);
		}
		fin.close();
	}
	
	public static void main (String args[]) throws Exception {
		ServerSocket Server = new ServerSocket (31415);         
		System.out.println ("HTTP Server Waiting for client on port 31415");
		//
		while(true) {                                         
			Socket connected = Server.accept();
			(new CGTServer(connected)).start();
		}      
	}
}
