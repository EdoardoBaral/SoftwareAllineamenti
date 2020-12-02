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

public class FunzioniVerticali
{
	public static void eseguiAllineamentoVerticali(List<String> listaVerticali, String comando)
	{
		String nomeBranch = comando.substring(10);
		System.out.println("--- Allineamento verticali - Branch: "+ nomeBranch +" ---\n");

		String percorso = inputPercorsoCartellaVerticali();

		proceduraCheckoutTuttiVerticali(listaVerticali, nomeBranch, percorso);
		System.out.println();

		System.out.println("--- Pull di tutti i verticali\n");
		pullTuttiVerticali(listaVerticali, percorso);
		
		boolean flagConflitti;
		
		if(!StringConstants.BRANCH_SVIL.equalsIgnoreCase(nomeBranch))
		{
			System.out.println("--- Merge di tutti i verticali dal branch '"+ StringConstants.BRANCH_SVIL+"'\n");
			flagConflitti = pullOriginVerticali(listaVerticali, percorso);
			if(flagConflitti)
				proceduraGestioneConflitti(listaVerticali, percorso);
		}

		System.out.println("--- Merge di tutti i verticali dal branch master\n");
		flagConflitti = pullOriginMasterVerticali(listaVerticali, percorso);
		if(flagConflitti)
			proceduraGestioneConflitti(listaVerticali, percorso);
		
		proceduraSostituzioneVersioniPom(percorso);

		boolean verticaliTuttiCommittati = statusVerticali(listaVerticali, percorso);
		if(verticaliTuttiCommittati)
			System.out.println("I verticali sono tutti allineati e non presentano modifiche non committate");
		else
			verificaModificheNonCommittate();
		
		commitVuotoVerticali(listaVerticali, nomeBranch, percorso);

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
				else
					flagIntervalloValido = true;
			} while(!flagIntervalloValido);

			pushVerticali(listaVerticali, v1, v2, percorso);

			System.out.print(">>> Vuoi eseguire la push su un altro intervallo di verticali (S/N)? ");
			String scelta = inputScelta();
			System.out.println();

			if("S".equalsIgnoreCase(scelta))
				checkTerminazionePush = false;
			else
				checkTerminazionePush = true;
		} while(!checkTerminazionePush);
		
		System.out.println("--- Simulazione allineamento verticali ---\n");
	}
	
	public static String inputScelta()
	{
		Scanner scanner = new Scanner(System.in);
		String comando = scanner.nextLine();
		
		return comando;
	}
	
	public static String inputPercorsoCartellaVerticali()
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
	
	public static boolean verificaPercorsoCartella(String percorso)
	{
		if(Files.exists(Paths.get(percorso)))
			return true;
		else
		{
			System.out.println("Percorso non trovato. Riprovare\n");
			return false;
		}
	}
	
	public static void proceduraCheckoutTuttiVerticali(List<String> listaVerticali, String nomeBranch, String percorso)
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
	
	public static List<String> checkoutTuttiVerticali(List<String> listaVerticali, String nomeBranch, String percorso)
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
	
	public static List<String> checkoutVerticaliNonSwitchati(List<String> verticaliNonSwitchati, String nomeBranch, String percorso)
	{
		for(String verticale : verticaliNonSwitchati)
		{
			boolean checkoutAvvenuto = checkoutVerticale(percorso +"\\"+ verticale, nomeBranch);
			if(checkoutAvvenuto)
				verticaliNonSwitchati.remove(verticale);
		}
		
		return verticaliNonSwitchati;
	}
	
	public static boolean checkoutVerticale(String percorso, String nomeBranch)
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
	
	public static void pullTuttiVerticali(List<String> listaVerticali, String percorso)
	{
		for(String verticale : listaVerticali)
			pullVerticale(percorso +"\\"+ verticale);
		System.out.println();
	}
	
	public static void pullVerticaliNonSwitchati(List<String> verticaliNonSwitchati, String percorso)
	{
		for(String verticale : verticaliNonSwitchati)
			pullVerticale(percorso +"\\"+ verticale);
		System.out.println();
	}
	
	public static void pullVerticale(String percorso)
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
	
	public static boolean pullOriginVerticali(List<String> listaVerticali, String percorso)
	{
		boolean flagConflitti = false;
		for(String verticale : listaVerticali)
			flagConflitti = flagConflitti | pullOriginVerticale(percorso +"\\"+ verticale);
		System.out.println();
		
		return flagConflitti;
	}
	
	public static boolean pullOriginVerticale(String percorso)
	{
		try
		{
			boolean flagConflitti = gitPullOrigin(StringConstants.COMANDO_GIT_PULL_ORIGIN + StringConstants.BRANCH_SVIL, percorso);
			if(!flagConflitti)
				System.out.println("-- Verticale: "+ percorso +" - "+ StringConstants.COMANDO_GIT_PULL_ORIGIN + StringConstants.BRANCH_SVIL +" --> OK");
			else
				System.out.println("-- Verticale: "+ percorso +" - "+ StringConstants.COMANDO_GIT_PULL_ORIGIN + StringConstants.BRANCH_SVIL +" --> CONFLITTI");
			
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
	
	public static void proceduraGestioneConflitti(List<String> listaVerticali, String percorso)
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
	
	public static void commitConflittiVerticali(List<String> listaVerticali, String percorso)
	{
		for(String verticale : listaVerticali)
			commitConflittiVerticale(percorso +"\\"+ verticale);
	}
	
	public static void commitConflittiVerticale(String percorso)
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
	
	public static boolean pullOriginMasterVerticali(List<String> listaVerticali, String percorso)
	{
		boolean flagConflitti = false;
		for(String verticale : listaVerticali)
			flagConflitti = flagConflitti | pullOriginMasterVerticale(percorso +"\\"+ verticale);
		System.out.println();
		
		return flagConflitti;
	}
	
	public static boolean pullOriginMasterVerticale(String percorso)
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
	
	public static boolean statusVerticali(List<String> listaVerticali, String percorso)
	{
		boolean tuttoCommittato = false;
		for(String verticale : listaVerticali)
			tuttoCommittato = tuttoCommittato | statusVerticale(percorso +"\\"+ verticale);
		System.out.println();
		
		return tuttoCommittato;
	}
	
	public static boolean statusVerticale(String percorso)
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
	
	public static void verificaModificheNonCommittate()
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
	
	public static void commitVuotoVerticali(List<String> listaVerticali, String nomeBranch, String percorso)
	{
		for(String verticale : listaVerticali)
			commitVuotoVerticale(percorso +"\\"+ verticale, nomeBranch);
		System.out.println();
	}
	
	public static void commitVuotoVerticale(String percorso, String nomeBranch)
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
	
	public static void pushVerticali(List<String> listaVerticali, String v1, String v2, String percorso)
	{
		List<String> intervalloVerticali = listaVerticali.subList(listaVerticali.indexOf(v1), listaVerticali.indexOf(v2)+1);
		for(String verticale : intervalloVerticali)
			pushVerticale(percorso +"\\"+ verticale);
		System.out.println();
	}
	
	public static void pushVerticale(String percorso)
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
	
	public static boolean verificaIntervalloVerticali(String v1, String v2)
	{
		if(v1 == null || v2 == null)
			return false;
		if("".equals(v1) || "".equals(v2))
			return false;
		if(v1.compareTo(v2) < 0)
			return true;
		else
			return false;
	}
	
	private static void proceduraSostituzioneVersioniPom(String percorsoCartellaVerticali)
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
			ricercaFilePom(percorsoCartellaVerticali, listaVersioni);
		}
		catch(IOException ex)
		{
			System.out.println("Errore nell'aggiornamento delle versioni dei POM");
			ex.printStackTrace();
		}
		System.out.println();
	}
	
	private static void ricercaFilePom(String percorsoRoot, List<String> listaVersioni)
	{
		File file = new File(percorsoRoot);
		ricercaFilePomRicorsiva(file, listaVersioni);
	}
	
	private static void ricercaFilePomRicorsiva(File file, List<String> listaVersioni)
	{
		if(!file.isDirectory())
		{
			if("pom.xml".equalsIgnoreCase(file.getName()))
				sostituisciVersioni(file, listaVersioni);
		}
		else
		{
			if(!"target".equalsIgnoreCase(file.getName()))
			{
				File[] listaFiles = file.listFiles();
				if(listaFiles != null)
					for(File f : listaFiles)
						ricercaFilePomRicorsiva(f, listaVersioni);
			}
		}
	}
	
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
		
		proceduraSostituzioneVersioniPom(percorso);
		commitVuotoVerticali(listaVerticali, nomeBranch, percorso);
		proceduraPushIntervalliVerticali(listaVerticali, percorso);
		
		System.out.println("--- Sostituzione automatica terminata ---\n");
	}
	
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
	
	/* Metodo main da tenere nel caso sia necessaria la sola procedura di aggiornamento dei POM dei verticali */
	public static void main(String[] args)
	{
		String percorso = "D:\\Openshift\\Verticali\\cdbp0";
		proceduraSostituzioneVersioniPom(percorso);
	}
}
