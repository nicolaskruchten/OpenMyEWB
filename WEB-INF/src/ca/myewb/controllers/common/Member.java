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

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpSession;

import org.apache.velocity.context.Context;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;

import ca.myewb.frame.GetParamWrapper;
import ca.myewb.frame.HibernateUtil;
import ca.myewb.frame.Message;
import ca.myewb.frame.Helpers;
import ca.myewb.frame.Controller;
import ca.myewb.frame.PostParamWrapper;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.forms.MemberSearchForm;
import ca.myewb.model.GroupChapterModel;
import ca.myewb.model.UserModel;


public class Member extends Controller
{
	public Member(HttpSession httpSession, Session hibernate,
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


	// This allows for searching one user
	// The view profile is automatically displayed, and the user is
	// returned in case the calling page wants to provide more options
	// ** Calling pages must #parse($memberpage) in their template!!!
	//    and only process the results if this doesn't return null!!!
	public UserModel view(Context ctx) throws Exception
	{		
		try //try loading the user directly from the url
		{
			Integer userId = new Integer(urlParams.get("mode"));
			ctx.put("memberpage", "frame/memberinfo.vm");
			return (UserModel)getAndCheck(UserModel.class, userId);
		}
		catch(NumberFormatException nfe) //just means it wasn't a userid!
		{
			if (getInterpageVar("membersearchtarget") == null) //this is not good
			{
				throw new RedirectionException(path + "/home/Home");
			}
	
			if (urlParams.get("mode").equals("new") || urlParams.get("mode").equals("advanced"))
			{
				removeInterpageVar("membersearchtempresults");
				newOrAdvancedMode(ctx);
			}
			else if (urlParams.get("mode").equals("search"))
			{
				searchMode(ctx);
			}
			else
			{
				throw getSecurityException("That URL is not allowed.", path + "home/Home");
			}
			return null;
		}
	}

	public static void viewMember(Context ctx, UserModel targetUser, boolean verbose, boolean restricted)
	{
		ctx.put("targetUser", targetUser);
		
		if(verbose)
		{
			ctx.put("verbose", "yes");
		}
		
		if(restricted)
		{
			ctx.put("restrictedView", "yes");
		}
		
		if (!targetUser.isMember(Helpers.getGroup("Deleted"), false))
		{
			ctx.put("userexists", "yes");
		}
		
		 //admins step in here, which is ok, the template skips over basicType for them
		if (targetUser.isMember(Helpers.getGroup("Associate"), false))
		{
			ctx.put("basicType", "Associate");
		}
		else if (targetUser.isMember(Helpers.getGroup("Regular"), false))
		{
			ctx.put("basicType", "Regular");
		}
		else if (targetUser.isMember(Helpers.getGroup("Deleted"), false))
		{
			ctx.put("basicType", "deleted");
		}
		else
		{
			ctx.put("basicType", "Mailing List");
		}

		ctx.put("chapter", targetUser.getChapter());
		ctx.put("execTitle", targetUser.getExecTitle());

		if (targetUser.isMember("Admin", false))
		{
			ctx.put("adminUser", "true");
		}
		else if (targetUser.isMember("NMT", false))
		{
			ctx.put("nmtUser", "true");
		}
	}

	private void newOrAdvancedMode(Context ctx) throws Exception, RedirectionException
	{
		MemberSearchForm searchForm = (MemberSearchForm)checkForValidationFailure(ctx);

		if (searchForm == null) // this is the first entry into this page     
		{
			List result = null;

			if (currentUser.isAdmin())
			{
				result = hibernateSession.createQuery("FROM GroupChapterModel where visible=true")
				         .list();
			}

			searchForm = new MemberSearchForm(getInterpageVar("membersearchtarget")
			                            + "/search", requestParams,
			                            !urlParams.get("mode").equals("new"), result);
			ctx.put("advanced", new Boolean(!urlParams.get("mode").equals("new")));
		}
		
		ctx.put("tempresults", new Vector());
		if (searchForm == null)
		{
			log.info("search form was null!");
			throw new RedirectionException(getInterpageVar("membersearchtarget")
			                               + "/new");
		}

		ctx.put("form", searchForm);
		ctx.put("target", getInterpageVar("membersearchtarget"));
	}

	private void searchMode(Context ctx) throws Exception, RedirectionException
	{
		MemberSearchForm searchForm = null;
		List result = null;

		if (currentUser.isAdmin())
		{
			result = hibernateSession.createQuery("FROM GroupChapterModel where visible=true")
			         .list();
		}

		// run search, store results in temp list
		if (requestParams.get("Advanced") != null)
		{
			searchForm = new MemberSearchForm(getInterpageVar("membersearchtarget")
			                            + "/search", requestParams,
			                            true, result);
			ctx.put("advanced", new Boolean(true));
		}
		else
		{
			searchForm = new MemberSearchForm(getInterpageVar("membersearchtarget")
			                            + "/search", requestParams,
			                            false, result);
		}

		Message m = searchForm.validate();

		if (m != null) // validation failed, redirect to self, next time we'll be entering the next block
		{
			// Display error and prompt user to fix
			throw getValidationException(searchForm, m,
			                             (String)getInterpageVar("membersearchtarget"));
		}

		//form validation succeeded!
		String first = searchForm.getParameter("Firstname");
		String last = searchForm.getParameter("Lastname");
		String email = searchForm.getParameter("Email");
		String city = searchForm.getParameter("City", false);
		String province = searchForm.getParameter("Province", false);
		String lang = searchForm.getParameter("Language", false);
		String gender = searchForm.getParameter("Gender", false);
		String birth = searchForm.getParameter("Birth", false);
		String student = searchForm.getParameter("Student", false);
		String username = searchForm.getParameter("Username", false);

		Criteria crit = hibernateSession.createCriteria(UserModel.class);

		if ((username != null) && !username.equals(""))
		{
			crit.add(Restrictions.like("username",
			                         "%" + username.trim() + "%"));
		}

		if ((first != null) && !first.equals(""))
		{
			crit.add(Restrictions.like("firstname",
			                         "%" + first.trim() + "%"));
		}

		if ((last != null) && !last.equals(""))
		{
			crit.add(Restrictions.like("lastname", "%" + last.trim()
			                         + "%"));
		}

		if ((email != null) && !email.equals(""))
		{
			List ids = HibernateUtil.currentSession().createSQLQuery("SELECT userid FROM useremails e WHERE e.email LIKE '%" + email.trim() + "%'").list();
			if(!ids.isEmpty())
			{
				crit.add(Restrictions.in("id", ids));
			}
			else
			{
				crit.add(Restrictions.eq("email", "###invalidemail###")); //so that no results are given
			}
		}

		if ((city != null) && !city.equals(""))
		{
			crit.add(Restrictions.like("address",
			                         "%\n%" + city.trim() + "%\n%"));
		}

		if ((province != null) && !province.equals(""))
		{
			crit.add(Restrictions.like("address",
			                         "%\n%" + province.trim() + "%\n%"));
		}

		if ((lang != null) && !lang.equals(""))
		{
			crit.add(Restrictions.eq("language", lang.trim()));
		}

		if ((gender != null) && !gender.equals(""))
		{
			crit.add(Restrictions.eq("gender", gender.trim()));
		}

		if ((birth != null) && !birth.equals(""))
		{
			crit.add(Restrictions.eq("birth", new Integer(birth)));
		}

		if ((student != null) && !student.equals(""))
		{
			crit.add(Restrictions.eq("student", new Boolean(student)));
		}

		// Get "my" own lead groups, since I can only
		// see people in groups I lead
		crit.createAlias("roles", "r");
		crit.add(Restrictions.isNull("r.end"));

		if (!currentUser.isAdmin())
		{
			crit.add(Restrictions.in("r.group", currentUser.getGroups('l')));
		}
		else
		{
			GroupChapterModel chapter = null;

			if (searchForm.getParameter("Chapter", false) != null)
			{
				if (!searchForm.getParameter("Chapter", false).equals(""))
				{
					chapter = (GroupChapterModel)hibernateSession.get(GroupChapterModel.class,
					                                             new Integer(searchForm
					                                                         .getParameter("Chapter",
					                                                                       false)));
				}
			}

			if (chapter != null)
			{
				crit.add(Restrictions.eq("r.group", chapter));
				crit.add(Restrictions.eq("r.level", new Character('m')));
			}

			//don't filter out deleted users!
		}

		crit.add(Restrictions.ne("id", new Integer(1)));

		crit.addOrder(Order.asc("lastname"));
		crit.addOrder(Order.asc("firstname"));
		crit.setProjection(Projections.groupProperty("id"));
		crit.setMaxResults(101);

		List uniqueResultsList = crit.list();
		Vector<UserModel> uniqueResults = new Vector<UserModel>();

		if (uniqueResultsList.size() < 101)
		{
			Iterator iter = uniqueResultsList.iterator();

			while (iter.hasNext())
			{
				Integer i = (Integer)iter.next();

				// This try/catch block is a workaround to the deleted-admin-causes-cgilib-blowup bug
				try
				{
					uniqueResults.add((UserModel)hibernateSession.get(UserModel.class,
					                                             i));
				}
				catch (Exception e)
				{
					log.warn("Unable to add user to usersearch: id "
					         + i.toString());
				}
			}
		}
		else
		{
			ctx.put("tooMany", "yes");
		}

		setInterpageVar("membersearchtempresults", uniqueResultsList);
		ctx.put("tempresults", uniqueResults); //NOT the ids, but the users
		ctx.put("searchmode", "yes");
		if (searchForm == null)
		{
			log.info("search form was null!");
			throw new RedirectionException(getInterpageVar("membersearchtarget")
			                               + "/new");
		}

		ctx.put("form", searchForm);
		ctx.put("target", getInterpageVar("membersearchtarget"));
	}

	public void handle(Context ctx) throws Exception
	{
		// You should never come here directly!
		throw getSecurityException("Someone accessed common/Member directly!",
		                           path + "/home/Home");
	}

	public static List<String> getRequiredInterpageVars()
	{
		Vector<String> vars = new Vector<String>();
		vars.add("membersearchtarget"); //preserve this for hitting 'back' to FindMember
		vars.add("membersearchtempresults"); //preserve this for hitting 'back' to FindMember

		return vars;
	}
}
