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
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.velocity.context.Context;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;

import ca.myewb.frame.GetParamWrapper;
import ca.myewb.frame.Controller;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.PostParamWrapper;
import ca.myewb.model.UserModel;
import ca.myewb.model.WhiteboardModel;


public class WhiteboardList extends Controller
{
	public WhiteboardList(HttpSession httpSession, Session hibernate,
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

	public void list(Context ctx, int pagesize) throws Exception
	{
		urlParams.processParams(new String[]{"parentType", "pagenum"},
                new String[]{"Any", "1"});

		int pageNo = 1;
		int firstWhiteboard = 0;
		int numTotalWhiteboards = 0;
				
		List<WhiteboardModel> whiteboards = null;
		
		//Retrieve all the whiteboards
		try{
			pageNo = new Integer(urlParams.get("pagenum")).intValue();
		}
		catch( NullPointerException npe )
		{
			pageNo = 1;
		}
		
		firstWhiteboard = (pageNo - 1) * pagesize;
		
		whiteboards = listPaginatedVisibleWhiteboards(firstWhiteboard, pagesize);
		numTotalWhiteboards = visibleWhiteboardCount();
		
		
		ctx.put("pageNum", new Integer(pageNo));
		ctx.put("pageSize", new Integer(pagesize));

		int numPages = (numTotalWhiteboards / pagesize);

		if ((numTotalWhiteboards % pagesize) != 0)
		{
			numPages++;
		}

		ctx.put("numPages", new Integer(numPages));
				
		ctx.put("whiteboards", whiteboards);
	}

	public void handle(Context ctx) throws Exception
	{
		// You should never come here directly!
		throw getSecurityException("Someone accessed common/WhiteboardList directly!",
		                           path + "/home/Home");
	}
	
	public List<WhiteboardModel> listPaginatedVisibleWhiteboards(int startPage,
			int eventsPerPage)
			throws HibernateException
	{
		Criteria criteria = hibernateSession.createCriteria(WhiteboardModel.class);

		if(!currentUser.isAdmin() || !currentUser.getAdminToggle())
		{
			criteria.add( Restrictions.in("group", Permissions.visibleGroups(currentUser, true)) );
		}
		criteria.add(Restrictions.ne("numEdits", 0));
		criteria.add(Restrictions.gt("lastEditDate", currentUser.getLastLogin()));
		criteria.addOrder(Order.desc("lastEditDate"));
		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		
		addPagination(startPage, eventsPerPage, criteria);
		
		return getUniqueWhiteboardList(criteria);
	}

	private void addPagination(int startPage, int whiteboardsPerPage, Criteria criteria)
	{
		if (whiteboardsPerPage > 0)
		{
			criteria.setMaxResults(whiteboardsPerPage);
		}
	
		if (startPage > 0)
		{
			criteria.setFirstResult(startPage);
		}
	}

	private List<WhiteboardModel> getUniqueWhiteboardList(Criteria criteria)
	{
		criteria.setProjection(Projections.groupProperty("id"));
	
		Iterator it = criteria.list().iterator();
	
		List<WhiteboardModel> list = new ArrayList<WhiteboardModel>();
	
		while (it.hasNext())
		{
			list.add((WhiteboardModel) hibernateSession.load(WhiteboardModel.class, (Integer) it
					.next()));
		}
	
		return list;
	}

	public int visibleWhiteboardCount()
			throws HibernateException
	{
		Criteria criteria = hibernateSession.createCriteria(WhiteboardModel.class);

		if(!currentUser.isAdmin() || !currentUser.getAdminToggle())
		{
			criteria.add( Restrictions.in("group", Permissions.visibleGroups(currentUser, true)) );
		}
		criteria.add(Restrictions.ne("numEdits", 0));
		criteria.add(Restrictions.gt("lastEditDate", currentUser.getLastLogin()));
		
		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		
		return getUniqueWhiteboardCount(criteria);
	}
	
	private int getUniqueWhiteboardCount(Criteria criteria)
	{
		criteria.setProjection(Projections.groupProperty("id"));
		return criteria.list().size();
	}

}
