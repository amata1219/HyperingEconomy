package amata1219.hypering.economy;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
public class Util {

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

	public static byte[] toByteArray(String sub, String... messages){
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream output = new DataOutputStream(bytes);
		try{
			output.writeUTF(HyperingEconomyChannel.PACKET_ID);
			output.writeUTF(sub);
			for(String message: messages){
				output.writeUTF(message);
			}
		}catch(IOException e){
			e.printStackTrace();
		}
		return bytes.toByteArray();
	}
}
