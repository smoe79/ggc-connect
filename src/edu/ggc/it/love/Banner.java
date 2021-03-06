package edu.ggc.it.love;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLPeerUnverifiedException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;

import edu.ggc.it.R;

import android.content.Context;
import android.util.Log;

public class Banner {
	private static final String BANNER_URL = "https://ggc.gabest.usg.edu";
	private static final String TAG = "BannerInterface";
	private final BannerForm P_GetCrse =
			new BannerForm("/pls/B400/bwskfcls.P_GetCrse", true,
					new NameValuePair[]{
					new BasicNameValuePair("rsts", "dummy"),
					new BasicNameValuePair("crn", "dummy"),
					new BasicNameValuePair("term_in", "201302"),
					new BasicNameValuePair("sel_subj", "dummy"),
					new BasicNameValuePair("sel_day", "dummy"),
					new BasicNameValuePair("sel_schd", "dummy"),
					new BasicNameValuePair("sel_insm", "dummy"),
					new BasicNameValuePair("sel_camp", "dummy"),
					new BasicNameValuePair("sel_levl", "dummy"),
					new BasicNameValuePair("sel_sess", "dummy"),
					new BasicNameValuePair("sel_instr", "dummy"),
					new BasicNameValuePair("sel_attr", "dummy"),
					new BasicNameValuePair("sel_crse", ""),
					new BasicNameValuePair("sel_title", ""),
					new BasicNameValuePair("sel_from_cred", ""),
					new BasicNameValuePair("sel_to_cred", ""),
					new BasicNameValuePair("sel_ptrm", "%"),
					new BasicNameValuePair("begin_hh", "0"),
					new BasicNameValuePair("begin_mi", "0"),
					new BasicNameValuePair("end_hh", "0"),
					new BasicNameValuePair("end_mi", "0"),
					new BasicNameValuePair("begin_ap", "x"),
					new BasicNameValuePair("end_ap", "y"),
					new BasicNameValuePair("path", "1"),
					new BasicNameValuePair("SUB_BTN", "Course Search")
			});
	private final BannerForm p_display_courses =
			new BannerForm("/pls/B400/bwckctlg.p_display_courses", false,
					new NameValuePair[]{
					new BasicNameValuePair("term_in", "201302"),
					new BasicNameValuePair("one_subj", ""),
					new BasicNameValuePair("sel_crse_strt", "0000"),
					new BasicNameValuePair("sel_crse_end", "9999"),
					new BasicNameValuePair("sel_subj", ""),
					new BasicNameValuePair("sel_coll", ""),
					new BasicNameValuePair("sel_schd", ""),
					new BasicNameValuePair("sel_divs", ""),
					new BasicNameValuePair("sel_dept", ""),
					new BasicNameValuePair("sel_levl", ""),
					new BasicNameValuePair("sel_attr", ""),
			});
	private final BannerForm p_disp_listcrse =
			new BannerForm("/pls/B400/bwckctlg.p_disp_listcrse", false,
					new NameValuePair[]{
					new BasicNameValuePair("term_in", "201302"),
					new BasicNameValuePair("subj_in", ""),
					new BasicNameValuePair("crse_in", ""),
					new BasicNameValuePair("schd_in", "")
			});
	private Context context;
	
	public Banner(Context context){
		this.context = context;
	}
	
	public Map<String, String> getCourseNumbers(String subject){
		p_display_courses.set("one_subj", subject);
		BufferedReader response = p_display_courses.request();
		Map<String, String> courses = new HashMap<String, String>();
		List<String> titles = scrapeInner(response, "A", "/pls/B400/bwckctlg.p_disp_course_detail?");
		
		for (int i = 0; i < titles.size(); i++){
			String href = titles.get(i);
			int numStart = href.indexOf(" ")+1;
			int numEnd = href.indexOf(" ", numStart);
			String number = href.substring(numStart, numEnd);
			int nameStart = href.indexOf(" ", numEnd+1)+1;
			String name = href.substring(nameStart);
			courses.put(number, name);
		}
		
		try{
			response.close();
		} catch (IOException ioe){
			Log.w(TAG, "Failed to close HTML stream", ioe);
		}
		return courses;
	}
	
	public Map<String, String> getSections(String subject, String course){
		final String separator = " - ";
		
		p_disp_listcrse.set("subj_in", subject);
		p_disp_listcrse.set("crse_in", course);
		BufferedReader response = p_disp_listcrse.request();
		Map<String, String> sections = new HashMap<String, String>();
		List<String> titles = scrapeInner(response, "A", "/pls/B400/bwckschd.p_disp_detail_sched?");
		
		for (int i = 0; i < titles.size(); i++){
			String href = titles.get(i);
			int crnStart = href.indexOf(separator)+separator.length();
			int crnEnd = href.indexOf(separator, crnStart);
			String crn = href.substring(crnStart, crnEnd);
			int sectStart = href.indexOf(separator, crnEnd+separator.length())+separator.length();
			String sectionId = href.substring(sectStart);
			sections.put(sectionId, crn);
		}
		
		try{
			response.close();
		} catch (IOException ioe){
			Log.w(TAG, "Failed to close HTML stream", ioe);
		}
		return sections;
	}
	
	private List<String> scrapeInner(BufferedReader html, String tag, String... matches){
		List<String> results = new ArrayList<String>();
		String matched = null, line = null;
		int begin = -1, end = -1;
		boolean inTag = false;
		
		// TODO: this will skip multiple matching tags on the same line
		try{
			searchHTML:
			while ((line = html.readLine()) != null){
				if (matched == null){
					begin = line.indexOf("<" + tag);
					if (begin != -1){
						end = line.indexOf(">", begin);
						if (end != -1)
							matched = line.substring(begin, end);
						else
							matched = line.substring(begin);
					}
				} else{
					if (inTag)
						end = line.indexOf("</" + tag);
					else
						end = line.indexOf(">");
					if (end == -1){
						matched += line;
					} else{
						matched += line.substring(0, end);
						if (inTag){
							inTag = false;
							results.add(matched);
							matched = null;
							end = -1;
						}
					}
				}
				
				if (begin != -1 && end != -1){
					// we found a complete tag
					for (String match: matches){
						if (matched.indexOf(match) == -1){
							matched = null;
							continue searchHTML;
						}
					}
					
					// it matched the requirements; grab the inner HTML
					inTag = true;
					matched = line.substring(end+1);
					end = matched.indexOf("</" + tag);
					if (end != -1){
						matched = matched.substring(0, end);
						inTag = false;
						results.add(matched);
						matched = null;
						end = -1;
					}
					begin = -1;
				}
			}
		} catch (IOException ioe){
			Log.e(TAG, "Failed to read response stream", ioe);
			return null;
		}
		
		return results;
	}
	
	private List<String> scrapeAttr(BufferedReader html, String tag, String attr,
			String... matches){
		List<String> results = new ArrayList<String>();
		String matched = null, line = null;
		int begin = -1, end = -1;
		
		// TODO: this will skip multiple matching tags on the same line
		try{
			searchHTML:
			while ((line = html.readLine()) != null){
				if (matched == null){
					begin = line.indexOf("<" + tag);
					if (begin != -1){
						end = line.indexOf(">", begin);
						if (end != -1)
							matched = line.substring(begin, end);
						else
							matched = line.substring(begin);
					}
				} else{
					end = line.indexOf(">");
					if (end == -1)
						matched += line;
					else
						matched += line.substring(0, end);
				}
				
				if (begin != -1 && end != -1){
					// we found a complete tag
					begin = -1;
					end = -1;
					
					for (String match: matches){
						if (matched.indexOf(match) == -1){
							matched = null;
							continue searchHTML;
						}
					}
					
					// it matched the requirements; grab the attribute value
					int attrLoc = matched.indexOf(attr);
					if (attrLoc == -1){
						matched = null;
						continue;
					}
					int eqLoc = matched.indexOf("=", attrLoc);
					if (eqLoc == -1){
						matched = null;
						Log.w(TAG, "Malformed attribute: " + matched);
						continue;
					}
					// attributes can start with single quotes or double quotes, so search for both;
					// whichever is first after the equals sign is the one they're using
					int sqLoc = matched.indexOf("'", eqLoc);
					int dqLoc = matched.indexOf("\"", eqLoc);
					int valStart = 0;
					
					if (sqLoc < dqLoc && sqLoc != -1){
						valStart = sqLoc+1;
					} else if (dqLoc < sqLoc && dqLoc != -1){
						valStart = dqLoc+1;
					} else{
						matched = null;
						Log.w(TAG, "Malformed attribute: " + matched);
						continue;
					}
					
					int valEnd = matched.indexOf("'", valStart)-1;
					String value = matched.substring(valStart, valEnd);
					results.add(value);
				}
			}
		} catch (IOException ioe){
			Log.e(TAG, "Failed to read response stream", ioe);
			return null;
		}
		
		return results;
	}
	
	// Android doesn't trust Banner's certificate, so to do a request, we have to implement
	// our own client class. This code essentially copied from:
	// http://blog.crazybob.org/2010/02/android-trusting-ssl-certificates.html
	// Android >=2.3.3 does appear to trust the Banner cert
	/*private static class BannerHttpClient extends DefaultHttpClient{
		private final Context context;
		private static final String STORE_PASS = "i23rFJ@Qf0#";
		
		public BannerHttpClient(Context context){
			this.context = context;
		}
		
		@Override
		protected ClientConnectionManager createClientConnectionManager(){
			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			registry.register(new Scheme("https", newSslSocketFactory(), 443));
			return new SingleClientConnManager(getParams(), registry);
		}

		private SocketFactory newSslSocketFactory() {
			try{
				KeyStore trusted = KeyStore.getInstance("BKS");
				InputStream in = context.getResources().openRawResource(R.raw.banner);
				try{
					trusted.load(in, STORE_PASS.toCharArray());
					return new SSLSocketFactory(trusted);
				} catch (CertificateException ce){
					Log.e(TAG, "Problem with keystore certificate", ce);
				} catch (NoSuchAlgorithmException nsae) {
					Log.e(TAG, "Algorithm exception", nsae);
				} catch (IOException ioe) {
					Log.e(TAG, "Error reading keystore file", ioe);
				} catch (KeyManagementException kme) {
					Log.e(TAG, "Key management exception", kme);
				} catch (UnrecoverableKeyException uke) {
					Log.e(TAG, "Keystore corrupted", uke);
				} finally{
					in.close();
				}
			} catch (KeyStoreException kse){
				Log.e(TAG, "Failed to create KeyStore", kse);
			} catch (IOException ioe){
				Log.e(TAG, "Failed to close keystore input stream", ioe);
			}
			
			return null;
		}
	}*/
	
	private class BannerForm{
		private String path;
		private NameValuePair[] defaultParms;
		private Map<String, String> currentParms;
		private boolean isPOST;
		
		public BannerForm(String path, boolean isPOST, NameValuePair... parms){
			this.path = path;
			this.isPOST = isPOST;
			defaultParms = parms;
			currentParms = new HashMap<String, String>();
			for (NameValuePair parm: parms)
				currentParms.put(parm.getName(), parm.getValue());
		}
		
		public boolean set(String name, String value){
			if (!currentParms.containsKey(name))
				return false;
			currentParms.put(name, value);
			return true;
		}
		
		public boolean set(NameValuePair parm){
			if (!currentParms.containsKey(parm.getName()))
				return false;
			currentParms.put(parm.getName(), parm.getValue());
			return true;
		}
		
		public BufferedReader request(){
			HttpClient client = new DefaultHttpClient();//BannerHttpClient(context);
			HttpUriRequest request = null;
			BufferedReader ret = null;
			
			try{
				if (isPOST){
					HttpPost post = new HttpPost(BANNER_URL + path);
					List<NameValuePair> parms = new ArrayList<NameValuePair>(defaultParms.length);
					for (Map.Entry<String, String> parm: currentParms.entrySet())
						parms.add(new BasicNameValuePair(parm.getKey(), parm.getValue()));
					post.setEntity(new UrlEncodedFormEntity(parms));
					request = post;
				} else{
					String requestPath = BANNER_URL + path + "?";
					for (Map.Entry<String, String> parm: currentParms.entrySet())
						requestPath += parm.getKey() + "=" + parm.getValue() + "&";
					HttpGet get = new HttpGet(requestPath);
					request = get;
				}
				HttpResponse response = null;
				response = client.execute(request);
				HttpEntity entity = response.getEntity();
				InputStream stream = entity.getContent();
				Header encoding = entity.getContentEncoding();
				ret = new BufferedReader(new InputStreamReader(stream,
						(encoding == null? "iso-8859-1": encoding.getValue())));
			} catch (UnsupportedEncodingException uee){
				Log.e(TAG, "Failed to send Banner request", uee);
			} catch (ClientProtocolException cpe) {
				Log.e(TAG, "Failed to send Banner request", cpe);
			} catch (IOException ioe) {
				Log.e(TAG, "Failed to send Banner request", ioe);
			} finally{
				for (NameValuePair parm: defaultParms)
					currentParms.put(parm.getName(), parm.getValue());
			}
			
			return ret;
		}
	}
}
