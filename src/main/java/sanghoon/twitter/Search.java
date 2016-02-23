package sanghoon.twitter;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

@Deprecated
public class Search {
	
	// 아직 미완성

	public static void main(String[] args) throws TwitterException {
		
		
	    // The factory instance is re-useable and thread safe.
	    TwitterFactory factory = new TwitterFactory();
//	    AccessToken accessToken = loadAccessToken(Integer.parseInt(args[0]));
	    String token = "62974491-7h4iNn2IrgqMY72SwpmMIPIdRxwxXf4iKwm9GDUCM";// load from a persistent store
	    	    String tokenSecret = "YRDoW20nGbB8hWMz8zSnKK3HUuyheBOBVrwVlneMLdg";// load from a persistent store
	    	    		AccessToken accessToken = new AccessToken(token, tokenSecret);
	    
	    Twitter twitter = factory.getInstance();
	    twitter.setOAuthConsumer("O6WQYcmxoYXCaDH2krROHw", "VB0ATpjp1FdlyqDyzwemUbG5IAlP1awRawS45PMFXEw");
	    twitter.setOAuthAccessToken(accessToken);
	    
//	    Status status = twitter.updateStatus(args[1]);
//	    System.out.println("Successfully updated the status to [" + status.getText() + "].");
//	    System.exit(0);
	    
		
		 // The factory instance is re-useable and thread safe.
//	    Twitter twitter = TwitterFactory.getSingleton();
	    Query query = new Query("hi");
	    query.setLang("en");
	    final int maxCount = 100;
	    query.setCount(maxCount);
	    
	    query.setSince("2015-12-21");
	    query.setUntil("2015-12-22");
//	    query.setSinceId(499211329786220544L);
//	    query.setMaxId(499211329786220544L);
//	    query.setSince("699211329786220544");
//	    query.setMaxId("0");
	    
	    QueryResult result = twitter.search(query);
	    System.out.println("count: " + result.getCount());
	    System.out.println("since id: " + result.getSinceId());
	    System.out.println("max id: " + result.getMaxId());
	    
	    for (Status status : result.getTweets()) {
	        System.out.println(status.getId() + " " +
	        		status.getCreatedAt()
	    
	        		+ " @" + status.getUser().getScreenName() + ":" + status.getText());
	    }
	    
	    
	}

}
