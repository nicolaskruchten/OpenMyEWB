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

import java.util.HashSet;
import java.util.Set;

import ca.myewb.model.ApplicationModel;
import ca.myewb.model.EvaluationResponseModel;

public abstract class Evaluation
{
	private int id;
	private String notes;
	private boolean rejectionSent;
	private ApplicationModel app;
	private Set<EvaluationResponseModel> evalResponses;
	
	protected Evaluation()
	{
		notes = "";
		rejectionSent = false;
		app = null;
		evalResponses = new HashSet<EvaluationResponseModel>();
	}

	protected ApplicationModel getApp()
	{
		return app;
	}

	protected void setApp(ApplicationModel app)
	{
		this.app = app;
	}

	public Set<EvaluationResponseModel> getEvalResponses()
	{
		return evalResponses;
	}

	protected void setEvalResponses(Set<EvaluationResponseModel> evalResponses)
	{
		this.evalResponses = evalResponses;
	}

	protected int getId()
	{
		return id;
	}

	protected void setId(int id)
	{
		this.id = id;
	}

	public String getNotes()
	{
		return notes;
	}

	public void setNotes(String notes)
	{
		this.notes = notes;
	}

	public boolean isRejectionSent()
	{
		return rejectionSent;
	}

	protected void setRejectionSent(boolean rejectionSent)
	{
		this.rejectionSent = rejectionSent;
	}
	
	

}
