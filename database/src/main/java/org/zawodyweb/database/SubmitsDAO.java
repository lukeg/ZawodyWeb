package org.zawodyweb.database;

import java.util.List;
import java.sql.Timestamp;

import org.zawodyweb.database.pojo.Submits;
/**
 * <p>Generic DAO layer for Submitss</p>
 * <p>Generated at Thu Mar 05 04:19:39 CET 2009</p>
 *
 * @author Salto-db Generator v1.1 / EJB3 + Hibernate DAO
 * @see http://www.hibernate.org/328.html
 */
public interface SubmitsDAO extends GenericDAO<Submits,Integer> {

	/*
	 * TODO : Add specific businesses daos here.
	 * These methods will be overwrited if you re-generate this interface.
	 * You might want to extend this interface and to change the dao factory to return 
	 * an instance of the new implemenation in buildSubmitsDAO()
	 */
	  	 
	/**
	 * Find Submits by sdate
	 */
	public List<Submits> findBySdate(Timestamp sdate);

	/**
	 * Find Submits by result
	 */
	public List<Submits> findByResult(Integer result);

	/**
	 * Find Submits by code
	 */
	public List<Submits> findByCode(byte[] code);

	/**
	 * Find Submits by filename
	 */
	public List<Submits> findByFilename(String filename);

	/**
	 * Find Submits by notes
	 */
	public List<Submits> findByNotes(String notes);

	/**
	 * Find Submits by tasksid
	 */
	public List<Submits> findByTasksid(Integer tasksid);

}