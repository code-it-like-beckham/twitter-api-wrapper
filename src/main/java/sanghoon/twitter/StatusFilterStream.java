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

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

public class StatusFilterStream {

	private static void usage() {
		System.err.println("usage: java " + StatusFilterStream.class.getName() + " [options] <output-dir>");
		System.err.println();
		System.err.println("Returns public statuses that match one or more filter predicates.");
		System.err.println("Multiple parameters may be specified which allows most clients");
		System.err.println("to use a single connection to the Streaming API.");
//		System.err.println("Both GET and POST requests are supported, but GET requests with too many parameters may cause the request to be rejected for excessive URL length. Use a POST request to avoid long URLs.
		System.err.println();
		System.err.println("-t <track>");
		System.err.println("    Keywords to track.");
		System.err.println("    Phrases of keywords are specified by a comma-separated list.");
		System.err.println("    For more information, see");
		System.err.println("    https://dev.twitter.com/streaming/overview/request-parameters#track");
		System.err.println();
		System.err.println("-l <locations>");
		System.err.println("    Specifies a set of bounding boxes to track.");
		System.err.println("    A comma-separated list of longitude,latitude pairs");
		System.err.println("	specifying a set of bounding boxes to filter Tweets by.");
		System.err.println("	Only geolocated Tweets falling within the requested");
		System.err.println("	bounding boxes will be included—unlike the Search API,");
		System.err.println("	the user’s location field is not used to filter tweets.");
		System.err.println("    e.g., -122.75,36.8,-121.75,37.8 for San Francisco");
//		System.err.println("     -122.75,36.8,-121.75,37.8,-74,40,-73,41 for San Francisco OR New York City");
		System.err.println("    For more information, see");
		System.err.println("    https://dev.twitter.com/streaming/overview/request-parameters#locations");
		System.err.println();
		System.err.println("https://dev.twitter.com/streaming/reference/post/statuses/filter");
		System.err.println();
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
		
		
		String[] track = null;
		double[][] locations = null;
		
		int argsIndex = 0;
		while (argsIndex < args.length - 1) {
			
			if ("-l".equals(args[argsIndex])) {

				String[] fields = args[argsIndex + 1].split(",", 4);
				if (fields.length < 4) {
					System.err.println("invalid locations parameter");
					return;
				}
				
				double minimum_longitude = Double.parseDouble(fields[0]);
				double minimum_latitude = Double.parseDouble(fields[1]);
				double maximum_longitude = Double.parseDouble(fields[2]);
				double maximum_latitude = Double.parseDouble(fields[3]);
				
				locations = new double[][] {
					{ minimum_longitude, minimum_latitude },
					{ maximum_longitude, maximum_latitude },
				};
				
				argsIndex += 2;
				
			} else if ("-t".equals(args[argsIndex])) {
			
				track = args[argsIndex + 1].split(",");
				
				argsIndex += 2;
			}
		}
		
		if (argsIndex >= args.length) {
			System.err.println("output-dir parameter is not specified");
			return;
		}

		final File outputDir = new File(args[argsIndex]);
		
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
				System.out.println(statusJson.toString());
				
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
		
		FilterQuery query = new FilterQuery();
		
		if (track != null)
			query.track(track);

		if (locations != null)
			query.locations(locations);
			
		query.language(new String[]{ "en" });
		
		twitterStream.filter(query);
	}

}
