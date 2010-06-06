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

package ca.myewb.beans;


import ca.myewb.model.EvaluationCriteriaModel;
import ca.myewb.model.EvaluationModel;

public abstract class EvaluationResponse
{
	
	private int id;
	private int response;
	private EvaluationCriteriaModel criteria;
	private EvaluationModel eval;
	
	protected EvaluationResponse()
	{
		response = 0;
		id = 0;
		criteria = null;
		eval = null;
	}
	
	public EvaluationCriteriaModel getCriteria()
	{
		return criteria;
	}
	protected void setCriteria(EvaluationCriteriaModel criteria)
	{
		this.criteria = criteria;
	}
	public int getId()
	{
		return id;
	}
	protected void setId(int id)
	{
		this.id = id;
	}
	public int getResponse()
	{
		return response;
	}
	public void setResponse(int response)
	{
		this.response = response;
	}

	protected EvaluationModel getEval()
	{
		return eval;
	}

	protected void setEval(EvaluationModel eval)
	{
		this.eval = eval;
	}
	
	

}
