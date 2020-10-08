package allineamenti;

import java.util.HashMap;
import java.util.Map;

public class StringConstants
{
	public static final String BRANCH_MASTER = "master";
	public static final String BRANCH_SVIL = "env/svil";
	public static final String BRANCH_SVIS = "env/svis";
	public static final String BRANCH_PTES = "env/ptes";
	public static final String BRANCH_SVIA = "env/svia";
	public static final String BRANCH_PTEA = "env/ptea";
	
	public static final String COMANDO_GIT_PULL = "git pull";
	public static final String COMANDO_GIT_PULL_ORIGIN = "git pull origin ";
	public static final String COMANDO_GIT_CHECKOUT = "git checkout ";
	public static final String COMANDO_GIT_BRANCH = "git rev-parse --abbrev-ref HEAD";
	public static final String COMANDO_GIT_COMMIT = "git commit";
	public static final String COMANDO_GIT_COMMIT_EJB_VUOTO = "git commit --allow-empty -m \"Commit vuoto per ricompilazione EJB\"";
	public static final String COMANDO_GIT_COMMIT_VERTICALE_VUOTO = "git commit --allow-empty -m \"Commit vuoto per ricompilazione verticale\"";
	public static final String COMANDO_GIT_RELEASE = "git commit --allow-empty -m \"RELEASE\"";
	public static final String COMANDO_GIT_TAG_PROMOTE = "git commit --allow-empty -m \"TAG-PROMOTE\"";
	public static final String COMANDO_GIT_STATUS = "git status";
	public static final String COMANDO_GIT_PUSH = "git push";
	
	public static final String ALLINEAMENTO_EJB = "ejb ";
	public static final String ALLINEAMENTO_VERTICALI = "verticali ";
	public static final String ESCI = "exit";

	public static final Map<String, String> PATH_EJB  = new HashMap<String, String>() {{
		put("edoejb", "D:\\Openshift\\EJB");
		put("fejb", "D:\\GIT\\cdbp0\\toBeBuildEJB");
	}};
	public static final Map<String, String> PATH_VERTICALI  = new HashMap<String, String>() {{
		put("edovert", "D:\\Openshift\\Verticali\\cdbp0");
		put("fvert", "D:\\GIT\\cdbp0\\toBeBuild");
	}};

}
