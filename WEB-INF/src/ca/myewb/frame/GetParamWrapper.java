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

package ca.myewb.frame;

public class GetParamWrapper extends RequestParamWrapper
{
	private String[] params;

	public GetParamWrapper(String[] path)
	{
		if (path.length > 2)
		{
			params = new String[path.length - 2];

			for (int i = 2; i < path.length; i++)
			{
				// Replace %20 with a space
				params[i - 2] = path[i].replaceAll("%20", " ");
			}
		}
		else
		{
			params = new String[]{null};
		}
	}
	
	public String[] getParams(){
		return params;
	}

	public void processParams(String[] names, String[] defaults)
	{
		for (int i = 0; i < names.length; i++)
		{
			if (((params.length < (i + 1)) || (params[i] == null)))
			{
				if (defaults[i] != null)
				{
					stringParams.put(names[i], defaults[i]);
					allParams.put(names[i], defaults[i]);
				}
			}
			else
			{
				stringParams.put(names[i], params[i]);
				allParams.put(names[i], params[i]);
			}
		}
	}

	public String getParam()
	{
		return params[0];
	}

	public String toString()
	{
		String result = "(";

		for (String param : params)
		{
			result += ("/" + param);
		}

		return result + ")";
	}
}
