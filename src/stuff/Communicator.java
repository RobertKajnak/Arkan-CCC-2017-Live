package stuff;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class Communicator {
	Socket clientSocket;
	DataOutputStream outToServer;
	BufferedReader inFromServer;
	public Communicator(int socket) {
		try {
			clientSocket = new Socket("localhost", socket);

			outToServer = new DataOutputStream(clientSocket.getOutputStream());
			inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public String readLine() {
		try {
			return inFromServer.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String[] readLine(int numberOfLines) {
		String[] lines = new String[numberOfLines] ;
		try{
			for (int i=0;i<numberOfLines;i++){
				lines[i]= inFromServer.readLine();
			}
		}
		catch (IOException e){
			e.printStackTrace();
		}
		return lines;
	}
	
	public void write(String s) {
		try {
			outToServer.writeBytes(s );
			outToServer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
			
	}
	
	///also does flush
	public void writeLine(String s) {
		try {
			outToServer.writeBytes(s + " \n");
			outToServer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
			
	}
	
	public void writeNoFlush(String s) {
		try {
			outToServer.writeBytes(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
			
	}
	
	public void close() {
		try {
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
