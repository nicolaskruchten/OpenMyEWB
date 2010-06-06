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

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import ca.myewb.model.ApplicationAnswerModel;
import ca.myewb.model.EvaluationModel;
import ca.myewb.model.UserModel;

public abstract class Application
{	
	private int id;
	private UserModel user;
	private ApplicationSession session;
	private Collection<ApplicationAnswerModel> answers;
	private int englishWriting;
	private int englishReading;
	private int englishSpeaking;
	private int frenchWriting;
	private int frenchReading;
	private int frenchSpeaking;
	private String schooling;
	private String refs;
	private String resume;
	private float GPA;
	private Date created;
	private Date modified;
	private EvaluationModel evaluation;

	protected Application() {
		
		id = 0;
		answers = new HashSet<ApplicationAnswerModel>();
		englishWriting = 0;
		englishReading = 0;
		englishSpeaking = 0;
		frenchWriting = 0;
		frenchReading = 0;
		frenchSpeaking = 0;
		schooling = "";
		refs = "";
		resume = "";
		GPA = 0;
		
		created = new Date();
		modified = new Date();
		evaluation = null;
		
	}
	
	public Collection<ApplicationAnswerModel> getAnswers()
	{
		return answers;
	}
	
	protected void setAnswers(Collection<ApplicationAnswerModel> answers)
	{
		this.answers = answers;
	}
	
	public ApplicationSession getSession()
	{
		return session;
	}
	
	protected void setSession(ApplicationSession appSession)
	{
		this.session = appSession;
	}
	
	public int getId()
	{
		return id;
	}
	
	protected void setId(int id)
	{
		this.id = id;
	}
	
	public UserModel getUser()
	{
		return user;
	}
	
	protected void setUser(UserModel user)
	{
		this.user = user;
	}

	public int getEnglishReading()
	{
		return englishReading;
	}

	protected void setEnglishReading(int englishReading)
	{
		this.englishReading = englishReading;
	}

	public int getEnglishSpeaking()
	{
		return englishSpeaking;
	}

	protected void setEnglishSpeaking(int englishSpeaking)
	{
		this.englishSpeaking = englishSpeaking;
	}

	public int getEnglishWriting()
	{
		return englishWriting;
	}

	protected void setEnglishWriting(int englishWriting)
	{
		this.englishWriting = englishWriting;
	}

	public int getFrenchReading()
	{
		return frenchReading;
	}

	protected void setFrenchReading(int frenchReading)
	{
		this.frenchReading = frenchReading;
	}

	public int getFrenchSpeaking()
	{
		return frenchSpeaking;
	}

	protected void setFrenchSpeaking(int frenchSpeaking)
	{
		this.frenchSpeaking = frenchSpeaking;
	}

	public int getFrenchWriting()
	{
		return frenchWriting;
	}

	protected void setFrenchWriting(int frenchWriting)
	{
		this.frenchWriting = frenchWriting;
	}

	public String getRefs()
	{
		return refs;
	}

	protected void setRefs(String references)
	{
		this.refs = references;
	}

	public String getResume()
	{
		return resume;
	}

	protected void setResume(String resume)
	{
		this.resume = resume;
	}

	public String getSchooling()
	{
		return schooling;
	}

	protected void setSchooling(String schooling)
	{
		this.schooling = schooling;
	}
	
	public float getGPA() {
		return GPA;
	}

	public void setGPA(float gpa) {
		GPA = gpa;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public EvaluationModel getEvaluation()
	{
		return evaluation;
	}

	protected void setEvaluation(EvaluationModel eval)
	{
		this.evaluation = eval;
	}
}
