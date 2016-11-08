import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import com.example.myapplication.socketdata.AverageRSSI;
import com.example.myapplication.socketdata.SendData;
import com.example.myapplication.socketdata.StudentInformation;

import db.QueryData;
import db.UpdataDB;
import fingerprint.CreateArray;
import fingerprint.FingerMapping;

//안드로이드 프로그래밍통신+보안 기초편 208page 참조

public class FingerPrintingServer extends Thread{
	private ServerSocketChannel serverChannel;
	private Selector selector;
	public static int PORT_NUMBER=8097;
	private static final long TIME_OUT=3000;
	int clientCounter=0;
	private final ByteBuffer buffer=ByteBuffer.allocate(10000);
	private Boolean loop;
	ArrayList<QueryData> ALQueryData=new ArrayList<QueryData>();
	public FingerPrintingServer(int port)
	{
		SocketAddress isa=new InetSocketAddress(port);
		
		try{
			serverChannel=ServerSocketChannel.open();
			serverChannel.configureBlocking(false);
			
			ServerSocket s=serverChannel.socket();
			
			s.bind(isa);
			
			selector=Selector.open();
			
			serverChannel.register(selector, SelectionKey.OP_ACCEPT);
			loop=true;
			
		}catch(IOException e)
		{}
		
	}
	public void run(){
		System.out.println("클라이언트의 접속을 기다립니다.");


		double total_table[][][];	// total_table[0]-> average |  total_table[1]-> var
		CreateArray ca=new CreateArray();
		
		total_table=ca.createArray();
		

		try{
			while(true)
			{
				int n=selector.select(TIME_OUT);
				if(n==0)continue;
				
				Set<SelectionKey> keys=selector.selectedKeys();
				Iterator<SelectionKey> it=keys.iterator();
				
				while(it.hasNext()){
					SelectionKey key=it.next();
					if(!key.isValid())continue;
					
					if(key.isAcceptable()){
						acceptData(key);
						
					}else if(key.isReadable())
					{
						processData(key,total_table);
						
					}
					it.remove();
				}				
			}			
		}catch(Exception e)
		{e.printStackTrace();}
		
	}
	public static Object byteToObject (byte[] bytes)
	{
	  Object obj = null;
	  try {
	    ByteArrayInputStream bis = new ByteArrayInputStream (bytes);
	    ObjectInputStream ois = new ObjectInputStream (bis);
	    obj = ois.readObject();
	  }
	  catch (IOException ex) {
	    //TODO: Handle the exception
	  }
	  catch (ClassNotFoundException ex) {
	    //TODO: Handle the exception
	  }
	  return obj;
	}

protected void acceptData(SelectionKey key)throws Exception{
	ServerSocketChannel server=(ServerSocketChannel)key.channel();
	SocketChannel channel=server.accept();
	channel.configureBlocking(false);
	
	Socket socket=channel.socket();
	SocketAddress remoteAddr=socket.getRemoteSocketAddress();
	System.out.println("Connection opened by client "+remoteAddr+" current user : "+ ++clientCounter+"clients");
		
	channel.register(selector, SelectionKey.OP_READ);
}

protected void processData(SelectionKey key,double total_table[][][])throws Exception{
	if(key==null)
		return;
	
	try{
		SocketChannel channel=(SocketChannel)key.channel();
		
		
		buffer.clear();
		
		int count=channel.read(buffer);
				
		if(count<0){
			Socket socket=channel.socket();
			SocketAddress remoteAddr=socket.getRemoteSocketAddress();
			System.out.println(remoteAddr+" 클라이언트가 접속을 종료하였습니다.");
			clientCounter--;
			channel.close();
			key.cancel();
			return;
		}
		
		if(count>0)
		{
			System.out.println("데이터 들어옴");
			SendData temp;
			//------------------ receive sendData class instance
			
			buffer.flip();
			byte[] receivedData=new byte[count];
			
			if(buffer.remaining()>0)
				key.selector().wakeup();
			
			System.arraycopy(buffer.array(), 0, receivedData, 0, count);
			temp=(SendData)byteToObject(receivedData);	
			
			
			//------------------ mapping with finger printing table
			int resultcoordinate[];

			StudentInformation si = temp.getStudentInformation();
			
			try{
			ArrayList<AverageRSSI> averageRSSI = temp.getAverageRSSIlist();
			
			if(averageRSSI.size()!=0)
			{
			FingerMapping fm = new FingerMapping(); 

			resultcoordinate = fm.mappingWithFingerPrinting(averageRSSI,total_table[0],total_table[1]); 
			
			System.out.println(si.name+"의 X좌표:"+resultcoordinate[0]+" Y좌표: "+resultcoordinate[1]);
			
			// ------------------access database
			QueryData tempQueryData=new QueryData(si.name,si.classID,(int)resultcoordinate[0],(int)resultcoordinate[1],si.checker);
			ALQueryData.add(tempQueryData);
			System.out.println(si.checker);
			}		
			if(ALQueryData.size()==1)
			{
				System.out.println("update db");
				UpdataDB uDB=new UpdataDB(ALQueryData);
				uDB.run();
				ALQueryData=new ArrayList<QueryData>();
			}
			
			}
			catch(NullPointerException e)
			{
				System.out.println("data err"+si.checker);
				if(si.checker==false)
				{
					System.out.println("data err"+si.checker);
					QueryData tempQueryData=new QueryData(si.name,si.classID,-9999,-9999,si.checker);
					
				}
				
			}
		}
		
		
	}catch(Exception e){
		e.printStackTrace();
		try{
			key.channel().close();
			
		}catch(IOException ex){
			ex.printStackTrace();			
		}
		key.selector().wakeup();
	}
	
	
}

	public static void main(String[] args) throws Exception{
		int port=PORT_NUMBER;
		
		FingerPrintingServer server=new FingerPrintingServer(port);
		server.start();
		server.join();
		
		
	}
}