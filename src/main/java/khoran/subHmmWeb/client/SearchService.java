/*
 * SearchService.java
 *
 * Created on August 6, 2009, 10:58 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package khoran.subHmmWeb.client;
import com.google.gwt.user.client.rpc.RemoteService;

/**
 *
 * @author khoran
 */
public interface SearchService extends RemoteService
{
	public SearchResult[] search(String data, int numResults,double minScore);

	/**
	 *  Expects  sequence data to be stored in session already
	 * @param numResults
	 * @return
	 */
	public SearchResult[] search(int numResults,double minScore);

	public SearchResult[] fetchStoredResults();
	public String fetchHighlighting();

	public double getSearchProgress();
}
