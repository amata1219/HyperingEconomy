package amata1219.hypering.economy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class HyperingEconomyChannel {

	public static final String PACKET_ID = "HyperingEconomy";

	private String message;

	private HyperingEconomyChannel(){

	}

	public static HyperingEconomyChannel newInstance(DataInputStream stream){
		return new HyperingEconomyChannel();
	}

	public void read(DataInputStream stream){
		try {
			message = stream.readUTF();
			System.out.println(message);
		}catch(IOException e){
			e.printStackTrace();
			message = null;
		}
	}

	public void write(DataOutputStream stream){
		try {
			stream.writeUTF(message);
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	public String getMessage(){
		return message;
	}

	public boolean isNull(){
		return message == null;
	}

}
