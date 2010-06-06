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

package ca.myewb.controllers.chapter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.velocity.context.Context;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Projections;

import ca.myewb.controllers.common.Member;
import ca.myewb.frame.HibernateUtil;
import ca.myewb.frame.Controller;
import ca.myewb.model.UserModel;


public class FindDupes extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		UserModel targetUser = (UserModel)getAndCheckFromUrl(UserModel.class);
		
		Member.viewMember(ctx, targetUser, false, false);

		findEmailMatch(ctx, targetUser.getLastname(), "lastnameInEmail");
		findEmailMatch(ctx, targetUser.getUsername(), "usernameInEmail");
		findEmailMatch(ctx, targetUser.getEmail().split("@")[0], "emailUserInEmail");
		findNameMatch(ctx, targetUser, "sameNames");
		findUsernameMatch(ctx, targetUser, "emailUserInUsername");
	}

	private void findEmailMatch(Context ctx, String toMatch, String key)
	{
		int maxSize = 11;
		String query = "SELECT userid FROM useremails e WHERE e.email LIKE '%" + toMatch + "%'";
		List ids = HibernateUtil.currentSession().createSQLQuery(query).list();
		if(ids.isEmpty()) 
		{
			ctx.put(key, new Vector());
			return;
		}
		
		Criteria crit = hibernateSession.createCriteria(UserModel.class);
		List uniqueResultsList = crit.add(Restrictions.in("id", ids))
			.add(Restrictions.ne("id", new Integer(1)))
			.setProjection(Projections.groupProperty("id"))
			.setMaxResults(maxSize)
			.list();

		Vector<UserModel> uniqueResults = new Vector<UserModel>();

		if (uniqueResultsList.size() < maxSize)
		{
			Iterator iter = uniqueResultsList.iterator();

			while (iter.hasNext())
			{
				Integer i = (Integer)iter.next();

				// This try/catch block is a workaround to the deleted-admin-causes-cgilib-blowup bug
				try
				{
					uniqueResults.add((UserModel)hibernateSession.get(UserModel.class, i));
				}
				catch (Exception e)
				{
					log.warn("Unable to add user to usersearch: id " + i.toString());
				}
			}
			ctx.put(key, uniqueResults);
		}
	}

	private void findNameMatch(Context ctx, UserModel targetUser, String key)
	{
		int maxSize = 11;
		
		Criteria crit = hibernateSession.createCriteria(UserModel.class);
		List uniqueResultsList = crit.add(Restrictions.eq("firstname", targetUser.getFirstname()))
			.add(Restrictions.eq("lastname", targetUser.getLastname()))
			.add(Restrictions.ne("id", new Integer(1)))
			.setProjection(Projections.groupProperty("id"))
			.setMaxResults(maxSize)
			.list();

		Vector<UserModel> uniqueResults = new Vector<UserModel>();

		if (uniqueResultsList.size() < maxSize)
		{
			Iterator iter = uniqueResultsList.iterator();

			while (iter.hasNext())
			{
				Integer i = (Integer)iter.next();

				// This try/catch block is a workaround to the deleted-admin-causes-cgilib-blowup bug
				try
				{
					uniqueResults.add((UserModel)hibernateSession.get(UserModel.class, i));
				}
				catch (Exception e)
				{
					log.warn("Unable to add user to usersearch: id " + i.toString());
				}
			}
			ctx.put(key, uniqueResults);
		}
	}
	
	private void findUsernameMatch(Context ctx, UserModel targetUser, String key)
	{
		int maxSize = 11;
		
		Criteria crit = hibernateSession.createCriteria(UserModel.class);
		List uniqueResultsList = crit
			.add(Restrictions.like("username", "%" + targetUser.getEmail().split("@")[0] + "%"))
			.add(Restrictions.ne("id", new Integer(1)))
			.setProjection(Projections.groupProperty("id"))
			.setMaxResults(maxSize)
			.list();

		Vector<UserModel> uniqueResults = new Vector<UserModel>();

		if (uniqueResultsList.size() < maxSize)
		{
			Iterator iter = uniqueResultsList.iterator();

			while (iter.hasNext())
			{
				Integer i = (Integer)iter.next();

				// This try/catch block is a workaround to the deleted-admin-causes-cgilib-blowup bug
				try
				{
					uniqueResults.add((UserModel)hibernateSession.get(UserModel.class, i));
				}
				catch (Exception e)
				{
					log.warn("Unable to add user to usersearch: id " + i.toString());
				}
			}
			ctx.put(key, uniqueResults);
		}
	}


	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Admin");

		return s;
	}

	public String displayName()
	{
		return "Possible Dupes";
	}

	public int weight()
	{
		return -100;
	}
}
