package it.polimi.tiw.exceptions;

import java.lang.Exception;

public class BlankFieldException extends Exception {
	public BlankFieldException (String msg) {
		super(msg);
	}
}
