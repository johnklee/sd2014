package ntu.sd.utils;

import java.io.File;
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

import org.apache.log4j.BasicConfigurator;
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
import ntu.sd.utils.SiTree.Node;

public class SiTree implements Observer, Iterable<Node>{
	public EIterWay iterWay = EIterWay.BFS;
	public HashMap<String,Node> nodeMap = new HashMap<String,Node>();
	Node root = null;
		
	public enum EFileType{
		HTML,TXT,BINARY
	}
	
	public enum EIterWay{
		BFS,DFS
	}
	
	public class DFSIter implements Iterator<Node>
	{		
    	Queue<Node> list  = new LinkedList<Node>(); 
    	Node root = null;
    	
    	public DFSIter(Node root)
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
			queue.add(root);
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
		}
		return null;
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
	
	public void initialize(String seed)
	{
		/*root = new Node(seed);
		nodeMap.put(seed, root);*/
	}
	
	public Node dfsSearchRC(String url){return _dfsSearchRC(root, url);}
	protected Node _dfsSearchRC(Node node, String url)
    {
    	if(node.url.equals(url)) return node;
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
    	if(node.url.equals(url)) return node;
    	else {
    		stack.add(node);
    		visitedSet.add(node);
    	}
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
    			if(next.url.equals(url)) return next;
    			else 
    			{
    				stack.add(next);
    				visitedSet.add(next);
    			}
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
    		if(pnode.url.equals(url)) return pnode;
    		queue.addAll(pnode.childs.values());
    	}
    	return null;
    }

	@Override
	public void update(Observable obs, Object obj) {
		Tuple rt = (Tuple)obj;	
		if(rt.getBoolean(0))
		{
			/*Page Done*/	
			Page page = (Page)rt.get(1);
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
	
	public static void main(String args[]) throws Exception
	{
		/* 0) Configure Log4j*/
		File log4j = new File("log4j.properties");
		if(log4j.exists())
		{
			PropertyConfigurator.configure(log4j.getAbsolutePath()); 
		}
		else
		{
			BasicConfigurator.configure();  
		}
		
		long st = System.currentTimeMillis();

		// crawlStorageFolder is a folder where intermediate crawl data is stored.
        String crawlStorageFolder = "C:/tmp/crawler_tmp/";
        
        // numberOfCrawlers shows the number of concurrent threads that should
        // be initiated for crawling.
        int numberOfCrawlers = 7;

        CrawlConfig config = new CrawlConfig();        
        config.setCrawlStorageFolder(crawlStorageFolder);
        
        /*
         * Be polite: Make sure that we don't send more than 1 request per
         * second (1000 milliseconds between requests).
         */
        config.setPolitenessDelay(1000);
        
        /*
         * You can set the maximum number of pages to crawl. The default value
         * is -1 for unlimited number of pages
         */
        config.setMaxPagesToFetch(1000);
        
        /*
         * Do you need to set a proxy? If so, you can use:
         * config.setProxyHost("proxyserver.example.com");
         * config.setProxyPort(8080);
         * 
         * If your proxy also needs authentication:
         * config.setProxyUsername(username); config.getProxyPassword(password);
         */
        
        /*
         * This config parameter can be used to set your crawl to be resumable
         * (meaning that you can resume the crawl from a previously
         * interrupted/crashed crawl). Note: if you enable resuming feature and
         * want to start a fresh crawl, you need to delete the contents of
         * rootFolder manually.
         */
        config.setResumableCrawling(false);
        config.setIncludeBinaryContentInCrawling(true);
        
        /*
         * Instantiate the controller for this crawl.
         */
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        /*
         * For each crawl, you need to add some seed urls. These are the first
         * URLs that are fetched and then the crawler starts following links
         * which are found in these pages
         */
        controller.addSeed("http://localhost/FF/crawlme/index.html");
        //controller.addSeed("http://localhost/FF/redir/documentloc.html");

        
        System.out.printf("\t[Info] Starting Crawler...\n");
        SiTree siTree = new SiTree();
        controller.addObserver(siTree);
        /*
         * Start the crawl. This is a blocking operation, meaning that your code
         * will reach the line after this only when crawling is finished.
         */       
        controller.start(MyCrawler.class, numberOfCrawlers);
        System.out.printf("\t[Info] Done! %s\n", TimeStr.ToStringFrom(st));
        controller.deleteObserver(siTree);
        
        // BFS
        System.out.printf("\t[Info] BFS:\n");
        for(Node n:siTree)
        {
        	System.out.printf("\t\t%s(%s)\n", n.url.getURL(), n.isValid);
        }
        
        siTree.iterWay = EIterWay.DFS;
        System.out.printf("\t[Info] DFS:\n");
        for(Node n:siTree)
        {
        	System.out.printf("\t\t%s(%s)\n", n.url.getURL(), n.isValid);
        }
	}	
}
