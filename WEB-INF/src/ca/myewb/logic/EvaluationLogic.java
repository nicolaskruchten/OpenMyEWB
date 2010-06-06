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

import ca.myewb.beans.Evaluation;
import ca.myewb.frame.HibernateUtil;
import ca.myewb.model.ApplicationModel;
import ca.myewb.model.EvaluationCriteriaModel;
import ca.myewb.model.EvaluationResponseModel;

public abstract class EvaluationLogic extends Evaluation
{
	protected EvaluationLogic(ApplicationModel app)
	{
		super();
		setApp(app);
	}
	
	protected EvaluationLogic()
	{
		super();
	}

	public EvaluationResponseModel getResponseForCriteria(EvaluationCriteriaModel c)
	{
		return (EvaluationResponseModel)HibernateUtil.currentSession().createQuery("FROM EvaluationResponseModel WHERE critid = :critid AND evalid = :evalid")
			.setInteger("critid", c.getId()).setInteger("evalid", getId()).uniqueResult();
	}
	
	public int getTotal()
	{
		return ((Long)HibernateUtil.currentSession().createQuery("SELECT SUM(response) FROM EvaluationResponseModel WHERE evalid = :evalid").setInteger("evalid", getId()).uniqueResult()).intValue();
	}
	
	public String getResponseStringForCriteria(EvaluationCriteriaModel c)
	{
		EvaluationResponseModel res = getResponseForCriteria(c);
		
		if(res == null)
		{
			return "&nbsp;";
		}
		else
		{
			return Integer.toString(res.getResponse());
		}
	}
}
