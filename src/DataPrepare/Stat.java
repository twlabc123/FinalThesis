package DataPrepare;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashSet;

/**
 * The function of this class is to gather statistics of Sogou new data
 * @author twl
 *
 */
public class Stat {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Stat.stat("news_split.dat", "stat.txt");
	}
	
	public static void stat(String input, String output)
	{
		HashSet<String> term = new HashSet<String>();
		HashSet<String> uniTerm = new HashSet<String>();
		try
		{
			FileOutputStream stream = new FileOutputStream(output);
			OutputStreamWriter sw = new OutputStreamWriter(stream, "utf-8");
			PrintWriter writer = new PrintWriter(sw);
			FileInputStream istream = new FileInputStream(input);
			InputStreamReader sr = new InputStreamReader(istream, "utf-8");
			BufferedReader reader = new BufferedReader(sr);
			String line;
			int docNum = 0;
			int termNum = 0;
			int termNumBiaodian = 0;
			int charNum = 0;
			int uniNum = 0;
			while ((line = reader.readLine()) != null)
			{
				String url = reader.readLine();
				String docN = reader.readLine();
				String title = reader.readLine();
				String content = reader.readLine();
				reader.readLine();
				content = content.substring(9, content.length()-10);
				//System.out.println(content);
				String[] ss = content.split(" ");
				termNumBiaodian += ss.length;
				termNum += ss.length;
				for (int i = 0; i<ss.length; i++)
				{
					if (!ss[i].contains("/"))
					{
						termNum--;
						termNumBiaodian--;
						continue;
					}
					if (ss[i].endsWith("/w"))
					{
						termNum--;
						continue;
					}
					term.add(ss[i]);
					charNum += ss[i].substring(0, ss[i].lastIndexOf('/')).length();
					if (ss[i].substring(0, ss[i].lastIndexOf('/')).length() == 1)
					{
						uniNum++;
						uniTerm.add(ss[i]);
					}
				}
				docNum++;
				if (docNum % 10000 == 0)
				{
					System.out.println(term.size());
					System.out.println(docNum);
				}
			}
			writer.println("Doc Num : " + docNum);
			writer.println("Term Num Punctuation : " + termNumBiaodian);
			writer.println("Term Num : " + termNum);
			writer.println("Different Term Num : " + term.size());
			writer.println("Char Num : " + charNum);
			writer.println("Different Uni Num : " + uniTerm.size());
			writer.println("Uni Num : " + uniNum);
			reader.close();
			writer.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
