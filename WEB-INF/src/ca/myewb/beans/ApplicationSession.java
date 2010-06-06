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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import ca.myewb.model.ApplicationModel;
import ca.myewb.model.ApplicationQuestionModel;
import ca.myewb.model.EvaluationCriteriaModel;

public abstract class ApplicationSession
{
	private int id;
	private String name;
	private Date openDate;		//Date the session becomes visible to system users to apply
	private Date closeDate;		//Date session closes and applications cut-off
	private Date dueDate; 		//Date show to users of when applciations are due, applications are still accpeted if this date is before the close date
	private String instructions;
	private String frenchInstructions;
	private String closeEmailText;
	private String rejectionEmailText;
	private String completedApplicationMessage;
	private Collection<ApplicationQuestionModel> questions;
	private List<ApplicationModel> applications;
	private boolean emailSent;
	private Collection<EvaluationCriteriaModel> evalCriteria;
	
	protected ApplicationSession() {
		
		id = 0;
		name = "";
		instructions = "";
		frenchInstructions = "";
		closeEmailText = "";
		rejectionEmailText = "";
		completedApplicationMessage = "";
		
		questions = new HashSet<ApplicationQuestionModel>();
		applications = new ArrayList<ApplicationModel>();
		evalCriteria = new ArrayList<EvaluationCriteriaModel>();
		
		emailSent = false;
	}
	
	public List<ApplicationModel> getApplications()
	{
		return applications;
	}
	
	protected void setApplications(List<ApplicationModel> applications)
	{
		this.applications = applications;
	}
	
	public Date getCloseDate()
	{
		return closeDate;
	}
	
	protected void setCloseDate(Date closeDate)
	{
		this.closeDate = closeDate;
	}
	
	public String getCloseEmailText()
	{
		return closeEmailText;
	}
	
	protected void setCloseEmailText(String closeEmailText)
	{
		this.closeEmailText = closeEmailText;
	}
	
	public String getCompletedApplicationMessage()
	{
		return completedApplicationMessage;
	}
	
	protected void setCompletedApplicationMessage(String completedApplicationMessage)
	{
		this.completedApplicationMessage = completedApplicationMessage;
	}
	
	public Date getDueDate()
	{
		return dueDate;
	}
	
	protected void setDueDate(Date dueDate)
	{
		this.dueDate = dueDate;
	}
	
	public int getId()
	{
		return id;
	}
	
	protected void setId(int id)
	{
		this.id = id;
	}
	
	public String getInstructions()
	{
		return instructions;
	}
	
	protected void setInstructions(String instructions)
	{
		this.instructions = instructions;
	}
	
	public String getName()
	{
		return name;
	}
	
	protected void setName(String name)
	{
		this.name = name;
	}
	
	public Collection<ApplicationQuestionModel> getQuestions()
	{
		return questions;
	}
	
	protected void setQuestions(Collection<ApplicationQuestionModel> questions)
	{
		this.questions = questions;
	}

	public Date getOpenDate()
	{
		return openDate;
	}

	protected void setOpenDate(Date openDate)
	{
		this.openDate = openDate;
	}

	public boolean isEmailSent()
	{
		return emailSent;
	}

	protected void setEmailSent(boolean emailSent)
	{
		this.emailSent = emailSent;
	}

	public String getFrenchInstructions() {
		return frenchInstructions;
	}

	public void setFrenchInstructions(String frenchInstructions) {
		this.frenchInstructions = frenchInstructions;
	}

	public Collection<EvaluationCriteriaModel> getEvalCriteria()
	{
		return evalCriteria;
	}

	protected void setEvalCriteria(Collection<EvaluationCriteriaModel> evalCriteria)
	{
		this.evalCriteria = evalCriteria;
	}

	public String getRejectionEmailText()
	{
		return rejectionEmailText;
	}

	public void setRejectionEmailText(String rejectionEmailText)
	{
		this.rejectionEmailText = rejectionEmailText;
	}
	
}
