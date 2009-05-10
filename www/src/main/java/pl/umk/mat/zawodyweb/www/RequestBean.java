/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.umk.mat.zawodyweb.www;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlInputText;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.ServletRequest;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Transaction;
import org.springframework.web.context.request.RequestScope;
import pl.umk.mat.zawodyweb.database.ClassesDAO;
import pl.umk.mat.zawodyweb.database.ContestsDAO;
import pl.umk.mat.zawodyweb.database.DAOFactory;
import pl.umk.mat.zawodyweb.database.LanguagesDAO;
import pl.umk.mat.zawodyweb.database.LanguagesProblemsDAO;
import pl.umk.mat.zawodyweb.database.ProblemsDAO;
import pl.umk.mat.zawodyweb.database.SeriesDAO;
import pl.umk.mat.zawodyweb.database.TestsDAO;
import pl.umk.mat.zawodyweb.database.UsersDAO;
import pl.umk.mat.zawodyweb.database.hibernate.HibernateUtil;
import pl.umk.mat.zawodyweb.database.pojo.Classes;
import pl.umk.mat.zawodyweb.database.pojo.Contests;
import pl.umk.mat.zawodyweb.database.pojo.Languages;
import pl.umk.mat.zawodyweb.database.pojo.LanguagesProblems;
import pl.umk.mat.zawodyweb.database.pojo.Problems;
import pl.umk.mat.zawodyweb.database.pojo.Series;
import pl.umk.mat.zawodyweb.database.pojo.Tests;
import pl.umk.mat.zawodyweb.database.pojo.Users;

/**
 *
 * @author slawek
 */
public class RequestBean {

    private final ResourceBundle messages = ResourceBundle.getBundle("pl.umk.mat.zawodyweb.www.Messages");
    private SessionBean sessionBean;
    private Users newUser = new Users();
    private String repPasswd;
    private Contests editedContest;
    private Series editedSeries;
    private Problems editedProblem;
    private Tests editedTest;
    private List<Contests> contests = null;
    private List<Series> contestsSeries = null;
    private List<Problems> seriesProblems = null;
    private List<Classes> diffClasses = null;
    private List<Languages> languages = null;
    private List<Problems> currentContestProblems = null;
    private Integer temporaryContestId;
    private Integer temporarySeriesId;
    private Integer temporaryProblemId;
    private Integer temporaryClassId;
    private Integer[] temporaryLanguagesIds;
    private String dummy;

    /**
     * @return the sessionBean
     */
    public SessionBean getSessionBean() {
        return sessionBean;
    }

    /**
     * @param sessionBean the sessionBean to set
     */
    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    /**
     * @return the newUser
     */
    public Users getNewUser() {
        return newUser;
    }

    /**
     * @return the repPasswd
     */
    public String getRepPasswd() {
        return repPasswd;
    }

    /**
     * @param repPasswd the repPasswd to set
     */
    public void setRepPasswd(String repPasswd) {
        this.repPasswd = repPasswd;
    }

    public List<Contests> getContests() {
        if (contests == null) {
            Transaction t = HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();

            try {
                ContestsDAO dao = DAOFactory.DEFAULT.buildContestsDAO();
                contests = dao.findAll();
                t.commit();
            } catch (Exception e) {
                t.rollback();
            }
        }

        return contests;
    }

    public List<Classes> getDiffClasses() {
        if (diffClasses == null) {
            Transaction t = HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();

            try {
                ClassesDAO dao = DAOFactory.DEFAULT.buildClassesDAO();
                LanguagesDAO ldao = DAOFactory.DEFAULT.buildLanguagesDAO();

                diffClasses = dao.findAll();
                List<Languages> tmp = ldao.findAll();
                for(Languages l: tmp){
                    diffClasses.remove(l.getClasses());
                }
                t.commit();
            } catch (Exception e) {
                t.rollback();
            }
        }

        return diffClasses;
    }

    public List<Languages> getLanguages() {
        if (languages == null) {
            Transaction t = HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();

            try {
                LanguagesDAO dao = DAOFactory.DEFAULT.buildLanguagesDAO();
                languages = dao.findAll();
                t.commit();
            } catch (Exception e) {
                t.rollback();
            }
        }

        return languages;
    }

    public List<Series> getContestsSeries() {
        if (contestsSeries == null) {
            Transaction t = HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();

            try {
                SeriesDAO dao = DAOFactory.DEFAULT.buildSeriesDAO();
                if (temporaryContestId == null) {
                    temporaryContestId = getTemporaryContestId();
                }
                contestsSeries = dao.findByContestsid(temporaryContestId);
                t.commit();
            } catch (Exception e) {
                t.rollback();
            }
        }

        return contestsSeries;
    }

    public List<Problems> getSeriesProblems() {
        if (seriesProblems == null) {
            Transaction t = HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();

            try {
                ProblemsDAO dao = DAOFactory.DEFAULT.buildProblemsDAO();
                seriesProblems = dao.findBySeriesid(temporarySeriesId);
                t.commit();
            } catch (Exception e) {
                t.rollback();
            }
        }

        return seriesProblems;
    }

    public List<Problems> getCurrentContestProblems() {      
         return null;
    }

    /**
     * @return the editedContest
     */
    public Contests getEditedContest() {
        FacesContext context = FacesContext.getCurrentInstance();
        int contestId = 0;
        try {
            contestId = Integer.parseInt(context.getExternalContext().getRequestParameterMap().get("id"));
        } catch (Exception e) {
            contestId = 0;
        }

        if (editedContest != null) {
            return editedContest;
        }

        if (contestId == 0) {
            editedContest = new Contests();
        } else {
            Transaction t = HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
            ContestsDAO dao = DAOFactory.DEFAULT.buildContestsDAO();
            editedContest = dao.getById(contestId);
            t.commit();
        }

        return editedContest;
    }

    public Integer getTemporaryContestId() {
        return temporaryContestId;
    }

    public void setTemporaryContestId(Integer id) {
        temporaryContestId = id;
    }

    public Integer getTemporarySeriesId() {
        return temporarySeriesId;
    }

    public void setTemporarySeriesId(Integer id) {
        temporarySeriesId = id;
    }

    public Integer getTemporaryProblemId() {
        return temporaryProblemId;
    }

    public void setTemporaryProblemId(Integer temporaryProblemId) {
        this.temporaryProblemId = temporaryProblemId;
    }

    public Integer getTemporaryClassId() {
        return temporaryClassId;
    }

    public void setTemporaryClassId(Integer temporaryClassId) {
        this.temporaryClassId = temporaryClassId;
    }

    public Integer[] getTemporaryLanguagesIds() {
        return temporaryLanguagesIds;
    }

    public void setTemporaryLanguagesIds(Integer[] temporaryLanguagesIds) {
        this.temporaryLanguagesIds = temporaryLanguagesIds;
    }

    public Series getEditedSeries() {
        FacesContext context = FacesContext.getCurrentInstance();
        int seriesId = 0;
        try {
            seriesId = Integer.parseInt(context.getExternalContext().getRequestParameterMap().get("id"));
        } catch (Exception e) {
            seriesId = 0;
        }

        if (editedSeries != null) {
            return editedSeries;
        }

        if (seriesId == 0) {
            editedSeries = new Series();
        } else {
            Transaction t = HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
            SeriesDAO dao = DAOFactory.DEFAULT.buildSeriesDAO();
            editedSeries = dao.getById(seriesId);
            t.commit();
        }

        return editedSeries;
    }

    public Problems getEditedProblem() {
        FacesContext context = FacesContext.getCurrentInstance();
        int problemId = 0;
        try {
            problemId = Integer.parseInt(context.getExternalContext().getRequestParameterMap().get("id"));
        } catch (Exception e) {
            problemId = 0;
        }

        if (editedProblem != null) {
            return editedProblem;
        }

        if (problemId == 0) {
            editedProblem = new Problems();
        } else {
            Transaction t = HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
            ProblemsDAO dao = DAOFactory.DEFAULT.buildProblemsDAO();
            editedProblem = dao.getById(problemId);
            t.commit();
        }

        return editedProblem;
    }

    public Tests getEditedTest() {
        FacesContext context = FacesContext.getCurrentInstance();
        int testId = 0;
        try {
            testId = Integer.parseInt(context.getExternalContext().getRequestParameterMap().get("id"));
        } catch (Exception e) {
            testId = 0;
        }

        if (editedTest != null) {
            return editedTest;
        }

        if (testId == 0) {
            editedTest = new Tests();
        } else {
            Transaction t = HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
            TestsDAO dao = DAOFactory.DEFAULT.buildTestsDAO();
            editedTest = dao.getById(testId);
            t.commit();
        }

        return editedTest;
    }

    public String registerUser() {
        Transaction t = HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
        try {
            UsersDAO dao = DAOFactory.DEFAULT.buildUsersDAO();
            dao.save(newUser);
            t.commit();
        } catch (Exception e) {
            t.rollback();
            FacesContext context = FacesContext.getCurrentInstance();
            String summary = messages.getString("login_exists");
            WWWHelper.AddMessage(context, FacesMessage.SEVERITY_ERROR, "formRegister:login", summary, null);
            newUser.setPass(StringUtils.EMPTY);
            return null;
        }

        return "login";
    }

    public String saveContest() {
        Transaction t = HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
        try {
            ContestsDAO dao = DAOFactory.DEFAULT.buildContestsDAO();
            if (editedContest.getId() == 0) {
                editedContest.setId(null);
            }

            dao.saveOrUpdate(editedContest);
            t.commit();
        } catch (Exception e) {
            t.rollback();
            FacesContext context = FacesContext.getCurrentInstance();
            String summary = String.format("%s: %s", messages.getString("unexpected_error"), e.getLocalizedMessage());
            WWWHelper.AddMessage(context, FacesMessage.SEVERITY_ERROR, "formEditContest:save", summary, null);
            return null;
        }

        return "start";
    }

    public String saveSeries() {
        Transaction t = HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
        try {
            ContestsDAO cdao = DAOFactory.DEFAULT.buildContestsDAO();

            editedSeries.setContests(cdao.getById(temporaryContestId));

            SeriesDAO dao = DAOFactory.DEFAULT.buildSeriesDAO();
            if (editedSeries.getId() == 0) {
                editedSeries.setId(null);
            }

            dao.saveOrUpdate(editedSeries);
            t.commit();
        } catch (Exception e) {
            t.rollback();
            FacesContext context = FacesContext.getCurrentInstance();
            String summary = String.format("%s: %s", messages.getString("unexpected_error"), e.getLocalizedMessage());
            WWWHelper.AddMessage(context, FacesMessage.SEVERITY_ERROR, "formEditSeries:save", summary, null);
            return null;
        }

        return "start";
    }

    public String saveProblem() {
        Transaction t = HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
        try {
            ProblemsDAO dao = DAOFactory.DEFAULT.buildProblemsDAO();
            SeriesDAO sdao = DAOFactory.DEFAULT.buildSeriesDAO();
            LanguagesDAO ldao = DAOFactory.DEFAULT.buildLanguagesDAO();
            LanguagesProblemsDAO lpdao = DAOFactory.DEFAULT.buildLanguagesProblemsDAO();

            if (editedProblem.getId() == 0) {
                editedProblem.setId(null);
            }
            editedProblem.setSeries(sdao.getById(temporarySeriesId));

            for (Integer lid : temporaryLanguagesIds) {
                LanguagesProblems lp = new LanguagesProblems();
                lp.setId(null);
                lp.setLanguages(ldao.getById(lid));
                lp.setProblems(editedProblem);
                lpdao.saveOrUpdate(lp);
            }

            dao.saveOrUpdate(editedProblem);
            t.commit();
        } catch (Exception e) {
            t.rollback();
            FacesContext context = FacesContext.getCurrentInstance();
            String summary = String.format("%s: %s", messages.getString("unexpected_error"), e.getLocalizedMessage());
            WWWHelper.AddMessage(context, FacesMessage.SEVERITY_ERROR, "formEditProblem:save", summary, null);
            return null;
        }

        return "start";
    }

    public String saveTest() {
        Transaction t = HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
        try {
            TestsDAO dao = DAOFactory.DEFAULT.buildTestsDAO();
            ProblemsDAO pdao = DAOFactory.DEFAULT.buildProblemsDAO();

            if (editedTest.getId() == 0) {
                editedTest.setId(null);
            }

            editedTest.setProblems(pdao.getById(temporaryProblemId));

            dao.saveOrUpdate(editedTest);
            t.commit();
        } catch (Exception e) {
            t.rollback();
            FacesContext context = FacesContext.getCurrentInstance();
            String summary = String.format("%s: %s", messages.getString("unexpected_error"), e.getLocalizedMessage());
            WWWHelper.AddMessage(context, FacesMessage.SEVERITY_ERROR, "formEditTest:save", summary, null);
            return null;
        }

        return "start";
    }

    /**
     * Validates component with captcha text entered.
     *
     * @param context
     *            faces context in which component resides
     * @param component
     *            component to be validated
     * @param obj
     *            data entered in component
     */
    public void validateCaptcha(FacesContext context, UIComponent component, Object obj) {
        String captcha = (String) obj;
        ExternalContext extContext = context.getExternalContext();
        String tmp = (String) extContext.getSessionMap().get("captchaKey");

        if (!captcha.toLowerCase().equals(tmp.toLowerCase())) {
            ((HtmlInputText) component).setValid(false);
            String summary = messages.getString("bad_captcha");
            WWWHelper.AddMessage(context, FacesMessage.SEVERITY_ERROR, component, summary, null);
        }
    }

    /**
     * @return the dummy
     */
    public String getDummy() {
        return dummy;
    }

    /**
     * @param dummy the dummy to set
     */
    public void setDummy(String dummy) {
        this.dummy = dummy;
    }
}
