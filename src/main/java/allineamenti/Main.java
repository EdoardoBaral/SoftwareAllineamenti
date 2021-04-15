package allineamenti;

import static allineamenti.FunzioniConsole.inputComandoAllineamento;
import static allineamenti.FunzioniEjb.eseguiAllineamentoEjb;
import static allineamenti.FunzioniEjb.eseguiCloneEjb;
import static allineamenti.FunzioniVerticali.eseguiAllineamentoVerticali;
import static allineamenti.FunzioniVerticali.eseguiCloneVerticali;
import static allineamenti.FunzioniVerticali.eseguiSostituzioneAutomatica;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * Classe principale del progetto, che contiene il metodo main() necessario all'avvio del programma di allineamento.
 *
 * @author Edoardo Baral
 */
public class Main
{
	/**
	 * Metodo principale del progetto, necessario per avviare il programma di allineamento.
	 * @param args: parametri passati da riga di comando, facoltativi
	 * @throws IOException nel caso in cui i metodi sottostanti riscontrino errori nella lettura dei file di testo ausiliari
	 */
	public static void main(String[] args) throws IOException
	{
		SetupApplication setupApplication = new SetupApplication();
		Map<String, List<String>> mapEjb = setupApplication.caricaListeEjb();
		List<String> listaVerticali = setupApplication.caricaListaVerticali();
		
		System.out.println(">>> NDCE GIT BRANCH MANAGER (v4.0) <<<\n");
		
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
			else if(StringUtils.containsIgnoreCase(comando, StringConstants.DOWNLOAD_EJB))
				eseguiCloneEjb(mapEjb);
			else if(StringUtils.containsIgnoreCase(comando, StringConstants.DOWNLOAD_VERTICALI))
				eseguiCloneVerticali(listaVerticali);
		} while(!StringConstants.ESCI.equals(comando));
		
		System.out.println("TERMINAZIONE PROGRAMMA");
	}
}
