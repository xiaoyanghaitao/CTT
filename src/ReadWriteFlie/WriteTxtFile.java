package ReadWriteFlie;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
public class WriteTxtFile {
	public String path;
	public WriteTxtFile(String path)
	{
		this.path=path;
	}
	public  void creatTxtFile(String fileName) 
    {
	File file = new File(path+fileName);
    if (!file.exists()) {
    try {
			file.createNewFile();
	    } catch (IOException e) {
		e.printStackTrace();
		}
     }
    }
    public  String onlyReadTxtFile(String fileName) {
		FileInputStream fs;
		String str = null;
		try {
			fs = new FileInputStream(path+fileName);
			StringBuffer   buffer   =   new   StringBuffer(); 
			String line;  
			BufferedReader reader = new BufferedReader(new InputStreamReader(fs)); 
			line   =   reader.readLine(); 
			 while(line   !=   null) {         
				 buffer.append(line);               
				 buffer.append("\r\n");               
				 line   =   reader.readLine();       
				 } 
			 str = buffer.toString();
			 while (str.length() != 0 && str.substring(str.length()-2).equals("\r\n"))
				 str = str.substring(0, str.length()-2);
			 while (str.length() >= 2 &&str.substring(0, 2).equals("\r\n")) 
				 str = str.substring(2);
		    	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
            e.printStackTrace();
        }
		return str;  
    }
public  void writeFile(String fileName, String newStr) {
	String oldStr = onlyReadTxtFile(fileName);
	String filein = oldStr + "\r\n" + newStr;
	FileOutputStream fos;
	PrintStream ps;
	try {
		fos = new FileOutputStream(path+fileName);
		ps = new PrintStream(fos);
		ps.print(filein);
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	}
}
}