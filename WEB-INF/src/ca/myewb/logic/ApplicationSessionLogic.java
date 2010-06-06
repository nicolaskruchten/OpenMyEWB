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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import ca.myewb.beans.ApplicationSession;
import ca.myewb.frame.HibernateUtil;
import ca.myewb.model.ApplicationModel;
import ca.myewb.model.EvaluationCriteriaModel;

public abstract class ApplicationSessionLogic extends ApplicationSession
{
	protected ApplicationSessionLogic(String name, String engInstructions, String frInstructions, String completedAppMessage, String closeEmailText, Date openDate, Date dueDate, Date closeDate, String rejectionEmailText)
	{
		super();
		setName(name);
		setInstructions(engInstructions);
		setFrenchInstructions(frInstructions);
		setCompletedApplicationMessage(completedAppMessage);
		setCloseEmailText(closeEmailText);
		setOpenDate(openDate);
		setDueDate(dueDate);
		setCloseDate(closeDate);
		setRejectionEmailText(rejectionEmailText);
	}

	protected ApplicationSessionLogic()
	{
	}
	
	public Collection<ApplicationModel> getSortedApplications(final String filter, final boolean isAscending)
	{
		Comparator<ApplicationModel> comp = new Comparator<ApplicationModel>() {

			public int compare(ApplicationModel a1, ApplicationModel a2)
			{
				int inverter = (isAscending ? 1: -1);
				
				if(filter.equals("name"))
				{
					String name1 = a1.getUser().getLastname() + ", " + a1.getUser().getFirstname();
					String name2 = a2.getUser().getLastname() + ", " + a2.getUser().getFirstname();
					return name1.compareToIgnoreCase(name2) * inverter;
				}
				else if(filter.equals("language"))
				{
					return a1.getUser().getLanguage().compareToIgnoreCase(a2.getUser().getLanguage())  * inverter;
				}
				else if(filter.equals("gpa"))
				{
					if(a1.getGPA() > a2.getGPA())
					{
						return 1 * inverter;
					}
					else if (a2.getGPA() > a1.getGPA())
					{
						return -1 * inverter;
					}
					else
					{
						return 0;
					}
				}
				else if(filter.equals("gender"))
				{
					return a1.getUser().getGenderString().compareToIgnoreCase(a2.getUser().getGenderString())  * inverter;
				}
				else if(filter.equals("total"))
				{
					int total1;
					int total2;
					
					if(a1.getEvaluation() == null)
					{
						total1 = -1;
					}
					else
					{
						total1 = a1.getEvaluation().getTotal();
					}
					
					if(a2.getEvaluation() == null)
					{
						total2 = -1;
					}
					else
					{
						total2 = a2.getEvaluation().getTotal();
					}
					
					return (total1 - total2)  * inverter;
				}
				else if(filter.endsWith("rejection"))
				{
					int reject1;
					int reject2;
					
					if(a1.getEvaluation() == null)
					{
						reject1 = 3;
					} 
					else if(a1.getEvaluation().isRejectionSent())
					{
						reject1 = 2;
					}
					else
					{
						reject1 = 1;
					}
					
					if(a2.getEvaluation() == null)
					{
						reject2 = 3;
					} 
					else if(a2.getEvaluation().isRejectionSent())
					{
						reject2 = 2;
					}
					else
					{
						reject2 = 1;
					}
					
					return (reject2 - reject1)  * inverter;
				}
				else if(filter.startsWith("crit-"))
				{
					int critId = Integer.parseInt(filter.substring(5));
					EvaluationCriteriaModel crit = (EvaluationCriteriaModel)HibernateUtil.currentSession().load(EvaluationCriteriaModel.class, critId);
					
					int res1;
					int res2;

					try
					{
						res1 = a1.getEvaluation().getResponseForCriteria(crit).getResponse();
					}
					catch (NullPointerException npe)
					{
						res1 = 1;
					}
					
					try
					{
						res2 = a1.getEvaluation().getResponseForCriteria(crit).getResponse();
					}
					catch (NullPointerException npe)
					{
						res2 = 1;
					}
					
					return (res1 - res2)  * inverter;
				}
				else
				{
					return 0;
				}
			}
		};
		
		List<ApplicationModel> applicationsList = getApplications();
		Collections.sort(applicationsList, comp);
		return applicationsList;
	}
	
	public boolean isOpen()
	{
		Date now = new Date();
		return now.getTime() >= getOpenDate().getTime() && now.getTime() < getCloseDate().getTime();
	}

	public boolean isClosed()
	{
		Date now = new Date();
		return now.getTime() >= getCloseDate().getTime();
	}
	
	public boolean isDue()
	{
		Date now = new Date();
		return now.getTime() >= getDueDate().getTime();
	}
	
	public int getNumApplicants()
	{
		return getApplications().size();
	}
	
	public long getRemainingMS()
	{
		if(!isDue())
		{
			return getDueDate().getTime() - new Date().getTime();
		}
		else
		{
			return getCloseDate().getTime() - new Date().getTime();
		}
	}

}
