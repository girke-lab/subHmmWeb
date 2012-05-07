/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package khoran.subHmmWeb.client;

import java.io.Serializable;


/**
 *
 * @author khoran
 */
public class SearchResult  implements Serializable
{
	private static final long serialVersionUID=842234L;
	public String sequenceKey;
	public String[][] hits;

	public SearchResult()
	{
		sequenceKey=null;
		hits=null;
	}
	public SearchResult(String sequenceKey, int numHits)
	{
		this.sequenceKey=sequenceKey;
		hits= new String[numHits][];
	}
	public void setHit(int index, String name, double score)
	{
		setHit(index,name,score,"");
	}
	public void setHit(int index, String name, double score,String highlightedSequence)
	{
		hits[index] = new String[3];
		hits[index][0] = name;
		hits[index][1] = Double.toString(score);
		hits[index][2] = highlightedSequence;
	}
}
