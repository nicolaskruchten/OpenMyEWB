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

import ca.myewb.model.ApplicationSessionModel;

public abstract class ApplicationQuestion
{
	private int id;
	private String question;
	private ApplicationSessionModel session;
	private int questionOrder;
	
	public int getQuestionOrder()
	{
		return questionOrder;
	}

	protected void setQuestionOrder(int order)
	{
		questionOrder = order;
	}

	protected ApplicationQuestion() {
		
		id = 0;
		question = "";
		questionOrder = -1;
	}
	
	public int getId()
	{
		return id;
	}
	
	protected void setId(int id)
	{
		this.id = id;
	}
	
	public String getQuestion()
	{
		return question;
	}
	
	protected void setQuestion(String question)
	{
		this.question = question;
	}
	
	public ApplicationSessionModel getSession()
	{
		return session;
	}
	
	protected void setSession(ApplicationSessionModel session)
	{
		this.session = session;
	}
}
