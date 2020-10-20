package allineamenti;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class GitCommands
{
	public static void executeCommand(String comando, String percorsoCartella) throws IOException
	{
		File fileCartella = new File(percorsoCartella);
		Process p = Runtime.getRuntime().exec(comando, null, fileCartella);
		BufferedReader inputStream = new BufferedReader(new InputStreamReader(p.getInputStream()));
		BufferedReader errorStream = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		
		String s1, s2;
		while( (s1 = inputStream.readLine()) != null )
			System.out.println(s1);
		
		while( (s2 = errorStream.readLine()) != null )
			System.out.println(s2);
	}
	
	public static boolean gitCheckout(String comando, String percorsoCartella) throws IOException
	{
		File fileCartella = new File(percorsoCartella);
		Process p = Runtime.getRuntime().exec(comando, null, fileCartella);
		BufferedReader inputStream = new BufferedReader(new InputStreamReader(p.getInputStream()));
		boolean checkoutAvvenuto = false;
		
		String s1;
		while( (s1 = inputStream.readLine()) != null )
		{
			if(s1.contains("Already on") || s1.contains("Switched to branch"))
				checkoutAvvenuto = true;
		}
		
		return checkoutAvvenuto;
	}
}
