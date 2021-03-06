/*
 * Copyright (c) 2009-2014, ZawodyWeb Team
 * All rights reserved.
 *
 * This file is distributable under the Simplified BSD license. See the terms
 * of the Simplified BSD license in the documentation provided with this file.
 */
package pl.umk.mat.zawodyweb.database.hibernate;

import java.sql.Timestamp;
import java.util.List;
import org.hibernate.criterion.Restrictions;
import pl.umk.mat.zawodyweb.database.SubmitsDAO;
import pl.umk.mat.zawodyweb.database.pojo.Submits;

/**
 * <p>Hibernate DAO layer for Submitss</p>
 * <p>Generated at Fri May 08 19:00:59 CEST 2009</p>
 *
 * @author Salto-db Generator v1.1 / EJB3 + Hibernate DAO
 * @see http://www.hibernate.org/328.html
 */
public class SubmitsHibernateDAO extends
		AbstractHibernateDAO<Submits, Integer> implements
		SubmitsDAO {

	/**
	 * Find Submits by sdate
	 */
	public List<Submits> findBySdate(Timestamp sdate) {
		return findByCriteria(Restrictions.eq("sdate", sdate));
	}
	
	/**
	 * Find Submits by result
	 */
	public List<Submits> findByState(Integer state) {
		return findByCriteria(Restrictions.eq("state", state));
	}

	/**
	 * Find Submits by languagesid
	 */
	@SuppressWarnings("unchecked")
	public List<Submits> findByUsersid(Integer usersid) {
		return findByCriteria(Restrictions.eq("users.id", usersid));
	}


	/**
	 * Find Submits by code
	 */
	public List<Submits> findByCode(byte[] code) {
		return findByCriteria(Restrictions.eq("code", code));
	}
	
	/**
	 * Find Submits by filename
	 */
	public List<Submits> findByFilename(String filename) {
		return findByCriteria(Restrictions.eq("filename", filename));
	}
	
	/**
	 * Find Submits by notes
	 */
	public List<Submits> findByNotes(String notes) {
		return findByCriteria(Restrictions.eq("notes", notes));
	}
	
	/**
	 * Find Submits by problemsid
	 */
	@SuppressWarnings("unchecked")
	public List<Submits> findByProblemsid(Integer problemsid) {
		return findByCriteria(Restrictions.eq("problems.id", problemsid));
	}
	
	/**
	 * Find Submits by languagesid
	 */
	@SuppressWarnings("unchecked")
	public List<Submits> findByLanguagesid(Integer languagesid) {
		return findByCriteria(Restrictions.eq("languages.id", languagesid));
	}
	

}
