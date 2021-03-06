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
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import pl.umk.mat.zawodyweb.database.*;
import pl.umk.mat.zawodyweb.database.hibernate.HibernateUtil;
import pl.umk.mat.zawodyweb.database.pojo.*;

/**
 * @author <a href="mailto:faramir@mat.umk.pl">Marek Nowicki</a>
 * @version $Rev$ $Date: 2010-10-10 02:53:49 +0200 (N, 10 paź 2010)$
 */
public class RankingACM implements RankingInterface {

    private final ResourceBundle messages = ResourceBundle.getBundle("pl.umk.mat.zawodyweb.www.Messages");

    private class SolutionACM implements Comparable {

        String abbrev;
        long date;
        long time;
        long time_from_bombs;
        int bombs;
        String name;
        boolean frozen;

        public SolutionACM(String abbrev, long date, long time, long time_from_bombs, int bombs, String name, boolean frozen) {
            this.abbrev = abbrev;
            this.date = date;
            this.time = time;
            this.time_from_bombs = time_from_bombs;
            this.bombs = bombs;
            this.name = name;
            this.frozen = frozen;
        }

        @Override
        public int compareTo(Object o) {
            if (this.date < ((SolutionACM) o).date) {
                return -1;
            }
            if (this.date > ((SolutionACM) o).date) {
                return 1;
            }
            return 0;
        }
    }

    private class UserACM implements Comparable {

        String login;
        String firstname;
        String lastname;
        boolean loginOnly;
        int id_user;
        int points;
        long totalTime;
        List<SolutionACM> solutions;

        public UserACM(int id_user, Users users) {
            this.id_user = id_user;
            this.points = 0;
            this.totalTime = 0;
            this.solutions = new ArrayList<>();

            this.login = users.getLogin();
            this.firstname = users.getFirstname();
            this.lastname = users.getLastname();
            this.loginOnly = users.getOnlylogin();
        }

        void add(int points, SolutionACM solutionACM) {
            this.points += points;
            this.totalTime += solutionACM.time + solutionACM.time_from_bombs;
            this.solutions.add(solutionACM);
        }

        String getSolutionsForRanking() {
            String r = "";
            String text;
            Collections.sort(this.solutions);

            for (SolutionACM solutionACM : solutions) {
                text = solutionACM.abbrev;
                if (solutionACM.bombs >= 4) {
                    text += "(" + solutionACM.bombs + ")";
                } else {
                    for (int i = 0; i < solutionACM.bombs; ++i) {
                        text += "*";
                    }
                }
                r += RankingUtils.formatText(text,
                        solutionACM.name + " [" + parseTime(solutionACM.time) + (solutionACM.time_from_bombs == 0 ? "" : "+" + parseTime(solutionACM.time_from_bombs)) + "]",
                        solutionACM.frozen ? "frozen" : null) + " ";
            }
            return r.trim();
        }

        @Override
        public int compareTo(Object o) {
            UserACM u2 = (UserACM) o;
            if (this.points < u2.points) {
                return 1;
            }
            if (this.points > u2.points) {
                return -1;
            }
            if (this.totalTime > u2.totalTime) {
                return 1;
            }
            if (this.totalTime < u2.totalTime) {
                return -1;
            }

            int r;
            r = RankingUtils.compareStrings(this.lastname, u2.lastname);
            if (r != 0) {
                return r;
            }

            r = RankingUtils.compareStrings(this.firstname, u2.firstname);
            if (r != 0) {
                return r;
            }

            return RankingUtils.compareStrings(this.login, u2.login);
        }
    }

    private String parseTime(long time) {
        long d, h, m, s;
        d = time / (24 * 60 * 60);
        time %= (24 * 60 * 60);
        h = time / (60 * 60);
        time %= (60 * 60);
        m = time / 60;
        time %= 60;
        s = time;

        if (d > 0) {
            return String.format("%dd %02d:%02d:%02d", d, h, m, s);
        } else if (h > 0) {
            return String.format("%d:%02d:%02d", h, m, s);
        } else {
            return String.format("%d:%02d", m, s);
        }
    }

    private RankingTable getRankingACM(int contest_id, Timestamp checkDate, boolean admin, Integer series_id) {
        Session hibernateSession = HibernateUtil.getSessionFactory().getCurrentSession();

        Timestamp checkTimestamp;
        Timestamp visibleTimestamp;

        UsersDAO usersDAO = DAOFactory.DEFAULT.buildUsersDAO();
        SeriesDAO seriesDAO = DAOFactory.DEFAULT.buildSeriesDAO();
        ProblemsDAO problemsDAO = DAOFactory.DEFAULT.buildProblemsDAO();
        Map<Integer, UserACM> mapUserACM = new HashMap<>();

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

            if (checkTimestamp.before(series.getStartdate())) {
                visibleTimestamp = new Timestamp(0);
            } else {
                visibleTimestamp = new Timestamp(series.getStartdate().getTime());
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

                // select sum(maxpoints) from tests where problemsid='7' and visibility=1
                Number maxPoints;
                Number testCount;
                if (allTests) {
                    Object[] o = (Object[]) hibernateSession.createCriteria(Tests.class).setProjection(
                            Projections.projectionList().add(Projections.sum("maxpoints")).add(Projections.rowCount())).add(Restrictions.eq("problems.id", problems.getId())).uniqueResult();
                    maxPoints = (Number) o[0];
                    testCount = (Number) o[1];
                } else {
                    Object[] o = (Object[]) hibernateSession.createCriteria(Tests.class).setProjection(
                            Projections.projectionList().add(Projections.sum("maxpoints")).add(Projections.rowCount())).add(Restrictions.and(Restrictions.eq("problems.id", problems.getId()), Restrictions.eq("visibility", 1))).uniqueResult();
                    maxPoints = (Number) o[0];
                    testCount = (Number) o[1];
                }
                if (maxPoints == null) {
                    maxPoints = 0; // To nie powinno się nigdy zdarzyć ;).. chyba, że nie ma testu przy zadaniu?
                }
                if (testCount == null) {
                    testCount = 0; // To nie powinno się zdarzyć nigdy.
                }

                Query query;
                if (allTests == true) {
                    query = hibernateSession.createSQLQuery(""
                            + "select usersid, min(sdate) sdate " // zapytanie zewnętrzne znajduję minimalną datę wysłania poprawnego rozwiązania dla każdego usera
                            + "from submits "
                            + "where id in ("
                            + "    select submits.id " // zapytanie wewnętrzne znajduje wszystkie id, które zdobyły maksa punktów
                            + "    from submits,results,tests "
                            + "    where submits.problemsid = :problemsId "
                            + "      and submits.id=results.submitsid "
                            + "	     and tests.id = results.testsid "
                            + "      and results.status = :statusAcc "
                            + "      and submits.state = :stateDone "
                            + "      and sdate <= :currentTimestamp "
                            + "      and sdate >= :visibleTimestamp "
                            + "      and visibleInRanking=true"
                            //+ "      and tests.visibility=1 "
                            + "    group by submits.id,usersid,sdate "
                            + "    having sum(points) = :maxPoints "
                            + "      and count(points) = :testCount "
                            + "  ) "
                            + "group by usersid")
                            .setInteger("problemsId", problems.getId())
                            .setInteger("statusAcc", ResultsStatusEnum.ACC.getCode())
                            .setInteger("stateDone", SubmitsStateEnum.DONE.getCode())
                            .setInteger("maxPoints", maxPoints.intValue())
                            .setInteger("testCount", testCount.intValue())
                            .setTimestamp("currentTimestamp", checkTimestamp)
                            .setTimestamp("visibleTimestamp", visibleTimestamp);
                } else {
                    query = hibernateSession.createSQLQuery(""
                            + "select usersid, min(sdate) sdate "
                            + "from submits "
                            + "where id in ("
                            + "    select submits.id "
                            + "    from submits,results,tests "
                            + "    where submits.problemsid = :problemsId "
                            + "      and submits.id=results.submitsid "
                            + "	     and tests.id = results.testsid "
                            + "      and results.status = :statusAcc "
                            + "      and submits.state = :stateDone "
                            + "      and sdate <= :currentTimestamp "
                            + "      and sdate >= :visibleTimestamp "
                            + "      and visibleInRanking=true"
                            + "	     and tests.visibility=1 " // FIXME: should be ok
                            + "    group by submits.id,usersid,sdate "
                            + "    having sum(points) = :maxPoints "
                            + "      and count(points) = :testCount "
                            + "  ) "
                            + "group by usersid")
                            .setInteger("problemsId", problems.getId())
                            .setInteger("statusAcc", ResultsStatusEnum.ACC.getCode())
                            .setInteger("stateDone", SubmitsStateEnum.DONE.getCode())
                            .setInteger("maxPoints", maxPoints.intValue())
                            .setInteger("testCount", testCount.intValue())
                            .setTimestamp("currentTimestamp", checkTimestamp)
                            .setTimestamp("visibleTimestamp", visibleTimestamp);
                }

                for (Object list : query.list()) { // tu jest zwrócona lista "zaakceptowanych" w danym momencie rozwiązań zadania
                    Object[] o = (Object[]) list;

                    Number bombs = (Number) hibernateSession.createCriteria(Submits.class).setProjection(Projections.rowCount()).add(Restrictions.eq("problems.id", (Number) problems.getId())).add(Restrictions.eq("users.id", (Number) o[0])).add(Restrictions.lt("sdate", (Timestamp) o[1])).uniqueResult();

                    if (bombs == null) {
                        bombs = 0;
                    }

                    UserACM user = mapUserACM.get((Integer) o[0]);
                    if (user == null) {
                        Integer user_id = (Integer) o[0];
                        Users users = usersDAO.getById(user_id);

                        user = new UserACM(user_id, users);
                        mapUserACM.put((Integer) o[0], user);
                    }

                    user.add(maxPoints.intValue(),
                            new SolutionACM(problems.getAbbrev(),
                                    ((Timestamp) o[1]).getTime(),
                                    (maxPoints.equals(0) ? 0 : ((Timestamp) o[1]).getTime() - series.getStartdate().getTime()) / 1000,
                                    series.getPenaltytime() * bombs.intValue(),
                                    bombs.intValue(),
                                    problems.getName(),
                                    frozenSeria));
                }
            }

        }

        /*
         * Tworzenie rankingu z danych
         */
        List<UserACM> cre = new ArrayList<>();
        cre.addAll(mapUserACM.values());
        Collections.sort(cre);

        /*
         * nazwy kolumn
         */
        List<String> columnsCaptions = new ArrayList<>();
        columnsCaptions.add(messages.getString("points"));
        columnsCaptions.add(messages.getString("time"));
        columnsCaptions.add(messages.getString("solutions"));

        /*
         * nazwy klas css-owych dla kolumn
         */
        List<String> columnsCSS = new ArrayList<>();
        columnsCSS.add("small");    // points
        columnsCSS.add("nowrap small");    // time
        columnsCSS.add("big left");      // solutions

        /*
         * tabelka z rankingiem
         */
        List<RankingEntry> vectorRankingEntry = new ArrayList<>();
        int place = 0;
        long totalTime = -1;
        int points = Integer.MAX_VALUE;
        for (UserACM user : cre) {
            if (points > user.points || (points == user.points && totalTime < user.totalTime)) {
                ++place;
                points = user.points;
                totalTime = user.totalTime;
            }
            List<String> v = new ArrayList<>();
            v.add(Integer.toString(user.points));
            v.add(parseTime(user.totalTime));
            v.add(user.getSolutionsForRanking());

            vectorRankingEntry.add(new RankingEntry(place, user.firstname, user.lastname, user.login, user.loginOnly, v));
        }

        return new RankingTable(columnsCaptions, columnsCSS, vectorRankingEntry, frozenRanking);
    }

    @Override
    public RankingTable getRanking(int contest_id, Timestamp checkDate, boolean admin) {
        return getRankingACM(contest_id, checkDate, admin, null);
    }

    @Override
    public RankingTable getRankingForSeries(int contest_id, int series_id, Timestamp checkDate, boolean admin) {
        return getRankingACM(contest_id, checkDate, admin, series_id);
    }

    @Override
    public List<Integer> getRankingSolutions(int contest_id, Integer series_id, Timestamp checkDate, boolean admin) {
        Session hibernateSession = HibernateUtil.getSessionFactory().getCurrentSession();

        Timestamp checkTimestamp;
        Timestamp visibleTimestamp;

        SeriesDAO seriesDAO = DAOFactory.DEFAULT.buildSeriesDAO();
        ProblemsDAO problemsDAO = DAOFactory.DEFAULT.buildProblemsDAO();

        List<Integer> submits = new ArrayList<>();

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

            if (checkTimestamp.before(series.getStartdate())) {
                visibleTimestamp = new Timestamp(0);
            } else {
                visibleTimestamp = new Timestamp(series.getStartdate().getTime());
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

                // select sum(maxpoints) from tests where problemsid='7' and visibility=1
                Number maxPoints;
                Number testCount;
                if (allTests) {
                    Object[] o = (Object[]) hibernateSession.createCriteria(Tests.class).setProjection(
                            Projections.projectionList().add(Projections.sum("maxpoints")).add(Projections.rowCount())).add(Restrictions.eq("problems.id", problems.getId())).uniqueResult();
                    maxPoints = (Number) o[0];
                    testCount = (Number) o[1];
                } else {
                    Object[] o = (Object[]) hibernateSession.createCriteria(Tests.class).setProjection(
                            Projections.projectionList().add(Projections.sum("maxpoints")).add(Projections.rowCount())).add(Restrictions.and(Restrictions.eq("problems.id", problems.getId()), Restrictions.eq("visibility", 1))).uniqueResult();
                    maxPoints = (Number) o[0];
                    testCount = (Number) o[1];
                }
                if (maxPoints == null) {
                    maxPoints = 0; // To nie powinno się nigdy zdarzyć ;).. chyba, że nie ma testu przy zadaniu?
                }
                if (testCount == null) {
                    testCount = 0; // To nie powinno się zdarzyć nigdy.
                }

                Query query;
                if (allTests == true) {
                    query = hibernateSession.createSQLQuery(""
                            + "select min(id) sid " // zapytanie zewnętrzne znajduję minimalną datę wysłania poprawnego rozwiązania dla każdego usera
                            + "from submits "
                            + "where id in ("
                            + "    select submits.id " // zapytanie wewnętrzne znajduje wszystkie id, które zdobyły maksa punktów
                            + "    from submits,results,tests "
                            + "    where submits.problemsid = :problemsId "
                            + "      and submits.id = results.submitsid "
                            + "	     and tests.id = results.testsid "
                            + "      and results.status = :statusAcc "
                            + "      and submits.state = :stateDone "
                            + "      and sdate <= :currentTimestamp "
                            + "      and sdate >= :visibleTimestamp "
                            + "      and visibleInRanking=true"
                            //+ "      and tests.visibility=1 "
                            + "    group by submits.id,usersid,sdate "
                            + "    having sum(points) = :maxPoints "
                            + "      and count(points) = :testsCount "
                            + "  ) "
                            + "group by usersid")
                            .setInteger("problemsId", problems.getId())
                            .setInteger("statusAcc", ResultsStatusEnum.ACC.getCode())
                            .setInteger("stateDone", SubmitsStateEnum.DONE.getCode())
                            .setInteger("maxPoints", maxPoints.intValue())
                            .setInteger("testCount", testCount.intValue())
                            .setTimestamp("currentTimestamp", checkTimestamp)
                            .setTimestamp("visibleTimestamp", visibleTimestamp);
                } else {
                    query = hibernateSession.createSQLQuery(""
                            + "select min(id) sid "
                            + "from submits "
                            + "where id in ("
                            + "    select submits.id "
                            + "    from submits,results,tests "
                            + "    where submits.problemsid = :problemsId "
                            + "      and submits.id = results.submitsid "
                            + "	     and tests.id = results.testsid "
                            + "      and results.status = :statusAcc "
                            + "      and submits.state = :stateDone "
                            + "      and sdate <= :currentTimestamp "
                            + "      and sdate >= :visibleTimestamp "
                            + "      and visibleInRanking=true"
                            + "	     and tests.visibility=1 " // FIXME: should be ok
                            + "    group by submits.id,usersid,sdate "
                            + "    having sum(points) = :maxPoints "
                            + "      and count(points) = :testCount "
                            + "  ) "
                            + "group by usersid")
                            .setInteger("problemsId", problems.getId())
                            .setInteger("statusAcc", ResultsStatusEnum.ACC.getCode())
                            .setInteger("stateDone", SubmitsStateEnum.DONE.getCode())
                            .setInteger("maxPoints", maxPoints.intValue())
                            .setInteger("testCount", testCount.intValue())
                            .setTimestamp("currentTimestamp", checkTimestamp)
                            .setTimestamp("visibleTimestamp", visibleTimestamp);
                }

                for (Object id : query.list()) { // tu jest zwrócona lista "zaakceptowanych" w danym momencie rozwiązań zadania
                    submits.add((Integer) id);
                }
            }
        }

        return submits;
    }
}
