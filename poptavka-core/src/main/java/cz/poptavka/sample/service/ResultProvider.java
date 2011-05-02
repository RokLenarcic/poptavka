package cz.poptavka.sample.service;

import cz.poptavka.sample.common.ResultCriteria;
import cz.poptavka.sample.domain.common.DomainObject;

import java.util.Collection;

/**
 * A Closure for deffered retrieval of collection of domain objects.
 *
 * @author Juraj Martinka
 *         Date: 1.5.11
 */
public abstract class ResultProvider<T extends DomainObject> {
    private ResultCriteria resultCriteria;

    public abstract Collection<T> getResult();


    protected ResultProvider(ResultCriteria resultCriteria) {
        this.resultCriteria = resultCriteria;
    }

    public ResultCriteria getResultCriteria() {
        return resultCriteria;
    }

    public ResultProvider setResultCriteria(ResultCriteria resultCriteria) {
        this.resultCriteria = resultCriteria;
        return this;
    }
}