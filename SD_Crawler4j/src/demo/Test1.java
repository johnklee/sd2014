package demo;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Test1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {

			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet("http://localhost/FF/crawlme/index.html");
			HttpResponse response = client.execute(request);

			System.out.println("Printing Response Header...\n");

			Header[] headers = response.getAllHeaders();
			for (Header header : headers) {
				System.out.println("Key : " + header.getName() + " ,Value : "
						+ header.getValue());

			}
			StringBuffer strBuf = new StringBuffer();
			BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line=null;
			while((line=br.readLine())!=null) strBuf.append(String.format("%s\r\n", line));
			Document doc = Jsoup.parse(strBuf.toString(), "http://localhost/FF/");
			Elements elms = doc.getElementsByTag("title");
			if(elms.size()>0) System.out.printf("Title: %s\n", elms.get(0).text());
			else System.out.printf("No title!\n");

			System.out.println("\nGet Response Header By Key ...\n");
			String server = response.getFirstHeader("Server").getValue();

			if (server == null) {
				System.out.println("Key 'Server' is not found!");
			} else {
				System.out.println("Server - " + server);
			}

			System.out.println("\n Done");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
