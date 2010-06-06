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

package ca.myewb.model;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import ca.myewb.frame.HibernateUtil;
import ca.myewb.frame.SafeHibList;
import ca.myewb.logic.ApplicationSessionLogic;

public class ApplicationSessionModel extends ApplicationSessionLogic
{
	public static ApplicationSessionModel newApplicationSession(String name, String engInstructions, String frInstructions, String completedAppMessage, String closeEmailText , Date openDate, Date dueDate, Date closeDate, String rejectionEmailText)
	{
		ensureDateCorrectness(openDate, dueDate, closeDate);
		
		ApplicationSessionModel session = new ApplicationSessionModel(name, engInstructions, frInstructions, completedAppMessage, closeEmailText, openDate, dueDate, closeDate, rejectionEmailText);
		HibernateUtil.currentSession().save(session);
		
		return session;
	}

	private static void ensureDateCorrectness(Date openDate, Date dueDate, Date closeDate) throws IllegalArgumentException
	{
		if(openDate.getTime() > dueDate.getTime())
		{
			throw new IllegalArgumentException("The session cannot open after it is due");
		}
		
		if(dueDate.getTime() > closeDate.getTime())
		{
			throw new IllegalArgumentException("The session cannot be due after it closes");
		}
	}
	
	public static List<ApplicationSessionModel> getOpenApplicationSessions() 
	{
		SafeHibList<ApplicationSessionModel> openSessions = new SafeHibList<ApplicationSessionModel>(HibernateUtil.currentSession().createCriteria(ApplicationSessionModel.class)
				.add(Restrictions.le("openDate", new Date()))
				.add(Restrictions.gt("closeDate", new Date())));
		return openSessions.list();
	}
	
	ApplicationSessionModel()
	{
		super();
	}

	ApplicationSessionModel(String name, String engInstructions, String frInstructions, String completedAppMessage, String closeEmailText, Date openDate, Date dueDate, Date closeDate, String rejectionEmailText)
	{
		super(name, engInstructions, frInstructions, completedAppMessage, closeEmailText, openDate, dueDate, closeDate, rejectionEmailText);
	}
	
	public ApplicationQuestionModel addQuestion(String q)
	{
		ApplicationQuestionModel question = ApplicationQuestionModel.newApplicationQuestion(q, this);
		getQuestions().add(question);
		return question;
	}
	
	public void close()
	{
		Date now = new Date();
		setCloseDate(now);
		if(now.getTime() < getDueDate().getTime())
		{
			setDueDate(now);
		}
		
		if(now.getTime() < getOpenDate().getTime())
		{
			setOpenDate(now);
		}
	}
	
	public void save(String name, String instructions, String frInstructions, String completedAppMessage, String closeEmailText, Date openDate, Date dueDate, Date closeDate, String rejectionEmailText)
	{
		ensureDateCorrectness(openDate, dueDate, closeDate);
		
		setName(name);
		setInstructions(instructions);
		setCompletedApplicationMessage(completedAppMessage);
		setCloseEmailText(closeEmailText);
		setFrenchInstructions(frInstructions);
		setRejectionEmailText(rejectionEmailText);
		
		setOpenDate(openDate);
		setDueDate(dueDate);
		setCloseDate(closeDate);
	}

	public static List<ApplicationSessionModel> getClosedApplicationSessions()
	{
		SafeHibList<ApplicationSessionModel> openSessions = new SafeHibList<ApplicationSessionModel>(HibernateUtil.currentSession().createCriteria(ApplicationSessionModel.class)
				.add(Restrictions.le("closeDate", new Date())));
		return openSessions.list();
		
	}

	public static List<ApplicationSessionModel> getFutureApplicationSessions()
	{
		SafeHibList<ApplicationSessionModel> openSessions = new SafeHibList<ApplicationSessionModel>(HibernateUtil.currentSession().createCriteria(ApplicationSessionModel.class)
				.add(Restrictions.gt("openDate", new Date())));
		return openSessions.list();	
	}

	public void open()
	{
		setOpenDate(new Date());
	}

	public void reopen(Date dueDate, Date closeDate)
	{
		Date now = new Date();
		
		ensureDateCorrectness(now, dueDate, closeDate);
		
		open();
		setDueDate(dueDate);
		setCloseDate(closeDate);
	}

	public ApplicationQuestionModel getNextQuestion(ApplicationQuestionModel question) 
	{
		int order = 0;
		if (question != null)
		{
			order = question.getQuestionOrder();
		}
		Session session = HibernateUtil.currentSession();
		List remainingQs = session.createQuery("FROM ApplicationQuestionModel q WHERE q.session.id = :sessionid AND q.questionOrder > :prevOrder ORDER BY q.questionOrder").setInteger("sessionid", getId()).setInteger("prevOrder", order).list();
		if(remainingQs.isEmpty())
		{
			return null;
		}
		return (ApplicationQuestionModel)remainingQs.get(0);
	}

	public List<String> getApplicantEmails(boolean includeRejectedApplications)
	{
		List<String> applicants = new SafeHibList<String>(HibernateUtil.currentSession().createQuery("SELECT a.user.email FROM ApplicationModel a WHERE a.session.id = :sessionid").setInteger("sessionid", getId())).list();
	
		if(!includeRejectedApplications)
		{
			List<String> rejects = new SafeHibList<String>(HibernateUtil.currentSession().createQuery("SELECT a.user.email FROM ApplicationModel a WHERE a.session.id = :sessionid AND a.evaluation.rejectionSent = true").setInteger("sessionid", getId())).list();
			applicants.removeAll(rejects);
		}
		
		return applicants;
	}

	public void croned()
	{
		setEmailSent(true);
	}

	public static List<ApplicationSessionModel> getRecentlyClosedSessions(int pastWeeks)
	{
		Calendar cal = GregorianCalendar.getInstance();
		cal.add(Calendar.DATE, pastWeeks * -7);
		
		SafeHibList<ApplicationSessionModel> openSessions = new SafeHibList<ApplicationSessionModel>(HibernateUtil.currentSession().createCriteria(ApplicationSessionModel.class)
				.add(Restrictions.gt("closeDate", cal.getTime()))
				.add(Restrictions.le("closeDate", new Date())));
		return openSessions.list();	
		
	}

	public EvaluationCriteriaModel addCriteria(String criteriaText, String tinyText)
	{
		EvaluationCriteriaModel c = EvaluationCriteriaModel.newEvaluationCriteria(criteriaText, tinyText, this);
		getEvalCriteria().add(c);
		return c;
	}
}
