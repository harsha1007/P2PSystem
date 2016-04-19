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
						// Reading the request from client.
						String addnumber = is.readUTF();
						String addhost = is.readUTF();
						String addport = is.readUTF();
						String addtitle = is.readUTF();
						// Printing the request to server screen;
						System.out.println("\n" + addnumber);
						System.out.println(addhost);
						System.out.println(addport);
						System.out.println(addtitle);
						// Storing the values in RFC pool.
						RFCslist[counter] = new RFCDetails();
						RFCslist[counter].number = Integer.parseInt(addnumber.substring(8, 11));
						RFCslist[counter].hostname = addhost.substring(6);
						RFCslist[counter].portnum = Integer.parseInt(addport.substring(6));
						RFCslist[counter].title = addtitle.substring(7);
						os.writeUTF("P2P-CI/1.0 200 OK");
						os.writeUTF("RFC " + RFCslist[counter].number + " " + RFCslist[counter].title + " " + RFCslist[counter].hostname + " " + RFCslist[counter].portnum);
						counter++;
					}
					
					// The LookUp operation initiated by a client. Positive/Negative reply based on the success/failure of lookup.
					else if (clientAction == 1){
						// Reading LOOKUP request from client.
						String lookupnumber = is.readUTF();
						String lookuphost = is.readUTF();
						String lookupport = is.readUTF();
						String lookuptitle = is.readUTF();
						// Printing LOOKUP request to the server screen.
						System.out.println("\n" + lookupnumber);
						System.out.println(lookuphost);
						System.out.println(lookupport);
						System.out.println(lookuptitle);
						int findRFC = Integer.parseInt(lookupnumber.substring(11, 14));
						boolean RFCfound = false;
						int hostcount = 0;
						for(int i=0; i < RFCslist.length; i++){
							if(RFCslist[i] == null){
								if(RFCfound == false){
									os.writeByte(0);
									os.writeUTF("P2P-CI/1.0 404 NOT FOUND");
									os.flush();
									break;
								}
								else{
									os.writeByte(2);
									os.flush();
									break;
								}
							}
							else if(RFCslist[i].number == findRFC){
								RFCfound = true;
								os.writeByte(1);
								if(hostcount == 0){
									os.writeUTF("P2P-CI/1.0 200 OK");
								}
								os.writeUTF("RFC " + RFCslist[i].number + " " + RFCslist[i].title + " " + RFCslist[i].hostname + " " + RFCslist[i].portnum);
								os.flush();
								hostcount++;
								continue;
							}
							else{
								continue;
							}
						}
					}
					
					// Listing all RFCs in pool upon client request.
					else if (clientAction == 2){
						System.out.println("\n" + is.readUTF());
						String listhost = is.readUTF();
						String listport = is.readUTF();
						System.out.println("Host: " + listhost);
						System.out.println("Port: " + listport);
						int hostcount = 0;
						
						for(int i=0; i < RFCslist.length; i++){
							if(RFCslist[i] == null){
								os.writeByte(0);
								break;
							}
							else if(RFCslist[i].number != -1 ){
								os.writeByte(1);
								if(hostcount == 0){
									os.writeUTF("P2P-CI/1.0 200 OK");
								}
								os.writeUTF("RFC " + RFCslist[i].number + " " + RFCslist[i].title + " " + RFCslist[i].hostname + " " + RFCslist[i].portnum);
								os.flush();
								hostcount++;
							}
							else{
								continue;
							}
						}
					}
					
					else if (clientAction == 4){
						String clientHost = is.readUTF();
						int clientPort = Integer.parseInt(is.readUTF());
						for(int i=0; i < RFCslist.length; i++){
							if(RFCslist[i] == null){
								os.writeByte(0);
								break;
							}
							else if(RFCslist[i].hostname.equals(clientHost) && RFCslist[i].portnum == clientPort){
								os.writeByte(1);
								os.writeUTF(Integer.toString(RFCslist[i].number));
								RFCslist[i].number = -1;
								os.writeUTF(RFCslist[i].title);
								RFCslist[i].title = "-1";
								RFCslist[i].hostname = "-1";
								RFCslist[i].portnum = -1;
								os.flush();
							}
							else{
								continue;
							}
						}
						break;
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