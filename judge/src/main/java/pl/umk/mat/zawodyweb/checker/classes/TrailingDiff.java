package pl.umk.mat.zawodyweb.checker.classes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import pl.umk.mat.zawodyweb.database.CheckerErrors;
import pl.umk.mat.zawodyweb.checker.CheckerInterface;
import pl.umk.mat.zawodyweb.checker.CheckerResult;
import pl.umk.mat.zawodyweb.checker.TestInput;
import pl.umk.mat.zawodyweb.checker.TestOutput;
import pl.umk.mat.zawodyweb.compiler.Program;

/**
 *
 * @author lukash2k
 */
public class TrailingDiff implements CheckerInterface {

    @Override
    public CheckerResult check(Program program, TestInput input, TestOutput output) {
        TestOutput codeOutput = program.runTest(input);
        if (codeOutput.getResult() != CheckerErrors.UNDEF) {
            return new CheckerResult(codeOutput.getResult(), codeOutput.getResultDesc());
        }
        CheckerResult result = new CheckerResult();
        String codeText = codeOutput.getText();
        String rightText = output.getText();
        try {
            if (diff(codeText, rightText) == 0) {
                result.setResult(CheckerErrors.ACC);
            } else {
                result.setResult(CheckerErrors.WA);
            }
        } catch (IOException ex) {
            System.err.println("IOException at TrailingDiff." + ex.getMessage());
        }
        return result;
    }

    private int diff(String codeText, String rightText) throws IOException {
        BufferedReader right = new BufferedReader(new StringReader(rightText));
        BufferedReader code = new BufferedReader(new StringReader(codeText));
        String codeLine = code.readLine();
        String rightLine = right.readLine();
        while (codeLine != null && rightLine != null) {
            if (codeLine.trim().compareTo(rightLine.trim()) != 0) {
                return 1;
            }
            codeLine = code.readLine();
            rightLine = right.readLine();
        }
        if (codeLine != null) {
            while (codeLine != null && codeLine.trim().isEmpty()) {
                codeLine = code.readLine();
            }
        }
        if (rightLine != null) {
            while (rightLine != null && rightLine.trim().isEmpty()) {
                rightLine = right.readLine();
            }
        }
        if (codeLine == null && rightLine == null) {
            return 0;
        } else {
            return 1;
        }
    }
}
