import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable{

	private ArrayList<ConnectionHandler> connections;
	private ServerSocket server;
	private boolean done;
	private ExecutorService pool;
	
	public Server() {
	    connections = new ArrayList<>();
	    done = false;
	    pool = Executors.newCachedThreadPool();  
	}

	@Override
	public void run() {
		
		try {
			
			server = new ServerSocket(9999);
			pool = Executors.newCachedThreadPool();
			while(!done) {
			Socket client= server.accept();
			
			ConnectionHandler handler = new ConnectionHandler(client);
			
			connections.add(handler);
			pool.execute(handler);
			
		}} catch (Exception e) {
			
			shutdown();
		}
	}
	
	public void broadcast(String message) {
	    

	    for (ConnectionHandler ch : connections) {
	        if (ch != null && ch.out != null) {
	            ch.SendMessage(message);  
	            System.out.println("Sent to: " + ch.name);  
	        }
	    }
	}


	
	public void shutdown() {
	    try {
	        done = true;
	        
	        if (pool != null) {  
	            pool.shutdown();
	        }
	        
	        if (server != null && !server.isClosed()) {
	            server.close();
	        }

	        if (connections != null) {  
	            for (ConnectionHandler ch : connections) {
	                ch.shutdown();
	            }
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}

	
	
	class ConnectionHandler implements Runnable{
		
		private Socket client;
		
		private BufferedReader in;
		
		private PrintWriter out;
		
		private String name;
		
		public ConnectionHandler(Socket client) {
			this.client=client;
		}
		
		@Override 
		public void run() {
			try {
				
				out = new PrintWriter(client.getOutputStream() , true);
				
				in = new BufferedReader(new InputStreamReader(client.getInputStream()));
				
				out.println("Please enter your name:");
				
				name = in.readLine();
				
				// TODO:check for valid user name
				
				System.out.println(name + " connected");
				
				broadcast(name + " has joined the chat room! ");
				String message;
				
				while ((message = in.readLine()) != null) {
				    if (message.startsWith("/name")) {
				        name = in.readLine();
				        System.out.println("your nick name has successfully been changed!");
				    } else if (message.startsWith("/quit")) {
				        broadcast(name + " left the chat!");
				        shutdown();
				        break;
				    } else {
				        broadcast(name + ": " + message); 
				    }
				}

			
			} catch (IOException e) {
				
				shutdown();
			}
		}
		
		public void SendMessage(String message) {
		    if (out != null) {  
		        out.println(message);
		        out.flush();  
		    }
		}

		
		public void shutdown() {
			try {
				in.close();
				out.close();
				if(!client.isClosed()){
					client.close();
			}
				}
				catch (IOException e) {
				
			
			}
		}
	}
	public static void main(String [] args) {
		Server server = new Server();
		server.run();
	}
}
