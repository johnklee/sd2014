package ntu.sd.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import ntu.sd.utils.SiTree.Node;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import demo.MyCrawler;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.fetcher.PageFetchResult;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.TextParseData;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.WebURL;
import flib.util.TimeStr;
import flib.util.Tuple;

public class SiTree implements Observer, Iterable<Node>{
	public EIterWay 			iterWay = EIterWay.BFS;
	public HashMap<String,Node> nodeMap = new HashMap<String,Node>();
	public Node 				root = null;
	static final Logger 		logger = Logger.getLogger(SiTree.class.getName());
		
	public enum EFileType{
		HTML,TXT,BINARY
	}
	
	public enum EIterWay{
		BFS,DFS,OTH
	}
	
	public class DFSIter implements Iterator<Node>
	{		
    	Queue<Node> list  = new LinkedList<Node>(); 
    	Node root = null;
    	
    	public DFSIter(Node root)
    	{
    		if(root!=null)
    		{
    			Set<Node> visitedSet = new HashSet<Node>();
            	Stack<Node> stack = new Stack<Node>();
        		this.root = root;
        		stack.add(root);
        		visitedSet.add(root);
        		while(stack.size()>0)
            	{
            		Node next = null;
            		for(Node n:stack.peek().childs.values())
            		{
            			if(!visitedSet.contains(n))
            			{
            				next = n;
            				break;
            			}
            		}
            		if(next!=null)
            		{
            			stack.add(next);
        				visitedSet.add(next);
            		}        		
            		else list.add(stack.pop());
            	}
    		}
    	}
    	
		@Override
		public boolean hasNext() {
			return list.size()>0;
		}

		@Override
		public Node next() {
			return list.poll();
		}

		@Override
		public void remove() {
			throw new java.lang.UnsupportedOperationException();			
		}
		
	}
	public class BFSIter implements Iterator<Node>{
		Queue<Node> queue = new LinkedList<Node>();
		
		public BFSIter(Node root)
		{
			if(root!=null) queue.add(root);
		}

		@Override
		public boolean hasNext() {
			return queue.size()>0;
		}

		@Override
		public Node next() {
			Node next = queue.poll();
			for(Node cn:next.childs.values()) queue.add(cn);
			return next;
		}

		@Override
		public void remove() {
			throw new java.lang.UnsupportedOperationException();			
		}
		
	};
	
	@Override
	public Iterator<Node> iterator() {
		switch(iterWay)
		{
		case BFS:
			return new BFSIter(root);			
		case DFS:
			return new DFSIter(root);
		default:
			return new BFSIter(root);
		}		
	}
	
	public static class Node implements Comparable<Node>{	
		public int					statusCode=-1;
		public boolean 				isValid=false;
		public Page					page;		
		public PageFetchResult		pageFetchResult=null;
		public WebURL				url;
		public EFileType 			ftype;
		public Map<String,Node> 	childs = new TreeMap<String,Node>();
        
		public Node(){}
		public Node(WebURL url){this.url = url; statusCode = 200;}
		public Node(WebURL url, Page page){
			this(url); this.page = page; isValid=true;
			if (page.getParseData() instanceof HtmlParseData) 
			{
				ftype = EFileType.HTML;
			}
			else if(page.getParseData() instanceof TextParseData)
			{
				ftype = EFileType.TXT;
			}
			else ftype = EFileType.BINARY;			
		}
		public Node(WebURL url, PageFetchResult pr)
		{
			this(url);
			statusCode = pr.getStatusCode();
			this.pageFetchResult = pr;
		}
		//public Node(String url){this.url = url;}
		
		@Override
		public boolean equals(Object o)
		{
			if(o instanceof Node) 
			{
				return url.getURL().equals(((Node)o).url.getURL());
			}
			return false;
		}
		
		@Override
		public int hashCode()
		{
			return url.getURL().hashCode();
		}
		
        

		@Override
		public int compareTo(Node node) {
			return url.getURL().compareTo(node.url.getURL());
		}
	}
	
	
	//public SiTree(String seed){initialize(seed);}
	public SiTree(){}
	
	/**
	 * BD: Free resource in memory
	 */
	public void close()
	{
		if(root!=null) root = null;
		nodeMap.clear();
	}
	
	public int outputTo(File dir)
	{
		if(root!=null && dir.isDirectory())
		{		
			try
			{
				int dc=0;
				HashMap<String,Integer> docMap = new HashMap<String,Integer>();
				for(Node node:nodeMap.values())
				{
					if(node.isValid)
					{										
						int did = node.url.getDocid();
						docMap.put(node.url.getURL(), did);
						File output = new File(dir,String.valueOf(did));
						output.createNewFile();
						FileOutputStream fos = new FileOutputStream(output);
						fos.write(node.page.getContentData());
						System.out.printf("\t[Test] Output %s...Done!\n", output.getAbsolutePath());
						fos.close();
						dc++;
					}
				}
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(dir, "DocMap.obj")));
				oos.writeObject(docMap);
				oos.close();
				return dc;
			}
			catch(Exception e)
			{
				logger.error("Fail to dump memory!");
				e.printStackTrace();
			}
		}
		return -1;
	}
	
	public BFSIter traverseBFS(){return new BFSIter(root);}
	public DFSIter traverseDFS(){return new DFSIter(root);}
	
	public Node dfsSearchRC(String url){return _dfsSearchRC(root, url);}
	protected Node _dfsSearchRC(Node node, String url)
    {
    	if(node.url.getURL().equals(url)) return node;    	
    	Node tn=null;
    	for(Node n:node.childs.values())
    	{
    		if((tn=_dfsSearchRC(n, url))!=null) return tn;
    	}        	
    	return null;
    }
    
	public Node dfsSearch(String url){return _dfsSearch(root, url);}
    protected Node _dfsSearch(Node node, String url)
    {
    	Set<Node> visitedSet = new HashSet<Node>();
    	Stack<Node> stack = new Stack<Node>();

    	//if(node.isValid) {if(node.page.getWebURL().getURL().equals(url)) return node;}
		//else {}
    	if(node.url.getURL().equals(url)) return node;
    	
    	stack.add(node);
		visitedSet.add(node);
    	while(stack.size()>0)
    	{
    		Node next = null;
    		for(Node n:stack.peek().childs.values())
    		{
    			if(!visitedSet.contains(n))
    			{
    				next = n;
    				break;
    			}
    		}
    		if(next!=null)
    		{
    			if(next.isValid) {if(next.page.getWebURL().getURL().equals(url)) return next;}
    			else {if(next.url.getURL().equals(url)) return next;}    			    			    	    	    	    	   		
    	    	stack.add(next);
				visitedSet.add(next);
    		}
    		else
    		{
    			stack.pop();
    		}
    	}
    	return null;
    }
    
    public Node bfsSearch(String url){return _bfsSearch(root, url);}
    protected Node _bfsSearch(Node node, String url)
    {
    	Queue<Node> queue = new LinkedList<Node>();
    	queue.add(node);
    	while(!queue.isEmpty())
    	{
    		Node pnode = queue.poll();
    		System.out.printf("\t[Test] Check '%s'...\n", pnode.url.getURL());
    		//if(pnode.isValid) if(pnode.page.getWebURL().getURL().equals(url)) return pnode;
    		//else {}
    		if(pnode.url.getURL().equals(url)) return pnode;
    		queue.addAll(pnode.childs.values());
    	}
    	return null;
    }

    /**
     * Callback of Observer
     * @param obs
     * 		  the registered Observable object.
     * @param obj
     *  	  the Observed event which is a Tuple class.<br/>
     *  	  - Tuple(0): True if the crawling success; False otherwise. <br/>
     *        - Tuple(1): If Tuple(0)=True, here is Page object; otherwise WebURL object.
     *        - Tuple(2): Only exist when Tuple(0)=false. PageFetchResult object.
     */
	@Override
	public void update(Observable obs, Object obj) {
		Tuple rt = (Tuple)obj;	
		if(rt.getBoolean(0))
		{
			/*Page Done*/	
			Page page = (Page)rt.get(1);
			//System.out.printf("\t[Test] ContentType=%s\n", page.getContentType());
									
			WebURL url = page.getWebURL();
			String purl=null;
			if((purl=url.getParentUrl())==null)
			{
				root = new Node(url, page);					
				nodeMap.put(url.getURL(), root);
			}
			else
			{
				Node pnode = nodeMap.get(purl);
				if(pnode!=null)
				{
					Node cnode = new Node(url, page);
					nodeMap.put(cnode.url.getURL(), cnode);
					pnode.childs.put(url.getURL(), cnode);
				}
				else
				{
					System.err.printf("\t[Error] Parent='%s' is not ready!\n", purl);
					pnode = new Node();
					pnode.childs.put(url.getURL(), new Node(url, page));
					nodeMap.put(purl, pnode);
				}
			}
		}
		else
		{
			/*Page Fail*/
			WebURL url = (WebURL)rt.get(1);
			PageFetchResult pr = (PageFetchResult)rt.get(2);
			String purl=null;
			if((purl=url.getParentUrl())==null)
			{
				root = new Node(url);
				root.statusCode = pr.getStatusCode();
				root.pageFetchResult = pr;
				nodeMap.put(url.getURL(), root);
			}
			else
			{
				Node pnode = nodeMap.get(purl);
				if(pnode!=null)
				{
					Node cnode = new Node(url, pr);
					nodeMap.put(cnode.url.getURL(), cnode);
					pnode.childs.put(url.getURL(), cnode);
				}
				else
				{
					System.err.printf("\t[Error] Parent='%s' is not ready!\n", purl);
				}
			}
		}
	}	
}
