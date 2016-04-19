import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.Paths;
import java.text.*;

public class client2 {
	@SuppressWarnings("deprecation")
	
	static String directoryPath;
	static int RFCcounter = 2;
	
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
					// Send ADD request to the server.
					os.writeByte(0);
					os.writeUTF("ADD RFC " + Integer.toString(RFCclist[i].number) + " P2P-CI/1.0");
					os.writeUTF("Host: " + RFCclist[i].hostname);
					os.writeUTF("Port: " + Integer.toString(RFCclist[i].portnum));
					os.writeUTF("Title: " + RFCclist[i].title);
					os.flush();
					
					//Print the status message received from server.
					System.out.println("\n" + is.readUTF());
					System.out.println(is.readUTF());
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
	
	public static void LOOKUP(DataOutputStream os, DataInputStream is, int findRFC, String findTitle, String clienthost, int clientport){
		try{
			// Send the RFC number to server for lookup.
			os.writeByte(1);
			os.writeUTF("LOOKUP RFC " + Integer.toString(findRFC) + " P2P-CI/1.0");
			os.writeUTF("Host: " + clienthost);
			os.writeUTF("Port: " + Integer.toString(clientport));
			os.writeUTF("Title: " + findTitle);
			os.flush();
			int hostcount = 0;
			
			//Wait on server reply which may be positive or negative.
			while(true){
				byte serverMessage = is.readByte();
				
				if(serverMessage == 0 || serverMessage == 2){
					if(serverMessage == 0){
						System.out.println("\n" + is.readUTF());
					}
					break;
				}
				else{
					if(hostcount == 0){
						System.out.println("\n" + is.readUTF());
					}
					System.out.println(is.readUTF());
					hostcount++;
					continue;
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
	
	public static void LIST(DataOutputStream os, DataInputStream is, String clienthost, int clientport){
		try{
			// Send a request to server to send all RFC details.
			os.writeByte(2);
			os.writeUTF("LIST ALL P2P-CI/1.0");
			os.writeUTF("Host: " + clienthost);
			os.writeUTF("Port: " + Integer.toString(clientport));
			os.flush();
			int hostcount = 0;
			
			//Reply from server with all the RFCs that are in pool.
			while(true){
				if(is.readByte() == 1){
					if(hostcount == 0){
						System.out.println("\n" + is.readUTF());
					}
					System.out.println(is.readUTF());
					hostcount++;
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
	
	public static void GET(DataOutputStream os, DataInputStream is, int getRFC, String getHost, int getPort, String clienthost, int clientport, ClientRFC[] RFCclist){
		FileOutputStream fos;
		BufferedOutputStream bos;
		DataOutputStream gos;
		DataInputStream gis;
		String os_name =System.getProperty("os.name");
		Socket getClient;
		String extension = ".txt";
		String fileTransferred = directoryPath+"\\"+getRFC+extension;
		
		try{
			// Initialize a connection to the peer with RFC.
			getClient = new Socket(getHost,getPort);
			gos = new DataOutputStream(getClient.getOutputStream());
			gis = new DataInputStream(getClient.getInputStream());
			// Send the RFC number to the peer with the file.
			gos.writeUTF("GET RFC " + Integer.toString(getRFC) + " P2P-CI/1.0");
			
			String statusMsg = gis.readUTF();
			int status = Integer.parseInt(statusMsg.substring(11, 14));
			System.out.println("\n" + statusMsg);
			if(status == 200){
				System.out.println(gis.readUTF()); // Printing Current Date
				System.out.println(gis.readUTF()); // Printing OS Name
				System.out.println(gis.readUTF()); // Printing Last Modified Date
				String lengthOfContent = gis.readUTF();
				System.out.println(lengthOfContent); // Printing Content Length
				System.out.println(gis.readUTF()); // Printing Content Type
				String getTitle = gis.readUTF();
				System.out.println(getTitle); // Printing Title
				
				// Adding the RFC details in the list at client side.
				RFCclist[RFCcounter] = new ClientRFC();
				RFCclist[RFCcounter].number = getRFC;
				RFCclist[RFCcounter].hostname = clienthost;
				RFCclist[RFCcounter].portnum = clientport;
				RFCclist[RFCcounter].title = getTitle;
				
				// Adding the RFC at the server pool.
				// Send ADD request to the server.
				os.writeByte(0);
				os.writeUTF("ADD RFC " + Integer.toString(RFCclist[RFCcounter].number) + " P2P-CI/1.0");
				os.writeUTF("Host: " + RFCclist[RFCcounter].hostname);
				os.writeUTF("Port: " + Integer.toString(RFCclist[RFCcounter].portnum));
				os.writeUTF("Title: " + RFCclist[RFCcounter].title);
				os.flush();
				
				//Print the status message received from server.
				System.out.println("\n" + is.readUTF());
				System.out.println(is.readUTF());
				
				byte [] mybytearray  = new byte [Integer.parseInt(lengthOfContent.substring(16))];
				InputStream iss = getClient.getInputStream();
				fos = new FileOutputStream(fileTransferred);
				bos = new BufferedOutputStream(fos);
				int bytesRead = iss.read(mybytearray,0,mybytearray.length);
				bos.write(mybytearray, 0 , bytesRead);
				bos.flush();
				System.out.println("File " + fileTransferred + " downloaded (" + bytesRead + " bytes read)");
			}
			else{
				System.out.println("Inside failure");
			}
		}
		catch (UnknownHostException e) {
	        System.err.println("Trying to connect to unknown host: " + e);
	    }
		catch (IOException e) {
	        System.err.println("Couldn't get I/O for the connection to: " + e);
	    }
	}
	
	public static void QUIT(DataOutputStream os, DataInputStream is, String clienthost, int clientport){
		try{
			os.writeByte(4);
			os.writeUTF(clienthost);
			os.writeUTF(Integer.toString(clientport));
			os.flush();
			while(true){
				if(is.readByte() == 1){
					System.out.println("The RFC " + is.readUTF() + ": " + is.readUTF() + " is deleted.");
				}
				else{
					System.out.println("Finished deleting all RFCs of this client on server pool");
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
	
	
    public static void main(String[] args) {
		directoryPath = Paths.get(".").toAbsolutePath().normalize().toString();
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
		RFCclist[0].title = "Message Switching Protocol";
		RFCclist[0].number = 333;
		RFCclist[0].hostname = clienthost;
		RFCclist[0].portnum = clientport;
		RFCclist[1] = new ClientRFC();
		RFCclist[1].title = "Mail Protocol";
		RFCclist[1].number = 555;
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
		
		ServerThread peerThread = new ServerThread(RFCclist, clienthost, clientport);
		peerThread.start();

    if (p2pSocket != null && os != null && is != null) {
            try {
				// Add the RFCs specific to this client to the server pool.
				ADD(os, is, RFCclist);
				
				// Run the loop to select one of the options and perform the selected operation.
				
				while(true){
					System.out.println("\n" + "Select one of the following: \n" + "1. Find an RFC" + "\n" + "2. List all the RFCs" + "\n" + "3. Get a file from a peer" + "\n" + "4. Exit the connection");
					System.out.print("Your selection: ");
					int selection = in.nextInt();
					
					if(selection == 1){
						System.out.print("\n" + "Enter the RFC that you want to find: ");
						int findRFC = in.nextInt();
						System.out.print("\n" + "Enter the title of RFC that you want to find: ");
						in.nextLine();
						String findTitle = in.nextLine();
						LOOKUP(os, is, findRFC, findTitle, clienthost, clientport);
					}
					
					else if(selection == 2){
						LIST(os, is, clienthost, clientport);
					}
					
					else if(selection == 3){
						System.out.print("\n" + "Enter the RFC number that you want to get: ");
						int getRFC = in.nextInt();
						System.out.print("\n" + "Enter the Hostname to connect: ");
						String getHost = in.next();
						System.out.print("\n" + "Enter the Port number to connect: ");
						int getPort = in.nextInt();
						GET(os, is, getRFC, getHost, getPort, clienthost, clientport, RFCclist);
					}
					
					else if(selection == 4){
						QUIT(os, is, clienthost, clientport);
						break;
					}
					
					else{
						System.out.println("Please enter an option from the list given.");
						continue;
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

	public static class ServerThread extends Thread{
		
		// Intializing socket variable for the connected peer as a separate thread.
		ServerSocket peerServer;
		Socket peerClient;
		ClientRFC[] RFClist;
		String extension = ".txt";
		
		// Assigning the connected peerSocket to the socket variable in this thread in the constructor.
		public ServerThread(ClientRFC[] RFCslist, String clienthost, int clientport){
			try{
				peerServer = new ServerSocket(clientport);
				RFClist = RFCslist;
			}
			catch (IOException e) {
				System.out.println("In creation of server socket: ");
				System.out.println(e);
			}
		}
		
		// Main implementation after assigning socket variable.
		public void run(){
			DataInputStream sis;
			DataOutputStream sos;
			FileInputStream fis;
			BufferedInputStream bis;
			OutputStream os;
			try{
				while(true){
					peerClient = peerServer.accept();
					sis = new DataInputStream(peerClient.getInputStream());
					sos = new DataOutputStream(peerClient.getOutputStream());
					String getRFC = sis.readUTF();
					System.out.println("\n" + getRFC);
					int RFCnum = Integer.parseInt(getRFC.substring(8, getRFC.indexOf(" ", 8)));
					String fileToTransfer = directoryPath+"\\"+RFCnum+extension;
					// Check if the file requested is present at the client.
					boolean filePresent = false;
					int index = 0;
					for(int i=0; i < RFClist.length; i++){
						if(RFClist[i] == null){
							break;
						}
						else if(RFClist[i].number == RFCnum){
							filePresent = true;
							index = i;
							break;
						}
						else{
							continue;
						}
					}
					if(filePresent == true){
						File myFile = new File(fileToTransfer);
						Date currentDate = new Date();
						SimpleDateFormat formattedDate = new SimpleDateFormat();
						formattedDate = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
						String DateString = formattedDate.format(currentDate);
						sos.writeUTF("P2P-CI/1.0 200 OK ");
						sos.writeUTF("Date: "+DateString);
						String os_name =System.getProperty("os.name");
						sos.writeUTF("OS: "+ os_name);
						sos.writeUTF("Last-Modified: "+ formattedDate.format(myFile.lastModified()));
						byte [] byteArray  = new byte [(int)myFile.length()];
						sos.writeUTF("Content-Length: "+ byteArray.length);
						sos.writeUTF("Content-Type: text");
						sos.writeUTF(RFClist[index].title);
						
						//Sending the file to the client peer.
						fis = new FileInputStream(myFile);
						bis = new BufferedInputStream(fis);
						bis.read(byteArray,0,byteArray.length);
						os = peerClient.getOutputStream();
						os.write(byteArray,0,byteArray.length);
						os.flush();
						
					}
					else{
						sos.writeUTF("P2P-CI/1.0 404 NOT FOUND");
					}
				}
			}
			catch(IOException e){
				System.out.println("IOException:  "+e);
			}
		}
	}
}