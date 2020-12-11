package allineamenti;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe che contiene i metodi necessari per inizializzare le strutture dati all'avvio del programma di allineamento.
 * Gli EJB migrati sono indicati all'interno di un file "EjbMigrati.txt", suddivisi per blocchi di compilazione, e vengono organizzati all'interno di un'hashmap.
 * I verticali, invece, sono indicati all'interno di un file "Verticali.txt" e, dal momento che non sono dipendenti l'uno dall'altro, vengono organizzati in una semplice lista ordinata
 * alfabeticamente.
 * Per quanto riguarda i tag XML contenenti le versioni aggiornate da sostituire nei POM padri dei verticali, sono indicati all'interno di un file "VersioniPOM.txt" e vengono organizzati
 * all'interno di una lista.
 *
 * @author Edoardo Baral
 */

public class SetupApplication
{
	/**
	 * Metodo che legge il contenuto del file "EjbMigrati.txt" e organizza tutti gli EJB migrati di Alten (dell'acronimo CDBP0) in un'hashmap, la cui chiave è una stringa indicante
	 * il nome del blocco di EJB (es. B1, B2...) e il valore è una lista di String contenente i nomi dei singoli EJB appartenenti a quel blocco di compilazione.
	 * @return un'hashmap con chiavi i nomi dei blocchi di EJB e come valori associati le liste dei nomi dei singoli EJB facenti parte di ognuno dei blocchi
	 * @throws IOException nel caso in cui si verifichino problemi nella lettura del file "EjbMigrati.txt"
	 */
	Map<String, List<String>> caricaListeEjb() throws IOException
	{
		final String nomeFile = "EjbMigrati.txt";
		Map<String, List<String>> map = new HashMap<>();
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(nomeFile).getFile());
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		String riga;
		while ((riga = br.readLine()) != null)
		{
			if (riga.length() > 0 && riga.contains("--B"))
			{
				String nomeBlocco = riga.substring(2, riga.indexOf(':'));
				String nomeEjb = riga.substring(riga.indexOf(':') + 2);
				List<String> listaEjb = map.get(nomeBlocco);
				
				if (listaEjb == null)
				{
					listaEjb = new ArrayList<>();
					listaEjb.add(nomeEjb);
					map.put(nomeBlocco, listaEjb);
				}
				else
				{
					listaEjb.add(nomeEjb);
					map.put(nomeBlocco, listaEjb);
				}
			}
		}
		
		return map;
	}
	
	/**
	 * Metodo che legge il contenuto del file "Verticali.txt" e organizza i nomi dei verticali di Alten (dell'acronimo CDBP0) in una lista di stringhe, ordinata alfabeticamente.
	 * @return una lista di stringhe contenente i nomi di tutti i verticali ordinati alfabeticamente
	 * @throws IOException se si verificano errori nella lettura dal file "Verticali.txt"
	 */
	List<String> caricaListaVerticali() throws IOException
	{
		final String nomeFile = "Verticali.txt";
		List<String> listaVerticali = new ArrayList<>();
		
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(nomeFile).getFile());
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		String riga;
		while ((riga = br.readLine()) != null)
		{
			listaVerticali.add(riga);
		}
		
		return listaVerticali;
	}
	
	/**
	 * Metodo che legge il contenuto del file "VersioniPOM.txt" e organizza i tag XML in esso contenuti in una lista di stringhe, in cui ogni elemento rappresenta un tag XML che va
	 * aggiornato all'interno dei POM padri dei verticali di Alten
	 * @return una lista di stringhe contenente tutti i tagXML che vanno aggiornati nei POM padri dei verticali
	 * @throws IOException nel caso in cui si verifichino errori nella lettura del file "VersioniPOM.txt"
	 */
	List<String> leggiVersioniPom() throws IOException
	{
		List<String> listaVersioni = new ArrayList<>();
		String nomeFile = "VersioniPOM.txt";
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(nomeFile).getFile());
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		String riga;
		while ((riga = br.readLine()) != null )
		{
			if (!"".equals(riga))
				listaVersioni.add(riga);
		}
		
		return listaVersioni;
	}
}
