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

import java.util.ArrayList;
import java.util.Date;

import org.hibernate.Session;

import ca.myewb.frame.HibernateUtil;
import ca.myewb.logic.ApplicationLogic;

public class ApplicationModel extends ApplicationLogic
{

	private ApplicationModel(ApplicationSessionModel session, UserModel user)
	{
		super(session, user);
	}
	
	public ApplicationModel() {
		super();
	}

	public static ApplicationModel newApplication(ApplicationSessionModel session, UserModel user)
	{
		ApplicationModel app = new ApplicationModel(session, user);
		HibernateUtil.currentSession().save(app);
		
		return app;
	}
	
	public ApplicationAnswerModel answerQuestion(ApplicationQuestionModel question, String answer)
	{
		ApplicationAnswerModel a = (ApplicationAnswerModel)HibernateUtil.currentSession().createQuery("FROM ApplicationAnswerModel WHERE questionid = :question AND appid = :application").setInteger("question", question.getId()).setInteger("application", getId()).uniqueResult();
		if (a == null) {
			a = ApplicationAnswerModel.newApplicationAnswer(question, this, answer);
			getAnswers().add(a);
		}
		else
		{
			a.setAnswer(answer);
		}
		
		return a;
	}
	
	public void unlinkSession()
	{
		//this is basically shorthand for 'delete'
		getSession().getApplications().remove(this);
		setSession(null);
		getUser().getApplications().remove(this);
		setUser(null);
	}

	public void save(int englishWriting, int englishReading, int englishSpeaking, int frenchWriting, int frenchReading, int frenchSpeaking, String schooling, String resume, String references, float gpa) {
		setEnglishReading(englishReading);
		setEnglishWriting(englishWriting);
		setEnglishSpeaking(englishSpeaking);
		setFrenchReading(frenchReading);
		setFrenchSpeaking(frenchSpeaking);
		setFrenchWriting(frenchWriting);
		setSchooling(schooling);
		setRefs(references);
		setResume(resume);
		setGPA(gpa);
		
		setModified(new Date());
	}

	public ApplicationAnswerModel getAnswerForQuestion(ApplicationQuestionModel question) {
		Session session = HibernateUtil.currentSession();
		return (ApplicationAnswerModel)session.createQuery("FROM ApplicationAnswerModel WHERE questionid = :questionid AND appid = :appid").setInteger("appid", getId()).setInteger("questionid", question.getId()).uniqueResult();
	}
	
	public String[] getIncompleteQuestions()
	{
		ArrayList<String> workingSet = new ArrayList<String>();
		if(getSchooling() == null || getSchooling().equals("")) 
		{
			workingSet.add("Education Background");
		}
		if(getGPA() <= 0)
		{
			workingSet.add("Final Graduation Grade Percentage");
		}
		if(getResume() == null || getResume().equals(""))
		{
			workingSet.add("Resum&eacute;");
		}
		if(getRefs() == null || getRefs().equals(""))
		{
			workingSet.add("References");
		}
		
		for (ApplicationQuestionModel q : getSession().getQuestions())
		{
			if(getAnswerForQuestion(q) == null || getAnswerForQuestion(q).getAnswer().equals(""))
			{
				workingSet.add("Application Question " + q.getQuestionOrder());
			}
		}
		
		return workingSet.toArray(new String[0]);
	}

	public void evaluateForCriteria(EvaluationCriteriaModel crit, int response)
	{
		if(getEvaluation() == null)
		{
			EvaluationModel eval = EvaluationModel.newEvaluationModel(this);
			setEvaluation(eval);
		}
		
		getEvaluation().responseForCriteria(crit, response);
	}

}
