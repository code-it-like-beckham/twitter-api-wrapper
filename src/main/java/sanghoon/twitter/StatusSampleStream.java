package sanghoon.twitter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

public class StatusSampleStream {
	
	private static void usage() {
		System.err.println("usage: java " + StatusSampleStream.class.getName() + " <output-dir>");
		System.err.println();
		System.err.println("Returns a small random sample of all public statuses.");
		System.err.println("    The Tweets returned by the default access level are the same,");
		System.err.println("    so if two different clients connect to this endpoint,");
		System.err.println("    they will see the same Tweets.");
		System.err.println();
		System.err.println("https://dev.twitter.com/streaming/reference/get/statuses/sample");
	}
	
	private static String getOutputFilename() {
		final SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd_HH");
		return df.format(new Date()) + ".gz";
	}

	public static void main(String[] args) throws TwitterException, FileNotFoundException, IOException {
		
		if (args.length < 1) {
			usage();
			return;
		}
		
		final File outputDir = new File(args[0]);
		
		if (outputDir.exists() == false)
			outputDir.mkdirs();
		
		TwitterStream twitterStream = new TwitterStreamFactory().getInstance();

		StatusListener listener = new StatusListener() {

			PrintWriter writer = null;
			String prevOutputFilename = null;
			
			int numTweetsInCurrentFile = 0;
			
			public void onStatus(Status status) {
				
				String outputFilename = getOutputFilename();
				
				if (writer == null ||
						outputFilename.equals(prevOutputFilename) == false) {
					
					if (writer != null) {
						writer.close();
						System.out.println("#tweets=" + numTweetsInCurrentFile + " in " + prevOutputFilename);
					}
					
					try {
						File outputFile = new File(outputDir, outputFilename);
						FileOutputStream os = new FileOutputStream(outputFile, true);
						GZIPOutputStream zip = new GZIPOutputStream(os);
						writer = new PrintWriter(new OutputStreamWriter(zip, "UTF-8"));
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				String statusJson = TwitterObjectFactory.getRawJSON(status);
				
				writer.println(statusJson.toString());
//				System.out.println(statusJson.toString());
				
				numTweetsInCurrentFile++;

				prevOutputFilename = outputFilename;
			}

			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
			}

			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
			}

			public void onScrubGeo(long userId, long upToStatusId) {
			}

			public void onStallWarning(StallWarning warning) {
			}

			public void onException(Exception ex) {
			}

		};

		twitterStream.addListener(listener);
		twitterStream.sample("en");
	}

}
