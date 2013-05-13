package Structure;

import java.io.BufferedReader;
import java.util.HashMap;

public class ArticleExtend extends Article {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public HashMap<String, Integer> tf;
	
	ArticleExtend()
	{
		tf = new HashMap<String, Integer>();
	}
	
	ArticleExtend(Article a)
	{
		this.title = a.title;
		this.content = a.content;
		this.url = a.url;
		this.time = a.time;
		this.source = a.source;
		tf = new HashMap<String, Integer>();
	}
	
	public static ArticleExtend readArticle(BufferedReader reader) throws Exception
	{
		ArticleExtend ret = new ArticleExtend();
		String line = reader.readLine();
		if (line == null || line.startsWith("</event>")) return null;
		line = reader.readLine();
		ret.title = line.substring(7, line.length()-8);
		line = reader.readLine();
		ret.content = line.substring(9, line.length()-10);
		line = reader.readLine();
		ret.url = line.substring(5, line.length()-6);
		line = reader.readLine();
		ret.time = line.substring(6, line.length()-7);
		line = reader.readLine();
		//System.out.println(line);
		ret.source = line.substring(8, line.length()-9);
		reader.readLine();
		return ret;
	}
	
	public Article getArticle()
	{
		Article ret = new Article();
		ret.content = content;
		ret.source = source;
		ret.time = time;
		ret.title = title;
		ret.url = url;
		return ret;
	}

}
