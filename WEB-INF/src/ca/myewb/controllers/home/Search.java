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

package ca.myewb.controllers.home;

import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.velocity.context.Context;
import org.hibernate.Hibernate;
import org.hibernate.Query;

import ca.myewb.frame.Controller;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.PostParamWrapper;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.forms.SearchForm;
import ca.myewb.model.EventModel;
import ca.myewb.model.GroupModel;
import ca.myewb.model.PostModel;
import ca.myewb.model.WhiteboardModel;

public class Search extends Controller
{
	private String[] types = {"Posts", "Events", "Whiteboards"};
	
	public void handle(Context ctx) throws Exception
	{
		if ((requestParams.get("Body") != null) && !requestParams.get("Body").equals(""))
		{
			throw new RedirectionException(path + "/home/Search/" + encode(requestParams));
		}

		urlParams.processParams(new String[]{"filter", "pagenum"},
		                        new String[]{"", "1"});
		

		HashMap<String, String> terms = decode(urlParams.get("filter"));

		if(terms.get("Body").equals(""))
		{
			ctx.put("noterms", true);
		}
		else
		{
			ctx.put("noterms", false);
			
			int pagesize = 30;
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
			
			int numSearchables = numMatchingSearchables(terms);
		
			ctx.put("searchables", matchingSearchables(terms, pageStart, pagesize));
			
			
		
			int numPages = (numSearchables / pagesize);
			if ((numSearchables % pagesize) != 0)
			{
				numPages++;
			}
		
			ctx.put("pageNum", new Integer(start));
			ctx.put("pageSize", new Integer(pagesize));
			ctx.put("numPages", new Integer(numPages));
			ctx.put("filterParam", urlParams.get("filter"));
			ctx.put("numSearchables", new Integer(numSearchables));
		}
		
		ctx.put("form", new SearchForm(path + "/home/Search/", terms));
	}

	private List matchingSearchables(HashMap<String, String> terms, int pageStart, int pagesize)
	{
		String sql = "select parentPost, parentEvent, parentWhiteboard " 
			+ getCommonQueryPart(terms) + " limit :start, :limit";
		
		Query query = hibernateSession.createSQLQuery(sql)
		.addScalar("parentPost", Hibernate.INTEGER)
		.addScalar("parentEvent", Hibernate.INTEGER)
		.addScalar("parentWhiteboard", Hibernate.INTEGER)
		.setString("terms", terms.get("Body"))
		.setInteger("start", pageStart)
		.setInteger("limit", pagesize);

		if(!terms.get("Since").equals(""))
			query.setString("since", terms.get("Since"));
		
		List searchableIDs = query.list();

		Vector<Object> searchables = new Vector<Object>();
		for (Object temp : searchableIDs)
		{
			Object[] idArray = (Object[])temp;
			Integer parentPostId = (Integer)idArray[0];
			Integer parentEventId = (Integer)idArray[1];
			Integer parentWhiteboardId = (Integer)idArray[2];
			if(parentPostId != null)
			{
				searchables.add(hibernateSession.load(PostModel.class, parentPostId));
			}
			else if(parentEventId != null)
			{
				searchables.add(hibernateSession.load(EventModel.class, parentEventId));
			}
			else if(parentWhiteboardId != null)
			{
				searchables.add(hibernateSession.load(WhiteboardModel.class, parentWhiteboardId));
			}
		}
		
		return searchables;
	}

	private String getCommonQueryPart(HashMap<String, String> terms)
	{
		String sql = " from searchables where match(body) against (:terms) ";
		
		if ( !currentUser.isAdmin() )
		{
			sql += " AND (groupid IN (";
			
			Iterator<GroupModel> groups = Permissions.visibleGroups(currentUser, true, false).iterator();
	
			sql += groups.next().getId();
			while(groups.hasNext())
			{
				sql += ", " + groups.next().getId();
			}
			
			sql += ")) ";
		}
		
		if(!terms.get("Posts").equals("on"))
			sql += " and parentPost is null ";
		
		if(!terms.get("Events").equals("on"))
			sql += " and parentEvent is null ";
		
		if(!terms.get("Whiteboards").equals("on"))
			sql += " and parentWhiteboard is null ";
		
		if(!terms.get("Since").equals(""))
			sql += " and date >= :since ";
		
		return sql;
	}

	private int numMatchingSearchables(HashMap<String, String> terms)
	{
		String sql = "select count(*) " + getCommonQueryPart(terms);
		
		Query query = hibernateSession.createSQLQuery(sql)
		.setString("terms", terms.get("Body"));

		if(!terms.get("Since").equals(""))
			query.setString("since", terms.get("Since"));

		return((BigInteger)query.uniqueResult()).intValue();
	}
	
	public HashMap<String, String> decode(String searchTerm) throws Exception
	{
		HashMap <String, String> lines = new HashMap<String, String>();

		lines.put("Body", "");
		lines.put("Since", "");
		lines.put("Mask", "111");
        
		String[] keys = searchTerm.split("&");
		for(int i = 0; i < keys.length; i++)
		{
			String line = keys[i];
			String[] keyValue = line.split("=");
			lines.put(keyValue[0], keyValue.length > 1 ? URLDecoder.decode(keyValue[1], "UTF-8") : "");
		}
		
		String mask = lines.remove("Mask");
		if(mask.equals("000"))
			mask = "111";
		
		for(int i=0; i<types.length; i++)
		{
			if(mask.charAt(i) == '1')
				lines.put(types[i], "on");
			else
				lines.put(types[i], "");
		}
		
		return lines;
	}

	private String encode(PostParamWrapper requestParams) throws Exception
	{
		String bodyTerm = requestParams.get("Body") != null ? 
				URLEncoder.encode(requestParams.get("Body").replaceAll("/"," "), "UTF-8") : "";
		String sinceTerm = requestParams.get("Since") != null ? 
				URLEncoder.encode(requestParams.get("Since"), "UTF-8") : "";
		
		String mask = "";
		for(String type: types)
		{
			if(requestParams.get(type) != null && requestParams.get(type).equals("on"))
				mask += "1";
			else
				mask += "0";
		}
				
		return "Body=" + bodyTerm + "&Since=" + sinceTerm + "&Mask=" + mask;
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Org");

		return s;
	}

	public String displayName()
	{
		return "Search";
	}
}
