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

package ca.myewb.frame.servlet;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.hibernate.Session;

import ca.myewb.controllers.common.EventList;
import ca.myewb.controllers.common.PostList;
import ca.myewb.frame.Controller;
import ca.myewb.frame.FileNameWrapper;
import ca.myewb.frame.GetParamWrapper;
import ca.myewb.frame.Helpers;
import ca.myewb.frame.HibernateUtil;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.PostParamWrapper;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.toolbars.AtAGlance;
import ca.myewb.frame.toolbars.Online;
import ca.myewb.model.EventModel;
import ca.myewb.model.GroupModel;
import ca.myewb.model.UserModel;


public class AjaxServlet extends CachingVelocityServlet
{	
	private static String frontPageCache = null;
	private static Calendar cacheCal = null;

	
	public Template handleRequest(HttpServletRequest request,
            HttpServletResponse response, Context ctx)
     throws Exception
     {	
		Logger log = Logger.getLogger(this.getClass());
		// Set to expire far in the past.
		response.setHeader("Expires", "Sat, 6 May 1995 12:00:00 GMT");
		// Set standard HTTP/1.1 no-cache headers.
		response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
		// Set IE extended HTTP/1.1 no-cache headers (use addHeader).
		response.addHeader("Cache-Control", "post-check=0, pre-check=0");
		// Set standard HTTP/1.0 no-cache header.
		response.setHeader("Pragma", "no-cache");
		
		try
		{
			HttpSession httpSession = request.getSession();
			
			String templatePath = "frame/ajaxsuccess.vm";

			if (request.getRequestURI().contains("keepalive"))
			{
				keepAlive(httpSession);
			}
			else if (request.getRequestURI().contains("hidestickies"))
			{
				hideStickies(request);
			}
			else if(request.getRequestURI().contains("help"))
			{
				templatePath = "help/" + request.getParameter("fragment") + ".vm";
			}
			else if(request.getRequestURI().contains("whosonlinesidebar"))
			{
				templatePath = "frame/toolbars/online.vm";
				whosOnlineSidebar(request, httpSession, ctx);
			}
			else
			{
				Session session = HibernateUtil.currentSession();
				UserModel currentUser = WrapperServlet.getUser(Helpers.getDefaultURL(), log, session, httpSession);
				
				if(request.getRequestURI().contains("postlist"))
				{
					postList(request, httpSession, session, currentUser, ctx);
					templatePath = "frame/postList.vm";
				}
				else if (request.getRequestURI().contains("autocomplete"))
				{
					autocomplete(request, httpSession, session, currentUser, ctx, log);
					templatePath = "frame/autocomplete.vm";
				}
				else if(request.getRequestURI().contains("calendarsidebar"))
				{
					calendarSidebar(request, httpSession, session, currentUser, ctx);
					templatePath = "frame/toolbars/calendar.vm";
				}
				else if(request.getRequestURI().contains("ataglancesidebar"))
				{
					atAGlanceSidebar(request, httpSession, session, currentUser, ctx);
					templatePath = "frame/toolbars/ataglance.vm";
				}
				else if (request.getRequestURI().contains("groupfiles"))
				{
					groupFiles(request, log, session, currentUser, ctx);
					templatePath = "frame/groupfiledirectory.vm";
				}
			}
			
			return getTemplate(templatePath);
		}
		catch (RedirectionException re)
		{
			log.info("Clean ajax redirect: " + re.getTargetURL());
			response.sendRedirect(re.getTargetURL());
			return null;
		}
	}

	private void autocomplete(HttpServletRequest request, HttpSession httpSession, Session session, UserModel currentUser, Context ctx, Logger log) 
	{
		String linktable = request.getParameter("area").equals("events") ? "tags2events" : "tags2posts";
		String input = request.getParameter("q").replace("'", "");
		String sql = "select t.uniquename from tags t, " + linktable + " x " +
				"where t.uniquename like '%" + input + "%' and t.id=x.tagid " +
				"group by t.uniquename " +
				"order by count(*) desc, t.uniquename limit 10";
		ctx.put("tags", session.createSQLQuery(sql).list());
	}

	private void hideStickies(HttpServletRequest request) 
	{
		request.getSession().setAttribute("hideStickies", "yes");
	}

	private void keepAlive(HttpSession httpSession)
	{
		try
		{
			int userid = (Integer)httpSession.getAttribute("userid");
			WrapperServlet.updateUserList(httpSession, userid, null, null);
		}
		catch(NullPointerException npe)
		{
			; //no harm, no foul
		}
	}

	private void postList(HttpServletRequest request,
			HttpSession httpSession, Session s,
			UserModel currentUser, Context ctx) throws Exception 
	{
		
		String[] params = new String[]{"area", "class",
				request.getParameter("filter"), request.getParameter("pagenum")};
		PostList postList = (new PostList(httpSession, s, new PostParamWrapper(request), 
				new GetParamWrapper(params), currentUser));
		postList.list(ctx, "posts", 12);
		
		ctx.put("base", Helpers.getAppPrefix());
		ctx.put("user", currentUser);
		ctx.put("area", "home");
		ctx.put("herepage", "Posts");
		ctx.put("isGuest", currentUser.getUsername().equals("guest"));
		ctx.put("helpers", new Helpers());
	}
	
	private void atAGlanceSidebar(HttpServletRequest request,
			HttpSession httpSession, Session s,
			UserModel currentUser, Context ctx) throws Exception 
	{
		AtAGlance toolbar = new AtAGlance();
		toolbar.compute(currentUser);
		ctx.put("toolbar", toolbar);
		ctx.put("base", Helpers.getAppPrefix());
		ctx.put("user", currentUser);
		ctx.put("renderAtaglance", true);
		ctx.put("helpers", new Helpers());
	}
	

	
	private void whosOnlineSidebar(HttpServletRequest request,
			HttpSession httpSession, Context ctx) throws Exception 
	{
		Online toolbar = new Online(httpSession);
		toolbar.setUpCtx(ctx);
		ctx.put("toolbar", toolbar);
		ctx.put("base", Helpers.getAppPrefix());
		ctx.put("renderWhosonline", true);
		ctx.put("helpers", new Helpers());
	}

	private void calendarSidebar(HttpServletRequest request,
			HttpSession httpSession, Session s,
			UserModel currentUser, Context ctx) throws Exception 
	{

		Calendar cal = GregorianCalendar.getInstance();
		EventList events = new EventList(httpSession, s, new PostParamWrapper(request), 
				new GetParamWrapper(new String[]{}), currentUser);
		
		cal.setTime(events.getDatesInNextNWeeks(cal.getTime(), 1).get(0).get(cal.get(Calendar.DAY_OF_WEEK) - 1));
		Date today = cal.getTime();
		cal.add(Calendar.WEEK_OF_YEAR, 2);
		Date twoweeks = cal.getTime();
		cal.add(Calendar.WEEK_OF_YEAR, -2);
		
		Vector<EventModel> nextEvents = new Vector<EventModel>();
		nextEvents.addAll(events.listVisibleEventsBetweenDates(today, twoweeks, null));
		if(nextEvents.size() >= 3 )
		{
			nextEvents.setSize(3);
		}

		ctx.put("base", Helpers.getAppPrefix());
		ctx.put("user", currentUser);
		ctx.put("calendar", events.mapToDateVisibleEventsForNextNWeeks(cal.getTime(), null, 5));
		ctx.put("cal", cal);
		ctx.put("datelist", events.getDatesInNextNWeeks(cal.getTime(), 5));
		ctx.put("nextEvents", nextEvents);
		ctx.put("filter", "nofilter");
		ctx.put("today", today);

	}

	private void groupFiles(HttpServletRequest request, Logger log,
			Session s, UserModel currentUser, Context ctx)
			throws Exception {
		GroupModel theGroup = (GroupModel)s.get(GroupModel.class, new Integer(request.getParameter("groupId")));
		
		if (theGroup == null)
		{				
			throw new RedirectionException(Controller.path + "/mailing/ListInfo/" + request.getParameter("groupId"));
		}
		
		if (currentUser.getUsername().equals("guest"))
		{
			log.warn("Guest tried to view files belonging to " + theGroup.getShortname());
			throw new RedirectionException(Helpers.getAppPrefix() + "/ajax/keepalive");
		}

		String action = request.getParameter("action");
		String decodedPath = URLDecoder.decode(request.getParameter("path"), "UTF-8");
		if (action.equals("delete") && (Permissions.canManageFilesInGroup(currentUser, theGroup)))
		{
			File file = new File(Helpers.getUserFilesDir() + "groupfiles/" + request.getParameter("groupId") + "/" + decodedPath);
			
			File trashFile = new File(Helpers.getUserFilesDir()
					+ "groupfiles/" + request.getParameter("groupId")
					+ "/.trash/"	+ decodedPath);
			
			// Re-create directory structure in .trash
			if (!trashFile.getParentFile().exists())
			{
				trashFile.getParentFile().mkdirs();
			}
			
			// Delete old file in .trash
			else if (trashFile.exists())
			{
				deleteDir(trashFile);
			}
			
			// Attempt to move into trash
			if (!file.renameTo(trashFile))
			{
				log.error("File rename failed for " + file.getAbsolutePath());
			}

			throw new RedirectionException(Helpers.getAppPrefix() + "/ajax/keepalive");					
		}
		else if (action.equals("showDirectory") && Permissions.canReadFilesInGroup(currentUser, theGroup))
		{				
			File dirRoot = new File (Helpers.getUserFilesDir() + "groupfiles/" + request.getParameter("groupId") + "/" + decodedPath);

			LinkedList<FileNameWrapper> directories = new LinkedList<FileNameWrapper>();
			LinkedList<FileNameWrapper> files = new LinkedList<FileNameWrapper>();
			
			log.info("Using the directory " + dirRoot.getAbsolutePath());
			if (!dirRoot.exists())
			{
				throw new RedirectionException(Controller.path + "/ajax/keepalive");
			}
			
			// Read entire directory, and stuff into directories/files lists
			TreeSet<File> dirList = new TreeSet<File>(Arrays.asList(dirRoot.listFiles()));
			
			for (File f : dirList)
			{
				if (!f.getName().equals(".trash"))
				{
					if (f.isDirectory())
					{
						directories.add(new FileNameWrapper(f));
					}
					else if (f.isFile())
					{
						files.add(new FileNameWrapper(f));
					}
				}
			}
			
			//Set up velocity stuff
			ctx.put("directories", directories);
			ctx.put("files", files);
			ctx.put("pathHash", new FileNameWrapper(dirRoot).getMD5Hash() );
			ctx.put("relPath", new FileNameWrapper(dirRoot).getRelativePath() );
			ctx.put("groupID", request.getParameter("groupId") );
			ctx.put("base", Helpers.getAppPrefix());
			ctx.put("canEdit", Permissions.canManageFilesInGroup(currentUser,theGroup));
		}
		else
		{
			throw new RedirectionException(Controller.path + "/mailing/ListInfo/" + request.getParameter("groupId"));
		}
	}
	
	private boolean deleteDir(File path)
	{
		// Recursively empty all contents if it's a directory
		if (path.isDirectory())
		{
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++)
			{
				if (files[i].isDirectory())
				{
					deleteDir(files[i]);
				}
				else
				{
					files[i].delete();
				}
			}
		}
		
		// Delete the file itself
		return (path.delete());
    }
	
	

	
	protected void error(HttpServletRequest request, HttpServletResponse response, Exception cause)
     throws IOException
	{
		Logger.getLogger(this.getClass()).error("AjaxServletError!", cause);
		response.sendError(500, cause.toString());
	}
	
	protected void requestCleanup(HttpServletRequest request, HttpServletResponse response)
	{
		HibernateUtil.closeSession();
	}

	public boolean isCachable(HttpServletRequest request) 
	{
		boolean isGuest = request.getSession().isNew() || 
		request.getSession().getAttribute("userid") == null ||
		request.getSession().getAttribute("userid").equals(1);

		if(isGuest)
		{
			boolean isFrontPage = request.getRequestURI().contains("postlist") &&
			request.getParameter("filter").equals("Any") &&
			request.getParameter("pagenum").equals("1");
			
			return isFrontPage;
		}
		
		return false;
	}
	
	public void saveOutputToCache(String fragment)
	{
		AjaxServlet.setFrontPageCache(fragment);
	}
	
	public static synchronized void invalidateFrontPageCache(Integer groupid)
	{
		//if the affected post shows up for guests
		if(groupid == null || groupid == 1 || groupid == 70 || groupid == 71)
		{
			AjaxServlet.cacheCal = null;
		}
	}
	
	public String getCachedOutputIfFresh(HttpServletRequest request) 
	{
		return AjaxServlet.getFrontPageCacheifFresh();
	}
	
	public static synchronized String getFrontPageCacheifFresh()
	{
		if(AjaxServlet.cacheCal == null)
		{
			return null;
		}
		
		Calendar treshHoldCal = GregorianCalendar.getInstance();
		treshHoldCal.add(Calendar.MINUTE, -30);
		if(AjaxServlet.cacheCal.before(treshHoldCal))
		{
			return null;
		}
		
		return AjaxServlet.frontPageCache + " <!-- cached copy from " + AjaxServlet.cacheCal.getTime().toString() + " -->";
	}
	
	public static synchronized void setFrontPageCache(String fragment)
	{
		AjaxServlet.frontPageCache = fragment;
		AjaxServlet.cacheCal = GregorianCalendar.getInstance();
	}

}
