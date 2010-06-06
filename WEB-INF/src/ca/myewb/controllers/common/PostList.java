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

package ca.myewb.controllers.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpSession;

import org.apache.velocity.context.Context;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;

import ca.myewb.frame.Controller;
import ca.myewb.frame.GetParamWrapper;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.PostOrder;
import ca.myewb.frame.PostParamWrapper;
import ca.myewb.logic.GroupLogic;
import ca.myewb.logic.UserLogic;
import ca.myewb.model.GroupChapterModel;
import ca.myewb.model.GroupModel;
import ca.myewb.model.PostModel;
import ca.myewb.model.TagModel;
import ca.myewb.model.UserModel;


public class PostList extends Controller
{
	public PostList(HttpSession httpSession, Session hibernate,
	                PostParamWrapper requestParams, GetParamWrapper urlParams,
	                UserModel currentUser)
	{
		super();
		this.httpSession = httpSession;
		this.hibernateSession = hibernate;
		this.requestParams = requestParams;
		this.currentUser = currentUser;
		this.urlParams = urlParams;
	}

	public void list(Context ctx, String mode, int pagesize) throws Exception
	{
		list(ctx, mode, pagesize, true);
	}
	
	public void list(Context ctx, String mode, int pagesize, boolean sessionOverride) throws Exception
	{

		urlParams.processParams(new String[]{"filter", "pagenum"},
		                        new String[]{"Any", "1"});

		String filterToUse = urlParams.get("filter");
		ctx.put("filterParam", filterToUse);

		if (filterToUse.equals("Any") || filterToUse.equals("Any plus Replies")
				|| filterToUse.equals("Any minus Emails"))
		{
			filterToUse = null;
		}

		int start = 1;
		
		try
		{
			start = new Integer(urlParams.get("pagenum")).intValue();
		}
		catch (Exception e)
		{
			start = 1;
		}

		if (start < 1)
		{
			start = 1;
		}
		int pageStart = (start - 1) * pagesize;

		boolean findEmails = (httpSession.getAttribute("showEmails") == null) ||
				httpSession.getAttribute("showEmails").equals("yes");
		boolean findReplies = !( (httpSession.getAttribute("showReplies") == null) ||
				httpSession.getAttribute("showReplies").equals("no") );
		boolean sortByLastReply = ( (httpSession.getAttribute("sortByLastReply") == null) ||
				httpSession.getAttribute("sortByLastReply").equals("yes") );
		
		if(currentUser.isMember("Users") && sessionOverride)
		{
			findEmails = currentUser.getShowemails();
			findReplies = currentUser.getShowreplies();
			sortByLastReply = currentUser.getSortByLastReply();
		}
		
		int numTotalPosts = 0;
		List<PostModel> posts = null;

		if (mode.equals("posts"))
		{
			
			posts = visiblePosts(filterToUse, pageStart, pagesize, false, false, false, false, true, sortByLastReply);
			numTotalPosts = visiblePostsCount(filterToUse, false, false, false, false, true, sortByLastReply);
			ctx.put("thetag", filterToUse);

		}
		if (mode.equals("postsrss"))
		{
			
			posts = visiblePosts(filterToUse, pageStart, pagesize, false, findReplies, false, false, findEmails, sortByLastReply);
			numTotalPosts = visiblePostsCount(filterToUse, false, findReplies, false, false, findEmails, sortByLastReply);
			ctx.put("thetag", filterToUse);

		}
		else if (mode.equals("chapterposts"))
		{
			GroupChapterModel chapter = currentUser.getChapter();
			posts = chapterPosts(chapter, pageStart, pagesize,
					findEmails, findReplies, false, filterToUse);
			numTotalPosts = chapterPostsCount(chapter, findEmails, findReplies, false, filterToUse);
			ctx.put("thetag", filterToUse);
		}
		else if (mode.equals("newposts"))
		{
			posts = visiblePosts(filterToUse, pageStart, pagesize, true, findReplies, false, false, findEmails, false);
			numTotalPosts = visiblePostsCount(filterToUse, true, findReplies, false, false, findEmails, false);
			ctx.put("thetag", filterToUse);
		}
		else if (mode.equals("flaggedposts"))
		{
			posts = visiblePosts(filterToUse, pageStart, pagesize, false, false, true, false, true, sortByLastReply);
			numTotalPosts = visiblePostsCount(filterToUse, false, false, true, false, true, sortByLastReply);
			ctx.put("thetag", filterToUse);
		}
		else if (mode.equals("featuredposts"))
		{
			posts = visiblePosts(filterToUse, pageStart, pagesize, false, false, false, true, true, sortByLastReply);
			numTotalPosts = visiblePostsCount(filterToUse, false, false, false, true, true, sortByLastReply);
			ctx.put("thetag", filterToUse);
		}
		else if (mode.equals("featuredpostsrss"))
		{
			posts = visiblePosts(filterToUse, pageStart, pagesize, false, findReplies, false, true, true, false);
			numTotalPosts = visiblePostsCount(filterToUse, false, findReplies, false, true, true, false);
			ctx.put("thetag", filterToUse);
		}
		else if (mode.equals("userposts"))
		{
			UserModel targetUser = (UserModel)getAndCheckFromUrl(UserModel.class, "filter");
			posts = userPosts(targetUser, pageStart, pagesize);
			numTotalPosts = userPostsCount(targetUser);
			ctx.put("targetUser", targetUser);
		}
		else if (mode.equals("listposts"))
		{
			GroupModel theGroup = (GroupModel)this.getAndCheckFromUrl(GroupModel.class, "filter");
			posts = listPosts(theGroup, pageStart, pagesize);
			numTotalPosts = listPostsCount(theGroup);
		}
		else if (mode.equals("myposts"))
		{
			posts = new LinkedList<PostModel>();
			posts.addAll(currentUser.getPosts());

			Collections.sort(posts, new PostOrder());

			numTotalPosts = posts.size();

			if ((numTotalPosts - (pageStart)) < pagesize)
			{
				posts = posts.subList((pageStart), numTotalPosts);
			}
			else
			{
				posts = posts.subList((pageStart),
				                      ((start) * pagesize));
			}
		}

		ctx.put("pageNum", new Integer(start));
		ctx.put("pageSize", new Integer(pagesize));

		int numPages = (numTotalPosts / pagesize);

		if ((numTotalPosts % pagesize) != 0)
		{
			numPages++;
		}

		ctx.put("numTotalPosts", new Integer(numTotalPosts));
		ctx.put("numPages", new Integer(numPages));

		ctx.put("posts", posts);
	}

	public void handle(Context ctx) throws Exception
	{
		// You should never come here directly!
		throw getSecurityException("Someone accessed common/PostList directly!",
		                           path + "/home/Home");
	}

	private void addBooleanFilters(boolean onlyNew, boolean findReplies, boolean onlyFlagged, boolean onlyFeatured, boolean findEmails, boolean sortByLastReply, Criteria criteria)
	{
		
		if (!findReplies)
		{
			criteria.add(Restrictions.isNull("parent"));
		}
		
		if(!findEmails)
		{
			criteria.add(Restrictions.eq("emailed", false));
		}
		
		if(sortByLastReply)
		{
			criteria.addOrder(Order.desc("lastReply"));
		}
		else if (onlyNew)
		{
			SimpleExpression mainDate = Restrictions.gt("date", currentUser.getLastLogin());
			criteria.add(mainDate);
			criteria.addOrder(Order.asc("date"));
		}
		else
		{
			criteria.addOrder(Order.desc("date"));
		}
		
		if(onlyFlagged)
		{
			Set<PostModel> flaggedPosts2 = currentUser.getFlaggedPosts();
			if(flaggedPosts2.isEmpty())
			{
				criteria.add(Restrictions.eq("id", 0));
			}
			else
			{
				Vector<Integer> flaggedIDs = new Vector<Integer>();
				for(PostModel p: flaggedPosts2)
				{
					flaggedIDs.add(p.getId());
				}
				
				Criterion flaggedSelf = Restrictions.in("id", flaggedIDs);
				Criterion flaggedParent = Restrictions.in("parent", flaggedPosts2);
				criteria.add(Restrictions.or(flaggedSelf, flaggedParent));
			}
		}
		
		if(onlyFeatured)
		{
			criteria.add(Restrictions.eq("featured", true));
		}
		
		
		
	}

	private void addFilter(String filter, Criteria criteria)
	{
		if (filter == null)
		{
			return; // just in case
		}
	
		log.debug("Filtering posts: " + filter);
	
		TagModel t = TagModel.getTag(filter);
		criteria.createAlias("tags", "t");
		
		if(t==null) //won't find anything
		{
			criteria.add(Restrictions.like("t.name", "%" + filter + "%"));
		}
		else //broaden the search
		{
			criteria.add(Restrictions.like("t.name", "%" + t.getName() + "%"));
		}
	}


	public List<PostModel> chapterPosts(GroupChapterModel chapter, int start,
			int limit, boolean findEmails, boolean findReplies, boolean frontpagebeta, String filter) throws HibernateException
	{
		Criteria criteria = getChapterPostsCriteria(chapter, findEmails, findReplies, frontpagebeta, filter);
	
		addPagination(start, limit, criteria);
	
		return getUniquePostList(criteria);
	}

	public int chapterPostsCount(GroupChapterModel chapter, boolean findEmails, boolean findReplies, boolean frontpagebeta, String filter) throws HibernateException
	{
		Criteria criteria = getChapterPostsCriteria(chapter, findEmails, findReplies, frontpagebeta, filter);
	
		return getUniquePostCount(criteria);
	}

	private Criteria getListPostsCriteria(GroupLogic theGroup)
	{
		Criteria criteria = hibernateSession.createCriteria(PostModel.class);
		
		criteria.add(Restrictions.eq("group", theGroup)); // just posts in this
		// group please
		criteria.add(Restrictions.isNull("parent")); // no replies
		criteria.addOrder(Order.desc("date"));
		return criteria;
	}

	private int getUniquePostCount(Criteria criteria)
	{
		criteria.setProjection(Projections.groupProperty("id"));
		return criteria.list().size();
	}

	private List<PostModel> getUniquePostList(Criteria criteria)
	{
		criteria.setProjection(Projections.groupProperty("id"));
	
		Iterator it = criteria.list().iterator();
	
		List<PostModel> list = new ArrayList<PostModel>();
	
		while (it.hasNext())
		{
			list.add((PostModel) hibernateSession.load(PostModel.class, (Integer) it
					.next()));
		}
	
		return list;
	}

	private Criteria getUserPostCriteria(UserLogic targetUser)
	{
		// Find matching posts
		Criteria criteria = hibernateSession.createCriteria(PostModel.class);
		criteria.addOrder(Order.desc("date"));
	
		criteria.add(Restrictions.eq("poster", targetUser));
	
		if (!currentUser.isAdmin())
		{
			criteria.add(Restrictions.in("group", Permissions.visibleGroups(currentUser, false)));
		}
		return criteria;
	}

	private Criteria getVisiblePostsCriteria(String filter, boolean onlyNew, boolean findReplies, boolean onlyFlagged, boolean onlyFeatured, boolean findEmails, boolean sortByLastReply)
	{
		Criteria criteria = hibernateSession.createCriteria(PostModel.class);
	
		addBooleanFilters(onlyNew, findReplies, onlyFlagged, onlyFeatured, findEmails, sortByLastReply, criteria);
	
		addFilter(filter, criteria);
	
		if(!currentUser.isAdmin() || !currentUser.getAdminToggle())
		{
			criteria.add(Restrictions.in("group", Permissions.visibleGroups(currentUser, true)));
		}
		
		return criteria;
	}

	public List<PostModel> listPosts(GroupLogic theGroup, int start, int limit)
			throws HibernateException
	{
		Criteria criteria = getListPostsCriteria(theGroup);
	
		addPagination(start, limit, criteria);
	
		return getUniquePostList(criteria);
	}

	public int listPostsCount(GroupLogic theGroup)
	throws HibernateException
	{
		Criteria criteria = getListPostsCriteria(theGroup);
		
		return getUniquePostCount(criteria);
	}

	public List<PostModel> userPosts(UserLogic targetUser, int start, int limit)
			throws HibernateException
	{
		Criteria criteria = getUserPostCriteria(targetUser);
	
		addPagination(start, limit, criteria);
	
		return getUniquePostList(criteria);
	}

	public int userPostsCount(UserLogic targetUser)
	throws HibernateException
	{
		Criteria criteria = getUserPostCriteria(targetUser);
		
		return getUniquePostCount(criteria);
	}
	
	public List<PostModel> visiblePosts(String filter, int start, int limit,
			boolean onlyNew, boolean findReplies, boolean onlyFlagged, boolean onlyFeatured, boolean findEmails, boolean sortByLastReply)
			throws HibernateException
	{
		Criteria criteria = getVisiblePostsCriteria(filter, onlyNew, findReplies, onlyFlagged, onlyFeatured, findEmails, sortByLastReply);
	
		addPagination(start, limit, criteria);
		
		return getUniquePostList(criteria);
	}

	public int visiblePostsCount(String filter, boolean onlyNew, boolean findReplies,
			boolean onlyFlagged, boolean onlyFeatured, boolean findEmails, boolean sortByLastReply)
			throws HibernateException
	{
		Criteria criteria = getVisiblePostsCriteria(filter, onlyNew, findReplies, onlyFlagged, onlyFeatured, findEmails, sortByLastReply);
		
		return getUniquePostCount(criteria);
	}

	private void addPagination(int start, int limit, Criteria criteria)
	{
		if (limit > 0)
		{
			criteria.setMaxResults(limit);
		}
	
		if (start > 0)
		{
			criteria.setFirstResult(start);
		}
	}

	private Criteria getChapterPostsCriteria(GroupChapterModel chapter, boolean findEmails, boolean findReplies, boolean frontpagebeta, String filter)
	{
		Criteria criteria = hibernateSession.createCriteria(PostModel.class);
		
		criteria.add(Restrictions.in("group", Permissions.visibleGroupsInChapter(currentUser, chapter)));
		
		addBooleanFilters(false, findReplies, false, false, findEmails, frontpagebeta, criteria);
	
		addFilter(filter, criteria);
		return criteria;
	}
}
