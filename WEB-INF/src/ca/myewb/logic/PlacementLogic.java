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

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Order;

import ca.myewb.beans.Placement;
import ca.myewb.frame.HibernateUtil;
import ca.myewb.frame.SafeHibList;
import ca.myewb.model.PlacementModel;

public abstract class PlacementLogic extends Placement {

	public boolean isAssigned() {
		return ov!=null;
	}


	public static List<PlacementModel> getUnassignedPlacements() {
		Criteria crit = HibernateUtil.currentSession().createCriteria(PlacementModel.class);
		crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		crit.add(Restrictions.eq("active", new Boolean(false)));
		crit.add(Restrictions.eq("deleted", new Boolean(false)));
		crit.add(Restrictions.isNull("ov"));
		crit.addOrder(Order.asc("name"));
		return (new SafeHibList<PlacementModel>(crit)).list();
	}

	public static List<PlacementModel> getActivePlacements() {
		Criteria crit = HibernateUtil.currentSession().createCriteria(PlacementModel.class);
		crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		crit.add(Restrictions.eq("active", new Boolean(true)));
		crit.add(Restrictions.eq("deleted", new Boolean(false)));
		crit.add(Restrictions.isNotNull("ov"));
		crit.addOrder(Order.asc("name"));
		return (new SafeHibList<PlacementModel>(crit)).list();
	}

	public static Object getInactivePlacements() {
		Criteria crit = HibernateUtil.currentSession().createCriteria(PlacementModel.class);
		crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		crit.add(Restrictions.eq("active", new Boolean(false)));
		crit.add(Restrictions.eq("deleted", new Boolean(false)));
		crit.add(Restrictions.isNotNull("ov"));
		crit.addOrder(Order.asc("name"));
		return (new SafeHibList<PlacementModel>(crit)).list();
	}

	public static Object getDeletedPlacements() {
		Criteria crit = HibernateUtil.currentSession().createCriteria(PlacementModel.class);
		crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		crit.add(Restrictions.eq("active", new Boolean(false)));
		crit.add(Restrictions.eq("deleted", new Boolean(true)));
		crit.add(Restrictions.isNull("ov"));
		crit.addOrder(Order.asc("name"));
		return (new SafeHibList<PlacementModel>(crit)).list();
	}

}
