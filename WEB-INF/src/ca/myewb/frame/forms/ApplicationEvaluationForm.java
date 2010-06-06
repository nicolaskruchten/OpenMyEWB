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

package ca.myewb.frame.forms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ca.myewb.frame.PostParamWrapper;
import ca.myewb.frame.forms.element.Text;
import ca.myewb.model.EvaluationCriteriaModel;


public class ApplicationEvaluationForm extends Form
{
	private List<Text> evalFields = new ArrayList<Text>();
	
	public ApplicationEvaluationForm(String target, PostParamWrapper requestParams, Collection<EvaluationCriteriaModel> criteria)
	{
		super(target, "save application evaluation");
		for(EvaluationCriteriaModel crit : criteria)
		{
			evalFields.add(addText("eval-" + crit.getId(), crit.getCriteria(), requestParams.get("eval-" + crit.getId()), false));
		}
		
		addTextArea("Notes", "Notes", requestParams.get("Notes"), false).makeTwoCols();
	}

	public boolean cleanAndValidate(boolean isClean)
	{
		for(Text field : evalFields)
		{
			isClean = field.ensureNumeric() && isClean;
		}
		
		return isClean;
	}
}
