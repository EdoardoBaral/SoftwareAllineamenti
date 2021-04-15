package allineamenti;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.lang3.StringUtils;

/**
 * Classe che contiene i metodi statici che eseguono i comandi Git necessari per eseguire l'allineamento degli EJB e dei verticali
 *
 * @author Edoardo Baral
 */
public class GitCommands
{
	/**
	 * Metodo statico che permette l'esecuzione del comando 'git checkout branch' che fa passare il repository su cui si sta operando sul branch indicato come parametro del comando
	 * @param comando: stringa che rappresenta il comando Git per il checkout
	 * @param percorsoCartella: percorso della cartella specifica dell'EJB/verticale per cui si vuole cambiare branch
	 * @return true se il repository passa con successo sul branch indicato, false in caso di errore
	 * @throws IOException nel caso in cui si verifichi un errore nella lettura dell'output del comando 'git checkout'
	 * @throws InterruptedException se si verifica un errore nell'esecuzione del comando Git
	 */
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
	
	/**
	 * Metodo statico che permette l'esecuzione del comando 'git pull branch' per sincronizzare il repository locale dell'EJB/verticale con la controparte remota
	 * @param comando: stringa che rappresenta il comando Git per la pull
	 * @param percorsoCartella: percorso della cartella specifica dell'EJB/verticale che si vuole sincronizzare
	 * @throws IOException nel caso in cui si verifichi un errore nella lettura dell'output del comando 'git pull'
	 */
	static void gitPull(String comando, String percorsoCartella) throws IOException
	{
		File fileCartella = new File(percorsoCartella);
		Process p = Runtime.getRuntime().exec(comando, null, fileCartella);
		BufferedReader errorStream = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		
		String s1;
		while( (s1 = errorStream.readLine()) != null )
		{
			if(StringUtils.containsIgnoreCase(s1, "conflict") | StringUtils.containsIgnoreCase(s1, "fatal") | StringUtils.containsIgnoreCase(s1, "error"))
				throw new IOException("Errore avvenuto durante la pull dell'EJB '"+ percorsoCartella +"'");
		}
	}
	
	/**
	 * Metodo statico che permette l'esecuzione del comando 'git pull origin branch' per effettuare un merge da un altro branch sull'EJB/verticale indicato come argomento
	 * @param comando: stringa che rappresenta il comando Git per il merge
	 * @param percorsoCartella: percorso della cartella specifica dell'EJB/verticale per cui si intende effettuare il merge
	 * @return true se si sono verificati errori o conflitti durante il merge, false in caso il merge sia stato effettuato senza problemi
	 * @throws IOException nel caso in cui si verifichi un errore nella lettura dell'output del comando 'git pull origin'
	 */
	static boolean gitPullOrigin(String comando, String percorsoCartella) throws IOException
	{
		File fileCartella = new File(percorsoCartella);
		Process p = Runtime.getRuntime().exec(comando, null, fileCartella);
		BufferedReader inputStream = new BufferedReader(new InputStreamReader(p.getInputStream()));
		BufferedReader errorStream = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		
		String s1, s2;
		while( (s1 = inputStream.readLine()) != null )
		{
			if(StringUtils.containsIgnoreCase(s1, "conflict") | StringUtils.containsIgnoreCase(s1, "fatal") | StringUtils.containsIgnoreCase(s1, "error"))
				return true;
		}
		while( (s2 = errorStream.readLine()) != null )
		{
			if(StringUtils.containsIgnoreCase(s2, "conflict") | StringUtils.containsIgnoreCase(s2, "fatal") | StringUtils.containsIgnoreCase(s2, "error"))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Metodo che permette l'esecuzione del comando 'git commit' per risolvere un conflitto emerso durante un merge sull'EJB/verticale in esame
	 * @param comando: stringa che rappresenta il comando Git per effettuare un commit
	 * @param percorsoCartella: percorso della cartella specifica dell'EJB/verticale per cui si intende effettuare il commit
	 * @return true se è stato risolto un conflitto con un commit, false se non c'era nessun conflitto da risolvere
	 * @throws IOException nel caso in cui si verifichi un errore nella lettura dell'output del comando 'git commit'
	 */
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
	
	/**
	 * Metodo statico che permette l'esecuzione del comando 'git status' per verificare lo stato del repository in uso
	 * @param comando: stringa che rappresenta il comando Git per verificare lo stato del repository
	 * @param percorsoCartella: percorso della cartella specifica dell'EJB/verticale di cui si intende controllare lo status
	 * @return true se non sono presenti modifiche non committate nel repository, false altrimenti
	 * @throws IOException nel caso in cui si verifichi un errore nella lettura dell'output del comando 'git status'
	 */
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
	
	/**
	 * Metodo che permette l'esecuzione del comando 'git commit --allow-empty -m "commento"' per forzare la ricompilazione su Jenkins dell'EJB/verticale indicato
	 * @param comando: stringa che rappresenta il comando Git per generare un commit vuoto
	 * @param percorsoCartella: percorso della cartella specifica dell'EJB/verticale di cui si intende creare un commit vuoto
	 * @throws IOException nel caso in cui si verifichi un errore nella lettura dell'output del comando 'git commit'
	 * @throws InterruptedException se si verifica un errore nell'esecuzione del comando 'git commit'
	 */
	static void gitCommitVuoto(String comando, String percorsoCartella) throws IOException, InterruptedException
	{
		File fileCartella = new File(percorsoCartella);
		Process p = Runtime.getRuntime().exec(comando, null, fileCartella);
		p.waitFor();
	}
	
	/**
	 * Metodo che permette l'esecuzione del comando 'git push' per inviare al repository remoto i commit effettuati in locale sull'EJB/verticale indicato come argomento
	 * @param comando: stringa che rappresenta il comando Git per inviare i commit al repository remoto
	 * @param percorsoCartella: percorso della cartella dell'EJB/verticale in esame
	 * @return true se la push avviene con successo, false in caso di errore
	 * @throws IOException se si verifica un errore nell'esecuzione del comando 'git push'
	 */
	static boolean gitPush(String comando, String percorsoCartella) throws IOException
	{
		File fileCartella = new File(percorsoCartella);
		Process p = Runtime.getRuntime().exec(comando, null, fileCartella);
		BufferedReader inputStream = new BufferedReader(new InputStreamReader(p.getInputStream()));
		BufferedReader errorStream = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		
		String s1, s2;
		while( (s1 = inputStream.readLine()) != null )
		{
			if(StringUtils.containsIgnoreCase(s1, "fatal: unable to access") || StringUtils.containsIgnoreCase(s1, "error") || StringUtils.containsIgnoreCase(s1, "rejected"))
				return false;
		}
		while( (s2 = errorStream.readLine()) != null )
		{
			if(StringUtils.containsIgnoreCase(s2, "fatal: unable to access") || StringUtils.containsIgnoreCase(s2, "error") || StringUtils.containsIgnoreCase(s2, "rejected"))
				return false;
		}
		
		return true;
	}
	
	/**
	 * Metodo che permette l'esecuzione del comando 'git clone' per scaricare dal repository remoto una copia dell'EJB/verticale indicato come argomento
	 * @param comando: stringa che rappresenta il comando Git per inviare i commit al repository remoto
	 * @param percorsoCartella: percorso della cartella dell'EJB/verticale in esame
	 * @repository: nome dell'EJB/verticale da scaricare
	 * @return true se la push avviene con successo, false in caso di errore
	 * @throws IOException se si verifica un errore nell'esecuzione del comando 'git clone'
	 */
	static void gitClone(String comando, String percorsoCartella, String repository) throws IOException
	{
		File fileCartella = new File(percorsoCartella);
		Process p = Runtime.getRuntime().exec(comando, null, fileCartella);
		BufferedReader errorStream = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		
		String s1;
		while( (s1 = errorStream.readLine()) != null )
		{
			if(StringUtils.containsIgnoreCase(s1, "fatal") | StringUtils.containsIgnoreCase(s1, "error"))
				throw new IOException("Errore avvenuto durante la clone di '"+ repository +"'");
		}
	}
}