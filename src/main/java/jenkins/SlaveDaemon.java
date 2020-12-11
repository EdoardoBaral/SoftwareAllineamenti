package jenkins;

import java.io.IOException;
import java.time.LocalTime;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class SlaveDaemon implements Runnable {

	private String myJobUrl;
	private String myJobNumber;

	private String nomeVerticale;
	private String building;
	private String result;
	private String versioneStaccata;
	private boolean buildingWhenCreated;

	public SlaveDaemon(String myJobUrl, String myJobNumber) {
		this.myJobUrl = myJobUrl + "api/xml";
		this.myJobNumber=myJobNumber;
	}

	@Override
	public void run() {

		try {
			while(true) {
				String slaveContent = JenkinsClient.getContent(myJobUrl, null);

				Document doc = Jsoup.parse(slaveContent);
				Elements displayNameElement = Jsoup.parse(slaveContent).getElementsByTag("displayName");
				Elements buildingElement = Jsoup.parse(slaveContent).getElementsByTag("building");
				Elements resultElement = Jsoup.parse(slaveContent).getElementsByTag("result");
				Elements descriptionElement = Jsoup.parse(slaveContent).getElementsByTag("description");

				nomeVerticale = displayNameElement.size() > 0 && displayNameElement.get(0).html().contains("-") ? displayNameElement.get(0).html().substring(displayNameElement.get(0).html().indexOf("-")+2) : null;
				building = buildingElement.size() > 0 ? StringUtils.trim(buildingElement.get(0).html()): "non ho lo status build" + " ";
				result=resultElement.size() > 0 ? resultElement.get(0).html(): " still building...";
				versioneStaccata = descriptionElement.size()>0 ? StringUtils.trim(descriptionElement.get(0).html().substring(descriptionElement.get(0).html().indexOf("Version:") + 9)) : null;

				if (!slaveContent.contains(MasterDaemon.branchDiLavoro) || (nomeVerticale != null && !MasterDaemon.elencoVerticali.contains(nomeVerticale))) {
					//ho trovato un job che non m interessa, ad es di un altro branch
					//oppure qualsiasi altra cosa dei nostri verticali
					if (!MasterDaemon.buildNonPertinenti.contains(myJobNumber)) {
						MasterDaemon.buildNonPertinenti.add(myJobNumber);
					}
					return;
				}
				if (!MasterDaemon.buildAttive.contains(myJobNumber) && nomeVerticale != null) {
					MasterDaemon.buildAttive.add(myJobNumber);
				}

				if(building.equalsIgnoreCase("false")) {
					//la build è completata, posso uscire
					MasterDaemon.buildAttive.remove(myJobNumber);
					MasterDaemon.buildSuccess.add(myJobNumber);
					return;
				}else {
					//voglio monitorare solo le build che sono state attivate da quando monitoro
					buildingWhenCreated = true;
				}

				//System.out.print(LocalTime.now() +"[#" +this.myJobNumber+"-" +this.nomeVerticale+ "] Still building " + this.versioneStaccata + " Build attive" + MasterDaemon.buildAttive.toString() + "\r");
				System.out.print(LocalTime.now() +"#" + "Build attive " + MasterDaemon.buildAttive.toString() + "\r");
				Thread.sleep(ThreadLocalRandom.current().nextInt(12000, 18000));
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}finally {

			if("SUCCESS".equals(result) && buildingWhenCreated){
					System.out.println();
					System.out.println(LocalTime.now() + " Lancio promotion di: #" + this.myJobNumber + " - "+ this.nomeVerticale +" " +this.versioneStaccata);
					String jsonData = JenkinsConstants.jsonData.replace("repositoryToChange", nomeVerticale).replace("imagetagToChange", versioneStaccata);
					try {
						JenkinsClient.getContent(JenkinsConstants.jeniknsBuildUrl,jsonData);
					} catch (IOException e) {

					}

			}
		}

	}
}
