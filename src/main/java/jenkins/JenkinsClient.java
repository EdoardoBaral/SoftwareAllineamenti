package jenkins;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.codec.binary.Base64;

public class JenkinsClient {

	public static String getContent(String urlToCall, String postJson) throws IOException {
		String result = "KO";
		URL url;
		try {
			url = new URL(urlToCall);
			HttpURLConnection uc;
			uc = (HttpURLConnection) url.openConnection();

			uc.setRequestProperty("X-Requested-With", "Curl");

			String userpass = "usr" + ":" + "psw";
			String basicAuth = "Basic " + new String(new Base64().encode(userpass.getBytes()));
			uc.setRequestProperty("Authorization", basicAuth);

			if(postJson != null){
				uc.setUseCaches(false);
				uc.setDoOutput(true);
				uc.setRequestProperty("Content-Type", "application/json");
				uc.setRequestMethod("POST");

				DataOutputStream wr = new DataOutputStream (uc.getOutputStream());
				wr.writeBytes(postJson);
				wr.close();

			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			StringBuilder builder = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
				builder.append(System.getProperty("line.separator"));

			}
			result= builder.toString();


		} catch (IOException e) {
			return result;
		}
		return result;
	}
}

