import java.io.*;
import java.net.*;
public class server {
	
	// Class template to store the RFC details shared by clients.
	public static class RFCDetails{
		String title;
		int number;
		String hostname;
		int portnum;
	}
	
	// Global variables to track RFC pool and current index in the pool.
	static RFCDetails[] RFCslist = new RFCDetails[10000];
	static int counter = 0;
	
    public static void main(String args[]) {
		//Initializing socket variables for server and peer.
        ServerSocket CIServer = null;
        Socket peerSocket = null;
		
		// Opening a socket on port 7734 for the CI Server so that peers can connect to it.
		try {
           CIServer = new ServerSocket(7734);
        }
        catch (IOException e) {
           System.out.println(e);
        }   
		
// Create a socket object for the CIServer to listen for the peer connections. Each connected peer will have a separate thread running on the server. 
	while (true) {
		try {
			   peerSocket = CIServer.accept();
			   new ServerThread(peerSocket).start();
			}   
		catch (IOException e) {
			   System.out.println(e);
			}
	}
    }
	
	public static class ServerThread extends Thread{
		
		// Intializing socket variable for the connected peer as a separate thread.
		protected Socket socket;
		
		// Assigning the connected peerSocket to the socket variable in this thread in the constructor.
		public ServerThread(Socket peerSocket){
			this.socket = peerSocket;
		}
		
		// Main implementation after assigning socket variable.
		public void run(){
			
			// Declaring input and output data streams.
			String line;
			DataInputStream is;
			DataOutputStream os;
			try{
				is = new DataInputStream(socket.getInputStream());
				os = new DataOutputStream(socket.getOutputStream());
				while (true) {
					// Reading the input from client in UTF format.
					byte clientAction = is.readByte();
					
					// If a new client joins the pool, its RFC details are added.
					if (clientAction == 0){
						RFCslist[counter] = new RFCDetails();
						RFCslist[counter].title = is.readUTF();
						RFCslist[counter].number = Integer.parseInt(is.readUTF());
						RFCslist[counter].hostname = is.readUTF();
						RFCslist[counter].portnum = Integer.parseInt(is.readUTF());
						System.out.println("\n" + "ADD RFC " + RFCslist[counter].number +" P2P-CI/1.0");
						System.out.println("Host: " + RFCslist[counter].hostname);
						System.out.println("Port: " + RFCslist[counter].portnum);
						System.out.println("Title: " + RFCslist[counter].title);
						counter++;
					}
					
					// The LookUp operation initiated by a client. Positive/Negative reply based on the success/failure of lookup.
					else if (clientAction == 1){
						int findRFC = Integer.parseInt(is.readUTF());
						for(int i=0; i < RFCslist.length; i++){
							if(RFCslist[i] == null){
								os.writeByte(0);
								os.flush();
								break;
							}
							else if(RFCslist[i].number == findRFC){
								os.writeByte(1);
								os.writeUTF(RFCslist[i].hostname);
								os.writeUTF(Integer.toString(RFCslist[i].portnum));
								os.writeUTF(RFCslist[i].title);
								os.flush();
								break;
							}
							else{
								continue;
							}
						}
					}
					
					// Listing all RFCs in pool upon client request.
					else if (clientAction == 2){
						System.out.println("\n" + "LIST ALL P2P-CI/1.0");
						System.out.println("Host: " + is.readUTF());
						System.out.println("Port: " + is.readUTF());
						
						for(int i=0; i < RFCslist.length; i++){
							if(RFCslist[i] == null){
								os.writeByte(0);
								break;
							}
							else if(RFCslist[i].number != -1 ){
								os.writeByte(1);
								os.writeUTF(Integer.toString(RFCslist[i].number));
								os.writeUTF(RFCslist[i].title);
								os.writeUTF(RFCslist[i].hostname);
								os.writeUTF(Integer.toString(RFCslist[i].portnum));
								os.flush();
							}
							else{
								continue;
							}
						}
					}
					
					else if (clientAction == 4){
						socket.close();
						return;
					}
					
					else{
						break;
					}
				}
				os.close();
	            is.close();
				socket.close();
			}
			catch (IOException e) {
			   System.out.println(e);
			}
		}
	}
}