package amata1219.hypering.economy.spigot;

public class Result {

	private Object obj;

	public Result(Object obj){
		this.obj = obj;
	}

	public boolean getBoolean(){
		return (boolean) obj;
	}

	public char getChar(){
		return (char) obj;
	}

	public byte getByte(){
		return (byte) obj;
	}

	public short getShort(){
		return (short) obj;
	}

	public int getInt(){
		return (int) obj;
	}

	public long getLong(){
		return (long) obj;
	}

	public float getFloat(){
		return (float) obj;
	}

	public double getDouble(){
		return (double) obj;
	}

	public String getString(){
		return (String) obj;
	}

}
