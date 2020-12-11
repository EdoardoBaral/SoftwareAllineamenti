package allineamenti;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe che contiene le stringhe che rappresentano le parti di comandi Git utilizzati all'interno del programma per effettuare gli allineamenti e altre informazioni
 * di utilità generale.
 *
 * @author Edoardo Baral
 */

public class StringConstants
{
	/** Nomi dei branch utilizzati per gli EJB migrati e i verticali */
	static final String BRANCH_MASTER = "master";
	static final String BRANCH_SVIL = "env/svil";
	static final String BRANCH_SVIS = "env/svis";
	static final String BRANCH_SVIA = "env/svia";
	
	/** Parti dei comandi Git utilizzati per gli allineamenti, assemblati a seconda della necessità */
	static final String COMANDO_GIT_PULL = "git pull";
	static final String COMANDO_GIT_PULL_ORIGIN = "git pull origin ";
	static final String COMANDO_GIT_CHECKOUT = "git checkout ";
	static final String COMANDO_GIT_BRANCH = "git rev-parse --abbrev-ref HEAD";
	static final String COMANDO_GIT_COMMIT = "git commit";
	static final String COMANDO_GIT_COMMIT_EJB_VUOTO = "git commit --allow-empty -m \"Commit vuoto per ricompilazione EJB\"";
	static final String COMANDO_GIT_RELEASE = "git commit --allow-empty -m \"RELEASE\"";
	static final String COMANDO_GIT_TAG_PROMOTE = "git commit --allow-empty -m \"TAG-PROMOTE\"";
	static final String COMANDO_GIT_STATUS = "git status";
	static final String COMANDO_GIT_PUSH = "git push";
	
	/** Comandi principali del programma di allineamento */
	static final String ALLINEAMENTO_EJB = "ejb ";
	static final String ALLINEAMENTO_VERTICALI = "verticali ";
	static final String SOSTITUZIONE_AUTOMATICA = "sostituzione ";
	static final String ESCI = "exit";

	/** Inizializzazione delle hashmap contenenti i percorsi predefiniti delle cartelle degli EJB migrati e dei verticali */
	static final Map<String, String> PATH_EJB  = new HashMap<String, String>() {{
		put("edoejb", "D:\\Openshift\\EJB");
		put("felixejb", "D:\\GIT\\cdbp0\\toBeBuildEJB");
	}};
	
	static final Map<String, String> PATH_VERTICALI  = new HashMap<String, String>() {{
		put("edovert", "D:\\Openshift\\Verticali\\cdbp0");
		put("felixvert", "D:\\GIT\\cdbp0\\toBeBuild");
	}};

}
