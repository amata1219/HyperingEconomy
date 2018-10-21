package amata1219.hypering.economy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Base64;

public class DataUtility {

	public DataUtility(){

	}

	public static String toBase64(PlayerData data){
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		ObjectOutput output;
		try{
			output = new ObjectOutputStream(bytes);
			output.writeObject(data);
			output.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		return Base64.getEncoder().encodeToString(bytes.toByteArray());
	}

	public static PlayerData fromBase64(String text){
		PlayerData data = null;
		try{
			data = (PlayerData) new ObjectInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(text))).readObject();
		}catch(IOException e){
			e.printStackTrace();
		}catch(ClassNotFoundException e){
			e.printStackTrace();
		}
		return data;
	}

}
