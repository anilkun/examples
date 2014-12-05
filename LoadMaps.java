package naive.search.servlets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class LoadMaps
 */


import javax.servlet.http.HttpSession;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;


import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import irs2.bin.edu.asu.irs13.*;
import irs2.src.edu.asu.irs13.PageRankVectorSimilarity;
import irs2.src.edu.asu.irs13.RankResults;
//@WebServlet("/LoadMaps")
public class LoadMaps extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public PageRankVectorSimilarity pagerankObj;
    /**
     * Default constructor. 
     */
    public LoadMaps() {
        // TODO Auto-generated constructor stub
    	
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
//		
	}

	/**
	 * @see Servlet#getServletConfig()
	 */
	public ServletConfig getServletConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	
	//Servlet to handle post request
	//Instantiate the required classes and get the output
	//Create a response object and dispatch the request, response to the jsp.
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpSession session = request.getSession(true);
		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();
		String query = request.getParameter("search_query");
		int topk = Integer.parseInt(request.getParameter("topk"));
		String type = request.getParameter("rank_type");
		PageRankVectorSimilarity pagerankObj = new PageRankVectorSimilarity();
		
		try {
			pagerankObj.createMap();
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			pagerankObj.calculate2NormDoc();
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(type.equals("cumulative"))
		{
	    pagerankObj.computePageRankOverride(25504);
		
		out.println(query+","+topk+","+type+",");
		pagerankObj.handleQueryRequest(query,topk,type, pagerankObj);
		int[] x = pagerankObj.getResult1();
		int[] y = pagerankObj.getResult2();
		System.out.println(x.length);
		StringBuffer outbuf = new StringBuffer();
		IndexReader r = IndexReader.open(FSDirectory.open(new File("C:\\Users\\ANIL\\workspace\\NaiveSearch\\index")));
		outbuf.append(" Pagerank + vector similarity values are");
		outbuf.append("<table>");
		for(int i=0;i<topk;i++){
			outbuf.append("<tr><td>");
			outbuf.append(x[i]);
			outbuf.append("</td>");
			Document d = r.document(x[i]);
			outbuf.append("<td>");
			String url = d.getFieldable("path").stringValue();
//			url = url.replace("%%", "/");
			outbuf.append("<a href="+url+">"+url+"</a>");
			outbuf.append("</td>");
			outbuf.append("</tr>");
		}
		outbuf.append("</table>");
		outbuf.append(" Vector similarity values are");
		outbuf.append("<table>");
		for(int i=0;i<topk;i++){
			outbuf.append("<tr><td>");
			outbuf.append(y[i]);
			outbuf.append("</td>");
			Document d = r.document(y[i]);
			outbuf.append("<td>");
			String url = d.getFieldable("path").stringValue();
//			url = url.replace("%%", "/");
			outbuf.append("<a href="+url+">"+url+"</a>");
			outbuf.append("</td>");
			outbuf.append("</tr>");
		}
		outbuf.append("</table>");
		request.setAttribute("utilOutput", outbuf.toString());
		request.getRequestDispatcher("/naivesearch.jsp").forward(request, response);
		}
		else if(type.equals("authorityhubs"))
		{
			System.out.println("Calculating authority hub values ..");
			pagerankObj.handleQueryRequest(query,topk,type, pagerankObj);
			int[] x = pagerankObj.getResult3();
			int[] y = pagerankObj.getResult4();
			System.out.println(x.length);
			StringBuffer outbuf = new StringBuffer();
			IndexReader r = IndexReader.open(FSDirectory.open(new File("C:\\Users\\ANIL\\workspace\\NaiveSearch\\index")));
			outbuf.append(" Top 10 Authority Values are:");
			outbuf.append("<table>");
			for(int i=0;i<topk;i++){
				outbuf.append("<tr><td>");
				outbuf.append(x[i]);
				outbuf.append("</td>");
				Document d = r.document(x[i]);
				outbuf.append("<td>");
				String url = d.getFieldable("path").stringValue();
//				url = url.replace("%%", "/");
				outbuf.append("<a href="+url+">"+url+"</a>");
				outbuf.append("</td>");
				outbuf.append("</tr>");
			}
			outbuf.append("</table>");
			outbuf.append(" Top 10 Hubs Values:");
			outbuf.append("<table>");
			for(int i=0;i<topk;i++){
				outbuf.append("<tr><td>");
				outbuf.append(y[i]);
				outbuf.append("</td>");
				Document d = r.document(y[i]);
				outbuf.append("<td>");
				String url = d.getFieldable("path").stringValue();
//				url = url.replace("%%", "/");
				outbuf.append("<a href="+url+">"+url+"</a>");
				outbuf.append("</td>");
				outbuf.append("</tr>");
			}
			outbuf.append("</table>");
			request.setAttribute("utilOutput", outbuf.toString());
			request.getRequestDispatcher("/naivesearch.jsp").forward(request, response);	
		}
		
		}

	/**
	 * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
