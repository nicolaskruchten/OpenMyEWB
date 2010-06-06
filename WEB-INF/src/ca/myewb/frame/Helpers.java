/*

    This file is part of OpenMyEWB.

    OpenMyEWB is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    OpenMyEWB is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with OpenMyEWB.  If not, see <http://www.gnu.org/licenses/>.

    OpenMyEWB is Copyright 2005-2009 Nicolas Kruchten (nicolas@kruchten.com), Francis Kung, Engineers Without Borders Canada, Michael Trauttmansdorff, Jon Fishbein, David Kadish

*/

package ca.myewb.frame;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;
import org.radeox.api.engine.RenderEngine;
import org.radeox.api.engine.context.InitialRenderContext;
import org.radeox.api.engine.context.RenderContext;
import org.radeox.engine.BaseRenderEngine;
import org.radeox.engine.context.BaseInitialRenderContext;
import org.radeox.engine.context.BaseRenderContext;

import ca.myewb.model.DailyStatsModel;
import ca.myewb.model.GroupModel;


public class Helpers
{
	private static String localRoot;
	private static String appPrefix;
	private static String defaultURL;
	private static String domain;
	private static boolean devMode = false;
	private static DecimalFormat decimalFormat = new DecimalFormat("$###,###.00");
	private static DecimalFormat dashboardFormat = new DecimalFormat("###,###,###,##0.##");
	private static SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, MMM d, yyyy");
	private static SimpleDateFormat timeFormatter = new SimpleDateFormat("h:mm a");
	private static SimpleDateFormat postDateFormatter = new SimpleDateFormat("MMM d, yyyy");
	private static SimpleDateFormat postDateFormatterNoYear = new SimpleDateFormat("EEE, MMM d");
	private static SimpleDateFormat postTimeFormatter = new SimpleDateFormat("h:mm a z");
	private static SimpleDateFormat sqlFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat rfcFormatter = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z");
	private static SimpleDateFormat iCalDateTimeFormatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
	private static ConcurrentHashMap<String, Integer> adminGroupIDs = new ConcurrentHashMap<String, Integer>(30);
	private static String frontPageCache = null;
	private static Calendar cacheCal = null;
	private static String longName;
	private static String enShortName;
	private static String frShortName;
	private static String systemEmail;
	
	public Helpers()
	{
		//empty constructor, so we can stick one of these in the Velocity context
	}
	
	
	public static void setLocalRoot(String localRoot)
	{
		Helpers.localRoot = localRoot;
	}

	public static String getLocalRoot()
	{
		return localRoot;
	}

	public static String getUserFilesDir()
	{
		return localRoot + "/userfiles/";
	}

	public static String getUserPicturesDir()
	{
		return localRoot + "/userpictures/";
	}

	public static String formatDollarAmount(float amount)
	{
		return 	decimalFormat.format(amount);
	}
	
	public static boolean isNull(Object a)
	{
		return a == null;
	}
	
	public static String formatDashboardNumber(Number amount)
	{
		return 	dashboardFormat.format(amount);
	}

	public static String formatRFCDate(Date date) {
		return rfcFormatter.format(date);
	}
	
	public static String formatDateAndTime(Date date)
	{
		return Helpers.formatDate(date) + " at " + timeFormatter.format(date);
	}
	
	public static String formatAbsDate(Date date)
	{
		return dateFormatter.format(date);
	}
	

	public static String formatSQLDate(Date date)
	{
		return sqlFormatter.format(date);
	}
	
	public static String formatDate(Date date)
	{
		if (date == null)
		{
			return "never";
		}
		
		GregorianCalendar today = new GregorianCalendar();
		today.setTime(new Date());
		today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 1);

		GregorianCalendar yesterday = (GregorianCalendar)today.clone();
		yesterday.add(Calendar.DAY_OF_YEAR, -1);
		yesterday.set(Calendar.HOUR_OF_DAY, 0);
		yesterday.set(Calendar.MINUTE, 0);
		yesterday.set(Calendar.SECOND, 1);

		GregorianCalendar tomorrow = (GregorianCalendar)today.clone();
		tomorrow.add(Calendar.DAY_OF_YEAR, 1);
		tomorrow.set(Calendar.HOUR_OF_DAY, 0);
		tomorrow.set(Calendar.MINUTE, 0);
		tomorrow.set(Calendar.SECOND, 1);

		GregorianCalendar dayAfterTomorrow = (GregorianCalendar)today.clone();
		dayAfterTomorrow.add(Calendar.DAY_OF_YEAR, 2);
		dayAfterTomorrow.set(Calendar.HOUR_OF_DAY, 0);
		dayAfterTomorrow.set(Calendar.MINUTE, 0);
		dayAfterTomorrow.set(Calendar.SECOND, 1);

		GregorianCalendar dayOfDate = new GregorianCalendar();
		dayOfDate.setTime(date);
		dayOfDate.set(Calendar.HOUR_OF_DAY, 0);
		dayOfDate.set(Calendar.MINUTE, 0);
		dayOfDate.set(Calendar.SECOND, 2);

		if (dayOfDate.before(yesterday)
		    || dayOfDate.after(dayAfterTomorrow))
		{
			return "on " + dateFormatter.format(date);
		}
		else if (dayOfDate.before(today))
		{
			return "yesterday";
		}
		else if (dayOfDate.before(tomorrow))
		{
			return "today";
		}
		else
		{
			return "tomorrow";
		}
	}

	public static String formatPostDateTime(Date date)
	{
		return Helpers.formatPostDateTime(date, true);
	}
	
	public static String formatPostDateTime(Date date, boolean showTime)
	{
		if (date == null)
		{
			return "never";
		}
		
		long dMillis = ((new Date()).getTime() - date.getTime());
		if (dMillis < 0)
		{
			return "in the future";
		}

		long dMinutes = dMillis/(1000*60);
		if(showTime)
		{	
			if(dMinutes < 30)
			{
				return "a few minutes ago";
			}
			else if(dMinutes < 60)
			{
				return "half an hour ago";
			}
			else if(dMinutes < 120)
			{
				return "an hour ago";
			}
			else if(dMinutes < 24*60)
			{
				return (dMinutes / 60) + " hours ago";
			}
		}
		
		long dDays =  dMillis / (1000*60*60*24);
		
		if (dDays < 2)
		{
			if(showTime)
			{
				return "a day ago at " + postTimeFormatter.format(date);
			}
			else if (dDays < 1)
			{
				return "in the past day";
			}
			else
			{
				return "a day ago";
			}
		}
		if (dDays < 7) // less than 1 week
		{
			if(showTime)
			{
				return dDays + " days ago at " + postTimeFormatter.format(date);
			}
			else
			{
				return dDays + " days ago";
			}
		}
		else if (dDays < 14) // less than 2 weeks
		{
			return "a week ago, on " + postDateFormatterNoYear.format(date);
		}
		else if (dDays < 28) // less than 4 weeks
		{
			return (dDays/7) + " weeks ago, on " + postDateFormatterNoYear.format(date);
		}
		else if (dDays < 60) // less than 2 months
		{
			return "a month ago, on " + postDateFormatterNoYear.format(date);
		}
		else if (dDays < 360) // less than a year
		{
			return (dDays/30) + " months ago, on " + postDateFormatter.format(date);
		}
		else if (dDays < 730) // less than 2 years
		{
			return " a year ago, on " + postDateFormatter.format(date);
		}
		else
		{
			return (dDays/365)  + " years ago, on " + postDateFormatter.format(date);
		}
		
		
	}
	
	public static String formatICalDateTime(Date date){
		return iCalDateTimeFormatter.format(date);		
	}
	
	public static String sanitizeFileName(String name){
		return name.replaceAll("/../", "/");
	}
	
	public static GroupModel getGroup(String name) throws HibernateException
	{
		//we cache the admin-group id's by name so that 
		//we can exploit the 2nd-level cache on later retrievals
		//these id's are different in existing and fresh installs
		//so they can't be hardcoded
		
		Integer id = adminGroupIDs.get(name);
		if(id != null)
		{
			return (GroupModel)HibernateUtil.currentSession().load(GroupModel.class, id);
		}
		else
		{
			//group id unknown, grab group from db and cache the id
			GroupModel theGroup = (GroupModel)HibernateUtil.currentSession()
		       .createQuery("FROM GroupModel g where g.admin=true and g.shortname=?")
		       .setString(0, name).uniqueResult();
			adminGroupIDs.put(theGroup.getShortname(), theGroup.getId());
			return theGroup;
		}
	}

	public static String getDefaultURL()
	{
		return defaultURL;
	}

	public static void setPrefixes(String appPrefix, String defaultURL, String domain)
	{
		Helpers.domain = domain;
		Helpers.appPrefix = appPrefix;
		Helpers.defaultURL = Helpers.appPrefix + defaultURL;
	}

	/**
	 * Return the uri, relative to the app base.  Thus, if you have no appbase
	 * ie http://sytem.com/home/Home/whatever/etc, or if you have an appbase ie
	 * http://localhost/sytem/home/Home/whatever/etc, this will always return
	 * [home, Home, whatever, etc]
	 */
	public static String[] getURIComponents(String uri)
	{
		return uri.substring(appPrefix.length() + 1).split("/");
	}

	public static String getAppPrefix()
	{
		return appPrefix;
	}

	public static void setDevMode(boolean devMode)
	{
		Helpers.devMode = devMode;
	}

	public static boolean isDevMode()
	{
		return devMode;
	}

	public static String getDomain()
	{
		return domain;
	}

	public static DailyStatsModel currentDailyStats()
	{
		List l = HibernateUtil.currentSession()
        		 .createQuery("from DailyStatsModel as ds where ds.day=?")
        		 .setDate(0, new Date()).list();
		
		if (l.size() == 0)
		{
			// If we had a log, we should record an error
			// (this is safe in local testing, when the cron doesn't run, 
			//  but we should never enter this block on the production site)
			Calendar cal = Calendar.getInstance();
			return DailyStatsModel.newDailyStats(cal.getTime());
		}
		
		return (DailyStatsModel)l.get(0);
	}
    
    public static String htmlSafe(String value)
    {
        value = value.replace("&", "&amp;");  // v. important to keep this first
        value = value.replace("<", "&lt;");
        value = value.replace(">", "&gt;");
        value = value.replace("\"", "&quot;");
      
        return value;
    }

	public static String wikiFormat(String input) {
	    // Escape HTML characters first.  Maybe this can be done via the 
	    // Wiki formatter, but meh.. it's probably useful to have this
	    // as our own seperate method anyways.
	    input = htmlSafe(input.trim());

	    //collapse multiple newlines
	    input = input.replaceAll("\r", "");
	    input = input.replaceAll("(?m)\n\\s*\n", "\r");
	    input = input.replaceAll("([\n\r])        ", "$1&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; ");
	    input = input.replaceAll("([\n\r])      ", "$1&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; ");
	    input = input.replaceAll("([\n\r])    ", "$1&nbsp; &nbsp; &nbsp; &nbsp; ");
	    input = input.replaceAll("([\n\r])  ", "$1&nbsp; &nbsp; ");
	    
	    //massage the input a little bit by re-spacing tags.
	    input = input.replaceAll("\\*\\*", " ** ");
	    input = input.replaceAll("\\^\\^", " ^^ ");
	    input = input.replaceAll("==", " == ");
	    
	    // Set up Wiki formatter
		InitialRenderContext initialContext = new BaseInitialRenderContext();
		initialContext.set(RenderContext.OUTPUT_BUNDLE_NAME, "radeox_markup_custom");
		initialContext.set(RenderContext.INPUT_BUNDLE_NAME, "radeox_markup_custom");
		RenderEngine engineWithContext = new BaseRenderEngine(initialContext);
		String renderedText = engineWithContext.render(input, new BaseRenderContext());
	
		//instead of trying to auto-close orphaned tags, just nuke them
		renderedText = renderedText.replaceAll("\\*\\*", "");
		renderedText = renderedText.replaceAll("\\^\\^", "");
		renderedText = renderedText.replaceAll("==", "");
			
		//radeox munges the h in the HREF in URL replacements starting with 'http'
		renderedText = renderedText.replaceAll("&#104;", "h"); 
		
		//big hack: radeox munges the w in the HREF in URL replacements starting with 'www'
		//so we use that to slip in the required http://
		renderedText = renderedText.replaceAll("href=\"&#119;", "href=\"http://w");
		
		//emails, booh-yah
		Matcher matcher = Pattern.compile("\\b(([._\\-\\p{Alnum}])+@([._\\-\\p{Alnum}])+\\.([._\\-\\p{Alnum}])+)\\b")
		.matcher(renderedText);
		String emailFiltered = renderedText;
		while(matcher.find())
		{
			String email = matcher.group(0);
			String escapedEmail = email.replace("@", "#atsign#");
			emailFiltered = emailFiltered.replaceFirst(email,
					"<a href=\"mailto:" + escapedEmail + "\">" + escapedEmail + "</a>");
		}
		
		emailFiltered = emailFiltered.replaceAll("#atsign#", "@");
		
		//various transformations above result in empty paragraphs, with or without newlines, strip'em
		String cleaned = emailFiltered.replaceAll("<p class=\"postbody\"><br/>", "<p class=\"postbody\">");
		cleaned = cleaned.replaceAll("<br/></p>", "</p>");
		cleaned = cleaned.replaceAll("<p class=\"postbody\">\\s*</p>", "");
		
		return cleaned; 
	}

	public static String textFormat(String input) {
		return input.replaceAll("\\^\\^", "").replaceAll("\\*\\*", "");
	}
	
	public static String iCalTextFormat(String input){
		return Helpers.textFormat(input).replaceAll(",","\\\\,").replaceAll("\r","").replaceAll("\n", "\\\\n");
	}
	
	public static List<GroupModel> getNationalRepLists(boolean includeUni, boolean includePro){
		Criteria crit = HibernateUtil.currentSession().createCriteria(GroupModel.class);
		crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		ArrayList<Object> types = new ArrayList<Object>();
		types.add('b');
		if(includePro)
		{
			types.add('p');
		}
		if(includeUni)
		{
			types.add('s');
		}
		crit.add(Restrictions.in("nationalRepType", types));

		return (new SafeHibList<GroupModel>(crit)).list();
	}
	
	public static float findElapsedTime(long stime)
	{
		long elapsed = System.currentTimeMillis() - stime;
		return (elapsed / 1000f);
	}
	
	public static String md5(String key)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(key.getBytes());
			byte[] v = md.digest();
			StringBuffer sb = new StringBuffer(v.length * 2);
			for (int i = 0; i < v.length; i++)
			{
				int b = v[i] & 0xFF;
				sb.append("0123456789abcdef".charAt(b >>> 4)).append("0123456789abcdef".charAt(b & 0xF));
			}
			return sb.toString();
		}
		catch(NoSuchAlgorithmException nsae)
		{
		    return "";
		}
	}
	
	public static String getLongName() {
		return longName;
	}


	public static void setLongName(String longName) {
		Helpers.longName = longName;
	}


	public static String getEnShortName() {
		return enShortName;
	}


	public static void setEnShortName(String enShortName) {
		Helpers.enShortName = enShortName;
	}


	public static String getFrShortName() {
		return frShortName;
	}


	public static void setFrShortName(String frShortName) {
		Helpers.frShortName = frShortName;
	}


	public static String getSystemEmail() {
		return systemEmail;
	}


	public static void setSystemEmail(String systemEmail) {
		Helpers.systemEmail = systemEmail;
	}
}
