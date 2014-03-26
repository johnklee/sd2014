package ntu.sd.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

public class SiTree {
	public enum EFileType{
		HTML,TXT,BINARY
	}
	
	public static class Node implements Comparable<Node>{		
		public boolean 				isValid=false;
		public int					SC=-1; // HTTP Status Code
		public int 					docid=-1;
		public String 				url;
		public String 				domain;
		public String 				path;
		public String 				subDomain;
		public String 				parentUrl;
		public String 				anchor;
		public EFileType 			ftype;
		public Map<String,Node> 	childs = new TreeMap<String,Node>();
        
		public Node(){}
		public Node(String url){this.url = url;}
		
		@Override
		public boolean equals(Object o)
		{
			if(o instanceof Node) 
			{
				return url.equals(((Node)o).url);
			}
			return false;
		}
		
		@Override
		public int hashCode()
		{
			return url.hashCode();
		}
		
        

		@Override
		public int compareTo(Node node) {
			return url.compareTo(node.url);
		}
	}
	public HashMap<String,Node> nodeMap = new HashMap<String,Node>();
	Node root = null;
	
	public void initialize(String seed)
	{
		root = new Node(seed);
		nodeMap.put(seed, root);
	}
	
	public Node dfsSearchRC(Node node, String url)
    {
    	if(node.url.equals(url)) return node;
    	Node tn=null;
    	for(Node n:node.childs.values())
    	{
    		if((tn=dfsSearchRC(n, url))!=null) return tn;
    	}        	
    	return null;
    }
    
    public Node dfsSearch(Node node, String url)
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
    
    public Node bfsSearch(Node node, String url)
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
}
