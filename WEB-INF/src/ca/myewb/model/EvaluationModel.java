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

import ca.myewb.frame.HibernateUtil;
import ca.myewb.logic.EvaluationLogic;

public class EvaluationModel extends EvaluationLogic
{

	private EvaluationModel(ApplicationModel app)
	{
		super(app);
	}
	
	public EvaluationModel()
	{
		super();
	}

	public static EvaluationModel newEvaluationModel(ApplicationModel app)
	{
		EvaluationModel eval = new EvaluationModel(app);
		HibernateUtil.currentSession().save(eval);
		
		return eval;
	}

	public EvaluationResponseModel responseForCriteria(EvaluationCriteriaModel crit, int response)
	{
		EvaluationResponseModel evalRes = getResponseForCriteria(crit);
		
		if(evalRes == null)
		{
			evalRes = EvaluationResponseModel.newEvaluationResponse(crit, response, this);
			getEvalResponses().add(evalRes);
		}
		else
		{
			evalRes.setResponse(response);
		}
		
		return evalRes;
	}

	public void rejectApplication()
	{
		setRejectionSent(true);
	}

}
