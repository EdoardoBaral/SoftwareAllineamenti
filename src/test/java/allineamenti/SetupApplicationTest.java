package allineamenti;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class SetupApplicationTest
{
	@Test
	public void caricaListeEjbTest() throws IOException
	{
		SetupApplication setupApplication = new SetupApplication();
		Map<String, List<String>> map = setupApplication.caricaListeEjb();
		
		assertNotNull(map);
		assertTrue(map.size() > 0);
	}
	
	@Test
	public void caricaListaVerticaliTest() throws IOException
	{
		SetupApplication setupApplication = new SetupApplication();
		List<String> listaVerticali = setupApplication.caricaListaVerticali();
		
		assertNotNull(listaVerticali);
		assertTrue(!listaVerticali.isEmpty());
	}
}
