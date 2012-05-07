/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package khoran.subHmmWeb.server;

/**
 * Thrown by an older search when it detects that a new search
 * has been started and thus its own results should not be reported.
 * @author khoran
 */
public class NewSearchException extends Exception {

    /**
     * Creates a new instance of <code>NewSearchException</code> without detail message.
     */
    public NewSearchException() {
    }


    /**
     * Constructs an instance of <code>NewSearchException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public NewSearchException(String msg) {
        super(msg);
    }
}
