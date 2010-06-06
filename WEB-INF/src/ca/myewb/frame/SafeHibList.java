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

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;


public class SafeHibList<T>
{
	Criteria crit;
	Query query;

	public SafeHibList(Criteria crit)
	{
		this.crit = crit;
	}

	public SafeHibList(Query query)
	{
		this.query = query;
	}

	@SuppressWarnings("unchecked")
	public List<T> list()
	{
		if (crit != null)
		{
			return crit.list();
		}
		else if (query != null)
		{
			return query.list();
		}
		else
		{
			Logger.getLogger(this.getClass())
			.warn("SafeHibList used with neither criteria nor query!");

			return null;
		}
	}
}
