/*
 * Copyright (c) 2009-2014, ZawodyWeb Team
 * All rights reserved.
 *
 * This file is distributable under the Simplified BSD license. See the terms
 * of the Simplified BSD license in the documentation provided with this file.
 */
package pl.umk.mat.zawodyweb.www.ranking;

import java.sql.Timestamp;
import java.util.*;
import org.hibernate.Query;
import org.hibernate.Session;
import pl.umk.mat.zawodyweb.database.*;
import pl.umk.mat.zawodyweb.database.hibernate.HibernateUtil;
import pl.umk.mat.zawodyweb.database.pojo.Problems;
import pl.umk.mat.zawodyweb.database.pojo.Series;
import pl.umk.mat.zawodyweb.database.pojo.Users;

/**
 * @author <a href="mailto:faramir@mat.umk.pl">Marek Nowicki</a>
 * @version $Rev$ $Date: 2010-10-10 02:53:49 +0200 (N, 10 paź 2010)$
 */
public class SubrankingKI implements RankingInterface {

    private final ResourceBundle messages = ResourceBundle.getBundle("pl.umk.mat.zawodyweb.www.Messages");
    private final String schooltype;

    protected SubrankingKI(String schooltype) {
        this.schooltype = schooltype;
    }

    private class ProblemsKI implements Comparable {

        int id_problem;
        long series_begin;
        String abbrev;
        String name;
        boolean frozen;

        public ProblemsKI(int id_problem, long series_begin, String abbrev, String name, boolean frozen) {
            this.id_problem = id_problem;
            this.series_begin = series_begin;
            this.abbrev = abbrev;
            this.name = name;
            this.frozen = frozen;
        }

        @Override
        public int compareTo(Object o) {
            ProblemsKI p2 = (ProblemsKI) o;

            if (this.series_begin < p2.series_begin) {
                return -1;
            }
            if (this.series_begin > p2.series_begin) {
                return 1;
            }

            return this.abbrev.compareTo(p2.abbrev);
        }
    }

    private class UserKI implements Comparable {

        String lastname;
        String firstname;
        String login;
        boolean loginOnly;
        int id_user;
        int points;
        HashMap<Integer, Integer> solutions;

        public UserKI(int id_user, Users users) {
            this.id_user = id_user;
            this.solutions = new HashMap<Integer, Integer>();

            this.login = users.getLogin();
            this.firstname = users.getFirstname();
            this.lastname = users.getLastname();
            this.loginOnly = users.getOnlylogin();
        }

        String formatName() {
            return String.format("%s %s (%s)", firstname, lastname, login);
        }

        void add(int problem_id, int points) {
            this.points += points;
            this.solutions.put(problem_id, points);
        }

        @Override
        public int compareTo(Object o) {
            final UserKI u2 = (UserKI) o;

            if (this.points < u2.points) {
                return 1;
            }
            if (this.points > u2.points) {
                return -1;
            }

            int r;
            r = RankingUtils.compareStrings(this.lastname, u2.lastname);
            if (r != 0) {
                return r;
            }

            r = RankingUtils.compareStrings(firstname, u2.firstname);
            if (r != 0) {
                return r;
            }

            return RankingUtils.compareStrings(this.login, u2.login);
        }
    }

    private RankingTable getRankingKI(int contest_id, Timestamp checkDate, boolean admin, Integer series_id) {
        Session hibernateSession = HibernateUtil.getSessionFactory().getCurrentSession();

        Timestamp checkTimestamp;

        UsersDAO usersDAO = DAOFactory.DEFAULT.buildUsersDAO();
        SeriesDAO seriesDAO = DAOFactory.DEFAULT.buildSeriesDAO();
        ProblemsDAO problemsDAO = DAOFactory.DEFAULT.buildProblemsDAO();
        HashMap<Integer, UserKI> mapUserKI = new HashMap<Integer, UserKI>();
        ArrayList<ProblemsKI> vectorProblemsKI = new ArrayList<ProblemsKI>();

        boolean allTests;
        boolean frozenRanking = false;
        boolean frozenSeria;

        long lCheckDate = checkDate.getTime();

        for (Series series : seriesDAO.findByContestsid(contest_id)) {

            if ((series_id == null && series.getVisibleinranking() == false)
                    || (series_id != null && series_id.equals(series.getId()) == false)) {
                continue;
            }

            if (series.getStartdate().getTime() > lCheckDate) {
                continue;
            }

            frozenSeria = false;
            checkTimestamp = checkDate;
            allTests = admin;

            if (!admin && series.getFreezedate() != null) {
                if (lCheckDate > series.getFreezedate().getTime() && (series.getUnfreezedate() == null || lCheckDate < series.getUnfreezedate().getTime())) {
                    checkTimestamp = new Timestamp(series.getFreezedate().getTime());
                    if (series.getUnfreezedate() != null) {
                        frozenRanking = true;
                    }
                    frozenSeria = true;
                }
            }

            if (series.getUnfreezedate() != null) {
                if (checkDate.after(series.getUnfreezedate())) {
                    allTests = true;
                }
            }

            for (Problems problems : problemsDAO.findBySeriesid(series.getId())) {
                if (problems.getVisibleinranking() == false) {
                    continue;
                }

                vectorProblemsKI.add(new ProblemsKI(problems.getId(), series.getStartdate().getTime(), problems.getAbbrev(), problems.getName(), frozenSeria));

                Query query = null;
                if (allTests == true) {
                    query = hibernateSession.createSQLQuery(""
                            + "select usersid, sum(points) "
                            + "from users,submits,results,tests "
                            + "where submits.problemsid='" + problems.getId() + "' "
                            + "  and submits.id=results.submitsid "
                            + "  and tests.id=results.testsid"
                            + "  and sdate in ( "
                            + "        select max(sdate) "
                            + "	     from submits "
                            + "        where submits.problemsid='" + problems.getId() + "' "
                            + "          and submits.result='" + SubmitsStateEnum.DONE.getCode() + "' "
                            + "          and sdate <= '" + checkTimestamp.toString() + "' "
                            + "          and visibleInRanking=true"
                            //+ "	       and tests.visibility=1 " +
                            + "	     group by usersid "
                            + "      ) "
                            + "  and usersid=users.id "
                            + "  and users.schooltype='" + schooltype + "' "
                            + "group by usersid, submits.id");
                } else {
                    query = hibernateSession.createSQLQuery(""
                            + "select usersid, sum(points) "
                            + "from users,submits,results,tests "
                            + "where submits.problemsid='" + problems.getId() + "' "
                            + "  and submits.id=results.submitsid "
                            + "  and tests.id=results.testsid"
                            + "  and tests.visibility=1 " // FIXME: should be ok
                            + "  and sdate in ( "
                            + "      select max(sdate) "
                            + "	     from submits "
                            + "        where submits.problemsid='" + problems.getId() + "' "
                            + "          and submits.result='" + SubmitsStateEnum.DONE.getCode() + "' "
                            + "          and sdate <= '" + checkTimestamp.toString() + "' "
                            + "          and visibleInRanking=true"
                            + "	     group by usersid "
                            + "      ) "
                            + "  and usersid=users.id "
                            + "  and users.schooltype='" + schooltype + "' "
                            + "group by usersid, submits.id");
                }

                for (Object list : query.list()) {
                    Object[] o = (Object[]) list; // 0 - user.id, 1 - sum(points)

                    UserKI user = mapUserKI.get((Integer) o[0]);
                    if (user == null) {
                        Integer user_id = (Integer) o[0];
                        Users users = usersDAO.getById(user_id);
                        user = new UserKI(user_id, users);
                        mapUserKI.put((Integer) o[0], user);
                    }

                    user.add(problems.getId(), ((Number) o[1]).intValue());
                }
            }
        }

        /*
         * Tworzenie rankingu z danych
         */
        ArrayList<UserKI> cre = new ArrayList<UserKI>();
        cre.addAll(mapUserKI.values());
        Collections.sort(cre);

        Collections.sort(vectorProblemsKI);

        /*
         * nazwy kolumn i CSSy
         */
        ArrayList<String> columnsCSS = new ArrayList<String>();
        ArrayList<String> columnsCaptions = new ArrayList<String>();
        for (ProblemsKI problemsKI : vectorProblemsKI) {
            columnsCaptions.add(RankingUtils.formatText(problemsKI.abbrev, problemsKI.name, problemsKI.frozen ? "frozen" : null));
            columnsCSS.add("small");
        }
        columnsCaptions.add(messages.getString("points"));
        columnsCSS.add("small");

        /*
         * tabelka z rankingiem
         */
        ArrayList<RankingEntry> vectorRankingEntry = new ArrayList<RankingEntry>();
        int place = 0;
        int points = Integer.MAX_VALUE;
        for (UserKI user : cre) {
            if (points > user.points) {
                ++place;
                points = user.points;
            }
            ArrayList<String> v = new ArrayList<String>();
            for (ProblemsKI problemsKI : vectorProblemsKI) {
                Integer solution_points = user.solutions.get(problemsKI.id_problem);
                if (solution_points == null) {
                    v.add("-");
                } else {
                    v.add(solution_points.toString());
                }
            }
            v.add(Integer.toString(user.points));

            vectorRankingEntry.add(new RankingEntry(place, user.firstname, user.lastname, user.login, user.loginOnly, v));
        }

        return new RankingTable(columnsCaptions, columnsCSS, vectorRankingEntry, frozenRanking);
    }

    @Override
    public RankingTable getRanking(int contest_id, Timestamp checkDate, boolean admin) {
        return getRankingKI(contest_id, checkDate, admin, null);
    }

    @Override
    public RankingTable getRankingForSeries(int contest_id, int series_id, Timestamp checkDate, boolean admin) {
        return getRankingKI(contest_id, checkDate, admin, series_id);
    }

    @Override
    public List<Integer> getRankingSolutions(int contest_id, Integer series_id, Timestamp checkDate, boolean admin) {
        Session hibernateSession = HibernateUtil.getSessionFactory().getCurrentSession();

        Timestamp checkTimestamp;

        SeriesDAO seriesDAO = DAOFactory.DEFAULT.buildSeriesDAO();
        ProblemsDAO problemsDAO = DAOFactory.DEFAULT.buildProblemsDAO();

        List<Integer> submits = new ArrayList<Integer>();

        boolean allTests;

        long lCheckDate = checkDate.getTime();

        for (Series series : seriesDAO.findByContestsid(contest_id)) {

            if ((series_id == null && series.getVisibleinranking() == false)
                    || (series_id != null && series_id.equals(series.getId()) == false)) {
                continue;
            }

            if (series.getStartdate().getTime() > lCheckDate) {
                continue;
            }

            checkTimestamp = checkDate;
            allTests = admin;

            if (!admin && series.getFreezedate() != null) {
                if (lCheckDate > series.getFreezedate().getTime() && (series.getUnfreezedate() == null || lCheckDate < series.getUnfreezedate().getTime())) {
                    checkTimestamp = new Timestamp(series.getFreezedate().getTime());
                }
            }

            if (series.getUnfreezedate() != null) {
                if (checkDate.after(series.getUnfreezedate())) {
                    allTests = true;
                }
            }

            for (Problems problems : problemsDAO.findBySeriesid(series.getId())) {
                if (problems.getVisibleinranking() == false) {
                    continue;
                }

                Query query = null;
                if (allTests == true) {
                    query = hibernateSession.createSQLQuery(""
                            + "select submits.id sid "
                            + "from users,submits,results,tests "
                            + "where submits.problemsid='" + problems.getId() + "' "
                            + "  and submits.id=results.submitsid "
                            + "  and tests.id=results.testsid"
                            + "  and sdate in ( "
                            + "        select max(sdate) "
                            + "	     from submits "
                            + "        where submits.problemsid='" + problems.getId() + "' "
                            + "          and submits.result='" + SubmitsStateEnum.DONE.getCode() + "' "
                            + "          and sdate <= '" + checkTimestamp.toString() + "' "
                            + "          and visibleInRanking=true"
                            //+ "	       and tests.visibility=1 " +
                            + "	     group by usersid "
                            + "      ) "
                            + "  and usersid=users.id "
                            + "  and users.schooltype='" + schooltype + "' "
                            + "group by usersid, submits.id");
                } else {
                    query = hibernateSession.createSQLQuery(""
                            + "select submits.id sid "
                            + "from users,submits,results,tests "
                            + "where submits.problemsid='" + problems.getId() + "' "
                            + "  and submits.id=results.submitsid "
                            + "  and tests.id=results.testsid"
                            + "  and tests.visibility=1 " // FIXME: should be ok
                            + "  and sdate in ( "
                            + "      select max(sdate) "
                            + "	     from submits "
                            + "        where submits.problemsid='" + problems.getId() + "' "
                            + "          and submits.result='" + SubmitsStateEnum.DONE.getCode() + "' "
                            + "          and sdate <= '" + checkTimestamp.toString() + "' "
                            + "          and visibleInRanking=true"
                            + "	     group by usersid "
                            + "      ) "
                            + "  and usersid=users.id "
                            + "  and users.schooltype='" + schooltype + "' "
                            + "group by usersid, submits.id");
                }

                for (Object id : query.list()) {
                    submits.add((Integer) id);
                }
            }
        }

        return submits;
    }
}
