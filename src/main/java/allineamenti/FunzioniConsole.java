package allineamenti;

import java.util.Scanner;

public class FunzioniConsole
{
	static void mostraFunzioniPossibili()
	{
		System.out.println("Operazioni possibili");
		System.out.println("  DESCRIZIONE                                                | COMANDO");
		System.out.println("  -----------------------------------------------------------|----------------------------------------------------------------");
		System.out.println("  1) Allineamento EJB migrati:                               | ejb <nomeBranch>          (es. --> ejb env/svis )");
		System.out.println("  2) Allineamento verticali:                                 | verticali <nomeBranch>    (es. --> verticali env/svis )");
		System.out.println("  3) Sostituzione automatica versioni nei POM dei verticali: | sostituzione <nomeBranch> (es. --> sostituzione env/svis");
		System.out.println("  3) Esci:                                                   | exit\n");
	}
	
	private static String inputComando()
	{
		Scanner scanner = new Scanner(System.in);
		System.out.print(">>> Comando: ");
		String comando = scanner.nextLine();
		System.out.println();
		
		return comando;
	}
	
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
	
	private static boolean verificaComandoAllineamento(String comando)
	{
		switch(comando)
		{
			case (StringConstants.ALLINEAMENTO_EJB + StringConstants.BRANCH_SVIL):
			case (StringConstants.ALLINEAMENTO_EJB + StringConstants.BRANCH_SVIS):
			case (StringConstants.ALLINEAMENTO_EJB + StringConstants.BRANCH_PTES):
			case (StringConstants.ALLINEAMENTO_EJB + StringConstants.BRANCH_SVIA):
			case (StringConstants.ALLINEAMENTO_EJB + StringConstants.BRANCH_PTEA):
			case (StringConstants.ALLINEAMENTO_VERTICALI + StringConstants.BRANCH_SVIL):
			case (StringConstants.ALLINEAMENTO_VERTICALI + StringConstants.BRANCH_SVIS):
			case (StringConstants.ALLINEAMENTO_VERTICALI + StringConstants.BRANCH_SVIA):
			case (StringConstants.SOSTITUZIONE_AUTOMATICA + StringConstants.BRANCH_SVIL):
			case (StringConstants.SOSTITUZIONE_AUTOMATICA + StringConstants.BRANCH_SVIS):
			case (StringConstants.SOSTITUZIONE_AUTOMATICA + StringConstants.BRANCH_SVIA):
			case StringConstants.ESCI:
				return true;
			default:
				System.out.println("Azione non consentita. Riprovare\n");
				return false;
		}
	}
}
