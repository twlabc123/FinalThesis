package Useless;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.Vector;

import Structure.ArticleExtend;
import Structure.Event;

public class Test {

	/**
	 * @param args
	 */
	public static double T = 0.4;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//Test.test("data/final/st_test_keyword_"+Test.T+".txt", "data/stat/keyword_"+Test.T+".txt");
		Test.test("data/final/st_test_tfidf_"+Test.T+".txt", "data/stat/tfidf_"+Test.T+".txt");
	}
	
	public static void test(String input, String output)
	{
		try
		{
			double sum = 0;
			int eventsum = 0;
			FileOutputStream stream = new FileOutputStream(output);
			OutputStreamWriter sw = new OutputStreamWriter(stream, "utf-8");
			PrintWriter writer = new PrintWriter(sw);
			FileInputStream istream = new FileInputStream(input);
			InputStreamReader sr = new InputStreamReader(istream, "utf-8");
			BufferedReader reader = new BufferedReader(sr);
			Event e;
			String s = reader.readLine();
			while (s != null)
			{
				System.out.println("New subtopic");
				Vector<String> summary = new Vector<String>();
				Vector<Integer> score = new Vector<Integer>();
				reader.readLine();//id
				reader.readLine();//start
				reader.readLine();//end
				s = reader.readLine();
				int I = Integer.parseInt(s.substring(10, s.length()-11));//eventnum
				String last = "";
				for (int i = 0; i<I; i++)
				{
					reader.readLine();//event
					reader.readLine();//id
					reader.readLine();//value
					s = reader.readLine();
					s = s.substring(9,s.length()-10);
					summary.add(s);
					reader.readLine();//</event>
					score.add(0);
				}
				int i = 0;
				while (i < I)
				{
					System.out.println(summary.elementAt(i));
					boolean in = false;
					int a = -1;
					while (!in)
					{
						try
						{
							in = true;
							a = new Scanner(System.in).nextInt();
						}catch (Exception ex)
						{
							ex.printStackTrace();
							in = false;
						}
					}
					if (a >= 0 && a <= 2)
					{
						score.set(i, a);
						sum += a;
						i++;
					}
					else if (a == 3)
					{
						sum -= score.elementAt(i);
						if (i > 0) i--;
					}
				}
				writer.println("===");
				for (i = 0; i<I; i++)
				{
					System.out.println(score.elementAt(i)+summary.elementAt(i));
					writer.println(score.elementAt(i));
				}
				reader.readLine();///subtopic
				s = reader.readLine();//next
				eventsum += I;
			}
			reader.close();
			writer.close();
			System.out.println(sum/eventsum);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}

}
