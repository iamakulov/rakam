package org.rakam.analysis.rule.aggregation;

import org.rakam.analysis.script.FieldScript;
import org.rakam.analysis.script.FilterScript;
import org.rakam.constant.AggregationType;
import org.rakam.constant.Analysis;

/**
 * Created by buremba on 16/01/14.
 */
public class MetricAggregationRule extends AggregationRule {
    public static final Analysis TYPE = Analysis.ANALYSIS_METRIC;

    public MetricAggregationRule(String projectId, AggregationType type, FieldScript select) {
        super(projectId, type, select);
        this.id = buildId();
    }

    public MetricAggregationRule(String projectId, AggregationType type, FieldScript select, FilterScript filters) {
        super(projectId, type, select, filters);
        this.id = buildId();
    }

    public MetricAggregationRule(String projectId, AggregationType type, FieldScript select, FilterScript filters, FieldScript groupBy) {
        super(projectId, type, select, filters, groupBy);
        this.id = buildId();
    }

    public String buildId() {
        return project+TYPE.id+type.id+(select==null ? "" : select)+(groupBy==null ? "" : groupBy)+(filters==null ? "" : filters);
    }

    public static String buildId(String project, AggregationType agg_type, FieldScript select, FilterScript filters, FieldScript groupBy) {
        return project+TYPE.id+agg_type.id+select+groupBy+filters;
    }

    @Override
    public Analysis analysisType() {
        return TYPE;
    }
}
