/*
 * MainEntryPoint.java
 *
 * Created on August 6, 2009, 10:25 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package khoran.subHmmWeb.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 *
 * @author khoran
 */
public class MainEntryPoint implements EntryPoint , ClickHandler, FormPanel.SubmitCompleteHandler
{
	
	final Label title=new Label("Search Sub-HMMs");
	final Label seqTitle = new Label("Sequence data in fasta format: ");
	final Label result=new Label();
	final Label searchTime=new Label();
	final TextArea seqData = new TextArea();
	final TextBox numResultsBox = new TextBox();
	final TextBox minScoreBox = new TextBox();
	final Button searchButton =new Button("Search");
	final FlexTable resultTable = new FlexTable();
	final FormPanel form =new FormPanel();
	final FileUpload fileUpload = new FileUpload();
	final Button clearButton=new Button("clear");
	Timer progressTimer;

	long searchStartTime;

	/** Creates a new instance of MainEntryPoint */
	public MainEntryPoint()
	{
		progressTimer=new Timer(){
			public void run() {
				getProgress();
			}
		};
	}
	
	/**
	 * The entry point method, called automatically by loading a module
	 * that declares an implementing class as an entry-point
	 */
	public void onModuleLoad()
	{

		searchButton.addClickHandler(this);
		clearButton.addClickHandler(this);

		VerticalPanel formPanel= new VerticalPanel();
		VerticalPanel mainPanel= new VerticalPanel();
		Panel temp;

		form.setAction("/subHmmWeb/DataUploadServlet");
		form.setEncoding(FormPanel.ENCODING_MULTIPART);
		form.setMethod(FormPanel.METHOD_POST);
		form.setWidget(formPanel);
		form.addSubmitCompleteHandler(this);

		fileUpload.setName("data");

		resultTable.setStyleName("result-table");

		seqData.setCharacterWidth(70);
		seqData.setVisibleLines(20);

		//title.setStyleName("page-title");
		//mainPanel.add(title);

		mainPanel.add(form);
		mainPanel.add(searchTime);
		mainPanel.add(result);
		mainPanel.add(resultTable);

		temp=new HorizontalPanel();
		((HorizontalPanel)temp).setSpacing(4);
		temp.add(new Label("Number of results: "));
		temp.add(numResultsBox);
		formPanel.add(temp);

		temp=new HorizontalPanel();
		((HorizontalPanel)temp).setSpacing(4);
		temp.add(new Label("Min score: "));
		temp.add(minScoreBox);
		formPanel.add(temp);

		formPanel.add(seqTitle);
		formPanel.add(seqData);

		temp=new HorizontalPanel();
		((HorizontalPanel)temp).setSpacing(4);
		temp.add(new Label("Or upload a fasta file: "));
		temp.add(fileUpload);

		formPanel.add(temp);
		formPanel.add(searchButton);

		
		RootPanel.get("shmm-search").add(mainPanel);

		// see if we have any existing results to display
		searchStartTime= System.currentTimeMillis();
		getService().fetchStoredResults(new AsyncCallback<SearchResult[]>(){
			public void onFailure(Throwable caught) {
			}
			public void onSuccess(SearchResult[] searchResults) {
				if(searchResults != null)
					loadTable(searchResults);
			}
		});
	}

	public void onClick(ClickEvent event)
	{
		if(event.getSource() == searchButton)
		{
			clearTable();
			searchTime.setText("");
			result.setText("fetching results...");
			searchStartTime= System.currentTimeMillis();
			progressTimer.scheduleRepeating(1000);

			if( ! fileUpload.getFilename().equals(""))
			{
				form.submit();
			}
			else
			{
				getService().search(seqData.getText(), getNumResults(),getMinScore() ,new AsyncCallback<SearchResult[]>(){
					public void onFailure(Throwable caught) {
						progressTimer.cancel();
						result.setText("search failed");
					}
					public void onSuccess(SearchResult[] searchResults) {
						// indicates that this search was aborted, not a failure
						if(searchResults == null)
							return;
						progressTimer.cancel();
						loadTable(searchResults);
					}
				});
			}
		}
		else if(event.getSource() == clearButton)
		{
			clearTable();
		}
	}


	public void onSubmitComplete(SubmitCompleteEvent event)
	{
		getService().search(getNumResults(), getMinScore() ,new AsyncCallback<SearchResult[]>(){
			public void onFailure(Throwable caught) {
				progressTimer.cancel();
				result.setText("search failed");
			}
			public void onSuccess(SearchResult[] searchResults) {
				// indicates that this search was aborted, not a failure
				if(searchResults == null)
					return;
				progressTimer.cancel();
				loadTable(searchResults);
			}
		});
	}
	public void getProgress()
	{
		getService().getSearchProgress(new AsyncCallback<Double>(){
			public void onFailure(Throwable caught) {
			}
			public void onSuccess(Double progress) {
				//result.setText((int)(progress*100)+"% done"+System.currentTimeMillis());
				result.setText((int)(progress*100)+"% done");
			}
		});
	}
	int getNumResults()
	{
		int numResults=10;
		try{
			if( ! numResultsBox.getText().equals(""))
				numResults = Integer.parseInt(numResultsBox.getText());
		}catch(NumberFormatException e){ }

		return numResults;
	}
	double getMinScore()
	{
		double minScore=0;
		try{
			if( ! minScoreBox.getText().equals(""))
				minScore = Double.parseDouble(minScoreBox.getText());
		}catch(NumberFormatException e){ }

		return minScore;
	}

	void clearTable()
	{
		if(resultTable == null)
			return;
		int numRows = resultTable.getRowCount();
		for(int i=0; i < numRows; i++)
		//while(resultTable.getRowCount() != 0)
			resultTable.removeRow(0);
	}
	void loadTable(SearchResult[] searchResults)
	{
		result.setText("");
		searchTime.setText("Search time: "+  (System.currentTimeMillis() - searchStartTime)+"ms"   );

		resultTable.setBorderWidth(2);
		int row=0,base;


		resultTable.setText(row,0,"Query");
		resultTable.getCellFormatter().addStyleName(row, 0, "header");

		resultTable.setText(row,1,"Sub-HMM");
		resultTable.getCellFormatter().addStyleName(row, 1, "header");

		resultTable.setText(row,2,"Score");
		resultTable.getCellFormatter().addStyleName(row, 2, "header");

		resultTable.setText(row,3,"Sequence");
		resultTable.getCellFormatter().addStyleName(row, 3, "header");
		row++;

		for(int i=0; i < searchResults.length; i++)
		{
			resultTable.setText(row,0, searchResults[i].sequenceKey);
			resultTable.getFlexCellFormatter().setRowSpan(row, 0, searchResults[i].hits.length);
			resultTable.getCellFormatter().addStyleName(row, 0, "query");

			for(int h=0; h < searchResults[i].hits.length; h++)
			{
				base =  h==0 ? 1 : 0;
				resultTable.setHTML(row, base,  "<a href='"+shmmLink(searchResults[i].hits[h][0])+"' >"+searchResults[i].hits[h][0]+"</a>");
				resultTable.setText(row, base+1, searchResults[i].hits[h][1]);
				resultTable.setHTML(row, base+2, searchResults[i].hits[h][2]);
				resultTable.getFlexCellFormatter().setWordWrap(row, base+2, false);
				row++;
			}
		}

	}
	String shmmLink(String shmm)
	{
		String[] parts = shmm.split("-");
		String link =  " /scripts/displaySubHMM.pl?familyName="+parts[0];
		if(parts.length > 1)
			link += "&subHMM="+parts[1];
		return link;
	}

	public static SearchServiceAsync getService()
	{
		// Create the client proxy. Note that although you are creating the
		// service interface proper, you cast the result to the asynchronous
		// version of
		// the interface. The cast is always safe because the generated proxy
		// implements the asynchronous interface automatically.
		SearchServiceAsync service = (SearchServiceAsync) GWT.create(SearchService.class);
		// Specify the URL at which our service implementation is running.
		// Note that the target URL must reside on the same domain and port from
		// which the host page was served.
		//
		ServiceDefTarget endpoint = (ServiceDefTarget) service;
		String moduleRelativeURL = GWT.getModuleBaseURL() + "searchservice";
		endpoint.setServiceEntryPoint(moduleRelativeURL);
		return service;
	}

}
