/*
 * Author: ANIL KUNCHAM
 */


package irs2.src.edu.asu.irs13;

import java.io.File;
import java.io.IOException;
import java.sql.Wrapper;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;

public class PageRankVectorSimilarity {
	long similarity;
	double weight=0.4;
	int K=10;
	Map<String, HashMap> termDocMap= new HashMap<String, HashMap>();
	Map<Integer,Long> docNorm = new HashMap<>();
	Map<String, Long> idfMap = new HashMap<>();
	Map<Integer, Integer> docidArrayIndex1 = new HashMap();
	Map<Integer, Integer> docidArrayIndex2 = new HashMap();
	Map<Integer, Double> pagerankMap = new HashMap<>();
	Map<Integer, Double> pagerankSimilarityMap = new HashMap();
	Map<Integer, Double> docSimilarity = new HashMap<>();
	int[] result1 = new int[K];
	int[] result2 = new int[K];
	int[] result3 = new int[K];
	int[] result4 = new int[K];
	long initial_time = 0;
	long final_time = 0;
	//Method to create a map of terms, documents in which the term occurs and the term frequency.
	//Also creates an IDF map for all the terms. 
	public void createMap() throws CorruptIndexException, IOException
	{
	long start = System.nanoTime();	
	IndexReader r = IndexReader.open(FSDirectory.open(new File("C:\\Users\\ANIL\\workspace\\NaiveSearch\\index")));	
	int i = 0;
	TermEnum t = r.terms();
	long idf;
	while(t.next())
	{
		HashMap docFreqMap = new HashMap<Integer,Integer>();
		if(t.term().field() == "contents"){
			TermDocs td = r.termDocs(t.term());
			idf = (long) Math.log(r.maxDoc()/(1+r.docFreq(t.term())));
			idfMap.put(t.term().text(), idf*idf);
			while(td.next()){
				docFreqMap.put(td.doc(),td.freq());
			}
			termDocMap.put(t.term().text(), docFreqMap);
		}
	}
	long stop = System.nanoTime();
//	System.out.println("Time taken to load Map "+(stop - start)/1000000);
	}
	
	// Method to calculate the document normalization square value and store it in a map
	public void calculate2NormDoc() throws CorruptIndexException, IOException{
		long start = System.nanoTime();
		IndexReader r = IndexReader.open(FSDirectory.open(new File("C:\\Users\\ANIL\\workspace\\NaiveSearch\\index")));
		Iterator<Entry<String, HashMap>> terms = termDocMap.entrySet().iterator();
		double idf;
		while(terms.hasNext()) {
			Map.Entry<String, HashMap> entry = terms.next();
			Term te = new Term("contents",entry.getKey());
			Iterator it = termDocMap.entrySet().iterator();
			Iterator<Map.Entry<Integer, Integer>> entries = ((Map<Integer, Integer>) entry.getValue()).entrySet().iterator();
			while (entries.hasNext()) {
				long sum = 0;
			    Map.Entry<Integer, Integer> entry1 = entries.next();
			    int docid = entry1.getKey();
			    long docfreq = entry1.getValue();
			    if(docNorm.containsKey(docid))
			    {
			    	long idftemp = idfMap.get(te.text());
			    	sum = (long) (docNorm.get(docid) + docfreq*docfreq*idftemp*idftemp);
			    	docNorm.put(docid, sum);
			    }
			    else{
			    	long idftemp = idfMap.get(te.text());
			    	docNorm.put(docid, (long) (docfreq*docfreq*idftemp*idftemp));
			    }
			}
		}
		long end=System.nanoTime();
//		System.out.println("Time taken"+(end-start)/1000000);
	}
	
	
	
	//Method to calculate the similarity and sort the values
	public void calculateSimilarity(String[] query, String type) throws CorruptIndexException, IOException
	{
		long starttime = System.nanoTime();
		IndexReader r = IndexReader.open(FSDirectory.open(new File("C:\\Users\\ANIL\\workspace\\NaiveSearch\\index")));
		for(String term : query)
		{
			System.out.println("Term is"+term);
		Term te = new Term("contents", term);	
		HashMap docs = termDocMap.get(te.text());
		double numer, denom = 0;
		double idf = Math.log(r.maxDoc()/(1+r.docFreq(te)));
		double similarity;
		Iterator<Map.Entry<Integer, Integer>> entries = docs.entrySet().iterator();
		while (entries.hasNext()) {
		    Map.Entry<Integer, Integer> entry = entries.next();
		    int docid = entry.getKey();
		    int docweight = entry.getValue();
		    numer = (docweight*idf);
		    denom = Math.sqrt(docNorm.get(docid));
		    similarity = numer/denom;
		    if(docSimilarity.containsKey(docid)){
		    	similarity = similarity + docSimilarity.get(docid);
		    	docSimilarity.put(docid, similarity);
		    }
		    else{
		    	docSimilarity.put(docid, similarity);
		    }
		   }
		}
		long sortstart = System.nanoTime();
		SortDesc sort=new SortDesc(docSimilarity);
		Map<Integer,Double> finalMap = new TreeMap(sort);
		finalMap.putAll(docSimilarity);
		long sortstop = System.nanoTime();
		long endtime = System.nanoTime();
//		System.out.println("Time taken to fetch the results "+(endtime - starttime)/1000000);
		System.out.println("Time taken to sort "+(sortstop - sortstart)/1000000);
		if(type.equals("cumulative"))
			computePageRankVectorSimilarity(finalMap);
		if(type.equals("authorityhubs"))
			printSimilarDocs(finalMap);
		
	}
	
	//Method to get the top pagerank
	public double getPageRankMax(Map<Integer, Double> pagerankMap2){
		double x = 0;
		SortCumulativeRank sort = new SortCumulativeRank(pagerankMap2);
		TreeMap SortedMap = new TreeMap(sort);
		SortedMap.putAll(pagerankMap2);
		Iterator<Entry<Integer, Double>> docs = SortedMap.entrySet().iterator();
		while(docs.hasNext()){
			Map.Entry<Integer, Double> entry = docs.next();
			 x = entry.getValue();
			 System.out.println("Highest page rank value is "+entry.getKey()+" ,"+"value is "+entry.getValue());
			 break;
		}
		
		return x;

	}
	
	//Compute the page rank + vector space similarity value 
	public void computePageRankVectorSimilarity(Map<Integer, Double> finalMap){
		Iterator<Entry<Integer, Double>> docs = finalMap.entrySet().iterator();
		double high_page_rank_val = getPageRankMax(pagerankMap);
		while(docs.hasNext()) {
			double cumulativeSimilarity = 0;
			Map.Entry<Integer, Double> entry = docs.next();
			int docid = entry.getKey();
			double vectorSimilarity = entry.getValue();
			cumulativeSimilarity = weight*(pagerankMap.get(docid)/high_page_rank_val) + (1 - weight)*vectorSimilarity;
			pagerankSimilarityMap.put(docid, cumulativeSimilarity);
		}
		SortCumulativeRank sort = new SortCumulativeRank(pagerankSimilarityMap);
		TreeMap cumulativeSortedMap = new TreeMap(sort);
		cumulativeSortedMap.putAll(pagerankSimilarityMap);
		printCumulativeSimilarityValues(finalMap, cumulativeSortedMap);	
	}
	
	//Output the Pagerank values to a map
	public void printCumulativeSimilarityValues(Map<Integer, Double> finalMap, TreeMap cumulativeSortedMap){
		System.out.println("Top 10 Cumulative Similarity based documents are");
		HashMap<Integer, Double> temp1 = new HashMap<>();
		HashMap<Integer, Double> temp2 = new HashMap<>();
		Iterator<Entry<Integer, Double>> docs = cumulativeSortedMap.entrySet().iterator();
		int count = 0;
		while(docs.hasNext()) {
			if(count < K)
			{
			double cumulativeSimilarity = 0;
			Map.Entry<Integer, Double> entry = docs.next();
			System.out.println(entry.getKey()+"->"+entry.getValue());
			result1[count] = entry.getKey();
			}
			else
				break;
			count++;
		}
		count = 0;
		System.out.println("Top 10 TF-IDF Similarity based documents are");
		Iterator<Entry<Integer, Double>> docs1 = finalMap.entrySet().iterator();
		while(docs.hasNext()) {
			if(count < K){
			double cumulativeSimilarity = 0;
			Map.Entry<Integer, Double> entry = docs1.next();
			System.out.println(entry.getKey()+"->"+entry.getValue());
			result2[count] = entry.getKey();
			}
			else
				break;
			count++;
		}
		RankResults obj = new RankResults();
		obj.setMap(temp1, temp2);
	}
	
	
	public void print_adjacencyMatrix(double[][] a_trans_start){
		int colsize = a_trans_start.length;
		for(int k=0;k<colsize;k++){
			for(int j=0;j<colsize;j++){
//				System.out.println(docidArrayIndex.get(k).intValue()+"-"+docidArrayIndex.get(j).intValue()+"->"+adjacencymatrix[k][j]);
				System.out.println(a_trans_start[k][j]);
			}
			System.out.println("*************************************");
		}
	}
	
	
	public void printGenericMatrix(double[][] A){
		for(int i=0;i<A.length;i++){
			for(int j=0;j<A[0].length;j++){
//				if(A[i][j]>0)
				System.out.println(A[i][j]);
			}
		}
	}
	
	//Generic method to compute halt condition for Authorities and Hubs
	public boolean computehaltCondition(double[][] A, double[][] B){
		System.out.println("Computing halt condition");
		for(int i=0;i<A.length;i++){
			System.out.println(Math.abs(A[i][0] - B[i][0]));
			if(Math.abs(A[i][0] - B[i][0])>0.00001){	
				return false;
			}
		}
		return true;
	}
	
	//Method to normalize Authority values
	public double[][] normalizeAuthority(double[][] a_prev, int size, int len){
		double sum = 0;
		double[][] a_temp = new double[size][len];
		for(int i=0;i<size;i++)
		{
			sum = sum + a_prev[i][0]*a_prev[i][0];
		}
		sum = Math.sqrt(sum);
		for(int i=0;i<size;i++){
			a_temp[i][0] = a_prev[i][0]/sum;
		}
		return a_temp;
	}
	
	
	//Method to compute initial authority value
	public double[][] computeInitialAuthority(double[][] a, int size, int len){
		double[][] a_temp = new double[size][len];
		for(int j=0;j<a.length;j++)
		{
			double sum=0;
			for(int i=0;i<a.length;i++){
				sum = sum + a[i][j];
			}
			a_temp[j][0]=sum;
		}
		System.out.println("Intial a0 matrix is");
		printGenericMatrix(a_temp);
		return a_temp;
	}
	
	//Method to compute initial hub value
	public double[][] computeInitialHub(double[][] a, int size, int len){
		double[][] a_temp = new double[size][len];
		for(int i=0;i<size;i++)
		{
			double sum=0;
			for(int j=0;j<size;j++){
				sum = sum + a[i][j];
			}
			a_temp[i][0]=sum;
		}
		System.out.println("Intial h0 matrix is");
		printGenericMatrix(a_temp);
		return a_temp;
	}
	
	//method to compute hub values
	public void computeHubValues(double[][] AA_Trans, double[][] h0, int size, int len, MatrixComputation matrix){
		int itr = 1;
		long start = System.currentTimeMillis();
		double[][] a_prev = new double[size][len];
		double[][] a_next = new double[size][len];
		double[][] a_start = new double[size][len];
		a_start = matrix.multiply(AA_Trans, h0);
		a_prev = h0;
		a_next = a_start;
		while(!computehaltCondition(normalizeAuthority(a_prev,size,len),normalizeAuthority(a_next,size,len)))
		{
			System.out.println("iteration - "+itr);
			a_prev = a_next;
			a_next = matrix.multiply(AA_Trans,a_prev);
			itr++;
		}
		System.out.println("Hub Values are..");
		long stop = System.currentTimeMillis();
		System.out.println("Time taken to compute Hub values"+(stop - start));
//		printGenericMatrix(normalizeAuthority(a_next, size, len));
		preSortingAuthorityHubs(normalizeAuthority(a_next, size, len),"hubs");
	}
	
	//Method to compute pagerank halt condition
	public Boolean computePageRankHaltCondition(double[][] R_prev, double[][] R_next, int csize){
		
		for(int i=0;i<csize;i++){
			System.out.println(R_prev[i][0]+"->%%%"+R_next[i][0]);
			System.out.println("The difference is "+Math.abs(R_prev[i][0]-R_next[i][0]));
			if(Math.abs(R_prev[i][0]-R_next[i][0])>0.000005){
				return false;
			}
		}
		return true;
	}
	

	public void printRMatrix(double[][] R){
		for(int i=0;i<R.length;i++){
			System.out.println("Val-"+i+"+"+R[i][0]);
		}
		System.out.println("-----------------------------------------");
	}
	
	public void computePageRankNew(int corpusSize){
//		LinkAnalysis.numDocs = corpusSize;
		LinkAnalysis.numDocs = 25054;
		int csize = LinkAnalysis.numDocs;
		System.out.println("xxxxxxxxxx"+csize);
		double Nval = (double)1/csize;
		System.out.println("yyyyyyyyyyy"+Nval);
		LinkAnalysis linkobj = new LinkAnalysis();
		
		double[][] R = new double[csize][1];
		double[][] R_prev = new double[csize][1];
		for(int i=0;i<csize;i++){
			R[i][0] = Nval;
		}
//		printRMatrix(R);
		double c = 0.8;
		int itr = 1;
		double sum = 0;
		int run = 1;
		
		while(true)
		{
//			System.arraycopy(R, 0, R_prev, 0, csize);
			for(int i=0;i<csize;i++)
			{
				R_prev[i][0] = R[i][0];
			}
			System.out.println("iteration - "+itr);
//			printRMatrix(R);
			for(int i=0;i<csize;i++)
			{
				double[] MStar = new double[csize];
				
				if(linkobj.getLinks(i).length != 0 )
				{
					int[] links3 = linkobj.getCitations(i);
					for(int pb:links3)
					{
					MStar[pb] = (double)(c*1/linkobj.getLinks(pb).length + (1-c)*Nval);
					}
				}
				else{
					MStar[i] = Nval;
				}
				for(int j=0;j<csize;j++){
						if(MStar[j]==0){
							MStar[j] = Nval;
						}
						sum = sum + MStar[j]*R[j][0];
					}
				R[i][0] = sum;
				sum = 0.0;
			}
//			printRMatrix(R_prev);
//			printRMatrix(R);
			int converge = 0;
			for(int k=0;k<csize;k++){
				System.out.println(">"+k+"<"+R_prev[k][0]+"->%%%"+R[k][0]);
				System.out.println("The difference is "+Math.abs(R_prev[k][0]-R[k][0]));
				if(Math.abs(R_prev[k][0]-R[k][0])>0.005){
					converge = 1;
					break;
					}
				}
		if(converge == 0)
			break;
		itr++;
		}
		for(int i=0;i<csize;i++){
			pagerankMap.put(i,R[i][0]);
		}
	}
	
	//Used method to compute pagerank
	public void computePageRankOverride(int corpusSize){
//		LinkAnalysis.numDocs = corpusSize;
		LinkAnalysis.numDocs = 25054;
		int csize = LinkAnalysis.numDocs;
		System.out.println("xxxxxxxxxx"+csize);
		double Nval = (double)1/csize;
		System.out.println("yyyyyyyyyyy"+Nval);
		LinkAnalysis linkobj = new LinkAnalysis();
		
		double[][] R = new double[csize][1];
		double[][] R_prev = new double[csize][1];
		for(int i=0;i<csize;i++){
			R[i][0] = Nval;
		}
//		printRMatrix(R);
		double c = 0.8;
		int itr = 1;
		while(true)
		{
//			System.arraycopy(R, 0, R_prev, 0, csize);
			for(int i=0;i<csize;i++)
			{
				R_prev[i][0] = R[i][0];
			}
			System.out.println("iteration - "+itr);
//			printRMatrix(R);
			for(int i=0;i<csize;i++)
			{			
				if(linkobj.getLinks(i).length != 0 )
				{
					int[] links3 = linkobj.getLinks(i);
					for(int pb:links3)
					{
					R[pb][0] = (double)R[pb][0]/linkobj.getCitations(pb).length;
					}
				}
				else{
					R[i][0] = R[i][0]*Nval;
				}
			}
				for(int j=0;j<csize;j++){
						R[j][0] = c * R[j][0]+(1-c)*Nval*R[j][0];
					}
			
//			printRMatrix(R_prev);
//			printRMatrix(R);
			int converge = 0;
			for(int k=0;k<csize;k++){
//				System.out.println(">"+k+"<"+R_prev[k][0]+"->%%%"+R[k][0]);
//				System.out.println("The difference is "+Math.abs(R_prev[k][0]-R[k][0]));
				if(Math.abs(R_prev[k][0]-R[k][0])>0.00001){
					converge = 1;
					break;
					}
				}
		if(converge == 0)
			break;
		itr++;
		}
		for(int i=0;i<csize;i++){
			pagerankMap.put(i,R[i][0]);
		}
		
	}
	
	
	
	void print_docidArrayIndex(Map<Integer, Integer> docidArrayIndex){
		Iterator<Map.Entry<Integer, Integer>> entries = docidArrayIndex.entrySet().iterator();
		while (entries.hasNext()) {
		    Map.Entry<Integer, Integer> entry = entries.next();
		    System.out.println(entry.getKey()+"->"+entry.getValue());
		}
	}

	public void computePageRankAlt()
	{
		double c = 0.8;
		LinkAnalysis.numDocs = 25504;
		LinkAnalysis linkobj = new LinkAnalysis();
		int csize = LinkAnalysis.numDocs;
		double[][] R = new double[csize][1];
		for(int i=0;i<csize;i++){
			R[i][0] = (double)1/csize;
		}
		int itr = 0;
		while(true){
			double[][] R_new = new double[csize][1];
			double sinkNodeSum = 0;
			double pageRankTotal = 0;
			for(int i=0;i<csize;i++)
			{
				int[] links3 = linkobj.getLinks(i);
				for(int pb:links3)
				{
					R_new[pb][0]+= c*R[i][0]; 
				}
				if(linkobj.getLinks(i).length == 0){
					sinkNodeSum+=R[i][0];
				}
				pageRankTotal+=R[i][0];
					
			}
			for(int i=0;i<csize;i++)
			{
				R_new[i][0]+= (c*sinkNodeSum) + (1-c)*pageRankTotal;
				R_new[i][0]/=pageRankTotal;
			}
			for(int i=0;i<csize;i++){
				R[i][0] = R_new[i][0];
			}
			itr++;
			System.out.println("Iteration"+itr);
			if(computePageRankHaltCondition(R,R_new,csize)){
				break;
			}
		}
//		for(int j=0;j<csize;j++){
//			pagerankMap.put(j,R[j][0]);
//		}
	}
	
	
	//Method to compute Authority values
	public void computeAuthorityHubs(double[][] A)
	{
		int size = A.length;
		int len = 1;
		System.out.println("length"+size);
		
		double[][] a0 = new double[size][len];
		double[][] h0 = new double[size][len];
		a0=computeInitialAuthority(A, size, len);
		h0=computeInitialHub(A,size,len);
		MatrixComputation matrix = new MatrixComputation(A);
		double[][] A_trans_start =	matrix.transpose(A);
//		System.out.println("Transpose of Matrix..");
//		print_adjacencyMatrix(A_trans_start);
		double[][] A_mult_start = new double[size][size]; 
		A_mult_start = matrix.multiply(A_trans_start,A);
//		System.out.println("After multiplication..");
//		printGenericMatrix(A_mult_start);
		double[][] AA_Trans = matrix.multiply(A, A_trans_start);
		double[][] a_start = matrix.multiply(A_mult_start, a0);
//		System.out.println("A start value is.. ");
//		printGenericMatrix(a_start);
		int itr = 1;
		double[][] a_prev = new double[size][len];
		double[][] a_next = new double[size][len];
		a_prev = a0;
		a_next = a_start;
		while(!computehaltCondition(normalizeAuthority(a_prev,size,len),normalizeAuthority(a_next,size,len)))
		{
			System.out.println("iteration - "+itr);
			a_prev = a_next;
			a_next = matrix.multiply(A_mult_start,a_prev);
			itr++;
//			printGenericMatrix(a_next);
		}
		System.out.println("Authority values are..");
//		printGenericMatrix(normalizeAuthority(a_next, size, len));
		preSortingAuthorityHubs(normalizeAuthority(a_prev, size, len),"authority");
		computeHubValues(AA_Trans, h0, size, len, matrix);
//		computePageRank(AA_Trans);
	}
	
	//Method to sort Authority and HUb values
	public void preSortingAuthorityHubs(double[][] A, String ttype){
		int size = A.length;
		HashMap<Integer, Double> preSortMap = new HashMap<>();
		for(int i=0;i<size;i++)
		{
			preSortMap.put(docidArrayIndex2.get(i), A[i][0]);
		}
		SortDesc sort=new SortDesc(preSortMap);
		Map<Integer,Double> sortedMap = new TreeMap(sort);
		sortedMap.putAll(preSortMap);
		final_time = System.nanoTime();
		System.out.println("Time taken to compute authority values"+(final_time - initial_time));
		printTopValues(sortedMap,ttype);
	}
	
	//Method to set top K results
	public void printTopValues(Map<Integer, Double> sortedMap, String ttype){
		Iterator<Map.Entry<Integer, Double>> entries = sortedMap.entrySet().iterator();
		int count = 0;
		System.out.println("Top Authority Values are : ");
		while (entries.hasNext()) {
			if(count < 10)
			{
		    Map.Entry<Integer, Double> entry = entries.next();
		    System.out.println(entry.getKey()+"->"+entry.getValue());
		    if(ttype.equals("authority"))
		    {
		    	result3[count] = entry.getKey();
		    }
		    else{
		    	result4[count] = entry.getKey();
		    }
			}
			else
				break;
			count++;
		}
	}
	
	//Method to populate adjacency matrix
	public void populateAdjacencyMatrix(Map<Integer, Double> finalMap, Set columnSet){
		long start = System.currentTimeMillis();
		int colsize = columnSet.size();
		double[][] adjacencymatrix = new double[colsize][colsize];
		LinkAnalysis.numDocs = 25054;
		LinkAnalysis linkana = new LinkAnalysis();
		Iterator<Integer> itr = columnSet.iterator();
		int i = 0;
		while(itr.hasNext()){
			Integer val = itr.next();
//			System.out.println("HashSet values.."+val+"->"+i);
			docidArrayIndex1.put(val, i);
			docidArrayIndex2.put(i, val);
			i++;
		}
//		print_docidArrayIndex(docidArrayIndex);
		
		Iterator<Map.Entry<Integer, Double>> entries = finalMap.entrySet().iterator();
		int count = 0;
		while (entries.hasNext()) {
			if(count <= 10)
			{
		    Map.Entry<Integer, Double> entry = entries.next();
		    int docid = entry.getKey();
			int[] links3 = linkana.getLinks(docid);
			for(int pb:links3)
			{
//				System.out.print("Inserting"+docidArrayIndex.get(entry.getKey())+"->"+docidArrayIndex.get(pb));
				adjacencymatrix[docidArrayIndex1.get(docid)][docidArrayIndex1.get(pb)] = 1;
				
			}
			
			// Find all the document numbers that point to doc #3
//			System.out.print("\nDocument number "+docid+ "is pointed by: ");
			int[] cit3 = linkana.getCitations(docid);
			for(int pb:cit3)
			{
//				System.out.print(pb + ",");
				adjacencymatrix[docidArrayIndex1.get(docid)][docidArrayIndex1.get(pb)] = 1;
			}
			count = count + 1;
		}
			else{
				break;
			}
		}
//		System.out.println("------------------------------------------------>");
//		print_adjacencyMatrix(adjacencymatrix);
		long stop = System.currentTimeMillis();
		System.out.println("Time taken to build the adjancency matrix is"+(stop - start));
		computeAuthorityHubs(adjacencymatrix);
	}
	
	
	//Method to print the similarity values
	public void printSimilarDocs(Map<Integer, Double> finalMap) throws CorruptIndexException, IOException
	{
			initial_time = System.currentTimeMillis();
			long start = System.currentTimeMillis();
			LinkAnalysis.numDocs = 25054;
			LinkAnalysis linkana = new LinkAnalysis();
			Set<Integer> columnset = new HashSet<Integer>();
			Iterator<Map.Entry<Integer, Double>> entries = finalMap.entrySet().iterator();
			System.out.println("Similar Documents are");
			IndexReader r = IndexReader.open(FSDirectory.open(new File("C:\\Users\\ANIL\\workspace\\NaiveSearch\\index")));
			int count = 0;
			while (entries.hasNext()) {
				if(count <= 10)
				{
			    Map.Entry<Integer, Double> entry = entries.next();
			    int docid = entry.getKey();
			    Document d = r.document(docid);
//			    System.out.println("Hub Values");
//			    System.out.print("Document number "+docid+" points to: ");
				int[] links3 = linkana.getLinks(docid);
				columnset.add(docid);
				for(int pb:links3)
				{
					System.out.print(pb + ",");
					columnset.add(pb);
				}
				
				// Find all the document numbers that point to doc #3
//				System.out.print("\nDocument number "+docid+ "is pointed by: ");
				int[] cit3 = linkana.getCitations(docid);
				for(int pb:cit3)
				{
					System.out.print(pb + ",");
					columnset.add(pb);
				}
				String url = d.getFieldable("path").stringValue(); // the 'path' field of the Document object holds the URL
//			    System.out.println(entry.getKey()+"->"+entry.getValue());
//			    System.out.println(url.replace("%%", "/"));
//				System.out.println(entry.getKey()+",");
			    count = count + 1;
			}
				else{
					break;
				}
			}
			long stop = System.currentTimeMillis();
			System.out.println("Time taken to compute Root set is "+(stop - start));
			System.out.println("No of elements in set"+columnset.size());
			System.out.println("Calculating Authority and Hub Values..");
			populateAdjacencyMatrix(finalMap,columnset);
	}
	
	
	//Helper method to print the terms and document mapping hashmap
	public void printTermDocsMap(){
		Iterator<Map.Entry<String, HashMap>> entries = termDocMap.entrySet().iterator();
		System.out.println("Similar Documents are");
		while (entries.hasNext()) 
		{
		    Map.Entry<String, HashMap> entry = entries.next();
//		    System.out.println(entry.getKey()+"->"+entry.getValue());
		}
	}
	
	
	
	//Method to print the IDF values of the terms
	public void printlowIDFValues(){
		SortAscLowIDF1 sort=new SortAscLowIDF1(idfMap);
		Map<String,Long> finalMap1 = new TreeMap(sort);
		finalMap1.putAll(idfMap);
		Iterator<Map.Entry<String, Long>> entries = finalMap1.entrySet().iterator();
		System.out.println("Low idf terms are");
		int count = 0;
		while (entries.hasNext()) 
		{
		    Map.Entry<String, Long> entry = entries.next();
		    System.out.println(entry.getKey()+"->"+entry.getValue());
			
		}
		
	}
	
	
	//Helper method to print the document normalization values
	public void printDocNorm()
	{	 
		Iterator<Map.Entry<Integer, Long>> entries = docNorm.entrySet().iterator();
		System.out.println("Similar Documents are");
		while (entries.hasNext()) 
		{
		    Map.Entry<Integer, Long> entry = entries.next();
//		    System.out.println(entry.getKey()+"->"+Math.sqrt(entry.getValue()));
		}
		
}
	//Method handler to serve the client requests from the servlet.. Controller
	public void handleQueryRequest(String query, int topk, String type, PageRankVectorSimilarity obj){
		System.out.println("Query is "+query);
		String[] terms = query.split("\\s+");
		if(type.equals("cumulative"))
		{
			try {
				obj.calculateSimilarity(terms, type);
			} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		}
	else if(type.equals("authorityhubs")){
		try {
			obj.calculateSimilarity(terms, type);
		} catch (CorruptIndexException e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		}
	}

	}
	
	//Getters for all the final computations
	public int[] getResult1(){
		return result1;
	}
	
	public int[] getResult2(){
		return result2;
	}
	
	public int[] getResult3(){
		return result3;
	}
	
	public int[] getResult4(){
		return result4;
	}
	
	public static void main(String[] args) throws CorruptIndexException, IOException{
		PageRankVectorSimilarity obj = new PageRankVectorSimilarity();
		System.out.println("Creating Inverted index data structure Map..");
		obj.createMap();
		System.out.println("Calculating 2 Norm values..");
		obj.calculate2NormDoc();
//		obj.printlowIDFValues();
//		obj.printTermDocsMap();
//		obj.printDocNorm();
		System.out.println("Computing page rank..");
		System.out.println("Enter N value - corpus size");
//		Scanner sc1 = new Scanner(System.in);
//		int corpusSize = sc1.nextInt();
//		obj.computePageRankNew(25504);
//		obj.computePageRankAlt();
		obj.computePageRankOverride(25504);
		Scanner sc = new Scanner(System.in);
		String str = "";
		System.out.print("query> ");
		while(!(str = sc.nextLine()).equals("quit"))
		{
			
			System.out.print("query> ");
		}
	}
}

// Class used to implement the Comparator interface for sorting
class SortCumulativeRank implements Comparator
{

	private Map<Integer, Double> map;
	public SortCumulativeRank(Map<Integer, Double> pagerankSimilarityMap){
		this.map = pagerankSimilarityMap;
	}
	@Override
	public int compare(Object arg0, Object arg1) {
		// TODO Auto-generated method stub
		
		return (map.get(arg0)).compareTo(map.get(arg1));
	}

}

//Class used to implement the Comparator interface for sorting
class SortAscLowIDF1 implements Comparator{

	private Map<String, Long> map;
	public SortAscLowIDF1(Map<String, Long> idfMaptemp){
		this.map = idfMaptemp;
	}
	@Override
	public int compare(Object arg0, Object arg1) {
		// TODO Auto-generated method stub
		
		return ((Long) map.get(arg0)).compareTo((Long) map.get(arg1));
	}

}

