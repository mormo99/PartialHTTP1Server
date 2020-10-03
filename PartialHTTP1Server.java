import java.io.*;
import java.net.*;
import java.util.ArrayList;

class PartialHTTP1Server {
	
	/*public void poolManager(){
		
	}*/

	public static void main (String args[]) throws IOException{
		int portID = Integer.parseInt(args[0]);
		
		ServerSocket server = new ServerSocket(portID);
		Socket connection;
		boolean connected = true;
		ArrayList<Socket> pool = new ArrayList(5);
		
		server.setSoTimeout(3000);
		
		while (connected){
			try {
				connection = server.accept();
				PrintWriter output = new PrintWriter(connection.getOutputStream(), true);
				if(pool.size() == 50){
					output.println("503 Service Unavailable");
						//Not sure if I should do this too - client.close();
					connection.close();
				} else {
					synchronized (pool) {
						pool.add(connection);
						Channel mainThread = new Channel(connection, pool);
						mainThread.start();
					}
				}
			} catch (IOException e) {
				//Figure out how to connect this to output
				connected = false;
				System.out.println("HTTP/1.0 500 Internal Server Error");
			}
		}
		server.close();
	}
}

class Channel extends Thread{

	//Use actual thread pool
	Socket current;
	ArrayList<Socket> pool;
	
	//Maybe include the pool so that you can remove the connection?
	Channel (Socket in, ArrayList<Socket> pooled){
		current = in;
		pool = pooled;
	}
	
	//Should actually be run?
	public void run (){
		//Reviews command from command line for socket
		PrintWriter output;
		BufferedReader input;
		//Skips the first one in the array for now
		try {
				output = new PrintWriter(current.getOutputStream(), true);
				input = new BufferedReader(new InputStreamReader(current.getInputStream()));
				String com = input.readLine();
				String[] command = com.split(" ");
				
				//Might have to double check on the length portion in cases of since modified
				//If command guarantees that it will not go further in checking if the request format is incorrect
				if (!command[2].startsWith("HTTP/") || !command[1].startsWith("/") || command.length != 3){
					output.println("HTTP/1.0 400 Bad Request");
				} else {
					//deal with cases when there is something besides a number afterwards
					double version = 0.0;
					try {
						version = Double.parseDouble(command[2].substring(5));
						if (version < 0.0 || version > 1.0){
							output.println("HTTP/1.0 505 HTTP Version Not Supported");
						} if (isNotImplemented(command[0])){
							output.println("HTTP/1.0 501 Not Implemented");
						} else if (!isImplemented(command[0])){
							output.println("HTTP/1.0 400 Bad Request");
						} else {
							//see if we can get object if so
							File looking = new File(command[1]);
							if (looking.exists() && looking.canRead()){
								output.println("HTTP/1.0 200 OK");
								if (command[0].equals("GET") || command[0].equals("POST")){
									output.print("Content-Type: " + filetype(command[0]) + "\nContent-Length: " + Long.toString(looking.length()) + "\nLast-Modified: " + looking.lastModified() + "\nContent-Encoding: identity\n");
								}
							} else if (!looking.canRead() && looking.exists()){
								output.println("HTTP/1.0 403 Forbidden");
							} else {
								output.println("HTTP/1.0 404 Not Found");
							}
						}
					} catch (Exception e){
						output.println("HTTP/1.0 400 Bad Request");
					}
				}
				
				//Auto is on, but still unsure -- output.flush();
			} catch (Exception e){
				e.printStackTrace();
			}
			
			try {
				pool.remove(current);
				current.close();
			} catch (IOException e) {
				e.printStackTrace();
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
	
	private String filetype (String s){
		int lastSlash = s.indexOf("/");
		int dotLocation = s.indexOf(".", lastSlash);
		String appType = s.substring(dotLocation + 1);
		String fullType = "";
		switch (appType){
			case "html":
			case "txt":
				fullType = "text/" + appType;
				break;
			case "gif":
			case "jpeg":
			case "png":
				fullType = "image/" + appType;
				break;
			case "pdf":
			case "x-gzip":
			case "zip":
				fullType = "application/" + appType;
			default:
				fullType = "application/octet-stream";
		}
		return fullType;
	}
	
}