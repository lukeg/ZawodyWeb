/*
 * Copyright (c) 2009-2014, ZawodyWeb Team
 * All rights reserved.
 *
 * This file is distributable under the Simplified BSD license. See the terms
 * of the Simplified BSD license in the documentation provided with this file.
 */
package pl.umk.mat.zawodyweb.compiler.classes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.*;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.log4j.Logger;
import pl.umk.mat.zawodyweb.judge.commons.TestInput;
import pl.umk.mat.zawodyweb.judge.commons.TestOutput;
import pl.umk.mat.zawodyweb.judge.commons.CompilerInterface;
import pl.umk.mat.zawodyweb.database.ResultsStatusEnum;

/**
 *
 * @author Marek Nowicki
 */
public class LanguageMAIN implements CompilerInterface {

    public static final org.apache.log4j.Logger logger = Logger.getLogger(LanguageMAIN.class);
    private Properties properties;

    @Override
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * Calculate gained points from MAIN to points in ZawodyWeb
     * @param _points string representing MAIN points
     * @param task_points points for test in ZawodyWeb
     * @param max_points max points for problem in MAIN
     * @return
     */
    private Integer calculatePoints(String _points, int task_points, Integer max_points) {
        try {
            int points = Integer.parseInt(_points);
            if (max_points == null) {
                if (points > task_points) {
                    points = task_points;
                }
                return points;
            } else {
                return points * task_points / max_points;
            }
        } catch (NumberFormatException ex) {
            logger.info("Failed to calculate gained points (" + _points + ").", ex);
        }
        return null;
    }

    /**
     * Sprawdza rozwiązanie na input
     * @param path kod źródłowy
     * @param input w formacie:
     *              <pre>c=NUMER_KONKURSU<br/>t=NUMER_ZADANIA<br/>m=MAX_POINTS</pre>
     * @return
     */
    @Override
    public TestOutput runTest(String path, TestInput input) {
        TestOutput result = new TestOutput(null);

        Integer contest_id = null;
        Integer task_id = null;
        Integer max_points = null;
        try {
            try {
                Matcher matcher = null;

                matcher = Pattern.compile("c=([0-9]+)").matcher(input.getInputText());
                if (matcher.find()) {
                    contest_id = Integer.valueOf(matcher.group(1));
                }

                matcher = Pattern.compile("t=([0-9]+)").matcher(input.getInputText());
                if (matcher.find()) {
                    task_id = Integer.valueOf(matcher.group(1));
                }

                matcher = Pattern.compile("m=([0-9]+)").matcher(input.getInputText());
                if (matcher.find()) {
                    max_points = Integer.valueOf(matcher.group(1));
                }

                if (contest_id == null) {
                    throw new IllegalArgumentException("task_id == null");
                }
                if (task_id == null) {
                    throw new IllegalArgumentException("task_id == null");
                }
            } catch (PatternSyntaxException ex) {
                throw new IllegalArgumentException(ex);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(ex);
            } catch (IllegalStateException ex) {
                throw new IllegalArgumentException(ex);
            }
        } catch (IllegalArgumentException e) {
            logger.error("Exception when parsing input", e);
            result.setStatus(ResultsStatusEnum.UNDEF.getCode());
            result.setNotes(e.getMessage());
            result.setOutputText("MAIN IllegalArgumentException");
            return result;
        }
        logger.debug("Contest id = " + contest_id);
        logger.debug("Task id = " + task_id);
        logger.debug("Max points = " + max_points);

        String loginUrl = "http://main.edu.pl/pl/login";
        String login = properties.getProperty("main_edu_pl.login");
        String password = properties.getProperty("main_edu_pl.password");

        HttpClient client = new HttpClient();

        HttpClientParams params = client.getParams();
        params.setParameter("http.useragent", "Opera/9.64 (Windows NT 6.0; U; pl) Presto/2.1.1");
        //params.setParameter("http.protocol.handle-redirects", true);
        client.setParams(params);
        /* logowanie */
        logger.debug("Logging in");
        PostMethod postMethod = new PostMethod(loginUrl);
        NameValuePair[] dataLogging = {
            new NameValuePair("auth", "1"),
            new NameValuePair("login", login),
            new NameValuePair("pass", password)
        };
        postMethod.setRequestBody(dataLogging);
        try {
            client.executeMethod(postMethod);
            if (Pattern.compile("Logowanie udane").matcher(postMethod.getResponseBodyAsString(1024 * 1024)).find() == false) {
                logger.error("Unable to login (" + login + ":" + password + ")");
                result.setStatus(ResultsStatusEnum.UNDEF.getCode());
                result.setOutputText("Logging in failed");
                postMethod.releaseConnection();
                return result;
            }
        } catch (HttpException e) {
            logger.error("Exception when logging in", e);
            result.setStatus(ResultsStatusEnum.UNDEF.getCode());
            result.setNotes(e.getMessage());
            result.setOutputText("HttpException");
            postMethod.releaseConnection();
            return result;
        } catch (IOException e) {
            logger.error("Exception when logging in", e);
            result.setStatus(ResultsStatusEnum.UNDEF.getCode());
            result.setNotes(e.getMessage());
            result.setOutputText("IOException");
            postMethod.releaseConnection();
            return result;
        }
        postMethod.releaseConnection();

        /* wchodzenie na stronę z wysyłaniem zadań i pobieranie pól z hidden */
        logger.debug("Getting submit page");
        ArrayList<Part> values = new ArrayList<Part>();
        try {
            GetMethod getMethod = new GetMethod("http://main.edu.pl/user.phtml?op=submit&m=insert&c=" + contest_id);
            client.executeMethod(getMethod);
            String response = getMethod.getResponseBodyAsString(1024 * 1024);
            getMethod.releaseConnection();

            Matcher tagMatcher = Pattern.compile("<input[^>]*>").matcher(response);
            Pattern namePattern = Pattern.compile("name\\s*=\"([^\"]*)\"");
            Pattern valuePattern = Pattern.compile("value\\s*=\"([^\"]*)\"");
            while (tagMatcher.find()) {
                Matcher matcher = null;
                String name = null;
                String value = null;

                String inputTag = tagMatcher.group();

                matcher = namePattern.matcher(inputTag);
                if (matcher.find()) {
                    name = matcher.group(1);
                }

                matcher = valuePattern.matcher(inputTag);
                if (matcher.find()) {
                    value = matcher.group(1);
                }


                if (name != null && value != null && name.equals("solution") == false) {
                    values.add(new StringPart(name, value));
                }
            }
        } catch (HttpException ex) {
            logger.error("Exception when getting submit page", ex);
            result.setStatus(ResultsStatusEnum.UNDEF.getCode());
            result.setNotes(ex.getMessage());
            result.setOutputText("IOException");
            return result;
        } catch (IOException ex) {
            logger.error("Exception when getting submit page", ex);
            result.setStatus(ResultsStatusEnum.UNDEF.getCode());
            result.setNotes(ex.getMessage());
            result.setOutputText("IOException");
            return result;
        }

        values.add(new StringPart("task", task_id.toString()));

        String filename = properties.getProperty("CODE_FILENAME");
        filename = filename.replaceAll("\\." + properties.getProperty("CODEFILE_EXTENSION") + "$", "");
        filename = filename + "." + properties.getProperty("CODEFILE_EXTENSION");
        FilePart filePart = new FilePart("solution", new ByteArrayPartSource(filename, path.getBytes()));
        values.add(filePart);

        /* wysyłanie rozwiązania */
        logger.debug("Submiting solution");
        Integer solution_id = null;
        postMethod = new PostMethod("http://main.edu.pl/user.phtml?op=submit&m=db_insert&c=" + contest_id);
        postMethod.setRequestEntity(new MultipartRequestEntity(values.toArray(new Part[0]), client.getParams()));
        try {
            try {
                client.executeMethod(postMethod);
                HttpMethod method = postMethod;

                /* check if redirect */
                Header locationHeader = postMethod.getResponseHeader("location");
                if (locationHeader != null) {
                    String redirectLocation = locationHeader.getValue();
                    GetMethod getMethod = new GetMethod(new URI(postMethod.getURI(), new URI(redirectLocation, false)).getURI());
                    client.executeMethod(getMethod);
                    method = getMethod;
                }
                BufferedReader br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                Matcher matcher = Pattern.compile("<tr id=\"rptr\">.*?</tr>", Pattern.DOTALL).matcher(sb.toString());
                if (matcher.find()) {
                    Matcher idMatcher = Pattern.compile("id=([0-9]+)").matcher(matcher.group());
                    if (idMatcher.find()) {
                        solution_id = Integer.parseInt(idMatcher.group(1));
                    }
                }
                if (solution_id == null) {
                    throw new IllegalArgumentException("solution_id == null");
                }
            } catch (HttpException e) {
                new IllegalArgumentException(e);
            } catch (IOException e) {
                new IllegalArgumentException(e);
            } catch (NumberFormatException e) {
                new IllegalArgumentException(e);
            } catch (IllegalStateException e) {
                new IllegalArgumentException(e);
            }

        } catch (IllegalArgumentException e) {
            logger.error("Exception when submiting solution", e);
            result.setStatus(ResultsStatusEnum.UNDEF.getCode());
            result.setNotes(e.getMessage());
            result.setOutputText("IllegalArgumentException");
            postMethod.releaseConnection();
            return result;
        }

        postMethod.releaseConnection();

        /* sprawdzanie statusu */
        logger.debug("Checking result for main.id=" + solution_id);
        Pattern resultRowPattern = Pattern.compile("id=" + solution_id + ".*?</tr>", Pattern.DOTALL);
        Pattern resultPattern = Pattern.compile("</td>.*?<td.*?>.*?</td>.*?<td.*?>(.*?)</td>.*?<td.*?>.*?</td>.*?<td.*?>(.*?)</td>", Pattern.DOTALL);
        result_loop:
        while (true) {
            try {
                Thread.sleep(7000);
            } catch (InterruptedException e) {
                result.setStatus(ResultsStatusEnum.UNDEF.getCode());
                result.setNotes(e.getMessage());
                result.setOutputText("InterruptedException");
                return result;
            }

            GetMethod getMethod = new GetMethod("http://main.edu.pl/user.phtml?op=zgloszenia&c=" + contest_id);
            try {
                client.executeMethod(getMethod);
                String response = getMethod.getResponseBodyAsString(1024 * 1024);
                getMethod.releaseConnection();

                Matcher matcher = resultRowPattern.matcher(response);
                // "</td>.*?<td.*?>.*?[NAZWA_ZADANIA]</td>.*?<td.*?>(.*?[STATUS])</td>.*?<td.*?>.*?</td>.*?<td.*?>(.*?[PUNKTY])</td>"
                while (matcher.find()) {
                    Matcher resultMatcher = resultPattern.matcher(matcher.group());

                    if (resultMatcher.find() && resultMatcher.groupCount() == 2) {
                        String resultType = resultMatcher.group(1);
                        if (resultType.equals("?")) {
                            continue;
                        } else if (resultType.matches("B..d kompilacji")) { // CE
                            result.setStatus(ResultsStatusEnum.CE.getCode());
                            result.setPoints(calculatePoints(resultMatcher.group(2), input.getMaxPoints(), max_points));
                        } else if (resultType.matches("Program wyw.aszczony")) { // TLE
                            result.setStatus(ResultsStatusEnum.TLE.getCode());
                            result.setPoints(calculatePoints(resultMatcher.group(2), input.getMaxPoints(), max_points));
                        } else if (resultType.matches("B..d wykonania")) { // RTE
                            result.setStatus(ResultsStatusEnum.RE.getCode());
                            result.setPoints(calculatePoints(resultMatcher.group(2), input.getMaxPoints(), max_points));
                        } else if (resultType.matches("Z.a odpowied.")) { // WA
                            result.setStatus(ResultsStatusEnum.WA.getCode());
                            result.setPoints(calculatePoints(resultMatcher.group(2), input.getMaxPoints(), max_points));
                        } else if (resultType.equals("OK")) { // AC
                            result.setStatus(ResultsStatusEnum.ACC.getCode());
                            result.setPoints(calculatePoints(resultMatcher.group(2), input.getMaxPoints(), max_points));
                        } else {
                            result.setStatus(ResultsStatusEnum.UNDEF.getCode());
                            result.setNotes("Unknown status: \"" + resultType + "\"");
                        }
                        break result_loop;
                    }
                }
            } catch (HttpException ex) {
                result.setStatus(ResultsStatusEnum.UNDEF.getCode());
                result.setNotes(ex.getMessage());
                result.setOutputText("HttpException");
                return result;
            } catch (IOException ex) {
                result.setStatus(ResultsStatusEnum.UNDEF.getCode());
                result.setNotes(ex.getMessage());
                result.setOutputText("IOException");
                return result;
            }
        }
        return result;
    }

    @Override
    public byte[] precompile(byte[] code) {
        return code;


    }

    @Override
    public String compile(byte[] code) {
        return new String(code);


    }

    @Override
    public String postcompile(String path) {
        return path;


    }

    @Override
    public void closeProgram(String path) {
    }
}
