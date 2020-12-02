package allineamenti;

import static allineamenti.FunzioniConsole.inputComandoAllineamento;
import static allineamenti.FunzioniEjb.eseguiAllineamentoEjb;
import static allineamenti.FunzioniEjb.eseguiAllineamentoEjbPostRilascio;
import static allineamenti.FunzioniVerticali.eseguiAllineamentoVerticali;
import static allineamenti.FunzioniVerticali.eseguiAllineamentoVerticaliPostRilascio;
import static allineamenti.FunzioniVerticali.eseguiSostituzioneAutomatica;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class Main
{
	public static void main(String[] args) throws IOException
	{
		SetupApplication setupApplication = new SetupApplication();
		Map<String, List<String>> mapEjb = setupApplication.caricaListeEjb();
		List<String> listaVerticali = setupApplication.caricaListaVerticali();
		
		System.out.println(">>> NDCE GIT BRANCH MANAGER (v3.0) <<<\n");
		
		String comando;
		do
		{
			comando = inputComandoAllineamento();
			
			if(StringUtils.containsIgnoreCase(comando, StringConstants.ALLINEAMENTO_EJB))
				eseguiAllineamentoEjb(mapEjb, comando);
			else if(StringUtils.containsIgnoreCase(comando, StringConstants.ALLINEAMENTO_VERTICALI))
				eseguiAllineamentoVerticali(listaVerticali, comando);
			else if(StringUtils.containsIgnoreCase(comando, StringConstants.SOSTITUZIONE_AUTOMATICA))
				eseguiSostituzioneAutomatica(listaVerticali, comando);
			else if(StringUtils.containsIgnoreCase(comando, StringConstants.ALLINEAMENTO_EJB_POST_RILASCIO))
				eseguiAllineamentoEjbPostRilascio(mapEjb, comando);
			else if(StringUtils.containsIgnoreCase(comando, StringConstants.ALLINEAMENTO_VERTICALI_POST_RILASCIO))
				eseguiAllineamentoVerticaliPostRilascio(listaVerticali, comando);
		} while(!StringConstants.ESCI.equals(comando));
		
		System.out.println("TERMINAZIONE PROGRAMMA");
	}
}
