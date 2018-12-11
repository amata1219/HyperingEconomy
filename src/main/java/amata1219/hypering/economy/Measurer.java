package amata1219.hypering.economy;

public abstract class Measurer {

	public abstract void execute();

	public double result(){
		long start = System.nanoTime();

		execute();

		long end = System.nanoTime();

		return (end - start) / 1000000d;
	}

	public void print(){
		System.out.print(result() + "ms");
	}

	public void println(){
		System.out.println(result() + "ms");
	}

}
