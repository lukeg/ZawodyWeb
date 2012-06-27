/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.mat.umk.zawodyweb.judge.tests;

import java.util.Properties;
import pl.umk.mat.zawodyweb.checker.TestInput;
import pl.umk.mat.zawodyweb.checker.TestOutput;
import pl.umk.mat.zawodyweb.compiler.CompilerInterface;
import pl.umk.mat.zawodyweb.compiler.classes.*;

/**
 *
 * @author faramir
 */
public class LMainTest {

    public static void main(String[] args) {
        CompilerInterface l = new LanguagePython();
        Properties properties = new Properties();
        properties.setProperty("uva.login", "spamz");
        properties.setProperty("uva.password", "spamz2");
        properties.setProperty("uva.max_time", "1000000");
        properties.setProperty("CODEFILE_EXTENSION", "java");
        properties.setProperty("COMPILED_DIR", "C:\\Users\\faramir\\Desktop");
        properties.setProperty("CODE_FILENAME", "MyFile.java");

        l.setProperties(properties);
        String script = l.compile(("#!/usr/bin/python\n"
                + "# -*- coding: utf-8 -*-\n"
                + "#\n"
                + "# end.\n"
                + "public class MyFile {\n"
                + "  public static void main(String[] args) {\n"
                + "#komentarz\n"
                + "    System.out.println(\"4\\n1 2 3 4\");\n"
                + "  }\n"
                + "}\n").getBytes());

        TestOutput o = l.runTest(script, new TestInput("11031", 1, 0, 0));

        System.out.println("text:" + o.getText());
        System.out.println("desc:" + o.getResultDesc());
        System.out.println("pnts:" + o.getPoints());
        System.out.println("rtme:" + o.getRuntime());
        System.out.println("rslt:" + o.getResult());
    }
}