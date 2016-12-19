package ReadWriteFlie;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
public class readTxtFile{
	private String path;
	public readTxtFile(String path)
	{
		this.path=path;
	}
	public String readFile(String filename, int number)
	{
		String result = null;
		FileInputStream fs;
		try
		{
			fs = new FileInputStream(path+filename);
			BufferedReader br = new BufferedReader(new InputStreamReader(fs)); 
			String line = null;
			int flag = 0;
			while((line = br.readLine())!=null)
			{
				if (number == flag)
				{
					result = line;
					break;
				}
				flag ++;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
	
}
