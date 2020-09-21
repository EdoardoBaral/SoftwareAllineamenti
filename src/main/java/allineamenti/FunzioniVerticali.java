package allineamenti;

import static allineamenti.GitCommands.executeCommand;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

		System.out.println("--- Pull di tutti i verticali\n");
		pullTuttiVerticali(listaVerticali, percorso);

		if(!StringConstants.BRANCH_SVIL.equals(nomeBranch))
		{
			System.out.println("--- Merge di tutti i verticali dal branch '"+ StringConstants.BRANCH_SVIL+"'\n");
			pullOriginVerticali(listaVerticali, percorso);
			proceduraGestioneConflitti(listaVerticali, percorso);
			confermaVersioniPomVerticali();
		}

		System.out.println("--- Merge di tutti i verticali dal branch master\n");
		pullOriginMasterVerticali(listaVerticali, percorso);
		proceduraGestioneConflitti(listaVerticali, percorso);
//		confermaVersioniPomVerticali();
		proceduraSostituzioneVersioniPom(percorso);

		statusVerticali(listaVerticali, percorso);
		verificaModificheNonCommittate();
		commitVuotoVerticali(listaVerticali, nomeBranch, percorso);

		boolean checkTerminazionePush = false;
		do
		{
			boolean flagIntervalloValido = true;
			String v1, v2;
			do
			{
				System.out.println("--- Specificare i nomi dei due verticali che determinano l'intervallo dei verticali da compilare, estremi inclusi");
				System.out.print("    1) Primo verticale");
				v1 = inputScelta();
				System.out.print("    2) Secondo verticale");
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

			System.out.println("Vuoi eseguire la push su un altro intervallo di verticali (S/N)?");
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
			System.out.print(">>> Inserisci il percorso della cartella contenente i verticali (es. 'D:\\Openshift\\Verticali\\cdbp0'): ");
			percorso = scanner.nextLine();
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
		boolean checkoutFlag = false;
		do
		{
			checkoutTuttiVerticali(listaVerticali, nomeBranch, percorso);
			System.out.println();
			mostraBranchTuttiVerticali(listaVerticali, percorso);
			System.out.println();
			
			System.out.print(">>> Confermi che tutti i verticali sono passati correttamente al branch '"+ nomeBranch +"' (S/N)? ");
			String cmd = inputScelta();
			System.out.println();
			
			if("S".equalsIgnoreCase(cmd))
			{
				checkoutFlag = true;
			}
			else if("N".equalsIgnoreCase(cmd))
			{
				System.out.println("Si e' verificato un problema nel checkout dei verticali che va risolto manualmente");
				System.out.print(">>> Richiesta conferma per poter continuare e ritentare il checkout dei verticali (S: continua - N: termina programma): ");
				cmd = inputScelta();
				if("N".equalsIgnoreCase(cmd))
				{
					System.out.println("TERMINAZIONE PROGRAMMA");
					System.exit(0);
				}
				else if("S".equalsIgnoreCase(cmd))
				{
					pullTuttiVerticali(listaVerticali, percorso);
				}
			}
		} while(!checkoutFlag);
	}
	
	public static void checkoutTuttiVerticali(List<String> listaVerticali, String nomeBranch, String percorso)
	{
		for(String verticale : listaVerticali)
			checkoutVerticale(percorso +"\\"+ verticale, nomeBranch);
	}
	
	public static void checkoutVerticale(String percorso, String nomeBranch)
	{
		System.out.println("-- Verticale: "+ percorso +" - "+ StringConstants.COMANDO_GIT_CHECKOUT + nomeBranch);
		try
		{
			executeCommand(StringConstants.COMANDO_GIT_CHECKOUT + nomeBranch, percorso);
		}
		catch(IOException ex)
		{
			System.out.println("Errore durante il checkout del verticale '"+ percorso +"' sul branch '"+ nomeBranch +"'");
			ex.printStackTrace();
		}
		
		System.out.println("--------------------");
	}
	
	public static void mostraBranchTuttiVerticali(List<String> listaVerticali, String percorso)
	{
		System.out.println("--- Status di tutti i verticali\n");
		for(String verticale : listaVerticali)
			mostraBranchVerticale(percorso +"\\"+ verticale);
	}
	
	public static void mostraBranchVerticale(String percorso)
	{
		System.out.println("-- Verticale: "+ percorso +" - "+ StringConstants.COMANDO_GIT_BRANCH);
		try
		{
			executeCommand(StringConstants.COMANDO_GIT_BRANCH, percorso);
		}
		catch(IOException ex)
		{
			System.out.println("Errore durante la verifica del branch del verticale '"+ percorso +"'");
			ex.printStackTrace();
		}
		
		System.out.println("--------------------");
	}
	
	public static void pullTuttiVerticali(List<String> listaVerticali, String percorso)
	{
		for(String verticale : listaVerticali)
			pullVerticale(percorso +"\\"+ verticale);
		System.out.println();
	}
	
	public static void pullVerticale(String percorso)
	{
		System.out.println("-- Verticale: "+ percorso +" - "+ StringConstants.COMANDO_GIT_PULL);
		try
		{
			executeCommand(StringConstants.COMANDO_GIT_PULL, percorso);
		}
		catch (IOException ex)
		{
			System.out.println("Errore durante la pull del verticale '"+ percorso +"'");
			ex.printStackTrace();
		}
		System.out.println("--------------------");
	}
	
	public static void pullOriginVerticali(List<String> listaVerticali, String percorso)
	{
		for(String verticale : listaVerticali)
			pullOriginVerticale(percorso +"\\"+ verticale);
		System.out.println();
	}
	
	public static void pullOriginVerticale(String percorso)
	{
		System.out.println("-- Verticale: "+ percorso +" - "+ StringConstants.COMANDO_GIT_PULL_ORIGIN + StringConstants.BRANCH_SVIL);
		try
		{
			executeCommand(StringConstants.COMANDO_GIT_PULL_ORIGIN + StringConstants.BRANCH_SVIL, percorso);
		}
		catch (IOException ex)
		{
			System.out.println("Errore durante il merge del verticale '"+ percorso +"' dal branch '"+ StringConstants.BRANCH_SVIL +"'");
			ex.printStackTrace();
		}
		System.out.println("--------------------");
	}
	
	public static void proceduraGestioneConflitti(List<String> listaVerticali, String percorso)
	{
		System.out.print(">>> Ci sono conflitti da risolvere (S/N)? ");
		String scelta = inputScelta();
		System.out.println();
		
		if("S".equalsIgnoreCase(scelta))
		{
			do
			{
				System.out.println("--- Ci sono conflitti da risolvere");
				System.out.println("    1) Risolvere su IntelliJ i conflitti segnalati");
				System.out.println("    2) Non effettuare il commit per risolvere i conflitti, ci pensa il software");
				System.out.println("    3) Digitare S per far continuare il programma");
				System.out.print(">>> Comando: ");
				scelta = inputScelta();
				System.out.println();
			} while(!"S".equalsIgnoreCase(scelta));
			
			System.out.println("--- Commit per risolvere i conflitti su tutti i verticali\n");
			commitConflittiVerticali(listaVerticali, percorso);
			System.out.println();
			System.out.println("--- Conflitti sui verticali risolti\n");
		}
	}
	
	public static void confermaVersioniPomVerticali()
	{
		String scelta;
		System.out.println("--- Verifica versioni POM dei verticali");
		System.out.println("    1) Verificare le versioni nei POM dei verticali");
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
	
	public static void commitConflittiVerticali(List<String> listaVerticali, String percorso)
	{
		for(String verticale : listaVerticali)
			commitConflittiVerticale(percorso +"\\"+ verticale);
	}
	
	public static void commitConflittiVerticale(String percorso)
	{
		System.out.println("-- Verticale: "+ percorso +" - "+ StringConstants.COMANDO_GIT_COMMIT);
		try
		{
			executeCommand(StringConstants.COMANDO_GIT_COMMIT, percorso);
		}
		catch (IOException ex)
		{
			System.out.println("Errore durante il commit per i conflitti del verticale '"+ percorso +"'");
			ex.printStackTrace();
		}
		System.out.println("--------------------");
	}
	
	public static void pullOriginMasterVerticali(List<String> listaVerticali, String percorso)
	{
		for(String verticale : listaVerticali)
			pullOriginMasterVerticale(percorso +"\\"+ verticale);
		System.out.println();
	}
	
	public static void pullOriginMasterVerticale(String percorso)
	{
		System.out.println("-- Verticale: "+ percorso +" - "+ StringConstants.COMANDO_GIT_PULL_ORIGIN + StringConstants.BRANCH_MASTER);
		try
		{
			executeCommand(StringConstants.COMANDO_GIT_PULL_ORIGIN + StringConstants.BRANCH_MASTER, percorso);
		}
		catch (IOException ex)
		{
			System.out.println("Errore durante il merge del verticale '"+ percorso +"' dal branch '"+ StringConstants.BRANCH_MASTER +"'");
			ex.printStackTrace();
		}
		System.out.println("--------------------");
	}
	
	public static void statusVerticali(List<String> listaVerticali, String percorso)
	{
		for(String verticale : listaVerticali)
			statusVerticale(percorso +"\\"+ verticale);
		System.out.println();
	}
	
	public static void statusVerticale(String percorso)
	{
		System.out.println("-- Verticale: "+ percorso +" - "+ StringConstants.COMANDO_GIT_STATUS);
		try
		{
			executeCommand(StringConstants.COMANDO_GIT_STATUS, percorso);
		}
		catch (IOException ex)
		{
			System.out.println("Errore durante la verifica dello status del verticale '"+ percorso +"'");
			ex.printStackTrace();
		}
		System.out.println("--------------------");
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
				System.out.println("-- Verticale: "+ percorso +" - "+ StringConstants.COMANDO_GIT_RELEASE);
				executeCommand(StringConstants.COMANDO_GIT_RELEASE, percorso);
			}
			else
			{
				System.out.println("-- Verticale: "+ percorso +" - "+ StringConstants.COMANDO_GIT_TAG_PROMOTE);
				executeCommand(StringConstants.COMANDO_GIT_TAG_PROMOTE, percorso);
			}
		}
		catch(IOException ex)
		{
			System.out.println("Errore durante il commit vuoto per innescare la build del verticale '"+ percorso +"'");
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
		System.out.println("-- Verticale: "+ percorso +" - "+ StringConstants.COMANDO_GIT_PUSH);
		try
		{
			executeCommand(StringConstants.COMANDO_GIT_PUSH, percorso);
		}
		catch (IOException ex)
		{
			System.out.println("Errore durante la push dei commit del verticale '"+ percorso +"'");
			ex.printStackTrace();
		}
		System.out.println("--------------------");
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
	
	public static void proceduraSostituzioneVersioniPom(String percorsoCartellaVerticali)
	{
		System.out.println("--- Verifica e sostituzione automatica delle versioni aggiornate nei POM dei verticali");
		System.out.println("    1) Assicurati che nel file VersioniPOM.txt siano presenti tutte le versioni da aggiornare") ;
		System.out.println("    2) Digita S per avviare la procedura oppure N per saltarla") ;
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
	
	public static void ricercaFilePom(String percorsoRoot, List<String> listaVersioni)
	{
		File file = new File(percorsoRoot);
		ricercaFilePomRicorsiva(file, listaVersioni);
	}
	
	public static void ricercaFilePomRicorsiva(File file, List<String> listaVersioni)
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
				for(File f : listaFiles)
					ricercaFilePomRicorsiva(f, listaVersioni);
			}
		}
	}
	
	public static void sostituisciVersioni(File filePom, List<String> listaVersioni)
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
	
	public static void main(String[] args)
	{
		String percorso = "D:\\Openshift\\Verticali\\cdbp0";
		proceduraSostituzioneVersioniPom(percorso);
	}
}
