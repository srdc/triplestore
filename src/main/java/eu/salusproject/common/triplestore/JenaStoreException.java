package eu.salusproject.common.triplestore;

public class JenaStoreException extends Exception {

	private static final long serialVersionUID = -1673845451000257156L;
	
	public JenaStoreException(String msg) {
		super(msg);
	}

	public JenaStoreException(Throwable cause) {
		super(cause);
	}

	public JenaStoreException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
