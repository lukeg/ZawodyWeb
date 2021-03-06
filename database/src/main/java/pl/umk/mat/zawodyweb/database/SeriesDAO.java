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
import pl.umk.mat.zawodyweb.database.pojo.Series;
/**
 * <p>Generic DAO layer for Seriess</p>
 * <p>Generated at Fri May 08 19:00:59 CEST 2009</p>
 *
 * @author Salto-db Generator v1.1 / EJB3 + Hibernate DAO
 * @see http://www.hibernate.org/328.html
 */
public interface SeriesDAO extends GenericDAO<Series,Integer> {

	/*
	 * TODO : Add specific businesses daos here.
	 * These methods will be overwrited if you re-generate this interface.
	 * You might want to extend this interface and to change the dao factory to return 
	 * an instance of the new implemenation in buildSeriesDAO()
	 */
	  	 
	/**
	 * Find Series by name
	 */
	public List<Series> findByName(String name);

	/**
	 * Find Series by startdate
	 */
	public List<Series> findByStartdate(Timestamp startdate);

	/**
	 * Find Series by enddate
	 */
	public List<Series> findByEnddate(Timestamp enddate);

	/**
	 * Find Series by freezedate
	 */
	public List<Series> findByFreezedate(Timestamp freezedate);

	/**
	 * Find Series by unfreezedate
	 */
	public List<Series> findByUnfreezedate(Timestamp unfreezedate);

	/**
	 * Find Series by penaltytime
	 */
	public List<Series> findByPenaltytime(Integer penaltytime);

	/**
	 * Find Series by contestsid
	 */
	public List<Series> findByContestsid(Integer contestsid);

}
