package it.polimi.tiw.exceptions;

import java.lang.Exception;

public class BlankFieldException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5046190295408853082L;

	public BlankFieldException (String msg) {
		super(msg);
	}
}
