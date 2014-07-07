package Model;

public class DebugPrint {
	private static final Boolean show = Boolean.TRUE;;
	public static DebugPrint printer;
	public static void print (String str) {
		if(show)
			System.out.println(str);
	}

}
