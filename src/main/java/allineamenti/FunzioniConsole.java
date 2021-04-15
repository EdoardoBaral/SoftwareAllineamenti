package allineamenti;

import java.util.Scanner;

/**
 * Classe che contiene i metodi principali per avviare le varie procedure di allineamento e sostituzione.
 *
 * @author Edoardo Baral
 */
public class FunzioniConsole
{
	/**
	 * Metodo statico che mostra su terminale le possibili funzionalità del programma di allineamento
	 * 1) allineamento degli EJB migrati
	 * 2) allineamento dei verticali
	 * 3) procedura di sostituzione automatica delle versioni nei POM dei verticali
	 * 4) terminazione
	 */
	static void mostraFunzioniPossibili()
	{
		System.out.println("Operazioni possibili");
		System.out.println("  DESCRIZIONE                                                | COMANDO                                | ESEMPIO");
		System.out.println("  -----------------------------------------------------------|----------------------------------------|----------------------------------------------------------------------------");
		System.out.println("  1) Allineamento EJB migrati:                               | ejb <nomeBranch> <branchOrigine>       | parametro branchOrigine facoltativo, es. --> ejb env/svis env/svia");
		System.out.println("  2) Allineamento verticali:                                 | verticali <nomeBranch> <branchOrigine> | parametro branchOrigine facoltativo, es. --> verticali env/svis env/svia");
		System.out.println("  3) Sostituzione automatica versioni nei POM dei verticali: | sostituzione <nomeBranch>              | es. --> sostituzione env/svis");
		System.out.println("  4) Download EJB:                                           | downloadEjb");
		System.out.println("  5) Downalod verticali:                                     | downloadVerticali");
		System.out.println("  6) Esci:                                                   | exit\n");
	}
	
	/**
	 * Metodo statico privato che permette l'acquisizione di una stringa immessa dall'utente nel terminale
	 * @return il comando digitato dall'utente sul terminale
	 */
	private static String inputComando()
	{
		Scanner scanner = new Scanner(System.in);
		System.out.print(">>> Comando: ");
		String comando = scanner.nextLine();
		System.out.println();
		
		return comando;
	}
	
	/**
	 * Metodo statico che permette di acquisire il comando dell'utente per avviare una delle possibili funzionalità principali. Il metodo verifica anche che il comando immesso dall'utente
	 * sia valido.
	 * @return il comando digitato dall'utente su terminale
	 */
	static String inputComandoAllineamento()
	{
		String comando;
		do
		{
			mostraFunzioniPossibili();
			comando = inputComando();
		} while(!verificaComandoAllineamento(comando));
		
		return comando;
	}
	
	/**
	 * Metodo statico che verifica la validità del comando digitato dall'utente
	 * @param comando: comando digitato dall'utente
	 * @return true se il comando è valido, false altrimenti
	 */
	private static boolean verificaComandoAllineamento(String comando)
	{
		switch(comando)
		{
			case (StringConstants.ALLINEAMENTO_EJB + StringConstants.BRANCH_SVIL):
			case (StringConstants.ALLINEAMENTO_EJB + StringConstants.BRANCH_SVIS):
			case (StringConstants.ALLINEAMENTO_EJB + StringConstants.BRANCH_SVIA):
			case (StringConstants.ALLINEAMENTO_EJB + StringConstants.BRANCH_SVIL +" "+ StringConstants.BRANCH_SVIA):
			case (StringConstants.ALLINEAMENTO_EJB + StringConstants.BRANCH_SVIL +" "+ StringConstants.BRANCH_SVIS):
			case (StringConstants.ALLINEAMENTO_EJB + StringConstants.BRANCH_SVIS +" "+ StringConstants.BRANCH_SVIA):
			case (StringConstants.ALLINEAMENTO_EJB + StringConstants.BRANCH_SVIA +" "+ StringConstants.BRANCH_SVIS):
			case (StringConstants.ALLINEAMENTO_VERTICALI + StringConstants.BRANCH_SVIL):
			case (StringConstants.ALLINEAMENTO_VERTICALI + StringConstants.BRANCH_SVIS):
			case (StringConstants.ALLINEAMENTO_VERTICALI + StringConstants.BRANCH_SVIA):
			case (StringConstants.ALLINEAMENTO_VERTICALI + StringConstants.BRANCH_SVIL +" "+ StringConstants.BRANCH_SVIA):
			case (StringConstants.ALLINEAMENTO_VERTICALI + StringConstants.BRANCH_SVIL +" "+ StringConstants.BRANCH_SVIS):
			case (StringConstants.ALLINEAMENTO_VERTICALI + StringConstants.BRANCH_SVIS +" "+ StringConstants.BRANCH_SVIA):
			case (StringConstants.ALLINEAMENTO_VERTICALI + StringConstants.BRANCH_SVIA +" "+ StringConstants.BRANCH_SVIS):
			case (StringConstants.SOSTITUZIONE_AUTOMATICA + StringConstants.BRANCH_SVIL):
			case (StringConstants.SOSTITUZIONE_AUTOMATICA + StringConstants.BRANCH_SVIS):
			case (StringConstants.SOSTITUZIONE_AUTOMATICA + StringConstants.BRANCH_SVIA):
			case (StringConstants.DOWNLOAD_EJB):
			case (StringConstants.DOWNLOAD_VERTICALI):
			case StringConstants.ESCI:
				return true;
			default:
				System.out.println("Azione non consentita. Riprovare\n");
				return false;
		}
	}
}
