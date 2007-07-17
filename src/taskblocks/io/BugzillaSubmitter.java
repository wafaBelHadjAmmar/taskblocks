package taskblocks.io;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Instances of this class can submit new bugs to Bugzilla.
 * It uses it's bug_submit cgi script to submit new bug. The output html page is parsed 
 * to recognize the status of the operation.
 * 
 * @author j.neubauer
 */
public class BugzillaSubmitter {

	/** Bug property name */
	public static final String KEYWORDS = "keywords";

	/** Bug property name */
	public static final String PRODUCT = "product";

	/** Bug property name */
	public static final String VERSION = "version";

	/** Bug property name */
	public static final String COMPONENT = "component";

	/** Bug property name */
	public static final String HARDWARE = "rep_platform";

	/** Bug property name */
	public static final String OS = "op_sys";

	/** Bug property name */
	public static final String PRIORITY = "priority";

	/** Bug property name */
	public static final String SEVERITY = "bug_severity";

	/**
	 * Bug property name Probably supported from bugzilla version 3.0, bug only
	 * NEW and ASSIGNED values
	 */
	public static final String STATUS = "bug_status";

	/** Bug property name */
	public static final String ASSIGNED_TO = "assigned_to";

	/** Bug property name */
	public static final String SUMMARY = "short_desc";

	/** Bug property name */
	public static final String DESCRIPTION = "comment";

	/** Bug property name */
	public static final String ESTIMATED_TIME = "estimated_time";

	/** Bug property name */
	public static final String BLOCKS = "blocked";

	/** Must be enabled on bugzilla server */
	public static final String STATUS_WHITEBOARD = "status_whiteboard";

	/**
	 * Regular expression used to parse output from bugzilla and to find the submitted bug id.
	 * if not found, it is supposed that error occured.
	 */
	public String _successRegexp = "Bug ([0-9]+) Submitted";
	
	/**
	 * Regular expression used to find title of the error if submission doesn't
	 * success. By default, it is the title of the web page
	 */
	public String _errTitleRegexp = "<title>(.*)</title>";

	/**
	 * Regular expression used to find description of error if submission doesn't
	 * success. This one retrieves the main body of the page.
	 */
	public String _errDetailRegexp = "<div id=\"bugzilla-body\">(.*)</div>.*?<div id=\"footer\">";

	/** Regular expressions used to clean the detail error message. */
	public String[] _errDetailRemovalRegexps = new String[] {
			"(?s)<script.*?</script>",
			"(?s)<div id=\"docslinks\">.*?</div>"
	};

	/**
	 * encodes a form data from the given key-value pairs.
	 * 
	 * @param formData
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private static String buildFormBody(Map<String, String> formData)
			throws UnsupportedEncodingException {
		StringBuilder body = new StringBuilder();
		int count = 0;
		for (Map.Entry<String, String> e : formData.entrySet()) {
			if (count > 0) {
				body.append("&");
			}
			body.append(URLEncoder.encode(e.getKey(), "UTF-8"));
			body.append("=");
			body.append(URLEncoder.encode(e.getValue(), "UTF-8"));
			count++;
		}
		return body.toString();
	}

	/**
	 * Submits the given body with POST method to specified url
	 * 
	 * @param url must be http protocol
	 * @param body
	 * @return http reply data
	 * @throws IOException
	 */
	private String submit(URL url, String body) throws IOException {

		StringBuffer sb = new StringBuffer();
		String newline = "\n";

		// URL must use the http protocol!
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setAllowUserInteraction(false); // you may not ask the user
		conn.setDoOutput(true); // we want to send things
		// the Content-type should be default, but we set it anyway
		conn.setRequestProperty("Content-type",
				"application/x-www-form-urlencoded; charset=utf-8");
		// the content-length should not be necessary, but we're cautious
		conn.setRequestProperty("Content-length", Integer.toString(body.length()));

		// get the output stream to POST our form data
		OutputStream rawOutStream = conn.getOutputStream();
		PrintWriter pw = new PrintWriter(rawOutStream);

		pw.print(body); // here we "send" our body!
		pw.flush();
		pw.close();

		// get the input stream for reading the reply
		// IMPORTANT! Your body will not get transmitted if you get the
		// InputStream before completely writing out your output first!
		InputStream rawInStream = conn.getInputStream();

		// get response
		BufferedReader rdr = new BufferedReader(new InputStreamReader(rawInStream));
		String line;

		while ((line = rdr.readLine()) != null) {
			sb.append(line);
			sb.append(newline);
		}

		conn.disconnect();
		return sb.toString();
	}

	private void ensureDefault(Map<String, String> map, String key,
			String defaultValue) {
		if (!map.containsKey(key)) {
			map.put(key, defaultValue);
		}
	}

	/**
	 * Submits new bug to bugzilla server running at specified url.
	 * If bugzilla returns error page, and exception is thrown with error message
	 * extracted by parsing the result html page with regular expressions
	 * {@link #_errTitleRegexp}, {@link #_errDetailRegexp} and {@link #_errDetailRemovalRegexps}.
	 * Bug submission success is recognized by parsing output and finding bug id with
	 * regular expressiont {@link #_successRegexp}.
	 * 
	 * 
	 * @param baseUrl
	 *          base url of bugzilla server
	 * @param user
	 *          user name for authentication
	 * @param password
	 *          password for authentication
	 * @param properties
	 *          properties of new bug. Use constants in this class as keys.
	 * @return submitted bug id.
	 *
	 * @throws IOException if connection error occures
	 * @throws Exception in other cases. If connection was successfull, error messages are
	 * extracted from the html page.
	 */
	public String submit(String baseUrl, String user, String password,
			Map<String, String> properties) throws Exception {

		// fill in default values
		ensureDefault(properties, STATUS, "NEW");
		ensureDefault(properties, SEVERITY, "normal");
		ensureDefault(properties, PRIORITY, "P2");
		ensureDefault(properties, "bug_file_loc", "http://");

		// authentication
		properties.put("form_name", "enter_bug");
		properties.put("Bugzilla_login", user);
		properties.put("Bugzilla_password", password);
		properties.put("GoAheadAndLogIn", "1");

		String formBody = buildFormBody(properties);
		String result = submit(new URL(baseUrl + "/post_bug.cgi"), formBody);
		// System.out.println(result);

		Matcher m = Pattern.compile(_successRegexp).matcher(result);
		if (m.find()) {
			String bugId = m.group(1);
			return bugId;
		} else {

			String errText = "";
			m = Pattern.compile(_errTitleRegexp).matcher(result);
			if (m.find()) {
				errText = m.group(1);
			}

			String errText2 = "";
			m = Pattern.compile(_errDetailRegexp, Pattern.DOTALL).matcher(result);
			if (m.find()) {
				errText2 = m.group(1);
			}
			if (errText2.length() > 0) {
				for (String removeRegexp : _errDetailRemovalRegexps) {
					errText2 = errText2.replaceAll(removeRegexp, "");
				}
				errText2 = errText2.replaceAll("<[^>]*>", "");
				errText2 = errText2.replaceAll("\r?\n", " ");
				errText2 = errText2.replaceAll(" +", " ");
			}
			throw new Exception(errText + ": " + errText2);
		}
	}

	public static void main(String[] args) {
		Map<String, String> formData = new HashMap<String, String>();

		formData.put(KEYWORDS, "Plan");
		formData.put(PRODUCT, "TestProduct");
		formData.put(VERSION, "unspecified");
		formData.put(COMPONENT, "TestComponent");
		formData.put(HARDWARE, "All");
		formData.put(OS, "All");
		formData.put(PRIORITY, "P2");
		formData.put(SEVERITY, "enhancement");
		formData.put(STATUS, "NEW");
		formData.put(ASSIGNED_TO, "j.neubauer@cz.gmc.net");
		formData.put(SUMMARY, "Test Bug");
		formData.put(DESCRIPTION, "Description of Test Bug");
		formData.put(ESTIMATED_TIME, "16");
		formData.put(STATUS_WHITEBOARD, "230407-240407");
		formData.put(BLOCKS, "1,2");

		try {
			String bugId = new BugzillaSubmitter().submit(
					"http://jakubpc/bugzilla-3.0", "j.neubauer@cz.gmc.net", "heslo",
					formData);
			System.out.println("Submitted bug #" + bugId);
		} catch (UnknownHostException e) {
			System.err.println("Unknown host: " + e.getMessage());
		} catch (FileNotFoundException e) {
			System.err.println("Page not found: " + e.getMessage());
		} catch (IOException e) {
			System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage());
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(2);
		}
	}
}
