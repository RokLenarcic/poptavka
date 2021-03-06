package com.eprovement.poptavka.domain.common;

import com.eprovement.poptavka.domain.enums.OrderType;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Additional criteria that can be applied on the result - used primary for operations that load data from database.
 *
 * @author Juraj Martinka
 *         Date: 30.4.11
 */
public final class ResultCriteria implements Serializable {

    /**
     * Handy constant - mainly for testing purposes.
     */
    public static final ResultCriteria EMPTY_CRITERIA = new ResultCriteria.Builder().build();

    /**
     * The collection of columns by which we want to order. The columns are applied in order as returned by
     * Collection iterator.
     * Map contains pairs (propertyName, OrderType). Ordering direction is specified by OrderType. Default OrderType
     * is ascending.
     * <p/>
     * It is a full responsibility of the programmer (client) that each column specify in this collection
     * represents some property on target entity.
     * <p/>
     *
     * @see GenericServiceIntegrationTest for examples on using this criteria class.
     */
    private Map<String, OrderType> orderByColumns;


    /**
     * Maximum number of results that should be returned.
     */
    private Integer maxResults;

    /**
     * The position from which to start selection. This is usually used together with {@link #maxResults}.
     * Numbered from 0!
     */
    private Integer firstResult;


    /**
     * Checks whether criteria passed as parameter include an order by definition
     *
     * @return true if criteria has set at least one column which should be used for ordering, false otherwise.
     */
    public static boolean isOrderBySpecified(ResultCriteria resultCriteria) {
        return resultCriteria != null && resultCriteria.getOrderByColumns() != null
                && (!resultCriteria.getOrderByColumns().isEmpty());
    }


    private ResultCriteria() {
        // nonparameteric constructor for GWT
    }

    private ResultCriteria(Builder builder) {
        // fill all the atributes from builder
        this.orderByColumns = builder.orderByColumns;
        this.maxResults = builder.maxResults;
        this.firstResult = builder.firstResult;
    }


    public static class Builder {

        private Map<String, OrderType> orderByColumns = new HashMap<String, OrderType>();

        private Integer maxResults;

        private Integer firstResult;


        public Builder() {
        }

        public Builder orderByColumns(List<String> orderByColumns) {
            for (String orderByColumn : orderByColumns) {
                this.orderByColumns.put(orderByColumn, OrderType.ASC);
            }
            return this;
        }

        public Builder orderByColumns(Map<String, OrderType> orderByColumns) {
            this.orderByColumns = orderByColumns;
            return this;
        }

        public Builder maxResults(Integer maxResults) {
            this.maxResults = maxResults;
            return this;
        }

        public Builder firstResult(Integer firstResult) {
            this.firstResult = firstResult;
            return this;
        }

        public ResultCriteria build() {
            return new ResultCriteria(this);
        }
    }

    //---------------------------------- GETTERS AND SETTERS -----------------------------------------------------------

    public Map<String, OrderType> getOrderByColumns() {
        return orderByColumns;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    public Integer getFirstResult() {
        return firstResult;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ResultCriteria");
        sb.append("{orderByColumns=").append(orderByColumns);
        sb.append(", maxResults=").append(maxResults);
        sb.append(", firstResult=").append(firstResult);
        sb.append('}');
        return sb.toString();
    }
}
