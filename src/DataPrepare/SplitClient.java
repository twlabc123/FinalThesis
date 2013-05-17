package DataPrepare;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
 
 
public class SplitClient {
	
	public static String SENDEND = "#END#";
	
	public static String SERVERIP = "127.0.0.1";//"166.111.138.18";
	public static int SERVERPORT = 12131;
 
   /**
   *
    * @param input
   *            : 输入要分词的 字符串
   * @param _ip
   *            ：服务器的ip地址
   * @param _port
   *            ：服务器的端口
   * @return ：返回已经分好词的内容
   */
   public static String splitString(String input, String _ip, int _port) {
     try {
        // System.out.println( "Using IP@"+_ip + ":" + _port );
 
        Socket client = null;
        client = new Socket(_ip, _port);
 
        PrintWriter out = new PrintWriter(new OutputStreamWriter(client
             .getOutputStream(), "UTF-8"));
        BufferedReader in = new BufferedReader(new InputStreamReader(client
             .getInputStream(), "UTF-8"));
 
        out.println(input);
        out.println(SENDEND);
        out.flush();
        StringBuilder sb = new StringBuilder();
        String line;
        while (true) {
          line = in.readLine();
          if (line == null || line.equals(SENDEND))
             break;
          sb.append(line);
          sb.append('\n');
        }
        // finished reading--- close in stream
        in.close();
        out.close();
        client.close();
 
        return sb.toString().trim();
     } catch (Exception e) {
        e.printStackTrace();
     }
     return null;
   }
 
   /**
   * 将输入的文本文件的内容 分词后保持为输出指定文件
   *
    * @param input
   *            ： 输入文件
   * @param output
   *            ：输出文件
   */
   public static void splitFile(String input, String output, String _ip, int _port) {
     try {
        BufferedReader bfReader = new BufferedReader(new FileReader(input));
        BufferedWriter bfWriter = new BufferedWriter(new FileWriter(output,
             false));
 
        String line;
        int c = 0;
        while ((line = bfReader.readLine()) != null) {
          String ret2 = SplitClient.splitString(line,
               _ip, _port);
          bfWriter.write(ret2);
          bfWriter.newLine();
          if (c++ % 2000 == 0)
             System.out.println("i= " + c + "\t " + ret2);
        }
 
        bfWriter.close();
        bfReader.close();
     } catch (IOException e) {
        e.printStackTrace();
     }
 
   }
   
   public static void splitSogouFile(String input, String output, String _ip, int _port) {
	     try {
	        BufferedReader bfReader = new BufferedReader(new FileReader(input));
	        BufferedWriter bfWriter = new BufferedWriter(new FileWriter(output,
	             false));
	 
	        String line;
	        int c = 0;
	        while ((line = bfReader.readLine()) != null) {
	          String ret2;
	          if (line.startsWith("<title>"))
	          {
	        	  ret2 = SplitClient.splitString(line.substring(7, line.length()-8),
	   	               _ip, _port);
	        	  ret2 = "<title>"+ret2+"</title>";
	          }
	          else if (line.startsWith("<content>"))
	          {
	        	  ret2 = SplitClient.splitString(line.substring(9, line.length()-10),
	   	               _ip, _port);
	        	  ret2 = "<content>"+ret2+"</content>";
	          }
	          else ret2 = line;
	          
	          bfWriter.write(ret2);
	          bfWriter.newLine();
	          if (c++ % 100 == 0)
	             System.out.println("i= " + c + "\t " + ret2);
	        }
	 
	        bfWriter.close();
	        bfReader.close();
	     } catch (IOException e) {
	        e.printStackTrace();
	     }
	 
	   }
 
   public static void main(String[] args) {
 
     // String ret = SplitClient.splitString(input, CONFIG._serverIP,
     // CONFIG._splitPort );
     //String ret = SplitClient.splitString(input, SplitClient.SERVERIP, SplitClient.SERVERPORT);
     //System.out.println(ret);
     SplitClient.splitSogouFile(args[0], args[1], SplitClient.SERVERIP, SplitClient.SERVERPORT);
   }
}

