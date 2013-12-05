package grammar.input.xml;

public class IllegalLoadOrderException extends RuntimeException {
	private static final long serialVersionUID = 348323403233L;
	
	public IllegalLoadOrderException(String message) {
		super(message);
	}
}
