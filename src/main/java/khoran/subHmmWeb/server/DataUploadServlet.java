/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package khoran.subHmmWeb.server;

import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.Part;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import khoran.microarrayClustering.SequenceData;
import org.biojava.bio.seq.ProteinTools;

/**
 *
 * @author khoran
 */
public class DataUploadServlet extends HttpServlet {
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
	{

		HttpSession session = request.getSession(true);

		MultipartParser parser = new MultipartParser(request, 5000000,true,true); // 5MB limit
		Part part;
		FilePart filePart;
		while( (part=parser.readNextPart()) != null)
		{
			if(part.isFile())
			{
				filePart=(FilePart)part;
				try{

					SequenceData sData =new SequenceData(new BufferedInputStream(filePart.getInputStream()), ProteinTools.getAlphabet());


					//BufferedReader br = new BufferedReader(new InputStreamReader(filePart.getInputStream()));
					//String line;
					//StringBuilder sb=new StringBuilder();
					//while( (line = br.readLine())!=null)
						//sb.append(line+"\n");
					//br.close();

					System.err.println("storing var "+filePart.getName());
					session.setAttribute(filePart.getName(), sData);

				}catch(IOException e){
					e.printStackTrace();
				}
			}
		}


		//String data = request.getParameter("data");
		//session.setAttribute("data", data);
		//System.err.println("stored data");
    } 

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
