package allineamenti;

import static allineamenti.GitCommands.gitCheckout;
import static allineamenti.GitCommands.gitCommitConflitto;
import static allineamenti.GitCommands.gitCommitVuoto;
import static allineamenti.GitCommands.gitPull;
import static allineamenti.GitCommands.gitPullOrigin;
import static allineamenti.GitCommands.gitPush;
import static allineamenti.GitCommands.gitStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;

public class FunzioniEjb
{
	static void eseguiAllineamentoEjb(Map<String, List<String>> mapEjb, String comando)
	{
		String[] partiComando = comando.split(" ");
		String nomeBranch = partiComando[1];
		String branchOrigine = null;
		if(partiComando.length == 3)
			branchOrigine = partiComando[2];
		
		System.out.println("--- Allineamento EJB migrati - Branch: "+ nomeBranch +" ---\n");
		
		String percorso = inputPercorsoCartellaEjb();
		
		proceduraCheckoutTuttiEjb(mapEjb, nomeBranch, percorso);
		
		System.out.println("--- Pull di tutti gli EJB migrati\n");
		pullTuttiEjb(mapEjb, percorso);
		
		String nomeBloccoEjb;
		boolean allineamentoEjbTerminato;
		do
		{
			do {
				System.out.print(">>> Quale blocco di EJB intendi compilare (es. B1, B2...)? ");
				nomeBloccoEjb = StringUtils.upperCase(inputScelta());
				if (!mapEjb.containsKey(nomeBloccoEjb))
					System.out.println("Il blocco selezionato non esiste. Riprovare\n");
			}	while((!mapEjb.containsKey(nomeBloccoEjb)));
			System.out.println();
			
			boolean flagConflitti;
			
			if(!nomeBranch.equalsIgnoreCase(StringConstants.BRANCH_SVIL))
			{
				if(branchOrigine == null)
				{
					System.out.println("--- Merge di tutti gli EJB migrati del blocco "+ nomeBloccoEjb +" dal branch '"+ StringConstants.BRANCH_SVIL +"'\n");
					flagConflitti = pullOriginEjbBlocco(mapEjb, nomeBloccoEjb, percorso, StringConstants.BRANCH_SVIL);
				}
				else
				{
					System.out.println("--- Merge di tutti gli EJB migrati del blocco "+ nomeBloccoEjb +" dal branch '"+ branchOrigine +"'\n");
					flagConflitti = pullOriginEjbBlocco(mapEjb, nomeBloccoEjb, percorso, branchOrigine);
				}
				if(flagConflitti)
					proceduraGestioneConflitti(mapEjb, nomeBloccoEjb, percorso);
				confermaVersioniPomEjbBlocco(nomeBloccoEjb);
			}
			
			System.out.println("--- Merge di tutti gli EJB migrati dal branch master\n");
			flagConflitti = pullOriginMasterEjbBlocco(mapEjb, nomeBloccoEjb, percorso);
			if(flagConflitti)
				proceduraGestioneConflitti(mapEjb, nomeBloccoEjb, percorso);
			confermaVersioniPomEjbBlocco(nomeBloccoEjb);
			
			boolean tuttoCommittatoEjbBlocco = statusEjbBlocco(mapEjb, nomeBloccoEjb, percorso);
			if(tuttoCommittatoEjbBlocco)
				System.out.println("--- Gli EJB del blocco "+ nomeBloccoEjb +" non presentano modifiche non committate");
			else
				verificaModificheNonCommittate(nomeBloccoEjb);
			
			commitVuotoEjbBlocco(mapEjb, nomeBloccoEjb, nomeBranch, percorso);
			pushEjbBlocco(mapEjb, nomeBloccoEjb, percorso);
			
			System.out.println("--- Allineamento degli EJB migrati del blocco "+ nomeBloccoEjb +" completato\n");
			allineamentoEjbTerminato = verificaTerminazioneAllineamento(nomeBranch);
		} while(!mapEjb.containsKey(nomeBloccoEjb) || !allineamentoEjbTerminato);
	}
	
	private static String inputScelta()
	{
		Scanner scanner = new Scanner(System.in);
		return scanner.nextLine();
	}
	
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
	}
	
	
	private static List<String> checkoutTuttiEjb(Map<String, List<String>> mapEjb, String nomeBranch, String percorso)
	{
		List<String> listaEjb = convertiMapEjbInLista(mapEjb);
		List<String> listaEjbNonSwitchati = new ArrayList<>();
		
		for(String ejb : listaEjb)
		{
			boolean checkoutAvvenuto = checkoutEjb(percorso + "\\" + ejb, nomeBranch);
			if (!checkoutAvvenuto)
				listaEjbNonSwitchati.add(ejb);
		}
		
		return listaEjbNonSwitchati;
	}
	
	private static List<String> checkoutEjbNonSwitchati(List<String> ejbNonSwitchati, String nomeBranch, String percorso)
	{
		for(String ejb : ejbNonSwitchati)
		{
			boolean checkoutAvvenuto = checkoutEjb(percorso +"\\"+ ejb, nomeBranch);
			if (checkoutAvvenuto)
				ejbNonSwitchati.remove(ejb);
		}
		
		return ejbNonSwitchati;
	}
	
	private static List<String> convertiMapEjbInLista(Map<String, List<String>> mapEjb)
	{
		List<String> listaEjb = new ArrayList<>();
		Collection<List<String>> collectionEjb = mapEjb.values();
		
		for(List<String> blocco : collectionEjb)
			listaEjb.addAll(blocco);
		
		Collections.sort(listaEjb);
		
		return listaEjb;
	}
	
	private static boolean checkoutEjb(String percorso, String nomeBranch)
	{
		try
		{
			boolean checkoutAvvenuto = gitCheckout(StringConstants.COMANDO_GIT_CHECKOUT + nomeBranch, percorso);
			if(checkoutAvvenuto)
				System.out.println("-- EJB: "+ percorso +" - "+ StringConstants.COMANDO_GIT_CHECKOUT + nomeBranch +" --> OK");
			else
				System.out.println("-- EJB: "+ percorso +" - "+ StringConstants.COMANDO_GIT_CHECKOUT + nomeBranch +" --> ERRORE");
			
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
	
	private static void pullTuttiEjb(Map<String, List<String>> mapEjb, String percorso)
	{
		List<String> listaEjb = convertiMapEjbInLista(mapEjb);
		for(String ejb : listaEjb)
			pullEjb(percorso +"\\"+ ejb);
		System.out.println();
	}
	
	private static void pullEjbNonSwitchati(List<String> ejbNonSwitchati, String percorso)
	{
		for(String ejb : ejbNonSwitchati)
			pullEjb(percorso +"\\"+ ejb);
		System.out.println();
	}
	
	private static void pullEjb(String percorso)
	{
		System.out.print("-- EJB: "+ percorso +" - "+ StringConstants.COMANDO_GIT_PULL);
		try
		{
			gitPull(StringConstants.COMANDO_GIT_PULL, percorso);
			System.out.println(" --> OK");
		}
		catch (IOException ex)
		{
			System.out.println(" --> ERROR");
			System.out.println("Errore durante la pull dell'EJB '"+ percorso +"'");
			ex.printStackTrace();
		}
	}
	
	private static boolean pullOriginEjbBlocco(Map<String, List<String>> mapEjb, String nomeBloccoEjb, String percorso, String nomeBranch)
	{
		List<String> listaEjb = mapEjb.get(nomeBloccoEjb);
		boolean flagConflitti = false;
		for(String ejb : listaEjb)
			flagConflitti = flagConflitti | pullOriginEjb(percorso +"\\"+ ejb, nomeBranch); //Se almeno un EJB presenta dei conflitti, il valore di flagConflitti passerà a true
		System.out.println();
		
		return flagConflitti;
	}
	
	private static boolean pullOriginEjb(String percorso, String nomeBranchOrigine)
	{
		try
		{
			boolean flagConflitti = gitPullOrigin(StringConstants.COMANDO_GIT_PULL_ORIGIN + nomeBranchOrigine, percorso);
			if(!flagConflitti)
				System.out.println("-- EJB: "+ percorso +" - "+ StringConstants.COMANDO_GIT_PULL_ORIGIN + nomeBranchOrigine +" --> OK");
			else
				System.out.println("-- EJB: "+ percorso +" - "+ StringConstants.COMANDO_GIT_PULL_ORIGIN + nomeBranchOrigine +" --> CONFLITTI");
			
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
	
	private static void commitConflittiEjbBlocco(List<String> bloccoEjb, String percorso)
	{
		for(String nomeEjb : bloccoEjb)
			commitConflittiEjb(percorso +"\\"+ nomeEjb);
	}
	
	private static void commitConflittiEjb(String percorso)
	{
		try
		{
			boolean flagCommit = gitCommitConflitto(StringConstants.COMANDO_GIT_COMMIT, percorso);
			if(flagCommit)
				System.out.println("-- EJB: "+ percorso +" - "+ StringConstants.COMANDO_GIT_COMMIT +" --> CONFLITTI RISOLTI");
			else
				System.out.println("-- EJB: "+ percorso +" - "+ StringConstants.COMANDO_GIT_COMMIT +" --> NESSUN CONFLITTO DA RISOLVERE");
		}
		catch (IOException ex)
		{
			System.out.println("Errore durante il commit per i conflitti dell'EJB '"+ percorso +"'");
			ex.printStackTrace();
			System.out.println("--------------------");
		}
	}
	
	private static boolean pullOriginMasterEjbBlocco(Map<String, List<String>> mapEjb, String nomeBloccoEjb, String percorso)
	{
		List<String> listaEjbBlocco = mapEjb.get(nomeBloccoEjb);
		boolean flagConflitti = false;
		for(String ejb : listaEjbBlocco)
			flagConflitti = flagConflitti | pullOriginMasterEjb(percorso +"\\"+ ejb);
		System.out.println();
		
		return flagConflitti;
	}
	
	private static boolean pullOriginMasterEjb(String percorso)
	{
		try
		{
			boolean flagConflitti = gitPullOrigin(StringConstants.COMANDO_GIT_PULL_ORIGIN + StringConstants.BRANCH_MASTER, percorso);
			if(!flagConflitti)
				System.out.println("-- EJB: "+ percorso +" - "+ StringConstants.COMANDO_GIT_PULL_ORIGIN + StringConstants.BRANCH_MASTER +" --> OK");
			else
				System.out.println("-- EJB: "+ percorso +" - "+ StringConstants.COMANDO_GIT_PULL_ORIGIN + StringConstants.BRANCH_MASTER +" --> CONFLITTI");
			
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
	
	private static boolean statusEjbBlocco(Map<String, List<String>> mapEjb, String nomeBloccoEjb, String percorso)
	{
		List<String> bloccoEjb = mapEjb.get(nomeBloccoEjb);
		boolean tuttoCommittato = false;
		for(String ejb : bloccoEjb)
			tuttoCommittato = tuttoCommittato | statusEjb(percorso +"\\"+ ejb);
		System.out.println();
		
		return tuttoCommittato;
	}
	
	private static boolean statusEjb(String percorso)
	{
		try
		{
			boolean flagCommit = gitStatus(StringConstants.COMANDO_GIT_STATUS, percorso);
			if(flagCommit)
				System.out.println("-- EJB: "+ percorso +" - "+ StringConstants.COMANDO_GIT_STATUS +" --> NESSUNA MODIFICA DA COMMITTARE");
			else
				System.out.println("-- EJB: "+ percorso +" - "+ StringConstants.COMANDO_GIT_STATUS +" --> CI SONO MODIFICHE DA COMMITTARE");
			
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
	
	private static void commitVuotoEjbBlocco(Map<String, List<String>> mapEjb, String nomeBloccoEjb, String nomeBranch, String percorso)
	{
		List<String> bloccoEjb = mapEjb.get(nomeBloccoEjb);
		for(String ejb : bloccoEjb)
			commitVuotoEjb(percorso +"\\"+ ejb, nomeBranch);
		System.out.println();
	}
	
	private static void commitVuotoEjb(String percorso, String nomeBranch)
	{
		try
		{
			if(StringConstants.BRANCH_SVIL.equals(nomeBranch))
			{
				System.out.print("-- EJB: "+ percorso +" - "+ StringConstants.COMANDO_GIT_RELEASE);
				gitCommitVuoto(StringConstants.COMANDO_GIT_RELEASE, percorso);
			}
			else
			{
				System.out.print("-- EJB: "+ percorso +" - "+ StringConstants.COMANDO_GIT_COMMIT_EJB_VUOTO);
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
	
	private static void pushEjbBlocco(Map<String, List<String>> mapEjb, String nomeBloccoEjb, String percorso)
	{
		List<String> bloccoEjb = mapEjb.get(nomeBloccoEjb);
		for(String ejb : bloccoEjb)
			pushEjb(percorso +"\\"+ ejb);
		System.out.println();
	}
	
	private static void pushEjb(String percorso)
	{
		try
		{
			boolean flagPush = gitPush(StringConstants.COMANDO_GIT_PUSH, percorso);
			if(flagPush)
				System.out.println("-- EJB: "+ percorso +" - "+ StringConstants.COMANDO_GIT_PUSH +" --> OK");
			else
				System.out.println("-- EJB: "+ percorso +" - "+ StringConstants.COMANDO_GIT_PUSH +" --> ERRORE");
		}
		catch(IOException ex)
		{
			System.out.println("Errore durante la push dei commit dell'EJB '"+ percorso +"'");
			ex.printStackTrace();
			System.out.println("--------------------");
		}
	}
	
	private static boolean verificaTerminazioneAllineamento(String nomeBranch)
	{
		System.out.print(">>> Hai terminato l'allineamento degli EJB migrati del branch '"+ nomeBranch +"' (S/N)? ");
		String scelta = inputScelta();
		return "S".equalsIgnoreCase(scelta);
	}
}
