package it.polimi.tiw.exceptions;

import java.lang.Exception;
public class TooManyChildrenException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = 2006862861738997400L;

	public TooManyChildrenException(String msg) {
        super(msg);
    }
}