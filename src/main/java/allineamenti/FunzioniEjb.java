package allineamenti;

import static allineamenti.GitCommands.gitCheckout;
import static allineamenti.GitCommands.gitClone;
import static allineamenti.GitCommands.gitCommitConflitto;
import static allineamenti.GitCommands.gitCommitVuoto;
import static allineamenti.GitCommands.gitPull;
import static allineamenti.GitCommands.gitPullOrigin;
import static allineamenti.GitCommands.gitPush;
import static allineamenti.GitCommands.gitStatus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Classe che contiene i metodi necessari per le operazioni di allineamento sugli EJB migrati di Alten
 *
 * @author Edoardo Baral
 */
public class FunzioniEjb
{
	/**
	 * Metodo statico che esegue la procedura di allineamento degli EJB migrati di Alten
	 * @param mapEjb: hashmap che contiene gli EJB di Alten suddivisi per blocchi di compilazione
	 * @param comando: comando digitato dall'utente sul terminale per avviare l'allineamento
	 */
	static void eseguiAllineamentoEjb(Map<String, List<String>> mapEjb, String comando)
	{
		String[] partiComando = comando.split(" ");
		String nomeBranch = partiComando[1];
		String branchOrigine = null;
		if(partiComando.length == 3)
			branchOrigine = partiComando[2];
		
		System.out.println("********** Allineamento EJB migrati - Branch: "+ nomeBranch +" **********\n");
		
		String percorso = inputPercorsoCartellaEjb();
		
		proceduraCheckoutTuttiEjb(mapEjb, nomeBranch, percorso);
		
		System.out.println("********** Pull di tutti gli EJB migrati **********\n");
		pullTuttiEjb(mapEjb, percorso);
		
		System.out.println("********** Inizializzazione dell'hashmap per le versioni delle dipendenze **********");
		HashMap<String, String> mappaDipendenze = new HashMap<>();
		System.out.print(">>> Indica la versione di arch.core.ndce: ");
		String archCoreNdceVersion = inputScelta();
		mappaDipendenze.put("arch.core.ndce.version", archCoreNdceVersion);
		System.out.println();
		
		String nomeBloccoEjb;
		boolean allineamentoEjbTerminato;
		do
		{
			do {
				System.out.print(">>> Quale blocco di EJB intendi compilare (es. B0, B1, B2...)? ");
				nomeBloccoEjb = StringUtils.upperCase(inputScelta());
				if(!mapEjb.containsKey(nomeBloccoEjb))
					System.out.println("Il blocco selezionato non esiste. Riprovare\n");
			}	while((!mapEjb.containsKey(nomeBloccoEjb)));
			System.out.println();
			
			boolean flagConflitti;
			
			if(nomeBranch.equalsIgnoreCase(StringConstants.BRANCH_SVIL))
			{
				if(branchOrigine != null)
				{
					System.out.println("********** Merge di tutti gli EJB migrati del blocco "+ nomeBloccoEjb +" dal branch '"+ branchOrigine +"' **********\n");
					flagConflitti = pullOriginEjbBlocco(mapEjb, nomeBloccoEjb, percorso, branchOrigine);
					if(flagConflitti)
						proceduraGestioneConflitti(mapEjb, nomeBloccoEjb, percorso);
				}
			}
			else
			{
				if(branchOrigine == null)
				{
					System.out.println("********** Merge di tutti gli EJB migrati del blocco "+ nomeBloccoEjb +" dal branch '"+ StringConstants.BRANCH_SVIL +"' **********\n");
					flagConflitti = pullOriginEjbBlocco(mapEjb, nomeBloccoEjb, percorso, StringConstants.BRANCH_SVIL);
				}
				else
				{
					System.out.println("********** Merge di tutti gli EJB migrati del blocco "+ nomeBloccoEjb +" dal branch '"+ branchOrigine +"' **********\n");
					flagConflitti = pullOriginEjbBlocco(mapEjb, nomeBloccoEjb, percorso, branchOrigine);
				}
				if(flagConflitti)
					proceduraGestioneConflitti(mapEjb, nomeBloccoEjb, percorso);
				aggiornamentoVersioniDipendenzePOM(mappaDipendenze, mapEjb, nomeBloccoEjb, percorso);
			}
			
			if(StringConstants.BRANCH_SVIL.equalsIgnoreCase(nomeBranch))
			{
				System.out.println("********** Merge di tutti gli EJB migrati dal branch master **********\n");
				flagConflitti = pullOriginMasterEjbBlocco(mapEjb, nomeBloccoEjb, percorso);
				if(flagConflitti)
					proceduraGestioneConflitti(mapEjb, nomeBloccoEjb, percorso);
				aggiornamentoVersioniDipendenzePOM(mappaDipendenze, mapEjb, nomeBloccoEjb, percorso);
			}
			
			boolean tuttoCommittatoEjbBlocco = statusEjbBlocco(mapEjb, nomeBloccoEjb, percorso);
			while(!tuttoCommittatoEjbBlocco)
			{
				verificaModificheNonCommittate(nomeBloccoEjb);
				tuttoCommittatoEjbBlocco = statusEjbBlocco(mapEjb, nomeBloccoEjb, percorso);
			}
			System.out.println("********** Gli EJB del blocco "+ nomeBloccoEjb +" non presentano modifiche non committate **********\n");
			
			commitVuotoEjbBlocco(mapEjb, nomeBloccoEjb, nomeBranch, percorso);
			pushEjbBlocco(mapEjb, nomeBloccoEjb, percorso);
			aggiornamentoFileVersioniPOM(mapEjb, mappaDipendenze, nomeBloccoEjb);
			
			System.out.println("********** Allineamento degli EJB migrati del blocco "+ nomeBloccoEjb +" completato **********\n");
			allineamentoEjbTerminato = verificaTerminazioneAllineamento(nomeBranch);
		} while(!mapEjb.containsKey(nomeBloccoEjb) || !allineamentoEjbTerminato);
	}
	
	/**
	 * Metodo statico privato che permette di acquisire da terminale un comando digitato dall'utente
	 * @return il comando digitato dall'utente su terminale
	 */
	private static String inputScelta()
	{
		Scanner scanner = new Scanner(System.in);
		return scanner.nextLine();
	}
	
	/**
	 * Metodo statico privato che permette l'acquisizione da terminale del percorso della cartella che contiene gli EJB di Alten
	 * @return il percorso della cartella contenente gli EJB da allineare
	 */
	private static String inputPercorsoCartellaEjb()
	{
		String percorso;
		do
		{
			Scanner scanner = new Scanner(System.in);
			System.out.println(">>> Inserisci il percorso della cartella contenente gli EJB (es. 'D:\\Openshift\\EJB') oppure inserisci una delle seguenti chiavi presenti: ");
			StringConstants.PATH_EJB.forEach((k, v) -> System.out.println("   -- "+ k + " --> " + v));
			System.out.print(">>> Scelta: ");
			percorso = scanner.nextLine();
			percorso = StringConstants.PATH_EJB.containsKey(percorso.toLowerCase()) ? StringConstants.PATH_EJB.get(percorso.toLowerCase()) : percorso;
			System.out.println();
		} while(!verificaPercorsoCartella(percorso));
		
		return percorso;
	}
	
	/**
	 * Metodo statico privato che verifica la validità del percorso della cartella contenente gli EJB
	 * @param percorso: percorso della cartella
	 * @return true se la cartella esiste nel percorso indicato, false altrimenti
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
	 * Metodo statico privato che avvia la procedura di checkout per far passare tutti gli EJB sul branch passato come argomento
	 * @param mapEjb: hashmap contenente tutti gli EJB suddivisi per blocchi di compilazione
	 * @param nomeBranch: nome del branch su cui effettuare il checkout
	 * @param percorso: percorso della cartella contenente gli EJB
	 */
	private static void proceduraCheckoutTuttiEjb(Map<String, List<String>> mapEjb, String nomeBranch, String percorso)
	{
		List<String> listaEjbNonSwitchati = checkoutTuttiEjb(mapEjb, nomeBranch, percorso);
		
		while(!listaEjbNonSwitchati.isEmpty())
		{
			System.out.println("Si e' verificato un problema nel checkout degli EJB che va risolto manualmente");
			System.out.print(">>> Richiesta conferma per poter continuare e ritentare il checkout degli EJB (S: continua - N: termina programma): ");
			String cmd = inputScelta();
			
			if("N".equalsIgnoreCase(cmd))
			{
				System.out.println("TERMINAZIONE PROGRAMMA");
				System.exit(0);
			}
			else if("S".equalsIgnoreCase(cmd))
			{
				pullEjbNonSwitchati(listaEjbNonSwitchati, percorso);
				listaEjbNonSwitchati = checkoutEjbNonSwitchati(listaEjbNonSwitchati, nomeBranch, percorso);
			}
		}
		System.out.println();
	}
	
	/**
	 * Metodo statico privato che effettua il checkout di tutti gli EJB sul branch indicato
	 * @param mapEjb: hashmap contenente tutti gli EJB suddivisi per blocchi di compilazione
	 * @param nomeBranch: branch su cui effettuare il checkout
	 * @param percorso: percorso della cartella contenente gli EJB
	 * @return la lista degli EJB che non sono passati sul branch voluto oppure una lista vuota nel caso in cui il checkout sia avvenuto correttamente per tutti gli EJB
	 */
	private static List<String> checkoutTuttiEjb(Map<String, List<String>> mapEjb, String nomeBranch, String percorso)
	{
		List<String> listaEjb = convertiMapEjbInLista(mapEjb);
		List<String> listaEjbNonSwitchati = new ArrayList<>();
		
		for(String ejb : listaEjb)
		{
			boolean checkoutAvvenuto = checkoutEjb(percorso + "\\" + ejb, nomeBranch);
			if(!checkoutAvvenuto)
				listaEjbNonSwitchati.add(ejb);
		}
		
		return listaEjbNonSwitchati;
	}
	
	/**
	 * Metodo statico privato che effettua il checkout dei soli EJB per cui l'operazione è precedentemente fallita
	 * @param ejbNonSwitchati: lista degli EJB che in precedenza non sono passati al branch indicato
	 * @param nomeBranch: branch su cui effettuare il checkout
	 * @param percorso: percorso della cartella contenente gli EJB
	 * @return la lista degli EJB che non sono passati sul branch voluto oppure una lista vuota nel caso in cui il checkout sia avvenuto correttamente per tutti gli EJB
	 */
	private static List<String> checkoutEjbNonSwitchati(List<String> ejbNonSwitchati, String nomeBranch, String percorso)
	{
		for(String ejb : ejbNonSwitchati)
		{
			boolean checkoutAvvenuto = checkoutEjb(percorso +"\\"+ ejb, nomeBranch);
			if(checkoutAvvenuto)
				ejbNonSwitchati.remove(ejb);
		}
		
		return ejbNonSwitchati;
	}
	
	/**
	 * Metodo statico privato che recupera da mapEjb i nomi degli EJB di Alten e li inserisce all'interno di una lista
	 * @param mapEjb: hashmap contenente gli EJB di Alten suddivisi per blocchi di compilazione
	 * @return la lista degli EJB di Alten ordinata alfabeticamente
	 */
	private static List<String> convertiMapEjbInLista(Map<String, List<String>> mapEjb)
	{
		List<String> listaEjb = new ArrayList<>();
		Collection<List<String>> collectionEjb = mapEjb.values();
		
		for(List<String> blocco : collectionEjb)
			listaEjb.addAll(blocco);
		
		Collections.sort(listaEjb);
		
		return listaEjb;
	}
	
	/**
	 * Metodo statico privato che effettua il checkout dell'EJB indicato su un determinato branch
	 * @param percorso: percorso dell'EJB su cui effettuare il checkout
	 * @param nomeBranch: nome del branch su cui effettuare il checkout
	 * @return true se il checkout è avvenuto correttamente, false altrimenti
	 */
	private static boolean checkoutEjb(String percorso, String nomeBranch)
	{
		try
		{
			boolean checkoutAvvenuto = gitCheckout(StringConstants.COMANDO_GIT_CHECKOUT + nomeBranch, percorso);
			if(checkoutAvvenuto)
				System.out.println("-- EJB: "+ StringUtils.rightPad(percorso, 60, " ") +" - "+ StringConstants.COMANDO_GIT_CHECKOUT + nomeBranch +" --> OK");
			else
				System.out.println("-- EJB: "+ StringUtils.rightPad(percorso, 60, " ") +" - "+ StringConstants.COMANDO_GIT_CHECKOUT + nomeBranch +" --> ERRORE");
			
			return checkoutAvvenuto;
		}
		catch(IOException | InterruptedException ex)
		{
			System.out.println("Errore durante il checkout dell'EJB '"+ percorso +"' sul branch '"+ nomeBranch +"'");
			ex.printStackTrace();
			System.out.println("--------------------");
			return false;
		}
	}
	
	/**
	 * Metodo statico privato che effettua la pull su tutti gli EJB di Alten, per sincronizzare il repository locale dell'EJB con il corrispondente repository remoto
	 * @param mapEjb: hashmap contenente tutti gli EJB di Alten suddivisi per blocchi di compilazione
	 * @param percorso: percorso della cartella contenente gli EJB
	 */
	private static void pullTuttiEjb(Map<String, List<String>> mapEjb, String percorso)
	{
		List<String> listaEjb = convertiMapEjbInLista(mapEjb);
		for(String ejb : listaEjb)
			pullEjb(percorso +"\\"+ ejb);
		System.out.println();
	}
	
	/**
	 * Metodo statico privato che effettua la pull su tutti gli EJB che in precedenza non sono passati sul branch desiderato con il checkout
	 * @param ejbNonSwitchati: lista degli EJB per cui il checkout non è avvenuto correttamente
	 * @param percorso: percorso della cartella che contiene gli EJB
	 */
	private static void pullEjbNonSwitchati(List<String> ejbNonSwitchati, String percorso)
	{
		for(String ejb : ejbNonSwitchati)
			pullEjb(percorso +"\\"+ ejb);
		System.out.println();
	}
	
	/**
	 * Metodo statico privato che effettua la pull sul singolo EJB passato come argomento
	 * @param percorso: percorso dell'EJB su cui effettuare la pull
	 */
	private static void pullEjb(String percorso)
	{
		System.out.print("-- EJB: "+ StringUtils.rightPad(percorso, 60, " ") +" - "+ StringConstants.COMANDO_GIT_PULL);
		try
		{
			gitPull(StringConstants.COMANDO_GIT_PULL, percorso);
			System.out.println(" --> OK");
		}
		catch(IOException ex)
		{
			System.out.println(" --> ERROR");
			System.out.println("Errore durante la pull dell'EJB '"+ percorso +"'");
			ex.printStackTrace();
		}
	}
	
	/**
	 * Metodo statico privato che effettua il merge di tutti gli EJB del blocco indicato dal branch passato come argomento
	 * @param mapEjb: hashmap contenente gli EJB di Alten suddivisi in blocchi di compilazione
	 * @param nomeBloccoEjb: blocco di EJB su cui effettuare il merge
	 * @param percorso: percorso della cartella contenente gli EJB
	 * @param nomeBranch: nome del branch da cui effettuare il merge
	 * @return true se durante il merge si sono verificati dei conflitti da risolvere su almeno uno degli EJB del blocco, false altrimenti
	 */
	private static boolean pullOriginEjbBlocco(Map<String, List<String>> mapEjb, String nomeBloccoEjb, String percorso, String nomeBranch)
	{
		List<String> listaEjb = mapEjb.get(nomeBloccoEjb);
		boolean flagConflitti = false;
		for(String ejb : listaEjb)
			flagConflitti = flagConflitti | pullOriginEjb(percorso +"\\"+ ejb, nomeBranch); //Se almeno un EJB presenta dei conflitti, il valore di flagConflitti passerà a true
		System.out.println();
		
		return flagConflitti;
	}
	
	/**
	 * Metodo statico privato che effettua il merge di un singolo EJB dal branch indicato
	 * @param percorso: percorso dell'EJB da allineare
	 * @param nomeBranchOrigine: nome del branch da cui effettuare il merge
	 * @return true se durante il merge si sono verificati dei conflitti da risolvere, false altrimenti
	 */
	private static boolean pullOriginEjb(String percorso, String nomeBranchOrigine)
	{
		try
		{
			boolean flagConflitti = gitPullOrigin(StringConstants.COMANDO_GIT_PULL_ORIGIN + nomeBranchOrigine, percorso);
			if(!flagConflitti)
				System.out.println("-- EJB: "+ StringUtils.rightPad(percorso, 60, " ") +" - "+ StringConstants.COMANDO_GIT_PULL_ORIGIN + nomeBranchOrigine +" --> OK");
			else
				System.out.println("-- EJB: "+ StringUtils.rightPad(percorso, 60, " ") +" - "+ StringConstants.COMANDO_GIT_PULL_ORIGIN + nomeBranchOrigine +" --> CONFLITTI");
			
			return flagConflitti;
		}
		catch(IOException ex)
		{
			System.out.println("Errore durante il checkout dell'EJB '"+ percorso +"' sul branch '"+ nomeBranchOrigine +"'");
			ex.printStackTrace();
			System.out.println("--------------------");
			return false;
		}
	}
	
	/**
	 * Metodo statico privato che permette di risolvere eventuali conflitti, emersi durante il merge, degli EJB di un blocco, mediante un commit
	 * @param bloccoEjb: nome del blocco di EJB
	 * @param percorso: percorso della cartella contenente gli EJB di Alten
	 */
	private static void commitConflittiEjbBlocco(List<String> bloccoEjb, String percorso)
	{
		for(String nomeEjb : bloccoEjb)
			commitConflittiEjb(percorso +"\\"+ nomeEjb);
	}
	
	/**
	 * Metodo statico privato che permette di risolvere eventuali conflitti su un EJB, emersi durante un merge, mediante un commit
	 * @param percorso: percorso dell'EJB per cui vanno risolti eventuali conflitti
	 */
	private static void commitConflittiEjb(String percorso)
	{
		try
		{
			boolean flagCommit = gitCommitConflitto(StringConstants.COMANDO_GIT_COMMIT, percorso);
			if(flagCommit)
				System.out.println("-- EJB: "+ StringUtils.rightPad(percorso, 60, " ") +" - "+ StringConstants.COMANDO_GIT_COMMIT +" --> CONFLITTI RISOLTI");
			else
				System.out.println("-- EJB: "+ StringUtils.rightPad(percorso, 60, " ") +" - "+ StringConstants.COMANDO_GIT_COMMIT +" --> NESSUN CONFLITTO DA RISOLVERE");
		}
		catch(IOException ex)
		{
			System.out.println("Errore durante il commit per i conflitti dell'EJB '"+ percorso +"'");
			ex.printStackTrace();
			System.out.println("--------------------");
		}
	}
	
	/**
	 * Metodo statico privato che effettua il merge di tutti gli EJB del blocco indicato dal branch master
	 * @param mapEjb: hashmap contenente gli EJB suddivisi nei blocchi di compilazione
	 * @param nomeBloccoEjb: nome del blocco su cui effettuare il merge dal branch master
	 * @param percorso: percorso della cartella contenente gli EJB
	 * @return true se sono presenti dei conflitti da risolvere emersi durante il merge, false se non ci sono conflitti
	 */
	private static boolean pullOriginMasterEjbBlocco(Map<String, List<String>> mapEjb, String nomeBloccoEjb, String percorso)
	{
		List<String> listaEjbBlocco = mapEjb.get(nomeBloccoEjb);
		boolean flagConflitti = false;
		for(String ejb : listaEjbBlocco)
			flagConflitti = flagConflitti | pullOriginMasterEjb(percorso +"\\"+ ejb);
		System.out.println();
		
		return flagConflitti;
	}
	
	/**
	 * Metodo statico privato che permette di effettuare il merge del singolo EJB dal branch master
	 * @param percorso: percorso della cartella dell'EJB da allineare al branch master
	 * @return true se sono presenti dei conflitti da risolvere emersi durante il merge, false se non ci sono conflitti
	 */
	private static boolean pullOriginMasterEjb(String percorso)
	{
		try
		{
			boolean flagConflitti = gitPullOrigin(StringConstants.COMANDO_GIT_PULL_ORIGIN + StringConstants.BRANCH_MASTER, percorso);
			if(!flagConflitti)
				System.out.println("-- EJB: "+ StringUtils.rightPad(percorso, 60, " ") +" - "+ StringConstants.COMANDO_GIT_PULL_ORIGIN + StringConstants.BRANCH_MASTER +" --> OK");
			else
				System.out.println("-- EJB: "+ StringUtils.rightPad(percorso, 60, " ") +" - "+ StringConstants.COMANDO_GIT_PULL_ORIGIN + StringConstants.BRANCH_MASTER +" --> CONFLITTI");
			
			return flagConflitti;
		}
		catch(IOException ex)
		{
			System.out.println("Errore durante il merge dell'EJB '"+ percorso +"' dal branch '"+ StringConstants.BRANCH_MASTER +"'");
			ex.printStackTrace();
			System.out.println("--------------------");
			return false;
		}
	}
	
	/**
	 * Metodo statico privato che permette di avviare la procedura di gestione dei conflitti emersi durante un merge da un altro branch
	 * @param mapEjb: hashmap che contiene gli EJB di Alten suddivisi per blocchi di compilazione
	 * @param nomeBloccoEjb: nome del blocco EJB
	 * @param percorso: percorso della cartella contenente gli EJB
	 */
	private static void proceduraGestioneConflitti(Map<String, List<String>> mapEjb, String nomeBloccoEjb, String percorso)
	{
		List<String> bloccoEjb = mapEjb.get(nomeBloccoEjb);
		
		System.out.println("--- Ci sono conflitti da risolvere");
		System.out.println("    1) Risolvere su IntelliJ i conflitti segnalati");
		System.out.println("    2) Non effettuare il commit per risolvere i conflitti, ci pensa il software");
		System.out.println("    3) Digitare S per far continuare il programma");
		System.out.print(">>> Comando: ");
		inputScelta();
		System.out.println();
		
		System.out.println("--- Commit per risolvere i conflitti su tutti gli EJB del blocco "+ nomeBloccoEjb +"\n");
		commitConflittiEjbBlocco(bloccoEjb, percorso);
		System.out.println();
		System.out.println("--- Conflitti sugli EJB del blocco "+ nomeBloccoEjb +" risolti\n");
	}
	
	/**
	 * Metodo statico privato che permette all'utente di verificare la correttezza delle versioni indicate nei POM padri degli EJB del blocco indicato
	 * @param nomeBloccoEjb: nome del blocco EJB che si sta allineando
	 * @deprecated
	 */
	private static void confermaVersioniPomEjbBlocco(String nomeBloccoEjb)
	{
		String scelta;
		System.out.println("--- Verifica versioni POM del blocco "+ nomeBloccoEjb);
		System.out.println("    1) Verificare le versioni nei POM degli EJB del blocco "+ nomeBloccoEjb);
		System.out.println("    2) Se necessario, aggiornare nei POM le versioni necessarie e committare da IntelliJ");
		System.out.println("    3) S: continuare - N: terminare il programma");
		System.out.print(">>> Comando: ");
		scelta = inputScelta();
		System.out.println();
		if("N".equalsIgnoreCase(scelta))
		{
			System.out.println("TERMINAZIONE PROGRAMMA");
			System.exit(0);
		}
	}
	
	/**
	 * Metodo statico privato che verifica se negli EJB di un determinato blocco sono presenti modifiche non ancora committate
	 * @param mapEjb: hashmap contenente gli EJB di Alten suddivisi per blocchi di compilazione
	 * @param nomeBloccoEjb: nome del blocco degli EJB
	 * @param percorso: percorso della cartella che contiene gli EJB di Alten
	 * @return true se tutti gli EJB del blocco non presentano aluna modifica da committare, false se almeno uno presenta modifiche non ancora committate
	 */
	private static boolean statusEjbBlocco(Map<String, List<String>> mapEjb, String nomeBloccoEjb, String percorso)
	{
		List<String> bloccoEjb = mapEjb.get(nomeBloccoEjb);
		boolean tuttoCommittato = true;
		for(String ejb : bloccoEjb)
			tuttoCommittato = tuttoCommittato & statusEjb(percorso +"\\"+ ejb);
		System.out.println();
		
		return tuttoCommittato;
	}
	
	/**
	 * Metodo statico privato che verifica se nel singolo EJB sono presenti modifiche non ancora committate
	 * @param percorso: percorso della cartella dell'EJB
	 * @return true se l'EJB non presenta alcuna modifica da committare, false altrimenti
	 */
	private static boolean statusEjb(String percorso)
	{
		try
		{
			boolean flagCommit = gitStatus(StringConstants.COMANDO_GIT_STATUS, percorso);
			if(flagCommit)
				System.out.println("-- EJB: "+ StringUtils.rightPad(percorso, 60, " ") +" - "+ StringConstants.COMANDO_GIT_STATUS +" --> NESSUNA MODIFICA DA COMMITTARE");
			else
				System.out.println("-- EJB: "+ StringUtils.rightPad(percorso, 60, " ") +" - "+ StringConstants.COMANDO_GIT_STATUS +" --> CI SONO MODIFICHE DA COMMITTARE");
			
			return flagCommit;
		}
		catch(IOException ex)
		{
			System.out.println("Errore durante la verifica dello status dell'EJB '"+ percorso +"'");
			ex.printStackTrace();
			System.out.println("--------------------");
			return false;
		}
	}
	
	/**
	 * Metodo statico privato che permette all'utente di verificare la presenza di modifiche non committate per gli EJB di un determinato blocco
	 * @param bloccoEjb: nome del blocco EJB da verificare
	 */
	private static void verificaModificheNonCommittate(String bloccoEjb)
	{
		String scelta;
		do
		{
			System.out.println("--- Verifica status EJB del blocco "+ bloccoEjb);
			System.out.println("    1) Verificare su IntelliJ la presenza di modifiche non committate negli EJB del blocco");
			System.out.println("    2) Procedere su IntelliJ con il commit delle modifiche o con il revert, a scelta");
			System.out.println("    3) Digitare S per continuare con il programma");
			System.out.print(">>> Comando: ");
			scelta = inputScelta();
			System.out.println();
		} while(!"S".equalsIgnoreCase(scelta));
		
		System.out.println();
	}
	
	/**
	 * Metodo statico privato che permette di effettuare un commit vuoto per forzare la ricompilazione degli EJB del blocco indicato
	 * @param mapEjb: hashmap che contiene gli EJB di Alten suddivisi per blocchi di compilazione
	 * @param nomeBloccoEjb: nome del blocco di EJB su cui effettuare i commit
	 * @param nomeBranch: nome del branch su cui si sta operando
	 * @param percorso: percorso della cartella che contiene gli EJB
	 */
	private static void commitVuotoEjbBlocco(Map<String, List<String>> mapEjb, String nomeBloccoEjb, String nomeBranch, String percorso)
	{
		List<String> bloccoEjb = mapEjb.get(nomeBloccoEjb);
		for(String ejb : bloccoEjb)
			commitVuotoEjb(percorso +"\\"+ ejb, nomeBranch);
		System.out.println();
	}
	
	/**
	 * Metodo statico privato che permette di effettuare un commit vuoto per forzare la ricompilazione dell'EJB indicato
	 * @param percorso: percorso della cartella dell'EJB da ricompilare
	 * @param nomeBranch: nome del branch su cui si sta operando
	 */
	private static void commitVuotoEjb(String percorso, String nomeBranch)
	{
		try
		{
			if(StringConstants.BRANCH_SVIL.equals(nomeBranch))
			{
				System.out.print("-- EJB: "+ StringUtils.rightPad(percorso, 60, " ") +" - "+ StringConstants.COMANDO_GIT_RELEASE);
				gitCommitVuoto(StringConstants.COMANDO_GIT_RELEASE, percorso);
			}
			else
			{
				System.out.print("-- EJB: "+ StringUtils.rightPad(percorso, 60, " ") +" - "+ StringConstants.COMANDO_GIT_COMMIT_EJB_VUOTO);
				gitCommitVuoto(StringConstants.COMANDO_GIT_COMMIT_EJB_VUOTO, percorso);
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
	 * Metodo static privato che permette di effettuare la push sugli EJB del blocco indicato, in modo da inviare i commit nuovi sul repository remoto
	 * @param mapEjb: hashmap che contiene gli EJB di Alten suddivisi per blocchi di compilazione
	 * @param nomeBloccoEjb: nome del blocco di EJB su cui va effettuata la push
	 * @param percorso: percorso della cartella contenente gli EJB
	 */
	private static void pushEjbBlocco(Map<String, List<String>> mapEjb, String nomeBloccoEjb, String percorso)
	{
		List<String> bloccoEjb = mapEjb.get(nomeBloccoEjb);
		for(String ejb : bloccoEjb)
			pushEjb(percorso +"\\"+ ejb);
		System.out.println();
	}
	
	/**
	 * Metodo static privato che permette di effettuare la push sull'EJB indicato, in modo da inviare i commit nuovi sul repository remoto
	 * @param percorso: percorso della cartella dell'EJB
	 */
	private static void pushEjb(String percorso)
	{
		try
		{
			boolean flagPush = gitPush(StringConstants.COMANDO_GIT_PUSH, percorso);
			if(flagPush)
				System.out.println("-- EJB: "+ StringUtils.rightPad(percorso, 60, " ") +" - "+ StringConstants.COMANDO_GIT_PUSH +" --> OK");
			else
				System.out.println("-- EJB: "+ StringUtils.rightPad(percorso, 60, " ") +" - "+ StringConstants.COMANDO_GIT_PUSH +" --> ERRORE");
		}
		catch(IOException ex)
		{
			System.out.println("Errore durante la push dei commit dell'EJB '"+ percorso +"'");
			ex.printStackTrace();
			System.out.println("--------------------");
		}
	}
	
	/**
	 * Metodo che permette all'utente di decidere se terminare l'allineamento o continuare con un altro blocco di EJB
	 * @param nomeBranch: nome del branch su cui si sta operando
	 * @return true se l'allineamento è terminato, false altrimenti
	 */
	private static boolean verificaTerminazioneAllineamento(String nomeBranch)
	{
		System.out.print(">>> Hai terminato l'allineamento degli EJB migrati del branch '"+ nomeBranch +"' (S/N)? ");
		String scelta = inputScelta();
		return "S".equalsIgnoreCase(scelta);
	}
	
	/**
	 * Metodo statico che permette di avviare la procedura per scaricare in locale tutti gli EJB di Alten
	 * @param mapEjb: hashmap che contiene i nomi degli EJB di Alten suddivisa per blocchi di compilazione
	 */
	static void eseguiCloneEjb(Map<String, List<String>> mapEjb)
	{
		List<String> listaEjb = convertiMapEjbInLista(mapEjb);
		String percorso = inputPercorsoCartellaDestinazioneEjb();
		
		cloneTuttiEjb(listaEjb, percorso);
		System.out.println("--- Download degli EJB terminato\n");
	}
	
	/**
	 * Metodo statico privato che permette all'utente di indicare il percorso della cartella che deve contenere gli EJB da scaricare
	 * @return il percorso della cartella indicato dall'utente
	 */
	private static String inputPercorsoCartellaDestinazioneEjb()
	{
		String percorso;
		do
		{
			Scanner scanner = new Scanner(System.in);
			System.out.println(">>> Inserisci il percorso della cartella in cui vuoi scaricare gli EJB (es. 'D:\\Openshift\\EJB') oppure inserisci una delle seguenti chiavi: ");
			StringConstants.PATH_EJB.forEach((k, v) -> System.out.println("   -- "+ k + " --> " + v));
			System.out.print(">>> Scelta: ");
			percorso = scanner.nextLine();
			percorso = StringConstants.PATH_EJB.containsKey(percorso.toLowerCase()) ? StringConstants.PATH_EJB.get(percorso.toLowerCase()) : percorso;
			System.out.println();
		} while(!verificaPercorsoCartella(percorso));
		
		return percorso;
	}
	
	/**
	 * Metodo statico privato che permette di scaricare tutti gli EJB di Alten
	 * @param listaEjb: lista di tutti gli EJB di Alten
	 * @param percorso: percorso della cartella di destinazione degli EJB da scaricare
	 */
	private static void cloneTuttiEjb(List<String> listaEjb, String percorso)
	{
		Collections.sort(listaEjb);
		for(String ejb : listaEjb)
		{
			if(Files.exists(Paths.get(percorso +"\\"+ ejb)))
				System.out.println("La cartella "+ percorso +"\\"+ ejb +" esiste gi\u00E0. Il download dell'EJB verr\u00E0 saltato");
			else
				cloneEjb(percorso, ejb);
		}
		System.out.println();
	}
	
	/**
	 * Metodo statico privato che permette di scaricare l'EJB specificato
	 * @param percorso: percorso della cartella di destinazione degli EJB da scaricare
	 * @param ejb: nome dell'EJB da scaricare
	 */
	private static void cloneEjb(String percorso, String ejb)
	{
		String urlEjb = StringConstants.URL_BITBUCKET + ejb +".git";
		String comando = StringConstants.COMANDO_GIT_CLONE + urlEjb;
		System.out.print("-- EJB: "+ StringUtils.rightPad(percorso, 60, " ") +" - "+ comando);
		try
		{
			gitClone(comando, percorso, ejb);
			System.out.println(" --> OK");
		}
		catch(IOException ex)
		{
			System.out.println(" --> ERROR");
			System.out.println("Errore durante la clone dell'EJB '"+ ejb +"'");
			ex.printStackTrace();
		}
	}
	
	/**
	 * Metodo statico privato che gestisce l'aggiornamento delle versioni delle dipendenze trovate nei POM, salvate all'interno dell'apposita hashmap
	 * @param mappaDipendenze: hashmap contenente le versioni delle dipendenze trovate nei POM degli EJB
	 * @param mapEjb: hashmap contenente i blocchi di compilazione degli EJB
	 * @param nomeBloccoEjb: nome del blocco di EJB in esame
	 * @param percorso: percorso della cartella contenente gli EJB
	 */
	private static void aggiornamentoVersioniDipendenzePOM(Map<String, String> mappaDipendenze, Map<String, List<String>> mapEjb, String nomeBloccoEjb, String percorso)
	{
		List<String> bloccoEjb = mapEjb.get(nomeBloccoEjb);
		for(String ejb : bloccoEjb)
		{
			File pom = new File(percorso +"\\"+ ejb +"\\pom.xml");
			try
			{
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document document = builder.parse(pom);
				
				System.out.println("--- Lettura del file "+ percorso +"\\"+ ejb +"\\pom.xml");
				
				aggiornamentoVersioneArchCodeNdce(document, mappaDipendenze.get("arch.core.ndce.version"));
				List<Node> listaDipendenzeEjb = ricercaDipendenzeEjb(document);
				aggiornamentoVersioniDipendenzeEjb(listaDipendenzeEjb, mappaDipendenze, ejb);
				aggiornamentoPOMEjb(document, pom);
			}
			catch(ParserConfigurationException | SAXException | IOException | TransformerException ex)
			{
				System.err.println("--- Errore nella sostituzione delle versioni nel POM "+ pom.getAbsolutePath());
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * Metodo statico privato che permette di aggiornare nel POM di un EJB la versione della libreria arch.core.ndce
	 * @param document: il POM parsificato dell'EJB in esame
	 * @param versione: versione di arch.core.ndce da sostituire nel POM
	 */
	private static void aggiornamentoVersioneArchCodeNdce(Document document, String versione)
	{
		NodeList archCoreList = document.getElementsByTagName("arch.core.ndce.version");
		if(archCoreList.getLength() > 0)
		{
			Node archCoreNode = archCoreList.item(0);
			System.out.print("--- "+ archCoreNode.getNodeName() +" [PRIMA] "+ archCoreNode.getTextContent());
			archCoreNode.setTextContent(versione);
			System.out.println(" --> [DOPO] "+ archCoreNode.getTextContent());
		}
	}
	
	/**
	 * Metodo statico privato che cerca all'interno di un file POM le dipendenze da altri EJB da aggiornare
	 * @param document: il POM parsificato dell'EJB da esaminare
	 * @return la lista di dipendenze da aggiornare trovate nel POM
	 */
	private static List<Node> ricercaDipendenzeEjb(Document document)
	{
		List<Node> listaDipendenzeEjb = new ArrayList<>();
		NodeList propertiesList = document.getElementsByTagName("properties");
		if(propertiesList.getLength() > 0)
		{
			Node propertiesNode = propertiesList.item(0);
			for(int i=0; i< propertiesNode.getChildNodes().getLength(); i++)
			{
				Node node = propertiesNode.getChildNodes().item(i);
				if(isTagValido(node))
					listaDipendenzeEjb.add(node);
			}
		}
		
		return listaDipendenzeEjb;
	}
	
	/**
	 * Metodo statico privato che verifica se un determinato tag XML rappresenti una dipendenza da un EJB che va aggiornata
	 * @param node: elemento XML del POM che rappresenta un tag figlio dell'elemento properties (in cui sono contenute le versioni delle dipendenze)
	 * @return true se l'elemento XML rappresenta una dipendenza da aggiornare, false altrimenti
	 */
	private static boolean isTagValido(Node node)
	{
		String nomeTag = node.getNodeName();
		return !"arch.core.ndce.version".equals(nomeTag) && !"junit.version".equals(nomeTag)
			&& !"notificator-model.version".equals(nomeTag) && !"notificator-common.version".equals(nomeTag) && !"xmlbeans.version".equals(nomeTag) && !"notificator-client-listener.version".equals(nomeTag)
			&& !"notificator-client-push.version".equals(nomeTag) && !"notificator-client-gateway.version".equals(nomeTag) && !"notificator-dispatcher.version".equals(nomeTag)
			&& !"ojdbc14.version".equals(nomeTag) && !"investimenti.version".equals(nomeTag) && !"jacoco.version".equals(nomeTag) && !"log4j.version".equals(nomeTag) && !"eclipselink.version".equals(nomeTag)
			&& !"j2ee.version".equals(nomeTag) && !"coherence.version".equals(nomeTag) && !"connector.version".equals(nomeTag) && !"opricorrenti.hazelcast.version".equals(nomeTag)
			&& !"notificator-domain-abc.version".equals(nomeTag) && !"jasperreports.version".equals(nomeTag) && !"barbecue.version".equals(nomeTag)
			&& nomeTag.contains(".version");
	}
	
	/**
	 * Metodo statico privato che aggiorna nel POM di un EJB le versioni delle sue dipendenze da altri EJB
	 * @param listaDipendenzeEjb: lista di tag XML che rappresentano dipendenze verso altri EJB
	 * @param mappaDipendenze: hashmap contenente le versioni delle dipendenze da aggiornare nei POM degli EJB
	 * @param ejb: nome dell'EJB in esame per cui vanno aggiornate le versioni delle dipendenze
	 */
	private static void aggiornamentoVersioniDipendenzeEjb(List<Node> listaDipendenzeEjb, Map<String, String> mappaDipendenze, String ejb)
	{
		for(Node node : listaDipendenzeEjb)
		{
			String versione = mappaDipendenze.get(node.getNodeName());
			if(versione == null) //Caso dipendenza non ancora inserita nell'hashmap
			{
				//Gestione dipendenze circolari che non vanno salvate nell'hashmap
				if(isDipendenzaCircolare(ejb, node))
				{
					System.out.print(">>> "+ node.getNodeName() +" non presente (dipendenza circolare). Indicare la versione da salvare: ");
					versione = inputScelta();
					
					System.out.print("--- "+ node.getNodeName() +" [PRIMA - Dipendenza circolare] "+ node.getTextContent());
					node.setTextContent(versione);
					System.out.println(" - [DOPO - Dipendenza circolare] "+ node.getTextContent());
				}
				else //Gestione dipendenze non circolari da salvare nell'hashmap
				{
					System.out.print(">>> "+ node.getNodeName() +" non presente. Indicare la versione da salvare: ");
					versione = inputScelta();
					
					mappaDipendenze.put(node.getNodeName(), versione);
					System.out.print("--- "+ node.getNodeName() +" [PRIMA] "+ node.getTextContent());
					node.setTextContent(versione);
					System.out.println(" --> [DOPO] "+ node.getTextContent());
				}
			}
			else //Caso dipendenza già inserita nell'hashmap
			{
				System.out.print("--- "+ node.getNodeName() +" [PRIMA] "+ node.getTextContent());
				node.setTextContent(versione);
				System.out.println(" --> [DOPO] "+ node.getTextContent());
			}
		}
	}
	
	/**
	 * Metodo statico privato che verifica se, per l'EJB specificato, un determinato tag di una dipendenza rappresenti una dipendenza circolare
	 * @param ejb: nome dell'EJB in esame
	 * @param node: elemento XML che rappresenta una dipendenza da un altro EJB
	 * @return true se la dipendenza verificata è circolare, false altrimenti
	 */
	private static boolean isDipendenzaCircolare(String ejb, Node node)
	{
		return (ejb.equalsIgnoreCase("filialevirtuale-ejb") && node.getNodeName().equals("miogestore.version"))
			|| (ejb.equalsIgnoreCase("commonssmallbusiness-ejb") && node.getNodeName().equals("pagamentiribamultipli.version"))
			|| (ejb.equalsIgnoreCase("pfm-ejb") && node.getNodeName().equals("globalposition.version"))
			|| (ejb.equalsIgnoreCase("pfm-ejb") && node.getNodeName().equals("salvadanaio.version"))
			|| (ejb.equalsIgnoreCase("comunicazioni-ejb") && node.getNodeName().equals("miogestore.version"));
	}
	
	/**
	 * Metodo statico privato che aggiorna il contenuto di un file POM in seguito all'aggiornamento delle dipendenze
	 * @param document: parsificazione XML del file POM con le versioni aggiornate
	 * @param pom: file POM dell'EJB che andrà aggiornato in base al contenuto dell'oggetto document
	 * @throws TransformerException qualora si verifichi un errore nella scrittura del file POM aggiornato a partire dal document
	 */
	private static void aggiornamentoPOMEjb(Document document, File pom) throws TransformerException
	{
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty("omit-xml-declaration", "yes");
		DOMSource source = new DOMSource(document);
		StreamResult result = new StreamResult(pom);
		
		transformer.transform(source, result);
		System.out.println("--- Scrittura completata");
		System.out.println();
	}
	
	/**
	 * Metodo statico privato che permette di aggiornare l'hashmap delle versioni delle dipendenze aggiungendoci quelle degli EJB del blocco appena ricompilato
	 * @param mapEjb: hashmap con la suddivisione in blocchi di compilazione degli EJB
	 * @param mappaDipendenze: hashmap contenente la lista delle versioni associate al relativo EJB o libreria esterna
	 * @param nomeBloccoEjb: nome del blocco di EJB in esame
	 */
	private static void aggiornamentoFileVersioniPOM(Map<String, List<String>> mapEjb, Map<String, String> mappaDipendenze, String nomeBloccoEjb)
	{
		List<String> nomiEjbBlocco = new ArrayList<>();
		for(String ejb : mapEjb.get(nomeBloccoEjb))
		{
			String ejbPulito = ejb.equals("isp-ib-om") ? "isp-ib-om" : ejb.substring(0, ejb.length()-4);
			
			if(ejbPulito.equals("jiffyv2"))
				ejbPulito = "jiffy-v2";
			else if(ejbPulito.equals("pagamentobollette"))
				ejbPulito = "pagamentibollette";
			else if(ejbPulito.equals("ristampapin"))
				ejbPulito = "ristampa-pin";
			
			nomiEjbBlocco.add(ejbPulito);
		}
		
		for(String ejb : nomiEjbBlocco) {
			String chiaveEjb = ejb +".version";
			System.out.print(">>> Inserire la versione di "+ ejb +"-ejb appena ricompilata: ");
			String versione = inputScelta();
			mappaDipendenze.put(chiaveEjb, versione);
		}
		System.out.println();
		
		scritturaFileVersioniPOM(mappaDipendenze);
	}
	
	/**
	 * Metodo statico privato che riscrive il file VersioniPOM.txt per aggiornarlo con le ultime versioni inserite nell'hashmap
	 * @param mappaDipendenze: hashmap contenente gli EJB e librerie esterne e le relative versioni associate
	 */
	private static void scritturaFileVersioniPOM(Map<String, String> mappaDipendenze)
	{
		System.out.print("Aggiornamento del file VersioniPOM.txt ... ");
		try
		{
			File file = new File("VersioniPOM.txt");

			if(file == null)
				throw new IOException("File "+ file.getAbsolutePath() +" non trovato");

			BufferedWriter bw = new BufferedWriter(new FileWriter(file, false));

			for(String tag : mappaDipendenze.keySet())
			{
				String versione = mappaDipendenze.get(tag);
				String riga = "<"+ tag +">"+ versione +"</"+ tag +">";
				bw.write(riga +"\n");
				bw.flush();
			}

			bw.close();
			System.out.println("COMPLETATO");
		}
		catch(IOException ex)
		{
			System.out.println("\n--- Aggiornamento VersioniPOM.txt interrotto a causa di un errore ---");
			ex.printStackTrace();
		}
		System.out.println();
	}
}