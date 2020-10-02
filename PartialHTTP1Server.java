import java.io.*;
import java.net.*;
import java.util.Scanner;

class PartialHTTP1Server {

	public void main (String args[]) throws IOException{
		int portID = Integer.parseInt(args[0]);
		Thread mainThread = null;
		
		ServerSocket server = new ServerSocket(portID);
		Socket connection;
		
		Scanner sc = new Scanner(System.in);
		
		String hostname = sc.next();
		
		int clientPort = sc.nextInt();
		
		Socket client = new Socket(hostname, clientPort);
		
		if (clientPort == portID){
			connection = server.accept();
			//Is it the actual client or is connection right?
			if(!mainThread.addToPool(connection)){
				System.out.println("503 Service Unavailable");
				//Not sure if I should do this too - client.close();
				connection.close();
			}
		}
	}
}

class Thread{
	//stores all of the threads >>> list, queue, or linked list of linked lists
	Socket [] pool;
	int active = 0;
	Scanner scanned = new Scanner(System.in);
	
	public boolean addToPool (Socket connect){
		//replace with chosen data structure
		if (active < 50){
			pool[active] = connect;
			active++;
			return true;
		} else {
			return false;
		}
	}
	
	//Should actually be run?
	public void runner () throws IOException{
		//Reviews command from command line for socket
		Socket current = pool[active];
		//Skips the first one in the array for now
		while (active != 0){
			scanned = new Scanner(current.getInputStream());
			String com = scanned.nextLine();
			String[] command = com.split(" ");
			
			//Might have to double check on the length portion in cases of since modified
			//If command guarantees that it will not go further in checking if the request format is incorrect
			if (!command[2].startsWith("HTTP/") || !command[1].startsWith("/") || command.length != 3){
				System.out.println("HTTP/1.0 400 Bad Request");
			} else {
				//deal with cases when there is something besides a number afterwards
				double version = 0.0;
				try {
					version = Double.parseDouble(command[2].substring(5));
				} catch (Exception e){
					System.out.println("HTTP/1.0 400 Bad Request");
				}
				if (version < 0.0 || version > 1.0){
					System.out.println("HTTP/1.0 505 HTTP Version Not Supported");
				} if (isNotImplemented(command[0])){
					System.out.println("HTTP/1.0 501 Not Implemented");
				} else if (!isImplemented(command[0])){
					System.out.println("HTTP/1.0 400 Bad Request");
				}
			}
			
			active--;
			current = pool[active];
		}
		
	}
	
	//Really for not implemented yet
	private boolean isNotImplemented(String s){
		if (s.equals("DELETE") || s.equals("PUT") || s.equals("LINK") || s.equals("UNLINK")){
			return true;
		}
		return false;
	}
	
	private boolean isImplemented(String s){
		if (s.equals("GET") || s.equals("POST") || s.equals("HEAD")){
			return true;
		}
		return false;
	}
	
}
