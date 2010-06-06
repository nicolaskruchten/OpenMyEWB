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

public class FileChooser extends Element
{
	protected boolean multifile;

	public FileChooser(String name, String label, String value, boolean required)
	{
		this(name, label, value, required, true);
	}


	public FileChooser(String name, String label, String value, boolean required, boolean multifile)
	{
		super(name, label, value, required);
		
		this.multifile = multifile;
		type = "filechooser";
	}
	
	public boolean isMultifile() {
		return multifile;
	}

	public void setMultifile(boolean multifile) {
		this.multifile = multifile;
	}
}