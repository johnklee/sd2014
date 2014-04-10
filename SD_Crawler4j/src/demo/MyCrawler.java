package demo;

import java.util.List;
import java.util.regex.Pattern;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.apache.http.Header;

/**
 * BD: You need to create a crawler class that extends WebCrawler. 
 *     This class decides which URLs should be crawled and handles the downloaded page. 
 *     The following is a sample implementation...
 * @author John
 *
 */
public class MyCrawler extends WebCrawler{
	/*static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g" + 
                                            "|png|tiff?|mid|mp2|mp3|mp4" + 
			                                 "|wav|avi|mov|mpeg|ram|m4v|pdf" + 
                                            "|rm|smil|wmv|swf|wma|zip|rar|gz))$");*/
	
	 /**
     * You should implement this function to specify whether
     * the given url should be crawled or not (based on your
     * crawling logic).
     */
    @Override
    public boolean shouldVisit(WebURL url) {    		
        //String href = url.getURL().toLowerCase();
        //return !FILTERS.matcher(href).matches() && href.startsWith("http://www.ics.uci.edu/");
    	if(DeepLimit>0 && url.getDepth()>DeepLimit) return false;	
    	return true;
    }
    
    
    /**
     * This function is called when a page is fetched and ready 
     * to be processed by your program.
     */
	@Override
	public void visit(Page page) {
		int docid = page.getWebURL().getDocid();
        String url = page.getWebURL().getURL();
        String domain = page.getWebURL().getDomain();
        String path = page.getWebURL().getPath();
        String subDomain = page.getWebURL().getSubDomain();
        String parentUrl = page.getWebURL().getParentUrl();
        String anchor = page.getWebURL().getAnchor();
		System.out.printf("\t[Info] URL=%s:\n", url);
		System.out.println("\tDocid: " + docid);
        System.out.println("\tURL: " + url);
        System.out.println("\tDomain: '" + domain + "'");
        System.out.println("\tSub-domain: '" + subDomain + "'");
        System.out.println("\tPath: '" + path + "'");
        System.out.println("\tParent page: " + parentUrl);
        System.out.println("\tAnchor text: " + anchor);

		if (page.getParseData() instanceof HtmlParseData) 
		{
			System.out.printf("\tHtml page:\n");
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			String text = htmlParseData.getText();
			String html = htmlParseData.getHtml();
			List<WebURL> links = htmlParseData.getOutgoingUrls();

			System.out.println("\tText length: " + text.length());
			System.out.println("\tHtml length: " + html.length());
			System.out.println("\tNumber of outgoing links: " + links.size());			
		}
		else
		{
			System.out.printf("\tBinary data.\n");
			
		}
		
		Header[] responseHeaders = page.getFetchResponseHeaders();
        if (responseHeaders != null) {
                System.out.printf("\tResponse headers:\n");
                for (Header header : responseHeaders) {
                        System.out.println("\t\t" + header.getName() + ": " + header.getValue());
                }
        }
        System.out.println();       
	}
}
