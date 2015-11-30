package org.rakam.report.abtesting;

import com.google.common.collect.ImmutableList;
import com.google.inject.name.Named;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.rakam.analysis.JDBCPoolDataSource;
import org.rakam.report.abtesting.ABTestingReport.Goal;
import org.rakam.report.abtesting.ABTestingReport.Variant;
import org.rakam.util.JsonHelper;
import org.rakam.util.RakamException;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class ABTestingMetastore {
    private final DBI dbi;

    ResultSetMapper<ABTestingReport> mapper = (index, r, ctx) -> {
        List<Variant> variants = Arrays.asList(JsonHelper.read(r.getString(4), Variant[].class));
        Goal goals = Arrays.asList(JsonHelper.read(r.getString(5), Goal[].class)).get(0);
        return new ABTestingReport(r.getInt(1), r.getString(2), r.getString(3),
                variants, goals, JsonHelper.read(r.getString(6)));
    };

    @Inject
    public ABTestingMetastore(@Named("report.metadata.store.jdbc") JDBCPoolDataSource dataSource) {
        dbi = new DBI(dataSource);
        setup();
    }

    public void setup() {
        try(Handle handle = dbi.open()) {
            handle.createStatement("CREATE TABLE IF NOT EXISTS ab_testing (" +
                    "  id SERIAL NOT NULL," +
                    "  project VARCHAR(255) NOT NULL," +
                    "  name VARCHAR(255) NOT NULL," +
                    "  variants TEXT NOT NULL," +
                    "  goals TEXT NOT NULL," +
                    "  options TEXT," +
                    "  PRIMARY KEY (id)" +
                    "  )")
                    .execute();
        }
    }

    public List<ABTestingReport> getReports(String project) {
        try(Handle handle = dbi.open()) {
            return handle.createQuery("SELECT id, project, name, variants, goals, options FROM ab_testing WHERE project = :project")
                    .bind("project", project).map(mapper).list();
        }
    }

    public void delete(String project, int id) {
        try(Handle handle = dbi.open()) {
            handle.createStatement("DELETE FROM reports WHERE project = :project AND id = :id")
                    .bind("project", project).bind("id", id).execute();
        }
    }

    public void save(ABTestingReport report) {
        if(report.id !=1) {
            throw new RakamException("Report already has an id.", HttpResponseStatus.BAD_REQUEST);
        }
        try(Handle handle = dbi.open()) {
            handle.createStatement("INSERT INTO ab_testing (project, name, variants, goals, options) VALUES (:project, :name, :variants, :goals, :options)")
                    .bind("project", report.project)
                    .bind("name", report.name)
                    .bind("variants", report.variants)
                    .bind("goals", JsonHelper.encode(ImmutableList.of(report.goal)))
                    .bind("options", JsonHelper.encode(report.options, false))
                    .execute();
        } catch (UnableToExecuteStatementException e) {
            if(e.getCause() instanceof SQLException && ((SQLException) e.getCause()).getSQLState().equals("23505")) {
                // TODO: replace
                throw new RakamException("Report already exists", HttpResponseStatus.BAD_REQUEST);
            }
        }
    }

    public ABTestingReport get(String project, int id) {
        try(Handle handle = dbi.open()) {
            return handle.createQuery("SELECT project, name, variants, goals, options FROM ab_testing WHERE project = :project AND id = :id")
                    .bind("project", project)
                    .bind("id", id).map(mapper).first();
        }
    }

    public ABTestingReport update(ABTestingReport report) {
        if(report.id == 1) {
            throw new RakamException("Report doesn't have an id.", HttpResponseStatus.BAD_REQUEST);
        }
        try(Handle handle = dbi.open()) {
            int execute = handle.createStatement("UPDATE reports SET name = :name, variants = :variants, goals = :goals, options = :options WHERE project = :project AND id = :id")
                    .bind("project", report.project)
                    .bind("id", report.id)
                    .bind("variants", report.variants)
                    .bind("goals",  JsonHelper.encode(ImmutableList.of(report.goal)))
                    .bind("options", JsonHelper.encode(report.options, false))
                    .execute();
            if(execute == 0) {
                throw new RakamException("Report does not exist", HttpResponseStatus.BAD_REQUEST);
            }
        }
        return report;
    }
}