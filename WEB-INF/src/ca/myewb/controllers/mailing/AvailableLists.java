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

package ca.myewb.controllers.mailing;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.velocity.context.Context;
import org.hibernate.Criteria;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Order;

import ca.myewb.frame.Helpers;
import ca.myewb.frame.Controller;
import ca.myewb.frame.SafeHibList;
import ca.myewb.model.GroupModel;
import ca.myewb.model.GroupChapterModel;


public class AvailableLists extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		List<GroupModel> currentLists = currentUser.getGroups();
		GroupChapterModel chapter = currentUser.getChapter();
		Criteria crit = null;
		Hashtable<String, List<GroupModel>> hash = new Hashtable<String, List<GroupModel>>();
		List<String> names = new Vector<String>();

		//chapter lists
		crit = hibernateSession.createCriteria(GroupChapterModel.class);
		crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		crit.add(Restrictions.eq("visible", new Boolean(true)));
		crit.addOrder(Order.asc("name"));

		List<GroupModel> chapterLists = (new SafeHibList<GroupModel>(crit)).list();
		chapterLists.removeAll(currentLists);

		//general public lists
		crit = hibernateSession.createCriteria(GroupModel.class);
		crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		crit.add(Restrictions.eq("admin", new Boolean(false)));
		crit.add(Restrictions.eq("visible", new Boolean(true)));

		crit.add(Restrictions.isNull("parent"));
		crit.add(Restrictions.eq("public", new Boolean(true)));

		List<GroupModel> generalPublicLists = (new SafeHibList<GroupModel>(crit)).list();
		generalPublicLists.removeAll(currentLists);
		generalPublicLists.removeAll(chapterLists);

		log.debug("Populating available lists:");

		if (currentUser.getUsername().equals("guest"))
		{
			generalPublicLists.add(0, Helpers.getGroup("Org"));
			log.debug("Global list, for the guest");
		}

		if (!generalPublicLists.isEmpty())
		{
			hash.put("General Public Lists", generalPublicLists);
			names.add("General Public Lists");
			log.debug("General public lists");
		}
		
		if(currentUser.isMember("Exec"))
		{
			// admin level lists
			List<GroupModel> adminLists = Helpers.getNationalRepLists(true, true);
			adminLists.add(0, Helpers.getGroup("ProChaptersExec"));
			adminLists.add(0, Helpers.getGroup("UniChaptersExec"));
			adminLists.add(0, Helpers.getGroup("Exec"));
			
			adminLists.removeAll(currentLists);

			if (!adminLists.isEmpty())
			{
				hash.put("Exec and Natl Rep Lists", adminLists);
				names.add("Exec and Natl Rep Lists");
			}
		}

		if (currentUser.isAdmin())
		{

			{
				//general private lists
				crit = hibernateSession.createCriteria(GroupModel.class);
				crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
				crit.add(Restrictions.eq("admin", new Boolean(false)));
				crit.add(Restrictions.eq("visible", new Boolean(true)));

				crit.add(Restrictions.isNull("parent"));
				crit.add(Restrictions.eq("public", new Boolean(false)));

				List<GroupModel> generalPrivateLists = (new SafeHibList<GroupModel>(crit))
				                                  .list();
				generalPrivateLists.removeAll(currentLists);

				if (!generalPrivateLists.isEmpty())
				{
					hash.put("General Private Lists", generalPrivateLists);
					names.add("General Private Lists");
					log.debug("General private lists");
				}
				else
				{
					log.debug("General private lists was empty");
				}
			}

			{
				//all chapter public lists
				crit = hibernateSession.createCriteria(GroupModel.class);
				crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
				crit.add(Restrictions.eq("admin", new Boolean(false)));
				crit.add(Restrictions.eq("visible", new Boolean(true)));

				crit.add(Restrictions.isNotNull("parent"));
				crit.add(Restrictions.eq("public", new Boolean(true)));
				crit.addOrder(Order.asc("parent"));

				List<GroupModel> chapterPublicLists = (new SafeHibList<GroupModel>(crit))
				                                 .list();
				chapterPublicLists.removeAll(currentLists);

				if (!chapterPublicLists.isEmpty())
				{
					hash.put("Chapter Public Lists (any chapter)",
					         chapterPublicLists);
					names.add("Chapter Public Lists (any chapter)");
					log.debug("Chapter public lists for admin");
				}
				else
				{
					log.debug("Chapter public lists for admin; empty");
				}
			}

			{
				//all chapter private lists
				crit = hibernateSession.createCriteria(GroupModel.class);
				crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
				crit.add(Restrictions.eq("admin", new Boolean(false)));
				crit.add(Restrictions.eq("visible", new Boolean(true)));

				crit.add(Restrictions.isNotNull("parent"));
				crit.add(Restrictions.eq("public", new Boolean(false)));
				crit.addOrder(Order.asc("parent"));

				List<GroupModel> chapterPrivateLists = (new SafeHibList<GroupModel>(crit))
				                                  .list();
				chapterPrivateLists.removeAll(currentLists);

				if (!chapterPrivateLists.isEmpty())
				{
					hash.put("Chapter Private Lists (any chapter)",
					         chapterPrivateLists);
					names.add("Chapter Private Lists (any chapter)");
					log.debug("Chapter private lists, admin");
				}
				else
				{
					log.debug("Chapter private lists, admin, empty");
				}
			}
		}
		else
		{
			if (chapter != null)
			{
				//chapter public lists
				crit = hibernateSession.createCriteria(GroupModel.class);
				crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
				crit.add(Restrictions.eq("admin", new Boolean(false)));
				crit.add(Restrictions.eq("visible", new Boolean(true)));

				crit.add(Restrictions.eq("parent", chapter));
				crit.add(Restrictions.eq("public", new Boolean(true)));

				List<GroupModel> chapterPublicLists = (new SafeHibList<GroupModel>(crit))
				                                 .list();
				chapterPublicLists.removeAll(currentLists);

				if (!chapterPublicLists.isEmpty())
				{
					hash.put("Chapter Public Lists", chapterPublicLists);
					names.add("Chapter Public Lists");
					log.debug("Chapter public lists");
				}
				else
				{
					log.debug("Chapter public lists was empty");
				}

				if (currentUser.isLeader(chapter, false))
				{
					//own chapter's private lists
					crit = hibernateSession.createCriteria(GroupModel.class);
					crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
					crit.add(Restrictions.eq("admin", new Boolean(false)));
					crit.add(Restrictions.eq("visible", new Boolean(true)));

					crit.add(Restrictions.eq("parent", chapter));
					crit.add(Restrictions.eq("public", new Boolean(false)));

					List<GroupModel> chapterPrivateLists = (new SafeHibList<GroupModel>(crit))
					                                  .list();
					chapterPrivateLists.removeAll(currentLists);

					if (!chapterPrivateLists.isEmpty())
					{
						hash.put("Chapter Private Lists", chapterPrivateLists);
						names.add("Chapter Private Lists");
						log.debug("Chapter private lists");
					}
					else
					{
						log.debug("Chapter private lists was empty");
					}
				}
			}
		}

		if (!chapterLists.isEmpty())
		{
			hash.put("Chapter Lists", chapterLists);
			names.add("Chapter Lists");
			log.debug("Chapter lists");
		}

		// Stick it all in the context
		ctx.put("names", names);
		ctx.put("names2", names);
		ctx.put("hash", hash);
	}

	public Set<String> defaultGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Org");

		return s;
	}

	public String displayName()
	{
		return "Available Mailing Lists";
	}

	public int weight()
	{
		return 99;
	}
}
