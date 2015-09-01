import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class TFTPClient {
	public static void main(String []args) throws IOException{
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String hostName = new String();
		System.out.print("tftp> ");
		String command = in.readLine();
		String[] commandArray = command.split(" ");
		hostName = checkForHostName(commandArray);		
		System.out.print("tftp> ");
		command = in.readLine();
		while(!command.equalsIgnoreCase("quit")){
			String[] stringArray = command.split(" ");
			if(stringArray[0].equalsIgnoreCase("get")){
				if(stringArray.length == 1){
					System.out.print("(files) ");
					command = in.readLine();
					DatagramSocket clientSocket = new DatagramSocket();
					readData(command,clientSocket, hostName);
				}else{
					DatagramSocket clientSocket = new DatagramSocket();
					readData(stringArray[1],clientSocket, hostName);
				}
			}else{
				System.out.println("?Invalid command");
			}
			System.out.print("tftp> ");
			command = in.readLine();
		}
	}

	private static String checkForHostName(String[] commandArray) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String hostName = new String();
		String command = new String();
		boolean goAhead = false;
		while(!goAhead){
			while(!commandArray[0].equalsIgnoreCase("connect") || commandArray.length>2){
				if(commandArray.length>2){
					System.out.println("?Invalid command");
				}else{
					System.out.println("usage: connect to a server");
				}
				System.out.print("tftp> ");
				command = in.readLine();
				commandArray = command.split(" ");
			}
			if(commandArray.length == 1){
				System.out.print("(to) ");
				hostName = in.readLine();
			}else if(commandArray.length == 2){
				hostName = commandArray[1];
			}
			try{
				InetAddress hostAddress = InetAddress.getByName(hostName);
				goAhead = true;
			}catch(UnknownHostException e){
				System.out.println(hostName+": unknown host");
				System.out.print("tftp> ");
				command = in.readLine();
				commandArray = command.split(" ");
			}
		}
		return hostName;
	}

	private static void readData(String command, DatagramSocket clientSocket, String hostName) throws IOException {
		int hostPort = 69, dataSize = 0;
		boolean display = false;
		FileOutputStream myFile = null;
		long startTime = System.currentTimeMillis();
		InetAddress hostAddress = InetAddress.getByName(hostName);
		ByteArrayOutputStream requestBytes = createPacket(command);
		byte[] myResponseData = new byte[516];
		byte[] myRequestData = new byte[516];
		byte[] fileData = new byte[512];
		myRequestData = requestBytes.toByteArray();
		DatagramPacket myDatagramReceivePacket = new DatagramPacket(myResponseData, myResponseData.length);
		DatagramPacket myDatagramSendPacket = 
				new DatagramPacket(myRequestData,requestBytes.size(),hostAddress,hostPort);
		clientSocket.send(myDatagramSendPacket);
		clientSocket.setSoTimeout(5000);
		try{
			do{
				clientSocket.receive(myDatagramReceivePacket);
				hostPort = myDatagramReceivePacket.getPort();
				byte[] opcode = new byte[2];
				byte[] blockNumber = new byte[2];
				opcode = Arrays.copyOfRange(myDatagramReceivePacket.getData(), 0, 2);
				if(opcode[1] == 5){
					readError(myDatagramReceivePacket);
				}else if(opcode[1] == 3){
					clientSocket.setSoTimeout(999999999);
					display = true;
					blockNumber = Arrays.copyOfRange(myDatagramReceivePacket.getData(), 2, 4);
					if(myFile == null)
						myFile = new FileOutputStream(command);
					fileData = Arrays.copyOfRange(myDatagramReceivePacket.getData(), 4, myDatagramReceivePacket.getLength());
					myFile.write(fileData);
					dataSize+=myDatagramReceivePacket.getLength();
					ByteArrayOutputStream ackBytes = new ByteArrayOutputStream();
					ackBytes.write(0);
					ackBytes.write(4);
					ackBytes.write(blockNumber);
					for(int i = ackBytes.size();i<516;i++){
						ackBytes.write(0);
					}
					DatagramPacket myDatagramAckSendPacket = 
							new DatagramPacket(ackBytes.toByteArray(),ackBytes.size(),hostAddress,hostPort);
					clientSocket.send(myDatagramAckSendPacket);
				}
			}while((myDatagramReceivePacket.getLength()==516));
			if(myFile!=null)
				myFile.close();
			if(display){
				long endTime = System.currentTimeMillis() - startTime;
				System.out.println("Tranferred "+dataSize+" bytes in "+ endTime +" milliseconds.");
			}
		}catch(SocketTimeoutException s){
			System.out.println("Transfer timed out.");
		}
	}

	private static void readError(DatagramPacket myDatagramReceivePacket) {
		byte[] errorCode = Arrays.copyOfRange(myDatagramReceivePacket.getData(), 2, 4);
		String errorMsg = 
				new String(myDatagramReceivePacket.getData(), 4, myDatagramReceivePacket.getLength()-5).toString();
		System.out.println("Error code " + errorCode[1]+ ": " + errorMsg);
	}

	private static ByteArrayOutputStream createPacket(String command) throws IOException {
		ByteArrayOutputStream myByteStream = new ByteArrayOutputStream();
		myByteStream.write(0);
		myByteStream.write(1);
		myByteStream.write(command.getBytes());
		myByteStream.write(0);
		myByteStream.write("octet".getBytes());
		myByteStream.write(0);
		for(int i = myByteStream.size();i<516;i++){
			myByteStream.write(0);
		}
		return myByteStream;
	}
}