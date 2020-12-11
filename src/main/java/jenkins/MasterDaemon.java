package jenkins;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MasterDaemon {

	public static String branchDiLavoro;
	public static List<String> buildAttive = new LinkedList<String>();
	public static List<String> buildSuccess = new LinkedList<String>();
	public static List<String> buildNonPertinenti = new LinkedList<String>();
	public static List<String> elencoVerticali = new LinkedList<>();

	public static void main(String args[]) throws IOException, InterruptedException {
		/*
		* Punto di ingresso del sw. Il master si occupa di monitorare la pipeline richiedendone le build in corso ogni 15 sec.
		* Appena trova una build nuova, delega uno slave ad occuparsene.
		* */
		String command = inputComando();
		System.out.println(">>> Daemon avviato, appena una build sarà completata vedrai i log della pomotion");
		branchDiLavoro = command;
		caricaListaVerticali();
		while(true) {
			String masterContent=JenkinsClient.getContent(JenkinsConstants.jenkinsUrl, null);
			Document doc = Jsoup.parse(masterContent);
			Elements builds = doc.getElementsByTag("build");
			for (Element e : builds) {
				String url = e.getElementsByTag("url").html();
				String number = e.getElementsByTag("number").html();
				if(buildSuccess.contains(number) || buildNonPertinenti.contains(number) || buildAttive.contains(number)){
					//ho trovato build che non mi interessano perché sono già completate o diverse dal branch d'interesse
					continue;
				}
				//delego un slave daemon per monitorare la singola build
				SlaveDaemon slaveDaemon = new SlaveDaemon(url,number);
				Thread t = new Thread(slaveDaemon);
				t.start();
				Thread.sleep(2000);
			}
			Thread.sleep(15000);
		}

	}
	private static String inputComando()
	{
		Scanner scanner = new Scanner(System.in);
		System.out.print(">>> Inserire il nome del branch di interesse es. env/svia oppure env/svis: ");
		String comando = scanner.nextLine();
		System.out.println();
		if(comando.equals("env/svia")){
			JenkinsConstants.jsonData = JenkinsConstants.jsonData.replace("environmentToChange","PTEA");

		}else if(comando.equals("env/svis")){
			JenkinsConstants.jsonData = JenkinsConstants.jsonData.replace("environmentToChange","PTES");
		}else{
			System.out.println("Nome branch non valido. Rilanciare");
			throw new IllegalArgumentException();
		}

		return comando;
	}
	private static void caricaListaVerticali() throws IOException {
		ClassLoader classLoader = MasterDaemon.class.getClassLoader();
		File file = new File(classLoader.getResource("Verticali.txt").getFile());
		Path path = file.toPath();
		elencoVerticali = Files.readAllLines(path, StandardCharsets.UTF_8);
	}
}
