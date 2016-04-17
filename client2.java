import java.io.*;
import java.net.*;
import java.util.*;

public class client2 {
	@SuppressWarnings("deprecation")
	
	public static class ClientRFC{
		String title;
		int number;
		String hostname;
		int portnum;
	}
	
	public static void ADD(DataOutputStream os, DataInputStream is, ClientRFC[] RFCclist){
		try{
			for(int i=0; i<RFCclist.length; i++){
				if(RFCclist[i] != null){
					os.writeByte(0);
					os.writeUTF(RFCclist[i].title);
					os.writeUTF(Integer.toString(RFCclist[i].number));
					os.writeUTF(RFCclist[i].hostname);
					os.writeUTF(Integer.toString(RFCclist[i].portnum));
					os.flush();
				}
				else{
					break;
				}
			}
		}
		catch (UnknownHostException e) {
	        System.err.println("Trying to connect to unknown host: " + e);
	    } 
	    catch (IOException e) {
	        System.err.println("IOException:  " + e);
		}
	}
	
	public static void LOOKUP(DataOutputStream os, DataInputStream is, int findRFC){
		try{
			// Send the RFC number to server for lookup.
			os.writeByte(1);
			os.writeUTF(Integer.toString(findRFC));
			os.flush();
			
			//Wait on server reply which may be positive or negative.
			byte serverMessage = is.readByte();
			
			if(serverMessage == 0){
				System.out.println("404 not found");
			}
			else{
				System.out.println("LOOKUP RFC " + findRFC + " P2P-CI/1.0");
				System.out.println("Host: " + is.readUTF());
				System.out.println("Port: " + is.readUTF());
				System.out.println("Title: " + is.readUTF());
			}
		}
		catch (UnknownHostException e) {
	        System.err.println("Trying to connect to unknown host: " + e);
	    } 
	    catch (IOException e) {
	        System.err.println("IOException:  " + e);
		}
	}
	
	public static void LIST(DataOutputStream os, DataInputStream is, String clienthost, int clientport){
		try{
			// Send a request to server to send all RFC details.
			os.writeByte(2);
			os.writeUTF(clienthost);
			os.writeUTF(Integer.toString(clientport));
			os.flush();
			//Reply from server with all the RFCs that are in pool.
			
			while(true){
				if(is.readByte() == 1){
					System.out.println("The RFC number: " + is.readUTF() + " with title: " + is.readUTF() + " is at host: " + is.readUTF() + " and port: " + is.readUTF());
				}
				else{
					break;
				}
			}
		}
		catch (UnknownHostException e) {
	        System.err.println("Trying to connect to unknown host: " + e);
	    } 
	    catch (IOException e) {
	        System.err.println("IOException:  " + e);
		}
	}
	
	public static void QUIT(DataOutputStream os, DataInputStream is, String clienthost, int clientport){
		try{
			os.writeByte(4);
		}
		catch (UnknownHostException e) {
	        System.err.println("Trying to connect to unknown host: " + e);
	    } 
	    catch (IOException e) {
	        System.err.println("IOException:  " + e);
		}
	}
	
	
    public static void main(String[] args) {
		// Initializing data stream operations
		Scanner in = new Scanner(System.in);	
		String clienthost = "";
		System.out.println("Enter the port number at which this client should run: ");
		int clientport = in.nextInt();
		// Resolving the hostname of the client.
		try{
			clienthost = InetAddress.getLocalHost().getHostName();
		}
		catch (UnknownHostException ex)
		{
		    System.out.println("Hostname can not be resolved");
		}
		
		//Populating the RFC details of the client manually.
		ClientRFC[] RFCclist = new ClientRFC[100];
		RFCclist[0] = new ClientRFC();
		RFCclist[0].title = "Implementing Sockets 2";
		RFCclist[0].number = 333;
		RFCclist[0].hostname = clienthost;
		RFCclist[0].portnum = clientport;
		RFCclist[1] = new ClientRFC();
		RFCclist[1].title = "Java Socket Programming 2";
		RFCclist[1].number = 444;
		RFCclist[1].hostname = clienthost; 
		RFCclist[1].portnum = clientport;
		
        Socket p2pSocket = null;  
        DataOutputStream os = null;
        DataInputStream is = null;
        try {
            p2pSocket = new Socket("localhost", 7734);
            os = new DataOutputStream(p2pSocket.getOutputStream());
            is = new DataInputStream(p2pSocket.getInputStream());
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: 7734");
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: 7734");
        }

    if (p2pSocket != null && os != null && is != null) {
            try {
				// Add the RFCs specific to this client to the server pool.
				ADD(os, is, RFCclist);
				
				// Run the loop to select one of the options and perform the selected operation.
				
				while(true){
					System.out.println("Select one of the following: \n" + "1. Find an RFC" + "\n" + "2. List all the RFCs" + "\n" + "3. Get a file from a peer" + "\n" + "4. Exit the connection");
					int selection = in.nextInt();
					System.out.println("You have selected: " + selection);
					
					if(selection == 1){
						System.out.println("\n" + "Enter the RFC that you want to find: ");
						int findRFC = in.nextInt();
						LOOKUP(os, is, findRFC);
					}
					
					else if(selection == 2){
						System.out.println("\n" + "Listing all the RFCs currently in server pool" + "\n");
						LIST(os, is, clienthost, clientport);
					}
					
					else if(selection == 4){
						QUIT(os, is, clienthost, clientport);
					}
					
					else{
						break;
					}
				}
				
				os.close();
                is.close();
                p2pSocket.close(); 				
            } catch (UnknownHostException e) {
                System.err.println("Trying to connect to unknown host: " + e);
            } catch (IOException e) {
                System.err.println("IOException:  " + e);
            }
        }
    }           
}