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
import java.util.Calendar;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import ca.myewb.frame.ErrorMessage;
import ca.myewb.frame.GetParamWrapper;
import ca.myewb.frame.HibernateUtil;
import ca.myewb.frame.Message;
import ca.myewb.frame.Helpers;
import ca.myewb.frame.Controller;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.PostParamWrapper;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.StickyMessage;
import ca.myewb.frame.StickyMessages;
import ca.myewb.frame.forms.Form;
import ca.myewb.model.PageModel;
import ca.myewb.model.UserModel;


public class WrapperServlet extends CachingVelocityServlet
{
	private static ThreadLocal<Transaction> transactionHolder = new ThreadLocal<Transaction>();
	private static String frontPageCache = null;
	private static Calendar cacheCal = null;

	public Template handleRequest(HttpServletRequest request,
	                              HttpServletResponse response, Context ctx)
	                       throws Exception
	{
		ctx.put("renderStart", System.currentTimeMillis());
		final String defaultPath = Helpers.getDefaultURL();
		String requestURIwithDomain = Helpers.getDomain() + request.getRequestURI();

		Logger log = Logger.getLogger(this.getClass());
		log.info("****** (wrapper)"); //to look through logs more easily
		if(request.isSecure())
		{
			log.info(request.getHeader("Referer") + " -> https://" + requestURIwithDomain);
		}
		else
		{
			log.info(request.getHeader("Referer") + " -> http://" + requestURIwithDomain);
		}
		log.info(request.getHeader("User-Agent"));

		// Break down URL to find appropriate controller
		String[] path = Helpers.getURIComponents(request.getRequestURI());

		try //make sure there are enough slashes, we're not in safemode etc
		{
			checkURL(request, ctx, defaultPath, log, path);
		}
		catch (RedirectionException re)
		{
			log.info("Clean redirect: " + re.getTargetURL());
			response.sendRedirect(re.getTargetURL());

			return null;
		}

		//ok, we can at least parse which controller they want
		String area = path[0].toLowerCase();
		String className = path[1].replaceAll("[^\\p{L}]*$",""); //nuke (trailing) punctuation
		
		// Redirect all requests for favicon.ico (wish this could be done at the apache level)
		if (path[path.length - 1].equals("favicon.ico") || path[path.length - 1].equals("favicon.gif"))
		{
			log.info("Redirect: favicon.ico");
			response.sendRedirect(Helpers.getAppPrefix() + "/favicon.ico");
			return null;
		}

		//########################################
		//if we get this far, the request is looking OK, we can go deeper with session and appsession mgmt
		//########################################

		Session hibernateSession = HibernateUtil.currentSession();
		if(area.startsWith("actions")) //if it doesn't, we shouldn't be making db changes
		{
			transactionHolder.set(hibernateSession.beginTransaction());
		}

		HttpSession httpSession = request.getSession();

		// Retrieve message (here because the catch block needs to see message)
		Message message = (Message)httpSession.getAttribute("message");
		ctx.put("messages", message);
		httpSession.removeAttribute("message");
		
		try //this block catches redirects cleanly
		{
			UserModel user = WrapperServlet.getUser(defaultPath, log,
			                                   hibernateSession, httpSession);
			httpSession.setAttribute("userid", new Integer(user.getId()));
			
			updateUserList(httpSession, user.getId(),
                           user.getFirstname() + " " + user.getLastname(), log);

		


			if (httpSession.getAttribute("interpageVars") == null)
			{
				httpSession.setAttribute("interpageVars", new Hashtable());
			}			

			if (httpSession.getAttribute("storedParams") == null)
			{
				httpSession.setAttribute("storedParams", new Hashtable<String, PostParamWrapper>());
			}			

			if (httpSession.getAttribute("storedForms") == null)
			{
				httpSession.setAttribute("storedForms", new Hashtable<String, Form>());
			}

			//deal with security, make sure the page exists etc
			PageModel page = getPageIfVisible(request, defaultPath, log, className,
			                             area, hibernateSession, httpSession,
			                             user);
			className = page.getName();

			// Load controller  
			Controller theController = getController(defaultPath, log,
			                                              className, area,
			                                              httpSession, user);
			
			if(!className.toLowerCase().equals("confirm") && !Helpers.isDevMode())
			{
				//force correct protocol for non-confirm pages
				//this way we can use a confirmation on secure flows
				if(theController.secureAccessRequired() && !request.isSecure())
				{
					throw new RedirectionException("https://" + requestURIwithDomain);
				}
				else if(!theController.secureAccessRequired() && request.isSecure())
				{
					throw new RedirectionException("http://" + requestURIwithDomain);
				}
			}
			
			theController.setHibernateSession(hibernateSession);
			theController.setHttpSession(httpSession);
			theController.setRequestParams(new PostParamWrapper(request));
			theController.setUrlParams(new GetParamWrapper(path));
			theController.setCurrentUser(user);
			theController.setHttpRequest(request);
			theController.setHttpResponse(response);

			//strip out unneeded vars from session
			cleanInterPageVars(httpSession, theController);
			
			List<StickyMessage> messages = new LinkedList<StickyMessage>();
			if(httpSession.getAttribute("hideStickies") == null)
			{
				messages = (new StickyMessages(user, request.getRequestURI())).getMessages();
			}
			
			ctx.put("toolbars", new Vector()); //controller may replace this

			//this is the big important call
			theController.handle(ctx); //###########################################
			log.debug("Controller handler returned OK");

			//now that that's ok, add some general stuff to the context
			List<PageModel> menu = Permissions.visiblePages(user, area);

			if (!menu.contains(page))
			{
				menu.add(page);
			}

			ctx.put("menu", menu);
			ctx.put("herepage", className);
			ctx.put("heretitle", page.getDisplayName());
			ctx.put("user", user);
			if (user.getUsername().equals("guest"))
			{
				ctx.put("isGuest", new Boolean(true));
			}
			ctx.put("stickyMessages", messages);
			ctx.put("localTemplate", area + "/" + className + ".vm");
			ctx.put("area", area);
			ctx.put("base", Helpers.getAppPrefix());
			ctx.put("helpers", new Helpers());
			ctx.put("perms", new Permissions());
			if(!ctx.containsKey("targetURL"))
			{
				ctx.put("targetURL", request.getRequestURI()); 
			}
		}
		catch (RedirectionException re)
		{
			try
			{
				// Retain old message on redirect unless a new one's been set
				if ((message != null)
				    && (httpSession.getAttribute("message") == null))
				{
					httpSession.setAttribute("message", message);
				}
			}
			catch (IllegalStateException e)
			{
				; //most likely this means that the session was invalidated by the controller
			}
				
			if (re.isRollbackRequested())
			{
				if (WrapperServlet.transactionHolder.get().isActive())
					WrapperServlet.transactionHolder.get().rollback();
			}

			log.info("Clean redirect: " + re.getTargetURL());
			response.sendRedirect(re.getTargetURL());

			return null;
		}

		response.setCharacterEncoding("UTF-8");
		
		return getTemplate("frame/wrapper.vm");
	}

	//########################################################
	//
	private Controller getController(final String defaultPath, Logger log,
	                                      String className, String area,
	                                      HttpSession httpSession, UserModel user)
	                               throws Exception
	{
		Controller theController = null;

		try
		{
			theController = (Controller)Class.forName("ca.myewb.controllers."
			                                               + area + "."
			                                               + className)
			                .newInstance();
		}
		catch (ClassNotFoundException e3)
		{
			httpSession.setAttribute("message",
			                         (new ErrorMessage("The previously requested page didn't exist, sorry!")));
			log.warn("ClassNotFound: " + area + "." + className + ", for "
			         + user.getUsername() + " - redirecting");
			throw new RedirectionException(defaultPath);
		}

		return theController;
	}

	private void cleanInterPageVars(HttpSession httpSession,
	                                Controller theController)
	{
		// Setting up interpage session information
		List<String> neededVars = theController.getNeededInterpageVars();
		Hashtable currentVars = (Hashtable)httpSession.getAttribute("interpageVars");
		Enumeration varNames = currentVars.keys();

		while (varNames.hasMoreElements())
		{
			String theName = (String)varNames.nextElement();

			if (!neededVars.contains(theName))
			{
				currentVars.remove(theName);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static void setInterpageVar(HttpSession httpSession, String name,
	                                   Object var)
	{
		Hashtable<String, Object> hashtable = (Hashtable)httpSession.getAttribute("interpageVars");

		if (hashtable == null)
		{
			httpSession.setAttribute("interpageVars", new Hashtable());
			hashtable = (Hashtable)httpSession.getAttribute("interpageVars");
		}

		hashtable.put(name, var);
	}

	private PageModel getPageIfVisible(HttpServletRequest request,
	                              final String defaultPath, Logger log,
	                              String className, String area,
	                              Session hibernateSession,
	                              HttpSession httpSession, UserModel user)
	                       throws HibernateException, RedirectionException
	{
		// Check if request page exists and if we have permission to see it
		List results = hibernateSession
			.createQuery("FROM PageModel WHERE (name=:name OR oldName=:name) AND area=:area")
			.setString("name", className)
			.setString("area", area)
			.list();

		if (results.isEmpty())
		{
			log.error("Classname " + className + " not found in db for "
			         + user.getUsername() + " - redirecting");
			httpSession.setAttribute("message",
			                         (new ErrorMessage("The previously requested page didn't exist, sorry!")));
			throw new RedirectionException(defaultPath);
		}

		PageModel page = (PageModel)results.get(0);

		if (!Permissions.canViewPage(user, page))
		{
			// Trying to access a restricted page!
			if (user.getUsername().equals("guest"))
			{
				WrapperServlet.setInterpageVar(httpSession, "requestedURL", request.getRequestURI());
				httpSession.setAttribute("message",
				                         new Message("Please sign in to reach the page you requested"));

				log.info("Access restricted for Guest at " + area + "."
				         + className + ", prompting for login - redirecting");

				throw new RedirectionException(Controller.path
				                               + "/home/SignIn");
			}
			else
			{
				httpSession.setAttribute("message",
				                         new ErrorMessage("You do not have access to the page you requested"));

				log.warn("Access restricted for " + user.getUsername() + " at "
				         + area + "." + className + " - redirecting");
				throw new RedirectionException(defaultPath);
			}
		}

		return page;
	}

	public static UserModel getUser(final String defaultPath, Logger log,
	                           Session hibernateSession, HttpSession httpSession)
	                    throws HibernateException, RedirectionException
	{
		UserModel user;
		Integer userid = (Integer)httpSession.getAttribute("userid");

		// New session: retrieve guest user from database
		if (httpSession.isNew() || (userid == null))
		{
			httpSession.setMaxInactiveInterval(60*60); //increase session length to 1 hour
			userid = new Integer(1);
			log.debug("No userid found, forcing default user: guest...");
		}

		user = (UserModel)hibernateSession.get(UserModel.class, userid);

		if (user == null)
		{
			log.warn("invalid userid in session! userid=" + userid);
			throw new RedirectionException(defaultPath);
		}

		log.debug("User identified: " + user.getUsername());

		return user;
	}

	private void checkURL(HttpServletRequest request, Context ctx,
	                      final String defaultPath, Logger log, String[] path)
	               throws RedirectionException
	{

		if (path.length < 2)
		{
			log.info("Not enough slashes in URL (" + request.getRequestURI() + ")");
			throw new RedirectionException(defaultPath);
		}
		
		if (path[0].equals("home") && path[1].equals("safemodetoken"))
		{
			log.info("Setting safemode token in session.");
			request.getSession().setAttribute("safemodetoken", "yes");
			throw new RedirectionException(defaultPath);
		}

		// are we in safe mode?
		if ((new File(this.getServletConfig().getServletContext()
		              .getRealPath("safe.html"))).exists())
		{
			// do we have a token to get through?
			if (request.getSession().getAttribute("safemodetoken") == null)
			{
				//no, get bounced out
				log.info("We're in safemode and user has no token.");
				throw new RedirectionException(Helpers.getAppPrefix()
				                               + "/safe.html", true);
			}
			else
			{
				ctx.put("safemodeon", "yes");
			}
		}
	}

	protected void requestCleanup(HttpServletRequest request,
	                              HttpServletResponse response)
	{
		try
		{
			Transaction threadTransaction = WrapperServlet.transactionHolder.get();

			if (threadTransaction == null)
			{
				Logger.getLogger(this.getClass())
				.debug("Cleanup: no transaction to commit");
			}
			else if (threadTransaction.wasCommitted())
			{
				Logger.getLogger(this.getClass())
				.debug("Cleanup: transaction already committed");
			}
			else if (threadTransaction.wasRolledBack())
			{
				Logger.getLogger(this.getClass())
				.debug("Cleanup: transaction had been rolled back");
			}
			else
			{
				HibernateUtil.currentSession().flush();
				Logger.getLogger(this.getClass()).debug("Committing transaction");
				threadTransaction.commit();
			}

			HibernateUtil.closeSession();
		}
		catch (Exception e)
		{
			Logger.getLogger(this.getClass())
			.fatal("Wrapper cleanup error: " + e.toString(), e);
			try
			{
				response.sendError(500, e.toString());
			}
			catch (IOException e1)
			{
				Logger.getLogger(this.getClass())
				.fatal("Wrapper cleanup SUB-error: " + e1.toString(), e1);
			}
		}
	}

	protected void error(HttpServletRequest request,
	                     HttpServletResponse response, Exception cause)
	              throws IOException
	{
		Logger.getLogger(this.getClass())
		.fatal("Fatal wrapper error: " + cause.toString(), cause);

		Throwable causeCause = cause.getCause();
		int i = 1;

		while (causeCause != null)
		{
			Logger.getLogger(this.getClass())
			.fatal("Fatal wrapper error cause " + i + ": "
			       + causeCause.toString(), causeCause);
			causeCause = causeCause.getCause();
			i++;
		}

		try
		{
			if (WrapperServlet.transactionHolder.get() != null)
			{
				if (WrapperServlet.transactionHolder.get().isActive())
					WrapperServlet.transactionHolder.get().rollback();
			}
		}
		catch (HibernateException e)
		{
			Logger.getLogger(this.getClass())
			.error("Rollback error: " + e.toString(), e);
		}
		catch (Exception e)
		{
			Logger.getLogger(this.getClass())
			.error("Secondary rollback error: " + e.toString(), e); //no idea, generic secondary error!
		}

		if (Helpers.isDevMode())
		{
			response.sendError(500, cause.toString());
		}
		else
		{
			//set something here which says which URL last threw an error
			//near the beginning of request processing, check if it was the default URL, if so, it'll likely happen again, right?
			//so we should redirect to a last-resort page, telling people to shut down their browser etc
			request.getSession()
			.setAttribute("message",
			              new ErrorMessage("We're sorry, but the previous request caused a server error and could not be completed. The system administrators have been automatically notified."));
			response.sendRedirect(Helpers.getDefaultURL());
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void updateUserList(HttpSession httpSession, int userId,
                                      String usersName, Logger log)
	{
		// Update the who's online list
		Hashtable userList = (Hashtable)httpSession.getServletContext()
        					 .getAttribute("userList");
		Hashtable userTime = (Hashtable)httpSession.getServletContext()
		 					 .getAttribute("userTime");

		// Expire old users - this is only done on an actual pageload
        // and not on AJAX-keepalive.  There's no point in running this after
        // an AJAX request as they're not viewing a new copy of the online list;
        // and we also reduce server load this way.
        //
        // We know whether we're in pageload vs AJAX by the usersName variable:
        // it will be NULL if it's AJAX, and an actual string if it's a pageload
        if (usersName != null)
        {
    		long threshold = System.currentTimeMillis() - (15*60*1000);
    
    		// Use toArray() instead of the more convenient iterator() to allow
    		// concurrent modification of the Hashtable even while iterating it
    		Object[] keys = userTime.keySet().toArray();
    		for (int i = 0; i < keys.length; i++)
    		{
    			long activetime = ((Long)userTime.get(keys[i])).longValue();
    			if (activetime < threshold)
    			{
    				userList.remove(keys[i]);
    				userTime.remove(keys[i]);
    			}
    		}
        }

		// Re-add myself if I was expired, and update my active time
		if (userId != 1)
			userTime.put(userId, new Long(System.currentTimeMillis()));
        
        if (userId != 1 && usersName != null)
            userList.put(userId, usersName);
	}

	public boolean isCachable(HttpServletRequest request) 
	{
		boolean isGuest = request.getSession().isNew() || 
		request.getSession().getAttribute("userid") == null ||
		request.getSession().getAttribute("userid").equals(1);

		if(isGuest)
		{
			String requestURI = request.getRequestURI().toLowerCase();
			boolean isFrontPage = requestURI.endsWith("/posts") ||
			requestURI.endsWith("/posts/any")  ||
			requestURI.endsWith("/posts/any/1");
			
			return isFrontPage;
		}
		
		return false;
	}
	
	public void saveOutputToCache(String fragment)
	{
		WrapperServlet.setFrontPageCache(fragment);
	}
	
	public String getCachedOutputIfFresh(HttpServletRequest request) 
	{
		return WrapperServlet.getFrontPageCacheifFresh();
	}
	
	public static synchronized String getFrontPageCacheifFresh()
	{
		if(WrapperServlet.cacheCal == null)
		{
			return null;
		}
		
		Calendar treshHoldCal = GregorianCalendar.getInstance();
		treshHoldCal.add(Calendar.MINUTE, -30);
		if(WrapperServlet.cacheCal.before(treshHoldCal))
		{
			return null;
		}
		
		return WrapperServlet.frontPageCache + " <!-- cached copy from " + WrapperServlet.cacheCal.getTime().toString() + " -->";
	}
	
	public static synchronized void setFrontPageCache(String fragment)
	{
		WrapperServlet.frontPageCache = fragment;
		WrapperServlet.cacheCal = GregorianCalendar.getInstance();
	}
}
