package it.polimi.tiw.exceptions;

import java.lang.Exception;
public class TooManyChildrenException extends Exception {
    public TooManyChildrenException(String msg) {
        super(msg);
    }
}