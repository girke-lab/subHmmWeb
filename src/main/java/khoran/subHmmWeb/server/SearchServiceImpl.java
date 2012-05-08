/*
 * SearchServiceImpl.java
 *
 * Created on August 6, 2009, 10:58 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package khoran.subHmmWeb.server;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import edu.ucf.cs.hmm.visualizer.DPMatrix;
import edu.ucf.cs.hmm.visualizer.Hmm;
import edu.ucf.cs.hmm.visualizer.InvalidFormatException;
import edu.ucf.cs.hmm.visualizer.P7Trace;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import javax.servlet.http.HttpSession;
import khoran.common.TopSet;
import khoran.common.ValuePriority;
import khoran.frag.FragUtils;
import khoran.microarrayClustering.SequenceData;
import khoran.subHmmWeb.client.SearchResult;
import khoran.subHmmWeb.client.SearchService;
import org.apache.log4j.Logger;
import org.biojava.bio.seq.ProteinTools;

/**
 *
 * @author khoran
 */
public class SearchServiceImpl extends RemoteServiceServlet implements
		SearchService
{
//	static{
 //       System.setProperty("log4j.configuration", SearchServiceImpl.class.getResource("log4j.properties").toString());
  //  }
    public static final Logger log =Logger.getLogger(SearchServiceImpl.class);

	//final private static String subHMMFilename = "/home/khoran/projects/protein_profiles/temp/all.profiles";
	//final private static String subHMMFilename = "/home/khoran/projects/protein_profiles/temp/med.profiles";
	//final private static String subHMMFilename = "/home/khoran/projects/protein_profiles/temp/small.profiles";

	// for use on bioweb
	final private static String subHMMFilename = "/var/www/all.profiles";

	private static class Loader
	{
		private final static List<byte[]> profileSources;

		static{
			List<byte[]> list=new LinkedList<byte[]>();
			BufferedReader in = null;
			FileChannel channel=null;
			try{
				log.debug("loading data");
				in = new BufferedReader(new FileReader(subHMMFilename));
				String line;
				MappedByteBuffer buffer;
				while( (line= in.readLine()) !=null)
				{
					channel=new FileInputStream(line).getChannel();
					buffer=channel.map(MapMode.READ_ONLY, 0, channel.size());
					byte[] b = new byte[(int)channel.size()];
					//log.debug("getting "+b.length+" bytes from "+line);
					buffer.get(b);
					channel.close();
					//log.debug(" bytes as string: "+new String(b));
					list.add(compress(b));
					if(list.size() % 1000 ==0)
						log.debug(list.size());
				}
			}catch(IOException e){
				System.err.println("failed to read file list from m"+subHMMFilename+": "+e);
				e.printStackTrace();
			}finally{
				if(in!=null)
					try{ in.close(); }catch(IOException e){}
				if(channel != null)
					try{ channel.close(); } catch(IOException e){}
				profileSources=list;
			}
		}
		private static final byte[] compress(byte[] data) throws IOException
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DeflaterOutputStream dos = new DeflaterOutputStream(baos );

			dos.write(data);
			dos.close();
			return baos.toByteArray();


//			Deflater d =new  Deflater();
//			d.setInput(data);
//			byte[] t = new byte[data.length];
//			int size = d.deflate(t);
//			byte[] result = new byte[size];
//			System.arraycopy(t, 0, result, 0, size);
//			return result;
		}
	}
	public SearchServiceImpl()
	{
		// just to trigger the loading code
		List<byte[]> t =Loader.profileSources;
	}
	private Object getSessionValue(String name)
	{
		HttpSession session = this.getThreadLocalRequest().getSession();
		if(session == null)
		{
			System.err.println("no session found");
			return null;
		}
		return session.getAttribute(name);
	}
	private void setSessionValue(String name, Object o)
	{
		HttpSession session = this.getThreadLocalRequest().getSession(true);
		session.setAttribute(name, o);
	}

	public SearchResult[] search(String data, int numResults,double  minScore)
	{
		try{
			return printResults(score(data, numResults, minScore));
		}catch(NewSearchException e){
			log.warn(e);
			return null;
		}
	}
	public SearchResult[] search( int numResults,double  minScore)
	{
		SequenceData data = (SequenceData) getSessionValue("data");
		if(data == null)
			throw new IllegalStateException("no sequence data stored in session");
		log.trace("uploaded data: "+data);

		try{
			return printResults(score(data, numResults,minScore));
		}catch(NewSearchException e){
			log.warn(e);
			return null;
		}
	}
	public SearchResult[] fetchStoredResults()
	{
		return (SearchResult[]) getSessionValue("searchResults");
	}
	public String fetchHighlighting()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}
	public double getSearchProgress()
	{
		Object o = getSessionValue("progress");
		if(o==null)
			return 0;
		return (Double)o;
	}



	private  SearchResult[] printResults(Map<String,TopSet<ValuePriority<String,ComparableTrace>>> results)
	{
		SearchResult[] sr = new SearchResult[results.size()];
		SequenceData sData = (SequenceData) getSessionValue("data");
		int c=0;
		int index;
		String sequence=null;

		for(Map.Entry<String, TopSet<ValuePriority<String,ComparableTrace>>> entry : results.entrySet())
		{
			Comparable<ValuePriority<String,ComparableTrace>>[] values= entry.getValue().getValues();

			Arrays.sort(values); // sort from small to high

			sr[c]=new SearchResult(entry.getKey(),values.length);

			if(sData != null)
			{
				index = sData.indexOf(entry.getKey());
				sequence=sData.getSequence(index).seqString();
			}

			for(int i=0; i < values.length; i++)
			{
				ValuePriority<String,ComparableTrace> pair = (ValuePriority<String, ComparableTrace>) values[i];
				int[] location = FragUtils.getMatchCoords(pair.getPriority().trace);
				sr[c].setHit(values.length - i - 1, pair.getValue(), pair.getPriority().trace.score, sData==null ? "" :
						//FragUtils.colorString( sequence  , location[0], location[1], FragUtils.colors[ i % FragUtils.colors.length ]));
						FragUtils.colorString( sequence  , location[0], location[1], "red" ));
			}
			c++;
		}
		setSessionValue("searchResults", sr);
		return sr;
	}
	private  Map<String,TopSet<ValuePriority<String,ComparableTrace>>> score(String seqData, int numResults, double minScore)
			throws NewSearchException
	{
		try
		{
			BufferedInputStream bis = makeStream(seqData);
			SequenceData sData = new SequenceData(bis, ProteinTools.getAlphabet());
			setSessionValue("data", sData);
			return score(sData, numResults,minScore);
		}catch(IOException e) {
			log.error(e,e);
			return null;
		}
	}
	private  Map<String,TopSet<ValuePriority<String,ComparableTrace>>> score(SequenceData sData, int numResults, double minScore)
			throws NewSearchException
	{

		Integer searchId = (Integer)getSessionValue("searchId");
		Integer tempSearchId;
		if(searchId == null)
			searchId=0;
		else
			searchId++;
		setSessionValue("searchId", searchId);
		log.debug("searchId: "+searchId);
		log.debug("init stored searchId: "+getSessionValue("searchId"));

		Map<String,TopSet<ValuePriority<String,ComparableTrace>>> results = new LinkedHashMap<String,TopSet<ValuePriority<String,ComparableTrace>>>();
		TopSet<ValuePriority<String,ComparableTrace>>  topSet;

		log.debug("minScore: "+minScore);

		String seq,key;
		DPMatrix workSpace=null;
		int numProfiles, profilesDone=0;

		setSessionValue("progress", 0.0);

		numProfiles = Loader.profileSources.size();
		for(byte[] profileData : Loader.profileSources)
		{
			Hmm profile =new Hmm();
			try{
				profile.read(
						new BufferedReader(
							new InputStreamReader(
								new InflaterInputStream(
									new ByteArrayInputStream(profileData)))));

				if(workSpace==null)
					workSpace = profile.getNewPlan7Matrix();

				for(int i=0; i < sData.size(); i++)
				{
					key=sData.getKeys().get(i);
					seq = sData.getSequence(i).seqString();

					P7Trace trace = profile.doAlign(profile.DigitizeSequence(seq),workSpace);
					
					//log.debug(profile.acc+"  "+key+"  "+trace.score);

					if(trace.score < minScore)
						continue;

					topSet = results.get(key);
					if(topSet == null)
					{
						topSet = new TopSet<ValuePriority<String,ComparableTrace>>(numResults);
						results.put(key, topSet);
					}
					topSet.add(new ValuePriority<String,ComparableTrace>(profile.acc,new ComparableTrace(trace)));
				}
				profilesDone++;


				tempSearchId = (Integer) getSessionValue("searchId");
				if(tempSearchId == null || tempSearchId != searchId)
					throw new NewSearchException("canceling "+searchId+" after seeing "+tempSearchId);

				setSessionValue("progress", (double)profilesDone/numProfiles);
			}catch(InvalidFormatException e){
				e.printStackTrace();
				throw new IllegalStateException(e);
			}catch(IOException e){
				e.printStackTrace();
				throw new IllegalStateException(e);
			}
		}

		return results;
	}

	private BufferedInputStream makeStream(String data)
	{
		return new BufferedInputStream( new ByteArrayInputStream(data.getBytes()));
	}




	static class ComparableTrace implements  Comparable<ComparableTrace>
	{
		P7Trace trace;
		public ComparableTrace(P7Trace trace)
		{
			this.trace=trace;
		}
		public int compareTo(ComparableTrace o)
		{
			return Double.compare(trace.score, o.trace.score);
		}
	}

}
