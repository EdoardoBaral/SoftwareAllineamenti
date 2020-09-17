package allineamenti;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SetupApplication {
	public Map<String, List<String>> caricaListeEjb() throws IOException {
		final String nomeFile = "EjbMigrati.txt";
		Map<String, List<String>> map = new HashMap<>();
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(nomeFile).getFile());
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		String riga;
		while ((riga = br.readLine()) != null) {
			if (riga.length() > 0 && riga.contains("--B")) {
				String nomeBlocco = riga.substring(2, riga.indexOf(':'));
				String nomeEjb = riga.substring(riga.indexOf(':') + 2);
				List<String> listaEjb = map.get(nomeBlocco);
				
				if (listaEjb == null) {
					listaEjb = new ArrayList<>();
					listaEjb.add(nomeEjb);
					map.put(nomeBlocco, listaEjb);
				} else {
					listaEjb.add(nomeEjb);
					map.put(nomeBlocco, listaEjb);
				}
			}
		}
		
		return map;
	}
	
	public List<String> caricaListaVerticali() throws IOException {
		final String nomeFile = "Verticali.txt";
		List<String> listaVerticali = new ArrayList<>();
		
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(nomeFile).getFile());
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		String riga;
		while ((riga = br.readLine()) != null) {
			listaVerticali.add(riga);
		}
		
		return listaVerticali;
	}
	
	public List<String> leggiVersioniPom() throws IOException
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
