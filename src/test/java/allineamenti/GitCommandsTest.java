package allineamenti;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class GitCommandsTest {
	
	@Test
	public void gitCheckoutTest() throws Exception
	{
		boolean resultSvil = GitCommands.gitCheckout(StringConstants.COMANDO_GIT_CHECKOUT +"env/svil", "D:\\Openshift\\EJB\\profilocliente-ejb");
		assertTrue(resultSvil);

		boolean resultSvia = GitCommands.gitCheckout(StringConstants.COMANDO_GIT_CHECKOUT +"env/svia", "D:\\Openshift\\EJB\\profilocliente-ejb");
		assertTrue(resultSvia);

		boolean resultSvis = GitCommands.gitCheckout(StringConstants.COMANDO_GIT_CHECKOUT +"env/svis", "D:\\Openshift\\EJB\\profilocliente-ejb");
		assertTrue(resultSvis);
		
		boolean resultFake = GitCommands.gitCheckout(StringConstants.COMANDO_GIT_CHECKOUT +"fake", "D:\\Openshift\\EJB\\profilocliente-ejb");
		assertFalse(resultFake);
	}
	
	@Test
	public void gitPullTest() throws Exception
	{
		GitCommands.gitPull(StringConstants.COMANDO_GIT_PULL, "D:\\Openshift\\EJB\\profilocliente-ejb");
	}
}
