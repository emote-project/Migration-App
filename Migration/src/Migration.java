import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Migration {
	
	
	public static void main(String[] args){
		try {
			System.out.println("Migration server started");
			HashMap<String,String> hm = new HashMap<String,String>();
	        hm.put("directionSkillLevel", "medium");
	        hm.put("distanceSkillLevel", "high");
	        hm.put("symbolSkillLevel", "low");
	        hm.put("directionToolUsed", "true");
	        hm.put("distanceToolUsed", "false");
	        hm.put("symbolToolUsed", "false");
	        
			migrate(hm);
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void migrate(HashMap<String,String> hm) throws IOException, ClassNotFoundException{
		ServerSocket serverSocket = new ServerSocket(5228);
		
		while (true){
			Socket client = serverSocket.accept();
			System.out.println("New client request!" + client.getInetAddress().toString());
			ObjectInputStream in = new ObjectInputStream(client.getInputStream());
			String str = (String) in.readObject();
			if (str.equals("INVITE")){
				
				System.out.println("migrateDataOut : Socket open");
		        
		        ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
		    
		        System.out.println("migrateDataOut : Sending data..");
		        
		        
		        out.writeObject(hm);
		        out.flush();
			}
		}
	}
}
