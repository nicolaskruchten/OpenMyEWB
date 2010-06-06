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

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.servlet.VelocityServlet;
import org.hibernate.Session;

import ca.myewb.controllers.common.EventList;
import ca.myewb.controllers.common.PostList;
import ca.myewb.frame.GetParamWrapper;
import ca.myewb.frame.HibernateUtil;
import ca.myewb.frame.Helpers;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.PostParamWrapper;
import ca.myewb.frame.RedirectionException;
import ca.myewb.model.EventModel;
import ca.myewb.model.GroupChapterModel;
import ca.myewb.model.GroupModel;
import ca.myewb.model.UserModel;


public class ApiServlet extends VelocityServlet
{
	public static final String pathprefix = Helpers.getAppPrefix();
	
	public Template handleRequest(HttpServletRequest req, HttpServletResponse res, Context ctx)
	           throws ServletException, IOException
	{
		Logger log = Logger.getLogger(this.getClass());

		try
		{
			String[] path = Helpers.getURIComponents(req.getRequestURI());
			if (path == null)
				log.info("path is null!");
			else
				log.info("path array length is " + path.length + "  (" + path + ")");
			
			String prefix = path[0]; //guaranteed, otherwise this wouldn't execute
			log.info("prefix is " + prefix);

			if(path.length < 3) // e.g. /api/type rather than /api/type/filter
			{
				throw new RedirectionException(pathprefix + "/home/SignIn");
			}
			
			if(!prefix.equals("api"))
			{
				redirectToNewURL(path);
			}
			
			String type = path[1];
			log.info("type is " + type);

			res.setCharacterEncoding("UTF-8");
			if(req.getRequestURI().endsWith("ics"))
			{
				res.setContentType("text/calendar");
			}
			else if(req.getRequestURI().endsWith("vcf"))
			{	
				res.setContentType("text/x-vcard");
			}
			else //assume RSS or XML
			{
				res.setContentType("text/xml");
			}
			
			Session s = HibernateUtil.currentSession();
			
			ctx.put("base", "http://" + Helpers.getDomain() + Helpers.getAppPrefix());
			ctx.put("helpers", new Helpers());

			log.info("switching depending on type");
			
			if(type.equals("posts") || type.equals("list") || type.equals("hot")) //RSS
			{
				return handlePosts(req, s, ctx, path, log);
			}
			else if(type.equals("calendar") || type.equals("event")) //ICS with legacy RSS fallback
			{
				return handleEvents(req, s, ctx, path, log);
			}
			else if(type.equals("person")) //VCF
			{
				return handlePerson(req, s, ctx, path, log);
			}
			else if(type.equals("chapter")) //XML
			{
				return handleChapter(req, s, ctx, path, log);
			}
			else
			{
				throw new RedirectionException(pathprefix + "/home/SignIn");
			}

		}
		catch (RedirectionException re)
		{
			res.sendRedirect(re.getTargetURL());
		}
		catch (Exception e)
		{
			log.info("api referrer: " + req.getHeader("Referer"));
			log.info("api URI: " + req.getRequestURI());
			log.fatal("api servlet error: " + e.toString(), e);
			res.sendError(500, e.toString());
		}
		return null;
	}

	private Template handlePosts(HttpServletRequest req, Session s, Context ctx, String[] path, Logger log) 
		throws ResourceNotFoundException, ParseErrorException, Exception 
	{
		log.info("handling posts");
		
		HttpSession httpSession = req.getSession();

		log.info("path is now " + path.length);
		PostParamWrapper requestParams = new PostParamWrapper(req);
		GetParamWrapper urlParams = new GetParamWrapper(path);
		String feedType = path[1];
		String filterParam = path[2];
		
		log.info("feedType is " + feedType + " and filterParam is " + filterParam);
		
		boolean showFullPost = false;
		if (path.length > 3)
			if (path[3].equals("full"))
				showFullPost = true;

		log.info("putting into context");
		
		ctx.put("feedType", feedType);
		ctx.put("now", Helpers.formatRFCDate(new Date()));
		ctx.put("fullPost", showFullPost);

		//cache display settings from session
		String showReplies = (String) httpSession.getAttribute("showReplies");
		String showEmails = (String) httpSession.getAttribute("showEmails");
		String sortByLastReply = (String) httpSession.getAttribute("sortByLastReply");
		UserModel currentUser = (UserModel)s.load(UserModel.class, new Integer(1));

		httpSession.setAttribute("showReplies", "no");
		httpSession.setAttribute("showEmails", "yes");
		httpSession.setAttribute("sortByLastReply", "no");
		
		log.info("checking feedType");
		
		if(feedType.equals("posts"))
		{
			 // handle legacy URLs
			if(filterParam.contains("Any plus Replies"))
			{
				httpSession.setAttribute("showReplies", "yes");
			}
			else
			{
				httpSession.setAttribute("showEmails", "no");
			}
			
			(new PostList(httpSession, 
					s, 
					requestParams, 
					urlParams,
					currentUser)).list(ctx, "postsrss", 20, false);
		}
		else if(feedType.equals("list"))
		{

			PostList postList = (new PostList(httpSession, 
						s, 
						requestParams, 
						urlParams,
						currentUser));
			
			GroupModel theGroup = null;
			try
			{			
				int id = Integer.parseInt(filterParam);
				theGroup = (GroupModel)postList.getAndCheck(GroupModel.class, id);
			}
			catch(NumberFormatException nfe)
			{
				theGroup = (GroupChapterModel)HibernateUtil.currentSession()
			       .createQuery("FROM GroupChapterModel g where g.shortname=?")
			       .setString(0, filterParam).uniqueResult();
			}

			ctx.put("list", theGroup);
			if(Permissions.guestsCanReadPostsInGroup( theGroup) )
			{
				postList.list(ctx, "listposts", 20, false);
			}
		}
		else if(feedType.equals("hot"))
		{
			 // handle legacy URLs
			if(filterParam.contains("Any plus Replies"))
			{
				httpSession.setAttribute("showReplies", "yes");
			}
			(new PostList(httpSession, 
					s, 
					requestParams, 
					urlParams,
					currentUser)).list(ctx, "featuredpostsrss", 20, false);
			
		}

		//undo any changes to display settings in session
		httpSession.setAttribute("showReplies", showReplies);
		httpSession.setAttribute("showEmails", showEmails);
		httpSession.setAttribute("sortByLastReply", sortByLastReply);
		
		return getTemplate("frame/rsswrapper.vm");
	}


	private Template handleEvents(HttpServletRequest req, Session s, Context ctx, String[] path, Logger log) 
		throws ResourceNotFoundException, ParseErrorException, Exception 
	{
		log.info("handling events (" + path.length + ")");
		
		String calType = path[1];
		String filterParam = path[2].replaceAll(".ics", "");
		
		log.info("calType is " + calType + " and filterParam is " + filterParam);
		
		if(calType.equals("calendar"))
		{			
			UserModel currentUser = (UserModel)s.load(UserModel.class, new Integer(1));
			GroupChapterModel chapter = (GroupChapterModel)s
		       .createQuery("FROM GroupChapterModel g where g.shortname=?")
		       .setString(0, filterParam).uniqueResult();
			
			EventList eventList = new EventList(req.getSession(),
					HibernateUtil.currentSession(),
					new PostParamWrapper(req),
					new GetParamWrapper(Helpers.getURIComponents(req.getRequestURI())),
					currentUser);
			
			Collection<EventModel> events = eventList.listVisibleEventsForQuarter(new Date(), chapter);
			ctx.put("now", Helpers.formatRFCDate(new Date()));
			ctx.put("events", events);
			ctx.put("chapterShortName", filterParam);
		}
		else if(calType.equals("event"))
		{
			UserModel currentUser = WrapperServlet.getUser(Helpers.getDefaultURL(),
                    log, s, req.getSession());

			EventModel e = (EventModel)s.load(EventModel.class, new Integer(filterParam));
			if(Permissions.canReadEvent(currentUser, e))
			{
				Vector<EventModel> events = new Vector<EventModel>();
				events.add(e);
				ctx.put("events", events);
			}
		}
		
		log.info("and getting template for calendars");
		
		if(path[2].endsWith("ics"))
		{
			return getTemplate("frame/icalcalendarwrapper.vm");
		}
		else //assume legacy RSS
		{
			return getTemplate("frame/rsscalendarwrapper.vm");
		}
	}

	private Template handlePerson(HttpServletRequest req, Session s, Context ctx, String[] path, Logger log) 
	throws ResourceNotFoundException, ParseErrorException, Exception 
	{
		String filterParam = path[2].substring(0, path[2].length()-4);
		UserModel targetUser = (UserModel)s.load(UserModel.class, new Integer(filterParam));
		UserModel currentUser = WrapperServlet.getUser(Helpers.getDefaultURL(),
                log, s, req.getSession());
		if(Permissions.canReadPersonalDetails(currentUser, targetUser))
		{
			ctx.put("u", targetUser);
		}
		
		return getTemplate("frame/vcard.vm");
	}
	
	private Template handleChapter(HttpServletRequest req, Session s, Context ctx, String[] path, Logger log) 
	throws ResourceNotFoundException, ParseErrorException, Exception 
	{
		String filterParam = path[2];

		GroupChapterModel chapter = (GroupChapterModel)HibernateUtil.currentSession()
	       .createQuery("FROM GroupChapterModel g where g.shortname=?")
	       .setString(0, filterParam).uniqueResult();

		ctx.put("chapter", chapter);

		List execs = HibernateUtil.currentSession().createQuery("SELECT u FROM UserModel u, RoleModel r "
		               + "WHERE r.user=u AND r.group=? AND r.level='l' AND r.end IS NULL")
		             .setEntity(0, chapter).list();

		ctx.put("execs", execs);
		UserModel currentUser = (UserModel)s.load(UserModel.class, new Integer(1));
		ctx.put("lists", Permissions.visibleGroupsInChapter(currentUser, chapter));
		
		return getTemplate("frame/chapterxml.vm");
	}

	private void redirectToNewURL(String[] path) throws RedirectionException 
	{
		String oldType = path[1];
		
		if(oldType.equals("rss"))
		{
			throw new RedirectionException(pathprefix + "/api/posts/" + path[2]);
		}
		else if(oldType.equals("listrss"))
		{
			throw new RedirectionException(pathprefix + "/api/list/" + path[2]);
		}
		else if(oldType.startsWith("chapter")) //either chapterrss or chapterical
		{
			throw new RedirectionException(pathprefix + "/api/calendar/" + path[2]);
		}
		else
		{
			throw new RedirectionException(path + "/home/SignIn");
		}
	}

}
