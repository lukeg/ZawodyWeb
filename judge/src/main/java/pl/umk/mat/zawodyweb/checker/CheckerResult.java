/*
 * Copyright (c) 2009-2014, ZawodyWeb Team
 * All rights reserved.
 *
 * This file is distributable under the Simplified BSD license. See the terms
 * of the Simplified BSD license in the documentation provided with this file.
 */
package pl.umk.mat.zawodyweb.checker;

import pl.umk.mat.zawodyweb.database.CheckerErrors;

/**
 *
 * @author lukash2k
 */
public class CheckerResult {

    private int result;
    private String description;
    private int memUsed;
    private int runtime;
    private int points;

    public void setPoints(int points) {
        this.points = points;
    }

    public int getPoints() {
        return points;
    }

    public void setMemUsed(int memUsed) {
        this.memUsed = memUsed;
    }

    public void setRuntime(int runtime) {
        this.runtime = runtime;
    }

    public int getMemUsed() {
        return memUsed;
    }

    public int getRuntime() {
        return runtime;
    }


    public CheckerResult() {
        result = CheckerErrors.UNDEF;
        description = null;
    }

    public CheckerResult(int result, String description) {
        this.result = result;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public int getResult() {
        return result;
    }

    public void setDescription(String decription) {
        this.description = decription;
    }

    public void setResult(int result) {
        this.result = result;
    }
}
