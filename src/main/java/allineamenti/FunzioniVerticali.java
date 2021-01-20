package allineamenti;

import static allineamenti.GitCommands.gitCheckout;
import static allineamenti.GitCommands.gitCommitConflitto;
import static allineamenti.GitCommands.gitCommitVuoto;
import static allineamenti.GitCommands.gitPull;
import static allineamenti.GitCommands.gitPullOrigin;
import static allineamenti.GitCommands.gitPush;
import static allineamenti.GitCommands.gitStatus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Classe che contiene tutti i metodi necessari per gestire la procedura di allineamento dei verticali in carico ad Alten
 *
 * @author Edoardo Baral
 */
public class FunzioniVerticali
{
	/**
	 * Metodo statico che esegue la procedura di allineamento dei verticali
	 * @param listaVerticali: lista dei verticali di Alten
	 * @param comando: comando digitato dall'utente sul terminale per avviare l'allineamento
	 */
	static void eseguiAllineamentoVerticali(List<String> listaVerticali, String comando)
	{
		String[] partiComando = comando.split(" ");
		String nomeBranch = partiComando[1];
		String branchOrigine = null;
		if(partiComando.length == 3)
			branchOrigine = partiComando[2];
		System.out.println("--- Allineamento verticali - Branch: "+ nomeBranch +" ---\n");

		String percorso = inputPercorsoCartellaVerticali();

		proceduraCheckoutTuttiVerticali(listaVerticali, nomeBranch, percorso);
		System.out.println();

		System.out.println("--- Pull di tutti i verticali\n");
		pullTuttiVerticali(listaVerticali, percorso);
		
		boolean flagConflitti;
		
		if(nomeBranch.equalsIgnoreCase(StringConstants.BRANCH_SVIL))
		{
			if(branchOrigine != null)
			{
				System.out.println("--- Merge di tutti i verticali dal branch '"+ branchOrigine +"'\n");
				flagConflitti = pullOriginVerticali(listaVerticali, percorso, branchOrigine);
				if(flagConflitti)
					proceduraGestioneConflitti(listaVerticali, percorso);
			}
		}
		else
		{
			if(branchOrigine == null)
			{
				System.out.println("--- Merge di tutti i verticali dal branch '"+ StringConstants.BRANCH_SVIL +"'\n");
				flagConflitti = pullOriginVerticali(listaVerticali, percorso, StringConstants.BRANCH_SVIL);
			}
			else
			{
				System.out.println("--- Merge di tutti i verticali dal branch '"+ branchOrigine +"'\n");
				flagConflitti = pullOriginVerticali(listaVerticali, percorso, branchOrigine);
			}
			if(flagConflitti)
				proceduraGestioneConflitti(listaVerticali, percorso);
		}

		System.out.println("--- Merge di tutti i verticali dal branch master\n");
		flagConflitti = pullOriginMasterVerticali(listaVerticali, percorso);
		if(flagConflitti)
			proceduraGestioneConflitti(listaVerticali, percorso);
		
		proceduraSostituzioneVersioniPom(percorso, listaVerticali);

		boolean verticaliTuttiCommittati = statusVerticali(listaVerticali, percorso);
		if(verticaliTuttiCommittati)
			System.out.println("I verticali sono tutti allineati e non presentano modifiche non committate");
		else
			verificaModificheNonCommittate();
		
		commitVuotoVerticali(listaVerticali, nomeBranch, percorso);
		proceduraPushIntervalliVerticali(listaVerticali, percorso);
		
		System.out.println("--- Allineamento verticali in '"+ nomeBranch +"' terminato ---\n");
	}
	
	/**
	 * Metodo statico privato che permette l'acquisizione di un comando digitato dall'utente su terminale
	 * @return il comando digitato dall'utente su terminale
	 */
	private static String inputScelta()
	{
		Scanner scanner = new Scanner(System.in);
		return scanner.nextLine();
	}
	
	/**
	 * Metodo statico privato che permette l'acquisizione del percorso della cartella contenente i verticali di Alten, digitato dall'utente su terminale
	 * @return il percorso della cartella digitato dall'utente
	 */
	private static String inputPercorsoCartellaVerticali()
	{
		String percorso;
		do
		{
			Scanner scanner = new Scanner(System.in);
			System.out.println(">>> Inserisci il percorso della cartella contenente i verticali (es. 'D:\\Openshift\\Verticali\\cdbp0'): ");
			System.out.println(">>> Oppure inserisci una delle seguenti chiavi presenti: ");
			StringConstants.PATH_VERTICALI.forEach((k, v) -> System.out.println(k + " -> " + v));
			System.out.print(">>> Scelta: ");
			percorso = scanner.nextLine();
			percorso = StringConstants.PATH_VERTICALI.containsKey(percorso.toLowerCase()) ? StringConstants.PATH_VERTICALI.get(percorso.toLowerCase()) : percorso;
			System.out.println();
		} while(!verificaPercorsoCartella(percorso));
		
		return percorso;
	}
	
	/**
	 * Metodo statico privato ceh verifica la validità del percorso della cartella dei verticali, indicata dall'utente su terminale
	 * @param percorso: percorso della cartella contenente i verticali
	 * @return true se il percorso è valido, false altrimenti
	 */
	private static boolean verificaPercorsoCartella(String percorso)
	{
		if(Files.exists(Paths.get(percorso)))
			return true;
		else
		{
			System.out.println("Percorso non trovato. Riprovare\n");
			return false;
		}
	}
	
	/**
	 * Metodo statico privato che avvia la procedura di checkout per far passare tutti i verticali sul branch passato come argomento
	 * @param listaVerticali: lista dei verticali di Alten
	 * @param nomeBranch: nome del branch su cui effettuare il checkout
	 * @param percorso: percorso della cartella contenente i verticali
	 */
	private static void proceduraCheckoutTuttiVerticali(List<String> listaVerticali, String nomeBranch, String percorso)
	{
		List<String> listaVerticaliNonSwitchati = checkoutTuttiVerticali(listaVerticali, nomeBranch, percorso);
		
		while(!listaVerticaliNonSwitchati.isEmpty())
		{
			System.out.println("Si e' verificato un problema nel checkout dei verticali che va risolto manualmente");
			System.out.print(">>> Richiesta conferma per poter continuare e ritentare il checkout dei verticali (S: continua - N: termina programma): ");
			String cmd = inputScelta();
			
			if("N".equalsIgnoreCase(cmd))
			{
				System.out.println("TERMINAZIONE PROGRAMMA");
				System.exit(0);
			}
			else if("S".equalsIgnoreCase(cmd))
			{
				pullVerticaliNonSwitchati(listaVerticali, percorso);
				listaVerticaliNonSwitchati = checkoutVerticaliNonSwitchati(listaVerticaliNonSwitchati, nomeBranch, percorso);
			}
		}
	}
	
	/**
	 * Metodo statico privato che effettua il checkout di tutti i verticali sul branch indicato
	 * @param listaVerticali: lista dei verticali di Alten
	 * @param nomeBranch: branch su cui effettuare il checkout
	 * @param percorso: percorso della cartella contenente i verticali
	 * @return la lista dei verticali che non sono passati sul branch voluto oppure una lista vuota nel caso in cui il checkout sia avvenuto correttamente per tutti i verticali
	 */
	private static List<String> checkoutTuttiVerticali(List<String> listaVerticali, String nomeBranch, String percorso)
	{
		List<String> listaVerticaliNonSwitchati = new ArrayList<>();
		
		for(String verticale : listaVerticali)
		{
			boolean checkoutAvvenuto = checkoutVerticale(percorso +"\\"+ verticale, nomeBranch);
			if(!checkoutAvvenuto)
				listaVerticaliNonSwitchati.add(verticale);
		}
		
		return listaVerticaliNonSwitchati;
	}
	
	/**
	 * Metodo statico privato che effettua il checkout dei soli verticali per cui l'operazione è precedentemente fallita
	 * @param verticaliNonSwitchati: lista dei verticali che in precedenza non sono passati al branch indicato
	 * @param nomeBranch: branch su cui effettuare il checkout
	 * @param percorso: percorso della cartella contenente i verticali
	 * @return la lista dei verticali che non sono passati sul branch voluto oppure una lista vuota nel caso in cui il checkout sia avvenuto correttamente per tutti i verticali
	 */
	private static List<String> checkoutVerticaliNonSwitchati(List<String> verticaliNonSwitchati, String nomeBranch, String percorso)
	{
		for(String verticale : verticaliNonSwitchati)
		{
			boolean checkoutAvvenuto = checkoutVerticale(percorso +"\\"+ verticale, nomeBranch);
			if(checkoutAvvenuto)
				verticaliNonSwitchati.remove(verticale);
		}
		
		return verticaliNonSwitchati;
	}
	
	/**
	 * Metodo statico privato che effettua il checkout del verticale indicato su un determinato branch
	 * @param percorso: percorso del verticale su cui effettuare il checkout
	 * @param nomeBranch: nome del branch su cui effettuare il checkout
	 * @return true se il checkout è avvenuto correttamente, false altrimenti
	 */
	private static boolean checkoutVerticale(String percorso, String nomeBranch)
	{
		try
		{
			boolean checkoutAvvenuto = gitCheckout(StringConstants.COMANDO_GIT_CHECKOUT + nomeBranch, percorso);
			if(checkoutAvvenuto)
				System.out.println("-- Verticale: "+ percorso +" - "+ StringConstants.COMANDO_GIT_CHECKOUT + nomeBranch +" --> OK");
			else
				System.out.println("-- Verticale: "+ percorso +" - "+ StringConstants.COMANDO_GIT_CHECKOUT + nomeBranch +" --> ERRORE");
			
			return checkoutAvvenuto;
		}
		catch(IOException | InterruptedException ex)
		{
			System.out.println("Errore durante il checkout del verticale '"+ percorso +"' sul branch '"+ nomeBranch +"'");
			ex.printStackTrace();
			System.out.println("--------------------");
			return false;
		}
	}
	
	/**
	 * Metodo che effettua la pull su tutti i verticali di Alten, per sincronizzare il repository locale del verticale con il corrispondente repository remoto
	 * @param listaVerticali: lista dei verticali di Alten
	 * @param percorso: percorso della cartella contenente i verticali
	 */
	private static void pullTuttiVerticali(List<String> listaVerticali, String percorso)
	{
		for(String verticale : listaVerticali)
			pullVerticale(percorso +"\\"+ verticale);
		System.out.println();
	}
	
	/**
	 * Metodo che effettua la pull su tutti i verticali che in precedenza non sono passati sul branch desiderato con il checkout
	 * @param verticaliNonSwitchati: lista dei verticali per cui il checkout non è avvenuto correttamente
	 * @param percorso: percorso della cartella contenente i verticali
	 */
	private static void pullVerticaliNonSwitchati(List<String> verticaliNonSwitchati, String percorso)
	{
		for(String verticale : verticaliNonSwitchati)
			pullVerticale(percorso +"\\"+ verticale);
		System.out.println();
	}
	
	/**
	 * Metodo statico privato che effettua la pull sul singolo verticale passato come argomento
	 * @param percorso: percorso dell'EJB su cui effettuare la pull
	 */
	private static void pullVerticale(String percorso)
	{
		System.out.print("-- Verticale: "+ percorso +" - "+ StringConstants.COMANDO_GIT_PULL);
		try
		{
			gitPull(StringConstants.COMANDO_GIT_PULL, percorso);
			System.out.println(" --> OK");
		}
		catch (IOException ex)
		{
			System.out.println(" --> ERROR");
			System.out.println("Errore durante la pull del verticale '"+ percorso +"'");
			ex.printStackTrace();
		}
	}
	
	/**
	 * Metodo statico privato che effettua il merge di tutti i verticali dal branch passato come argomento
	 * @param listaVerticali: lista dei verticali di Alten
	 * @param percorso: percorso della cartella contenente i verticali
	 * @param branchOrigine: nome del branch da cui effettuare il merge
	 * @return true se durante il merge si sono verificati dei conflitti da risolvere su almeno uno dei verticali, false altrimenti
	 */
	private static boolean pullOriginVerticali(List<String> listaVerticali, String percorso, String branchOrigine)
	{
		boolean flagConflitti = false;
		for(String verticale : listaVerticali)
			flagConflitti = flagConflitti | pullOriginVerticale(percorso +"\\"+ verticale, branchOrigine);
		System.out.println();
		
		return flagConflitti;
	}
	
	/**
	 * Metodo statico privato che effettua il merge di un singolo verticale dal branch indicato
	 * @param percorso: percorso del verticale da allineare
	 * @param branchOrigine: nome del branch da cui effettuare il merge
	 * @return true se durante il merge si sono verificati dei conflitti da risolvere, false altrimenti
	 */
	private static boolean pullOriginVerticale(String percorso, String branchOrigine)
	{
		try
		{
			boolean flagConflitti = gitPullOrigin(StringConstants.COMANDO_GIT_PULL_ORIGIN + branchOrigine, percorso);
			if(!flagConflitti)
				System.out.println("-- Verticale: "+ percorso +" - "+ StringConstants.COMANDO_GIT_PULL_ORIGIN + branchOrigine +" --> OK");
			else
				System.out.println("-- Verticale: "+ percorso +" - "+ StringConstants.COMANDO_GIT_PULL_ORIGIN + branchOrigine +" --> CONFLITTI");
			
			return flagConflitti;
		}
		catch (IOException ex)
		{
			System.out.println("Errore durante il merge del verticale '"+ percorso +"' dal branch '"+ StringConstants.BRANCH_SVIL +"'");
			ex.printStackTrace();
			System.out.println("--------------------");
			return false;
		}
	}
	
	/**
	 * Metodo statico privato che permette di avviare la procedura di gestione dei conflitti emersi durante un merge da un altro branch
	 * @param listaVerticali: lista dei verticali di Alten
	 * @param percorso: percorso della cartella contenente i verticali
	 */
	private static void proceduraGestioneConflitti(List<String> listaVerticali, String percorso)
	{
		System.out.println("--- Ci sono conflitti da risolvere");
		System.out.println("    1) Risolvere su IntelliJ i conflitti segnalati");
		System.out.println("    2) Non effettuare il commit per risolvere i conflitti, ci pensa il software");
		System.out.println("    3) Digitare S per far continuare il programma");
		System.out.print(">>> Comando: ");
		inputScelta();
		System.out.println();
		
		System.out.println("--- Commit per risolvere i conflitti su tutti i verticali\n");
		commitConflittiVerticali(listaVerticali, percorso);
		System.out.println();
		System.out.println("--- Conflitti sui verticali risolti\n");
	}
	
	/**
	 * Metodo statico privato che permette di risolvere eventuali conflitti sui verticali, emersi durante un merge, mediante un commit
	 * @param listaVerticali: lista dei verticali di Alten
	 * @param percorso: percorso della cartella contenente i verticali
	 */
	private static void commitConflittiVerticali(List<String> listaVerticali, String percorso)
	{
		for(String verticale : listaVerticali)
			commitConflittiVerticale(percorso +"\\"+ verticale);
	}
	
	/**
	 * Metodo statico privato che permette di risolvere eventuali conflitti su un verticale, emersi durante un merge, mediante un commit
	 * @param percorso: percorso del verticale per cui vanno risolti eventuali conflitti
	 */
	private static void commitConflittiVerticale(String percorso)
	{
		try
		{
			boolean flagCommit = gitCommitConflitto(StringConstants.COMANDO_GIT_COMMIT, percorso);
			if(flagCommit)
				System.out.println("-- Verticale: "+ percorso +" - "+ StringConstants.COMANDO_GIT_COMMIT +" --> CONFLITTI RISOLTI");
			else
				System.out.println("-- Verticale: "+ percorso +" - "+ StringConstants.COMANDO_GIT_COMMIT +" --> NESSUN CONFLITTO DA RISOLVERE");
		}
		catch (IOException ex)
		{
			System.out.println("Errore durante il commit per i conflitti del verticale '"+ percorso +"'");
			ex.printStackTrace();
			System.out.println("--------------------");
		}
	}
	
	/**
	 * Metodo statico privato che effettua il merge di tutti i verticali dal branch master
	 * @param listaVerticali: lista dei verticali di Alten
	 * @param percorso: percorso della cartella contenente i verticali
	 * @return true se sono presenti dei conflitti da risolvere emersi durante il merge, false se non ci sono conflitti
	 */
	private static boolean pullOriginMasterVerticali(List<String> listaVerticali, String percorso)
	{
		boolean flagConflitti = false;
		for(String verticale : listaVerticali)
			flagConflitti = flagConflitti | pullOriginMasterVerticale(percorso +"\\"+ verticale);
		System.out.println();
		
		return flagConflitti;
	}
	
	/**
	 * Metodo statico privato che permette di effettuare il merge del singolo verticale dal branch master
	 * @param percorso: percorso della cartella del verticale da allineare al branch master
	 * @return true se sono presenti dei conflitti da risolvere emersi durante il merge, false se non ci sono conflitti
	 */
	private static boolean pullOriginMasterVerticale(String percorso)
	{
		try
		{
			boolean flagConflitti = gitPullOrigin(StringConstants.COMANDO_GIT_PULL_ORIGIN + StringConstants.BRANCH_MASTER, percorso);
			if(!flagConflitti)
				System.out.println("-- Verticale: "+ percorso +" - "+ StringConstants.COMANDO_GIT_PULL_ORIGIN + StringConstants.BRANCH_MASTER +" --> OK");
			else
				System.out.println("-- Verticale: "+ percorso +" - "+ StringConstants.COMANDO_GIT_PULL_ORIGIN + StringConstants.BRANCH_MASTER +" --> CONFLITTI");
			
			return flagConflitti;
		}
		catch (IOException ex)
		{
			System.out.println("Errore durante il merge del verticale '"+ percorso +"' dal branch '"+ StringConstants.BRANCH_MASTER +"'");
			ex.printStackTrace();
			System.out.println("--------------------");
			return false;
		}
	}
	
	/**
	 * Metodo statico privato che verifica se nei verticalidi Alten sono presenti modifiche non ancora committate
	 * @param listaVerticali: lista dei verticali di Alten
	 * @param percorso: percorso della cartella che contiene i verticali di Alten
	 * @return true se tutti i verticali non presentano aluna modifica da committare, false se almeno uno presenta modifiche non ancora committate
	 */
	private static boolean statusVerticali(List<String> listaVerticali, String percorso)
	{
		boolean tuttoCommittato = false;
		for(String verticale : listaVerticali)
			tuttoCommittato = tuttoCommittato | statusVerticale(percorso +"\\"+ verticale);
		System.out.println();
		
		return tuttoCommittato;
	}
	
	/**
	 * Metodo statico privato che verifica se nel singolo verticale sono presenti modifiche non ancora committate
	 * @param percorso: percorso della cartella del verticale
	 * @return true se il verticale non presenta alcuna modifica da committare, false altrimenti
	 */
	private static boolean statusVerticale(String percorso)
	{
		try
		{
			boolean flagCommit = gitStatus(StringConstants.COMANDO_GIT_STATUS, percorso);
			if(flagCommit)
				System.out.println("-- Verticale: "+ percorso +" - "+ StringConstants.COMANDO_GIT_STATUS +" --> NESSUNA MODIFICA DA COMMITTARE");
			else
				System.out.println("-- Verticale: "+ percorso +" - "+ StringConstants.COMANDO_GIT_STATUS +" --> CI SONO MODIFICHE DA COMMITTARE");
			
			return flagCommit;
		}
		catch (IOException ex)
		{
			System.out.println("Errore durante la verifica dello status del verticale '"+ percorso +"'");
			ex.printStackTrace();
			System.out.println("--------------------");
			return false;
		}
	}
	
	/**
	 * Metodo statico privato che permette all'utente di verificare la presenza di modifiche non committate per i verticali di Alten
	 */
	private static void verificaModificheNonCommittate()
	{
		String scelta;
		do
		{
			System.out.println("--- Verifica status verticali");
			System.out.println("    1) Verificare su IntelliJ la presenza di modifiche non committate nei verticali");
			System.out.println("    2) Procedere su IntelliJ con il commit delle modifiche o con il revert, a scelta");
			System.out.println("    3) Digitare S per continuare con il programma");
			System.out.print(">>> Comando: ");
			scelta = inputScelta();
			System.out.println();
		} while(!"S".equalsIgnoreCase(scelta));
		
		System.out.println();
	}
	
	/**
	 * Metodo statico privato che permette di effettuare un commit vuoto per forzare la ricompilazione dei verticali
	 * @param listaVerticali: lista dei verticali di Alten
	 * @param nomeBranch: nome del branch su cui si sta operando
	 * @param percorso: percorso della cartella che contiene i verticali
	 */
	private static void commitVuotoVerticali(List<String> listaVerticali, String nomeBranch, String percorso)
	{
		for(String verticale : listaVerticali)
			commitVuotoVerticale(percorso +"\\"+ verticale, nomeBranch);
		System.out.println();
	}
	
	/**
	 * Metodo statico privato che permette di effettuare un commit vuoto per forzare la ricompilazione del verticale indicato
	 * @param percorso: percorso della cartella del verticale da ricompilare
	 * @param nomeBranch: nome del branch su cui si sta operando
	 */
	private static void commitVuotoVerticale(String percorso, String nomeBranch)
	{
		try
		{
			if(StringConstants.BRANCH_SVIL.equals(nomeBranch))
			{
				System.out.print("-- Verticale: "+ percorso +" - "+ StringConstants.COMANDO_GIT_RELEASE);
				gitCommitVuoto(StringConstants.COMANDO_GIT_RELEASE, percorso);
			}
			else
			{
				System.out.print("-- Verticale: "+ percorso +" - "+ StringConstants.COMANDO_GIT_TAG_PROMOTE);
				gitCommitVuoto(StringConstants.COMANDO_GIT_TAG_PROMOTE, percorso);
			}
			System.out.println(" --> OK");
		}
		catch(IOException | InterruptedException ex)
		{
			System.out.println(" --> ERRORE");
			ex.printStackTrace();
		}
	}
	
	/**
	 * Metodo static privato che permette di effettuare la push sui verticali, in modo da inviare i commit nuovi sul repository remoto.
	 * Per evitare di sovraccaricare Jenkins, le buil vengono lanciate a blocchi: data la lista completa dei verticali, l'utente deve
	 * indicare due verticali che delimitano l'intervallo dei verticali per cui va effettuata la push
	 * @param listaVerticali: lista dei verticali di Alten
	 * @param v1: primo verticale che rappresenta l'estremo inferiore dell'intervallo di verticali da ricompilare
	 * @param v2: secondo verticale che rappresenta l'estremo superiore dell'intervallo di verticali da ricompilare
	 * @param percorso: percorso della cartella contenente i verticali
	 */
	private static void pushVerticali(List<String> listaVerticali, String v1, String v2, String percorso)
	{
		List<String> intervalloVerticali = listaVerticali.subList(listaVerticali.indexOf(v1), listaVerticali.indexOf(v2)+1);
		for(String verticale : intervalloVerticali)
			pushVerticale(percorso +"\\"+ verticale);
		System.out.println();
	}
	
	/**
	 * Metodo static privato che permette di effettuare la push sul verticale indicato, in modo da inviare i commit nuovi sul repository remoto
	 * @param percorso: percorso della cartella del verticale
	 */
	private static void pushVerticale(String percorso)
	{
		try
		{
			boolean flagPush = gitPush(StringConstants.COMANDO_GIT_PUSH, percorso);
			if(flagPush)
				System.out.println("-- Verticale: "+ percorso +" - "+ StringConstants.COMANDO_GIT_PUSH +" --> OK");
			else
				System.out.println("-- Verticale: "+ percorso +" - "+ StringConstants.COMANDO_GIT_PUSH +" --> ERRORE");
		}
		catch (IOException ex)
		{
			System.out.println("Errore durante la push dei commit del verticale '"+ percorso +"'");
			ex.printStackTrace();
			System.out.println("--------------------");
		}
	}
	
	/**
	 * Metodo statico privato che verifica la validità dell'intervallo di verticali indicato dall'utente: data la lista dei verticali
	 * ordinata alfabeticamente, il primo verticale deve precedere il secondo nell'ordine alfabetico altrimenti l'intervallo non è valido
	 * @param v1: primo verticale, estremo inferiore dell'intervallo
	 * @param v2: secondo verticale, estremo superiore dell'intervallo
	 * @return true se il primo verticale precede alfabeticamente il secondo e l'intervallo è valido, false altrimenti
	 */
	private static boolean verificaIntervalloVerticali(String v1, String v2)
	{
		if(v1 == null || v2 == null)
			return false;
		if("".equals(v1) || "".equals(v2))
			return false;
		
		return (v1.compareTo(v2) < 0);
	}
	
	/**
	 * Metodo statico privato che permette di eseguire la procedura di sostituzione automatica delle versioni nei POM padri dei verticali
	 * @param percorsoCartellaVerticali: percorso della cartella dei verticali
	 */
	private static void proceduraSostituzioneVersioniPom(String percorsoCartellaVerticali, List<String> listaVerticali)
	{
		System.out.println("--- Verifica e sostituzione automatica delle versioni aggiornate nei POM dei verticali");
		System.out.println("    1) Assicurati che nel file VersioniPOM.txt siano presenti tutte le versioni da aggiornare") ;
		System.out.println("    2) Digita S per avviare la procedura oppure N per saltarla") ;
		System.out.println("    3) Una volta terminata la procedura, verifica da IntelliJ la correttezza delle modifiche e committa");
		System.out.print(">>> Scelta: ");
		String scelta = inputScelta();
		
		if("N".equalsIgnoreCase(scelta))
			return;
		
		SetupApplication setupApplication = new SetupApplication();
		try
		{
			List<String> listaVersioni = setupApplication.leggiVersioniPom();
			sostituzioneVersioniPom(percorsoCartellaVerticali, listaVerticali, listaVersioni);
		}
		catch(IOException ex)
		{
			System.out.println("Errore nell'aggiornamento delle versioni dei POM");
			ex.printStackTrace();
		}
		System.out.println();
	}
	
	private static void sostituzioneVersioniPom(String percorsoCartellaVerticali, List<String> listaVerticali, List<String> listaVersioni)
	{
		for(String verticale : listaVerticali)
		{
			String pathPomPadre = percorsoCartellaVerticali +"\\"+ verticale +"\\pom.xml";
			File pom = new File(pathPomPadre);
			sostituisciVersioni(pom, listaVersioni);
		}
	}
	
	/**
	 * Metodo statico privato che opera la sostituzione vera e propria delle versioni nei POM dei verticali. I file POM vengono prima parsificati come XML dopodiché, per ognuno dei
	 * tag presenti nella lista, si verifica se è presente nel file POM e, nel caso, si aggiorna con il corrispondente preso dalla lista
	 * @param filePom: file POM su cu va operata la sostituzione delle versioni
	 * @param listaVersioni: lista di stringhe che contiene tutti i tag XML che vanno aggiornati nei POM, ove presenti
	 */
	private static void sostituisciVersioni(File filePom, List<String> listaVersioni)
	{
		System.out.println("--- Aggiornamento versioni nel file "+ filePom.getAbsolutePath());
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(filePom);
			
			boolean aggiornamentoFileNecessario = false;
			
			for(String riga : listaVersioni)
			{
				String nomeTag = riga.substring(1, riga.indexOf('>'));
				String versioneAggiornata = riga.substring(riga.indexOf('>')+1, riga.lastIndexOf('<'));
				
				NodeList nodeList = document.getElementsByTagName(nomeTag);
				if(nodeList.getLength() > 0)
					aggiornamentoFileNecessario = true;
				
				for(int i=0; i<nodeList.getLength(); i++)
				{
					Node node = nodeList.item(i);
					System.out.println("--- [PRIMA] "+ node.getNodeName() +" - "+ node.getTextContent());
					node.setTextContent(versioneAggiornata);
					System.out.println("--- [DOPO]  "+ node.getNodeName() +" - "+ node.getTextContent());
				}
			}
			
			if(aggiornamentoFileNecessario)
			{
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				transformer.setOutputProperty("omit-xml-declaration", "yes");
				DOMSource source = new DOMSource(document);
				StreamResult result = new StreamResult(filePom);
				
				transformer.transform(source, result);
				System.out.println("--- Scrittura completata");
			}
		}
		catch (ParserConfigurationException | SAXException | IOException | TransformerException ex)
		{
			System.err.println("--- Errore nella sostituzione delle versioni nel POM "+ filePom.getAbsolutePath());
			ex.printStackTrace();
		}
		System.out.println("--------------------");
	}
	
	/**
	 * Metodo statico che permette di avviare la procedura di sostituzione automatica delle versioni nei POM padri dei verticali
	 * @param listaVerticali: lista dei verticali di Alten
	 * @param comando: stringa che rappresenta il comando digitato dall'utente nel terminale
	 */
	static void eseguiSostituzioneAutomatica(List<String> listaVerticali, String comando)
	{
		String nomeBranch = comando.substring(13);
		System.out.println("--- Procedura di sostituzione automatica delle versioni nei POM dei verticali - Branch: "+ nomeBranch +" ---\n");
		
		String percorso = inputPercorsoCartellaVerticali();
		
		System.out.println("--- Checkout di tutti i verticali\n");
		proceduraCheckoutTuttiVerticali(listaVerticali, nomeBranch, percorso);
		System.out.println();
		
		System.out.println("--- Pull di tutti i verticali\n");
		pullTuttiVerticali(listaVerticali, percorso);
		System.out.println();
		
		boolean flagConflitti;
		
		System.out.println("--- Merge di tutti i verticali dal branch master\n");
		flagConflitti = pullOriginMasterVerticali(listaVerticali, percorso);
		if(flagConflitti)
			proceduraGestioneConflitti(listaVerticali, percorso);
		
		proceduraSostituzioneVersioniPom(percorso, listaVerticali);
		verificaModificheNonCommittate();
		commitVuotoVerticali(listaVerticali, nomeBranch, percorso);
		proceduraPushIntervalliVerticali(listaVerticali, percorso);
		
		System.out.println("--- Sostituzione automatica terminata ---\n");
	}
	
	/**
	 * Metodo statico privato che gestisce la procedura di push a blocchi sui verticali
	 * @param listaVerticali: lista dei verticali di Alten
	 * @param percorso: percorso della cartella contenente i verticali
	 */
	private static void proceduraPushIntervalliVerticali(List<String> listaVerticali, String percorso)
	{
		boolean checkTerminazionePush;
		do
		{
			boolean flagIntervalloValido = true;
			String v1, v2;
			do
			{
				System.out.println("--- Specificare i nomi dei due verticali che determinano l'intervallo dei verticali da compilare, estremi inclusi");
				System.out.print("    1) Primo verticale: ");
				v1 = inputScelta();
				System.out.print("    2) Secondo verticale: ");
				v2 = inputScelta();
				
				if(!verificaIntervalloVerticali(v1, v2))
				{
					System.out.println("Uno dei verticali indicati o entrambi non sono validi");
					System.out.println("Riprovare assicurandosi che entrambi i nomi siano validi e che siano indicati in ordine alfabetico");
					flagIntervalloValido = false;
				}
			} while(!flagIntervalloValido);
			
			pushVerticali(listaVerticali, v1, v2, percorso);
			
			System.out.print(">>> Vuoi eseguire la push su un altro intervallo di verticali (S/N)? ");
			String scelta = inputScelta();
			System.out.println();
			
			checkTerminazionePush = !"S".equalsIgnoreCase(scelta);
		} while(!checkTerminazionePush);
	}
}
