package allineamenti;

import static allineamenti.FunzioniConsole.inputComandoAllineamento;
import static allineamenti.FunzioniEjb.eseguiAllineamentoEjb;
import static allineamenti.FunzioniVerticali.eseguiAllineamentoVerticali;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Main
{
	public static void main(String[] args) throws IOException
	{
		SetupApplication setupApplication = new SetupApplication();
		Map<String, List<String>> mapEjb = setupApplication.caricaListeEjb();
		List<String> listaVerticali = setupApplication.caricaListaVerticali();
		
		System.out.println(">>> NDCE GIT BRANCH MANAGER (v2.0) <<<\n");
		
		String comando;
		do
		{
			comando = inputComandoAllineamento();
			
			if (comando.contains(StringConstants.ALLINEAMENTO_EJB))
				eseguiAllineamentoEjb(mapEjb, comando);
			else if(comando.contains(StringConstants.ALLINEAMENTO_VERTICALI))
				eseguiAllineamentoVerticali(listaVerticali, comando);
		} while(!StringConstants.ESCI.equals(comando));
		
		System.out.println("TERMINAZIONE PROGRAMMA");
	}
}
