package allineamenti;

import java.util.Scanner;

public class FunzioniConsole
{
	public static void mostraAllineamentiPossibili()
	{
		System.out.println("Operazioni possibili");
		System.out.println("  DESCRIZIONE                  | COMANDO");
		System.out.println("  -----------------------------|------------------------------------------------------");
		System.out.println("  1) Allineamento EJB migrati: | ejb <nomeBranch>       (es. --> ejb env/svis )");
		System.out.println("  2) Allineamento verticali:   | verticali <nomeBranch> (es. --> verticali env/svis )");
		System.out.println("  3) Esci:                     | exit\n");
	}
	
	public static String inputComando()
	{
		Scanner scanner = new Scanner(System.in);
		System.out.print(">>> Comando: ");
		String comando = scanner.nextLine();
		System.out.println();
		
		return comando;
	}
	
	public static String inputComandoAllineamento()
	{
		String comando;
		do
		{
			mostraAllineamentiPossibili();
			comando = inputComando();
		} while(!verificaComandoAllineamento(comando));
		
		return comando;
	}
	
	public static boolean verificaComandoAllineamento(String comando)
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
		case StringConstants.ESCI:
			return true;
		default:
			System.out.println("Azione non consentita. Riprovare\n");
			return false;
		}
	}
}