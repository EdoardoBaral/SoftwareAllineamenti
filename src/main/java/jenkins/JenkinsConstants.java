package jenkins;

public class JenkinsConstants {
	public static String jenkinsUrl="https://jenkins-master2-cbjk0-prod.cloudapps.intesasanpaolo.com/job/cdbp0/job/Application/api/xml";
	public static String jeniknsBuildUrl = "https://jenkins-master2-cbjk0-prod.cloudapps.intesasanpaolo.com/blue/rest/organizations/jenkins/pipelines/_UTILITY-BITBUCKET/pipelines/promotion-parallel-env/runs/";
	public static String jsonData = "{\"parameters\":[{\"name\":\"platform\",\"value\":\"Openshift\"},{\"name\":\"acronimo\",\"value\":\"cdbp0\"},{\"name\":\"repository\",\"value\":\"repositoryToChange\"},{\"name\":\"imagetag\",\"value\":\"imagetagToChange\"},{\"name\":\"environment\",\"value\":\"environmentToChange\"}]}";

}
