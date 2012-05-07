/*
 * SearchServiceAsync.java
 *
 * Created on August 6, 2009, 10:58 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package khoran.subHmmWeb.client;
import com.google.gwt.user.client.rpc.AsyncCallback;


/**
 *
 * @author khoran
 */
public interface SearchServiceAsync
{


	public abstract void search(java.lang.String data, int numResults, double minScore, AsyncCallback<SearchResult[]> asyncCallback);

	public abstract void search(int numResults, double minScore, AsyncCallback<SearchResult[]> asyncCallback);

	public abstract void fetchStoredResults(AsyncCallback<SearchResult[]> asyncCallback);

	public abstract void fetchHighlighting(AsyncCallback<String> asyncCallback);
	public abstract void getSearchProgress(AsyncCallback<Double> asyncCallback);
}
