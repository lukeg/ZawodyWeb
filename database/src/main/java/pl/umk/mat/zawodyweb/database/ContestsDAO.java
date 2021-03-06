/*
 * Copyright (c) 2009-2014, ZawodyWeb Team
 * All rights reserved.
 *
 * This file is distributable under the Simplified BSD license. See the terms
 * of the Simplified BSD license in the documentation provided with this file.
 */
package pl.umk.mat.zawodyweb.database;

import java.sql.Timestamp;
import java.util.List;
import pl.umk.mat.zawodyweb.database.pojo.Contests;
/**
 * <p>Generic DAO layer for Contestss</p>
 * <p>Generated at Fri May 08 19:00:59 CEST 2009</p>
 *
 * @author Salto-db Generator v1.1 / EJB3 + Hibernate DAO
 * @see http://www.hibernate.org/328.html
 */
public interface ContestsDAO extends GenericDAO<Contests,Integer> {

	/*
	 * TODO : Add specific businesses daos here.
	 * These methods will be overwrited if you re-generate this interface.
	 * You might want to extend this interface and to change the dao factory to return 
	 * an instance of the new implemenation in buildContestsDAO()
	 */
	  	 
	/**
	 * Find Contests by name
	 */
	public List<Contests> findByName(String name);

	/**
	 * Find Contests by type
	 */
	public List<Contests> findByType(Integer type);

	/**
	 * Find Contests by startdate
	 */
	public List<Contests> findByStartdate(Timestamp startdate);

	/**
	 * Find Contests by about
	 */
	public List<Contests> findByAbout(String about);

	/**
	 * Find Contests by rules
	 */
	public List<Contests> findByRules(String rules);

	/**
	 * Find Contests by tech
	 */
	public List<Contests> findByTech(String tech);

	/**
	 * Find Contests by visibility
	 */
	public List<Contests> findByVisibility(Integer visibility);

}
