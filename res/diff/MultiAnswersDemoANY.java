/*
 * Copyright (c) 2009-2014, ZawodyWeb Team
 * All rights reserved.
 *
 * This file is distributable under the Simplified BSD license. See the terms
 * of the Simplified BSD license in the documentation provided with this file.
 */
package pl.umk.mat.zawodyweb.checker.classes;

import pl.umk.mat.zawodyweb.checker.CheckerInterface;
import pl.umk.mat.zawodyweb.checker.CheckerResult;
import pl.umk.mat.zawodyweb.checker.TestInput;
import pl.umk.mat.zawodyweb.checker.TestOutput;
import pl.umk.mat.zawodyweb.compiler.Program;
import pl.umk.mat.zawodyweb.database.CheckerErrors;

/**
 *
 * @author lukash2k
 */
public class MultiAnswersDemoANY implements CheckerInterface {

    @Override
    public CheckerResult check(Program program, TestInput input, TestOutput output) {
        TestOutput codeOutput = program.runTest(input);
        if (codeOutput.getResult() != CheckerErrors.UNDEF) {
            return new CheckerResult(codeOutput.getResult(), codeOutput.getResultDesc());
        }
        CheckerResult result = new CheckerResult();
        result.setPoints(0);
        int i = 0;
        try {
            i = Integer.parseInt(codeOutput.getText().trim());
        } catch (Exception ex) {
            result.setResult(CheckerErrors.WA);
        }
        if (i < Integer.parseInt(input.getText())) {
            result.setResult(CheckerErrors.ACC);
            result.setPoints(input.getMaxPoints());
        } else {
            result.setResult(CheckerErrors.WA);
        }
        result.setRuntime(codeOutput.getRuntime());
        result.setMemUsed(codeOutput.getMemUsed());
        return result;
    }
}
