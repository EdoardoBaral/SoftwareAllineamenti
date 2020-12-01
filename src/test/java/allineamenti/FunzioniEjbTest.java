package allineamenti;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;

public class FunzioniEjbTest {
	
	@Test
	public void checkoutEjbTest() throws Exception
	{
		boolean resultSvil = FunzioniEjb.checkoutEjb("D:\\Openshift\\EJB\\profilocliente-ejb", "env/svil");
		assertTrue(resultSvil);
		
		boolean resultSvia = FunzioniEjb.checkoutEjb("D:\\Openshift\\EJB\\profilocliente-ejb", "env/svia");
		assertTrue(resultSvia);
		
		boolean resultSvis = FunzioniEjb.checkoutEjb("D:\\Openshift\\EJB\\profilocliente-ejb", "env/svis");
		assertTrue(resultSvis);
		
		boolean resultFake = FunzioniEjb.checkoutEjb("D:\\Openshift\\EJB\\profilocliente-ejb", "Fake");
		assertFalse(resultFake);
	}
	
	@Test
	public void checkoutTuttiEjbTest() throws Exception
	{
		SetupApplication setupApplication = new SetupApplication();
		Map<String, List<String>> map = setupApplication.caricaListeEjb();
		
		List<String> ejbNonSwitchatiSvil = FunzioniEjb.checkoutTuttiEjb(map, "env/svil", "D:\\Openshift\\EJB");
		assertNotNull(ejbNonSwitchatiSvil);
		assertTrue(ejbNonSwitchatiSvil.isEmpty());
		System.out.println();
		
		List<String> ejbNonSwitchatiSvia = FunzioniEjb.checkoutTuttiEjb(map, "env/svia", "D:\\Openshift\\EJB");
		assertNotNull(ejbNonSwitchatiSvia);
		assertTrue(ejbNonSwitchatiSvia.isEmpty());
		System.out.println();
		
		List<String> ejbNonSwitchatiSvis = FunzioniEjb.checkoutTuttiEjb(map, "env/svis", "D:\\Openshift\\EJB");
		assertNotNull(ejbNonSwitchatiSvis);
		assertTrue(ejbNonSwitchatiSvis.isEmpty());
		System.out.println();
		
		List<String> ejbNonSwitchatiFake = FunzioniEjb.checkoutTuttiEjb(map, "fake", "D:\\Openshift\\EJB");
		assertNotNull(ejbNonSwitchatiFake);
		assertFalse(ejbNonSwitchatiFake.isEmpty());
	}
}
