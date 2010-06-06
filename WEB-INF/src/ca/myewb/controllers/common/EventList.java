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
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.velocity.context.Context;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;

import ca.myewb.frame.GetParamWrapper;
import ca.myewb.frame.Helpers;
import ca.myewb.frame.Controller;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.PostParamWrapper;
import ca.myewb.frame.SafeHibList;
import ca.myewb.model.EventModel;
import ca.myewb.model.GroupChapterModel;
import ca.myewb.model.GroupModel;
import ca.myewb.model.TagModel;
import ca.myewb.model.UserModel;


public class EventList extends Controller
{
	Logger log = Logger.getLogger(this.getClass());
	
	public EventList(HttpSession httpSession, Session hibernate,
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
		urlParams.processParams(new String[]{"filter", "pagenum"},
                new String[]{"Any", "0"});
		
		log.info("EventList started with params " + urlParams.get("filter") + ", " + urlParams.get("pagenum"));

		String filterToUse = urlParams.get("filter");
		ctx.put("filterParam", filterToUse);
		
		if (filterToUse.equals("Any"))
		{
			filterToUse = null;
		}
		
		int pageNo = 1;
		int firstEvent = 0;
		int numTotalEvents = 0;
				
		List<EventModel> events = null;
		
		log.info("EventList mode: events");
		try{
			pageNo = new Integer(urlParams.get("pagenum")).intValue();
		}
		catch( NullPointerException npe )
		{
			log.debug("EventList NPE: pageNo");
			pageNo = 0;
		}
		
		Date now = new Date();
		
		if (mode.equals("events"))
		{
			if (pageNo < 1)
			{
				pageNo = (visiblePreviousEventCount(filterToUse, now) / pagesize) + 1;
			}
			
			firstEvent = (pageNo - 1) * pagesize;
			log.info("EventList first event: " + firstEvent);
			
			events = listPaginatedVisibleEvents(filterToUse, firstEvent, pagesize, null);
			numTotalEvents = visibleEventCount(filterToUse, null);
			
		}
		else if (mode.equals("upcoming"))
		{
			if (pageNo < 1)
			{
				pageNo = 1;
			}
			
			firstEvent = (pageNo - 1) * pagesize;
			log.info("EventList first event: " + firstEvent);
			
			events = listPaginatedVisibleEvents(filterToUse, firstEvent, pagesize, now);
			numTotalEvents = visibleEventCount(filterToUse, now);
		}
		ctx.put("thetag", filterToUse);
		
		ctx.put("pageNum", new Integer(pageNo));
		ctx.put("pageSize", new Integer(pagesize));

		int numPages = (numTotalEvents / pagesize);

		if ((numTotalEvents % pagesize) != 0)
		{
			numPages++;
		}

		ctx.put("numPages", new Integer(numPages));
				
		ctx.put("events", events);
	}

	public void handle(Context ctx) throws Exception
	{
		// You should never come here directly!
		throw getSecurityException("Someone accessed common/PostList directly!",
		                           path + "/home/Home");
	}
	
	public Collection<EventModel> listVisibleEventsBetweenDates(Date start, Date end, GroupChapterModel chapter) throws HibernateException
	{
		Criteria criteria = hibernateSession.createCriteria(EventModel.class);
	
		LogicalExpression singleDayEvents = Restrictions.and(Restrictions.ge(
				"startDate", start), Restrictions.le("endDate", end));
		LogicalExpression endsToday = Restrictions.and(Restrictions.lt("startDate",
				start), Restrictions.and(Restrictions.ge("endDate", start),
				Restrictions.le("endDate", end)));
		LogicalExpression startsToday = Restrictions.and(Restrictions.and(
				Restrictions.ge("startDate", start), Restrictions.le("startDate",
						end)), Restrictions.gt("endDate", end));
		LogicalExpression ongoing = Restrictions.and(Restrictions.lt("startDate",
				start), Restrictions.gt("endDate", end));
	
		criteria.add(Restrictions.or(singleDayEvents, Restrictions.or(endsToday,
				Restrictions.or(startsToday, ongoing))));
		
		if (chapter == null)
		{
			if(!currentUser.isAdmin())
			{
				criteria.add(Restrictions.in("group", Permissions.visibleGroups(currentUser, true)));
			}
			else
			{
				List<GroupModel> adminGroups = Helpers.getNationalRepLists(true, true);
				adminGroups.add(Helpers.getGroup("Exec"));
				adminGroups.add(Helpers.getGroup("ProChaptersExec"));
				
				criteria.add(Restrictions.in("group", adminGroups));
			}
		}
		else
		{
			criteria.add(Restrictions.in("group", Permissions.visibleGroupsInChapter(currentUser, chapter)));
		}
	
		criteria.addOrder(Order.asc("startDate"));
		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
	
		return new SafeHibList<EventModel>(criteria).list();
	}
	
	public Collection<EventModel> listVisibleEventsForDay(Date time, GroupChapterModel chapter)
	{
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(time);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.AM_PM, Calendar.AM);
	
		Date dayStart = cal.getTime();
	
		cal.add(Calendar.DATE, 1);
		cal.add(Calendar.SECOND, -1);
	
		Date dayEnd = cal.getTime();
	
		return listVisibleEventsBetweenDates(dayStart, dayEnd, chapter);
	}

	public Collection<EventModel> listVisibleEventsForQuarter(Date date, GroupChapterModel chapter) throws HibernateException
	{
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.AM_PM, Calendar.AM);
		cal.add(Calendar.MONTH, -1);
		Date monthStart = cal.getTime(); //the first of the previous month
	
		cal.add(Calendar.MONTH, 5);
		cal.add(Calendar.SECOND, -1);
		Date monthEnd = cal.getTime(); //the last of the next month
	
		return listVisibleEventsBetweenDates(monthStart, monthEnd, chapter);
	}

	public Map<Date, Set<EventModel>> mapToDateVisibleEventsForMonth(Date d, GroupChapterModel chapter)
	{
		//Get List of Events
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(d);
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 1);
		cal.set(Calendar.AM_PM, Calendar.AM);
		
		cal.add(Calendar.DATE, -cal.get(Calendar.DAY_OF_WEEK) + 1);
		Date monthStart = cal.getTime();

		cal.setTime(d);
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 1);
		cal.set(Calendar.AM_PM, Calendar.AM);
	
		cal.add(Calendar.MONTH, 1);
		cal.add(Calendar.DATE, 7);
		cal.add(Calendar.DATE, -cal.get(Calendar.DAY_OF_WEEK) + 1);
		cal.add(Calendar.SECOND, -1);
	
		Date monthEnd = cal.getTime();
		
		return mapToDateVisibleEventsBetweenDates(monthStart, monthEnd, chapter);
	}
	
	public Map<Date, Set<EventModel>> mapToDateVisibleEventsForNextNWeeks(Date d, GroupChapterModel chapter, int n)
	{
		// Get List of Events
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(d);
		cal.add(Calendar.DATE, -cal.get(Calendar.DAY_OF_WEEK) + 1);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 1);
		cal.set(Calendar.AM_PM, Calendar.AM);
	
		Date start = cal.getTime();
	
		cal.add(Calendar.WEEK_OF_YEAR, n);
		cal.add(Calendar.DATE, 7);
		cal.add(Calendar.DATE, -cal.get(Calendar.DAY_OF_WEEK) + 1);
		cal.add(Calendar.SECOND, -1);
	
		Date end = cal.getTime();
		
		return mapToDateVisibleEventsBetweenDates(start, end, chapter);
	}
	
	private Map<Date, Set<EventModel>> mapToDateVisibleEventsBetweenDates(Date start, Date end, GroupChapterModel chapter)
	{
		Hashtable<Date, Set<EventModel>> month = new Hashtable<Date, Set<EventModel>>();
		
		//Get List of Events
		Calendar cal = GregorianCalendar.getInstance();
		LinkedList<EventModel> events = new LinkedList<EventModel>(listVisibleEventsBetweenDates(start, end, chapter));
		
		//Sort Events into Dates
		for( EventModel e : events )
		{
			cal.setTime(e.getStartDate());
			cal.set(Calendar.HOUR, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 1);
			cal.set(Calendar.AM_PM, Calendar.AM);
			
			do
			{
				if( !month.containsKey(cal.getTime()) )
				{
					month.put(cal.getTime(), new HashSet<EventModel>());
				}
				month.get(cal.getTime()).add(e);
				cal.add(Calendar.DATE, 1);
			}
			while(cal.getTime().before(e.getEndDate()));
		}
		
		return month;	
	}

	public List<EventModel> listPaginatedVisibleEvents(String filter, int startPage, int eventsPerPage, Date endAfter)
			throws HibernateException
	{
		Criteria criteria = hibernateSession.createCriteria(EventModel.class);
		
		if(!currentUser.isAdmin())
		{
			log.info("EventList visible groups added to criteria");
			criteria.add(Restrictions.in("group", Permissions.visibleGroups(currentUser, true)));
		}
		
		criteria.addOrder(Order.asc("startDate"));
		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		
		addBooleanFilters(endAfter, criteria);
		addFilter(filter, criteria);
		addPagination(startPage, eventsPerPage, criteria);
		
		return getUniqueEventList(criteria);
	}

	private void addPagination(int startPage, int eventsPerPage, Criteria criteria)
	{
		if (eventsPerPage > 0)
		{
			criteria.setMaxResults(eventsPerPage);
		}
	
		if (startPage > 0)
		{
			criteria.setFirstResult(startPage);
		}
	}

	private List<EventModel> getUniqueEventList(Criteria criteria)
	{
		criteria.setProjection(Projections.groupProperty("id"));
	
		Iterator it = criteria.list().iterator();
	
		List<EventModel> list = new ArrayList<EventModel>();
	
		while (it.hasNext())
		{
			list.add((EventModel) hibernateSession.load(EventModel.class, (Integer) it
					.next()));
		}
	
		return list;
	}

	public int visibleEventCount(String filter, Date endAfter)
			throws HibernateException
	{
		Criteria criteria = hibernateSession.createCriteria(EventModel.class);
		
		if(!currentUser.isAdmin())
		{
			criteria.add(Restrictions.in("group", Permissions.visibleGroups(currentUser, true)));
		}
		
		addBooleanFilters(endAfter, criteria);
		addFilter(filter, criteria);
	
		criteria.addOrder(Order.asc("startDate"));
		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		
		return getUniqueEventCount(criteria);
	}

	public int visiblePreviousEventCount(String filter, Date fromDate)
			throws HibernateException
	{
		Criteria criteria = hibernateSession.createCriteria(EventModel.class);
		
		if(!currentUser.isAdmin())
		{
			criteria.add(Restrictions.in("group", Permissions.visibleGroups(currentUser, true)));
		}
	
		criteria.add(Restrictions.lt("startDate", fromDate));
		addFilter(filter, criteria);
		
		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		
		return getUniqueEventCount(criteria);
	}

	private int getUniqueEventCount(Criteria criteria)
	{
		criteria.setProjection(Projections.groupProperty("id"));
		return criteria.list().size();
	}

	private void addBooleanFilters(Date endAfter, Criteria criteria)
	{
		if(endAfter != null)
		{
			log.info("Events that ended after endDate set");
			criteria.add(Restrictions.ge("endDate", endAfter));
		}
	}
	
	private void addFilter(String filter, Criteria criteria)
	{
		if (filter == null)
		{
			return; // just in case
		}
	
		log.debug("Filtering events: " + filter);
	
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
	
	public List<List<Date>> getDatesInMonth(Date d)
	{
		ArrayList<List<Date>> dates = new ArrayList<List<Date>>();
		
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(d);
		
		int monthNo = cal.get(Calendar.MONTH);
		
		cal.set(Calendar.DATE, 1);
		cal.add(Calendar.DATE, -cal.get(Calendar.DAY_OF_WEEK) + 1);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 1);
		cal.set(Calendar.AM_PM, Calendar.AM);
		
		do
		{
			dates.add(new LinkedList<Date>());
			for(int i = 0; i < 7; i++)
			{
				dates.get(dates.size() - 1).add(cal.getTime());
				cal.add(Calendar.DATE, 1);
			}
		}
		while(!((cal.get(Calendar.DAY_OF_WEEK) == 1) && (cal.get(Calendar.MONTH) != monthNo)));
		
		return dates;
	}

	public List<List<Date>> getDatesInNextNWeeks(Date d, int n)
	{
		ArrayList<List<Date>> dates = new ArrayList<List<Date>>();
		
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(d);
		
		cal.add(Calendar.DATE, -cal.get(Calendar.DAY_OF_WEEK) + 1);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 1);
		cal.set(Calendar.AM_PM, Calendar.AM);
		
		cal.add(Calendar.WEEK_OF_YEAR, n);
		Date endDate = cal.getTime();
		
		cal.add(Calendar.WEEK_OF_YEAR, -n);		
		
		do
		{
			dates.add(new LinkedList<Date>());
			for(int i = 0; ( i < 7 ) && (!cal.getTime().equals(endDate)); i++)
			{
				dates.get(dates.size() - 1).add(cal.getTime());
				cal.add(Calendar.DATE, 1);
			}
		}
		while(!cal.getTime().equals(endDate) && !cal.getTime().after(endDate));
		
		return dates;
	}

}
