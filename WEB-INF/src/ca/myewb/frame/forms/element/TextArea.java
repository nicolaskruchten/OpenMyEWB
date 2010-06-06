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

package ca.myewb.frame.forms.element;

public class TextArea extends Element
{
	int cols;
	int rows;
	boolean twoCols;
	boolean twoColQuestion;

	public TextArea(String name, String label, String value, boolean required)
	{
		super(name, label, value, required);

		type = "textarea";

		cols = 50;
		rows = 5;
	}

	public void setSize(int c, int r)
	{
		cols = c;
		rows = r;
	}

	public int getCols()
	{
		return cols;
	}

	public int getRows()
	{
		return rows;
	}

	public void makeTwoCols()
	{
		makeTwoCols(false);
	}
	
	public void makeTwoCols(boolean twoColQuestion)
	{
		twoCols = true;
		this.twoColQuestion = twoColQuestion;
		cols = 75;
		rows = 17;
	}

	public boolean getTwoCols()
	{
		return twoCols;
	}

	public boolean getTwoColQuestion()
	{
		return twoColQuestion;
	}
}
