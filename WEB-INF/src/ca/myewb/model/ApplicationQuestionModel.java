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

import org.hibernate.criterion.Restrictions;

import ca.myewb.frame.HibernateUtil;
import ca.myewb.logic.ApplicationQuestionLogic;

public class ApplicationQuestionModel extends ApplicationQuestionLogic
{

	ApplicationQuestionModel(String q, ApplicationSessionModel session, int order)
	{
		super(q, session, order);
	}
	
	ApplicationQuestionModel()
	{
		super();
	}

	static ApplicationQuestionModel newApplicationQuestion(String q, ApplicationSessionModel session)
	{
		int newOrder = session.getQuestions().size() + 1;
		ApplicationQuestionModel question = new ApplicationQuestionModel(q, session, newOrder);
		HibernateUtil.currentSession().save(question);
		
		return question;
	}
	
	public void save(String q)
	{
		setQuestion(q);
	}
	
	public void delete()
	{
		getSession().getQuestions().remove(this);
		setSession(null);
	}
	
	public void upOrder()
	{
		if(getQuestionOrder() > 1)
		{
			ApplicationQuestionModel swap = (ApplicationQuestionModel)HibernateUtil.currentSession().createCriteria(ApplicationQuestionModel.class).add(Restrictions.eq("session", getSession())).add(Restrictions.eq("questionOrder", getQuestionOrder() - 1)).uniqueResult();
			swap.setQuestionOrder(swap.getQuestionOrder() + 1);
			setQuestionOrder(getQuestionOrder() - 1);
		}
	}
	
	public void downOrder()
	{
		if(getQuestionOrder() < getSession().getQuestions().size())
		{
			ApplicationQuestionModel swap = (ApplicationQuestionModel)HibernateUtil.currentSession().createCriteria(ApplicationQuestionModel.class).add(Restrictions.eq("session", getSession())).add(Restrictions.eq("questionOrder", getQuestionOrder() + 1)).uniqueResult();
			swap.setQuestionOrder(swap.getQuestionOrder() - 1);
			setQuestionOrder(getQuestionOrder() + 1);
		}
	}

}
