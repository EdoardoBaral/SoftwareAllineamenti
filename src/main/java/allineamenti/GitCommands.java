package allineamenti;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.lang3.StringUtils;

public class GitCommands
{
	static void executeCommand(String comando, String percorsoCartella) throws IOException
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
	
	static boolean gitCheckout(String comando, String percorsoCartella) throws IOException, InterruptedException
	{
		File fileCartella = new File(percorsoCartella);
		Process p1 = Runtime.getRuntime().exec(comando, null, fileCartella); //Esecuzione del comando 'git checkout <nomeBranch>'
		p1.waitFor();
		
		String nomeBranch = comando.substring(comando.length()-8);
		Process p2 = Runtime.getRuntime().exec(StringConstants.COMANDO_GIT_BRANCH, null, fileCartella); //Stampa del branch corrente in seguito al checkout
		p2.waitFor();
		
		BufferedReader inputStream = new BufferedReader(new InputStreamReader(p2.getInputStream()));
		boolean checkoutAvvenuto = false;
		
		String s1;
		while( (s1 = inputStream.readLine()) != null )
		{
			if(StringUtils.containsIgnoreCase(s1, nomeBranch))
				checkoutAvvenuto = true;
		}
		
		return checkoutAvvenuto;
	}
	
	static void gitPull(String comando, String percorsoCartella) throws IOException
	{
		File fileCartella = new File(percorsoCartella);
		Process p = Runtime.getRuntime().exec(comando, null, fileCartella);
		BufferedReader errorStream = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		
		String s1;
		while( (s1 = errorStream.readLine()) != null )
		{
			if(StringUtils.containsIgnoreCase(s1, "conflict"))
				throw new IOException("Errore avvenuto durante la pull dell'EJB '"+ percorsoCartella +"'");
		}
	}
	
	static boolean gitPullOrigin(String comando, String percorsoCartella) throws IOException
	{
		File fileCartella = new File(percorsoCartella);
		Process p = Runtime.getRuntime().exec(comando, null, fileCartella);
		BufferedReader inputStream = new BufferedReader(new InputStreamReader(p.getInputStream()));
		BufferedReader errorStream = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		
		String s1, s2;
		while( (s1 = inputStream.readLine()) != null )
		{
			if(StringUtils.containsIgnoreCase(s1, "conflict"))
				return true;
		}
		while( (s2 = errorStream.readLine()) != null )
		{
			if(StringUtils.containsIgnoreCase(s2, "conflict"))
				return true;
		}
		
		return false;
	}
	
	static boolean gitCommitConflitto(String comando, String percorsoCartella) throws IOException
	{
		File fileCartella = new File(percorsoCartella);
		Process p = Runtime.getRuntime().exec(comando, null, fileCartella);
		BufferedReader inputStream = new BufferedReader(new InputStreamReader(p.getInputStream()));
		
		String s1;
		while( (s1 = inputStream.readLine()) != null )
		{
			if(StringUtils.containsIgnoreCase(s1, "nothing to commit"))
				return false;
		}
		
		return true;
	}
	
	static boolean gitStatus(String comando, String percorsoCartella) throws IOException
	{
		File fileCartella = new File(percorsoCartella);
		Process p = Runtime.getRuntime().exec(comando, null, fileCartella);
		BufferedReader inputStream = new BufferedReader(new InputStreamReader(p.getInputStream()));
		
		String s1;
		while( (s1 = inputStream.readLine()) != null )
		{
			if(StringUtils.containsIgnoreCase(s1, "nothing to commit"))
				return true;
		}
		
		return false;
	}
	
	static void gitCommitVuoto(String comando, String percorsoCartella) throws IOException, InterruptedException
	{
		File fileCartella = new File(percorsoCartella);
		Process p = Runtime.getRuntime().exec(comando, null, fileCartella);
		p.waitFor();
	}
	
	static boolean gitPush(String comando, String percorsoCartella) throws IOException
	{
		File fileCartella = new File(percorsoCartella);
		Process p = Runtime.getRuntime().exec(comando, null, fileCartella);
		BufferedReader inputStream = new BufferedReader(new InputStreamReader(p.getInputStream()));
		BufferedReader errorStream = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		
		String s1, s2;
		while( (s1 = inputStream.readLine()) != null )
		{
			if(StringUtils.containsIgnoreCase(s1, "fatal: unable to access"))
				return false;
		}
		while( (s2 = errorStream.readLine()) != null )
		{
			if(StringUtils.containsIgnoreCase(s2, "fatal: unable to access"))
				return false;
		}
		
		return false;
	}
}
