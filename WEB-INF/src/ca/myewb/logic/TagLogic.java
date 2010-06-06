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

package ca.myewb.logic;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;

import sun.text.Normalizer;

import ca.myewb.beans.Tag;
import ca.myewb.frame.HibernateUtil;
import ca.myewb.frame.Permissions;
import ca.myewb.model.EventModel;
import ca.myewb.model.GroupModel;
import ca.myewb.model.PostModel;
import ca.myewb.model.UserModel;

public abstract class TagLogic extends Tag {

	protected void addPost(PostLogic p) {
		posts.add((PostModel)p);
	}

	protected void remPost(PostLogic p) {
		posts.remove(p);
	}
	
	protected void addEvent(EventLogic e) {
			events.add((EventModel)e);
	}

	protected void remEvent(EventLogic e) {
		events.remove(e);
	}

	public boolean equals(TagLogic t) {
		return ((t.getName().equals(name)));
	}
	
	public static List<String> extractTagNames(String unsplitTags)
	{
		String[] tags = unsplitTags.split(",");
		Vector<String> result = new Vector<String>();
		
		for (String tag: tags)
		{
			tag = tag.trim();
			if (!tag.equals(""))
			{
				tag = tag.substring(0, 1).toUpperCase()
				          + tag.substring(1).toLowerCase();
				tag =( tag.length() > 50 ? tag.substring(0, 50) : tag);
				tag = Normalizer.normalize(tag, Normalizer.DECOMP, 0); //decompose accented into ascii plus unicode accent mark
				tag = tag.replaceAll("[^\\p{ASCII}]",""); //nuke non-ascii (so unicode accent marks from above)
	
				result.add(tag);
			}
		}
		return result;
	}

	public static List getMatchingVisibleTags(UserModel currentUser, int pageSize, boolean posts)
			throws HibernateException
	{	
		String queryString = "";
		queryString += "SELECT t.name, count(*) as num ";
		
		
		if(posts)
		{
			queryString += "FROM tags t, groups g, tags2posts x, posts p ";
			queryString += "WHERE t.id = x.tagid AND x.postid = p.id  AND p.groupid = g.id ";
			queryString += "AND datediff(now(), p.date) < 24*30  ";
		}
		else
		{
			queryString += "FROM tags t, groups g, tags2events x, events e ";
			queryString += "WHERE t.id = x.tagid AND x.eventid = e.id AND e.groupid = g.id ";
		}
	
		if (!currentUser.isAdmin())
		{
			Iterator<GroupModel> groups = Permissions.visibleGroups(currentUser, true, false).iterator();
			queryString += "AND g.id in (" + groups.next().getId();
			while (groups.hasNext())
			{
				queryString += ", " + groups.next().getId();
			}
			queryString += ") ";
		}
	
		queryString += "GROUP BY name ORDER BY num desc";
	
		Query query = HibernateUtil.currentSession().createSQLQuery(queryString).addScalar("name",
				Hibernate.STRING).addScalar("num", Hibernate.INTEGER)
				.setMaxResults(pageSize);
	
		return query.list();
	}

}
