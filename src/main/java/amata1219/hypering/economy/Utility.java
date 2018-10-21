package amata1219.hypering.economy;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public class Utility {

	public static String getString(DataInputStream input){
		try {
			return input.readUTF();
		}catch(IOException e){
			e.printStackTrace();
		}
		return null;
	}

	public static int getInt(DataInputStream input){
		String s = getString(input);
		if(s == null)
			return -1;
		return Integer.valueOf(s).intValue();
	}

	public static long getLong(DataInputStream input){
		String s = getString(input);
		if(s == null)
			return -1L;
		return Long.valueOf(s).longValue();
	}

	public static byte[] toByteArray(Channel channel, UUID sender, UUID uuid, String... messages){
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream output = new DataOutputStream(bytes);
		try{
			output.writeUTF(channel.toString());
			output.writeUTF(sender.toString());
			output.writeUTF(uuid.toString());
			for(String message: messages){
				output.writeUTF(message);
			}
		}catch(IOException e){
			e.printStackTrace();
		}
		return bytes.toByteArray();
	}

	public static byte[] toByteArray(Channel channel, UUID sender, String... messages){
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream output = new DataOutputStream(bytes);
		try{
			output.writeUTF(channel.toString());
			output.writeUTF(sender.toString());
			for(String message: messages){
				output.writeUTF(message);
			}
		}catch(IOException e){
			e.printStackTrace();
		}
		return bytes.toByteArray();
	}

}
